package hardcorequesting.common.client.interfaces.edit;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.ResourceHelper;
import hardcorequesting.common.client.interfaces.graphic.Graphic;
import hardcorequesting.common.client.interfaces.widget.LargeButton;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.FormattedText;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public abstract class GuiEditMenu extends Graphic {
    
    public static final int BOX_OFFSET = 30;
    private static final int CHECK_BOX_SRC_X = 192;
    private static final int CHECK_BOX_SRC_Y = 102;
    private static final int CHECK_BOX_SIZE = 7;
    protected final List<CheckBox> checkboxes = new ArrayList<>();
    private boolean hasButtons;
    protected final GuiQuestBook gui;
    protected final UUID playerId;
    
    protected GuiEditMenu(GuiQuestBook gui, UUID playerId) {
        this.gui = gui;
        this.playerId = playerId;
    }
    
    protected GuiEditMenu(GuiQuestBook gui, UUID playerId, boolean isControlOnFirstPage) {
        this(gui, playerId);
        hasButtons = true;
        int xOffset = isControlOnFirstPage ? 0 : 145;
        
        addButton(new LargeButton(gui, "hqm.edit.ok", xOffset + 40, 200) {
            @Override
            public boolean isEnabled() {
                return true;
            }
            
            @Override
            public boolean isVisible() {
                return true;
            }
            
            @Override
            public void onClick() {
                save();
                close();
            }
        });
        
        addButton(new LargeButton(gui, "hqm.edit.cancel", xOffset + 100, 200) {
            @Override
            public boolean isEnabled() {
                return true;
            }
            
            @Override
            public boolean isVisible() {
                return true;
            }
            
            @Override
            public void onClick() {
                close();
            }
        });
    }
    
    public void draw(PoseStack matrices, int mX, int mY) {
        super.draw(matrices, mX, mY);
        
        for (CheckBox checkbox : checkboxes) {
            checkbox.draw(matrices, gui, mX, mY);
        }
    }
    
    public void onClick(int mX, int mY, int b) {
        if (!hasButtons && b == 1) {
            save();
            close();
            return;
        }
        
        super.onClick(mX, mY, b);
        
        for (CheckBox checkbox : checkboxes) {
            checkbox.onClick(gui, mX, mY);
        }
    }
    
    public void close() {
        gui.setEditMenu(null);
    }
    
    public abstract void save();
    
    public boolean doesRequiredDoublePage() {
        return true;
    }
    
    public boolean hasButtons() {
        return hasButtons;
    }
    
    public abstract static class CheckBox {
        
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
