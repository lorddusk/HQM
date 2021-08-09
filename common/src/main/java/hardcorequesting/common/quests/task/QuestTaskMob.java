package hardcorequesting.common.quests.task;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiColor;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenuMob;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenuTextEditor;
import hardcorequesting.common.client.interfaces.edit.PickItemMenu;
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
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class QuestTaskMob extends IconQuestTask<QuestTaskMob.Mob> {
    private static final String MOBS = "mobs";
    private static final int Y_OFFSET = 30;
    private static final int X_TEXT_OFFSET = 23;
    private static final int X_TEXT_INDENT = 0;
    private static final int Y_TEXT_OFFSET = 0;
    private static final int ITEM_SIZE = 18;
    
    public QuestTaskMob(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
        register(EventTrigger.Type.DEATH);
    }
    
    @Override
    protected Mob createEmpty() {
        return new Mob();
    }
    
    public static Player getKiller(DamageSource source) {
        Entity entity = source.getEntity();
        
        if (entity instanceof Player) {
            return (Player) entity;
        }
        
        return null;
    }
    
    public void setMob(int id, Mob mob, Player player) {
        if (id >= elements.size()) {
            elements.add(mob);
            QuestDataTaskMob data = (QuestDataTaskMob) getData(player);
            data.killed = Arrays.copyOf(data.killed, data.killed.length + 1);
            SaveHelper.add(SaveHelper.EditType.MONSTER_CREATE);
        } else {
            elements.set(id, mob);
            SaveHelper.add(SaveHelper.EditType.MONSTER_CHANGE);
        }
    }
    
    public void setIcon(int id, ItemStack stack, Player player) {
        if (stack.isEmpty()) return;
        
        setMob(id, id >= elements.size() ? createEmpty() : elements.get(id), player);
    
        elements.get(id).setIconStack(stack);
    }
    
    public void setName(int id, String str, Player player) {
        setMob(id, id >= elements.size() ? createEmpty() : elements.get(id), player);
    
        elements.get(id).setName(str);
    }
    
    private void setInfo(int id, ResourceLocation mobId, int amount, Player player) {
        setMob(id, id >= elements.size() ? createEmpty() : elements.get(id), player);
        
        Mob mob = elements.get(id);
        mob.setMob(mobId);
        mob.setCount(amount);
    }
    
    private int killed(int id, Player player) {
        return id < elements.size() ? ((QuestDataTaskMob) getData(player)).killed[id] : 0;
    }
    
    @Override
    public Class<? extends QuestDataTask> getDataType() {
        return QuestDataTaskMob.class;
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void draw(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY) {
        List<Mob> mobs = getShownElements();
        for (int i = 0; i < mobs.size(); i++) {
            Mob mob = mobs.get(i);
            
            int x = START_X;
            int y = START_Y + i * Y_OFFSET;
            gui.drawItemStack(mob.getIconStack(), x, y, mX, mY, false);
            gui.drawString(matrices, Translator.plain(mob.getName()), x + X_TEXT_OFFSET, y + Y_TEXT_OFFSET, 0x404040);
            
            int killed = killed(i, player);
            if (killed == mob.count) {
                gui.drawString(matrices, Translator.translatable("hqm.mobTask.allKilled", GuiColor.GREEN), x + X_TEXT_OFFSET + X_TEXT_INDENT, y + Y_TEXT_OFFSET + 9, 0.7F, 0x404040);
            } else {
                gui.drawString(matrices, Translator.translatable("hqm.mobTask.partKills", killed, (100 * killed / mob.count)), x + X_TEXT_OFFSET + X_TEXT_INDENT, y + Y_TEXT_OFFSET + 9, 0.7F, 0x404040);
            }
            gui.drawString(matrices, Translator.translatable("hqm.mobTask.totalKills", mob.count), x + X_TEXT_OFFSET + X_TEXT_INDENT, y + Y_TEXT_OFFSET + 15, 0.7F, 0x404040);
        }
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void onClick(GuiQuestBook gui, Player player, int mX, int mY, int b) {
        if (Quest.canQuestsBeEdited() && gui.getCurrentMode() != EditMode.NORMAL) {
            List<Mob> mobs = getShownElements();
            for (int i = 0; i < mobs.size(); i++) {
                Mob mob = mobs.get(i);
                
                int x = START_X;
                int y = START_Y + i * Y_OFFSET;
                
                if (gui.inBounds(x, y, ITEM_SIZE, ITEM_SIZE, mX, mY)) {
                    final int taskId = i;
                    switch (gui.getCurrentMode()) {
                        case MOB:
                            GuiEditMenuMob.display(gui, player, mob.getMob(), mob.getCount(),
                                    result -> setInfo(taskId, result.getMobId(), result.getAmount(), player));
                            break;
                        case ITEM:
                            PickItemMenu.display(gui, player, mob.getIconStack(), PickItemMenu.Type.ITEM,
                                    result -> this.setIcon(taskId, result.get(), player));
                            break;
                        case RENAME:
                            gui.setEditMenu(new GuiEditMenuTextEditor(gui, player, this, i, mob));
                            break;
                        case DELETE:
                            if (i < this.elements.size()) {
                                elements.remove(i);
                                SaveHelper.add(SaveHelper.EditType.MONSTER_REMOVE);
                            }
                            break;
                        default:
                    }
                    
                    break;
                }
            }
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
            killed += ((QuestDataTaskMob) getData(playerID)).killed[i];
            total += elements.get(i).count;
        }
        
        return (float) killed / total;
    }
    
    @Override
    public void mergeProgress(UUID playerID, QuestDataTask own, QuestDataTask other) {
        int[] killed = ((QuestDataTaskMob) own).killed;
        int[] otherKilled = ((QuestDataTaskMob) other).killed;
        
        boolean all = true;
        for (int i = 0; i < killed.length; i++) {
            killed[i] = Math.max(killed[i], otherKilled[i]);
            if (killed[i] < elements.get(i).count) {
                all = false;
            }
        }
        
        if (all) {
            completeTask(playerID);
        }
    }
    
    @Override
    public void autoComplete(UUID playerID, boolean status) {
        int[] killed = ((QuestDataTaskMob) getData(playerID)).killed;
        for (int i = 0; i < killed.length; i++) {
            if (status) {
                killed[i] = elements.get(i).count;
            } else {
                killed[i] = 0;
            }
        }
    }
    
    @Override
    public void copyProgress(QuestDataTask own, QuestDataTask other) {
        super.copyProgress(own, other);
        int[] killed = ((QuestDataTaskMob) own).killed;
        System.arraycopy(((QuestDataTaskMob) other).killed, 0, killed, 0, killed.length);
    }
    
    @Override
    public void onLivingDeath(LivingEntity entity, DamageSource source) {
        Player killer = getKiller(source);
        
        if (killer != null && parent.isEnabled(killer) && parent.isAvailable(killer) && this.isVisible(killer) && !isCompleted(killer)) {
            boolean updated = false;
            for (int i = 0; i < elements.size(); i++) {
                Mob mob = elements.get(i);
                if (mob.count > ((QuestDataTaskMob) getData(killer)).killed[i]) {
                    EntityType<?> type = Registry.ENTITY_TYPE.get(mob.mob);
                    if (type != null) {
                        if (type.equals(entity.getType())) {
                            ((QuestDataTaskMob) getData(killer)).killed[i]++;
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
