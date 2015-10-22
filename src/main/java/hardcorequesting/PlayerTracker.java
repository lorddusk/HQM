package hardcorequesting;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import hardcorequesting.network.PacketHandler;
import hardcorequesting.quests.QuestLine;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;

public class PlayerTracker {


    public PlayerTracker() {
        FMLCommonHandler.instance().bus().register(this);
    }


	public int getRemainingLives(ICommandSender sender) {
		return  QuestingData.getQuestingData((EntityPlayer) sender).getLives();
	}

		
    @SubscribeEvent
	public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        EntityPlayer player = event.player;
        if (!QuestingData.hasData(player)) {
            QuestingData.getQuestingData(player).getDeathStat().refreshSync();
            //TODO Load Player here.
        }

        QuestLine.sendServerSync(player);

		if(QuestingData.isHardcoreActive())
			sendLoginMessage(player);
		else
			player.addChatMessage(new ChatComponentText("This server doesn\'t have Hardcore Questing Mode enabled."));

        NBTTagCompound tags = player.getEntityData();
        if(tags.hasKey("HardcoreQuesting")) {
            if (tags.getCompoundTag("HardcoreQuesting").getBoolean("questBook")) {
                QuestingData.getQuestingData(player).receivedBook = true;
            }
            if (QuestingData.isQuestActive()) {
                tags.removeTag("HardcoreQuesting");
            }
        }



        QuestingData.spawnBook(player);

	}



    private void sendLoginMessage(EntityPlayer player) {
		player.addChatMessage(new ChatComponentText("This server currently has"
                + " Hardcore Questing Mode enabled! You currently have " + getRemainingLives(player) + " live(s) left."));
		
	}

    @SubscribeEvent
	public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        EntityPlayer player = event.player;
        if (!player.worldObj.isRemote) {
            PacketHandler.remove(player);
        }
        QuestingData.savePlayerData(player.getDisplayName());
    }



	

}
