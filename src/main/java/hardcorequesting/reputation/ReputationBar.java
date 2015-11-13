package hardcorequesting.reputation;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import hardcorequesting.client.interfaces.GuiQuestBook;
import hardcorequesting.client.interfaces.ResourceHelper;
import net.minecraft.entity.player.EntityPlayer;

import java.awt.Polygon;

public class ReputationBar
{
    private int id, x, y;

    public ReputationBar(Reputation reputation, int x, int y)
    {
        this(reputation.getId(), x, y);
    }

    public ReputationBar(int id, int x, int y)
    {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public void moveTo(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    @SideOnly(Side.CLIENT)
    public void draw(GuiQuestBook gui, int mX, int mY, EntityPlayer player)
    {
        Reputation reputation = Reputation.getReputation(this.id);
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
        Polygon polygon = new Polygon();

        polygon.addPoint(this.x , this.y - Reputation.BAR_HEIGHT*2);
        polygon.addPoint(this.x + Reputation.BAR_WIDTH, this.y - Reputation.BAR_HEIGHT*2);
        polygon.addPoint(this.x , this.y + Reputation.BAR_HEIGHT*2);
        polygon.addPoint(this.x + Reputation.BAR_WIDTH, this.y + Reputation.BAR_HEIGHT*2);

        return polygon.contains(mX, mY);
    }
}
