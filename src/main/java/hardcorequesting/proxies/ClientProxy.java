package hardcorequesting.proxies;

import hardcorequesting.blocks.ModBlocks;
import hardcorequesting.client.sounds.SoundHandler;
import hardcorequesting.client.sounds.Sounds;
import hardcorequesting.items.ModItems;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestTicker;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;


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
        ModItems.initRender();
        ModBlocks.initRender();
        Sounds.initSounds();
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
        return ctx.side == Side.CLIENT ? Minecraft.getMinecraft().player : super.getPlayer(ctx);
    }
}
