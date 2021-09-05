package hardcorequesting.common.client.interfaces;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.ClientChange;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.edit.IntInputMenu;
import hardcorequesting.common.client.interfaces.edit.TextMenu;
import hardcorequesting.common.client.interfaces.widget.LargeButton;
import hardcorequesting.common.client.interfaces.widget.ScrollBar;
import hardcorequesting.common.event.EventTrigger;
import hardcorequesting.common.network.GeneralUsage;
import hardcorequesting.common.network.NetworkManager;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestingData;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.quests.data.QuestData;
import hardcorequesting.common.quests.task.DeathTask;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.quests.task.TaskType;
import hardcorequesting.common.quests.task.item.ConsumeItemTask;
import hardcorequesting.common.quests.task.reputation.KillReputationTask;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.OPBookHelper;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class QuestGraphic extends Graphic {
    
    private static final int VISIBLE_DESCRIPTION_LINES = 7;
    private static final int VISIBLE_TASKS = 3;
    //region pixelinfo
    private static final int START_X = Quest.START_X;
    private static final int TEXT_HEIGHT = 9;
    private static final int TASK_LABEL_START_Y = 100;
    private static final int TASK_MARGIN = 2;
    private static final int TITLE_START_Y = 15;
    private static final int DESCRIPTION_START_Y = 30;
    private static final int TASK_DESCRIPTION_X = 180;
    private static final int TASK_DESCRIPTION_Y = 20;
    
    private final Quest quest;
    private QuestTask<?> selectedTask;
    
    private final List<LargeButton> buttons = new ArrayList<>();
    private final ScrollBar descriptionScroll;
    private final ScrollBar taskDescriptionScroll;
    private final ScrollBar taskScroll;
    private final List<ScrollBar> scrollBars = new ArrayList<>();
    private List<FormattedText> cachedDescription;
    
    {
        buttons.add(new LargeButton("hqm.quest.manualSubmit", 185, 200) {
            @Override
            public boolean isEnabled(GuiBase gui, Player player) {
                return selectedTask.allowManual();
            }
            
            @Override
            public boolean isVisible(GuiBase gui, Player player) {
                return selectedTask != null && selectedTask.allowManual() && !selectedTask.isCompleted(player);
            }
            
            @Override
            public void onClick(GuiBase gui, Player player) {
                NetworkManager.sendToServer(ClientChange.UPDATE_TASK.build(selectedTask));
            }
        });
        
        buttons.add(new LargeButton("hqm.quest.manualDetect", 185, 200) {
            @Override
            public boolean isEnabled(GuiBase gui, Player player) {
                return selectedTask.allowDetect();
            }
            
            @Override
            public boolean isVisible(GuiBase gui, Player player) {
                return selectedTask != null && selectedTask.allowDetect() && !selectedTask.isCompleted(player);
            }
            
            @Override
            public void onClick(GuiBase gui, Player player) {
                NetworkManager.sendToServer(ClientChange.UPDATE_TASK.build(selectedTask));
            }
        });
        
        buttons.add(new LargeButton("hqm.quest.requirement", 185, 200) {
            @Override
            public boolean isEnabled(GuiBase gui, Player player) {
                return true;
            }
            
            @Override
            public boolean isVisible(GuiBase gui, Player player) {
                return selectedTask != null && selectedTask instanceof DeathTask && Quest.canQuestsBeEdited();
            }
            
            @Override
            public void onClick(GuiBase gui, Player player) {
                DeathTask task = (DeathTask) selectedTask;
                IntInputMenu.display(gui, player, "hqm.deathTask.reqDeathCount", task.getDeathsRequired(), task::setDeaths);
            }
        });
        
        buttons.add(new LargeButton("hqm.quest.requirement", 250, 95) {
            @Override
            public boolean isEnabled(GuiBase gui, Player player) {
                return true;
            }
            
            @Override
            public boolean isVisible(GuiBase gui, Player player) {
                return selectedTask != null && selectedTask instanceof KillReputationTask && Quest.canQuestsBeEdited();
            }
            
            @Override
            public void onClick(GuiBase gui, Player player) {
                KillReputationTask task = (KillReputationTask) selectedTask;
                IntInputMenu.display(gui, player, "hqm.mobTask.reqKills", task.getKillsRequirement(), task::setKills);
            }
        });
        
        
        buttons.add(new LargeButton("hqm.quest.selectTask", 250, 200) {
            @Override
            public boolean isEnabled(GuiBase gui, Player player) {
                QuestingData data = QuestingDataManager.getInstance().getQuestingData(player);
                if (data != null && data.selectedQuestId != null && data.selectedQuestId.equals(quest.getQuestId())) {
                    return data.selectedTask != selectedTask.getId();
                }
                return false;
            }
            
            @Override
            public boolean isVisible(GuiBase gui, Player player) {
                return selectedTask instanceof ConsumeItemTask && !selectedTask.isCompleted(player);
            }
            
            @Override
            public void onClick(GuiBase gui, Player player) {
                //update locally too, then we don't have to refresh all the data(i.e. the server won't notify us about the change we already know about)
                QuestingDataManager.getInstance().getQuestingData(player).selectedQuestId = quest.getQuestId();
                QuestingDataManager.getInstance().getQuestingData(player).selectedTask = selectedTask.getId();
                
                player.displayClientMessage(new TranslatableComponent("tile.hqm:item_barrel.selectedTask", selectedTask.getDescription()).withStyle(ChatFormatting.GREEN), false);
                
                //NetworkManager.sendToServer(ClientChange.SELECT_QUEST.build(selectedTask));
                GeneralUsage.sendBookSelectTaskUpdate(selectedTask);
            }
        });
        
        for (final TaskType taskType : TaskType.values()) {
            buttons.add(new LargeButton(taskType.getLangKeyName(), taskType.getLangKeyDescription(), 185 + (taskType.ordinal() % 2) * 65, 50 + (taskType.ordinal() / 2) * 20) {
                @Override
                public boolean isEnabled(GuiBase gui, Player player) {
                    return true;
                }
                
                @Override
                public boolean isVisible(GuiBase gui, Player player) {
                    return Quest.canQuestsBeEdited() && selectedTask == null && ((GuiQuestBook) gui).getCurrentMode() == EditMode.TASK;
                }
                
                @Override
                public void onClick(GuiBase gui, Player player) {
                    taskType.addTask(quest);
                }
            });
        }
    }
    
    {
        scrollBars.add(descriptionScroll = new ScrollBar(155, 28, 64, 249, 102, START_X) {
            @Override
            public boolean isVisible(GuiBase gui) {
                return getCachedDescription(gui).size() > VISIBLE_DESCRIPTION_LINES;
            }
        });
        scrollBars.add(taskDescriptionScroll = new ScrollBar(312, 18, 64, 249, 102, TASK_DESCRIPTION_X) {
            @Override
            public boolean isVisible(GuiBase gui) {
                return selectedTask != null && selectedTask.getCachedLongDescription(gui).size() > VISIBLE_DESCRIPTION_LINES;
            }
        });
        
        scrollBars.add(taskScroll = new ScrollBar(155, 100, 29, 242, 102, START_X) {
            @Override
            public boolean isVisible(GuiBase gui) {
                return quest.getTasks().size() > VISIBLE_TASKS && getVisibleTasks(gui) > VISIBLE_TASKS;
            }
        });
    }
    
    public QuestGraphic(Quest quest) {
        this.quest = quest;
    }
    
    private List<FormattedText> getCachedDescription(GuiBase gui) {
        if (cachedDescription == null) {
            cachedDescription = gui.getLinesFromText(Translator.plain(quest.getDescription()), 0.7F, 130);
        }
        return cachedDescription;
    }
    
    @Override
    public void draw(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY) {
        if (!Quest.canQuestsBeEdited() && selectedTask != null && !selectedTask.isVisible(player)) {
            if (quest.getTasks().size() > 0) {
                selectedTask = quest.getTasks().get(0);
            } else {
                selectedTask = null;
            }
        }
        
        gui.drawString(matrices, Translator.plain(quest.getName()), START_X, TITLE_START_Y, 0x404040);
        
        int startLine = descriptionScroll.isVisible(gui) ? Math.round((getCachedDescription(gui).size() - VISIBLE_DESCRIPTION_LINES) * descriptionScroll.getScroll()) : 0;
        gui.drawString(matrices, getCachedDescription(gui), startLine, VISIBLE_DESCRIPTION_LINES, START_X, DESCRIPTION_START_Y, 0.7F, 0x404040);
        
        int id = 0;
        int start = taskScroll.isVisible(gui) ? Math.round((getVisibleTasks(gui) - VISIBLE_TASKS) * taskScroll.getScroll()) : 0;
        int end = Math.min(start + VISIBLE_TASKS, quest.getTasks().size());
        for (int i = start; i < end; i++) {
            QuestTask<?> task = quest.getTasks().get(i);
            boolean isVisible = task.isVisible(player);
            if (isVisible || Quest.canQuestsBeEdited()) {
                boolean completed = task.isCompleted(player);
                int yPos = getTaskY(gui, id);
                boolean inBounds = gui.inBounds(START_X, yPos, gui.getStringWidth(task.getDescription()), TEXT_HEIGHT, mX, mY);
                boolean isSelected = task == selectedTask;
                gui.drawString(matrices, Translator.plain(task.getDescription()), START_X, yPos, completed ? isSelected ? inBounds ? 0x40BB40 : 0x40A040 : inBounds ? 0x10A010 : 0x107010 : isSelected ? inBounds ? 0xAAAAAA : 0x888888 : inBounds ? 0x666666 : isVisible ? 0x404040 : 0xDDDDDD);
                
                id++;
            }
        }
        
        for (LargeButton button : buttons) {
            button.draw(matrices, gui, player, mX, mY);
        }
        
        super.draw(matrices, gui, player, mX, mY);
        
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
        for (ScrollBar scrollBar : scrollBars) {
            scrollBar.draw(matrices, gui);
        }
        
        quest.getRewards().draw(matrices, gui, player, mX, mY, quest.getQuestData(player));
        
        if (selectedTask != null) {
            List<FormattedText> description = selectedTask.getCachedLongDescription(gui);
            int taskStartLine = taskDescriptionScroll.isVisible(gui) ? Math.round((description.size() - VISIBLE_DESCRIPTION_LINES) * taskDescriptionScroll.getScroll()) : 0;
            gui.drawString(matrices, description, taskStartLine, VISIBLE_DESCRIPTION_LINES, TASK_DESCRIPTION_X, TASK_DESCRIPTION_Y, 0.7F, 0x404040);
            
            selectedTask.getGraphic().draw(matrices, gui, player, mX, mY);
        } else if (Quest.canQuestsBeEdited() && gui.getCurrentMode() == EditMode.TASK) {
            gui.drawString(matrices, gui.getLinesFromText(Translator.translatable("hqm.quest.createTasks"), 0.7F, 130), 180, 20, 0.7F, 0x404040);
        }
    }
    
    @Override
    public void drawTooltip(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY) {
        super.drawTooltip(matrices, gui, player, mX, mY);
    
        if (selectedTask != null) {
            selectedTask.getGraphic().drawTooltip(matrices, gui, player, mX, mY);
        }
    
        for (LargeButton button : buttons) {
            button.renderTooltip(matrices, gui, player, mX, mY);
        }
    
        quest.getRewards().drawTooltips(matrices, gui, player, mX, mY, quest.getQuestData(player));
    }
    
    private int getVisibleTasks(GuiBase gui) {
        if (Quest.canQuestsBeEdited()) {
            return quest.getTasks().size();
        }
        
        int count = 0;
        for (QuestTask<?> task : quest.getTasks()) {
            if (task.isVisible(((GuiQuestBook) gui).getPlayer())) {
                count++;
            }
        }
        return count;
    }
    
    private int getTaskY(GuiQuestBook gui, int id) {
        return TASK_LABEL_START_Y + id * (TEXT_HEIGHT + TASK_MARGIN);
    }
    
    @Override
    public void onClick(GuiQuestBook gui, Player player, int mX, int mY, int b) {
        if (b == 1) {
            gui.loadMap();
        } else {
            int id = 0;
            int start = taskScroll.isVisible(gui) ? Math.round((getVisibleTasks(gui) - VISIBLE_TASKS) * taskScroll.getScroll()) : 0;
            int end = Math.min(start + VISIBLE_TASKS, quest.getTasks().size());
            for (int i = start; i < end; i++) {
                QuestTask<?> task = quest.getTasks().get(i);
                if (task.isVisible(player) || Quest.canQuestsBeEdited()) {
                    if (gui.inBounds(START_X, getTaskY(gui, id), gui.getStringWidth(task.getDescription()), TEXT_HEIGHT, mX, mY)) {
                        if (gui.isOpBook && Screen.hasShiftDown()) {
                            OPBookHelper.reverseTaskCompletion(task, player);
                            return;
                        }
                        if (Quest.canQuestsBeEdited() && gui.getCurrentMode() == EditMode.TASK) {
                            gui.setCurrentMode(EditMode.NORMAL);
                        }
                        if (Quest.canQuestsBeEdited() && (gui.getCurrentMode() == EditMode.RENAME || gui.getCurrentMode() == EditMode.DELETE)) {
                            if (gui.getCurrentMode() == EditMode.RENAME) {
                                TextMenu.display(gui, player, task.getDescription(), true,
                                        task::setDescription);
                            } else if (gui.getCurrentMode() == EditMode.DELETE) {
                                if (i + 1 < quest.getTasks().size()) {
                                    quest.getTasks().get(i + 1).clearRequirements();
                                    
                                    if (i > 0) {
                                        quest.getTasks().get(i + 1).addRequirement(quest.getTasks().get(i - 1));
                                    }
                                }
                                if (selectedTask == task) {
                                    selectedTask = null;
                                }
                                
                                task.onDelete();
    
                                quest.getTasks().remove(i);
                                quest.nextTaskId = 0;
                                for (QuestTask<?> questTask : quest.getTasks()) {
                                    questTask.updateId();
                                }
                                
                                quest.getQuestData(player).clearTaskData(quest);
                                SaveHelper.add(EditType.TASK_REMOVE);
                            }
                        } else if (task == selectedTask) {
                            selectedTask = null;
                        } else {
                            selectedTask = task;
                            taskDescriptionScroll.resetScroll();
                        }
                        break;
                    }
                    
                    id++;
                }
            }
            
            for (ScrollBar scrollBar : scrollBars) {
                scrollBar.onClick(gui, mX, mY);
            }
            
            quest.getRewards().onClick(gui, player, mX, mY);
            
            if (selectedTask != null) {
                selectedTask.getGraphic().onClick(gui, player, mX, mY, b);
            }
            
            
            for (LargeButton button : buttons) {
                if (button.inButtonBounds(gui, mX, mY) && button.isVisible(gui, player) && button.isEnabled(gui, player)) {
                    button.onClick(gui, player);
                    break;
                }
            }
            
            if (gui.getCurrentMode() == EditMode.RENAME) {
                if (gui.inBounds(START_X, TITLE_START_Y, 140, TEXT_HEIGHT, mX, mY)) {
                    TextMenu.display(gui, player, quest.getName(), true, quest::setName);
                } else if (gui.inBounds(START_X, DESCRIPTION_START_Y, 130, (int) (VISIBLE_DESCRIPTION_LINES * TEXT_HEIGHT * 0.7), mX, mY)) {
                    TextMenu.display(gui, player, quest.getDescription(), false, description -> {
                        cachedDescription = null;
                        quest.setDescription(description);
                    });
                } else if (selectedTask != null && gui.inBounds(TASK_DESCRIPTION_X, TASK_DESCRIPTION_Y, 130, (int) (VISIBLE_DESCRIPTION_LINES * TEXT_HEIGHT * 0.7), mX, mY)) {
                    TextMenu.display(gui, player, selectedTask.getLongDescription(), false,
                            selectedTask::setLongDescription);
                }
            }
            
            if (Quest.canQuestsBeEdited() && selectedTask != null && gui.getCurrentMode() == EditMode.TASK) {
                selectedTask = null;
            }
        }
    }
    
    public void onDrag(GuiQuestBook gui, Player player, int mX, int mY, int b) {
        for (ScrollBar scrollBar : scrollBars) {
            scrollBar.onDrag(gui, mX, mY);
        }
    }
    
    public void onRelease(GuiQuestBook gui, Player player, int mX, int mY, int b) {
        for (ScrollBar scrollBar : scrollBars) {
            scrollBar.onRelease(gui, mX, mY);
        }
    }
    
    public void onScroll(GuiQuestBook gui, double x, double y, double scroll) {
        for (ScrollBar scrollBar : scrollBars) {
            scrollBar.onScroll(gui, x, y, scroll);
        }
    }
    
    public void onOpen(GuiQuestBook gui, Player player) {
        if (selectedTask == null) {
            for (QuestTask<?> task : quest.getTasks()) {
                if (!task.isCompleted(player)) {
                    selectedTask = task;
                    break;
                }
            }
        }
        
        if (selectedTask == null && quest.getTasks().size() > 0)
            selectedTask = quest.getTasks().get(0);
        
        QuestingDataManager.getInstance().getQuestingData(player).selectedQuestId = quest.getQuestId();
        QuestingDataManager.getInstance().getQuestingData(player).selectedTask = selectedTask == null ? -1 : selectedTask.getId();
        if (selectedTask != null) {
            //NetworkManager.sendToServer(ClientChange.SELECT_QUEST.build(selectedTask));
            GeneralUsage.sendBookSelectTaskUpdate(selectedTask);
        }
        
        EventTrigger.instance().onQuestSelected(new EventTrigger.QuestSelectedEvent(player, quest.getQuestId()));
    }
    
}
