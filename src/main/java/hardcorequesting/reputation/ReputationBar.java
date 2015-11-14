package hardcorequesting.reputation;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import hardcorequesting.FileVersion;
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

    public int writeDate()
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
        return getQuestSet() != null;
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
}
