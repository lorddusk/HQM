package hardcorequesting.common.quests.reward;

import net.minecraft.world.item.ItemStack;

public class ItemStackReward extends QuestReward<ItemStack> {
    public ItemStackReward(ItemStack stack) {
        super(stack != null ? stack : ItemStack.EMPTY);
    }
}
