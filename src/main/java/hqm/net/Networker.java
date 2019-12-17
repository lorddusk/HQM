package hqm.net;

import hqm.HQM;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;

/**
 * @author canitzp
 */
public class Networker {

    // todo 1.15 implement network traffic
    //public static final SimpleNetworkWrapper NET;

    static {
        //NET = new SimpleNetworkWrapper(HQM.MODNAME);
        //NET.registerMessage(HQMPacket.class, HQMPacket.class, 0, Side.SERVER);
    }

    public static CompoundNBT singleTag(String name, INBT nbt){
        CompoundNBT data = new CompoundNBT();
        data.put(name, nbt);
        return data;
    }

}
