package hardcorequesting.common.quests.reward;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public class ItemStackRewardList extends QuestRewardList<ItemStack> {
    
    public void addAll(NonNullList<ItemStack> list) {
        for (ItemStack stack : list)
            add(new ItemStackReward(stack));
    }
    
    public void set(NonNullList<ItemStack> list) {
        clear();
        addAll(list);
    }
    
    public void set(int id, ItemStack stack) {
        set(id, new ItemStackReward(stack));
    }
    
    public void add(ItemStack stack) {
        add(new ItemStackReward(stack));
    }
    
    public NonNullList<ItemStack> toList() {
        NonNullList<ItemStack> result = NonNullList.create();
        for (QuestReward<ItemStack> reward : list) {
            if (!reward.getReward().isEmpty()) {
                result.add(reward.getReward());
            }
        }
        return result;
    }
}
