package hardcorequesting.proxies;

import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import hardcorequesting.blocks.ModBlocks;
import hardcorequesting.client.sounds.SoundHandler;
import hardcorequesting.client.sounds.Sounds;
import hardcorequesting.items.ModItems;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestTicker;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;


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
        //MinecraftForge.EVENT_BUS.register(new BlockHighlightRemover());
        ModBlocks.initRender();
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

    @Override
    public EntityPlayer getPlayer(MessageContext ctx) {
        return ctx.side == Side.CLIENT ? Minecraft.getMinecraft().thePlayer : super.getPlayer(ctx);
    }
}
