package hardcorequesting.common.reputation;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.ResourceHelper;
import hardcorequesting.common.client.interfaces.edit.PickReputationMenu;
import hardcorequesting.common.quests.QuestSet;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.FormattedText;

import java.util.UUID;

public class ReputationBar {
    
    private String repId;
    private int x, y;
    
    public ReputationBar(Reputation reputation, int x, int y) {
        this(reputation.getId(), x, y);
    }
    
    public ReputationBar(String repId, int x, int y) {
        this.repId = repId;
        this.x = x;
        this.y = y;
    }
    
    public void moveTo(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public String getRepId() {
        return repId;
    }
    
    public Reputation getReputation() {
        return ReputationManager.getInstance().getReputation(repId);
    }
    
    public void setReputation(Reputation reputation) {
        repId = reputation.getId();
    }
    
    public boolean isValid() {
        return ReputationManager.getInstance().getReputation(this.repId) != null;
    }
    
    public boolean sameLocation(ReputationBar reputationBar) {
        return reputationBar != null && reputationBar.x == this.x && reputationBar.y == this.y;
    }
    
    @Environment(EnvType.CLIENT)
    public void draw(PoseStack matrices, GuiQuestBook gui, int mX, int mY, UUID playerId) {
        Reputation reputation = ReputationManager.getInstance().getReputation(this.repId);
        if (reputation == null) return;
        
        gui.applyColor(0xFFFFFFFF);
        ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
    
        FormattedText info = reputation.drawAndGetTooltip(matrices, gui, this.x, this.y, mX, mY, null, playerId, false, null, null, false, null, null, false);
        
        if (info != null) {
            gui.renderTooltip(matrices, info, mX + gui.getLeft(), mY + gui.getTop());
        }
    }
    
    @Environment(EnvType.CLIENT)
    public boolean inBounds(int mX, int mY) {
        return
                this.x <= mX &&
                this.x + Reputation.BAR_WIDTH >= mX &&
                this.y - Reputation.BAR_HEIGHT * 3 <= mY &&
                this.y + Reputation.BAR_HEIGHT * 6 >= mY;
    }
    
    @Environment(EnvType.CLIENT)
    public void mouseClicked(GuiQuestBook gui, QuestSet set, int x, int y) {
        if (this.inBounds(x, y)) {
            switch (gui.getCurrentMode()) {
                case MOVE:
                    gui.modifyingBar = this;
                    SaveHelper.add(EditType.REPUTATION_BAR_MOVE);
                    break;
                case REP_BAR_CHANGE:
                    PickReputationMenu.display(gui, getReputation(), reputation -> {
                        this.setReputation(reputation);
                        SaveHelper.add(EditType.REPUTATION_BAR_CHANGE);
                    });
                    break;
                case DELETE:
                    set.removeRepBar(this);
                    SaveHelper.add(EditType.REPUTATION_BAR_REMOVE);
                default:
                    break;
            }
        }
    }
}