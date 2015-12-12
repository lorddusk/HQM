package hardcorequesting.quests;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import hardcorequesting.EventHandler;
import hardcorequesting.FileVersion;
import hardcorequesting.SaveHelper;
import hardcorequesting.Translator;
import hardcorequesting.client.EditMode;
import hardcorequesting.client.interfaces.*;
import hardcorequesting.network.DataBitHelper;
import hardcorequesting.network.DataReader;
import hardcorequesting.network.DataWriter;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import java.util.Arrays;

public class QuestTaskMob extends QuestTask {


    private static final int Y_OFFSET = 30;
    private static final int X_TEXT_OFFSET = 23;
    private static final int X_TEXT_INDENT = 0;
    private static final int Y_TEXT_OFFSET = 0;
    private static final int ITEM_SIZE = 18;
    public Mob[] mobs = new Mob[0];

    public QuestTaskMob(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
        register(EventHandler.Type.DEATH);
    }

    public static EntityPlayer getKiller(LivingDeathEvent event) {
        if (event.entityLiving != null && !event.entityLiving.worldObj.isRemote && event.source != null) {
            if (event.source.getSourceOfDamage() != null && event.source.getSourceOfDamage() instanceof EntityPlayer) {
                return (EntityPlayer) event.source.getSourceOfDamage();
            } else if (event.entityLiving.func_94060_bK() instanceof EntityPlayer) {
                return (EntityPlayer) event.entityLiving.func_94060_bK();
            }
        }

        return null;
    }

    @Override
    public void onLivingDeath(LivingDeathEvent event) {
        EntityPlayer player = getKiller(event);

        if (player != null && parent.isEnabled(player) && parent.isAvailable(player) && this.isVisible(player) && !isCompleted(player)) {
            boolean updated = false;
            for (int i = 0; i < mobs.length; i++) {
                Mob mob = mobs[i];
                if (mob.count > ((QuestDataTaskMob) getData(player)).killed[i]) {
                    Class clazz = (Class) EntityList.stringToClassMapping.get(mob.mob);
                    if (clazz != null) {
                        if (mob.isExact()) {
                            if (clazz.equals(event.entityLiving.getClass())) {
                                ((QuestDataTaskMob) getData(player)).killed[i]++;
                                updated = true;
                            }
                        } else {
                            if (clazz.isAssignableFrom(event.entityLiving.getClass())) {
                                ((QuestDataTaskMob) getData(player)).killed[i]++;
                                updated = true;
                            }
                        }
                    }
                }
            }

            if (updated) {
                boolean done = true;
                for (int i = 0; i < mobs.length; i++) {
                    Mob mob = mobs[i];

                    if (killed(i, player) < mob.count) {
                        done = false;
                        break;
                    }
                }

                if (done) {
                    completeTask(player.getGameProfile().getName());
                }

                parent.sendUpdatedDataToTeam(player);
            }
        }

    }

    public void setMob(int id, Mob mob, EntityPlayer player) {
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

    public void setIcon(int id, ItemStack item, EntityPlayer player) {
        setMob(id, id >= mobs.length ? new Mob() : mobs[id], player);

        mobs[id].icon = item;
    }

    public void setName(int id, String str, EntityPlayer player) {
        setMob(id, id >= mobs.length ? new Mob() : mobs[id], player);

        mobs[id].name = str;
    }

    private Mob[] getEditFriendlyMobs(Mob[] mobs) {
        if (Quest.isEditing && mobs.length < DataBitHelper.TASK_MOB_COUNT.getMaximum()) {
            mobs = Arrays.copyOf(mobs, mobs.length + 1);
            mobs[mobs.length - 1] = new Mob();
            return mobs;
        } else {
            return mobs;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void draw(GuiQuestBook gui, EntityPlayer player, int mX, int mY) {
        Mob[] mobs = getEditFriendlyMobs(this.mobs);
        for (int i = 0; i < mobs.length; i++) {
            Mob mob = mobs[i];

            int x = START_X;
            int y = START_Y + i * Y_OFFSET;
            gui.drawItem(mob.icon, x, y, mX, mY, false);
            gui.drawString(mob.name, x + X_TEXT_OFFSET, y + Y_TEXT_OFFSET, 0x404040);

            int killed = killed(i, player);
            if (killed == mob.count) {
                gui.drawString(GuiColor.GREEN + Translator.translate("hqm.mobTask.allKilled"), x + X_TEXT_OFFSET + X_TEXT_INDENT, y + Y_TEXT_OFFSET + 9, 0.7F, 0x404040);
            } else {
                gui.drawString(Translator.translate("hqm.mobTask.partKills", killed, (100 * killed / mob.count)), x + X_TEXT_OFFSET + X_TEXT_INDENT, y + Y_TEXT_OFFSET + 9, 0.7F, 0x404040);
            }
            gui.drawString(Translator.translate("hqm.mobTask.totalKills", mob.count), x + X_TEXT_OFFSET + X_TEXT_INDENT, y + Y_TEXT_OFFSET + 15, 0.7F, 0x404040);
        }
    }

    private int killed(int id, EntityPlayer player) {
        return id < mobs.length ? ((QuestDataTaskMob) getData(player)).killed[id] : 0;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void onClick(GuiQuestBook gui, EntityPlayer player, int mX, int mY, int b) {
        if (Quest.isEditing && gui.getCurrentMode() != EditMode.NORMAL) {
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
                            gui.setEditMenu(new GuiEditMenuItem(gui, player, mob.icon, i, GuiEditMenuItem.Type.MOB, 1, ItemPrecision.PRECISE));
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
    public void onUpdate(EntityPlayer player, DataReader dr) {

    }

    @Override
    public Class<? extends QuestDataTask> getDataType() {
        return QuestDataTaskMob.class;
    }

    @Override
    public void save(DataWriter dw) {
        dw.writeData(mobs.length, DataBitHelper.TASK_MOB_COUNT);
        for (Mob mob : mobs) {
            mob.save(dw);
        }
    }

    @Override
    public void load(DataReader dr, FileVersion version) {
        int count = dr.readData(DataBitHelper.TASK_MOB_COUNT);
        mobs = new Mob[count];
        for (int i = 0; i < mobs.length; i++) {
            mobs[i] = new Mob();
            mobs[i].load(dr, version);
        }
    }

    public void write(DataWriter dw, QuestDataTask task, boolean light) {
        super.write(dw, task, light);

        if (!light) {
            dw.writeData(((QuestDataTaskMob) task).killed.length, DataBitHelper.TASK_MOB_COUNT);
        }
        for (int val : ((QuestDataTaskMob) task).killed) {
            dw.writeData(val, DataBitHelper.KILL_COUNT);
        }
    }

    @Override
    public void read(DataReader dr, QuestDataTask task, FileVersion version, boolean light) {
        super.read(dr, task, version, light);

        QuestDataTaskMob mobData = ((QuestDataTaskMob) task);

        if (light) {
            int[] killed = mobData.killed;
            for (int i = 0; i < killed.length; i++) {
                killed[i] = dr.readData(DataBitHelper.KILL_COUNT);
            }
        } else {
            int count = dr.readData(DataBitHelper.TASK_MOB_COUNT);
            int[] killed = mobData.killed;
            for (int i = 0; i < count; i++) {
                int val = dr.readData(DataBitHelper.KILL_COUNT);
                if (i < killed.length) {
                    killed[i] = Math.min(mobs[i].count, Math.max(0, val));
                }
            }
        }
    }

    @Override
    public float getCompletedRatio(String playerName) {
        int killed = 0;
        int total = 0;

        for (int i = 0; i < mobs.length; i++) {
            killed += ((QuestDataTaskMob) getData(playerName)).killed[i];
            total += mobs[i].count;
        }

        return (float) killed / total;
    }

    @Override
    public void mergeProgress(String playerName, QuestDataTask own, QuestDataTask other) {
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
            completeTask(playerName);
        }
    }

    @Override
    public void copyProgress(QuestDataTask own, QuestDataTask other) {
        super.copyProgress(own, other);
        int[] killed = ((QuestDataTaskMob) own).killed;
        System.arraycopy(((QuestDataTaskMob) other).killed, 0, killed, 0, killed.length);
    }

    @Override
    public void autoComplete(String playerName) {
        int[] killed = ((QuestDataTaskMob) getData(playerName)).killed;
        for (int i = 0; i < killed.length; i++) {
            killed[i] = mobs[i].count;
        }
    }

    public class Mob {
        private ItemStack icon;
        private String name = "New";
        private String mob;
        private int count = 1;
        private boolean exact;

        public Mob copy() {
            Mob other = new Mob();
            other.icon = icon == null ? null : icon.copy();
            other.name = name;
            other.mob = mob;
            other.count = count;
            other.exact = exact;

            return other;
        }

        public void save(DataWriter dw) {
            dw.writeBoolean(icon != null);
            if (icon != null) {
                dw.writeItem(icon.getItem());
                dw.writeData(icon.getItemDamage(), DataBitHelper.SHORT);
                dw.writeNBT(icon.getTagCompound());
            }
            dw.writeString(name, DataBitHelper.NAME_LENGTH);
            dw.writeString(mob, DataBitHelper.MOB_ID_LENGTH);
            dw.writeData(count, DataBitHelper.KILL_COUNT);
            dw.writeBoolean(exact);
        }

        public void load(DataReader dr, FileVersion version) {
            if (dr.readBoolean()) {
                Item item = dr.readItem();
                int dmg = dr.readData(DataBitHelper.SHORT);
                NBTTagCompound compound = dr.readNBT();
                ItemStack itemStack = new ItemStack(item, 1, dmg);
                itemStack.setTagCompound(compound);
                this.icon = itemStack;
            } else {
                this.icon = null;
            }
            name = dr.readString(DataBitHelper.NAME_LENGTH);
            mob = dr.readString(DataBitHelper.MOB_ID_LENGTH);
            count = dr.readData(DataBitHelper.KILL_COUNT);
            exact = dr.readBoolean();
        }

        public ItemStack getIcon() {
            return icon;
        }

        public void setIcon(ItemStack icon) {
            this.icon = icon;
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

        public boolean isExact() {
            return exact;
        }

        public void setExact(boolean exact) {
            this.exact = exact;
        }
    }
}
