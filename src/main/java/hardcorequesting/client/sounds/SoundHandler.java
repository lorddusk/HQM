package hardcorequesting.client.sounds;


import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import hardcorequesting.ModInformation;
import hardcorequesting.QuestingData;
import hardcorequesting.network.DataReader;
import hardcorequesting.network.DataWriter;
import hardcorequesting.network.PacketHandler;
import hardcorequesting.network.PacketId;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.audio.SoundList;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.resources.FallbackResourceManager;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.sound.SoundEvent;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.common.MinecraftForge;


import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SoundHandler {

    private SoundHandler() {
    }

    private static List<String> paths = new ArrayList<String>();
    private static final String LABEL = "lore";

    @SideOnly(Side.CLIENT)
    public static boolean loadLoreReading(String path) {
        loreMusic = false;
        loreNumber = -1;

        int index = paths.indexOf(path);
        if (index == -1) {
            if (new File(path + "lore.ogg").exists()) {
                int number = paths.size();

                // Add resource pack to discover lore
                Map resourceManagers = ReflectionHelper.getPrivateValue(SimpleReloadableResourceManager.class, (SimpleReloadableResourceManager) Minecraft.getMinecraft().getResourceManager(), 2);
                FallbackResourceManager resourceManager = (FallbackResourceManager) resourceManagers.get("hqm");
                resourceManager.addResourcePack(new LoreResourcePack(new File(path)));

                // Add lore file to sound handler
                net.minecraft.client.audio.SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();

                SoundList list = new SoundList();
                list.setSoundCategory(SoundCategory.MASTER);

                SoundList.SoundEntry entry = new SoundList.SoundEntry();
                entry.setSoundEntryName(LABEL + number);
                list.getSoundList().add(entry);

                Method method = ReflectionHelper.findMethod(net.minecraft.client.audio.SoundHandler.class, handler, new String[]{"loadSoundResource", "func_147693_a", "a"}, ResourceLocation.class, SoundList.class);
                if (method == null || handler == null) {
                    return false;
                }
                try {
                    method.invoke(handler, new ResourceLocation(ModInformation.SOUNDLOC, LABEL + number), list);
                    loreMusic = true;
                    loreNumber = number;
                    paths.add(path);
                    return true;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        } else {
            loreNumber = index;
            loreMusic = true;
            return true;
        }
        return false;
    }

    private static int loreNumber;
    private static boolean loreMusic = false;
    @SideOnly(Side.CLIENT)
    private static ISound loreSound;

    @SideOnly(Side.CLIENT)
    public static void playLoreMusic() {
        loreSound = play(LABEL + loreNumber, 4F, 1F);
    }


    private static int bitCount = -1;

    private static int getBitCount() {
        if (bitCount == -1) {
            bitCount = (int) (Math.log10(Sounds.values().length + 1) / Math.log10(2)) + 1;
        }

        return bitCount;
    }

    public static void play(Sounds sound, EntityPlayer player) {
        DataWriter dw = PacketHandler.getWriter(PacketId.SOUND);
        dw.writeData(sound.ordinal(), getBitCount());
        PacketHandler.sendToRawPlayer(player, dw);
    }

    public static void playToAll(Sounds sound) {
        DataWriter dw = PacketHandler.getWriter(PacketId.SOUND);
        dw.writeData(sound.ordinal(), getBitCount());
        PacketHandler.sendToAllPlayers(dw);
    }

    @SideOnly(Side.CLIENT)
    private static ISound play(String sound, float volume, float pitch) {
        return play(new ResourceLocation(ModInformation.SOUNDLOC, sound), volume, pitch);
    }

    @SideOnly(Side.CLIENT)
    private static ISound play(ResourceLocation resource, float volume, float pitch) {
        ISound soundObj = new ClientSound(resource, volume, pitch);
        Minecraft.getMinecraft().getSoundHandler().playSound(soundObj);
        return soundObj;
    }

    public static void stopLoreMusic() {
        if (isLorePlaying()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isLorePlaying()) {    // Somehow it doesn't stop the sound on closing the book with escape
                        Minecraft.getMinecraft().getSoundHandler().stopSound(loreSound);
                    }
                    loreSound = null;
                }
            }).start();
        }
    }

    public static boolean isLorePlaying() {
        boolean value = loreSound != null && Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(loreSound);

        if (!value) {
            loreSound = null;
        }

        return value;
    }

    public static boolean hasLoreMusic() {
        return loreMusic;
    }

    public static void handleSoundPacket(DataReader dr) {
        int id = dr.readData(getBitCount());
        play(Sounds.values()[id].getSound(), 1F, 1F);
    }

    public static void triggerFirstLore() {
        PacketHandler.sendToServer(PacketHandler.getWriter(PacketId.LORE));
        playLoreMusic();
    }

    public static void handleLorePacket(EntityPlayer player, DataReader dr) {
        QuestingData.getQuestingData(player).playedLore = true;
    }
}

