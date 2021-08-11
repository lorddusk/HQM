package hardcorequesting.common.quests.task;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import hardcorequesting.common.quests.data.QuestDataTask;
import hardcorequesting.common.quests.data.QuestDataTaskMob;
import hardcorequesting.common.util.SaveHelper;
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
public class KillMobsTask extends IconQuestTask<KillMobsTask.Mob> {
    private static final String MOBS = "mobs";
    
    public KillMobsTask(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
        register(EventTrigger.Type.DEATH);
    }
    
    @Override
    protected Mob createEmpty() {
        return new Mob();
    }
    
    @Override
    protected void onAddElement(Player player) {
        SaveHelper.add(SaveHelper.EditType.MONSTER_CREATE);
    }
    
    @Override
    protected void onModifyElement() {
        SaveHelper.add(SaveHelper.EditType.MONSTER_CHANGE);
    }
    
    @Override
    protected void onRemoveElement() {
        SaveHelper.add(SaveHelper.EditType.MONSTER_REMOVE);
    }
    
    public static Player getKiller(DamageSource source) {
        Entity entity = source.getEntity();
        
        if (entity instanceof Player) {
            return (Player) entity;
        }
        
        return null;
    }
    
    private void setInfo(int id, ResourceLocation mobId, int amount, Player player) {
        Mob mob = getOrCreateForModify(id, player);
        mob.setMob(mobId);
        mob.setCount(amount);
    }
    
    private int killed(int id, Player player) {
        return ((QuestDataTaskMob) getData(player)).getValue(id);
    }
    
    @Override
    public Class<? extends QuestDataTask> getDataType() {
        return QuestDataTaskMob.class;
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    protected void drawElementText(PoseStack matrices, GuiQuestBook gui, Player player, Mob mob, int index, int x, int y) {
        int killed = killed(index, player);
        if (killed == mob.count) {
            gui.drawString(matrices, Translator.translatable("hqm.mobTask.allKilled", GuiColor.GREEN), x, y, 0.7F, 0x404040);
        } else {
            gui.drawString(matrices, Translator.translatable("hqm.mobTask.partKills", killed, (100 * killed / mob.count)), x, y, 0.7F, 0x404040);
        }
        gui.drawString(matrices, Translator.translatable("hqm.mobTask.totalKills", mob.count), x, y + 6, 0.7F, 0x404040);
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    protected void handleElementEditClick(GuiQuestBook gui, Player player, EditMode mode, int id, Mob mob) {
        if (mode == EditMode.MOB) {
            PickMobMenu.display(gui, player, mob.getMob(), mob.getCount(), "mobTask",
                    result -> setInfo(id, result.getMobId(), result.getAmount(), player));
        }
    }
    
    @Override
    public void onUpdate(Player player) {
        
    }
    
    @Override
    public float getCompletedRatio(UUID playerID) {
        int killed = 0;
        int total = 0;
        
        for (int i = 0; i < elements.size(); i++) {
            int req = elements.get(i).count;
            killed += Math.min(req, ((QuestDataTaskMob) getData(playerID)).getValue(i));
            total += req;
        }
        
        return (float) killed / total;
    }
    
    @Override
    public void mergeProgress(UUID playerID, QuestDataTask own, QuestDataTask other) {
        QuestDataTaskMob data = (QuestDataTaskMob) own;
        data.merge((QuestDataTaskMob) other);
        
        boolean all = true;
        for (int i = 0; i < elements.size(); i++) {
            if (data.getValue(i) < elements.get(i).count) {
                all = false;
                break;
            }
        }
        
        if (all) {
            completeTask(playerID);
        }
    }
    
    @Override
    public void autoComplete(UUID playerID, boolean status) {
        QuestDataTaskMob data = (QuestDataTaskMob) getData(playerID);
        if (status) {
            for (int i = 0; i < elements.size(); i++) {
                data.setValue(i, elements.get(i).count);
            }
        } else {
            for (int i = 0; i < elements.size(); i++) {
                data.setValue(i, elements.get(i).count);
            }
        }
    }
    
    @Override
    public void copyProgress(QuestDataTask own, QuestDataTask other) {
        own.update(other);
    }
    
    @Override
    public void onLivingDeath(LivingEntity entity, DamageSource source) {
        Player killer = getKiller(source);
        
        if (killer != null && parent.isEnabled(killer) && parent.isAvailable(killer) && this.isVisible(killer) && !isCompleted(killer)) {
            QuestDataTaskMob data = (QuestDataTaskMob) getData(killer);
            boolean updated = false;
            for (int i = 0; i < elements.size(); i++) {
                Mob mob = elements.get(i);
                if (mob.count > data.getValue(i)) {
                    EntityType<?> type = Registry.ENTITY_TYPE.get(mob.mob);
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
                for (int i = 0; i < elements.size(); i++) {
                    Mob mob = elements.get(i);
                    
                    if (killed(i, killer) < mob.count) {
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
        Adapter.JsonArrayBuilder array = Adapter.array();
        for (Mob mob : elements) {
            array.add(QuestTaskAdapter.MOB_ADAPTER.toJsonTree(mob));
        }
        builder.add(MOBS, array.build());
    }
    
    @Override
    public void read(JsonObject object) {
        elements.clear();
        for (JsonElement element : GsonHelper.getAsJsonArray(object, MOBS, new JsonArray())) {
            Mob mob = QuestTaskAdapter.MOB_ADAPTER.fromJsonTree(element);
            if (mob != null)
                elements.add(mob);
        }
    }
    
    public static class Mob extends IconTask {
        
        private ResourceLocation mob = Registry.ENTITY_TYPE.getDefaultKey();
        private int count = 1;
        
        public ResourceLocation getMob() {
            return mob;
        }
        
        public void setMob(ResourceLocation mob) {
            if (Registry.ENTITY_TYPE.getOptional(mob).isPresent()) {
                this.mob = mob;
            } else {
                this.mob = Registry.ENTITY_TYPE.getDefaultKey();
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
