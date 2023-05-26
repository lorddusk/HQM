package hardcorequesting.common.quests.task.icon;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import hardcorequesting.common.event.EventTrigger;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.data.TameTaskData;
import hardcorequesting.common.quests.task.TaskType;
import hardcorequesting.common.team.Team;
import hardcorequesting.common.util.EditType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

import java.util.UUID;

/**
 * A task where the player needs to part certain mobs.
 */
public class TameMobsTask extends IconLayoutTask<TameMobsTask.Part, TameTaskData> {
    private static final String TAME = "part";
    
    public static final ResourceLocation ABSTRACT_HORSE = new ResourceLocation("abstracthorse");
    
    public TameMobsTask(Quest parent) {
        super(TaskType.TAME.get(), TameTaskData.class, EditType.Type.MONSTER, parent);
        register(EventTrigger.Type.ANIMAL_TAME);
    }
    
    @Override
    protected Part createEmpty() {
        return new Part();
    }
    
    @Environment(EnvType.CLIENT)
    public void setInfo(int id, String entityId, int amount) {
        
        Part part = parts.getOrCreateForModify(id);
        part.setTame(entityId);
        part.setCount(amount);
        
        if(entityId != null && (part.hasNoIcon() || part.getIconStack().left().orElse(ItemStack.EMPTY).getItem() instanceof SpawnEggItem)) {
            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(entityId));
            if(entityType != null) {
                Item egg = SpawnEggItem.byId(entityType);
                if(egg != null) {
                    part.setIconStack(Either.left(new ItemStack(egg)));
                    parent.setIconIfEmpty(new ItemStack(egg));
                }
            }
        }
    
    }
    
    public int tamed(int id, UUID playerId) {
        return getData(playerId).getValue(id);
    }
    
    @Override
    public TameTaskData newQuestData() {
        return new TameTaskData(parts.size());
    }
    
    @Override
    public TameTaskData loadData(JsonObject json) {
        return TameTaskData.construct(json);
    }
    
    @Override
    public void onUpdate(Player player) {
        
    }
    
    @Override
    public float getCompletedRatio(Team team) {
        TameTaskData data = getData(team);
        int tamed = 0;
        int total = 0;
        
        for (int i = 0; i < parts.size(); i++) {
            int req = parts.get(i).count;
            tamed += Math.min(req, data.getValue(i));
            total += req;
        }
        
        return (float) tamed / total;
    }
    
    @Override
    public void mergeProgress(UUID uuid, TameTaskData own, TameTaskData other) {
        own.merge(other);
    
        boolean all = true;
        for (int i = 0; i < parts.size(); i++) {
            if (own.getValue(i) < parts.get(i).count) {
                all = false;
                break;
            }
        }
        
        if (all) {
            completeTask(uuid);
        }
    }
    
    @Override
    public void setComplete(TameTaskData data) {
        for (int i = 0; i < parts.size(); i++) {
            data.setValue(i, parts.get(i).count);
        }
        data.completed = true;
    }
    
    @Override
    public void copyProgress(TameTaskData own, TameTaskData other) {
        own.update(other);
    }
    
    @Override
    public void onAnimalTame(Player tamer, Entity entity) {
        if (tamer != null && parent.isEnabled(tamer) && parent.isAvailable(tamer) && this.isVisible(tamer.getUUID()) && !isCompleted(tamer)) {
            TameTaskData data = getData(tamer);
            boolean updated = false;
            for (int i = 0; i < parts.size(); i++) {
                Part part = parts.get(i);
                if (part.count > data.getValue(i) && part.mobId != null) {
                    if (part.mobId.equals(ABSTRACT_HORSE.toString())) {
                        if (entity instanceof AbstractHorse) {
                            data.setValue(i, data.getValue(i) + 1);
                            updated = true;
                        }
                    } else {
                        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(part.mobId));
                        if (type != null) {
                            if (type.equals(entity.getType())) {
                                data.setValue(i, data.getValue(i) + 1);
                                updated = true;
                            }
                        }
                    }
                }
            }
            
            if (updated) {
                boolean done = true;
                for (int i = 0; i < parts.size(); i++) {
                    Part part = parts.get(i);
                    
                    if (tamed(i, tamer.getUUID()) < part.count) {
                        done = false;
                        break;
                    }
                }
                
                if (done) {
                    completeTask(tamer.getUUID());
                }
                
                parent.sendUpdatedDataToTeam(tamer);
            }
        }
    }
    
    @Override
    public void write(Adapter.JsonObjectBuilder builder) {
        builder.add(TAME, parts.write(QuestTaskAdapter.TAME_ADAPTER));
    }
    
    @SuppressWarnings("ConstantConditions")
    @Override
    public void read(JsonObject object) {
        parts.read(GsonHelper.getAsJsonArray(object, TAME, new JsonArray()), QuestTaskAdapter.TAME_ADAPTER);
    }
    
    public static class Part extends IconLayoutTask.Part {
        private String mobId;
        private int count = 1;
        
        public String getTame() {
            return mobId;
        }
        
        public void setTame(String part) {
            this.mobId = part;
        }
        
        public int getCount() {
            return count;
        }
        
        public void setCount(int count) {
            this.count = count;
        }
    }
}
