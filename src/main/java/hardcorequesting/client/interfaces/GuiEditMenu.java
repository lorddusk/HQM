package hardcorequesting.client.interfaces;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import hardcorequesting.Translator;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public abstract class GuiEditMenu {
    protected List<LargeButton> buttons;
    private boolean hasButtons;
    protected EntityPlayer player;
    protected List<CheckBox> checkboxes;

    protected GuiEditMenu(final GuiBase gui, EntityPlayer player) {
        buttons = new ArrayList<LargeButton>();
        this.player = player;

        checkboxes = new ArrayList<CheckBox>();
    }

    protected GuiEditMenu(final GuiBase gui, EntityPlayer player, boolean isControlOnFirstPage) {
        this(gui, player);
        hasButtons = true;
        int xOffset = isControlOnFirstPage ? 0 : 145;

        buttons.add(new LargeButton("hqm.edit.ok", xOffset + 40, 200) {
            @Override
            public boolean isEnabled(GuiBase gui, EntityPlayer player) {
                return true;
            }

            @Override
            public boolean isVisible(GuiBase gui, EntityPlayer player) {
                return true;
            }

            @Override
            public void onClick(GuiBase gui, EntityPlayer player) {
                save(gui);
                close(gui);
            }
        });

        buttons.add(new LargeButton("hqm.edit.cancel", xOffset + 100, 200) {
            @Override
            public boolean isEnabled(GuiBase gui, EntityPlayer player) {
                return true;
            }

            @Override
            public boolean isVisible(GuiBase gui, EntityPlayer player) {
                return true;
            }

            @Override
            public void onClick(GuiBase gui, EntityPlayer player) {
                close(gui);
            }
        });
    }

    public void draw(GuiBase gui, int mX, int mY) {
        for (LargeButton button : buttons) {
            if (button.isVisible(gui, null)) {
                button.draw(gui, player, mX, mY);
            }
        }
        for (CheckBox checkbox : checkboxes) {
            checkbox.draw(gui, mX, mY);
        }
    }

    public void drawMouseOver(GuiBase gui, int mX, int mY) {
        for (LargeButton button : buttons) {
            if (button.isVisible(gui, null)) {
                button.drawMouseOver(gui, player, mX, mY);
            }
        }
    }

    public void onClick(GuiBase gui, int mX, int mY, int b) {
        if (!hasButtons && b == 1) {
            save(gui);
            close(gui);
            return;
        }

        for (LargeButton button : buttons) {
            if (button.inButtonBounds(gui, mX, mY) && button.isVisible(gui, null) && button.isEnabled(gui, null)) {
                button.onClick(gui, player);
            }
        }

        for (CheckBox checkbox : checkboxes) {
            checkbox.onClick(gui, mX, mY);
        }

    }

    protected void close(GuiBase gui) {
        gui.setEditMenu(null);
    }

    public void onKeyTyped(GuiBase gui, char c, int k) {

    }

    public void onDrag(GuiBase gui, int mX, int mY) {

    }

    public void onRelease(GuiBase gui, int mX, int mY) {

    }

    public void onScroll(GuiBase gui, int mX, int mY, int scroll) {

    }

    protected abstract void save(GuiBase gui);

    public boolean doesRequiredDoublePage() {
        return true;
    }

    public boolean hasButtons() {
        return hasButtons;
    }

    private static final int CHECK_BOX_SRC_X = 192;
    private static final int CHECK_BOX_SRC_Y = 102;
    private static final int CHECK_BOX_SIZE = 7;

    public abstract class CheckBox {
        private int x;
        private int y;
        private String name;
        private List<String> cached;
        private int width = Integer.MAX_VALUE;

        protected CheckBox(String name, int x, int y) {
            this.x = x;
            this.y = y;
            this.name = name;
        }

        protected CheckBox(String name, int x, int y, int width) {
            this(name, x, y);
            this.width = width;
        }

        protected void draw(GuiBase gui, int mX, int mY) {
            if (!isVisible()) {
                return;
            }

            if (cached == null) {
                cached = gui.getLinesFromText(Translator.translate(name), 0.7F, width);
            }

            boolean selected = getValue();
            boolean hover = gui.inBounds(x, y, CHECK_BOX_SIZE, CHECK_BOX_SIZE, mX, mY);

            gui.applyColor(0xFFFFFFFF);

            ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);

            gui.drawRect(x, y, CHECK_BOX_SRC_X + (selected ? CHECK_BOX_SIZE : 0), CHECK_BOX_SRC_Y + (hover ? CHECK_BOX_SIZE : 0), CHECK_BOX_SIZE, CHECK_BOX_SIZE);
            gui.drawString(cached, x + 12, y + 2, 0.7F, 0x404040);
        }

        protected void onClick(GuiBase gui, int mX, int mY) {
            if (isVisible() && gui.inBounds(x, y, CHECK_BOX_SIZE, CHECK_BOX_SIZE, mX, mY)) {
                setValue(!getValue());
            }
        }

        protected boolean isVisible() {
            return true;
        }

        public abstract boolean getValue();

        public abstract void setValue(boolean val);
    }
}
