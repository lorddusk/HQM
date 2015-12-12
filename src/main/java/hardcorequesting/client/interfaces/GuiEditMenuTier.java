package hardcorequesting.client.interfaces;

import hardcorequesting.SaveHelper;
import hardcorequesting.Translator;
import hardcorequesting.bag.BagTier;
import hardcorequesting.bag.GroupTier;
import hardcorequesting.config.ModConfig;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;

public class GuiEditMenuTier extends GuiEditMenu {
    private GroupTier tier;
    private GroupTier original;
    private TextBoxGroup textBoxes;
    private boolean clicked;

    public GuiEditMenuTier(GuiQuestBook gui, EntityPlayer player, final GroupTier tier) {
        super(gui, player, true);
        this.original = tier;
        this.tier = tier.copy();
        this.textBoxes = new TextBoxGroup();

        BagTier[] values = BagTier.values();
        for (int i = 0; i < values.length; i++) {
            final int id = i;
            textBoxes.add(new TextBoxGroup.TextBox(gui, String.valueOf(tier.getWeights()[id]), TIERS_WEIGHTS_X + TIERS_TEXT_BOX_X, TIERS_WEIGHTS_Y + TIERS_WEIGHTS_SPACING * id + TIERS_TEXT_BOX_Y, false) {
                @Override
                protected boolean isCharacterValid(char c) {
                    return getText().length() < 6 && Character.isDigit(c);
                }

                @Override
                protected void textChanged(GuiBase gui) {
                    try {
                        int number;
                        if (getText().equals("")) {
                            number = 1;
                        } else {
                            number = Integer.parseInt(getText());
                        }

                        tier.getWeights()[id] = number;
                    } catch (Exception ignored) {
                    }

                }
            });
        }
    }

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


    @Override
    public void draw(GuiBase gui, int mX, int mY) {
        super.draw(gui, mX, mY);

        gui.drawString(tier.getName(), TIERS_TEXT_X, TIERS_TEXT_Y, tier.getColor().getHexColor());

        gui.drawString(Translator.translate("hqm.menuTier.weights"), TIERS_TEXT_X, TIERS_WEIGHTS_TEXT_Y, 0x404040);

        BagTier[] values = BagTier.values();
        for (int i = 0; i < values.length; i++) {
            BagTier bagTier = values[i];

            int posY = TIERS_WEIGHTS_Y + i * TIERS_WEIGHTS_SPACING;
            gui.drawString(bagTier.getColor().toString() + bagTier.getName(), TIERS_WEIGHTS_X, posY, 0x404040);
        }

        GL11.glColor3f(1F, 1F, 1F);

        ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);

        drawArrow(gui, mX, mY, true);
        drawArrow(gui, mX, mY, false);
        gui.drawCenteredString(tier.getColor().getName(), ARROW_X_LEFT + ARROW_W, ARROW_Y, 1F, ARROW_X_RIGHT - (ARROW_X_LEFT + ARROW_W), ARROW_H, 0x404040);

        textBoxes.draw(gui);
    }

    @Override
    public void onClick(GuiBase gui, int mX, int mY, int b) {
        super.onClick(gui, mX, mY, b);

        if (inArrowBounds(gui, mX, mY, true)) {
            tier.setColor(GuiColor.values()[(tier.getColor().ordinal() + GuiColor.values().length - 1) % GuiColor.values().length]);
            clicked = true;
        } else if (inArrowBounds(gui, mX, mY, false)) {
            tier.setColor(GuiColor.values()[(tier.getColor().ordinal() + 1) % GuiColor.values().length]);
            clicked = true;
        }

        textBoxes.onClick(gui, mX, mY);
    }

    private boolean inArrowBounds(GuiBase gui, int mX, int mY, boolean left) {
        return gui.inBounds(left ? ARROW_X_LEFT : ARROW_X_RIGHT, ARROW_Y, ARROW_W, ARROW_H, mX, mY);
    }

    private void drawArrow(GuiBase gui, int mX, int mY, boolean left) {
        int srcX = ARROW_SRC_X + (left ? 0 : ARROW_W);
        int srcY = ARROW_SRC_Y + (inArrowBounds(gui, mX, mY, left) ? clicked ? 1 : 2 : 0) * ARROW_H;

        gui.drawRect(left ? ARROW_X_LEFT : ARROW_X_RIGHT, ARROW_Y, srcX, srcY, ARROW_W, ARROW_H);
    }

    @Override
    public void onKeyTyped(GuiBase gui, char c, int k) {
        super.onKeyTyped(gui, c, k);

        textBoxes.onKeyStroke(gui, c, k);
    }

    @Override
    protected void save(GuiBase gui) {
        original.load(tier);
        SaveHelper.add(SaveHelper.EditType.TIER_CHANGE);
    }

    @Override
    public void onRelease(GuiBase gui, int mX, int mY) {
        super.onRelease(gui, mX, mY);
        clicked = false;
    }
}
