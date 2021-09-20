package hardcorequesting.common.reputation;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.ResourceHelper;
import hardcorequesting.common.client.interfaces.edit.EditRepBarMenu;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestSet;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.UUID;

public class ReputationBar {
    
    private String repId;
    private int x, y, questSet;
    
    public ReputationBar(Reputation reputation, int x, int y, QuestSet questSet) {
        this(reputation.getId(), x, y, questSet.getId());
    }
    
    public ReputationBar(String repId, int x, int y, int questSet) {
        this.repId = repId;
        this.x = x;
        this.y = y;
        this.questSet = questSet;
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
    
    public void setReputation(Reputation reputation) {
        repId = reputation.getId();
    }
    
    public QuestSet getQuestSet() {
        return Quest.getQuestSets().get(this.questSet);
    }
    
    public void setQuestSet(int id) {
        this.questSet = id;
    }
    
    public boolean isValid() {
        return Quest.getQuestSets().size() > this.questSet && getQuestSet() != null && ReputationManager.getInstance().getReputation(this.repId) != null;
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
        
        String info = reputation.drawAndGetTooltip(matrices, gui, this.x, this.y, mX, mY, null, playerId, false, null, null, false, null, null, false);
        
        if (info != null) {
            gui.renderTooltip(matrices, Translator.plain(info), mX + gui.getLeft(), mY + gui.getTop());
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
    public void mouseClicked(GuiQuestBook gui, int x, int y) {
        if (this.inBounds(x, y)) {
            switch (gui.getCurrentMode()) {
                case MOVE:
                    gui.modifyingBar = this;
                    SaveHelper.add(EditType.REPUTATION_BAR_MOVE);
                    break;
                case REP_BAR_CHANGE:
                    gui.setEditMenu(new EditRepBarMenu(gui, gui.getPlayer().getUUID(), this));
                    break;
                case DELETE:
                    this.getQuestSet().removeRepBar(this);
                    SaveHelper.add(EditType.REPUTATION_BAR_REMOVE);
                default:
                    break;
            }
        }
    }
}