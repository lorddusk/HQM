package hardcorequesting.client.interfaces;


import java.util.ArrayList;
import java.util.List;

import hardcorequesting.bag.Group;
import hardcorequesting.config.ModConfig;
import hardcorequesting.items.ItemBag;
import hardcorequesting.util.TooltipFlag;
import hardcorequesting.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

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

    public GuiReward(Group group, int bagTier, EntityPlayer player) {
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

        statisticsText = Translator.translate("hqm.rewardGui.chance", ((int) (chance * 10000)) / 100F);


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

    public static void open(EntityPlayer player, String groupId, int bag, List<Integer> limits) {
        Group rewardGroup = Group.getGroups().get(groupId);
        int i = 0;
        for (Group group : Group.getGroups().values())
            if (group.getLimit() != 0)
                group.setRetrievalCount(player, limits.get(i++));

        if (ItemBag.displayGui) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiReward(rewardGroup, bag, player));
        }
    }

    @Override
    public void drawScreen(int mX0, int mY0, float f) {
        applyColor(0xFFFFFFFF);
        ResourceHelper.bindResource(TEXTURE);


        int height = TOP_HEIGHT + MIDDLE_HEIGHT * lines + BOTTOM_HEIGHT;
        this.left = (this.width - TEXTURE_WIDTH) / 2;
        this.top = (this.height - height) / 2;

        drawRect(0, 0, 0, TOP_SRC_Y, TEXTURE_WIDTH, TOP_HEIGHT);
        for (int i = 0; i < lines; i++) {
            drawRect(0, TOP_HEIGHT + i * MIDDLE_HEIGHT, 0, MIDDLE_SRC_Y, TEXTURE_WIDTH, MIDDLE_HEIGHT);
        }
        drawRect(0, TOP_HEIGHT + lines * MIDDLE_HEIGHT, 0, BOTTOM_SRC_Y, TEXTURE_WIDTH, BOTTOM_HEIGHT);


        int mX = mX0 - left;
        int mY = mY0 - top;

        String title = group.getName();

        // fall back to the tier's name if this particular bag has no title,
        // or if the user explicitly asked us to do so.
        if (ModConfig.ALWAYS_USE_TIER_NAME_FOR_REWARD_TITLES || title == null || title.isEmpty()) {
            title = Translator.translate("hqm.rewardGui.tierReward", group.getTier().getName());
        }

        drawCenteredString(group.getTier().getColor() + title, 0, 0, 1F, TEXTURE_WIDTH, TITLE_HEIGHT, 0x404040);
        drawCenteredString(statisticsText, 0, TITLE_HEIGHT, 0.7F, TEXTURE_WIDTH, TOP_HEIGHT - TITLE_HEIGHT, 0x707070);
        drawCenteredString(Translator.translate("hqm.rewardGui.close"), 0, TOP_HEIGHT + lines * MIDDLE_HEIGHT, 0.7F, TEXTURE_WIDTH, BOTTOM_HEIGHT, 0x707070);

        for (Reward reward : rewards) {
            try {
                drawItemStack(reward.stack, reward.x, reward.y, true);
                //itemRenderer.renderItemOverlayIntoGUI(fontRendererObj, Minecraft.getMinecraft().getTextureManager(), reward.fluidStack, reward.x + left + 1, reward.y + top + 1);
                itemRenderer.renderItemOverlayIntoGUI(fontRenderer, reward.stack, (reward.x + left + 1), (reward.y + top + 1), "");
            } catch (Throwable ignored) {
            }
        }

        for (Reward reward : rewards) {
            if (inBounds(reward.x, reward.y, ITEM_SIZE, ITEM_SIZE, mX, mY)) {
                try {
                    if (GuiScreen.isShiftKeyDown()) {
                        drawMouseOver(reward.stack.getTooltip(Minecraft.getMinecraft().player, new TooltipFlag(Minecraft.getMinecraft().gameSettings.advancedItemTooltips)), mX0, mY0);
                    } else {
                        List<String> str = new ArrayList<String>();
                        try {
                            List<?> info = reward.stack.getTooltip(Minecraft.getMinecraft().player, new TooltipFlag(Minecraft.getMinecraft().gameSettings.advancedItemTooltips));
                            if (info.size() > 0) {
                                str.add((String) info.get(0));
                                if (info.size() > 1) {
                                    str.add(GuiColor.GRAY + Translator.translate("hqm.rewardGui.shiftInfo"));
                                }
                            }
                            drawMouseOver(str, mX0, mY0);
                        } catch (Throwable ignored) {
                        }
                    }
                } catch (Throwable ignored) {
                }
            }
        }
    }

    @Override
    protected void mouseClicked(int mX0, int mY0, int b) {
        Minecraft.getMinecraft().displayGuiScreen(null);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private class Reward {

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
