package hardcorequesting.client.interfaces;

import hardcorequesting.Translator;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;

public abstract class GuiEditMenuExtended extends GuiEditMenu {

    protected GuiEditMenuExtended(GuiBase gui, EntityPlayer player, boolean isControlOnFirstPage, int arrowX, int arrowY, int boxX, int boxY) {
        super(gui, player, isControlOnFirstPage);

        this.textBoxes = new TextBoxGroup();
        ARROW_X_LEFT = arrowX;
        ARROW_Y = arrowY;
        ARROW_DESCRIPTION_Y = ARROW_Y + 20;
        ARROW_X_RIGHT = ARROW_X_LEFT + 130;
        BOX_X = boxX;
        BOX_Y = boxY;
    }

    protected TextBoxGroup textBoxes;


    protected final int BOX_X;
    protected final int BOX_Y;
    protected final int BOX_OFFSET = 30;
    protected final int TEXT_OFFSET = -10;

    protected abstract class TextBoxNumber extends TextBoxGroup.TextBox {

        private String title;
        private int id;
        private boolean loaded;

        public TextBoxNumber(GuiBase gui, int id, String title) {
            super(gui, "", BOX_X, BOX_Y + BOX_OFFSET * id, false);
            loaded = true;
            reloadText(gui);
            this.title = title;
            this.id = id;
        }


        @Override
        public void reloadText(GuiBase gui) {
            setTextAndCursor(gui, isVisible() ? String.valueOf(getValue()) : "0");
        }

        @Override
        protected boolean isCharacterValid(char c) {
            return getText().length() < 32 && (Character.isDigit(c) || (c == '-' && isNegativeAllowed()));
        }

        protected boolean isNegativeAllowed() {
            return false;
        }

        @Override
        protected void textChanged(GuiBase gui) {
            if (loaded) {
                try {
                    int number;
                    if (getText().equals("")) {
                        number = 1;
                    } else {
                        number = Integer.parseInt(getText());
                    }
                    setValue(number);
                } catch (Exception ignored) {
                }
            }
        }

        @Override
        protected void draw(GuiBase gui, boolean selected) {
            super.draw(gui, selected);

            gui.drawString(Translator.translate(title), BOX_X, BOX_Y + BOX_OFFSET * id + TEXT_OFFSET, 0x404040);
        }

        protected abstract int getValue();

        protected abstract void setValue(int number);
    }

    private final int ARROW_X_LEFT;
    private final int ARROW_Y;
    private final int ARROW_DESCRIPTION_Y;
    private final int ARROW_X_RIGHT;

    private static final int ARROW_SRC_X = 244;
    private static final int ARROW_SRC_Y = 176;
    private static final int ARROW_W = 6;
    private static final int ARROW_H = 10;
    private boolean clicked;

    @Override
    public void draw(GuiBase gui, int mX, int mY) {
        super.draw(gui, mX, mY);


        ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);

        GL11.glColor4f(1F, 1F, 1F, 1F);
        if (isArrowVisible()) {
            drawArrow(gui, mX, mY, true);
            drawArrow(gui, mX, mY, false);

            gui.drawCenteredString(getArrowText(), ARROW_X_LEFT + ARROW_W, ARROW_Y, 0.7F, ARROW_X_RIGHT - (ARROW_X_LEFT + ARROW_W), ARROW_H, 0x404040);
            String description = getArrowDescription();
            if (description != null) {
                gui.drawString(gui.getLinesFromText(description, 0.7F, ARROW_X_RIGHT - ARROW_X_LEFT + ARROW_W), ARROW_X_LEFT, ARROW_DESCRIPTION_Y, 0.7F, 0x404040);
            }
        }

        textBoxes.draw(gui);
    }

    protected boolean isArrowVisible() {
        return ARROW_Y != -1;
    }

    private void drawArrow(GuiBase gui, int mX, int mY, boolean left) {
        int srcX = ARROW_SRC_X + (left ? 0 : ARROW_W);
        int srcY = ARROW_SRC_Y + (inArrowBounds(gui, mX, mY, left) ? clicked ? 1 : 2 : 0) * ARROW_H;

        gui.drawRect(left ? ARROW_X_LEFT : ARROW_X_RIGHT, ARROW_Y, srcX, srcY, ARROW_W, ARROW_H);
    }

    private boolean inArrowBounds(GuiBase gui, int mX, int mY, boolean left) {
        return gui.inBounds(left ? ARROW_X_LEFT : ARROW_X_RIGHT, ARROW_Y, ARROW_W, ARROW_H, mX, mY);
    }

    @Override
    public void onClick(GuiBase gui, int mX, int mY, int b) {
        super.onClick(gui, mX, mY, b);

        if (isArrowVisible()) {
            if (inArrowBounds(gui, mX, mY, true)) {
                onArrowClick(true);
                clicked = true;
            } else if (inArrowBounds(gui, mX, mY, false)) {
                onArrowClick(false);
                clicked = true;
            }
        }

        textBoxes.onClick(gui, mX, mY);
    }

    @Override
    public void onRelease(GuiBase gui, int mX, int mY) {
        super.onRelease(gui, mX, mY);

        clicked = false;
    }

    @Override
    public void onKeyTyped(GuiBase gui, char c, int k) {
        super.onKeyTyped(gui, c, k);

        textBoxes.onKeyStroke(gui, c, k);
    }

    protected abstract void onArrowClick(boolean left);

    protected abstract String getArrowText();

    protected abstract String getArrowDescription();
}
