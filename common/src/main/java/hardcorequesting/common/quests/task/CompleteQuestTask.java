package hardcorequesting.common.quests.task;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hardcorequesting.common.event.EventTrigger;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.data.CompleteQuestTaskData;
import hardcorequesting.common.quests.task.client.CompleteQuestTaskGraphic;
import hardcorequesting.common.quests.task.client.TaskGraphic;
import hardcorequesting.common.team.Team;
import hardcorequesting.common.util.EditType;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class CompleteQuestTask extends QuestTask<CompleteQuestTaskData> {
    private static final String COMPLETED_QUESTS = "completed_quests";
    
    protected static final int LIMIT = 3;
    
    public final PartList<Part> parts = new PartList<>(Part::new, EditType.Type.COMPLETION, LIMIT);
    
    public CompleteQuestTask(Quest parent, String description, String longDescription) {
        super(CompleteQuestTaskData.class, parent, description, longDescription);
        
        register(EventTrigger.Type.QUEST_COMPLETED, EventTrigger.Type.OPEN_BOOK);
    }
    
    @Override
    protected TaskGraphic createGraphic() {
        return new CompleteQuestTaskGraphic(this);
    }
    
    public boolean completed(int id, Player player) {
        return getData(player).getValue(id);
    }
    
    @SuppressWarnings("unused")
    public void setQuest(int id, UUID quest) {
        parts.getOrCreateForModify(id).setQuest(quest);
    }
    
    @Override
    public CompleteQuestTaskData newQuestData() {
        return new CompleteQuestTaskData(parts.size());
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
            CompleteQuestTaskData data = this.getData(player);
            
            boolean completed = true;
            
            for (int i = 0; i < parts.size(); i++) {
                if (data.getValue(i)) continue;
                
                Part task_quest = parts.get(i);
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
            
            if (completed && !parts.isEmpty()) {
                completeTask(player.getUUID());
                parent.sendUpdatedDataToTeam(player);
            }
        }
    }
    
    @Override
    public float getCompletedRatio(Team team) {
        return getData(team).getCompletedRatio(parts.size());
    }
    
    @Override
    public void mergeProgress(UUID uuid, CompleteQuestTaskData own, CompleteQuestTaskData other) {
        own.mergeResult(other);
        
        if (own.areAllCompleted(parts.size())) {
            completeTask(uuid);
        }
    }
    
    @Override
    public void setComplete(CompleteQuestTaskData data) {
        for (int i = 0; i < parts.size(); i++) {
            data.complete(i);
        }
        data.completed = true;
    }
    
    @Override
    public void copyProgress(CompleteQuestTaskData own, CompleteQuestTaskData other) {
        own.update(other);
    }
    
    @Override
    public boolean allowDetect() {
        return true;
    }
    
    @Override
    public void write(Adapter.JsonObjectBuilder builder) {
        builder.add(COMPLETED_QUESTS, parts.write(QuestTaskAdapter.QUEST_COMPLETED_ADAPTER));
    }
    
    @SuppressWarnings("ConstantConditions")
    @Override
    public void read(JsonObject object) {
        parts.read(GsonHelper.getAsJsonArray(object, COMPLETED_QUESTS, new JsonArray()), QuestTaskAdapter.QUEST_COMPLETED_ADAPTER);
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

