package hardcorequesting.common.bag;

import hardcorequesting.common.quests.QuestLine;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.quests.reward.QuestRewards;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import hardcorequesting.common.util.WrappedText;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class LootGroup {
    private GroupTier tier;
    private final NonNullList<ItemStack> items;
    @Nullable
    private WrappedText name;
    private int limit;
    private UUID groupId;
    
    public LootGroup(UUID groupId) {
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
    
    public static Map<UUID, LootGroup> getGroups() {
        return GroupTierManager.getInstance().groups;
    }
    
    public static void remove(UUID groupId) {
        getGroups().remove(groupId);
    }
    
    public static void add(LootGroup group) {
        getGroups().put(group.getId(), group);
    }
    
    public static LootGroup getGroup(UUID groupId) {
        return LootGroup.getGroups().get(groupId);
    }
    
    public GroupTier getTier() {
        return tier;
    }
    
    public void setTier(GroupTier tier) {
        this.tier = tier;
    }
    
    public FormattedText getDisplayName() {
        return name != null ? name.getText() : getDefaultName();
    }
    
    @NotNull
    public WrappedText getRawName() {
        return name != null ? name : WrappedText.create(getDefaultName().getString());
    }
    
    private Component getDefaultName() {
        return Translator.translatable("hqm.bag.group", tier.getName());
    }
    
    public void setName(@Nullable WrappedText name) {
        this.name = name;
        SaveHelper.add(EditType.NAME_CHANGE);
    }
    
    public boolean hasName() {
        return name != null;
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
        
        QuestRewards.addItems(player, itemsToAdd);
        
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
        if (obj instanceof LootGroup) {
            if (Objects.equals(name, ((LootGroup) obj).name) && limit == ((LootGroup) obj).limit && items.size() == ((LootGroup) obj).items.size()) {
                for (ItemStack stack : items) {
                    if (!listContains(stack, ((LootGroup) obj).items)) return false;
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
}
