package hardcorequesting.common.client.interfaces.edit;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.LargeButton;
import hardcorequesting.common.client.interfaces.ResourceHelper;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public abstract class GuiEditMenu {
    
    private static final int CHECK_BOX_SRC_X = 192;
    private static final int CHECK_BOX_SRC_Y = 102;
    private static final int CHECK_BOX_SIZE = 7;
    protected List<LargeButton> buttons;
    protected Player player;
    protected List<CheckBox> checkboxes;
    private boolean hasButtons;
    
    protected GuiEditMenu(final GuiBase gui, Player player) {
        buttons = new ArrayList<>();
        this.player = player;
        
        checkboxes = new ArrayList<>();
    }
    
    protected GuiEditMenu(final GuiBase gui, Player player, boolean isControlOnFirstPage) {
        this(gui, player);
        hasButtons = true;
        int xOffset = isControlOnFirstPage ? 0 : 145;
        
        buttons.add(new LargeButton("hqm.edit.ok", xOffset + 40, 200) {
            @Override
            public boolean isEnabled(GuiBase gui, Player player) {
                return true;
            }
            
            @Override
            public boolean isVisible(GuiBase gui, Player player) {
                return true;
            }
            
            @Override
            public void onClick(GuiBase gui, Player player) {
                save(gui);
                close(gui);
            }
        });
        
        buttons.add(new LargeButton("hqm.edit.cancel", xOffset + 100, 200) {
            @Override
            public boolean isEnabled(GuiBase gui, Player player) {
                return true;
            }
            
            @Override
            public boolean isVisible(GuiBase gui, Player player) {
                return true;
            }
            
            @Override
            public void onClick(GuiBase gui, Player player) {
                close(gui);
            }
        });
    }
    
    public void draw(PoseStack matrices, GuiBase gui, int mX, int mY) {
        for (LargeButton button : buttons) {
            if (button.isVisible(gui, null)) {
                button.draw(matrices, gui, player, mX, mY);
            }
        }
        for (CheckBox checkbox : checkboxes) {
            checkbox.draw(matrices, gui, mX, mY);
        }
    }
    
    public void renderTooltip(PoseStack matrices, GuiBase gui, int mX, int mY) {
        for (LargeButton button : buttons) {
            if (button.isVisible(gui, null)) {
                button.renderTooltip(matrices, gui, player, mX, mY);
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
    
    public void close(GuiBase gui) {
        gui.setEditMenu(null);
    }
    
    public void onKeyStroke(GuiBase gui, char c, int k) {
        
    }
    
    public void onDrag(GuiBase gui, int mX, int mY) {
        
    }
    
    public void onRelease(GuiBase gui, int mX, int mY) {
        
    }
    
    public void onScroll(GuiBase gui, double mX, double mY, double scroll) {
        
    }
    
    public abstract void save(GuiBase gui);
    
    public boolean doesRequiredDoublePage() {
        return true;
    }
    
    public boolean hasButtons() {
        return hasButtons;
    }
    
    public abstract class CheckBox {
        
        private int x;
        private int y;
        private String name;
        private List<FormattedText> cached;
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
        
        protected void draw(PoseStack matrices, GuiBase gui, int mX, int mY) {
            if (!isVisible()) {
                return;
            }
            
            if (cached == null) {
                cached = gui.getLinesFromText(Translator.translatable(name), 0.7F, width);
            }
            
            boolean selected = getValue();
            boolean hover = gui.inBounds(x, y, CHECK_BOX_SIZE, CHECK_BOX_SIZE, mX, mY);
            
            gui.applyColor(0xFFFFFFFF);
            
            ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
            
            gui.drawRect(matrices, x, y, CHECK_BOX_SRC_X + (selected ? CHECK_BOX_SIZE : 0), CHECK_BOX_SRC_Y + (hover ? CHECK_BOX_SIZE : 0), CHECK_BOX_SIZE, CHECK_BOX_SIZE);
            gui.drawString(matrices, cached, x + 12, y + 2, 0.7F, 0x404040);
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
