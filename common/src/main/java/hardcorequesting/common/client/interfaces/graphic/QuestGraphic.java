package hardcorequesting.common.client.interfaces.graphic;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenuCommandEditor;
import hardcorequesting.common.client.interfaces.edit.TextMenu;
import hardcorequesting.common.client.interfaces.graphic.task.TaskGraphic;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public final class QuestGraphic extends EditableGraphic {
    
    private static final int VISIBLE_DESCRIPTION_LINES = 7;
    private static final int VISIBLE_TASKS = 3;
    //region pixelinfo
    public static final int START_X = 20;
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
    private TaskGraphic taskGraphic;
    
    private final QuestRewardsGraphic rewardsGraphic;
    
    private final ScrollBar descriptionScroll;
    private final ScrollBar taskDescriptionScroll;
    private final ScrollBar taskScroll;
    private List<FormattedText> cachedDescription;
    
    {
        for (final TaskType taskType : TaskType.values()) {
            addButton(new LargeButton(taskType.getLangKeyName(), taskType.getLangKeyDescription(), 185 + (taskType.ordinal() % 2) * 65, 50 + (taskType.ordinal() / 2) * 20) {
                @Override
                public boolean isEnabled() {
                    return true;
                }
                
                @Override
                public boolean isVisible() {
                    return Quest.canQuestsBeEdited() && selectedTask == null && QuestGraphic.this.gui.getCurrentMode() == EditMode.TASK;
                }
                
                @Override
                public void onClick(GuiBase gui) {
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
                return quest.getTasks().size() > VISIBLE_TASKS && getVisibleTasks() > VISIBLE_TASKS;
            }
        });
    }
    
    public QuestGraphic(UUID playerId, Quest quest, GuiQuestBook gui) {
        super(gui, EditMode.NORMAL, EditMode.RENAME, EditMode.TASK, /*EditMode.CHANGE_TASK,*/ EditMode.ITEM, EditMode.LOCATION, EditMode.MOB, EditMode.REPUTATION_TASK, EditMode.REPUTATION_REWARD, EditMode.COMMAND_CREATE, EditMode.COMMAND_CHANGE, EditMode.DELETE);
        this.playerId = playerId;
        this.quest = quest;
        rewardsGraphic = new QuestRewardsGraphic(quest, playerId);
        this.onOpen(gui.getPlayer());
    }
    
    private List<FormattedText> getCachedDescription(GuiBase gui) {
        if (cachedDescription == null) {
            cachedDescription = gui.getLinesFromText(Translator.plain(quest.getDescription()), 0.7F, 130);
        }
        return cachedDescription;
    }
    
    @Override
    public void draw(PoseStack matrices, GuiQuestBook gui, int mX, int mY) {
        if (!Quest.canQuestsBeEdited() && selectedTask != null && !selectedTask.isVisible(playerId)) {
            setSelectedTask(quest.getTasks().size() > 0 ? quest.getTasks().get(0) : null);
        }
        
        gui.drawString(matrices, Translator.plain(quest.getName()), START_X, TITLE_START_Y, 0x404040);
        
        int startLine = descriptionScroll.isVisible(gui) ? Math.round((getCachedDescription(gui).size() - VISIBLE_DESCRIPTION_LINES) * descriptionScroll.getScroll()) : 0;
        gui.drawString(matrices, getCachedDescription(gui), startLine, VISIBLE_DESCRIPTION_LINES, START_X, DESCRIPTION_START_Y, 0.7F, 0x404040);
        
        int id = 0;
        int start = taskScroll.isVisible(gui) ? Math.round((getVisibleTasks() - VISIBLE_TASKS) * taskScroll.getScroll()) : 0;
        int end = Math.min(start + VISIBLE_TASKS, quest.getTasks().size());
        for (int i = start; i < end; i++) {
            QuestTask<?> task = quest.getTasks().get(i);
            boolean isVisible = task.isVisible(playerId);
            if (isVisible || Quest.canQuestsBeEdited()) {
                boolean completed = task.isCompleted(playerId);
                int yPos = getTaskY(id);
                boolean inBounds = gui.inBounds(START_X, yPos, gui.getStringWidth(task.getDescription()), TEXT_HEIGHT, mX, mY);
                boolean isSelected = task == selectedTask;
                gui.drawString(matrices, Translator.plain(task.getDescription()), START_X, yPos, completed ? isSelected ? inBounds ? 0x40BB40 : 0x40A040 : inBounds ? 0x10A010 : 0x107010 : isSelected ? inBounds ? 0xAAAAAA : 0x888888 : inBounds ? 0x666666 : isVisible ? 0x404040 : 0xDDDDDD);
                
                id++;
            }
        }
        
        super.draw(matrices, gui, mX, mY);
        
        rewardsGraphic.draw(matrices, gui, mX, mY);
        
        if (selectedTask != null) {
            List<FormattedText> description = selectedTask.getCachedLongDescription(gui);
            int taskStartLine = taskDescriptionScroll.isVisible(gui) ? Math.round((description.size() - VISIBLE_DESCRIPTION_LINES) * taskDescriptionScroll.getScroll()) : 0;
            gui.drawString(matrices, description, taskStartLine, VISIBLE_DESCRIPTION_LINES, TASK_DESCRIPTION_X, TASK_DESCRIPTION_Y, 0.7F, 0x404040);
    
            taskGraphic.draw(matrices, gui, mX, mY);
        } else if (Quest.canQuestsBeEdited() && gui.getCurrentMode() == EditMode.TASK) {
            gui.drawString(matrices, gui.getLinesFromText(Translator.translatable("hqm.quest.createTasks"), 0.7F, 130), 180, 20, 0.7F, 0x404040);
        }
    }
    
    @Override
    public void drawTooltip(PoseStack matrices, GuiQuestBook gui, int mX, int mY) {
        super.drawTooltip(matrices, gui, mX, mY);
    
        if (taskGraphic != null) {
            taskGraphic.drawTooltip(matrices, gui, mX, mY);
        }
    
        rewardsGraphic.drawTooltip(matrices, gui, mX, mY);
    }
    
    private int getVisibleTasks() {
        if (Quest.canQuestsBeEdited()) {
            return quest.getTasks().size();
        }
        
        int count = 0;
        for (QuestTask<?> task : quest.getTasks()) {
            if (task.isVisible(playerId)) {
                count++;
            }
        }
        return count;
    }
    
    private int getTaskY(int id) {
        return TASK_LABEL_START_Y + id * (TEXT_HEIGHT + TASK_MARGIN);
    }
    
    @Override
    public void onClick(GuiQuestBook gui, int mX, int mY, int b) {
        int id = 0;
        int start = taskScroll.isVisible(gui) ? Math.round((getVisibleTasks() - VISIBLE_TASKS) * taskScroll.getScroll()) : 0;
        int end = Math.min(start + VISIBLE_TASKS, quest.getTasks().size());
        for (int i = start; i < end; i++) {
            QuestTask<?> task = quest.getTasks().get(i);
            if (task.isVisible(playerId) || Quest.canQuestsBeEdited()) {
                if (gui.inBounds(START_X, getTaskY(id), gui.getStringWidth(task.getDescription()), TEXT_HEIGHT, mX, mY)) {
                    if (gui.isOpBook && Screen.hasShiftDown()) {
                        OPBookHelper.reverseTaskCompletion(task, playerId);
                        return;
                    }
                    if (Quest.canQuestsBeEdited() && gui.getCurrentMode() == EditMode.TASK) {
                        gui.setCurrentMode(EditMode.NORMAL);
                    }
                    if (Quest.canQuestsBeEdited() && (gui.getCurrentMode() == EditMode.RENAME || gui.getCurrentMode() == EditMode.DELETE)) {
                        if (gui.getCurrentMode() == EditMode.RENAME) {
                            TextMenu.display(gui, playerId, task.getDescription(), true,
                                    task::setDescription);
                        } else if (gui.getCurrentMode() == EditMode.DELETE) {
                            if (i + 1 < quest.getTasks().size()) {
                                quest.getTasks().get(i + 1).clearRequirements();
                            
                                if (i > 0) {
                                    quest.getTasks().get(i + 1).addRequirement(quest.getTasks().get(i - 1));
                                }
                            }
                            if (selectedTask == task) {
                                setSelectedTask(null);
                            }
                        
                            task.onDelete();
                        
                            quest.getTasks().remove(i);
                            quest.nextTaskId = 0;
                            for (QuestTask<?> questTask : quest.getTasks()) {
                                questTask.updateId();
                            }
                        
                            quest.getQuestData(playerId).clearTaskData(quest);
                            SaveHelper.add(EditType.TASK_REMOVE);
                        }
                    } else if (task == selectedTask) {
                        setSelectedTask(null);
                    } else {
                        setSelectedTask(task);
                        taskDescriptionScroll.resetScroll();
                    }
                    break;
                }
            
                id++;
            }
        }
    
        rewardsGraphic.onClick(gui, mX, mY, b);
    
        if (taskGraphic != null) {
            taskGraphic.onClick(gui, mX, mY, b);
        }
    
        super.onClick(gui, mX, mY, b);
    
        if (gui.getCurrentMode() == EditMode.RENAME) {
            if (gui.inBounds(START_X, TITLE_START_Y, 140, TEXT_HEIGHT, mX, mY)) {
                TextMenu.display(gui, playerId, quest.getName(), true, quest::setName);
            } else if (gui.inBounds(START_X, DESCRIPTION_START_Y, 130, (int) (VISIBLE_DESCRIPTION_LINES * TEXT_HEIGHT * 0.7), mX, mY)) {
                TextMenu.display(gui, playerId, quest.getDescription(), false, description -> {
                    cachedDescription = null;
                    quest.setDescription(description);
                });
            } else if (selectedTask != null && gui.inBounds(TASK_DESCRIPTION_X, TASK_DESCRIPTION_Y, 130, (int) (VISIBLE_DESCRIPTION_LINES * TEXT_HEIGHT * 0.7), mX, mY)) {
                TextMenu.display(gui, playerId, selectedTask.getLongDescription(), false,
                        selectedTask::setLongDescription);
            }
        }
    
        if (Quest.canQuestsBeEdited() && selectedTask != null && gui.getCurrentMode() == EditMode.TASK) {
            setSelectedTask(null);
        }
    }
    
    @Override
    public void onDrag(GuiQuestBook gui, int mX, int mY, int b) {
        super.onDrag(gui, mX, mY, b);
        if (taskGraphic != null)
            taskGraphic.onDrag(gui, mX, mY, b);
    }
    
    @Override
    public void onRelease(GuiQuestBook gui, int mX, int mY, int b) {
        super.onRelease(gui, mX, mY, b);
        if (taskGraphic != null)
            taskGraphic.onRelease(gui, mX, mY, b);
    }
    
    @Override
    public void onScroll(GuiQuestBook gui, double x, double y, double scroll) {
        super.onScroll(gui, x, y, scroll);
        if (taskGraphic != null)
            taskGraphic.onScroll(gui, x, y, scroll);
    }
    
    public void onOpen(Player player) {
        if (selectedTask == null) {
            for (QuestTask<?> task : quest.getTasks()) {
                if (!task.isCompleted(playerId)) {
                    setSelectedTask(task);
                    break;
                }
            }
        }
        
        if (selectedTask == null && quest.getTasks().size() > 0)
            setSelectedTask(quest.getTasks().get(0));
        
        QuestingDataManager.getInstance().getQuestingData(playerId).selectedQuestId = quest.getQuestId();
        QuestingDataManager.getInstance().getQuestingData(playerId).selectedTask = selectedTask == null ? -1 : selectedTask.getId();
        if (selectedTask != null) {
            GeneralUsage.sendBookSelectTaskUpdate(selectedTask);
        }
        
        EventTrigger.instance().onQuestSelected(new EventTrigger.QuestSelectedEvent(player, quest.getQuestId()));
    }
    
    private void setSelectedTask(@Nullable QuestTask<?> task) {
        selectedTask = task;
        taskGraphic = task == null ? null : task.createGraphic(playerId);
    }
    
    @Override
    protected void setEditMode(EditMode editMode) {
        if (editMode == EditMode.COMMAND_CREATE || editMode == EditMode.COMMAND_CHANGE) {
            gui.setEditMenu(new GuiEditMenuCommandEditor(gui, playerId, quest));
        } else super.setEditMode(editMode);
    }
}
