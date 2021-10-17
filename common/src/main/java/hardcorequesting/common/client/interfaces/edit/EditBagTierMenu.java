package hardcorequesting.common.client.interfaces.edit;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.bag.BagTier;
import hardcorequesting.common.bag.GroupTier;
import hardcorequesting.common.bag.TierColor;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.widget.ArrowSelectionHelper;
import hardcorequesting.common.client.interfaces.widget.NumberTextBox;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.HQMUtil;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.minecraft.network.chat.FormattedText;

/**
 * A menu which allows editing the color and weights of a group tier.
 */
public class EditBagTierMenu extends GuiEditMenu {
    
    private static final int TIERS_TEXT_X = 20;
    private static final int TIERS_TEXT_Y = 20;
    private static final int TIERS_WEIGHTS_X = 30;
    private static final int TIERS_WEIGHTS_Y = 80;
    private static final int TIERS_WEIGHTS_SPACING = 15;
    private static final int TIERS_TEXT_BOX_X = 65;
    private static final int TIERS_TEXT_BOX_Y = -2;
    private static final int TIERS_WEIGHTS_TEXT_Y = 65;
    
    private final GroupTier tier;
    private final GroupTier original;
    
    public static void display(GuiQuestBook gui, GroupTier original) {
        gui.setEditMenu(new EditBagTierMenu(gui, original));
    }
    
    private EditBagTierMenu(GuiQuestBook gui, GroupTier original) {
        super(gui, true);
        this.original = original;
        this.tier = original.copy();
        
        BagTier[] values = BagTier.values();
        for (int i = 0; i < values.length; i++) {
            final int id = i;
            addTextBox(new NumberTextBox(gui, TIERS_WEIGHTS_X + TIERS_TEXT_BOX_X, TIERS_WEIGHTS_Y + TIERS_WEIGHTS_SPACING * id + TIERS_TEXT_BOX_Y,
                    FormattedText.EMPTY, false, 6, () -> tier.getWeights()[id], value -> tier.getWeights()[id] = value));
        }
        
        addClickable(new ArrowSelectionHelper(gui, 20, 40) {
            @Override
            protected void onArrowClick(boolean left) {
                if (left) {
                    tier.setColor(HQMUtil.cyclePrev(TierColor.values(), tier.getColor()));
                } else {
                    tier.setColor(HQMUtil.cycleNext(TierColor.values(), tier.getColor()));
                }
            }
    
            @Override
            protected FormattedText getArrowText() {
                return Translator.plain(tier.getColor().getName());
            }
        });
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
    }
    
    @Override
    public void save() {
        original.load(tier);
        SaveHelper.add(EditType.TIER_CHANGE);
    }
}
