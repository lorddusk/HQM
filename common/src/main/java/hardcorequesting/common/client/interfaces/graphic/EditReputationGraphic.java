package hardcorequesting.common.client.interfaces.graphic;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenuReputationValue;
import hardcorequesting.common.client.interfaces.edit.TextMenu;
import hardcorequesting.common.client.interfaces.widget.LargeButton;
import hardcorequesting.common.client.interfaces.widget.ScrollBar;
import hardcorequesting.common.reputation.Reputation;
import hardcorequesting.common.reputation.ReputationManager;
import hardcorequesting.common.reputation.ReputationMarker;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.FormattedText;

import java.util.List;
import java.util.UUID;

/**
 * A graphic element for displaying the page for editing reputations.
 * This page should only be accessed when editing quest line data.
 */
@Environment(EnvType.CLIENT)
public class EditReputationGraphic extends EditableGraphic {
    public static final int VISIBLE_REPUTATION_TIERS = 9;
    public static final int VISIBLE_REPUTATIONS = 10;
    public static final int REPUTATION_LIST_X = 20;
    public static final int REPUTATION_MARKER_LIST_X = 180;
    public static final int REPUTATION_LIST_Y = 20;
    public static final int REPUTATION_MARKER_LIST_Y = 35;
    public static final int REPUTATION_NEUTRAL_Y = 20;
    public static final int REPUTATION_OFFSET = 20;
    public static final int FONT_HEIGHT = 9;
    
    public static Reputation selectedReputation;
    
    private final ScrollBar reputationScroll;
    private final ScrollBar reputationTierScroll;
    {
        addClickable(new LargeButton(gui, "Create New", 180, 20) {
            @Override
            public boolean isVisible() {
                return EditReputationGraphic.this.gui.getCurrentMode() == EditMode.CREATE && selectedReputation == null;
            }
        
            @Override
            public void onClick() {
                ReputationManager.getInstance().addReputation(new Reputation("Unnamed", "Neutral"));
                SaveHelper.add(EditType.REPUTATION_ADD);
            }
        });
    
        addClickable(new LargeButton(gui, "hqm.questBook.createTier", 20, 20) {
            @Override
            public boolean isVisible() {
                return EditReputationGraphic.this.gui.getCurrentMode() == EditMode.CREATE && selectedReputation != null;
            }
        
            @Override
            public void onClick() {
                selectedReputation.add(new ReputationMarker("Unnamed", 0, false));
                SaveHelper.add(EditType.REPUTATION_MARKER_CREATE);
            }
        });
    }
    
    public EditReputationGraphic(GuiQuestBook gui) {
        super(gui, EditMode.NORMAL, EditMode.CREATE, EditMode.RENAME, EditMode.REPUTATION_VALUE, EditMode.DELETE);
        addScrollBar(reputationTierScroll = new ScrollBar(gui, ScrollBar.Size.LONG, 312, 23, EditReputationGraphic.REPUTATION_MARKER_LIST_X) {
            @Override
            public boolean isVisible() {
                return selectedReputation != null && selectedReputation.getMarkerCount() > VISIBLE_REPUTATION_TIERS;
            }
        });
    
        addScrollBar(reputationScroll = new ScrollBar(gui, ScrollBar.Size.LONG, 160, 23, EditReputationGraphic.REPUTATION_LIST_X) {
            @Override
            public boolean isVisible() {
                return (EditReputationGraphic.this.gui.getCurrentMode() != EditMode.CREATE || selectedReputation == null) && ReputationManager.getInstance().size() > VISIBLE_REPUTATIONS;
            }
        });
    }
    
    @Override
    public void draw(PoseStack matrices, int mX, int mY) {
        super.draw(matrices, mX, mY);
    
        if (gui.getCurrentMode() != EditMode.CREATE || selectedReputation == null) {
            int x = REPUTATION_LIST_X;
            int y = REPUTATION_LIST_Y;
            List<Reputation> reputationList = ReputationManager.getInstance().getReputationList();
            for (Reputation reputation : reputationScroll.getVisibleEntries(reputationList, VISIBLE_REPUTATIONS)) {
                String str = reputation.getName();
                
                boolean hover = gui.inBounds(x, y, gui.getStringWidth(str), FONT_HEIGHT, mX, mY);
                boolean selected = reputation.equals(selectedReputation);
                
                gui.drawString(matrices, Translator.plain(str), x, y, selected ? hover ? 0x40CC40 : 0x409040 : hover ? 0xAAAAAA : 0x404040);
                
                y += REPUTATION_OFFSET;
            }
        }
        
        if (selectedReputation != null) {
            FormattedText neutralName = Translator.translatable("hqm.rep.neutral", selectedReputation.getNeutralName());
            gui.drawString(matrices, neutralName, REPUTATION_MARKER_LIST_X, REPUTATION_NEUTRAL_Y, gui.inBounds(REPUTATION_MARKER_LIST_X, REPUTATION_NEUTRAL_Y, gui.getStringWidth(neutralName), FONT_HEIGHT, mX, mY) ? 0xAAAAAA : 0x404040);
    
            int x = REPUTATION_MARKER_LIST_X;
            int y = REPUTATION_MARKER_LIST_Y;
            
            for (ReputationMarker marker : reputationTierScroll.getVisibleEntries(selectedReputation.getMarkers(), VISIBLE_REPUTATION_TIERS)) {
                String str = marker.getTitle();
                
                boolean hover = gui.inBounds(x, y, gui.getStringWidth(str), FONT_HEIGHT, mX, mY);
                gui.drawString(matrices, Translator.plain(str), x, y, hover ? 0xAAAAAA : 0x404040);
                
                y += REPUTATION_OFFSET;
            }
        }
    }
    
    @Override
    public void onClick(int mX, int mY, int b) {
        super.onClick(mX, mY, b);
        UUID playerId = gui.getPlayer().getUUID();
        
        ReputationManager reputationManager = ReputationManager.getInstance();
        if (gui.getCurrentMode() != EditMode.CREATE || selectedReputation == null) {
            int x = REPUTATION_LIST_X;
            int y = REPUTATION_LIST_Y;
            
            List<Reputation> reputationList = reputationManager.getReputationList();
            for (Reputation reputation : reputationScroll.getVisibleEntries(reputationList, VISIBLE_REPUTATIONS)) {
                String str = reputation.getName();
                
                if (gui.inBounds(x, y, gui.getStringWidth(str), FONT_HEIGHT, mX, mY)) {
                    if (gui.getCurrentMode() == EditMode.NORMAL) {
                        if (reputation.equals(selectedReputation)) {
                            selectedReputation = null;
                        } else {
                            selectedReputation = reputation;
                        }
                    } else if (gui.getCurrentMode() == EditMode.RENAME) {
                        TextMenu.display(gui, playerId, reputation.getName(), true, reputation::setName);
                    } else if (gui.getCurrentMode() == EditMode.DELETE) {
                        if (selectedReputation == reputation) {
                            selectedReputation = null;
                        }
                        
                        reputationManager.removeReputation(reputation);
                        SaveHelper.add(EditType.REPUTATION_REMOVE);
                    }
                    return;
                }
                
                y += REPUTATION_OFFSET;
            }
        }
        
        if (selectedReputation != null) {
            FormattedText neutralName = Translator.translatable("hqm.rep.neutral", selectedReputation.getNeutralName());
            if (gui.inBounds(REPUTATION_MARKER_LIST_X, REPUTATION_NEUTRAL_Y, gui.getStringWidth(neutralName), FONT_HEIGHT, mX, mY)) {
                if (gui.getCurrentMode() == EditMode.RENAME) {
                    TextMenu.display(gui, playerId, selectedReputation.getNeutralName(), true, selectedReputation::setNeutralName);
                }
                return;
            }
    
            int x = REPUTATION_MARKER_LIST_X;
            int y = REPUTATION_MARKER_LIST_Y;
            for (ReputationMarker marker : reputationTierScroll.getVisibleEntries(selectedReputation.getMarkers(), VISIBLE_REPUTATION_TIERS)) {
                String str = marker.getTitle();
                
                if (gui.inBounds(x, y, gui.getStringWidth(str), FONT_HEIGHT, mX, mY)) {
                    if (gui.getCurrentMode() == EditMode.RENAME) {
                        TextMenu.display(gui, playerId, marker.getName(), true, marker::setName);
                    } else if (gui.getCurrentMode() == EditMode.REPUTATION_VALUE) {
                        gui.setEditMenu(new GuiEditMenuReputationValue(gui, marker));
                    } else if (gui.getCurrentMode() == EditMode.DELETE) {
                        
                        selectedReputation.remove(marker);
                        SaveHelper.add(EditType.REPUTATION_MARKER_REMOVE);
                    }
                    
                    return;
                }
                y += REPUTATION_OFFSET;
            }
        }
    }
}
