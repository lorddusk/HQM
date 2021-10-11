package hardcorequesting.common.client.interfaces;


import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.bag.Group;
import hardcorequesting.common.config.HQMConfig;
import hardcorequesting.common.items.BagItem;
import hardcorequesting.common.util.Translator;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GuiReward extends GuiBase {
    
    public static final ResourceLocation TEXTURE = ResourceHelper.getResource("reward");
    public static final ResourceLocation C_TEXTURE = ResourceHelper.getResource("c_reward");
    private static final int ITEMS_PER_LINE = 7;
    private static final int ITEM_SIZE = 16;
    private static final int ITEM_MARGIN = 5;
    private static final int TOP_HEIGHT = 52;
    private static final int MIDDLE_HEIGHT = 22;
    private static final int BOTTOM_HEIGHT = 24;
    private static final int TITLE_HEIGHT = 25;
    private static final int TOP_SRC_Y = 0;
    private static final int MIDDLE_SRC_Y = 67;
    private static final int BOTTOM_SRC_Y = 107;
    private static final int TEXTURE_WIDTH = 170;
    private Group group;
    private int lines;
    private List<Reward> rewards;
    private String statisticsText;
    
    public GuiReward(Group group, int bagTier, Player player) {
        super(NarratorChatListener.NO_TITLE);
        this.group = group;
        this.rewards = new ArrayList<>();
        
        
        int totalWeight = 0;
        for (Group other : Group.getGroups().values()) {
            if (other.isValid(player)) {
                totalWeight += other.getTier().getWeights()[bagTier];
            }
        }
        
        int myWeight = group.getTier().getWeights()[bagTier];
        float chance = ((float) myWeight / totalWeight);
        
        statisticsText = I18n.get("hqm.rewardGui.chance", ((int) (chance * 10000)) / 100F);
        
        
        lines = (int) Math.ceil((float) group.getItems().size() / ITEMS_PER_LINE);
        for (int i = 0; i < lines; i++) {
            int y = TOP_HEIGHT + MIDDLE_HEIGHT * i + (MIDDLE_HEIGHT - ITEM_SIZE) / 2;
            int itemsInLine = Math.min(group.getItems().size() - i * ITEMS_PER_LINE, ITEMS_PER_LINE);
            for (int j = 0; j < itemsInLine; j++) {
                int x = (TEXTURE_WIDTH - (itemsInLine * ITEM_SIZE + (itemsInLine - 1) * ITEM_MARGIN)) / 2 + j * (ITEM_SIZE + ITEM_MARGIN);
                ItemStack stack = group.getItems().get(i * ITEMS_PER_LINE + j);
                if (!stack.isEmpty()) {
                    rewards.add(new Reward(stack, x, y));
                }
            }
        }
    }
    
    public static void open(Player player, UUID groupId, int bag, int[] limits) {
        Group rewardGroup = Group.getGroups().get(groupId);
        int i = 0;
        for (Group group : Group.getGroups().values())
            if (group.getLimit() != 0)
                group.setRetrievalCount(player, limits[i++]);
        
        if (BagItem.displayGui && rewardGroup != null) {
            Minecraft.getInstance().setScreen(new GuiReward(rewardGroup, bag, player));
        }
    }
    
    @Override
    public void render(PoseStack matrices, int mX0, int mY0, float f) {
        applyColor(0xFFFFFFFF);
        ResourceHelper.bindResource(TEXTURE);
        
        
        int height = TOP_HEIGHT + MIDDLE_HEIGHT * lines + BOTTOM_HEIGHT;
        this.left = (this.width - TEXTURE_WIDTH) / 2;
        this.top = (this.height - height) / 2;
        
        drawRect(matrices, 0, 0, 0, TOP_SRC_Y, TEXTURE_WIDTH, TOP_HEIGHT);
        for (int i = 0; i < lines; i++) {
            drawRect(matrices, 0, TOP_HEIGHT + i * MIDDLE_HEIGHT, 0, MIDDLE_SRC_Y, TEXTURE_WIDTH, MIDDLE_HEIGHT);
        }
        drawRect(matrices, 0, TOP_HEIGHT + lines * MIDDLE_HEIGHT, 0, BOTTOM_SRC_Y, TEXTURE_WIDTH, BOTTOM_HEIGHT);
        
        
        int mX = mX0 - left;
        int mY = mY0 - top;
        
        String title = group.getDisplayName();
        
        // fall back to the tier's name if this particular bag has no title,
        // or if the user explicitly asked us to do so.
        if (HQMConfig.getInstance().Loot.ALWAYS_USE_TIER || title == null || title.isEmpty()) {
            title = I18n.get("hqm.rewardGui.tierReward", group.getTier().getName());
        }
        
        drawCenteredString(matrices, FormattedText.of(title, Style.EMPTY.withColor(TextColor.fromRgb(group.getTier().getColor().getHexColor() & 0xFFFFFF))), 0, 0, 1F, TEXTURE_WIDTH, TITLE_HEIGHT, 0x404040);
        drawCenteredString(matrices, Translator.plain(statisticsText), 0, TITLE_HEIGHT, 0.7F, TEXTURE_WIDTH, TOP_HEIGHT - TITLE_HEIGHT, 0x707070);
        drawCenteredString(matrices, Translator.translatable("hqm.rewardGui.close"), 0, TOP_HEIGHT + lines * MIDDLE_HEIGHT, 0.7F, TEXTURE_WIDTH, BOTTOM_HEIGHT, 0x707070);
        
        for (Reward reward : rewards) {
            try {
                drawItemStack(reward.stack, reward.x, reward.y, true);
                //itemRenderer.renderItemOverlayIntoGUI(fontRendererObj, MinecraftClient.getInstance().getTextureManager(), reward.stack, reward.x + left + 1, reward.y + top + 1);
                itemRenderer.renderGuiItemDecorations(font, reward.stack, (reward.x + left + 1), (reward.y + top + 1), "");
            } catch (Throwable ignored) {
            }
        }
        
        for (Reward reward : rewards) {
            if (inBounds(reward.x, reward.y, ITEM_SIZE, ITEM_SIZE, mX, mY)) {
                try {
                    if (Screen.hasShiftDown()) {
                        renderTooltip(matrices, reward.stack, mX0, mY0);
                    } else {
                        List<FormattedCharSequence> str = new ArrayList<>();
                        try {
                            List<Component> info = reward.stack.getTooltipLines(Minecraft.getInstance().player, minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
                            if (info.size() > 0) {
                                str.add(Language.getInstance().getVisualOrder(info.get(0)));
                                if (info.size() > 1) {
                                    str.add(Language.getInstance().getVisualOrder(Translator.translatable("hqm.rewardGui.shiftInfo").withStyle(ChatFormatting.DARK_GRAY)));
                                }
                            }
                            renderTooltip(matrices, str, mX0, mY0);
                        } catch (Throwable ignored) {
                        }
                    }
                } catch (Throwable ignored) {
                }
            }
        }
    }
    
    @Override
    public boolean mouseClicked(double mX0, double mY0, int b) {
        Minecraft.getInstance().setScreen(null);
        return true;
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    private static class Reward {
        private ItemStack stack;
        private int x;
        private int y;
        
        private Reward(ItemStack stack, int x, int y) {
            this.stack = stack;
            this.x = x;
            this.y = y;
        }
    }
    
}
