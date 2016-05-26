package hardcorequesting.proxies;

import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestTicker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CommonProxy {

    public void initSounds(String path) {

    }

    public void initRenderers() {

    }

    public void init() {
        Quest.serverTicker = new QuestTicker(false);
    }

    public boolean isClient() {
        return false;
    }

    public boolean isServer() {
        return true;
    }

    public EntityPlayer getPlayer(MessageContext ctx)
    {
        return ctx.getServerHandler().playerEntity;
    }
}
