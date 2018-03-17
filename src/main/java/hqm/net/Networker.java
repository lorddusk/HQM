package hqm.net;

import hqm.HQM;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

/**
 * @author canitzp
 */
public class Networker {

    public static final SimpleNetworkWrapper NET;

    static {
        NET = new SimpleNetworkWrapper(HQM.MODNAME);
        NET.registerMessage(HQMPacket.class, HQMPacket.class, 0, Side.SERVER);
    }

    public static NBTTagCompound singleTag(String name, NBTBase nbt){
        NBTTagCompound data = new NBTTagCompound();
        data.setTag(name, nbt);
        return data;
    }

}
