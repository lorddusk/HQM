package hardcorequesting.reputation;

import hardcorequesting.client.interfaces.GuiBase;
import hardcorequesting.client.interfaces.GuiQuestBook;
import hardcorequesting.client.interfaces.ResourceHelper;
import hardcorequesting.client.interfaces.edit.GuiEditMenu;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestSet;
import hardcorequesting.util.SaveHelper;
import hardcorequesting.util.Translator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

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

    public int save() {
        //TODO
        return -1;
    }

    public QuestSet getQuestSet() {
        return Quest.getQuestSets().get(this.questSet);
    }

    public void setQuestSet(int id) {
        this.questSet = id;
    }

    public boolean isValid() {
        return Quest.getQuestSets().size() > this.questSet && getQuestSet() != null && Reputation.getReputation(this.repId) != null;
    }

    public boolean sameLocation(ReputationBar reputationBar) {
        return reputationBar != null && reputationBar.x == this.x && reputationBar.y == this.y;
    }

    @SideOnly(Side.CLIENT)
    public void draw(GuiQuestBook gui, int mX, int mY, EntityPlayer player) {
        Reputation reputation = Reputation.getReputation(this.repId);
        if (reputation == null) return;

        gui.applyColor(0xFFFFFFFF);
        ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);

        String info = reputation.draw(gui, this.x, this.y, mX, mY, null, player, false, null, null, false, null, null, false);

        if (info != null) {
            gui.drawMouseOver(info, mX + gui.getLeft(), mY + gui.getTop());
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean inBounds(int mX, int mY) {
        return
                this.x <= mX &&
                        this.x + Reputation.BAR_WIDTH >= mX &&
                        this.y - Reputation.BAR_HEIGHT * 3 <= mY &&
                        this.y + Reputation.BAR_HEIGHT * 6 >= mY;
    }

    @SideOnly(Side.CLIENT)
    public void mouseClicked(GuiQuestBook gui, int x, int y) {
        if (this.inBounds(x, y)) {
            switch (gui.getCurrentMode()) {
                case MOVE:
                    gui.modifyingBar = this;
                    SaveHelper.add(SaveHelper.EditType.REPUTATION_BAR_MOVE);
                    break;
                case REP_BAR_CHANGE:
                    gui.setEditMenu(new ReputationBar.EditGui(gui, gui.getPlayer(), this));
                    break;
                case DELETE:
                    this.getQuestSet().removeRepBar(this);
                    SaveHelper.add(SaveHelper.EditType.REPUTATION_BAR_REMOVE);
                default:
                    break;
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static class EditGui extends GuiEditMenu {

        private ReputationBar bar;
        private boolean isNew;

        public EditGui(GuiBase guiBase, EntityPlayer player, ReputationBar bar) {
            super(guiBase, player);
            this.bar = bar;
            this.isNew = false;
        }

        public EditGui(GuiBase guiBase, EntityPlayer player, int x, int y, int selectedSet) {
            super(guiBase, player);
            this.bar = new ReputationBar(null, x, y, selectedSet);
            this.isNew = true;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void draw(GuiBase guiB, int mX, int mY) {
            GuiQuestBook gui = (GuiQuestBook) guiB;
            int start = gui.reputationScroll.isVisible(gui) ? Math.round((Reputation.getReputations().size() - GuiQuestBook.VISIBLE_REPUTATIONS) * gui.reputationScroll.getScroll()) : 0;
            int end = Math.min(start + GuiQuestBook.VISIBLE_REPUTATIONS, Reputation.getReputations().size());
            List<Reputation> reputationList = Reputation.getReputationList();
            for (int i = start; i < end; i++) {
                int x = Reputation.REPUTATION_LIST_X;
                int y = Reputation.REPUTATION_LIST_Y + (i - start) * Reputation.REPUTATION_OFFSET;
                String str = reputationList.get(i).getName();

                boolean hover = gui.inBounds(x, y, gui.getStringWidth(str), Reputation.FONT_HEIGHT, mX, mY);
                boolean selected = reputationList.get(i).equals(Reputation.getReputation(bar.repId));

                gui.drawString(str, x, y, selected ? hover ? 0x40CC40 : 0x409040 : hover ? 0xAAAAAA : 0x404040);
            }
            gui.drawString(gui.getLinesFromText(Translator.translate("hqm.rep.select"), 1F, 120), Reputation.REPUTATION_MARKER_LIST_X, Reputation.REPUTATION_LIST_Y, 1F, 0x404040);
        }

        @SideOnly(Side.CLIENT)
        public void onClick(GuiBase guiB, int mX, int mY, int b) {
            super.onClick(guiB, mX, mY, b);
            GuiQuestBook gui = (GuiQuestBook) guiB;
            int start = gui.reputationScroll.isVisible(gui) ? Math.round((Reputation.getReputations().size() - GuiQuestBook.VISIBLE_REPUTATIONS) * gui.reputationScroll.getScroll()) : 0;
            int end = Math.min(start + GuiQuestBook.VISIBLE_REPUTATIONS, Reputation.getReputations().size());
            List<Reputation> reputationList = Reputation.getReputationList();
            for (int i = start; i < end; i++) {
                int x = Reputation.REPUTATION_LIST_X;
                int y = Reputation.REPUTATION_LIST_Y + (i - start) * Reputation.REPUTATION_OFFSET;
                String str = reputationList.get(i).getName();

                if (gui.inBounds(x, y, gui.getStringWidth(str), Reputation.FONT_HEIGHT, mX, mY)) {
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
                SaveHelper.add(SaveHelper.EditType.REPUTATION_BAR_ADD);
            } else {
                SaveHelper.add(SaveHelper.EditType.REPUTATION_BAR_CHANGE);
            }
        }
    }
}
