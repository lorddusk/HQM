package hardcorequesting.common.client.interfaces.edit;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.widget.NumberTextBox;
import hardcorequesting.common.tileentity.TrackerBlockEntity;
import hardcorequesting.common.tileentity.TrackerType;
import hardcorequesting.common.util.Translator;

import java.util.UUID;

public class GuiEditMenuTracker extends GuiEditMenuExtended {
    
    private TrackerBlockEntity tracker;
    
    public GuiEditMenuTracker(GuiBase gui, UUID playerId, final TrackerBlockEntity tracker) {
        super(gui, playerId, true, 20, 30);
        
        this.tracker = tracker;
        
        textBoxes.add(new NumberTextBox(gui, 20, 130, "hqm.menuTracker.radius.title") {
            @Override
            protected void draw(PoseStack matrices, boolean selected) {
                super.draw(matrices, selected);
    
                this.gui.drawString(matrices, this.gui.getLinesFromText(Translator.translatable("hqm.menuTracker.radius.desc"), 0.7F, 130), x, y + BOX_OFFSET + TEXT_OFFSET, 0.7F, 0x404040);
            }
            
            @Override
            protected int getValue() {
                return tracker.getRadius();
            }
            
            @Override
            protected void setValue(int number) {
                tracker.setRadius(number);
            }
        });
    }
    
    @Override
    public void draw(PoseStack matrices, int mX, int mY) {
        super.draw(matrices, mX, mY);
        
        gui.drawCenteredString(matrices, tracker.getCurrentQuest() != null ? Translator.plain(tracker.getCurrentQuest().getName()) : Translator.translatable("hqm.menuTracker.noQuest"), 0, 5, 1F, 170, 20, 0x404040);
    }
    
    @Override
    protected void onArrowClick(boolean left) {
        if (left) {
            tracker.setTrackerType(TrackerType.values()[(tracker.getTrackerType().ordinal() + TrackerType.values().length - 1) % TrackerType.values().length]);
        } else {
            tracker.setTrackerType(TrackerType.values()[(tracker.getTrackerType().ordinal() + 1) % TrackerType.values().length]);
        }
    }
    
    @Override
    protected String getArrowText() {
        return tracker.getTrackerType().getName();
    }
    
    @Override
    protected String getArrowDescription() {
        return tracker.getTrackerType().getDescription();
    }
    
    @Override
    public void save() {
        tracker.sendToServer();
    }
    
    public boolean doesRequiredDoublePage() {
        return false;
    }
}
