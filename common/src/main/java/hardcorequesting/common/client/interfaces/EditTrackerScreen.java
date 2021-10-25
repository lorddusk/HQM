package hardcorequesting.common.client.interfaces;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenu;
import hardcorequesting.common.client.interfaces.widget.ArrowSelectionHelper;
import hardcorequesting.common.client.interfaces.widget.LargeButton;
import hardcorequesting.common.client.interfaces.widget.NumberTextBox;
import hardcorequesting.common.client.interfaces.widget.TextBoxGroup;
import hardcorequesting.common.tileentity.TrackerBlockEntity;
import hardcorequesting.common.tileentity.TrackerType;
import hardcorequesting.common.util.HQMUtil;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * A screen for editing information in the quest tracker block. Should only be accessible when editing quest line data.
 */
@Environment(EnvType.CLIENT)
public class EditTrackerScreen extends GuiBase {
    
    public static final ResourceLocation BG_TEXTURE = ResourceHelper.getResource("wrapper");
    public static final ResourceLocation C_BG_TEXTURE = ResourceHelper.getResource("c_wrapper");
    private static final int TEXTURE_WIDTH = 170;
    private static final int TEXTURE_HEIGHT = 234;
    
    private final TrackerBlockEntity tracker;
    
    private final ArrowSelectionHelper selectionHelper;
    private final List<LargeButton> buttons = new ArrayList<>();
    private final TextBoxGroup textBoxes = new TextBoxGroup();
    
    public EditTrackerScreen(final TrackerBlockEntity trackerIn) {
        super(NarratorChatListener.NO_TITLE);
    
        this.tracker = trackerIn;
        selectionHelper = new ArrowSelectionHelper(this, 20, 30) {
            @Override
            protected void onArrowClick(boolean left) {
                if (left) {
                    tracker.setTrackerType(HQMUtil.cyclePrev(TrackerType.values(), tracker.getTrackerType()));
                } else {
                    tracker.setTrackerType(HQMUtil.cycleNext(TrackerType.values(), tracker.getTrackerType()));
                }
            }
        
            @Override
            protected FormattedText getArrowText() {
                return tracker.getTrackerType().getName();
            }
        
            @Override
            protected FormattedText getArrowDescription() {
                return tracker.getTrackerType().getDescription();
            }
        };
        textBoxes.add(new NumberTextBox(this, 20, 130, Translator.translatable("hqm.menuTracker.radius.title"), tracker::getRadius, tracker::setRadius) {
            @Override
            protected void draw(PoseStack matrices, boolean selected, int mX, int mY) {
                super.draw(matrices, selected, mX, mY);
            
                this.gui.drawString(matrices, this.gui.getLinesFromText(Translator.translatable("hqm.menuTracker.radius.desc"), 0.7F, 130), x, y + GuiEditMenu.BOX_OFFSET + TEXT_OFFSET, 0.7F, 0x404040);
            }
        });
        
        buttons.add(new LargeButton(this, "hqm.edit.ok", 40, 200) {
            @Override
            public void onClick() {
                tracker.sendToServer();
                onClose();
            }
        });
    
        buttons.add(new LargeButton(this, "hqm.edit.cancel", 100, 200) {
            @Override
            public void onClick() {
                onClose();
            }
        });
    }
    
    @Override
    protected void init() {
        super.init();
    
        this.left = (this.width - TEXTURE_WIDTH) / 2;
        this.top = (this.height - TEXTURE_HEIGHT) / 2;
    }
    
    @Override
    public void render(PoseStack matrices, int mX0, int mY0, float f) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        
        ResourceHelper.bindResource(BG_TEXTURE);
        drawRect(matrices, 0, 0, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    
        int mX = mX0 - left;
        int mY = mY0 - top;
        for (LargeButton button : buttons) {
            button.render(matrices, mX, mY);
        }
        
        textBoxes.render(matrices, mX, mY);
    
        drawCenteredString(matrices, tracker.getCurrentQuest() != null ? Translator.plain(tracker.getCurrentQuest().getName()) : Translator.translatable("hqm.menuTracker.noQuest"), 0, 5, 1F, 170, 20, 0x404040);
    
        selectionHelper.render(matrices, mX, mY);
    
        for (LargeButton button : buttons) {
            button.renderTooltip(matrices, mX, mY);
        }
    }
    
    @Override
    public boolean charTyped(char c, int k) {
        if (super.charTyped(c, k)) {
            return true;
        }
        
        return textBoxes.onCharTyped(c);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        
        return textBoxes.onKeyStroke(keyCode);
    }
    
    @Override
    public boolean mouseClicked(double mX0, double mY0, int b) {
        int mX = (int) (mX0 - left);
        int mY = (int) (mY0 - top);
    
        for (LargeButton button : buttons) {
            if (button.onClick(mX, mY))
                return true;
        }
    
        return textBoxes.onClick(mX, mY)
                || selectionHelper.onClick(mX, mY)
                || super.mouseClicked(mX0, mY0, b);
    }
    
    @Override
    public boolean mouseReleased(double mX0, double mY0, int b) {
        int mX = (int) (mX0 - left);
        int mY = (int) (mY0 - top);
        
        return selectionHelper.onRelease(mX, mY)
                || super.mouseReleased(mX0, mY0, b);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}