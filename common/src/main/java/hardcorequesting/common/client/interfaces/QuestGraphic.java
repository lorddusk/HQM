package hardcorequesting.common.client.interfaces;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.edit.TextMenu;
import hardcorequesting.common.client.interfaces.widget.LargeButton;
import hardcorequesting.common.client.interfaces.widget.ScrollBar;
import hardcorequesting.common.event.EventTrigger;
import hardcorequesting.common.network.GeneralUsage;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.quests.task.TaskType;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.OPBookHelper;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public final class QuestGraphic extends Graphic {
    
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
    
    private final UUID playerId;
    private final Quest quest;
    private QuestTask<?> selectedTask;
    
    private final ScrollBar descriptionScroll;
    private final ScrollBar taskDescriptionScroll;
    private final ScrollBar taskScroll;
    private List<FormattedText> cachedDescription;
    
    {
        for (final TaskType taskType : TaskType.values()) {
            addButton(new LargeButton(taskType.getLangKeyName(), taskType.getLangKeyDescription(), 185 + (taskType.ordinal() % 2) * 65, 50 + (taskType.ordinal() / 2) * 20) {
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
        addScrollBar(descriptionScroll = new ScrollBar(155, 28, 64, 249, 102, START_X) {
            @Override
            public boolean isVisible(GuiBase gui) {
                return getCachedDescription(gui).size() > VISIBLE_DESCRIPTION_LINES;
            }
        });
        addScrollBar(taskDescriptionScroll = new ScrollBar(312, 18, 64, 249, 102, TASK_DESCRIPTION_X) {
            @Override
            public boolean isVisible(GuiBase gui) {
                return selectedTask != null && selectedTask.getCachedLongDescription(gui).size() > VISIBLE_DESCRIPTION_LINES;
            }
        });
        
        addScrollBar(taskScroll = new ScrollBar(155, 100, 29, 242, 102, START_X) {
            @Override
            public boolean isVisible(GuiBase gui) {
                return quest.getTasks().size() > VISIBLE_TASKS && getVisibleTasks(gui) > VISIBLE_TASKS;
            }
        });
    }
    
    public QuestGraphic(UUID playerId, Quest quest) {
        this.playerId = playerId;
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
        
        super.draw(matrices, gui, player, mX, mY);
        
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
            
            quest.getRewards().onClick(gui, player, mX, mY);
            
            if (selectedTask != null) {
                selectedTask.getGraphic().onClick(gui, player, mX, mY, b);
            }
            
            super.onClick(gui, player, mX, mY, b);
            
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
    
    @Override
    public void onDrag(GuiQuestBook gui, int mX, int mY, int b) {
        super.onDrag(gui, mX, mY, b);
        if (selectedTask != null)
            selectedTask.getGraphic().onDrag(gui, mX, mY, b);
    }
    
    @Override
    public void onRelease(GuiQuestBook gui, int mX, int mY, int b) {
        super.onRelease(gui, mX, mY, b);
        if (selectedTask != null)
            selectedTask.getGraphic().onRelease(gui, mX, mY, b);
    }
    
    @Override
    public void onScroll(GuiQuestBook gui, double x, double y, double scroll) {
        super.onScroll(gui, x, y, scroll);
        if (selectedTask != null)
            selectedTask.getGraphic().onScroll(gui, x, y, scroll);
    }
    
    public void onOpen(Player player) {
        if (selectedTask == null) {
            for (QuestTask<?> task : quest.getTasks()) {
                if (!task.isCompleted(playerId)) {
                    selectedTask = task;
                    break;
                }
            }
        }
        
        if (selectedTask == null && quest.getTasks().size() > 0)
            selectedTask = quest.getTasks().get(0);
        
        QuestingDataManager.getInstance().getQuestingData(playerId).selectedQuestId = quest.getQuestId();
        QuestingDataManager.getInstance().getQuestingData(playerId).selectedTask = selectedTask == null ? -1 : selectedTask.getId();
        if (selectedTask != null) {
            GeneralUsage.sendBookSelectTaskUpdate(selectedTask);
        }
        
        EventTrigger.instance().onQuestSelected(new EventTrigger.QuestSelectedEvent(player, quest.getQuestId()));
    }
    
}
