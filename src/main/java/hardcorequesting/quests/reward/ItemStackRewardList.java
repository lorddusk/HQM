package hardcorequesting.quests.reward;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemStackRewardList extends QuestRewardList<ItemStack> {
    
    public void addAll(ItemStack[] array) {
        for (ItemStack stack : array)
            add(new ItemStackReward(stack));
    }
    
    public void set(ItemStack[] array) {
        clear();
        if (array != null)
            addAll(array);
    }
    
    public void set(int id, ItemStack stack) {
        set(id, new ItemStackReward(stack));
    }
    
    public void add(ItemStack stack) {
        add(new ItemStackReward(stack));
    }
    
    @SuppressWarnings("ConstantConditions")
    @Override
    public ItemStack[] toArray() { // TODO change every ItemStack[] to NonNullList<ItemStack>
        List<ItemStack> result = new ArrayList<>();
        for (QuestReward<ItemStack> reward : list) {
            if (reward.getReward() != null) {
                result.add(reward.getReward());
            }
        }
        return result.isEmpty() ? null : result.toArray(new ItemStack[0]);
    }
}
