package hardcorequesting.client.interfaces;

import hardcorequesting.Translator;
import hardcorequesting.tileentity.TileEntityTracker;
import hardcorequesting.tileentity.TrackerType;
import net.minecraft.entity.player.EntityPlayer;

public class GuiEditMenuTracker extends GuiEditMenuExtended {
    private TileEntityTracker tracker;

    public GuiEditMenuTracker(GuiBase gui, EntityPlayer player, final TileEntityTracker tracker) {
        super(gui, player, true, 20, 30, 20, 130);

        this.tracker = tracker;

        textBoxes.add(new TextBoxNumber(gui, 0, "hqm.menuTracker.radius.title") {
            @Override
            protected void setValue(int number) {
                tracker.setRadius(number);
            }

            @Override
            protected int getValue() {
                return tracker.getRadius();
            }

            @Override
            protected void draw(GuiBase gui, boolean selected) {
                super.draw(gui, selected);

                gui.drawString(gui.getLinesFromText(Translator.translate("hqm.menuTracker.radius.desc"), 0.7F, 130), BOX_X, BOX_Y + BOX_OFFSET + TEXT_OFFSET, 0.7F, 0x404040);
            }
        });
    }

    @Override
    public void draw(GuiBase gui, int mX, int mY) {
        super.draw(gui, mX, mY);

        gui.drawCenteredString(tracker.getCurrentQuest() != null ? tracker.getCurrentQuest().getName() : Translator.translate("hqm.menuTracker.noQuest"), 0, 5, 1F, 170, 20, 0x404040);
    }

    @Override
    protected void onArrowClick(boolean left) {
        if (left) {
            tracker.setType(TrackerType.values()[(tracker.getType().ordinal() + TrackerType.values().length - 1) % TrackerType.values().length]);
        } else {
            tracker.setType(TrackerType.values()[(tracker.getType().ordinal() + 1) % TrackerType.values().length]);
        }
    }

    @Override
    protected String getArrowText() {
        return tracker.getType().getName();
    }

    @Override
    protected String getArrowDescription() {
        return tracker.getType().getDescription();
    }

    @Override
    protected void save(GuiBase gui) {
        tracker.sendToServer();
    }

    public boolean doesRequiredDoublePage() {
        return false;
    }
}
