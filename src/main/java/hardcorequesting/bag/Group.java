package hardcorequesting.bag;

import hardcorequesting.client.interfaces.edit.GuiEditMenuItem;
import hardcorequesting.client.interfaces.edit.GuiEditMenuTextEditor;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.util.SaveHelper;
import hardcorequesting.util.Translator;
import hardcorequesting.client.EditMode;
import hardcorequesting.client.interfaces.*;
import hardcorequesting.quests.ItemPrecision;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestLine;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class Group
{
    public static int size() {
        return QuestLine.getActiveQuestLine().groups.size();
    }

    private GroupTier tier;
    private List<ItemStack> items;
    private String name;
    private int limit;
    private String uuid;

    public Group(String id) {
        this.uuid = id;
        while (this.uuid == null || getGroups().containsKey(this.uuid)) {
            this.uuid = UUID.randomUUID().toString();
        }
        if (id == null)
        {
            if (GroupTier.getTiers().size() < 1)
                GroupTier.initBaseTiers(QuestLine.getActiveQuestLine());
            this.tier = GroupTier.getTiers().get(0);
        }
        items = new ArrayList<>();
    }

    public GroupTier getTier() {
        return tier;
    }

    public static Map<String, Group> getGroups() {
        return QuestLine.getActiveQuestLine().groups;
    }


    public String getName() {
        return hasName() ? name : Translator.translate("hqm.bag.group", tier.getName());
    }

    public boolean hasName() {
        return name != null && !name.isEmpty();
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public void setTier(GroupTier tier) {
        this.tier = tier;
    }

    public void setItem(int id, ItemStack item) {
        if (id >= items.size()) {
            items.add(item);
            SaveHelper.add(SaveHelper.EditType.GROUP_ITEM_CREATE);
        } else {
            items.set(id, item);
            SaveHelper.add(SaveHelper.EditType.GROUP_ITEM_CHANGE);
        }
    }

    public void open(EntityPlayer player) {
        if (limit > 0) {
            GroupData data = QuestingData.getQuestingData(player).getGroupData(getId());
            if (data != null) {
                data.retrieved++;
            }
        }

        List<ItemStack> itemsToAdd = items.stream().map(ItemStack::copy).collect(Collectors.toList());

        Quest.addItems(player, itemsToAdd);

        itemsToAdd.stream().filter(item -> item.stackSize > 0).forEach(item -> {
            EntityItem entityItem = new EntityItem(player.worldObj, player.posX + 0.5D, player.posY + 0.5D, player.posZ + 0.5D, item);
            player.worldObj.spawnEntityInWorld(entityItem);
        });
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId()
    {
        return uuid;
    }

    public void removeItem(int i) {
        if (i >= 0 && i < items.size()) {
            items.remove(i);
        }
    }

    public static void remove(String id) {
        getGroups().remove(id);
    }

    public static void add(Group group) {
        getGroups().put(group.getId(), group);
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getLimit() {
        return limit;
    }

    public static Group getGroup(String id) {
        return Group.getGroups().get(id);
    }

    public int getRetrievalCount(EntityPlayer player) {
        GroupData data = QuestingData.getQuestingData(player).getGroupData(getId());
        return data != null ? data.retrieved : 0;
    }

    public void setRetrievalCount(EntityPlayer player, int count) {
        GroupData data = QuestingData.getQuestingData(player).getGroupData(getId());
        if (data != null) {
            data.retrieved = count;
        }
    }

    public boolean isValid(EntityPlayer player) {
        return limit == 0 || getRetrievalCount(player) < limit;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Group) {
            if (Objects.equals(name, ((Group) obj).name) && limit == ((Group) obj).limit && items.size() == ((Group) obj).items.size()) {
                for (ItemStack stack : items) {
                    if (!listContains(stack, ((Group) obj).items)) return false;
                }
                return true;
            }
        }
        return false;
    }

    private boolean listContains(ItemStack stack, List<ItemStack> stacks) {
        for (ItemStack stack2 : stacks) {
            if (ItemStack.areItemStacksEqual(stack, stack2)) return true;
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    public static void drawOverview(GuiQuestBook gui, ScrollBar tierScroll, ScrollBar groupScroll, int x, int y) {
        List<GroupTier> tiers = GroupTier.getTiers();
        int start = tierScroll.isVisible(gui) ? Math.round((tiers.size() - GuiQuestBook.VISIBLE_TIERS) * tierScroll.getScroll()) : 0;
        for (int i = start; i < Math.min(start + GuiQuestBook.VISIBLE_TIERS, tiers.size()); i++) {
            GroupTier groupTier = tiers.get(i);

            String str = groupTier.getName();
            int yPos = GuiQuestBook.TIERS_Y + GuiQuestBook.TIERS_SPACING * (i - start);
            boolean inBounds = gui.inBounds(GuiQuestBook.TIERS_X, yPos, gui.getStringWidth(str), GuiQuestBook.TEXT_HEIGHT, x, y);
            int color = groupTier.getColor().getHexColor();
            if (inBounds) {
                color &= 0xFFFFFF;
                color |= 0xBB << 24;
                GlStateManager.enableBlend();
            }
            gui.drawString(str, GuiQuestBook.TIERS_X, yPos, color);
            if (inBounds) {
                GlStateManager.disableBlend();
            }

            for (int j = 0; j < BagTier.values().length; j++) {
                BagTier bagTier = BagTier.values()[j];
                gui.drawCenteredString(bagTier.getColor().toString() + groupTier.getWeights()[j],
                        GuiQuestBook.TIERS_X + GuiQuestBook.TIERS_SECOND_LINE_X + j * GuiQuestBook.WEIGHT_SPACING,
                        yPos + GuiQuestBook.TIERS_SECOND_LINE_Y, 0.7F,
                        GuiQuestBook.WEIGHT_SPACING, 0, 0x404040);
            }
        }

        List<Group> groups = new ArrayList<>(Group.getGroups().values());
        start = groupScroll.isVisible(gui) ? Math.round((groups.size() - GuiQuestBook.VISIBLE_GROUPS) * groupScroll.getScroll()) : 0;
        for (int i = start; i < Math.min(start + GuiQuestBook.VISIBLE_GROUPS, groups.size()); i++) {
            Group group = groups.get(i);

            String str = group.getName();
            int yPos = GuiQuestBook.GROUPS_Y + GuiQuestBook.GROUPS_SPACING * (i - start);
            boolean inBounds = gui.inBounds(GuiQuestBook.GROUPS_X, yPos, gui.getStringWidth(str), GuiQuestBook.TEXT_HEIGHT, x, y);
            int color = group.getTier().getColor().getHexColor();
            boolean selected = group == gui.modifyingGroup;
            if (inBounds || selected) {
                color &= 0xFFFFFF;
                GlStateManager.enableBlend();

                if (selected) {
                    color |= 0x50 << 24;
                } else {
                    color |= 0xBB << 24;
                }
            }

            gui.drawString(str, GuiQuestBook.GROUPS_X, yPos, color);
            if (inBounds || selected) {
                GlStateManager.disableBlend();
            }

            gui.drawString(Translator.translate("hqm.questBook.items", group.getItems().size()),
                    GuiQuestBook.GROUPS_X + GuiQuestBook.GROUPS_SECOND_LINE_X,
                    yPos + GuiQuestBook.GROUPS_SECOND_LINE_Y,
                    0.7F, 0x404040);
        }
    }

    @SideOnly(Side.CLIENT)
    public void draw(GuiQuestBook gui, int x, int y) {
        gui.drawString(this.getName(), GuiQuestBook.GROUPS_X, GuiQuestBook.GROUPS_Y, this.getTier().getColor().getHexColor());
        List<ItemStack> items = this.getItems();
        for (int i = 0; i < items.size(); i++) {
            ItemStack itemStack = i < items.size() ? items.get(i) : null;

            int xPos = (i % GuiQuestBook.ITEMS_PER_LINE) * GuiQuestBook.GROUP_ITEMS_SPACING + GuiQuestBook.GROUP_ITEMS_X;
            int yPos = (i / GuiQuestBook.ITEMS_PER_LINE) * GuiQuestBook.GROUP_ITEMS_SPACING + GuiQuestBook.GROUP_ITEMS_Y;

            gui.drawItem(itemStack, xPos, yPos, x, y, false);
        }

        for (int i = 0; i < items.size(); i++) {
            ItemStack itemStack = items.get(i);

            int xPos = (i % GuiQuestBook.ITEMS_PER_LINE) * GuiQuestBook.GROUP_ITEMS_SPACING + GuiQuestBook.GROUP_ITEMS_X;
            int yPos = (i / GuiQuestBook.ITEMS_PER_LINE) * GuiQuestBook.GROUP_ITEMS_SPACING + GuiQuestBook.GROUP_ITEMS_Y;

            if (gui.inBounds(xPos, yPos, GuiQuestBook.ITEM_SIZE, GuiQuestBook.ITEM_SIZE, x, y)) {
                if (itemStack != null && itemStack.getItem() != null) {
                    try {
                        gui.drawMouseOver(itemStack.getTooltip(Minecraft.getMinecraft().thePlayer, Minecraft.getMinecraft().gameSettings.advancedItemTooltips), x + gui.getLeft(), y + gui.getTop());
                    } catch (Exception ignored) {
                    }
                }
                break;
            }
        }

        gui.drawString(Translator.translate("hqm.questBook.maxRetrieval"), 180, 20, 0x404040);
        gui.drawString(Translator.translate("hqm.questBook.noRestriction"), 180, 48, 0.7F, 0x404040);
    }

    @SideOnly(Side.CLIENT)
    public void mouseClicked(GuiQuestBook gui, int x, int y) {
        List<ItemStack> items = this.getItems();
        for (int i = 0; i < items.size(); i++) {
            int xPos = (i % GuiQuestBook.ITEMS_PER_LINE) * GuiQuestBook.GROUP_ITEMS_SPACING + GuiQuestBook.GROUP_ITEMS_X;
            int yPos = (i / GuiQuestBook.ITEMS_PER_LINE) * GuiQuestBook.GROUP_ITEMS_SPACING + GuiQuestBook.GROUP_ITEMS_Y;

            if (gui.inBounds(xPos, yPos, GuiQuestBook.ITEM_SIZE, GuiQuestBook.ITEM_SIZE, x, y)) {
                if (gui.getCurrentMode() == EditMode.ITEM) {
                    ItemStack itemStack = i < items.size() ? items.get(i) : null;
                    int amount;
                    if (itemStack != null) {
                        itemStack = itemStack.copy();
                        amount = itemStack.stackSize;
                    } else {
                        amount = 1;
                    }

                    gui.setEditMenu(new GuiEditMenuItem(gui, gui.getPlayer(), itemStack, i, GuiEditMenuItem.Type.BAG_ITEM, amount, ItemPrecision.PRECISE));
                } else if (gui.getCurrentMode() == EditMode.DELETE) {
                    this.removeItem(i);
                    SaveHelper.add(SaveHelper.EditType.GROUP_ITEM_REMOVE);
                }
                break;
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static void mouseClickedOverview(GuiQuestBook gui, ScrollBar groupScroll, int x, int y) {
        List<Group> groups = new ArrayList<>(getGroups().values());
        int start = groupScroll.isVisible(gui) ? Math.round((groups.size() - GuiQuestBook.VISIBLE_GROUPS) * groupScroll.getScroll()) : 0;
        for (int i = start; i < Math.min(start + GuiQuestBook.VISIBLE_GROUPS, groups.size()); i++) {
            Group group = groups.get(i);

            int posY = GuiQuestBook.GROUPS_Y + GuiQuestBook.GROUPS_SPACING * (i - start);
            if (gui.inBounds(GuiQuestBook.GROUPS_X, posY, gui.getStringWidth(group.getName()), GuiQuestBook.TEXT_HEIGHT, x, y)) {
                switch (gui.getCurrentMode()) {
                    case TIER:
                        gui.modifyingGroup = (group == gui.modifyingGroup ? null : group);
                        break;
                    case NORMAL:
                        GuiQuestBook.selectedGroup = group;
                        gui.getTextBoxGroupAmount().setTextAndCursor(gui, String.valueOf(GuiQuestBook.getSelectedGroup().getLimit()));
                        break;
                    case RENAME:
                        gui.setEditMenu(new GuiEditMenuTextEditor(gui, gui.getPlayer(), group));
                        break;
                    case DELETE:
                        remove(group.getId());
                        SaveHelper.add(SaveHelper.EditType.GROUP_REMOVE);
                        break;
                    default:
                        break;
                }
                break;
            }
        }
    }
}
