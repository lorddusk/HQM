package hardcorequesting.common.quests.task.icon;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiColor;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.PickMobMenu;
import hardcorequesting.common.event.EventTrigger;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.data.MobTaskData;
import hardcorequesting.common.team.Team;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

/**
 * A task where the player has to kill certain mobs.
 */
public class KillMobsTask extends IconLayoutTask<KillMobsTask.Part, MobTaskData> {
    private static final String MOBS = "mobs";
    
    public KillMobsTask(Quest parent, String description, String longDescription) {
        super(MobTaskData.class, EditType.Type.MONSTER, parent, description, longDescription);
        register(EventTrigger.Type.DEATH);
    }
    
    @Override
    protected Part createEmpty() {
        return new Part();
    }
    
    public static Player getKiller(DamageSource source) {
        Entity entity = source.getEntity();
        
        if (entity instanceof Player) {
            return (Player) entity;
        }
        
        return null;
    }
    
    private void setInfo(int id, ResourceLocation mobId, int amount) {
        Part part = parts.getOrCreateForModify(id);
        part.setMob(mobId);
        part.setCount(amount);
    }
    
    private int killed(int id, Player player) {
        return getData(player).getValue(id);
    }
    
    @Override
    public MobTaskData newQuestData() {
        return new MobTaskData(parts.size());
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    protected void drawElementText(PoseStack matrices, GuiQuestBook gui, Player player, Part part, int index, int x, int y) {
        int killed = killed(index, player);
        if (killed == part.count) {
            gui.drawString(matrices, Translator.translatable("hqm.mobTask.allKilled", GuiColor.GREEN), x, y, 0.7F, 0x404040);
        } else {
            gui.drawString(matrices, Translator.translatable("hqm.mobTask.partKills", killed, (100 * killed / part.count)), x, y, 0.7F, 0x404040);
        }
        gui.drawString(matrices, Translator.translatable("hqm.mobTask.totalKills", part.count), x, y + 6, 0.7F, 0x404040);
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    protected void handleElementEditClick(GuiQuestBook gui, Player player, EditMode mode, int id, Part part) {
        if (mode == EditMode.MOB) {
            PickMobMenu.display(gui, player, part.getMob(), part.getCount(), "mobTask",
                    result -> setInfo(id, result.getMobId(), result.getAmount()));
        }
    }
    
    @Override
    public void onUpdate(Player player) {
        
    }
    
    @Override
    public float getCompletedRatio(Team team) {
        MobTaskData data = getData(team);
        int killed = 0;
        int total = 0;
        
        for (int i = 0; i < parts.size(); i++) {
            int req = parts.get(i).count;
            killed += Math.min(req, data.getValue(i));
            total += req;
        }
        
        return (float) killed / total;
    }
    
    @Override
    public void mergeProgress(UUID playerID, MobTaskData own, MobTaskData other) {
        own.merge(other);
        
        boolean all = true;
        for (int i = 0; i < parts.size(); i++) {
            if (own.getValue(i) < parts.get(i).count) {
                all = false;
                break;
            }
        }
        
        if (all) {
            completeTask(playerID);
        }
    }
    
    
    @Override
    public void setComplete(MobTaskData data) {
        for (int i = 0; i < parts.size(); i++) {
            data.setValue(i, parts.get(i).count);
        }
        data.completed = true;
    }
    
    @Override
    public void copyProgress(MobTaskData own, MobTaskData other) {
        own.update(other);
    }
    
    @Override
    public void onLivingDeath(LivingEntity entity, DamageSource source) {
        Player killer = getKiller(source);
        
        if (killer != null && parent.isEnabled(killer) && parent.isAvailable(killer) && this.isVisible(killer) && !isCompleted(killer)) {
            MobTaskData data = getData(killer);
            boolean updated = false;
            for (int i = 0; i < parts.size(); i++) {
                Part part = parts.get(i);
                if (part.count > data.getValue(i)) {
                    EntityType<?> type = Registry.ENTITY_TYPE.get(part.mobId);
                    if (type != null) {
                        if (type.equals(entity.getType())) {
                            data.setValue(i, data.getValue(i) + 1);
                            updated = true;
                        }
                    }
                }
            }
            
            if (updated) {
                boolean done = true;
                for (int i = 0; i < parts.size(); i++) {
                    Part part = parts.get(i);
                    
                    if (killed(i, killer) < part.count) {
                        done = false;
                        break;
                    }
                }
                
                if (done) {
                    completeTask(killer.getUUID());
                }
                
                parent.sendUpdatedDataToTeam(killer);
            }
        }
        
    }
    
    @Override
    public void write(Adapter.JsonObjectBuilder builder) {
        builder.add(MOBS, parts.write(QuestTaskAdapter.MOB_ADAPTER));
    }
    
    @SuppressWarnings("ConstantConditions")
    @Override
    public void read(JsonObject object) {
        parts.read(GsonHelper.getAsJsonArray(object, MOBS, new JsonArray()), QuestTaskAdapter.MOB_ADAPTER);
    }
    
    public static class Part extends IconLayoutTask.Part {
        
        private ResourceLocation mobId = Registry.ENTITY_TYPE.getDefaultKey();
        private int count = 1;
        
        public ResourceLocation getMob() {
            return mobId;
        }
        
        public void setMob(ResourceLocation mobId) {
            if (Registry.ENTITY_TYPE.getOptional(mobId).isPresent()) {
                this.mobId = mobId;
            } else {
                this.mobId = Registry.ENTITY_TYPE.getDefaultKey();
            }
        }
        
        public int getCount() {
            return count;
        }
        
        public void setCount(int count) {
            this.count = count;
        }
    }
}
