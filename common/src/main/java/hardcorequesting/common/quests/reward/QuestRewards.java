package hardcorequesting.common.quests.reward;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.ClientChange;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.*;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenuReputationReward;
import hardcorequesting.common.client.interfaces.edit.PickItemMenu;
import hardcorequesting.common.client.sounds.SoundHandler;
import hardcorequesting.common.client.sounds.Sounds;
import hardcorequesting.common.event.EventTrigger;
import hardcorequesting.common.network.NetworkManager;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.quests.data.QuestData;
import hardcorequesting.common.team.RewardSetting;
import hardcorequesting.common.team.Team;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class QuestRewards {
    public static final int START_X = Quest.START_X;
    private static final int REWARD_STR_Y = 140;
    private static final int REWARD_Y = 150;
    private static final int REWARD_Y_OFFSET = 40;
    private static final int REWARD_OFFSET = 20;
    private static final int ITEM_SIZE = 18;
    private static final int MAX_REWARD_SLOTS = 7;
    private static final int MAX_SELECT_REWARD_SLOTS = 4;
    private static final int REPUTATION_X = 142;
    private static final int REPUTATION_Y = 133;
    private static final int REPUTATION_Y_LOWER = 150;
    private static final int REPUTATION_SIZE = 16;
    private static final int REPUTATION_SRC_X = 30;
    private static final int REPUTATION_SRC_Y = 82;
    
    private int selectedReward = -1;
    private int lastClicked;
    private final LargeButton claimButton = new LargeButton("hqm.quest.claim", 100, 190) {
        @Override
        public boolean isEnabled(GuiBase gui, Player player) {
            return hasReward(quest.getQuestData(player), player) && (rewardChoices.isEmpty() || selectedReward != -1) && quest.isEnabled(player);
        }
    
        @Override
        public boolean isVisible(GuiBase gui, Player player) {
            return hasReward(quest.getQuestData(player), player);
        }
    
        @Override
        public void onClick(GuiBase gui, Player player) {
            NetworkManager.sendToServer(ClientChange.CLAIM_QUEST.build(new Tuple<>(quest.getQuestId(), rewardChoices.isEmpty() ? -1 : selectedReward)));
        }
    };
    
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
    
    public String[] getCommandRewardsAsStrings() {
        return this.commandRewardList.asStrings();
    }
    
    public void setCommandRewards(String[] commands) {
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
    
    public boolean hasReward(QuestData data, Player player) {
        return (data.canClaimReward(player) && (!rewards.isEmpty() || !rewardChoices.isEmpty())) || (data.canClaim() && (reputationRewards != null || !commandRewardList.isEmpty()));
    }
    
    public void claimReward(Player player, int selectedReward) {
        QuestData data = quest.getQuestData(player);
        if (hasReward(data, player)) {
            boolean sentInfo = false;
            if (data.canClaimReward(player) && (!rewards.isEmpty() || !rewardChoices.isEmpty())) {
                List<ItemStack> items = new ArrayList<>();
                if (!rewards.isEmpty()) {
                    for (ItemStack stack : rewards.toList()) {
                        items.add(stack.copy());
                    }
                }
                if (!rewardChoices.isEmpty()) {
                    if (selectedReward >= 0 && selectedReward < rewardChoices.size()) {
                        items.add(rewardChoices.getReward(selectedReward).copy());
                    } else {
                        return;
                    }
                }
                
                List<ItemStack> itemsToAdd = new ArrayList<>();
                for (ItemStack stack : items) {
                    boolean added = false;
                    for (ItemStack stack1 : itemsToAdd) {
                        if (stack.sameItemStackIgnoreDurability(stack1) && ItemStack.tagMatches(stack, stack1)) {
                            stack1.grow(stack.getCount());
                            added = true;
                            break;
                        }
                    }
                    
                    if (!added) {
                        itemsToAdd.add(stack.copy());
                    }
                }
                
                List<ItemStack> itemsToCheck = new ArrayList<>();
                for (ItemStack stack : itemsToAdd) {
                    itemsToCheck.add(stack.copy());
                }
                for (int i = 0; i < player.getInventory().items.size(); i++) {
                    for (ItemStack stack1 : itemsToCheck) {
                        if (stack1.getCount() > 0) {
                            ItemStack stack = player.getInventory().items.get(i);
                            if (stack == ItemStack.EMPTY) {
                                stack1.shrink(stack1.getMaxStackSize());
                                break;
                            } else if (stack.sameItemStackIgnoreDurability(stack1) && ItemStack.tagMatches(stack1, stack)) {
                                stack1.shrink(stack1.getMaxStackSize() - stack.getCount());
                                break;
                            }
                        }
                    }
                    
                }
                
                
                boolean valid = true;
                for (ItemStack stack : itemsToCheck) {
                    if (stack.getCount() > 0) {
                        valid = false;
                        break;
                    }
                }
                
                if (valid) {
                    addItems(player, itemsToAdd);
                    player.getInventory().setChanged();
                    Team team = QuestingDataManager.getInstance().getQuestingData(player).getTeam();
                    if (!team.isSingle() && team.getRewardSetting() == RewardSetting.ANY) {
                        data.claimFullReward();
                        quest.sendUpdatedDataToTeam(player);
                    } else {
                        data.claimReward(player);
                        if (player instanceof ServerPlayer)
                            quest.sendUpdatedData((ServerPlayer) player);
                    }
                    sentInfo = true;
                } else {
                    return;
                }
            }
            
            
            if (reputationRewards != null && data.canClaim()) {
                QuestingDataManager.getInstance().getQuestingData(player).getTeam().receiveAndSyncReputation(quest, reputationRewards);
                EventTrigger.instance().onReputationChange(new EventTrigger.ReputationEvent(player));
                sentInfo = true;
            }
            
            if (data.canClaim()) {
                commandRewardList.executeAll(player);
                sentInfo = true;
            }
            data.claimed = true;
            
            if (sentInfo) {
                SoundHandler.play(Sounds.COMPLETE, player);
            }
            
        }
    }
    
    private void setReward(ItemStack stack, int id, boolean isStandardReward) {
        ItemStackRewardList rewardList = isStandardReward ? this.rewards : this.rewardChoices;
        
        if (id < rewardList.size()) {
            rewardList.set(id, stack);
            SaveHelper.add(EditType.REWARD_CHANGE);
        } else {
            SaveHelper.add(EditType.REWARD_CREATE);
            rewardList.add(stack);
        }
    }
    
    @Environment(EnvType.CLIENT)
    public void draw(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY, QuestData data) {
        if (selectedReward != -1 && !hasReward(data, player)) {
            selectedReward = -1;
        }
        
        drawItemRewards(matrices, gui, mX, mY);
    
        claimButton.draw(matrices, gui, player, mX, mY);
        
        drawReputationIcon(gui, mX, mY, data);
    }
    
    @Environment(EnvType.CLIENT)
    public void drawTooltips(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY, QuestData data) {
        drawItemRewardTooltips(matrices, gui, mX, mY);
    
        claimButton.renderTooltip(matrices, gui, player, mX, mY);
    
        drawRepIconTooltip(matrices, gui, mX, mY, data);
    }
    
    @Environment(EnvType.CLIENT)
    private void drawItemRewards(PoseStack matrices, GuiQuestBook gui, int mX, int mY) {
        if (!rewards.isEmpty() || Quest.canQuestsBeEdited()) {
            gui.drawString(matrices, Translator.translatable("hqm.quest.rewards"), START_X, REWARD_STR_Y, 0x404040);
            drawRewards(gui, rewards.toList(), REWARD_Y, -1, mX, mY, MAX_SELECT_REWARD_SLOTS);
            if (!rewardChoices.isEmpty() || Quest.canQuestsBeEdited()) {
                gui.drawString(matrices, Translator.translatable("hqm.quest.pickOne"), START_X, REWARD_STR_Y + REWARD_Y_OFFSET, 0x404040);
                drawRewards(gui, rewardChoices.toList(), REWARD_Y + REWARD_Y_OFFSET, selectedReward, mX, mY, MAX_REWARD_SLOTS);
            }
        } else if (!rewardChoices.isEmpty()) {
            gui.drawString(matrices, Translator.translatable("hqm.quest.pickOneReward"), START_X, REWARD_STR_Y, 0x404040);
            drawRewards(gui, rewardChoices.toList(), REWARD_Y, selectedReward, mX, mY, MAX_REWARD_SLOTS);
        }
    }
    
    @Environment(EnvType.CLIENT)
    private void drawItemRewardTooltips(PoseStack matrices, GuiQuestBook gui, int mX, int mY) {
        if (!rewards.isEmpty() || Quest.canQuestsBeEdited()) {
            drawRewardMouseOver(matrices, gui, rewards.toList(), REWARD_Y, -1, mX, mY);
            if (!rewardChoices.isEmpty() || Quest.canQuestsBeEdited()) {
                drawRewardMouseOver(matrices, gui, rewardChoices.toList(), REWARD_Y + REWARD_Y_OFFSET, selectedReward, mX, mY);
            }
        } else if (!rewardChoices.isEmpty()) {
            drawRewardMouseOver(matrices, gui, rewardChoices.toList(), REWARD_Y, selectedReward, mX, mY);
        }
    }
    
    @Environment(EnvType.CLIENT)
    private void drawReputationIcon(GuiQuestBook gui, int mX, int mY, QuestData data) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
        
        if (reputationRewards != null || Quest.canQuestsBeEdited()) {
            boolean claimed = data.claimed || reputationRewards == null;
            
            int backgroundIndex = claimed ? 2 : isOnReputationIcon(gui, mX, mY) ? 1 : 0;
            int foregroundIndex;
            if (claimed) {
                foregroundIndex = 3;
            } else {
                boolean positive = false;
                boolean negative = false;
                for (ReputationReward reputationReward : reputationRewards) {
                    if (reputationReward.getValue() < 0) {
                        negative = true;
                    } else if (reputationReward.getValue() > 0) {
                        positive = true;
                    }
                }
                
                if (negative == positive) {
                    foregroundIndex = 2;
                } else {
                    foregroundIndex = positive ? 0 : 1;
                }
            }
            
            int y = getRepIconY();
            foregroundIndex += 3;
            gui.drawRect(REPUTATION_X, y, REPUTATION_SRC_X + backgroundIndex * REPUTATION_SIZE, REPUTATION_SRC_Y, REPUTATION_SIZE, REPUTATION_SIZE);
            gui.drawRect(REPUTATION_X, y, REPUTATION_SRC_X + foregroundIndex * REPUTATION_SIZE, REPUTATION_SRC_Y, REPUTATION_SIZE, REPUTATION_SIZE);
        }
    }
    
    @Environment(EnvType.CLIENT)
    private void drawRepIconTooltip(PoseStack matrices, GuiQuestBook gui, int mX, int mY, QuestData data) {
        if (reputationRewards != null && isOnReputationIcon(gui, mX, mY)) {
            List<FormattedText> str = new ArrayList<>();
            for (ReputationReward reputationReward : reputationRewards) {
                if (reputationReward.getValue() != 0 && reputationReward.getReward() != null && reputationReward.getReward().isValid()) {
                    str.add(Translator.plain(reputationReward.getLabel()));
                }
                
            }
            
            List<FormattedText> commentLines = gui.getLinesFromText(Translator.translatable("hqm.quest.partyRepReward" + (data.claimed ? "Claimed" : "")), 1, 200);
            if (commentLines != null) {
                str.add(FormattedText.EMPTY);
                for (FormattedText commentLine : commentLines) {
                    str.add(Translator.text(Translator.rawString(commentLine), GuiColor.GRAY));
                }
            }
            gui.renderTooltipL(matrices, str, mX + gui.getLeft(), mY + gui.getTop());
        }
    }
    
    @Environment(EnvType.CLIENT)
    public void onClick(GuiQuestBook gui, Player player, int mX, int mY) {
        if (!rewards.isEmpty() || Quest.canQuestsBeEdited()) {
            handleRewardClick(gui, player, rewards.toList(), REWARD_Y, false, mX, mY);
            if (!rewardChoices.isEmpty() || Quest.canQuestsBeEdited()) {
                handleRewardClick(gui, player, rewardChoices.toList(), REWARD_Y + REWARD_Y_OFFSET, true, mX, mY);
            }
        } else if (!rewardChoices.isEmpty()) {
            handleRewardClick(gui, player, rewardChoices.toList(), REWARD_Y, true, mX, mY);
        }
        
        if (Quest.canQuestsBeEdited() && gui.getCurrentMode() == EditMode.REPUTATION_REWARD) {
            if (isOnReputationIcon(gui, mX, mY)) {
                gui.setEditMenu(new GuiEditMenuReputationReward(gui, player, reputationRewards));
            }
        }
    
        if (claimButton.inButtonBounds(gui, mX, mY) && claimButton.isVisible(gui, player) && claimButton.isEnabled(gui, player)) {
            claimButton.onClick(gui, player);
        }
    }
    
    @Environment(EnvType.CLIENT)
    private NonNullList<ItemStack> getEditFriendlyRewards(NonNullList<ItemStack> rewards, int max) {
        if (rewards.isEmpty()) {
            return NonNullList.withSize(1, ItemStack.EMPTY);
        } else if (Quest.canQuestsBeEdited() && rewards.size() < max) {
            NonNullList<ItemStack> rewardsWithEmpty = NonNullList.create();
            rewardsWithEmpty.addAll(rewards);
            rewardsWithEmpty.add(ItemStack.EMPTY);
            return rewardsWithEmpty;
        } else {
            return rewards;
        }
    }
    
    @Environment(EnvType.CLIENT)
    private void drawRewards(GuiQuestBook gui, NonNullList<ItemStack> rewards, int y, int selected, int mX, int mY, int max) {
        rewards = getEditFriendlyRewards(rewards, max);
        
        
        for (int i = 0; i < rewards.size(); i++) {
            gui.drawItemStack(rewards.get(i), START_X + i * REWARD_OFFSET, y, mX, mY, selected == i);
        }
    }
    
    @Environment(EnvType.CLIENT)
    private void drawRewardMouseOver(PoseStack matrices, GuiQuestBook gui, NonNullList<ItemStack> rewards, int y, int selected, int mX, int mY) {
        if (rewards != null) {
            for (int i = 0; i < rewards.size(); i++) {
                if (gui.inBounds(START_X + i * REWARD_OFFSET, y, ITEM_SIZE, ITEM_SIZE, mX, mY)) {
                    if (!rewards.get(i).isEmpty()) {
                        GuiQuestBook.setSelectedStack(rewards.get(i));
                        List<Component> str = rewards.get(i).getTooltipLines(Minecraft.getInstance().player, Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
                        List<FormattedText> list2 = Lists.newArrayList(str);
                        if (selected == i) {
                            list2.add(FormattedText.EMPTY);
                            list2.add(Translator.translatable("hqm.quest.selected", GuiColor.GREEN));
                        }
                        gui.renderTooltipL(matrices, list2, gui.getLeft() + mX, gui.getTop() + mY);
                    }
                    break;
                }
            }
        }
    }
    
    @Environment(EnvType.CLIENT)
    private void handleRewardClick(GuiQuestBook gui, Player player, NonNullList<ItemStack> rawRewards, int y, boolean canSelect, int mX, int mY) {
        NonNullList<ItemStack> rewards = getEditFriendlyRewards(rawRewards, canSelect ? MAX_SELECT_REWARD_SLOTS : MAX_REWARD_SLOTS);
        
        boolean doubleClick = false;
        
        for (int i = 0; i < rewards.size(); i++) {
            if (gui.inBounds(START_X + i * REWARD_OFFSET, y, ITEM_SIZE, ITEM_SIZE, mX, mY)) {
                if (gui.getCurrentMode() == EditMode.NORMAL) {
                    int lastDiff = player.tickCount - lastClicked;
                    if (lastDiff < 0) {
                        lastClicked = player.tickCount;
                    } else if (lastDiff < 6) {
                        doubleClick = true;
                    } else {
                        lastClicked = player.tickCount;
                    }
                }
                if (canSelect && (!Quest.canQuestsBeEdited() || (gui.getCurrentMode() == EditMode.NORMAL && !doubleClick))) {
                    if (selectedReward == i) {
                        selectedReward = -1;
                    } else if (!rewards.get(i).isEmpty()) {
                        selectedReward = i;
                    }
                } else if (Quest.canQuestsBeEdited()) {
                    if (gui.getCurrentMode() == EditMode.DELETE) {
                        if (i < rawRewards.size()) {
                            rawRewards.remove(i);
                            if (canSelect && selectedReward != -1) {
                                if (selectedReward == i) {
                                    selectedReward = -1;
                                } else if (selectedReward > i) {
                                    selectedReward--;
                                }
                            }
                            
                            if (canSelect) {
                                this.rewardChoices.set(rawRewards);
                            } else {
                                this.rewards.set(rawRewards);
                            }
                            SaveHelper.add(EditType.REWARD_REMOVE);
                        }
                    } else if (gui.getCurrentMode() == EditMode.ITEM || doubleClick) {
                        final int id = i;
                        PickItemMenu.display(gui, player, rewards.get(i), PickItemMenu.Type.ITEM, rewards.get(i).isEmpty() ? 1 : rewards.get(i).getCount(),
                                result -> this.setReward(result.getWithAmount(), id, !canSelect));
                    }
                }
                
                break;
            }
        }
    }
    
    @Environment(EnvType.CLIENT)
    private int getRepIconY() {
        return rewards.size() <= MAX_REWARD_SLOTS - (Quest.canQuestsBeEdited() ? 2 : 1) ? REPUTATION_Y_LOWER : REPUTATION_Y;
    }
    
    @Environment(EnvType.CLIENT)
    private boolean isOnReputationIcon(GuiQuestBook gui, int mX, int mY) {
        return gui.inBounds(REPUTATION_X, getRepIconY(), REPUTATION_SIZE, REPUTATION_SIZE, mX, mY);
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
                } else if (stack.sameItemStackIgnoreDurability(nextStack) && ItemStack.tagMatches(nextStack, stack)) {
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
