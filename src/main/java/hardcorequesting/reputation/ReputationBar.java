package hardcorequesting.reputation;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import hardcorequesting.FileVersion;
import hardcorequesting.SaveHelper;
import hardcorequesting.Translator;
import hardcorequesting.client.interfaces.GuiBase;
import hardcorequesting.client.interfaces.GuiEditMenu;
import hardcorequesting.client.interfaces.GuiQuestBook;
import hardcorequesting.client.interfaces.ResourceHelper;
import hardcorequesting.network.DataBitHelper;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestSet;
import net.minecraft.entity.player.EntityPlayer;

public class ReputationBar
{
    private int repId, x, y, questSet;

    public ReputationBar(Reputation reputation, int x, int y, QuestSet questSet)
    {
        this(reputation.getId(), x, y, questSet.getId());
    }

    public ReputationBar(int repId, int x, int y, int questSet)
    {
        this.repId = repId;
        this.x = x;
        this.y = y;
        this.questSet = questSet;
    }

    private static final int posBits = 9, posBitMask = 511;

    public ReputationBar(FileVersion version, int data)
    {
        int questSetSize = DataBitHelper.QUEST_SETS.getBitCount(version);
        this.repId = data >> questSetSize + posBits*2;
        this.x = (data >> questSetSize + posBits) & posBitMask;
        this.y = (data >> questSetSize) & posBitMask;
        this.questSet = data & ((1 << questSetSize+1) - 1);
    }

    public void moveTo(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public int save()
    {
        int questSetSize = DataBitHelper.QUEST_SETS.getBitCount();
        return this.repId << questSetSize + 18 | this.x << questSetSize + 9 | this.y << questSetSize | this.questSet;
    }

    public QuestSet getQuestSet()
    {
        return Quest.getQuestSets().get(this.questSet);
    }

    public int getQuestSetId()
    {
        return this.questSet;
    }

    public boolean isValid()
    {
        return getQuestSet() != null && Reputation.getReputation(this.repId) != null;
    }

    @SideOnly(Side.CLIENT)
    public void draw(GuiQuestBook gui, int mX, int mY, EntityPlayer player)
    {
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
    public boolean inBounds(int mX, int mY)
    {
        return
                this.x <= mX &&
                this.x + Reputation.BAR_WIDTH >= mX &&
                this.y - Reputation.BAR_HEIGHT*3 <= mY &&
                this.y + Reputation.BAR_HEIGHT*6 >= mY;
    }

    public static class EditGui extends GuiEditMenu {

        private ReputationBar bar;
        private boolean isNew;

        public EditGui(GuiBase guiBase, EntityPlayer player, ReputationBar bar)
        {
            super(guiBase, player);
            this.bar = bar;
            this.isNew = false;
        }

        public EditGui(GuiBase guiBase, EntityPlayer player, int x, int y, int selectedSet)
        {
            super(guiBase, player);
            this.bar = new ReputationBar(-1, x, y, selectedSet);
            this.isNew = true;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void draw(GuiBase guiB, int mX, int mY) {
            GuiQuestBook gui = (GuiQuestBook) guiB;
            int start = gui.reputationScroll.isVisible(gui) ? Math.round((Reputation.getReputationList().size() - GuiQuestBook.VISIBLE_REPUTATIONS) * gui.reputationScroll.getScroll()) : 0;
            int end = Math.min(start + GuiQuestBook.VISIBLE_REPUTATIONS, Reputation.getReputationList().size());
            for (int i = start; i < end; i++) {
                int x = Reputation.REPUTATION_LIST_X;
                int y = Reputation.REPUTATION_LIST_Y + (i - start) * Reputation.REPUTATION_OFFSET;
                String str = Reputation.getReputationList().get(i).getName();

                boolean hover = gui.inBounds(x, y, gui.getStringWidth(str), Reputation.FONT_HEIGHT, mX, mY);
                boolean selected = Reputation.getReputationList().get(i).equals(Reputation.getReputation(bar.repId));

                gui.drawString(str, x, y, selected ? hover ? 0x40CC40 : 0x409040 : hover ? 0xAAAAAA : 0x404040);
            }

            gui.drawString(Translator.translate("hqm.rep.select"), Reputation.REPUTATION_MARKER_LIST_X, Reputation.REPUTATION_LIST_Y, 0x404040);
        }

        @SideOnly(Side.CLIENT)
        public void onClick(GuiBase guiB, int mX, int mY, int b) {
            super.onClick(guiB, mX, mY, b);
            GuiQuestBook gui = (GuiQuestBook) guiB;
            int start = gui.reputationScroll.isVisible(gui) ? Math.round((Reputation.getReputationList().size() - GuiQuestBook.VISIBLE_REPUTATIONS) * gui.reputationScroll.getScroll()) : 0;
            int end = Math.min(start + GuiQuestBook.VISIBLE_REPUTATIONS, Reputation.getReputationList().size());
            for (int i = start; i < end; i++) {
                int x = Reputation.REPUTATION_LIST_X;
                int y = Reputation.REPUTATION_LIST_Y + (i - start) * Reputation.REPUTATION_OFFSET;
                String str = Reputation.getReputationList().get(i).getName();

                if (gui.inBounds(x, y, gui.getStringWidth(str), Reputation.FONT_HEIGHT, mX, mY)) {
                    bar.repId = Reputation.getReputationList().get(i).getId();
                    save(guiB);
                    close(guiB);
                }
            }
        }

        @Override
        protected void save(GuiBase gui) {
            if (isNew) {
                Quest.getQuestSets().get(bar.questSet).addRepBar(bar);
                SaveHelper.add(SaveHelper.EditType.REP_BAR_ADD);
            }
        }
    }
}
