package hardcorequesting.common.client.interfaces.edit;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.bag.BagTier;
import hardcorequesting.common.bag.GroupTier;
import hardcorequesting.common.bag.TierColor;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.ResourceHelper;
import hardcorequesting.common.client.interfaces.widget.NumberTextBox;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.minecraft.network.chat.FormattedText;

public class GuiEditMenuTier extends GuiEditMenu {
    
    private static final int ARROW_X_LEFT = 20;
    private static final int ARROW_X_RIGHT = 150;
    private static final int ARROW_Y = 40;
    private static final int ARROW_SRC_X = 244;
    private static final int ARROW_SRC_Y = 176;
    private static final int ARROW_W = 6;
    private static final int ARROW_H = 10;
    private static final int TIERS_TEXT_X = 20;
    private static final int TIERS_TEXT_Y = 20;
    private static final int TIERS_WEIGHTS_X = 30;
    private static final int TIERS_WEIGHTS_Y = 80;
    private static final int TIERS_WEIGHTS_SPACING = 15;
    private static final int TIERS_TEXT_BOX_X = 65;
    private static final int TIERS_TEXT_BOX_Y = -2;
    private static final int TIERS_WEIGHTS_TEXT_Y = 65;
    private GroupTier tier;
    private GroupTier original;
    private boolean clicked;
    
    public GuiEditMenuTier(GuiQuestBook gui, GroupTier original) {
        super(gui, true);
        this.original = original;
        this.tier = original.copy();
        
        BagTier[] values = BagTier.values();
        for (int i = 0; i < values.length; i++) {
            final int id = i;
            addTextBox(new NumberTextBox(gui, TIERS_WEIGHTS_X + TIERS_TEXT_BOX_X, TIERS_WEIGHTS_Y + TIERS_WEIGHTS_SPACING * id + TIERS_TEXT_BOX_Y,
                    FormattedText.EMPTY, false, 6, () -> tier.getWeights()[id], value -> tier.getWeights()[id] = value));
        }
    }
    
    @Override
    public void draw(PoseStack matrices, int mX, int mY) {
        super.draw(matrices, mX, mY);
        
        gui.drawString(matrices, Translator.plain(tier.getName()), TIERS_TEXT_X, TIERS_TEXT_Y, tier.getColor().getHexColor());
        
        gui.drawString(matrices, Translator.translatable("hqm.menuTier.weights"), TIERS_TEXT_X, TIERS_WEIGHTS_TEXT_Y, 0x404040);
        
        BagTier[] values = BagTier.values();
        for (int i = 0; i < values.length; i++) {
            BagTier bagTier = values[i];
            
            int posY = TIERS_WEIGHTS_Y + i * TIERS_WEIGHTS_SPACING;
            gui.drawString(matrices, bagTier.getColoredName(), TIERS_WEIGHTS_X, posY, 0x404040);
        }
        
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        
        ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
        
        drawArrow(matrices, gui, mX, mY, true);
        drawArrow(matrices, gui, mX, mY, false);
        gui.drawCenteredString(matrices, Translator.plain(tier.getColor().getName()), ARROW_X_LEFT + ARROW_W, ARROW_Y, 1F, ARROW_X_RIGHT - (ARROW_X_LEFT + ARROW_W), ARROW_H, 0x404040);
    }
    
    @Override
    public void onClick(int mX, int mY, int b) {
        super.onClick(mX, mY, b);
        
        if (inArrowBounds(gui, mX, mY, true)) {
            tier.setColor(TierColor.values()[(tier.getColor().ordinal() + TierColor.values().length - 1) % TierColor.values().length]);
            clicked = true;
        } else if (inArrowBounds(gui, mX, mY, false)) {
            tier.setColor(TierColor.values()[(tier.getColor().ordinal() + 1) % TierColor.values().length]);
            clicked = true;
        }
    }
    
    @Override
    public void onRelease(int mX, int mY, int button) {
        super.onRelease(mX, mY, button);
        clicked = false;
    }
    
    @Override
    public void save() {
        original.load(tier);
        SaveHelper.add(EditType.TIER_CHANGE);
    }
    
    private boolean inArrowBounds(GuiBase gui, int mX, int mY, boolean left) {
        return gui.inBounds(left ? ARROW_X_LEFT : ARROW_X_RIGHT, ARROW_Y, ARROW_W, ARROW_H, mX, mY);
    }
    
    private void drawArrow(PoseStack matrices, GuiBase gui, int mX, int mY, boolean left) {
        int srcX = ARROW_SRC_X + (left ? 0 : ARROW_W);
        int srcY = ARROW_SRC_Y + (inArrowBounds(gui, mX, mY, left) ? clicked ? 1 : 2 : 0) * ARROW_H;
        
        gui.drawRect(matrices, left ? ARROW_X_LEFT : ARROW_X_RIGHT, ARROW_Y, srcX, srcY, ARROW_W, ARROW_H);
    }
}
