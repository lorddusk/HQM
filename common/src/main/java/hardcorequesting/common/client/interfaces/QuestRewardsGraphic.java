package hardcorequesting.common.client.interfaces;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.ClientChange;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenuReputationReward;
import hardcorequesting.common.client.interfaces.edit.PickItemMenu;
import hardcorequesting.common.client.interfaces.widget.LargeButton;
import hardcorequesting.common.network.NetworkManager;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.data.QuestData;
import hardcorequesting.common.quests.reward.QuestRewards;
import hardcorequesting.common.quests.reward.ReputationReward;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QuestRewardsGraphic extends Graphic {
    
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
            return rewards.hasReward(playerId) && !(rewards.hasChoiceReward() && selectedReward == -1) && quest.isEnabled(player);
        }
        
        @Override
        public boolean isVisible(GuiBase gui, Player player) {
            return rewards.hasReward(playerId);
        }
        
        @Override
        public void onClick(GuiBase gui, Player player) {
            NetworkManager.sendToServer(ClientChange.CLAIM_QUEST.build(new Tuple<>(quest.getQuestId(), rewards.hasChoiceReward() ? selectedReward : -1)));
        }
    };
    
    private final Quest quest;
    private final QuestRewards rewards;
    private final UUID playerId;
    
    public QuestRewardsGraphic(Quest quest, UUID playerId) {
        this.quest = quest;
        this.rewards = quest.getRewards();
        this.playerId = playerId;
    }
    
    @Environment(EnvType.CLIENT)
    public void draw(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY, QuestData data) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        if (selectedReward != -1 && !rewards.hasReward(data, player.getUUID())) {
            selectedReward = -1;
        }
        
        drawItemRewards(matrices, gui, mX, mY);
        
        claimButton.draw(matrices, gui, player, mX, mY);
        
        drawReputationIcon(matrices, gui, mX, mY, data);
    }
    
    @Environment(EnvType.CLIENT)
    public void drawTooltips(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY, QuestData data) {
        drawItemRewardTooltips(matrices, gui, mX, mY);
        
        claimButton.renderTooltip(matrices, gui, player, mX, mY);
        
        drawRepIconTooltip(matrices, gui, mX, mY, data);
    }
    
    @Environment(EnvType.CLIENT)
    private void drawItemRewards(PoseStack matrices, GuiQuestBook gui, int mX, int mY) {
        NonNullList<ItemStack> itemRewards = rewards.getReward();
        NonNullList<ItemStack> choiceRewards = rewards.getReward();
        if (!itemRewards.isEmpty() || Quest.canQuestsBeEdited()) {
            gui.drawString(matrices, Translator.translatable("hqm.quest.rewards"), START_X, REWARD_STR_Y, 0x404040);
            drawRewards(matrices, gui, itemRewards, REWARD_Y, -1, mX, mY, MAX_SELECT_REWARD_SLOTS);
            if (!choiceRewards.isEmpty() || Quest.canQuestsBeEdited()) {
                gui.drawString(matrices, Translator.translatable("hqm.quest.pickOne"), START_X, REWARD_STR_Y + REWARD_Y_OFFSET, 0x404040);
                drawRewards(matrices, gui, choiceRewards, REWARD_Y + REWARD_Y_OFFSET, selectedReward, mX, mY, MAX_REWARD_SLOTS);
            }
        } else if (!choiceRewards.isEmpty()) {
            gui.drawString(matrices, Translator.translatable("hqm.quest.pickOneReward"), START_X, REWARD_STR_Y, 0x404040);
            drawRewards(matrices, gui, choiceRewards, REWARD_Y, selectedReward, mX, mY, MAX_REWARD_SLOTS);
        }
    }
    
    @Environment(EnvType.CLIENT)
    private void drawItemRewardTooltips(PoseStack matrices, GuiQuestBook gui, int mX, int mY) {
        NonNullList<ItemStack> itemRewards = rewards.getReward();
        NonNullList<ItemStack> choiceRewards = rewards.getReward();
        if (!itemRewards.isEmpty() || Quest.canQuestsBeEdited()) {
            drawRewardMouseOver(matrices, gui, itemRewards, REWARD_Y, -1, mX, mY);
            if (!choiceRewards.isEmpty() || Quest.canQuestsBeEdited()) {
                drawRewardMouseOver(matrices, gui, choiceRewards, REWARD_Y + REWARD_Y_OFFSET, selectedReward, mX, mY);
            }
        } else if (!choiceRewards.isEmpty()) {
            drawRewardMouseOver(matrices, gui, choiceRewards, REWARD_Y, selectedReward, mX, mY);
        }
    }
    
    @Environment(EnvType.CLIENT)
    private void drawReputationIcon(PoseStack matrices, GuiQuestBook gui, int mX, int mY, QuestData data) {
        List<ReputationReward> reputationRewards = rewards.getReputationRewards();
        
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
        
        if (reputationRewards != null || Quest.canQuestsBeEdited()) {
            boolean claimed = data.teamRewardClaimed || reputationRewards == null;
            
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
            gui.drawRect(matrices, REPUTATION_X, y, REPUTATION_SRC_X + backgroundIndex * REPUTATION_SIZE, REPUTATION_SRC_Y, REPUTATION_SIZE, REPUTATION_SIZE);
            gui.drawRect(matrices, REPUTATION_X, y, REPUTATION_SRC_X + foregroundIndex * REPUTATION_SIZE, REPUTATION_SRC_Y, REPUTATION_SIZE, REPUTATION_SIZE);
        }
    }
    
    @Environment(EnvType.CLIENT)
    private void drawRepIconTooltip(PoseStack matrices, GuiQuestBook gui, int mX, int mY, QuestData data) {
        List<ReputationReward> reputationRewards = rewards.getReputationRewards();
        if (reputationRewards != null && isOnReputationIcon(gui, mX, mY)) {
            List<FormattedText> str = new ArrayList<>();
            for (ReputationReward reputationReward : reputationRewards) {
                if (reputationReward.getValue() != 0 && reputationReward.getReward() != null && reputationReward.getReward().isValid()) {
                    str.add(Translator.plain(reputationReward.getLabel()));
                }
                
            }
            
            List<FormattedText> commentLines = gui.getLinesFromText(Translator.translatable("hqm.quest.partyRepReward" + (data.teamRewardClaimed ? "Claimed" : "")), 1, 200);
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
        NonNullList<ItemStack> itemRewards = rewards.getReward();
        NonNullList<ItemStack> choiceRewards = rewards.getReward();
        List<ReputationReward> reputationRewards = rewards.getReputationRewards();
        
        if (!itemRewards.isEmpty() || Quest.canQuestsBeEdited()) {
            handleRewardClick(gui, player, itemRewards, REWARD_Y, false, mX, mY);
            if (!choiceRewards.isEmpty() || Quest.canQuestsBeEdited()) {
                handleRewardClick(gui, player, choiceRewards, REWARD_Y + REWARD_Y_OFFSET, true, mX, mY);
            }
        } else if (!choiceRewards.isEmpty()) {
            handleRewardClick(gui, player, choiceRewards, REWARD_Y, true, mX, mY);
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
    private void drawRewards(PoseStack matrices, GuiQuestBook gui, NonNullList<ItemStack> rewards, int y, int selected, int mX, int mY, int max) {
        rewards = getEditFriendlyRewards(rewards, max);
        
        
        for (int i = 0; i < rewards.size(); i++) {
            gui.drawItemStack(matrices, rewards.get(i), START_X + i * REWARD_OFFSET, y, mX, mY, selected == i);
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
        NonNullList<ItemStack> itemRewards = getEditFriendlyRewards(rawRewards, canSelect ? MAX_SELECT_REWARD_SLOTS : MAX_REWARD_SLOTS);
        
        boolean doubleClick = false;
        
        for (int i = 0; i < itemRewards.size(); i++) {
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
                    } else if (!itemRewards.get(i).isEmpty()) {
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
                                this.rewards.setRewardChoice(rawRewards);
                            } else {
                                this.rewards.setReward(rawRewards);
                            }
                            SaveHelper.add(EditType.REWARD_REMOVE);
                        }
                    } else if (gui.getCurrentMode() == EditMode.ITEM || doubleClick) {
                        final int id = i;
                        PickItemMenu.display(gui, player, itemRewards.get(i), PickItemMenu.Type.ITEM, itemRewards.get(i).isEmpty() ? 1 : itemRewards.get(i).getCount(),
                                result -> rewards.setItemReward(result.getWithAmount(), id, !canSelect));
                    }
                }
                
                break;
            }
        }
    }
    
    @Environment(EnvType.CLIENT)
    private int getRepIconY() {
        return rewards.getReward().size() <= MAX_REWARD_SLOTS - (Quest.canQuestsBeEdited() ? 2 : 1) ? REPUTATION_Y_LOWER : REPUTATION_Y;
    }
    
    @Environment(EnvType.CLIENT)
    private boolean isOnReputationIcon(GuiQuestBook gui, int mX, int mY) {
        return gui.inBounds(REPUTATION_X, getRepIconY(), REPUTATION_SIZE, REPUTATION_SIZE, mX, mY);
    }
}