package hardcorequesting.common.quests.reward;

import hardcorequesting.common.client.sounds.SoundHandler;
import hardcorequesting.common.client.sounds.Sounds;
import hardcorequesting.common.event.EventTrigger;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.quests.data.QuestData;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class QuestRewards {
    private final Quest quest;
    private final ItemStackRewardList rewards = new ItemStackRewardList();
    private final ItemStackRewardList rewardChoices = new ItemStackRewardList();
    private final CommandRewardList commandRewardList = new CommandRewardList();
    private List<ReputationReward> reputationRewards;
    
    
    public QuestRewards(Quest quest) {
        this.quest = quest;
    }
    
    public NonNullList<ItemStack> getReward() {
        return rewards.toList();
    }
    
    public void setReward(NonNullList<ItemStack> reward) {
        this.rewards.set(reward);
    }
    
    public NonNullList<ItemStack> getRewardChoice() {
        return rewardChoices.toList();
    }
    
    public void setRewardChoice(NonNullList<ItemStack> rewardChoice) {
        this.rewardChoices.set(rewardChoice);
    }
    
    public List<String> getCommandRewardsAsStrings() {
        return this.commandRewardList.asStrings();
    }
    
    public void setCommandRewards(List<String> commands) {
        this.commandRewardList.set(commands);
    }
    
    public void addCommand(String command) {
        this.commandRewardList.add(command);
    }
    
    public void editCommand(int id, String command) {
        this.commandRewardList.set(id, command);
    }
    
    public void removeCommand(int id) {
        this.commandRewardList.remove(id);
    }
    
    public List<ReputationReward> getReputationRewards() {
        return reputationRewards;
    }
    
    public void setReputationRewards(List<ReputationReward> reputationRewards) {
        this.reputationRewards = reputationRewards;
    }
    
    public boolean hasReward(UUID playerId) {
        return hasReward(quest.getQuestData(playerId), playerId);
    }
    
    public boolean hasReward(QuestData data, UUID playerId) {
        return isItemRewardAvailable(playerId, data) || isRepRewardAvailable(data) || isCommandRewardAvailable(data);
    }
    
    public boolean hasChoiceReward() {
        return !rewardChoices.isEmpty();
    }
    
    
    
    public void claimReward(Player player, int selectedReward) {
        QuestData data = quest.getQuestData(player);
    
        Result result = tryClaimItemReward(player, selectedReward, data);
        if (result == Result.FAIL)
            return;
        boolean claimedAny = result == Result.SUCCESS;
    
        claimedAny |= tryClaimReputationReward(player, data);
    
        claimedAny |= tryClaimCommandReward(player, data);
    
        if (claimedAny) {
            data.claimReward(quest, player);
            SoundHandler.play(Sounds.COMPLETE, player);
        }
    }
    
    private enum Result {
        SUCCESS,
        PASS,
        FAIL
    }
    
    private boolean isItemRewardAvailable(UUID playerId, QuestData data) {
        return (!rewards.isEmpty() || !rewardChoices.isEmpty()) && data.canClaimPlayerReward(playerId);
    }
    
    private boolean isRepRewardAvailable(QuestData data) {
        return reputationRewards != null && data.canClaimTeamRewards();
    }
    
    private boolean isCommandRewardAvailable(QuestData data) {
        return !commandRewardList.isEmpty() && data.canClaimTeamRewards();
    }
    
    private Result tryClaimItemReward(Player player, int selectedReward, QuestData data) {
        if (isItemRewardAvailable(player.getUUID(), data)) {
            List<ItemStack> items = new ArrayList<>();
            if (!rewards.isEmpty()) {
                items.addAll(rewards.toList());
            }
            if (!rewardChoices.isEmpty()) {
                if (selectedReward >= 0 && selectedReward < rewardChoices.size()) {
                    items.add(rewardChoices.getReward(selectedReward));
                } else {
                    return Result.FAIL;
                }
            }
        
            List<ItemStack> itemsToAdd = copyAndMergeStacks(items);
        
            if (!canInventoryHoldAll(player, itemsToAdd))
                return Result.FAIL;
        
            addItems(player, itemsToAdd);
            player.getInventory().setChanged();
            return Result.SUCCESS;
        }
        return Result.PASS;
    }
    
    private boolean tryClaimReputationReward(Player player, QuestData data) {
        if (isRepRewardAvailable(data)) {
            QuestingDataManager.getInstance().getQuestingData(player).getTeam().receiveAndSyncReputation(quest, reputationRewards);
            EventTrigger.instance().onReputationChange(new EventTrigger.ReputationEvent(player));
            return true;
        }
        return false;
    }
    
    private boolean tryClaimCommandReward(Player player, QuestData data) {
        if (isCommandRewardAvailable(data)) {
            commandRewardList.executeAll(player);
            return true;
        }
        return false;
    }
    
    public void setItemReward(ItemStack stack, int id, boolean isStandardReward) {
        ItemStackRewardList rewardList = isStandardReward ? this.rewards : this.rewardChoices;
        
        if (id < rewardList.size()) {
            rewardList.set(id, stack);
            SaveHelper.add(EditType.REWARD_CHANGE);
        } else {
            SaveHelper.add(EditType.REWARD_CREATE);
            rewardList.add(stack);
        }
        quest.setIconIfEmpty(stack);
    }
    
    @NotNull
    public static List<ItemStack> copyAndMergeStacks(List<ItemStack> items) {
        List<ItemStack> itemsToAdd = new ArrayList<>();
        for (ItemStack stack : items) {
            boolean added = false;
            for (ItemStack stack1 : itemsToAdd) {
                if (stack.sameItem(stack1) && ItemStack.tagMatches(stack, stack1)) {
                    stack1.grow(stack.getCount());
                    added = true;
                    break;
                }
            }
            
            if (!added) {
                itemsToAdd.add(stack.copy());
            }
        }
        return itemsToAdd;
    }
    
    public static boolean canInventoryHoldAll(Player player, List<ItemStack> items) {
        List<ItemStack> itemsToCheck = new ArrayList<>();
        for (ItemStack stack : items) {
            itemsToCheck.add(stack.copy());
        }
        
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().items.get(i);
            for (ItemStack stack1 : itemsToCheck) {
                if (!stack1.isEmpty()) {
                    if (stack.isEmpty()) {
                        stack1.shrink(stack1.getMaxStackSize());
                        break;
                    } else if (stack.sameItem(stack1) && ItemStack.tagMatches(stack1, stack)) {
                        stack1.shrink(stack1.getMaxStackSize() - stack.getCount());
                        break;
                    }
                }
            }
            
        }
        
        return itemsToCheck.stream().allMatch(ItemStack::isEmpty);
    }
    
    public static void addItems(Player player, List<ItemStack> itemsToAdd) {
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            Iterator<ItemStack> iterator = itemsToAdd.iterator();
            while (iterator.hasNext()) {
                ItemStack nextStack = iterator.next();
                ItemStack stack = player.getInventory().items.get(i);
                
                if (stack.isEmpty()) {
                    int amount = Math.min(nextStack.getMaxStackSize(), nextStack.getCount());
                    ItemStack copyStack = nextStack.copy();
                    copyStack.setCount(amount);
                    player.getInventory().items.set(i, copyStack);
                    nextStack.shrink(amount);
                    if (nextStack.getCount() <= 0) {
                        iterator.remove();
                    }
                    break;
                } else if (stack.sameItem(nextStack) && ItemStack.tagMatches(nextStack, stack)) {
                    int amount = Math.min(nextStack.getMaxStackSize() - stack.getCount(), nextStack.getCount());
                    stack.grow(amount);
                    nextStack.shrink(amount);
                    if (nextStack.getCount() <= 0) {
                        iterator.remove();
                    }
                    break;
                }
            }
        }
    }
}
