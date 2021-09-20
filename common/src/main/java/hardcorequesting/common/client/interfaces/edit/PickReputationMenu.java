package hardcorequesting.common.client.interfaces.edit;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.graphic.EditReputationGraphic;
import hardcorequesting.common.client.interfaces.widget.ScrollBar;
import hardcorequesting.common.reputation.Reputation;
import hardcorequesting.common.reputation.ReputationManager;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class PickReputationMenu extends GuiEditMenu {
    
    private final Consumer<Reputation> resultConsumer;
    private Reputation reputation;
    private final ScrollBar scrollBar;
    
    public static void display(GuiQuestBook gui, Consumer<Reputation> resultConsumer) {
        display(gui, null, resultConsumer);
    }
    
    public static void display(GuiQuestBook gui, Reputation reputation, Consumer<Reputation> resultConsumer) {
        gui.setEditMenu(new PickReputationMenu(gui, gui.getPlayer().getUUID(), reputation, resultConsumer));
    }
    
    private PickReputationMenu(GuiQuestBook gui, UUID playerId, Reputation reputation, Consumer<Reputation> resultConsumer) {
        super(gui, playerId);
        this.resultConsumer = resultConsumer;
        this.reputation = reputation;
        addScrollBar(scrollBar = new ScrollBar(gui, 160, 23, 186, 171, 69, EditReputationGraphic.REPUTATION_LIST_X) {
            @Override
            public boolean isVisible() {
                return ReputationManager.getInstance().size() > EditReputationGraphic.VISIBLE_REPUTATIONS;
            }
        });
    }
    
    @Override
    public void draw(PoseStack matrices, int mX, int mY) {
        ReputationManager reputationManager = ReputationManager.getInstance();
        int start = scrollBar.isVisible() ? Math.round((reputationManager.size() - EditReputationGraphic.VISIBLE_REPUTATIONS) * scrollBar.getScroll()) : 0;
        int end = Math.min(start + EditReputationGraphic.VISIBLE_REPUTATIONS, reputationManager.size());
        List<Reputation> reputationList = reputationManager.getReputationList();
        for (int i = start; i < end; i++) {
            int x = EditReputationGraphic.REPUTATION_LIST_X;
            int y = EditReputationGraphic.REPUTATION_LIST_Y + (i - start) * EditReputationGraphic.REPUTATION_OFFSET;
            String str = reputationList.get(i).getName();
            
            boolean hover = gui.inBounds(x, y, gui.getStringWidth(str), EditReputationGraphic.FONT_HEIGHT, mX, mY);
            boolean selected = reputationList.get(i).equals(reputation);
            
            gui.drawString(matrices, Translator.plain(str), x, y, selected ? hover ? 0x40CC40 : 0x409040 : hover ? 0xAAAAAA : 0x404040);
        }
        gui.drawString(matrices, gui.getLinesFromText(Translator.translatable("hqm.rep.select"), 1F, 120), EditReputationGraphic.REPUTATION_MARKER_LIST_X, EditReputationGraphic.REPUTATION_LIST_Y, 1F, 0x404040);
    }
    
    @Override
    public void onClick(int mX, int mY, int b) {
        super.onClick(mX, mY, b);
        ReputationManager reputationManager = ReputationManager.getInstance();
        
        int start = scrollBar.isVisible() ? Math.round((reputationManager.size() - EditReputationGraphic.VISIBLE_REPUTATIONS) * scrollBar.getScroll()) : 0;
        int end = Math.min(start + EditReputationGraphic.VISIBLE_REPUTATIONS, reputationManager.size());
        List<Reputation> reputationList = reputationManager.getReputationList();
        for (int i = start; i < end; i++) {
            int x = EditReputationGraphic.REPUTATION_LIST_X;
            int y = EditReputationGraphic.REPUTATION_LIST_Y + (i - start) * EditReputationGraphic.REPUTATION_OFFSET;
            String str = reputationList.get(i).getName();
            
            if (gui.inBounds(x, y, gui.getStringWidth(str), EditReputationGraphic.FONT_HEIGHT, mX, mY)) {
                reputation = reputationList.get(i);
                save();
                close();
            }
        }
    }
    
    @Override
    public void save() {
        resultConsumer.accept(reputation);
    }
}
