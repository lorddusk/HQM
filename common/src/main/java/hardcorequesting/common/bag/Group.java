package hardcorequesting.common.bag;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.ScrollBar;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenuTextEditor;
import hardcorequesting.common.client.interfaces.edit.PickItemMenu;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestLine;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class Group {
    private GroupTier tier;
    private NonNullList<ItemStack> items;
    private String name;
    private int limit;
    private UUID groupId;
    
    public Group(UUID groupId) {
        this.groupId = groupId;
        while (this.groupId == null || getGroups().containsKey(this.groupId)) {
            this.groupId = UUID.randomUUID();
        }
        if (groupId == null) {
            if (GroupTierManager.getInstance().getTiers().size() < 1)
                GroupTier.initBaseTiers(QuestLine.getActiveQuestLine());
            this.tier = GroupTierManager.getInstance().getTiers().get(0);
        }
        items = NonNullList.create();
    }
    
    public static int size() {
        return GroupTierManager.getInstance().groups.size();
    }
    
    public static Map<UUID, Group> getGroups() {
        return GroupTierManager.getInstance().groups;
    }
    
    public static void remove(UUID groupId) {
        getGroups().remove(groupId);
    }
    
    public static void add(Group group) {
        getGroups().put(group.getId(), group);
    }
    
    public static Group getGroup(UUID groupId) {
        return Group.getGroups().get(groupId);
    }
    
    @Environment(EnvType.CLIENT)
    public static void drawOverview(PoseStack matrices, GuiQuestBook gui, ScrollBar tierScroll, ScrollBar groupScroll, int x, int y) {
        List<GroupTier> tiers = GroupTierManager.getInstance().getTiers();
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
                RenderSystem.enableBlend();
            }
            gui.drawString(matrices, Translator.plain(str), GuiQuestBook.TIERS_X, yPos, color);
            if (inBounds) {
                RenderSystem.disableBlend();
            }
            
            for (int j = 0; j < BagTier.values().length; j++) {
                BagTier bagTier = BagTier.values()[j];
                gui.drawCenteredString(matrices, Translator.text(groupTier.getWeights()[j] + "", bagTier.getColor()),
                        GuiQuestBook.TIERS_X + GuiQuestBook.TIERS_SECOND_LINE_X + j * GuiQuestBook.WEIGHT_SPACING,
                        yPos + GuiQuestBook.TIERS_SECOND_LINE_Y, 0.7F,
                        GuiQuestBook.WEIGHT_SPACING, 0, 0x404040);
            }
        }
        
        List<Group> groups = new ArrayList<>(Group.getGroups().values());
        start = groupScroll.isVisible(gui) ? Math.round((groups.size() - GuiQuestBook.VISIBLE_GROUPS) * groupScroll.getScroll()) : 0;
        for (int i = start; i < Math.min(start + GuiQuestBook.VISIBLE_GROUPS, groups.size()); i++) {
            Group group = groups.get(i);
            
            FormattedText str = Translator.plain(group.getDisplayName());
            int yPos = GuiQuestBook.GROUPS_Y + GuiQuestBook.GROUPS_SPACING * (i - start);
            boolean inBounds = gui.inBounds(GuiQuestBook.GROUPS_X, yPos, gui.getStringWidth(str), GuiQuestBook.TEXT_HEIGHT, x, y);
            int color = group.getTier().getColor().getHexColor();
            boolean selected = group == gui.modifyingGroup;
            if (inBounds || selected) {
                color &= 0xFFFFFF;
                RenderSystem.enableBlend();
                
                if (selected) {
                    color |= 0x50 << 24;
                } else {
                    color |= 0xBB << 24;
                }
            }
            
            gui.drawString(matrices, str, GuiQuestBook.GROUPS_X, yPos, color);
            if (inBounds || selected) {
                RenderSystem.disableBlend();
            }
            
            gui.drawString(matrices, Translator.translatable("hqm.questBook.items", group.getItems().size()),
                    GuiQuestBook.GROUPS_X + GuiQuestBook.GROUPS_SECOND_LINE_X,
                    yPos + GuiQuestBook.GROUPS_SECOND_LINE_Y,
                    0.7F, 0x404040);
        }
    }
    
    @Environment(EnvType.CLIENT)
    public static void mouseClickedOverview(GuiQuestBook gui, ScrollBar groupScroll, int x, int y) {
        List<Group> groups = new ArrayList<>(getGroups().values());
        int start = groupScroll.isVisible(gui) ? Math.round((groups.size() - GuiQuestBook.VISIBLE_GROUPS) * groupScroll.getScroll()) : 0;
        for (int i = start; i < Math.min(start + GuiQuestBook.VISIBLE_GROUPS, groups.size()); i++) {
            Group group = groups.get(i);
            
            int posY = GuiQuestBook.GROUPS_Y + GuiQuestBook.GROUPS_SPACING * (i - start);
            if (gui.inBounds(GuiQuestBook.GROUPS_X, posY, gui.getStringWidth(group.getDisplayName()), GuiQuestBook.TEXT_HEIGHT, x, y)) {
                switch (gui.getCurrentMode()) {
                    case TIER:
                        gui.modifyingGroup = (group == gui.modifyingGroup ? null : group);
                        break;
                    case NORMAL:
                        GuiQuestBook.selectedGroup = group;
                        gui.getTextBoxGroupAmount().setTextAndCursor(gui, String.valueOf(GuiQuestBook.getSelectedGroup().getLimit()));
                        break;
                    case RENAME:
                        GuiEditMenuTextEditor.display(gui, gui.getPlayer(), group.getDisplayName(), true, group::setName);
                        break;
                    case DELETE:
                        remove(group.getId());
                        SaveHelper.add(EditType.GROUP_REMOVE);
                        break;
                    default:
                        break;
                }
                break;
            }
        }
    }
    
    public GroupTier getTier() {
        return tier;
    }
    
    public void setTier(GroupTier tier) {
        this.tier = tier;
    }
    
    @Environment(EnvType.CLIENT)
    public String getDisplayName() {
        return hasName() ? name : I18n.get("hqm.bag.group", tier.getName());
    }
    
    public String getName() {
        return hasName() ? name : Translator.translatable("hqm.bag.group", tier.getName()).getString();
    }
    
    public void setName(String name) {
        this.name = name;
        SaveHelper.add(EditType.NAME_CHANGE);
    }
    
    public boolean hasName() {
        return name != null && !name.isEmpty();
    }
    
    public NonNullList<ItemStack> getItems() {
        return items;
    }
    
    public void setItem(int id, ItemStack stack) {
        if (stack.isEmpty()) return;
        
        if (id >= items.size()) {
            items.add(stack);
            SaveHelper.add(EditType.GROUP_ITEM_CREATE);
        } else {
            items.set(id, stack);
            SaveHelper.add(EditType.GROUP_ITEM_CHANGE);
        }
    }
    
    public void open(Player player) {
        if (limit > 0) {
            GroupData data = QuestingDataManager.getInstance().getQuestingData(player).getGroupData(getId());
            if (data != null) {
                data.retrieved++;
            }
        }
        
        List<ItemStack> itemsToAdd = items.stream().map(ItemStack::copy).collect(Collectors.toList());
        
        Quest.addItems(player, itemsToAdd);
        
        itemsToAdd.stream().filter(item -> item.getCount() > 0).forEach(item -> {
            ItemEntity entityItem = new ItemEntity(player.getCommandSenderWorld(), player.getX() + 0.5D, player.getY() + 0.5D, player.getZ() + 0.5D, item);
            player.getCommandSenderWorld().addFreshEntity(entityItem);
        });
    }
    
    public UUID getId() {
        return this.groupId;
    }
    
    public void removeItem(int i) {
        if (i >= 0 && i < items.size()) {
            items.remove(i);
        }
    }
    
    public int getLimit() {
        return limit;
    }
    
    public void setLimit(int limit) {
        this.limit = limit;
    }
    
    public int getRetrievalCount(Player player) {
        GroupData data = QuestingDataManager.getInstance().getQuestingData(player).getGroupData(getId());
        return data != null ? data.retrieved : 0;
    }
    
    public void setRetrievalCount(Player player, int count) {
        GroupData data = QuestingDataManager.getInstance().getQuestingData(player).getGroupData(getId());
        if (data != null) {
            data.retrieved = count;
        }
    }
    
    public boolean isValid(Player player) {
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
            if (ItemStack.matches(stack, stack2)) return true;
        }
        return false;
    }
    
    @Environment(EnvType.CLIENT)
    public void draw(PoseStack matrices, GuiQuestBook gui, int x, int y) {
        gui.drawString(matrices, Translator.plain(this.getDisplayName()), GuiQuestBook.GROUPS_X, GuiQuestBook.GROUPS_Y, this.getTier().getColor().getHexColor());
        List<ItemStack> items = new ArrayList<>(this.getItems());
        items.add(ItemStack.EMPTY);
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            
            int xPos = (i % GuiQuestBook.ITEMS_PER_LINE) * GuiQuestBook.GROUP_ITEMS_SPACING + GuiQuestBook.GROUP_ITEMS_X;
            int yPos = (i / GuiQuestBook.ITEMS_PER_LINE) * GuiQuestBook.GROUP_ITEMS_SPACING + GuiQuestBook.GROUP_ITEMS_Y;
            
            gui.drawItemStack(stack, xPos, yPos, x, y, false);
        }
        
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            
            int xPos = (i % GuiQuestBook.ITEMS_PER_LINE) * GuiQuestBook.GROUP_ITEMS_SPACING + GuiQuestBook.GROUP_ITEMS_X;
            int yPos = (i / GuiQuestBook.ITEMS_PER_LINE) * GuiQuestBook.GROUP_ITEMS_SPACING + GuiQuestBook.GROUP_ITEMS_Y;
            
            if (gui.inBounds(xPos, yPos, GuiQuestBook.ITEM_SIZE, GuiQuestBook.ITEM_SIZE, x, y)) {
                if (!stack.isEmpty()) {
                    try {
                        gui.renderTooltip(matrices, stack, x + gui.getLeft(), y + gui.getTop());
                    } catch (Exception ignored) {
                    }
                }
                break;
            }
        }
        
        gui.drawString(matrices, Translator.translatable("hqm.questBook.maxRetrieval"), 180, 20, 0x404040);
        gui.drawString(matrices, Translator.translatable("hqm.questBook.noRestriction"), 180, 48, 0.7F, 0x404040);
    }
    
    @Environment(EnvType.CLIENT)
    public void mouseClicked(GuiQuestBook gui, int x, int y) {
        List<ItemStack> items = new ArrayList<>(this.getItems());
        items.add(ItemStack.EMPTY);
        for (int i = 0; i < items.size(); i++) {
            int xPos = (i % GuiQuestBook.ITEMS_PER_LINE) * GuiQuestBook.GROUP_ITEMS_SPACING + GuiQuestBook.GROUP_ITEMS_X;
            int yPos = (i / GuiQuestBook.ITEMS_PER_LINE) * GuiQuestBook.GROUP_ITEMS_SPACING + GuiQuestBook.GROUP_ITEMS_Y;
            
            if (gui.inBounds(xPos, yPos, GuiQuestBook.ITEM_SIZE, GuiQuestBook.ITEM_SIZE, x, y)) {
                if (gui.getCurrentMode() == EditMode.ITEM) {
                    ItemStack stack = i < items.size() ? items.get(i) : ItemStack.EMPTY;
                    int amount;
                    if (!stack.isEmpty()) {
                        stack = stack.copy();
                        amount = stack.getCount();
                    } else {
                        amount = 1;
                    }
                    
                    final int id = i;
                    PickItemMenu.display(gui, gui.getPlayer(), stack, PickItemMenu.Type.ITEM, amount,
                            result -> this.setItem(id, result.getWithAmount()));
                    
                } else if (gui.getCurrentMode() == EditMode.DELETE) {
                    this.removeItem(i);
                    SaveHelper.add(EditType.GROUP_ITEM_REMOVE);
                }
                break;
            }
        }
    }
}
