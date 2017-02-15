package hardcorequesting.quests.task;

import hardcorequesting.client.EditMode;
import hardcorequesting.client.interfaces.GuiColor;
import hardcorequesting.client.interfaces.GuiQuestBook;
import hardcorequesting.client.interfaces.edit.GuiEditMenuItem;
import hardcorequesting.client.interfaces.edit.GuiEditMenuMob;
import hardcorequesting.client.interfaces.edit.GuiEditMenuTextEditor;
import hardcorequesting.event.EventHandler;
import hardcorequesting.quests.ItemPrecision;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.data.QuestDataTask;
import hardcorequesting.quests.data.QuestDataTaskMob;
import hardcorequesting.util.SaveHelper;
import hardcorequesting.util.Translator;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
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
        if (event.getEntityLiving() != null && !event.getEntityLiving().getEntityWorld().isRemote && event.getSource() != null) {
            if (event.getSource().getSourceOfDamage() != null && event.getSource().getSourceOfDamage() instanceof EntityPlayer) {
                return (EntityPlayer) event.getSource().getSourceOfDamage();
            } else if (event.getEntityLiving().getCommandSenderEntity() instanceof EntityPlayer) {
                return (EntityPlayer) event.getEntityLiving().getCommandSenderEntity();
            }
        }

        return null;
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

    public void setIcon(int id, ItemStack stack, EntityPlayer player) {
        System.out.println(stack);
        setMob(id, id >= mobs.length ? new Mob() : mobs[id], player);

        mobs[id].iconStack = stack;
    }

    public void setName(int id, String str, EntityPlayer player) {
        setMob(id, id >= mobs.length ? new Mob() : mobs[id], player);

        mobs[id].name = str;
    }

    private Mob[] getEditFriendlyMobs(Mob[] mobs) {
        if (Quest.isEditing) {
            mobs = Arrays.copyOf(mobs, mobs.length + 1);
            mobs[mobs.length - 1] = new Mob();
            return mobs;
        } else {
            return mobs;
        }
    }

    private int killed(int id, EntityPlayer player) {
        return id < mobs.length ? ((QuestDataTaskMob) getData(player)).killed[id] : 0;
    }

    @Override
    public Class<? extends QuestDataTask> getDataType() {
        return QuestDataTaskMob.class;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void draw(GuiQuestBook gui, EntityPlayer player, int mX, int mY) {
        Mob[] mobs = getEditFriendlyMobs(this.mobs);
        for (int i = 0; i < mobs.length; i++) {
            Mob mob = mobs[i];

            int x = START_X;
            int y = START_Y + i * Y_OFFSET;
            gui.drawItemStack(mob.iconStack, x, y, mX, mY, false);
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
    public void onUpdate(EntityPlayer player) {

    }

    @Override
    public float getCompletedRatio(String uuid) {
        int killed = 0;
        int total = 0;

        for (int i = 0; i < mobs.length; i++) {
            killed += ((QuestDataTaskMob) getData(uuid)).killed[i];
            total += mobs[i].count;
        }

        return (float) killed / total;
    }

    @Override
    public void mergeProgress(String uuid, QuestDataTask own, QuestDataTask other) {
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
            completeTask(uuid);
        }
    }

    @Override
    public void autoComplete(String uuid) {
        int[] killed = ((QuestDataTaskMob) getData(uuid)).killed;
        for (int i = 0; i < killed.length; i++) {
            killed[i] = mobs[i].count;
        }
    }

    @Override
    public void copyProgress(QuestDataTask own, QuestDataTask other) {
        super.copyProgress(own, other);
        int[] killed = ((QuestDataTaskMob) own).killed;
        System.arraycopy(((QuestDataTaskMob) other).killed, 0, killed, 0, killed.length);
    }

    @Override
    public void onLivingDeath(LivingDeathEvent event) {
        EntityPlayer player = getKiller(event);

        if (player != null && parent.isEnabled(player) && parent.isAvailable(player) && this.isVisible(player) && !isCompleted(player)) {
            boolean updated = false;
            for (int i = 0; i < mobs.length; i++) {
                Mob mob = mobs[i];
                if (mob.count > ((QuestDataTaskMob) getData(player)).killed[i]) {
                    Class clazz = EntityList.NAME_TO_CLASS.get(mob.mob);
                    if (clazz != null) {
                        if (mob.isExact()) {
                            if (clazz.equals(event.getEntityLiving().getClass())) {
                                ((QuestDataTaskMob) getData(player)).killed[i]++;
                                updated = true;
                            }
                        } else {
                            if (clazz.isAssignableFrom(event.getEntityLiving().getClass())) {
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

    public class Mob {

        private ItemStack iconStack;
        private String name = "New";
        private String mob;
        private int count = 1;
        private boolean exact;

        public Mob copy() {
            Mob other = new Mob();
            other.iconStack = iconStack == null ? null : iconStack.copy();
            other.name = name;
            other.mob = mob;
            other.count = count;
            other.exact = exact;

            return other;
        }

        public ItemStack getIconStack() {
            return iconStack;
        }

        public void setIconStack(@Nonnull ItemStack iconStack) {
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

        public boolean isExact() {
            return exact;
        }

        public void setExact(boolean exact) {
            this.exact = exact;
        }
    }
}
