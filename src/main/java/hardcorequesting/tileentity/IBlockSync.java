package hardcorequesting.tileentity;


import hardcorequesting.network.DataReader;
import hardcorequesting.network.DataWriter;
import net.minecraft.entity.player.EntityPlayer;

public interface IBlockSync {
    public void writeData(DataWriter dw, EntityPlayer player, boolean onServer, int id);

    public void readData(DataReader dr, EntityPlayer player, boolean onServer, int id);

    public int infoBitLength();
}
