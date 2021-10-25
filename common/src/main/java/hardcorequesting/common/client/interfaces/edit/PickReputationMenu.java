package hardcorequesting.common.client.interfaces.edit;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.graphic.EditReputationGraphic;
import hardcorequesting.common.client.interfaces.widget.ExtendedScrollBar;
import hardcorequesting.common.client.interfaces.widget.ScrollBar;
import hardcorequesting.common.reputation.Reputation;
import hardcorequesting.common.reputation.ReputationManager;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.function.Consumer;

/**
 * An edit menu for picking a reputation bar.
 */
@Environment(EnvType.CLIENT)
public class PickReputationMenu extends GuiEditMenu {
    
    private final Consumer<Reputation> resultConsumer;
    private Reputation selectedReputation;
    private final ExtendedScrollBar<Reputation> scrollBar;
    
    public static void display(GuiQuestBook gui, Consumer<Reputation> resultConsumer) {
        display(gui, null, resultConsumer);
    }
    
    public static void display(GuiQuestBook gui, Reputation reputation, Consumer<Reputation> resultConsumer) {
        gui.setEditMenu(new PickReputationMenu(gui, reputation, resultConsumer));
    }
    
    private PickReputationMenu(GuiQuestBook gui, Reputation reputation, Consumer<Reputation> resultConsumer) {
        super(gui, false);
        this.resultConsumer = resultConsumer;
        this.selectedReputation = reputation;
        addScrollBar(scrollBar = new ExtendedScrollBar<>(gui, ScrollBar.Size.LONG, 160, 23, EditReputationGraphic.REPUTATION_LIST_X,
                EditReputationGraphic.VISIBLE_REPUTATIONS, () -> ReputationManager.getInstance().getReputationList()));
    }
    
    @Override
    public void draw(PoseStack matrices, int mX, int mY) {
        super.draw(matrices, mX, mY);
        
        int x = EditReputationGraphic.REPUTATION_LIST_X;
        int y = EditReputationGraphic.REPUTATION_LIST_Y;
        
        for (Reputation reputation : scrollBar.getVisibleEntries()) {
            String str = reputation.getName();
            
            boolean hover = gui.inBounds(x, y, gui.getStringWidth(str), EditReputationGraphic.FONT_HEIGHT, mX, mY);
            boolean selected = reputation.equals(selectedReputation);
            
            gui.drawString(matrices, Translator.plain(str), x, y, selected ? hover ? 0x40CC40 : 0x409040 : hover ? 0xAAAAAA : 0x404040);
            
            y += EditReputationGraphic.REPUTATION_OFFSET;
        }
        gui.drawString(matrices, gui.getLinesFromText(Translator.translatable("hqm.rep.select"), 1F, 120), EditReputationGraphic.REPUTATION_MARKER_LIST_X, EditReputationGraphic.REPUTATION_LIST_Y, 1F, 0x404040);
    }
    
    @Override
    public void onClick(int mX, int mY, int b) {
        super.onClick(mX, mY, b);
    
        int x = EditReputationGraphic.REPUTATION_LIST_X;
        int y = EditReputationGraphic.REPUTATION_LIST_Y;
        
        for (Reputation reputation : scrollBar.getVisibleEntries()) {
            String str = reputation.getName();
            
            if (gui.inBounds(x, y, gui.getStringWidth(str), EditReputationGraphic.FONT_HEIGHT, mX, mY)) {
                selectedReputation = reputation;
            }
            y += EditReputationGraphic.REPUTATION_OFFSET;
        }
    }
    
    @Override
    public void save() {
        if (selectedReputation != null)
            resultConsumer.accept(selectedReputation);
    }
}
