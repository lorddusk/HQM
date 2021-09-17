package hardcorequesting.common.reputation;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.ResourceHelper;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenu;
import hardcorequesting.common.client.interfaces.graphic.EditReputationGraphic;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestSet;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;
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

    /*
    public int save() {
        //T ODO
        return -1;
    }*/
    
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
                    gui.setEditMenu(new EditGui(gui, gui.getPlayer().getUUID(), this));
                    break;
                case DELETE:
                    this.getQuestSet().removeRepBar(this);
                    SaveHelper.add(EditType.REPUTATION_BAR_REMOVE);
                default:
                    break;
            }
        }
    }
    
    @Environment(EnvType.CLIENT)
    public static class EditGui extends GuiEditMenu {
        
        private final ReputationBar bar;
        private final boolean isNew;
        
        public EditGui(GuiBase guiBase, UUID playerId, ReputationBar bar) {
            super(playerId);
            this.bar = bar;
            this.isNew = false;
        }
        
        public EditGui(GuiBase guiBase, UUID playerId, int x, int y, int selectedSet) {
            super(playerId);
            this.bar = new ReputationBar(null, x, y, selectedSet);
            this.isNew = true;
        }
        
        @Override
        @Environment(EnvType.CLIENT)
        public void draw(PoseStack matrices, GuiBase guiB, int mX, int mY) {
            ReputationManager reputationManager = ReputationManager.getInstance();
            GuiQuestBook gui = (GuiQuestBook) guiB;
            int start = gui.reputationScroll.isVisible(gui) ? Math.round((reputationManager.size() - GuiQuestBook.VISIBLE_REPUTATIONS) * gui.reputationScroll.getScroll()) : 0;
            int end = Math.min(start + GuiQuestBook.VISIBLE_REPUTATIONS, reputationManager.size());
            List<Reputation> reputationList = reputationManager.getReputationList();
            for (int i = start; i < end; i++) {
                int x = EditReputationGraphic.REPUTATION_LIST_X;
                int y = EditReputationGraphic.REPUTATION_LIST_Y + (i - start) * EditReputationGraphic.REPUTATION_OFFSET;
                String str = reputationList.get(i).getName();
                
                boolean hover = gui.inBounds(x, y, gui.getStringWidth(str), EditReputationGraphic.FONT_HEIGHT, mX, mY);
                boolean selected = reputationList.get(i).equals(reputationManager.getReputation(bar.repId));
                
                gui.drawString(matrices, Translator.plain(str), x, y, selected ? hover ? 0x40CC40 : 0x409040 : hover ? 0xAAAAAA : 0x404040);
            }
            gui.drawString(matrices, gui.getLinesFromText(Translator.translatable("hqm.rep.select"), 1F, 120), EditReputationGraphic.REPUTATION_MARKER_LIST_X, EditReputationGraphic.REPUTATION_LIST_Y, 1F, 0x404040);
        }
        
        @Environment(EnvType.CLIENT)
        public void onClick(GuiBase guiB, int mX, int mY, int b) {
            super.onClick(guiB, mX, mY, b);
            ReputationManager reputationManager = ReputationManager.getInstance();
            
            GuiQuestBook gui = (GuiQuestBook) guiB;
            int start = gui.reputationScroll.isVisible(gui) ? Math.round((reputationManager.size() - GuiQuestBook.VISIBLE_REPUTATIONS) * gui.reputationScroll.getScroll()) : 0;
            int end = Math.min(start + GuiQuestBook.VISIBLE_REPUTATIONS, reputationManager.size());
            List<Reputation> reputationList = reputationManager.getReputationList();
            for (int i = start; i < end; i++) {
                int x = EditReputationGraphic.REPUTATION_LIST_X;
                int y = EditReputationGraphic.REPUTATION_LIST_Y + (i - start) * EditReputationGraphic.REPUTATION_OFFSET;
                String str = reputationList.get(i).getName();
                
                if (gui.inBounds(x, y, gui.getStringWidth(str), EditReputationGraphic.FONT_HEIGHT, mX, mY)) {
                    bar.repId = reputationList.get(i).getId();
                    save(guiB);
                    close(guiB);
                }
            }
        }
        
        @Override
        public void save(GuiBase gui) {
            if (isNew) {
                Quest.getQuestSets().get(bar.questSet).addRepBar(bar);
                SaveHelper.add(EditType.REPUTATION_BAR_ADD);
            } else {
                SaveHelper.add(EditType.REPUTATION_BAR_CHANGE);
            }
        }
    }
}
