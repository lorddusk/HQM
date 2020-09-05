package hardcorequesting.quests.task;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.client.EditMode;
import hardcorequesting.client.interfaces.GuiColor;
import hardcorequesting.client.interfaces.GuiQuestBook;
import hardcorequesting.client.interfaces.edit.GuiEditMenuItem;
import hardcorequesting.client.interfaces.edit.GuiEditMenuMob;
import hardcorequesting.client.interfaces.edit.GuiEditMenuTextEditor;
import hardcorequesting.event.EventTrigger;
import hardcorequesting.quests.ItemPrecision;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.data.QuestDataTask;
import hardcorequesting.quests.data.QuestDataTaskMob;
import hardcorequesting.util.SaveHelper;
import hardcorequesting.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.UUID;

public class QuestTaskMob extends QuestTask {
    
    
    private static final int Y_OFFSET = 30;
    private static final int X_TEXT_OFFSET = 23;
    private static final int X_TEXT_INDENT = 0;
    private static final int Y_TEXT_OFFSET = 0;
    private static final int ITEM_SIZE = 18;
    public Mob[] mobs = new Mob[0];
    
    public QuestTaskMob(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
        register(EventTrigger.Type.DEATH);
    }
    
    public static Player getKiller(DamageSource source) {
        Entity entity = source.getEntity();
        
        if (entity instanceof Player) {
            return (Player) entity;
        }
        
        return null;
    }
    
    public void setMob(int id, Mob mob, Player player) {
        if (id >= mobs.length) {
            mobs = Arrays.copyOf(mobs, mobs.length + 1);
            QuestDataTaskMob data = (QuestDataTaskMob) getData(player);
            data.killed = Arrays.copyOf(data.killed, data.killed.length + 1);
            SaveHelper.add(SaveHelper.EditType.MONSTER_CREATE);
        } else {
            SaveHelper.add(SaveHelper.EditType.MONSTER_CHANGE);
        }
        
        mobs[id] = mob;
    }
    
    public void setIcon(int id, ItemStack stack, Player player) {
        System.out.println(stack);
        setMob(id, id >= mobs.length ? new Mob() : mobs[id], player);
        
        mobs[id].iconStack = stack;
    }
    
    public void setName(int id, String str, Player player) {
        setMob(id, id >= mobs.length ? new Mob() : mobs[id], player);
        
        mobs[id].name = str;
    }
    
    @Environment(EnvType.CLIENT)
    private Mob[] getEditFriendlyMobs(Mob[] mobs) {
        if (Quest.canQuestsBeEdited()) {
            mobs = Arrays.copyOf(mobs, mobs.length + 1);
            mobs[mobs.length - 1] = new Mob();
            return mobs;
        } else {
            return mobs;
        }
    }
    
    private int killed(int id, Player player) {
        return id < mobs.length ? ((QuestDataTaskMob) getData(player)).killed[id] : 0;
    }
    
    @Override
    public Class<? extends QuestDataTask> getDataType() {
        return QuestDataTaskMob.class;
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void draw(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY) {
        Mob[] mobs = getEditFriendlyMobs(this.mobs);
        for (int i = 0; i < mobs.length; i++) {
            Mob mob = mobs[i];
            
            int x = START_X;
            int y = START_Y + i * Y_OFFSET;
            gui.drawItemStack(mob.iconStack, x, y, mX, mY, false);
            gui.drawString(matrices, Translator.plain(mob.name), x + X_TEXT_OFFSET, y + Y_TEXT_OFFSET, 0x404040);
            
            int killed = killed(i, player);
            if (killed == mob.count) {
                gui.drawString(matrices, Translator.translated("hqm.mobTask.allKilled", GuiColor.GREEN), x + X_TEXT_OFFSET + X_TEXT_INDENT, y + Y_TEXT_OFFSET + 9, 0.7F, 0x404040);
            } else {
                gui.drawString(matrices, Translator.translated("hqm.mobTask.partKills", killed, (100 * killed / mob.count)), x + X_TEXT_OFFSET + X_TEXT_INDENT, y + Y_TEXT_OFFSET + 9, 0.7F, 0x404040);
            }
            gui.drawString(matrices, Translator.translated("hqm.mobTask.totalKills", mob.count), x + X_TEXT_OFFSET + X_TEXT_INDENT, y + Y_TEXT_OFFSET + 15, 0.7F, 0x404040);
        }
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void onClick(GuiQuestBook gui, Player player, int mX, int mY, int b) {
        if (Quest.canQuestsBeEdited() && gui.getCurrentMode() != EditMode.NORMAL) {
            Mob[] mobs = getEditFriendlyMobs(this.mobs);
            for (int i = 0; i < mobs.length; i++) {
                Mob mob = mobs[i];
                
                int x = START_X;
                int y = START_Y + i * Y_OFFSET;
                
                if (gui.inBounds(x, y, ITEM_SIZE, ITEM_SIZE, mX, mY)) {
                    switch (gui.getCurrentMode()) {
                        case MOB:
                            gui.setEditMenu(new GuiEditMenuMob(gui, this, mob.copy(), i, player));
                            break;
                        case ITEM:
                            gui.setEditMenu(new GuiEditMenuItem(gui, player, mob.iconStack, i, GuiEditMenuItem.Type.MOB, 1, ItemPrecision.PRECISE));
                            break;
                        case RENAME:
                            gui.setEditMenu(new GuiEditMenuTextEditor(gui, player, this, i, mob));
                            break;
                        case DELETE:
                            if (i < this.mobs.length) {
                                Mob[] newMobs = new Mob[this.mobs.length - 1];
                                int id = 0;
                                for (int j = 0; j < this.mobs.length; j++) {
                                    if (j != i) {
                                        newMobs[id] = this.mobs[j];
                                        id++;
                                    }
                                }
                                this.mobs = newMobs;
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
        
        for (int i = 0; i < mobs.length; i++) {
            killed += ((QuestDataTaskMob) getData(playerID)).killed[i];
            total += mobs[i].count;
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
            if (killed[i] < mobs[i].count) {
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
                killed[i] = mobs[i].count;
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
            for (int i = 0; i < mobs.length; i++) {
                Mob mob = mobs[i];
                if (mob.count > ((QuestDataTaskMob) getData(killer)).killed[i]) {
                    EntityType<?> type = Registry.ENTITY_TYPE.get(new ResourceLocation(mob.mob));
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
                for (int i = 0; i < mobs.length; i++) {
                    Mob mob = mobs[i];
                    
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
    
    public class Mob {
        
        private ItemStack iconStack = ItemStack.EMPTY;
        private String name = "New";
        private String mob;
        private int count = 1;
        private boolean exact;
        
        public Mob copy() {
            Mob other = new Mob();
            other.iconStack = iconStack.isEmpty() ? ItemStack.EMPTY : iconStack.copy();
            other.name = name;
            other.mob = mob;
            other.count = count;
            other.exact = exact;
            
            return other;
        }
        
        public ItemStack getIconStack() {
            return iconStack;
        }
        
        public void setIconStack(@NotNull ItemStack iconStack) {
            this.iconStack = iconStack;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getMob() {
            return mob;
        }
        
        public void setMob(String mob) {
            this.mob = mob;
        }
        
        public int getCount() {
            return count;
        }
        
        public void setCount(int count) {
            this.count = count;
        }
    }
}
