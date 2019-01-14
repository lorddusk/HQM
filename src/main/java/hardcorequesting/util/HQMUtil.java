package hardcorequesting.util;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author canitzp
 * @since 5.4.0
 */
public class HQMUtil{
    
    /**
     * A true and save way to determine if a player is actually in a single player only game. No Server nor Integrated Server (LAN World)
     *
     * @param world The world of the player
     * @return true if the player plays in a real single player world
     */
    public static boolean isGameSingleplayer(@Nonnull World world){
        MinecraftServer server = world.getMinecraftServer();
        return server == null || (server instanceof IntegratedServer && !(((IntegratedServer) server).getPublic()));
    }
    
    @Nullable
    public static <T> T tryToCreateClassOfType(@Nonnull String className, @Nonnull Class<T> hasToBeAssignableFrom){
        try{
            Class c = Class.forName(className);
            if(hasToBeAssignableFrom.isAssignableFrom(c)){
                return (T) c.newInstance();
            }
        }catch(ClassNotFoundException | InstantiationException | IllegalAccessException e){
            e.printStackTrace();
        }
        return null;
    }
    
    public static List<Integer> getIntListFromNBT(NBTTagCompound nbt, String key){
        List<Integer> list = new ArrayList<>();
        for(NBTBase base : nbt.getTagList(key, Constants.NBT.TAG_INT)){
            if(base instanceof NBTTagInt){
                list.add(((NBTTagInt) base).getInt());
            }
        }
        return list;
    }
    
}
