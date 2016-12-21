package hardcorequesting.client.sounds;

import com.google.common.collect.Lists;
import hardcorequesting.ModInformation;
import hardcorequesting.client.ClientChange;
import hardcorequesting.network.NetworkManager;
import hardcorequesting.quests.QuestingData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundList;
import net.minecraft.client.resources.FallbackResourceManager;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SoundHandler {
    private SoundHandler() {
    }

    private static List<String> paths = new ArrayList<>();
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
                FallbackResourceManager resourceManager = (FallbackResourceManager) resourceManagers.get("hardcorequesting");
                resourceManager.addResourcePack(new LoreResourcePack(new File(path)));

                // Add lore file to sound handler
                net.minecraft.client.audio.SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();

                Sound entry = new Sound(LABEL + number, 1.0f, 1.0f, 0, Sound.Type.SOUND_EVENT, true);
                SoundList list = new SoundList(Lists.newArrayList(entry), true, "sub");
//                list.setSoundCategory(SoundCategory.MASTER);

//                entry.setSoundEntryName(LABEL + number);
//                list.getSoundList().add(entry);

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
                } catch (InvocationTargetException | IllegalAccessException e) {
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


    public static void play(Sounds sound, EntityPlayer player) {
        if (player instanceof EntityPlayerMP)
            NetworkManager.sendToPlayer(ClientChange.SOUND.build(sound), (EntityPlayerMP) player);
    }

    public static void playToAll(Sounds sound) {
        NetworkManager.sendToAllPlayers(ClientChange.SOUND.build(sound));
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
            new Thread(() ->
            {
                while (isLorePlaying()) {    // Somehow it doesn't stop the sound on closing the book with escape
                    Minecraft.getMinecraft().getSoundHandler().stopSound(loreSound);
                }
                loreSound = null;
            }).start();
        }
    }

    public static boolean isLorePlaying() {
        boolean value = loreSound != null && Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(loreSound);

        if (!value)
            loreSound = null;

        return value;
    }

    public static boolean hasLoreMusic() {
        return loreMusic;
    }

    public static void handleSoundPacket(Sounds sound) {
        play(sound.getSoundName(), 1F, 1F);
    }

    public static void triggerFirstLore() {
        NetworkManager.sendToServer(ClientChange.LORE.build(null));
        playLoreMusic();
    }

    public static void handleLorePacket(EntityPlayer player) {
        QuestingData.getQuestingData(player).playedLore = true;
    }
}

