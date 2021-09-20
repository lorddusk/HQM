package hardcorequesting.common.client.interfaces.edit;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.graphic.EditReputationGraphic;
import hardcorequesting.common.client.interfaces.widget.ScrollBar;
import hardcorequesting.common.reputation.Reputation;
import hardcorequesting.common.reputation.ReputationBar;
import hardcorequesting.common.reputation.ReputationManager;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class EditRepBarMenu extends GuiEditMenu {
    
    private final ReputationBar bar;
    private final boolean isNew;
    private final ScrollBar scrollBar;
    
    public EditRepBarMenu(GuiQuestBook gui, UUID playerId, ReputationBar bar) {
        this(gui, playerId, bar, false);
    }
    
    public EditRepBarMenu(GuiQuestBook gui, UUID playerId, int x, int y, int selectedSet) {
        this(gui, playerId, new ReputationBar(null, x, y, selectedSet), true);
    }
    
    private EditRepBarMenu(GuiQuestBook gui, UUID playerId, ReputationBar bar, boolean isNew) {
        super(gui, playerId);
        this.bar = bar;
        this.isNew = isNew;
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
            boolean selected = reputationList.get(i).equals(reputationManager.getReputation(bar.getRepId()));
            
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
                bar.setReputation(reputationList.get(i));
                save();
                close();
            }
        }
    }
    
    @Override
    public void save() {
        if (isNew) {
            bar.getQuestSet().addRepBar(bar);
            SaveHelper.add(EditType.REPUTATION_BAR_ADD);
        } else {
            SaveHelper.add(EditType.REPUTATION_BAR_CHANGE);
        }
    }
}
