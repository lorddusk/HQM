package hardcorequesting.util;

import hqm.api.ISimpleParsable;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author canitzp
 * @since 5.4.0
 */
public class HQMUtil {

    /**
     * A true and save way to determine if a player is actually in a single player only game. No Server nor Integrated Server (LAN World)
     *
     * @return true if the player plays in a real single player world
     */
    public static boolean isGameSingleplayer() {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        return server == null || server.isSinglePlayer();
        // casuses dedicated server crash
        // return server == null || (server instanceof IntegratedServer && !(((IntegratedServer) server).getPublic()));
    }

    @Nullable
    public static <T> T tryToCreateClassOfType(@Nonnull String className, @Nonnull Class<T> hasToBeAssignableFrom) {
        try {
            Class c = Class.forName(className);
            if (hasToBeAssignableFrom.isAssignableFrom(c)) {
                return (T) c.newInstance();
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Integer> getIntListFromNBT(NBTTagCompound nbt, String key) {
        List<Integer> list = new ArrayList<>();
        for (NBTBase base : nbt.getTagList(key, Constants.NBT.TAG_INT)) {
            if (base instanceof NBTTagInt) {
                list.add(((NBTTagInt) base).getInt());
            }
        }
        return list;
    }

    public static void setIntListToNBT(NBTTagCompound nbt, String key, List<Integer> ints) {
        NBTTagList nbtTagList = new NBTTagList();
        for (int i : ints) {
            nbtTagList.appendTag(new NBTTagInt(i));
        }
        nbt.setTag(key, nbtTagList);
    }

    @Nullable
    public static <T extends ISimpleParsable> T getInstanceFromNBT(NBTTagCompound nbt, String key, Class<T> classToGet) {
        NBTTagCompound rawData = nbt.getCompoundTag(key);
        if (!rawData.isEmpty() && rawData.hasKey("Class", Constants.NBT.TAG_STRING) && rawData.hasKey("Data", Constants.NBT.TAG_COMPOUND)) {
            try {
                Class c = Class.forName(rawData.getString("Class"));
                if (classToGet.isAssignableFrom(c)) {
                    Object o = c.newInstance();
                    if (o instanceof ISimpleParsable) {
                        ((ISimpleParsable) o).onLoad(rawData.getCompoundTag("Data"));
                        return (T) o;
                    }
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void setInstanceToNBT(@Nonnull NBTTagCompound nbt, @Nonnull String key, @Nonnull ISimpleParsable object) {
        NBTTagCompound rawData = new NBTTagCompound();
        rawData.setString("Class", object.getClassName());
        rawData.setTag("Data", object.getData());
        nbt.setTag(key, rawData);
    }

}
