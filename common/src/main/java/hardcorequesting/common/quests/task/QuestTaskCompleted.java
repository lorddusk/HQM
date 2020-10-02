package hardcorequesting.common.quests.task;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiColor;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.event.EventTrigger;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.data.QuestDataTask;
import hardcorequesting.common.quests.data.QuestDataTaskCompleted;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.UUID;

public class QuestTaskCompleted extends QuestTask {
    private static final int Y_OFFSET = 30;
    private static final int X_TEXT_OFFSET = 23;
    private static final int X_TEXT_INDENT = 0;
    private static final int Y_TEXT_OFFSET = 0;
    private static final int ITEM_SIZE = 18;
    public CompletedQuestTask[] quests = new CompletedQuestTask[0];
    
    public QuestTaskCompleted(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
        
        register(EventTrigger.Type.QUEST_COMPLETED, EventTrigger.Type.OPEN_BOOK);
    }
    
    @Environment(EnvType.CLIENT)
    private CompletedQuestTask[] getEditFriendlyCompleted(CompletedQuestTask[] completed) {
        if (Quest.canQuestsBeEdited()) {
            completed = Arrays.copyOf(completed, completed.length + 1);
            completed[completed.length - 1] = new CompletedQuestTask();
            return completed;
        } else {
            return completed;
        }
    }
    
    private boolean completed(int id, Player player) {
        return id < quests.length && ((QuestDataTaskCompleted) getData(player)).quests[id];
    }
    
    public void setTask(int id, CompletedQuestTask task, Player player) {
        if (id >= quests.length) {
            quests = Arrays.copyOf(quests, quests.length + 1);
            QuestDataTaskCompleted data = (QuestDataTaskCompleted) getData(player);
            data.quests = Arrays.copyOf(data.quests, data.quests.length + 1);
            SaveHelper.add(SaveHelper.EditType.COMPLETE_CHECK_CREATE);
        } else {
            SaveHelper.add(SaveHelper.EditType.COMPLETE_CHECK_CHANGE);
        }
        
        quests[id] = task;
    }
    
    @SuppressWarnings("unused")
    public void setQuest(int id, UUID quest, Player player) {
        setTask(id, id >= quests.length ? new CompletedQuestTask() : quests[id], player);
        
        quests[id].setQuest(quest);
    }
    
    @Override
    public Class<? extends QuestDataTask> getDataType() {
        return QuestDataTaskCompleted.class;
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void draw(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY) {
        CompletedQuestTask[] completed_quests = getEditFriendlyCompleted(this.quests);
        for (int i = 0; i < completed_quests.length; i++) {
            CompletedQuestTask completed = completed_quests[i];
            
            int x = START_X;
            int y = START_Y + i * Y_OFFSET;
            gui.drawItemStack(completed.getIconStack(), x, y, mX, mY, false);
            if (completed.getQuest() != null) {
                gui.drawString(matrices, Translator.plain(completed.getName()), x + X_TEXT_OFFSET, y + Y_TEXT_OFFSET, 0x404040);
            } else {
                gui.drawString(matrices, Translator.translatable("hqm.completionTask.firstline", GuiColor.RED), x + X_TEXT_OFFSET, y + Y_TEXT_OFFSET, 0x404040);
                gui.drawString(matrices, Translator.translatable("hqm.completionTask.secondline", GuiColor.RED), x + X_TEXT_OFFSET, y + Y_TEXT_OFFSET + 9, 0x404040);
                gui.drawString(matrices, Translator.translatable("hqm.completionTask.thirdline", GuiColor.RED), x + X_TEXT_OFFSET, y + Y_TEXT_OFFSET + 18, 0x404040);
            }
            
            if (completed(i, player)) {
                gui.drawString(matrices, Translator.translatable("hqm.completedMenu.visited", GuiColor.GREEN), x + X_TEXT_OFFSET + X_TEXT_INDENT, y + Y_TEXT_OFFSET + 9, 0.7F, 0x404040);
            }
        }
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void onClick(GuiQuestBook gui, Player player, int mX, int mY, int b) {
        if (Quest.canQuestsBeEdited()) {
            CompletedQuestTask[] completed_quests = getEditFriendlyCompleted(this.quests);
            for (int i = 0; i < completed_quests.length; i++) {
                CompletedQuestTask completed = completed_quests[i];
                
                int x = START_X;
                int y = START_Y + i * Y_OFFSET;
                
                if (gui.inBounds(x, y, ITEM_SIZE, ITEM_SIZE, mX, mY)) {
                    if (gui.getCurrentMode() == EditMode.DELETE) {
                        if (i < this.quests.length) {
                            CompletedQuestTask[] newCompleted = new CompletedQuestTask[this.quests.length - 1];
                            int id = 0;
                            for (int j = 0; j < this.quests.length; j++) {
                                if (j != i) {
                                    newCompleted[id] = this.quests[j];
                                    id++;
                                }
                            }
                            this.quests = newCompleted;
                            SaveHelper.add(SaveHelper.EditType.COMPLETE_CHECK_REMOVE);
                        }
                    } else if (completed.getQuest() == null) {
                        CompletedQuestTask copy = completed.copy();
                        copy.setQuest(Quest.speciallySelectedQuestId);
                        this.setTask(i, copy, player);
                        SaveHelper.add(SaveHelper.EditType.COMPLETE_CHECK_CHANGE);
                    }
                    
                    break;
                }
            }
        }
    }
    
    @Override
    public void onQuestCompleted(EventTrigger.QuestCompletedEvent event) {
        checkCompleted(event.getPlayer());
    }
    
    public void onQuestSelected(EventTrigger.QuestSelectedEvent event) {
        checkCompleted(event.getPlayer());
    }
    
    @Override
    public void onUpdate(Player player) {
        checkCompleted(player);
    }
    
    private void checkCompleted(Player player) {
        Level world = player.getCommandSenderWorld();
        if (!world.isClientSide && !this.isCompleted(player) && player.getServer() != null) {
            boolean[] other_completed_quests = ((QuestDataTaskCompleted) this.getData(player)).quests;
            
            if (other_completed_quests.length < this.quests.length) {
                boolean[] oldCompleted = ArrayUtils.addAll(other_completed_quests, (boolean[]) null);
                other_completed_quests = new boolean[this.quests.length];
                System.arraycopy(oldCompleted, 0, other_completed_quests, 0, oldCompleted.length);
                ((QuestDataTaskCompleted) this.getData(player)).quests = other_completed_quests;
            }
            
            boolean completed = true;
            
            for (int i = 0; i < this.quests.length; i++) {
                if (other_completed_quests[i]) continue;
                
                CompletedQuestTask task_quest = this.quests[i];
                if (task_quest == null || task_quest.getName() == null || task_quest.getQuest() == null) continue;
                
                Quest quest = task_quest.getQuest();
                if (quest != null) {
                    if (quest.isCompleted(player)) {
                        other_completed_quests[i] = true;
                    } else {
                        other_completed_quests[i] = false;
                        completed = false;
                    }
                } else {
                    completed = false;
                }
            }
            
            if (completed && this.quests.length > 0) {
                completeTask(player.getUUID());
                parent.sendUpdatedDataToTeam(player);
            }
        }
    }
    
    @Override
    public float getCompletedRatio(UUID uuid) {
        int completed = 0;
        for (boolean b : ((QuestDataTaskCompleted) getData(uuid)).quests) {
            if (b) {
                completed++;
            }
        }
        
        return (float) completed / quests.length;
    }
    
    @Override
    public void mergeProgress(UUID uuid, QuestDataTask own, QuestDataTask other) {
        boolean[] completed = ((QuestDataTaskCompleted) own).quests;
        boolean[] otherCompleted = ((QuestDataTaskCompleted) other).quests;
        
        boolean all = true;
        for (int i = 0; i < completed.length; i++) {
            if (otherCompleted[i]) {
                completed[i] = true;
            } else if (!completed[i]) {
                all = false;
            }
        }
        
        if (all) {
            completeTask(uuid);
        }
    }
    
    @Override
    public void autoComplete(UUID uuid, boolean status) {
        boolean[] completed = ((QuestDataTaskCompleted) getData(uuid)).quests;
        for (int i = 0; i < quests.length; i++) {
            completed[i] = status;
        }
    }
    
    @Override
    public void copyProgress(QuestDataTask own, QuestDataTask other) {
        super.copyProgress(own, other);
        boolean[] completed = ((QuestDataTaskCompleted) own).quests;
        System.arraycopy(((QuestDataTaskCompleted) other).quests, 0, completed, 0, completed.length);
    }
    
    @Override
    public boolean allowDetect() {
        return true;
    }
    
    public static class CompletedQuestTask {
        
        private UUID quest_id;
        
        private CompletedQuestTask copy() {
            CompletedQuestTask completed = new CompletedQuestTask();
            completed.quest_id = quest_id;
            
            return completed;
        }
        
        public ItemStack getIconStack() {
            Quest q = getQuest();
            return (q != null) ? q.getIconStack() : ItemStack.EMPTY;
        }
        
        public String getName() {
            Quest q = getQuest();
            return (q != null) ? q.getName() : "Use \"Select Quest\" to pick";
        }
        
        public void setQuest(UUID quest_id) {
            this.quest_id = quest_id;
        }
        
        public UUID getQuestId() {
            return this.quest_id;
        }
        
        public Quest getQuest() {
            if (getQuestId() == null) return null;
            
            return Quest.getQuest(getQuestId());
        }
    }
}

