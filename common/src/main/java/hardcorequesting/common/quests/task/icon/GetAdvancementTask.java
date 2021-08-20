package hardcorequesting.common.quests.task.icon;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hardcorequesting.common.event.EventTrigger;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.data.AdvancementTaskData;
import hardcorequesting.common.quests.task.client.AdvancementTaskGraphic;
import hardcorequesting.common.quests.task.client.TaskGraphic;
import hardcorequesting.common.team.Team;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.Translator;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * A task where the player has to complete advancements.
 */
public class GetAdvancementTask extends IconLayoutTask<GetAdvancementTask.Part, AdvancementTaskData> {
    private static final String ADVANCEMENTS = "advancements";
    
    public GetAdvancementTask(Quest parent, String description, String longDescription) {
        super(AdvancementTaskData.class, EditType.Type.ADVANCEMENT, parent, description, longDescription);
        
        register(EventTrigger.Type.ADVANCEMENT, EventTrigger.Type.OPEN_BOOK);
    }
    
    @Override
    protected TaskGraphic createGraphic() {
        return new AdvancementTaskGraphic(this);
    }
    
    @Override
    protected Part createEmpty() {
        return new Part();
    }
    
    public boolean advanced(int id, Player player) {
        return getData(player).getValue(id);
    }
    
    public void setAdvancement(int id, String advancement) {
        parts.getOrCreateForModify(id).setAdvancement(advancement);
    }
    
    @Override
    public AdvancementTaskData newQuestData() {
        return new AdvancementTaskData(parts.size());
    }
    
    @Override
    public void onAdvancement(ServerPlayer playerEntity) {
        checkAdvancement(playerEntity);
    }
    
    @Override
    public void onUpdate(Player player) {
        checkAdvancement(player);
    }
    
    private void checkAdvancement(Player player) {
        Level world = player.getCommandSenderWorld();
        if (!world.isClientSide && !this.isCompleted(player) && player.getServer() != null) {
            AdvancementTaskData data = this.getData(player);
            
            boolean completed = true;
            ServerAdvancementManager manager = player.getServer().getAdvancements();
            PlayerAdvancements playerAdvancements = player.getServer().getPlayerList().getPlayerAdvancements((ServerPlayer) player);
            
            for (int i = 0; i < parts.size(); i++) {
                if (data.getValue(i)) continue;
                
                Part part = this.parts.get(i);
                if (part == null || part.getName() == null || part.getAdvancement() == null) continue;
                
                ResourceLocation advResource = new ResourceLocation(part.getAdvancement());
                
                Advancement advAdvancement = manager.getAdvancement(advResource);
                
                if (advAdvancement == null) {
                    completed = false;
                } else {
                    AdvancementProgress progress = playerAdvancements.getOrStartProgress(advAdvancement);
                    
                    if (progress.isDone()) {
                        data.complete(i);
                    } else {
                        completed = false;
                    }
                }
            }
            
            if (completed && parts.size() > 0) {
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
    public void mergeProgress(UUID uuid, AdvancementTaskData own, AdvancementTaskData other) {
        own.mergeResult(other);
        
        if (own.areAllCompleted(parts.size())) {
            completeTask(uuid);
        }
    }
    
    @Override
    public void setComplete(AdvancementTaskData data) {
        for (int i = 0; i < parts.size(); i++) {
            data.complete(i);
        }
        data.completed = true;
    }
    
    @Override
    public void copyProgress(AdvancementTaskData own, AdvancementTaskData other) {
        own.update(other);
    }
    
    @Override
    public boolean allowDetect() {
        return true;
    }
    
    @Override
    public void write(Adapter.JsonObjectBuilder builder) {
        builder.add(ADVANCEMENTS, parts.write(QuestTaskAdapter.ADVANCEMENT_TASK_ADAPTER));
    }
    
    @SuppressWarnings("ConstantConditions")
    @Override
    public void read(JsonObject object) {
        parts.read(GsonHelper.getAsJsonArray(object, ADVANCEMENTS, new JsonArray()), QuestTaskAdapter.ADVANCEMENT_TASK_ADAPTER);
    }
    
    public enum Visibility {
        FULL("Full"),
        NONE("None");
        
        private final String id;
        
        Visibility(String id) {
            this.id = id;
        }
        
        // TODO: fix these
        public String getName() {
            return Translator.get("hqm.locationMenu.vis" + id + ".title");
        }
        
        public String getDescription() {
            return Translator.get("hqm.locationMenu.vis" + id + ".desc");
        }
    }
    
    public static class Part extends IconLayoutTask.Part {
        
        private Visibility visible = Visibility.FULL;
        private String adv_name;
        
        public Visibility getVisible() {
            return visible;
        }
        
        public void setVisible(Visibility visible) {
            this.visible = visible;
        }
        
        public void setAdvancement(String name) {
            this.adv_name = name;
        }
        
        public String getAdvancement() {
            return this.adv_name;
        }
        
        public void setAdvancement(ResourceLocation name) {
            setAdvancement(name.toString());
        }
    }
}

