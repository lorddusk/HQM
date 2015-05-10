package hardcorequesting.commands;

import com.mojang.authlib.GameProfile;
import cpw.mods.fml.common.FMLCommonHandler;
import hardcorequesting.ModInformation;
import hardcorequesting.QuestingData;
import hardcorequesting.config.ModConfig;
import hardcorequesting.items.ItemQuestBook;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StringUtils;

public class CommandHandler extends CommandBase{

	
	public int lives;
	public int amount;
	public String player;
	
	@Override
	public String getCommandName() 
	{
		return "hqm";
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender) 
	{
		return "/" + this.getCommandName() + " help";
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender par1ICommandSender)
	{
		return true;
	}


    @Override
	public void processCommand(ICommandSender sender, String[] arguments) 
	{


		if(arguments.length <= 0)
			throw new WrongUsageException("Type '" + this.getCommandUsage(sender) + "' for help.");


        if(arguments[0].matches("edit") && sender instanceof EntityPlayer && isOwnerOrOp(sender)) {
            EntityPlayer player = (EntityPlayer)sender;

            String name = arguments.length == 2 ? arguments[1] : QuestingData.getUserName(player);
            if (QuestingData.hasData(name)) {
                player.inventory.addItemStackToInventory(ItemQuestBook.getOPBook(name));
            }else{
                sendChat(player, "That player does no exist.");
            }
            return;
        }

		if(arguments[0].matches("lives"))
		{
            if (!QuestingData.isHardcoreActive()) {
                sendChat(sender, "Hardcore Mode isn't enabled yet. use '/hqm hardcore' to enable it.");
                return;
            }

			if (arguments.length == 1)
			{
				if (sender instanceof EntityPlayer) {
					currentLives((EntityPlayer)sender);
				}
				return;
			}
			if(arguments.length == 2)
			{
				if(arguments[1].matches("remove") && isPlayerOp(sender))
				{	
					if (sender instanceof EntityPlayer) {
						removeLive((EntityPlayer)sender);
					}					
					return;
				}
				else if(arguments[1].matches("add") && isPlayerOp(sender))
				{	
					if (sender instanceof EntityPlayer) {
						addLive((EntityPlayer)sender);
					}						
					return;
				}
			}
			if(arguments.length == 3)
			{
				//remove amount own.
				if(arguments[1].matches("remove") && isPlayerOp(sender))
				{
					try
					{
						amount = Integer.parseInt(arguments[2]);
						if (sender instanceof EntityPlayer) {
							removeLives((EntityPlayer)sender, amount);
						}							
					}
					catch(Exception e)
					{
						throw new WrongUsageException("Please use only positive numbers.");
					}
					return;
				}
				// remove playername
				else if(arguments[1].matches("remove") && isPlayerOp(sender))
				{
					try
					{
						amount = 1;
						removeLivesFrom(sender, arguments[2], amount);
					}
					catch(Exception e)
					{
						throw new WrongUsageException("Please use only positive numbers.");
					}
					return;
				}
				//add amount own
				else if(arguments[1].matches("add")  && isPlayerOp(sender))
				{
					try
					{
						amount = Integer.parseInt(arguments[2]);
						if (sender instanceof EntityPlayer) {
							addLives((EntityPlayer)sender, amount);
						}						
					}
					catch(Exception e)
					{
						throw new WrongUsageException("Please use only positive numbers.");
					}
					return;
				}
				//add amount playername
				else if(arguments[1].matches("add") && isPlayerOp(sender))
				{
					try
					{
						amount = 1;
						addLivesTo(sender, arguments[2], amount);
					}
					catch(Exception e)
					{
						throw new WrongUsageException("Please use only positive numbers.");
					}
					return;
				}
				else if(arguments[1].matches("playercheck") && isPlayerOp(sender))
				{
					getPlayerLives(sender, arguments[2]);
					return;
				}
			}
			if(arguments.length == 4)
			{
				// /hqm lives remove playername amount
				if(arguments[1].matches("remove") && isPlayerOp(sender))
				{
					try
					{
						amount = Integer.parseInt(arguments[3]);
						removeLivesFrom(sender, arguments[2], amount);
					}
					catch(Exception e)
					{
						throw new WrongUsageException("Please use only positive numbers and / or a correct Playername.");
					}
					return;
				}
				// /hqm lives add playername amount
				else if(arguments[1].matches("add") && isPlayerOp(sender))
				{
					try
					{
						amount = Integer.parseInt(arguments[3]);
						addLivesTo(sender, arguments[2], amount);
					}
					catch(Exception e)
					{
						throw new WrongUsageException("Please use only positive numbers and / or a correct Playername.");
					}
					return;
					
				}
			}
		}
// else if(arguments[0].matches("quest") && isPlayerOp(sender)) {
//			//Just for testing
//
//			if (arguments.length == 3 && arguments[1].matches("complete")) {
//				try
//				{
//					int id = Integer.parseInt(arguments[2]);
//					if (sender instanceof EntityPlayer) {
//						EntityPlayer player = (EntityPlayer)sender;
//						QuestingData.getQuestingData(player).getQuestData(id).completed = true;
//					}
//				}
//				catch(Exception e)
//				{
//					throw new WrongUsageException("Please add a valid quest id");
//				}
//				return;
//			}
//		}

		if(arguments[0].matches("help") && isPlayerOp(sender))
		{
			sendChat(sender, "Format: '" + this.getCommandName() + " <command> <arguments>'");
			sendChat(sender, "Available commands:");
			sendChat(sender, "- version : Version information.");
			sendChat(sender, "- hardcore : Enable Hardcore mode.");
            sendChat(sender, "- quest : Enable Questing mode.");
            sendChat(sender, "- enable : Enable both Questing and Hardcore mode.");
            sendChat(sender, "- edit [<playername>] : Give yourself a book in edit mode, playername optional.");
			sendChat(sender, "- lives : Check your current lives remaining.");
			sendChat(sender, "- lives playercheck <playername>: Check current lives remaining of playername.");
			sendChat(sender, "- lives add : 1 live added to your lifepool to <playername>.");
			sendChat(sender, "- lives add <amount>: amount of lives added to your lifepool.");
			sendChat(sender, "- lives add <playername> <amount>: amount of lives added to lifepool of <playername>.");
			sendChat(sender, "- lives remove : 1 live removed from your lifepool.");
			sendChat(sender, "- lives remove <amount>: amount of lives remove from your lifepool.");
			sendChat(sender, "- lives remove <playername> <amount>: amount of lives removed from lifepool of <playername>.");
			return;
		}
		else if (arguments[0].matches("help"))
		{
			sendChat(sender, "Format: '" + this.getCommandName() + " <command> <arguments>'");
			sendChat(sender, "Available commands:");
			sendChat(sender, "- version : Version information.");
			sendChat(sender, "- lives : Check your current lives remaining.");
            sendChat(sender, "- hardcore : Enable Hardcore mode.");
            sendChat(sender, "- quest : Enable Questing mode.");
            sendChat(sender, "- enable : Enable both Questing and Hardcore mode.");
			return;
		}

		if(arguments[0].matches("version"))
		{
			commandVersion(sender, arguments);
			return;
		}

		if(arguments[0].matches("hardcore") && isOwnerOrOp(sender))
		{
            QuestingData.disableHardcore(sender);
			if (MinecraftServer.getServer().getEntityWorld().getWorldInfo().isHardcoreModeEnabled()) {
				sendChat(sender, "Vanilla hardcore mode is already enabled. Can't enable Hardcore Mode.");
			}	
			else if(!QuestingData.isHardcoreActive())
			{
				sendChat(sender, "Hardcore Mode has been activated. Enjoy!");
				QuestingData.activateHardcore();
				if (sender instanceof EntityPlayer) {
					currentLives((EntityPlayer)sender);
				}
			}
			else
			{
				sendChat(sender, "Hardcore Mode is already activated.");
				if (sender instanceof EntityPlayer) {
					currentLives((EntityPlayer)sender);
				}
			}
			return;
		}

        if(arguments[0].matches("quest") && isOwnerOrOp(sender))
        {
            if(!QuestingData.isQuestActive())
            {
                sendChat(sender, "Questing mode has been activated. Enjoy!");
                QuestingData.activateQuest();
            }
            else
            {
                sendChat(sender, "Questing mode is already activated.");
            }
            return;
        }

//        if(arguments[0].matches("debug") && isOwnerOrOp(sender))
//        {
//            if(!QuestingData.isDebugActive())
//            {
//                sendChat(sender, "Debug Mode activated");
//                QuestingData.activateDebug();
//            }
//            else if(QuestingData.isDebugActive())
//            {
//                sendChat(sender, "Debug Mode deactivated");
//                QuestingData.deactivateDebug();
//            }
//        }

        if(arguments[0].matches("enable") && isOwnerOrOp(sender))
        {
            System.out.println("ENABLE");
            QuestingData.disableHardcore(sender);

            if (MinecraftServer.getServer().getEntityWorld().getWorldInfo().isHardcoreModeEnabled() && !QuestingData.isQuestActive()) {
                sendChat(sender, "Vanilla hardcore mode is already enabled. Can't enable Hardcore Mode.");
                sendChat(sender, "Questing mode has been activated. Enjoy!");
                QuestingData.activateQuest();
            }
            else if(MinecraftServer.getServer().getEntityWorld().getWorldInfo().isHardcoreModeEnabled() && QuestingData.isQuestActive()) {
                sendChat(sender, "Vanilla hardcore mode is already enabled. Can't enable Hardcore Mode.");
                sendChat(sender, "Questing mode is already activated.");
            }
            else if(!QuestingData.isHardcoreActive() && !QuestingData.isQuestActive())
            {
                sendChat(sender, "Hardcore Mode has been activated. Enjoy!");
                sendChat(sender, "Questing mode has been activated. Enjoy!");
                QuestingData.activateHardcore();
                QuestingData.activateQuest();
                if (sender instanceof EntityPlayer) {
                    currentLives((EntityPlayer)sender);
                }
            }
            else if(!QuestingData.isHardcoreActive() && QuestingData.isQuestActive())
            {
                sendChat(sender, "Hardcore Mode has been activated. Enjoy!");
                QuestingData.activateHardcore();
                if (sender instanceof EntityPlayer) {
                    currentLives((EntityPlayer)sender);
                }
                sendChat(sender, "Questing mode is already activated.");
            }
            else if(QuestingData.isHardcoreActive() && !QuestingData.isQuestActive())
            {
                sendChat(sender, "Questing mode has been activated. Enjoy!");
                QuestingData.activateQuest();
                sendChat(sender, "Hardcore Mode is already activated.");
                if (sender instanceof EntityPlayer) {
                    currentLives((EntityPlayer)sender);
                }
            }
            else
            {
                sendChat(sender, "Hardcore Mode is already activated.");
                if (sender instanceof EntityPlayer) {
                    currentLives((EntityPlayer)sender);
                }
                sendChat(sender, "Questing mode is already activated.");
            }
            return;
        }

        throw  new CommandException("commands.generic.permission");
	}

	private void removeLive(EntityPlayer player) 
	{
		if(QuestingData.getQuestingData(player).getLives() <= QuestingData.getQuestingData(player).getLivesToStayAlive())
		{
			sendChat(player, "You currently have " + QuestingData.getQuestingData(player).getLives() + " live remaining, you can't remove any lives.");
		}
		else
		{
			QuestingData.getQuestingData(player).removeLives((EntityPlayerMP)player, 1);
			sendChat(player, "You have removed 1 life from your lifepool.");
			currentLives(player);
		}
	}
	
	private void removeLives(EntityPlayer player, int amount)
	{
		if(QuestingData.getQuestingData(player).getLives() - amount < QuestingData.getQuestingData(player).getLivesToStayAlive())
		{
			sendChat(player, "You currently have " + QuestingData.getQuestingData(player).getLives() + " live remaining, you can't remove any lives.");
		}
		else
		{
			QuestingData.getQuestingData(player).removeLives((EntityPlayerMP)player, amount);
			sendChat(player, "You have removed "+amount+" live(s) from your lifepool");
			currentLives(player);
		}
	}
	
	private void removeLivesFrom(ICommandSender sender, String playerName, int amount)
	{
		String playername = playerName;
		EntityPlayer player = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(playername);
		
		QuestingData.getQuestingData(player).removeLives((EntityPlayerMP)sender, amount);
		
		sendChat(sender, "You have removed " +amount+" live(s) from " + playerName + "");
		sendChat(player, "You had "+amount+" live(s) removed by "+sender.getCommandSenderName()+"");
		currentLives(player);
	}
	
	private void addLive(EntityPlayer player)
	{
		int Max = ModConfig.MAXLIVES;
		
		if(QuestingData.getQuestingData(player).getRawLives() + 1 <= Max)
		{
			QuestingData.getQuestingData(player).addLives(player, 1);
			sendChat(player, "You have added 1 life to your lifepool.");
			currentLives(player);
		}
		else
		{
			QuestingData.getQuestingData(player).addLives(player, 1);
			sendChat(player, "You can't have more than "+Max+ " lives.");
			currentLives(player);
		}
	}

    private void addLives(EntityPlayer player, int amount)
	{
		int Max = ModConfig.MAXLIVES;
		
		if(QuestingData.getQuestingData(player).getRawLives()+ amount <= Max)
		{
			QuestingData.getQuestingData(player).addLives(player, amount);
			sendChat(player, "You have added "+amount+" live(s) to your lifepool");
			currentLives(player);
		}
		else
		{
			QuestingData.getQuestingData(player).addLives(player, amount);
			sendChat(player, "You can't have more than "+Max+ " lives.");
			currentLives(player);
		}
	}
	
	private void addLivesTo(ICommandSender sender, String playerName, int amount)
	{
		String playername = playerName;
		EntityPlayer player = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(playername);
		
		int Max = ModConfig.MAXLIVES;
		
		if(QuestingData.getQuestingData(player).getRawLives()+ amount <= Max)
		{
			QuestingData.getQuestingData(player).addLives(player, amount);
			sendChat(sender, "You have added " +amount+" live(s) to " + playerName + "");
			sendChat(player, "You had "+amount+" live(s) added by "+sender.getCommandSenderName()+"");
			currentLives(player);
		}
		else
		{
			QuestingData.getQuestingData(player).addLives(player, amount);
			sendChat(sender, "You can't give " +playerName +" more than " + Max + " lives.");
			sendChat(sender, "Setting " +playerName +" to " + Max + " lives instead.");
			sendChat(player, "You have got your lives set to "+Max+" by "+sender.getCommandSenderName()+"");
			currentLives(player);
		}
	}
	
	private void getPlayerLives(ICommandSender sender, String playerName)
	{
		String playername = playerName;
		EntityPlayer player = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(playername);
		int lives = QuestingData.getQuestingData(player).getLives();
		
		sendChat(sender, ""+playerName+" has "+lives+ " live(s) remaining");
	}
	
	

	private void sendChat(ICommandSender sender, String string) {
		sender.addChatMessage(new ChatComponentText(string));
	}

	public boolean isPlayerOp(ICommandSender sender)
	{	
		if(sender instanceof EntityPlayer)
		{
			return MinecraftServer.getServer().getConfigurationManager().func_152596_g(getCommandSenderAsPlayer(sender).getGameProfile());
		}
		else
			return true;
	}
	
	public static boolean isOwnerOrOp(ICommandSender sender)
	{
		if(sender instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) sender;
			GameProfile username =  player.getGameProfile();
			return isCommandsAllowedOrOwner(username);
		}
		else
			return true;
	}

	
	public static boolean isCommandsAllowedOrOwner(GameProfile username)
    {
        return MinecraftServer.getServer().getConfigurationManager().func_152596_g(username) || MinecraftServer.getServer().isSinglePlayer() && MinecraftServer.getServer().getServerOwner().equals(username);
    }

	private void commandVersion(ICommandSender sender, String[] arguments)
	{
		String colour = "\u00A7a";

		sender.addChatMessage(new ChatComponentText(String.format(colour + "Hardcore Questing Mode - Version : %s", ModInformation.VERSION)));
	}
	
	private void currentLives(EntityPlayer player)
	{
		sendChat(player, "You currently have "+ QuestingData.getQuestingData(player).getLives() +" live(s) left.");
	}

	@Override
	public int compareTo(Object obj) {
		 {
		        if (obj instanceof ICommand)
		        {
		            return this.compareTo((ICommand) obj);
		        }
		        else
		        {
		            return 0;
		        }
		 }
}

	

}
