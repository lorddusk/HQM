package hqm.net;

import hqm.HQM;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

/**
 * @author canitzp
 */
public class Networker {

    public static SimpleChannel NET;

    static {
        NET = NetworkRegistry.newSimpleChannel(new ResourceLocation(HQM.MODID, "main"), () -> "1.0", s -> true, s -> true);
        
        NET.registerMessage(0, HQMPacket.class, HQMPacket::toBytes, HQMPacket::new, HQMPacket::handle);
    }

    public static CompoundNBT singleTag(String name, INBT nbt){
        CompoundNBT data = new CompoundNBT();
        data.put(name, nbt);
        return data;
    }

}
