package hardcorequesting.common.quests.task;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiColor;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.event.EventTrigger;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.data.QuestDataTask;
import hardcorequesting.common.quests.data.QuestDataTaskCompleted;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;

public class CompleteQuestTask extends ListTask<CompleteQuestTask.Part> {
    private static final String COMPLETED_QUESTS = "completed_quests";
    private static final int Y_OFFSET = 30;
    private static final int X_TEXT_OFFSET = 23;
    private static final int X_TEXT_INDENT = 0;
    private static final int Y_TEXT_OFFSET = 0;
    private static final int ITEM_SIZE = 18;
    
    public CompleteQuestTask(Quest parent, String description, String longDescription) {
        super(EditType.Type.COMPLETION, parent, description, longDescription);
        
        register(EventTrigger.Type.QUEST_COMPLETED, EventTrigger.Type.OPEN_BOOK);
    }
    
    @Override
    protected Part createEmpty() {
        return new Part();
    }
    
    private boolean completed(int id, Player player) {
        return ((QuestDataTaskCompleted) getData(player)).getValue(id);
    }
    
    @SuppressWarnings("unused")
    public void setQuest(int id, UUID quest) {
        getOrCreateForModify(id).setQuest(quest);
    }
    
    @Override
    public Class<? extends QuestDataTask> getDataType() {
        return QuestDataTaskCompleted.class;
    }
    
    @Override
    public QuestDataTask newQuestData() {
        return new QuestDataTaskCompleted(elements.size());
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void draw(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY) {
        List<Part> quests = getShownElements();
        for (int i = 0; i < quests.size(); i++) {
            Part completed = quests.get(i);
            
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
            List<Part> quests = getShownElements();
            for (int i = 0; i < quests.size(); i++) {
                Part completed = quests.get(i);
                
                int x = START_X;
                int y = START_Y + i * Y_OFFSET;
                
                if (gui.inBounds(x, y, ITEM_SIZE, ITEM_SIZE, mX, mY)) {
                    if (gui.getCurrentMode() == EditMode.DELETE) {
                        if (i < elements.size()) {
                            elements.remove(i);
                            SaveHelper.add(EditType.COMPLETE_CHECK_REMOVE);
                        }
                    } else if (completed.getQuest() == null) {
                        completed.setQuest(Quest.speciallySelectedQuestId);
                        SaveHelper.add(EditType.COMPLETE_CHECK_CHANGE);
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
            QuestDataTaskCompleted data = ((QuestDataTaskCompleted) this.getData(player));
            
            boolean completed = true;
            
            for (int i = 0; i < elements.size(); i++) {
                if (data.getValue(i)) continue;
                
                Part task_quest = elements.get(i);
                if (task_quest == null || task_quest.getName() == null || task_quest.getQuest() == null) continue;
                
                Quest quest = task_quest.getQuest();
                if (quest != null) {
                    if (quest.isCompleted(player)) {
                        data.complete(i);
                    } else {
                        completed = false;
                    }
                } else {
                    completed = false;
                }
            }
            
            if (completed && !elements.isEmpty()) {
                completeTask(player.getUUID());
                parent.sendUpdatedDataToTeam(player);
            }
        }
    }
    
    @Override
    public float getCompletedRatio(UUID uuid) {
        return ((QuestDataTaskCompleted) getData(uuid)).getCompletedRatio(elements.size());
    }
    
    @Override
    public void mergeProgress(UUID uuid, QuestDataTask own, QuestDataTask other) {
        ((QuestDataTaskCompleted) own).mergeResult((QuestDataTaskCompleted) other);
        
        if (((QuestDataTaskCompleted) own).areAllCompleted(elements.size())) {
            completeTask(uuid);
        }
    }
    
    @Override
    public void autoComplete(UUID uuid, boolean status) {
        QuestDataTaskCompleted data = (QuestDataTaskCompleted) getData(uuid);
        if (status) {
            for (int i = 0; i < elements.size(); i++) {
                data.complete(i);
            }
        } else {
            data.clear();
        }
    }
    
    @Override
    public void copyProgress(QuestDataTask own, QuestDataTask other) {
        own.update(other);
    }
    
    @Override
    public boolean allowDetect() {
        return true;
    }
    
    @Override
    public void write(Adapter.JsonObjectBuilder builder) {
        builder.add(COMPLETED_QUESTS, writeElements(QuestTaskAdapter.QUEST_COMPLETED_ADAPTER));
    }
    
    @SuppressWarnings("ConstantConditions")
    @Override
    public void read(JsonObject object) {
        readElements(GsonHelper.getAsJsonArray(object, COMPLETED_QUESTS, new JsonArray()), QuestTaskAdapter.QUEST_COMPLETED_ADAPTER);
    }
    
    public static class Part {
        private UUID quest_id;
    
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

