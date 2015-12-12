package hardcorequesting;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class PlayerDeathEventListener {

	public PlayerDeathEventListener() {
		MinecraftForge.EVENT_BUS.register(this);
	}



	@SubscribeEvent
	public void onPlayerDeath(LivingDeathEvent event) {
		if (event.entityLiving instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP)event.entityLiving;
			QuestingData.getQuestingData(player).die(player);
            DeathType.onDeath(player, event.source);
		}
	}

}
