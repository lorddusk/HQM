package hardcorequesting.common.client.interfaces.graphic;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenuCommandEditor;
import hardcorequesting.common.client.interfaces.edit.TextMenu;
import hardcorequesting.common.client.interfaces.graphic.task.TaskGraphic;
import hardcorequesting.common.client.interfaces.widget.ExtendedScrollBar;
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
    private static final int TASK_LABEL_START_Y = 100;
    private static final int TASK_MARGIN = 2;
    private static final int TITLE_START_Y = 15;
    private static final int DESCRIPTION_START_Y = 30;
    
    private final UUID playerId;
    private final Quest quest;
    private QuestTask<?> selectedTask;
    private TaskGraphic taskGraphic;
    
    private final QuestRewardsGraphic rewardsGraphic;
    
    private final ExtendedScrollBar<FormattedText> descriptionScroll;
    private final ScrollBar taskScroll;
    private List<FormattedText> cachedDescription;
    
    {
        for (final TaskType taskType : TaskType.values()) {
            addButton(new LargeButton(gui, taskType.getLangKeyName(), taskType.getLangKeyDescription(), 185 + (taskType.ordinal() % 2) * 65, 50 + (taskType.ordinal() / 2) * 20) {
                @Override
                public boolean isVisible() {
                    return Quest.canQuestsBeEdited() && selectedTask == null && QuestGraphic.this.gui.getCurrentMode() == EditMode.TASK;
                }
                
                @Override
                public void onClick() {
                    taskType.addTask(quest);
                }
            });
        }
    }
    
    public QuestGraphic(UUID playerId, Quest quest, GuiQuestBook gui) {
        super(gui, EditMode.NORMAL, EditMode.RENAME, EditMode.TASK, /*EditMode.CHANGE_TASK,*/ EditMode.ITEM, EditMode.LOCATION, EditMode.MOB, EditMode.REPUTATION_TASK, EditMode.REPUTATION_REWARD, EditMode.COMMAND_CREATE, EditMode.COMMAND_CHANGE, EditMode.DELETE);
        this.playerId = playerId;
        this.quest = quest;
        rewardsGraphic = new QuestRewardsGraphic(quest, playerId, gui);
        this.onOpen(gui.getPlayer());
        
        addScrollBar(descriptionScroll = new ExtendedScrollBar<>(gui, 155, 28, 64, 249, 102, START_X, VISIBLE_DESCRIPTION_LINES, this::getCachedDescription));
    
        addScrollBar(taskScroll = new ScrollBar(gui, 155, 100, 29, 242, 102, START_X) {
            @Override
            public boolean isVisible() {
                return quest.getTasks().size() > VISIBLE_TASKS && getVisibleTasks() > VISIBLE_TASKS;
            }
        });
    }
    
    private List<FormattedText> getCachedDescription() {
        if (cachedDescription == null) {
            cachedDescription = gui.getLinesFromText(Translator.plain(quest.getDescription()), 0.7F, 130);
        }
        return cachedDescription;
    }
    
    @Override
    public void draw(PoseStack matrices, int mX, int mY) {
        if (!Quest.canQuestsBeEdited() && selectedTask != null && !selectedTask.isVisible(playerId)) {
            setSelectedTask(quest.getTasks().size() > 0 ? quest.getTasks().get(0) : null);
        }
        
        gui.drawString(matrices, Translator.plain(quest.getName()), START_X, TITLE_START_Y, 0x404040);
        
        List<FormattedText> description = descriptionScroll.getVisibleEntries();
        gui.drawString(matrices, description, START_X, DESCRIPTION_START_Y, 0.7F, 0x404040);
        
        int id = 0;
        for (QuestTask<?> task : taskScroll.getVisibleEntries(quest.getTasks(), VISIBLE_TASKS)) {
            boolean isVisible = task.isVisible(playerId);
            if (isVisible || Quest.canQuestsBeEdited()) {
                boolean completed = task.isCompleted(playerId);
                int yPos = getTaskY(id);
                boolean inBounds = gui.inBounds(START_X, yPos, gui.getStringWidth(task.getDescription()), GuiBase.TEXT_HEIGHT, mX, mY);
                boolean isSelected = task == selectedTask;
                gui.drawString(matrices, Translator.plain(task.getDescription()), START_X, yPos, completed ? isSelected ? inBounds ? 0x40BB40 : 0x40A040 : inBounds ? 0x10A010 : 0x107010 : isSelected ? inBounds ? 0xAAAAAA : 0x888888 : inBounds ? 0x666666 : isVisible ? 0x404040 : 0xDDDDDD);
                
                id++;
            }
        }
        
        super.draw(matrices, mX, mY);
        
        rewardsGraphic.draw(matrices, mX, mY);
        
        if (taskGraphic != null) {
            taskGraphic.draw(matrices, mX, mY);
        } else if (Quest.canQuestsBeEdited() && gui.getCurrentMode() == EditMode.TASK) {
            gui.drawString(matrices, gui.getLinesFromText(Translator.translatable("hqm.quest.createTasks"), 0.7F, 130), 180, 20, 0.7F, 0x404040);
        }
    }
    
    @Override
    public void drawTooltip(PoseStack matrices, int mX, int mY) {
        super.drawTooltip(matrices, mX, mY);
    
        if (taskGraphic != null) {
            taskGraphic.drawTooltip(matrices, mX, mY);
        }
    
        rewardsGraphic.drawTooltip(matrices, mX, mY);
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
        return TASK_LABEL_START_Y + id * (GuiBase.TEXT_HEIGHT + TASK_MARGIN);
    }
    
    @Override
    public void onClick(int mX, int mY, int b) {
        int id = 0;
        for (QuestTask<?> task : taskScroll.getVisibleEntries(quest.getTasks(), VISIBLE_TASKS)) {
            if (task.isVisible(playerId) || Quest.canQuestsBeEdited()) {
                if (gui.inBounds(START_X, getTaskY(id), gui.getStringWidth(task.getDescription()), GuiBase.TEXT_HEIGHT, mX, mY)) {
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
                            
                            if (selectedTask == task) {
                                setSelectedTask(null);
                            }
                            
                            quest.removeTask(task);
                            SaveHelper.add(EditType.TASK_REMOVE);
                        }
                    } else if (task == selectedTask) {
                        setSelectedTask(null);
                    } else {
                        setSelectedTask(task);
                    }
                    break;
                }
            
                id++;
            }
        }
    
        rewardsGraphic.onClick(mX, mY, b);
    
        if (taskGraphic != null) {
            taskGraphic.onClick(mX, mY, b);
        }
    
        super.onClick(mX, mY, b);
    
        if (gui.getCurrentMode() == EditMode.RENAME) {
            if (gui.inBounds(START_X, TITLE_START_Y, 140, GuiBase.TEXT_HEIGHT, mX, mY)) {
                TextMenu.display(gui, playerId, quest.getName(), true, quest::setName);
            } else if (gui.inBounds(START_X, DESCRIPTION_START_Y, 130, (int) (VISIBLE_DESCRIPTION_LINES * GuiBase.TEXT_HEIGHT * 0.7), mX, mY)) {
                TextMenu.display(gui, playerId, quest.getDescription(), false, description -> {
                    cachedDescription = null;
                    quest.setDescription(description);
                });
            }
        }
    
        if (Quest.canQuestsBeEdited() && selectedTask != null && gui.getCurrentMode() == EditMode.TASK) {
            setSelectedTask(null);
        }
    }
    
    @Override
    public void onDrag(int mX, int mY, int b) {
        super.onDrag(mX, mY, b);
        rewardsGraphic.onDrag(mX, mY, b);
        if (taskGraphic != null)
            taskGraphic.onDrag(mX, mY, b);
    }
    
    @Override
    public void onRelease(int mX, int mY, int b) {
        super.onRelease(mX, mY, b);
        rewardsGraphic.onRelease(mX, mY, b);
        if (taskGraphic != null)
            taskGraphic.onRelease(mX, mY, b);
    }
    
    @Override
    public void onScroll(double mX, double mY, double scroll) {
        super.onScroll(mX, mY, scroll);
        rewardsGraphic.onScroll(mX, mY, scroll);
        if (taskGraphic != null)
            taskGraphic.onScroll(mX, mY, scroll);
    }
    
    @Override
    public boolean keyPressed(int keyCode) {
        return super.keyPressed(keyCode)
                || rewardsGraphic.keyPressed(keyCode)
                || (taskGraphic != null && taskGraphic.keyPressed(keyCode));
    }
    
    @Override
    public boolean charTyped(char c) {
        return super.charTyped(c)
                || rewardsGraphic.charTyped(c)
                || (taskGraphic != null && taskGraphic.charTyped(c));
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
        taskGraphic = task == null ? null : task.createGraphic(playerId, gui);
    }
    
    @Override
    protected void setEditMode(EditMode editMode) {
        if (editMode == EditMode.COMMAND_CREATE || editMode == EditMode.COMMAND_CHANGE) {
            gui.setEditMenu(new GuiEditMenuCommandEditor(gui, playerId, quest));
        } else super.setEditMode(editMode);
    }
}
