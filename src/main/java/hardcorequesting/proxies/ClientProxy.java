package hardcorequesting.proxies;

//import hardcorequesting.BlockHighlightRemover;
import hardcorequesting.client.interfaces.hud.GUIOverlay;
import hardcorequesting.client.sounds.SoundHandler;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestTicker;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;


public class ClientProxy extends CommonProxy {

    public SoundHandler soundHandler;

    @Override
    public void initSounds(String path) {
        //init all the sounds
    }

    @Override
    public void initRenderers() {
        //init the rendering stuff


        //MinecraftForge.EVENT_BUS.register(new GUIOverlay(Minecraft.getMinecraft()));
        //new BlockHighlightRemover();
    }

    @Override
    public void init() {
        Quest.serverTicker = new QuestTicker(false);
        Quest.clientTicker = new QuestTicker(true);
    }

    @Override
    public boolean isClient() {
        return true;
    }

    @Override
    public boolean isServer() {
        return false;
    }
}
