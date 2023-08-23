package hardcorequesting.common.client.interfaces.graphic;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.ClientChange;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.ResourceHelper;
import hardcorequesting.common.client.interfaces.edit.PickItemMenu;
import hardcorequesting.common.client.interfaces.edit.ReputationRewardMenu;
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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A subsection of {@link QuestGraphic} which handles info related to {@link QuestRewards}.
 */
@Environment(EnvType.CLIENT)
public class QuestRewardsGraphic extends Graphic {
    
    public static final int START_X = QuestGraphic.START_X;
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
    private long lastClicked;
    
    private final Quest quest;
    private final QuestRewards rewards;
    private final UUID playerId;
    private final GuiQuestBook gui;
    
    public QuestRewardsGraphic(Quest quest, UUID playerId, GuiQuestBook gui) {
        this.quest = quest;
        this.rewards = quest.getRewards();
        this.playerId = playerId;
        this.gui = gui;
    
        addClickable(new LargeButton(gui, "hqm.quest.claim", 100, 190) {
            @Override
            public boolean isEnabled() {
                return rewards.hasReward(playerId) && !(rewards.hasChoiceReward() && selectedReward == -1) && quest.isEnabled(playerId);
            }
        
            @Override
            public boolean isVisible() {
                return rewards.hasReward(playerId);
            }
        
            @Override
            public void onClick() {
                NetworkManager.sendToServer(ClientChange.CLAIM_QUEST.build(new Tuple<>(quest.getQuestId(), rewards.hasChoiceReward() ? selectedReward : -1)));
            }
        });
    }
    
    @Override
    public void draw(GuiGraphics graphics, int mX, int mY) {
        QuestData data = quest.getQuestData(playerId);
        
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        if (selectedReward != -1 && !rewards.hasReward(data, playerId)) {
            selectedReward = -1;
        }
        
        drawItemRewards(graphics, mX, mY);
        
        super.draw(graphics, mX, mY);
        
        drawReputationIcon(graphics, mX, mY, data);
    }
    
    @Override
    public void drawTooltip(GuiGraphics graphics, int mX, int mY) {
        
        drawItemRewardTooltips(graphics, mX, mY);
    
        super.drawTooltip(graphics, mX, mY);
        
        drawRepIconTooltip(graphics, mX, mY);
    }
    
    private void drawItemRewards(GuiGraphics graphics, int mX, int mY) {
        NonNullList<ItemStack> itemRewards = rewards.getReward();
        NonNullList<ItemStack> choiceRewards = rewards.getRewardChoice();
        if (!itemRewards.isEmpty() || Quest.canQuestsBeEdited()) {
            gui.drawString(graphics, Translator.translatable("hqm.quest.rewards"), START_X, REWARD_STR_Y, 0x404040);
            drawRewards(graphics, gui, itemRewards, REWARD_Y, -1, mX, mY, MAX_REWARD_SLOTS);
            if (!choiceRewards.isEmpty() || Quest.canQuestsBeEdited()) {
                gui.drawString(graphics, Translator.translatable("hqm.quest.pickOne"), START_X, REWARD_STR_Y + REWARD_Y_OFFSET, 0x404040);
                drawRewards(graphics, gui, choiceRewards, REWARD_Y + REWARD_Y_OFFSET, selectedReward, mX, mY, MAX_SELECT_REWARD_SLOTS);
            }
        } else if (!choiceRewards.isEmpty()) {
            gui.drawString(graphics, Translator.translatable("hqm.quest.pickOneReward"), START_X, REWARD_STR_Y, 0x404040);
            drawRewards(graphics, gui, choiceRewards, REWARD_Y, selectedReward, mX, mY, MAX_SELECT_REWARD_SLOTS);
        }
    }
    
    private void drawItemRewardTooltips(GuiGraphics graphics, int mX, int mY) {
        NonNullList<ItemStack> itemRewards = rewards.getReward();
        NonNullList<ItemStack> choiceRewards = rewards.getRewardChoice();
        if (!itemRewards.isEmpty() || Quest.canQuestsBeEdited()) {
            drawRewardMouseOver(graphics, gui, itemRewards, REWARD_Y, -1, mX, mY);
            if (!choiceRewards.isEmpty() || Quest.canQuestsBeEdited()) {
                drawRewardMouseOver(graphics, gui, choiceRewards, REWARD_Y + REWARD_Y_OFFSET, selectedReward, mX, mY);
            }
        } else if (!choiceRewards.isEmpty()) {
            drawRewardMouseOver(graphics, gui, choiceRewards, REWARD_Y, selectedReward, mX, mY);
        }
    }
    
    private void drawReputationIcon(GuiGraphics graphics, int mX, int mY, QuestData data) {
        List<ReputationReward> reputationRewards = rewards.getReputationRewards();
        
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

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
            gui.drawRect(graphics, GuiBase.MAP_TEXTURE, REPUTATION_X, y, REPUTATION_SRC_X + backgroundIndex * REPUTATION_SIZE, REPUTATION_SRC_Y, REPUTATION_SIZE, REPUTATION_SIZE);
            gui.drawRect(graphics, GuiBase.MAP_TEXTURE, REPUTATION_X, y, REPUTATION_SRC_X + foregroundIndex * REPUTATION_SIZE, REPUTATION_SRC_Y, REPUTATION_SIZE, REPUTATION_SIZE);
        }
    }
    
    private void drawRepIconTooltip(GuiGraphics graphics, int mX, int mY) {
        QuestData data = quest.getQuestData(playerId);
        List<ReputationReward> reputationRewards = rewards.getReputationRewards();
        if (reputationRewards != null && isOnReputationIcon(gui, mX, mY)) {
            List<FormattedText> str = new ArrayList<>();
            for (ReputationReward reputationReward : reputationRewards) {
                if (reputationReward.getValue() != 0 && reputationReward.getReward() != null && reputationReward.getReward().isValid()) {
                    str.add(reputationReward.getLabel());
                }
            }
            
            List<FormattedText> commentLines = gui.getLinesFromText(Translator.translatable("hqm.quest.partyRepReward" + (data.teamRewardClaimed ? "Claimed" : "")), 1, 200);
            if (commentLines != null) {
                str.add(FormattedText.EMPTY);
                for (FormattedText commentLine : commentLines) {
                    str.add(Translator.text(Translator.rawString(commentLine)).withStyle(ChatFormatting.DARK_GRAY));
                }
            }
            gui.renderTooltipL(graphics, str, mX + gui.getLeft(), mY + gui.getTop());
        }
    }
    
    @Override
    public void onClick(int mX, int mY, int b) {
        NonNullList<ItemStack> itemRewards = rewards.getReward();
        NonNullList<ItemStack> choiceRewards = rewards.getRewardChoice();
        List<ReputationReward> reputationRewards = rewards.getReputationRewards();
        
        if (!itemRewards.isEmpty() || Quest.canQuestsBeEdited()) {
            handleRewardClick(gui, itemRewards, REWARD_Y, false, mX, mY);
            if (!choiceRewards.isEmpty() || Quest.canQuestsBeEdited()) {
                handleRewardClick(gui, choiceRewards, REWARD_Y + REWARD_Y_OFFSET, true, mX, mY);
            }
        } else if (!choiceRewards.isEmpty()) {
            handleRewardClick(gui, choiceRewards, REWARD_Y, true, mX, mY);
        }
        
        if (Quest.canQuestsBeEdited() && gui.getCurrentMode() == EditMode.REPUTATION_REWARD) {
            if (isOnReputationIcon(gui, mX, mY)) {
                ReputationRewardMenu.display(gui, reputationRewards, newRewards -> {
                    rewards.setReputationRewards(newRewards.isEmpty() ? null : newRewards);
                    SaveHelper.add(EditType.REPUTATION_REWARD_CHANGE);
                });
            }
        }
        
        super.onClick(mX, mY, b);
    }
    
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
    
    private void drawRewards(GuiGraphics graphics, GuiQuestBook gui, NonNullList<ItemStack> rewards, int y, int selected, int mX, int mY, int max) {
        rewards = getEditFriendlyRewards(rewards, max);
        
        
        for (int i = 0; i < rewards.size(); i++) {
            gui.drawItemStack(graphics, rewards.get(i), START_X + i * REWARD_OFFSET, y, mX, mY, selected == i);
        }
    }
    
    private void drawRewardMouseOver(GuiGraphics graphics, GuiQuestBook gui, NonNullList<ItemStack> rewards, int y, int selected, int mX, int mY) {
        if (rewards != null) {
            for (int i = 0; i < rewards.size(); i++) {
                if (gui.inBounds(START_X + i * REWARD_OFFSET, y, ITEM_SIZE, ITEM_SIZE, mX, mY)) {
                    if (!rewards.get(i).isEmpty()) {
                        List<Component> str = rewards.get(i).getTooltipLines(Minecraft.getInstance().player, Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
                        if (selected == i) {
                            str.add(Component.empty());
                            str.add(Translator.translatable("hqm.quest.selected").withStyle(ChatFormatting.DARK_GREEN));
                        }
                        graphics.renderComponentTooltip(this.gui.getFont(), str, gui.getLeft() + mX, gui.getTop() + mY);
                    }
                    break;
                }
            }
        }
    }
    
    private void handleRewardClick(GuiQuestBook gui, NonNullList<ItemStack> rawRewards, int y, boolean canSelect, int mX, int mY) {
        NonNullList<ItemStack> itemRewards = getEditFriendlyRewards(rawRewards, canSelect ? MAX_SELECT_REWARD_SLOTS : MAX_REWARD_SLOTS);
        
        boolean doubleClick = false;
        
        for (int i = 0; i < itemRewards.size(); i++) {
            if (gui.inBounds(START_X + i * REWARD_OFFSET, y, ITEM_SIZE, ITEM_SIZE, mX, mY)) {
                if (gui.getCurrentMode() == EditMode.NORMAL) {
                    long tickCount = Minecraft.getInstance().level.getGameTime();
                    long lastDiff = tickCount - lastClicked;
                    if (lastDiff < 0) {
                        lastClicked = tickCount;
                    } else if (lastDiff < 6) {
                        doubleClick = true;
                    } else {
                        lastClicked = tickCount;
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
                        PickItemMenu.display(gui, itemRewards.get(i), PickItemMenu.Type.ITEM, itemRewards.get(i).isEmpty() ? 1 : itemRewards.get(i).getCount(),
                                result -> rewards.setItemReward(result.getWithAmount(), id, !canSelect));
                    }
                }
                
                break;
            }
        }
    }
    
    private int getRepIconY() {
        return rewards.getReward().size() <= MAX_REWARD_SLOTS - (Quest.canQuestsBeEdited() ? 2 : 1) ? REPUTATION_Y_LOWER : REPUTATION_Y;
    }
    
    private boolean isOnReputationIcon(GuiQuestBook gui, int mX, int mY) {
        return gui.inBounds(REPUTATION_X, getRepIconY(), REPUTATION_SIZE, REPUTATION_SIZE, mX, mY);
    }
}