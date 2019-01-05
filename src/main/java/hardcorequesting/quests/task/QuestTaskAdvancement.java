package hardcorequesting.quests.task;

import hardcorequesting.client.EditMode;
import hardcorequesting.client.interfaces.GuiColor;
import hardcorequesting.client.interfaces.GuiQuestBook;
import hardcorequesting.client.interfaces.edit.GuiEditMenuAdvancement;
import hardcorequesting.client.interfaces.edit.GuiEditMenuItem;
import hardcorequesting.client.interfaces.edit.GuiEditMenuTextEditor;
import hardcorequesting.event.EventTrigger;
import hardcorequesting.quests.ItemPrecision;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.data.QuestDataTask;
import hardcorequesting.quests.data.QuestDataTaskAdvancement;
import hardcorequesting.util.SaveHelper;
import hardcorequesting.util.Translator;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.UUID;

public class QuestTaskAdvancement extends QuestTask {
    private static final int Y_OFFSET = 30;
    private static final int X_TEXT_OFFSET = 23;
    private static final int X_TEXT_INDENT = 0;
    private static final int Y_TEXT_OFFSET = 0;
    private static final int ITEM_SIZE = 18;
    public AdvancementTask[] advancements = new AdvancementTask[0];

    public QuestTaskAdvancement(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);

        register(EventTrigger.Type.ADVANCEMENT, EventTrigger.Type.OPEN_BOOK);
    }

    @SideOnly(Side.CLIENT)
    private AdvancementTask[] getEditFriendlyAdvancements(AdvancementTask[] advancements) {
        if (Quest.canQuestsBeEdited(Minecraft.getMinecraft().player)) {
            advancements = Arrays.copyOf(advancements, advancements.length + 1);
            advancements[advancements.length - 1] = new AdvancementTask();
            return advancements;
        } else {
            return advancements;
        }
    }

    private boolean advanced(int id, EntityPlayer player) {
        return id < advancements.length && ((QuestDataTaskAdvancement) getData(player)).advanced[id];
    }

    public void setAdvancement(int id, AdvancementTask advancement, EntityPlayer player) {
        if (id >= advancements.length) {
            advancements = Arrays.copyOf(advancements, advancements.length + 1);
            QuestDataTaskAdvancement data = (QuestDataTaskAdvancement) getData(player);
            data.advanced = Arrays.copyOf(data.advanced, data.advanced.length + 1);
            SaveHelper.add(SaveHelper.EditType.LOCATION_CREATE);
        } else {
            SaveHelper.add(SaveHelper.EditType.LOCATION_CHANGE);
        }

        advancements[id] = advancement;
    }

    public void setIcon(int id, ItemStack stack, EntityPlayer player) {
        setAdvancement(id, id >= advancements.length ? new AdvancementTask() : advancements[id], player);

        advancements[id].iconStack = stack;
    }

    public void setName(int id, String str, EntityPlayer player) {
        setAdvancement(id, id >= advancements.length ? new AdvancementTask() : advancements[id], player);

        advancements[id].name = str;
    }

    @Override
    public Class<? extends QuestDataTask> getDataType() {
        return QuestDataTaskAdvancement.class;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void draw(GuiQuestBook gui, EntityPlayer player, int mX, int mY) {
        AdvancementTask[] advancements = getEditFriendlyAdvancements(this.advancements);
        for (int i = 0; i < advancements.length; i++) {
            AdvancementTask advancement = advancements[i];

            int x = START_X;
            int y = START_Y + i * Y_OFFSET;
            gui.drawItemStack(advancement.iconStack, x, y, mX, mY, false);
            gui.drawString(advancement.name, x + X_TEXT_OFFSET, y + Y_TEXT_OFFSET, 0x404040);

            if (advanced(i, player)) {
                gui.drawString(GuiColor.GREEN + Translator.translate("hqm.advancementMenu.visited"), x + X_TEXT_OFFSET + X_TEXT_INDENT, y + Y_TEXT_OFFSET + 9, 0.7F, 0x404040);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void onClick(GuiQuestBook gui, EntityPlayer player, int mX, int mY, int b) {
        if (Quest.canQuestsBeEdited(player) && gui.getCurrentMode() != EditMode.NORMAL) {
            AdvancementTask[] advancements = getEditFriendlyAdvancements(this.advancements);
            for (int i = 0; i < advancements.length; i++) {
                AdvancementTask advancement = advancements[i];

                int x = START_X;
                int y = START_Y + i * Y_OFFSET;

                if (gui.inBounds(x, y, ITEM_SIZE, ITEM_SIZE, mX, mY)) {
                    switch (gui.getCurrentMode()) {
                        case LOCATION:
                            gui.setEditMenu(new GuiEditMenuAdvancement(gui, this, advancement.copy(), i, player));
                            break;
                        case ITEM:
                            gui.setEditMenu(new GuiEditMenuItem(gui, player, advancement.iconStack, i, GuiEditMenuItem.Type.ADVANCEMENT, 1, ItemPrecision.PRECISE));
                            break;
                        case RENAME:
                            gui.setEditMenu(new GuiEditMenuTextEditor(gui, player, this, i, advancement));
                            break;
                        case DELETE:
                            if (i < this.advancements.length) {
                                AdvancementTask[] newAdvancements = new AdvancementTask[this.advancements.length - 1];
                                int id = 0;
                                for (int j = 0; j < this.advancements.length; j++) {
                                    if (j != i) {
                                        newAdvancements[id] = this.advancements[j];
                                        id++;
                                    }
                                }
                                this.advancements = newAdvancements;
                                SaveHelper.add(SaveHelper.EditType.LOCATION_REMOVE);
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
    public void onAdvancement(AdvancementEvent event) {
        checkAdvancement(event.getEntityPlayer());
    }

    @Override
    public void onUpdate(EntityPlayer player) {
        checkAdvancement(player);
    }

    private void checkAdvancement(EntityPlayer player) {
        World world = player.getEntityWorld();
        if (!world.isRemote && !this.isCompleted(player) && player.getServer() != null) {
            boolean[] advanced = ((QuestDataTaskAdvancement) this.getData(player)).advanced;

            if (advanced.length < advancements.length) {
                boolean[] oldVisited = ArrayUtils.addAll(advanced, (boolean[]) null);
                advanced = new boolean[advancements.length];
                System.arraycopy(oldVisited, 0, advanced, 0, oldVisited.length);
                ((QuestDataTaskAdvancement) this.getData(player)).advanced = advanced;
            }

            boolean completed = true;
            AdvancementManager manager = player.getServer().getAdvancementManager();
            PlayerAdvancements playerAdvancements = player.getServer().getPlayerList().getPlayerAdvancements((EntityPlayerMP) player);

            for (int i = 0; i < advancements.length; i++) {
                if (advanced[i]) continue;

                AdvancementTask advancement = this.advancements[i];
                if (advancement == null) continue;

                ResourceLocation advResource = new ResourceLocation(advancement.getAdvancement());

                Advancement advAdvancement = manager.getAdvancement(advResource);

                AdvancementProgress progress = playerAdvancements.getProgress(advAdvancement);

                if (progress.isDone()) {
                    advanced[i] = true;
                } else {
                    completed = false;
                }
            }

            if (completed && advancements.length > 0) {
                completeTask(player.getUniqueID());
                parent.sendUpdatedDataToTeam(player);
            }
        }
    }

    @Override
    public float getCompletedRatio(UUID uuid) {
        int advanced = 0;
        for (boolean b : ((QuestDataTaskAdvancement) getData(uuid)).advanced) {
            if (b) {
                advanced++;
            }
        }

        return (float) advanced / advancements.length;
    }

    @Override
    public void mergeProgress(UUID uuid, QuestDataTask own, QuestDataTask other) {
        boolean[] advanced = ((QuestDataTaskAdvancement) own).advanced;
        boolean[] otherVisited = ((QuestDataTaskAdvancement) other).advanced;

        boolean all = true;
        for (int i = 0; i < advanced.length; i++) {
            if (otherVisited[i]) {
                advanced[i] = true;
            } else if (!advanced[i]) {
                all = false;
            }
        }

        if (all) {
            completeTask(uuid);
        }
    }

    @Override
    public void autoComplete(UUID uuid) {
        boolean[] advanced = ((QuestDataTaskAdvancement) getData(uuid)).advanced;
        for (int i = 0; i < advanced.length; i++) {
            advanced[i] = true;
        }
    }

    @Override
    public void copyProgress(QuestDataTask own, QuestDataTask other) {
        super.copyProgress(own, other);
        boolean[] advanced = ((QuestDataTaskAdvancement) own).advanced;
        System.arraycopy(((QuestDataTaskAdvancement) other).advanced, 0, advanced, 0, advanced.length);
    }

    @Override
    public boolean allowDetect () {
        return true;
    }

    public enum Visibility {
        FULL("Full"),
        NONE("None");

        private String id;

        Visibility(String id) {
            this.id = id;
        }

        // TODO: fix these
        public String getName() {
            return Translator.translate("hqm.locationMenu.vis" + id + ".title");
        }

        public String getDescription() {
            return Translator.translate("hqm.locationMenu.vis" + id + ".desc");
        }
    }

    public static class AdvancementTask {

        private ItemStack iconStack = ItemStack.EMPTY;
        private String name = "New";
        private Visibility visible = Visibility.FULL;
        private String adv_name;

        // TODO: Implement the actual storing of the advancement
        private AdvancementTask copy() {
            AdvancementTask advancement = new AdvancementTask();
            advancement.iconStack = iconStack.isEmpty() ? ItemStack.EMPTY : iconStack.copy();
            advancement.name = name;
            advancement.visible = visible;
            advancement.adv_name = adv_name;

            return advancement;
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

        public void unsetAdvancement() {
            this.adv_name = null;
        }
    }
}

