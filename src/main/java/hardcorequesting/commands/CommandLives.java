package hardcorequesting.commands;

import cpw.mods.fml.common.FMLCommonHandler;
import hardcorequesting.Lang;
import hardcorequesting.QuestingData;
import hardcorequesting.config.ModConfig;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.List;

public class CommandLives extends CommandBase
{
    private static String ADD = "add";
    private static String REMOVE = "remove";
    
    public CommandLives()
    {
        super("lives", ADD, REMOVE);
        permissionLevel = 0;
    }

    @Override
    public boolean isVisible(ICommandSender sender)
    {
        return true;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args)
    {
        if (isPlayerOp(sender))
            return super.addTabCompletionOptions(sender, args);
        else
            return null;
    }

    @Override
    public void handleCommand(ICommandSender sender, String[] arguments)
    {
        if (!QuestingData.isHardcoreActive()) {
            sendChat(sender, "Hardcore Mode isn't enabled yet. use '/hqm hardcore' to enable it.");
            return;
        }
        int amount;

        if (arguments.length == 0) {
            if (sender instanceof EntityPlayer) {
                currentLives((EntityPlayer) sender);
            }
        }
        if (!isPlayerOp(sender))
        {
            throw new CommandException(Lang.NO_PERMISSION);
        }
        if (arguments.length == 1) {
            if (arguments[0].matches(REMOVE)) {
                if (sender instanceof EntityPlayer) {
                    removeLive((EntityPlayer) sender);
                }
            } else if (arguments[0].matches(ADD)) {
                if (sender instanceof EntityPlayer) {
                    addLive((EntityPlayer) sender);
                }
            } else
            {
                getPlayerLives(sender, arguments[0]);
            }
        }
        else if (arguments.length == 2) {
            //remove amount own.
            if (arguments[0].matches(REMOVE)) {
                try {
                    amount = Integer.parseInt(arguments[1]);
                    if (sender instanceof EntityPlayer) {
                        removeLives((EntityPlayer) sender, amount);
                    }
                } catch (Exception e) {
                    throw new WrongUsageException("Please use only positive numbers.");
                }
            }
            // remove playername
            else if (arguments[1].matches(REMOVE)) {
                try {
                    amount = 1;
                    removeLivesFrom(sender, arguments[1], amount);
                } catch (Exception e) {
                    throw new WrongUsageException("Please use only positive numbers.");
                }
            }
            //add amount own
            else if (arguments[0].matches(ADD)) {
                try {
                    amount = Integer.parseInt(arguments[1]);
                    if (sender instanceof EntityPlayer) {
                        addLives((EntityPlayer) sender, amount);
                    }
                } catch (Exception e) {
                    throw new WrongUsageException("Please use only positive numbers.");
                }
            }
            //add amount playername
            else if (arguments[0].matches(ADD)) {
                try {
                    amount = 1;
                    addLivesTo(sender, arguments[1], amount);
                } catch (Exception e) {
                    throw new WrongUsageException("Please use only positive numbers.");
                }
            }
        }
        else if (arguments.length == 3) {
            // /hqm lives remove playername amount
            if (arguments[0].matches(REMOVE)) {
                try {
                    amount = Integer.parseInt(arguments[2]);
                    removeLivesFrom(sender, arguments[1], amount);
                } catch (Exception e) {
                    throw new WrongUsageException("Please use only positive numbers and / or a correct Playername.");
                }
            }
            // /hqm lives add playername amount
            else if (arguments[0].matches(ADD)) {
                try {
                    amount = Integer.parseInt(arguments[2]);
                    addLivesTo(sender, arguments[1], amount);
                } catch (Exception e) {
                    throw new WrongUsageException("Please use only positive numbers and / or a correct Playername.");
                }
            }
        }
    }

    @Override
    public int[] getSyntaxOptions(ICommandSender sender)
    {
        return isPlayerOp(sender)? new int[]{0, 1, 2, 3} : super.getSyntaxOptions(sender);
    }

    private void removeLive(EntityPlayer player) {
        if (QuestingData.getQuestingData(player).getLives() <= QuestingData.getQuestingData(player).getLivesToStayAlive()) {
            sendChat(player, "You currently have " + QuestingData.getQuestingData(player).getLives() + " live remaining, you can't remove any lives.");
        } else {
            QuestingData.getQuestingData(player).removeLives(player, 1);
            sendChat(player, "You have removed 1 life from your lifepool.");
            currentLives(player);
        }
    }

    private void removeLives(EntityPlayer player, int amount) {
        if (QuestingData.getQuestingData(player).getLives() - amount < QuestingData.getQuestingData(player).getLivesToStayAlive()) {
            sendChat(player, "You currently have " + QuestingData.getQuestingData(player).getLives() + " live remaining, you can't remove any lives.");
        } else {
            QuestingData.getQuestingData(player).removeLives(player, amount);
            sendChat(player, "You have removed " + amount + " live(s) from your lifepool");
            currentLives(player);
        }
    }

    private void removeLivesFrom(ICommandSender sender, String playerName, int amount) {
        EntityPlayer player = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(playerName);

        QuestingData.getQuestingData(player).removeLives((EntityPlayerMP) sender, amount);

        sendChat(sender, "You have removed " + amount + " live(s) from " + playerName + "");
        sendChat(player, "You had " + amount + " live(s) removed by " + sender.getCommandSenderName() + "");
        currentLives(player);
    }

    private void addLive(EntityPlayer player) {
        int Max = ModConfig.MAXLIVES;

        if (QuestingData.getQuestingData(player).getRawLives() + 1 <= Max) {
            QuestingData.getQuestingData(player).addLives(player, 1);
            sendChat(player, "You have added 1 life to your lifepool.");
            currentLives(player);
        } else {
            QuestingData.getQuestingData(player).addLives(player, 1);
            sendChat(player, "You can't have more than " + Max + " lives.");
            currentLives(player);
        }
    }

    private void addLives(EntityPlayer player, int amount) {
        int Max = ModConfig.MAXLIVES;

        if (QuestingData.getQuestingData(player).getRawLives() + amount <= Max) {
            QuestingData.getQuestingData(player).addLives(player, amount);
            sendChat(player, "You have added " + amount + " live(s) to your lifepool");
            currentLives(player);
        } else {
            QuestingData.getQuestingData(player).addLives(player, amount);
            sendChat(player, "You can't have more than " + Max + " lives.");
            currentLives(player);
        }
    }

    private void addLivesTo(ICommandSender sender, String playerName, int amount) {
        EntityPlayer player = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(playerName);

        int Max = ModConfig.MAXLIVES;

        if (QuestingData.getQuestingData(player).getRawLives() + amount <= Max) {
            QuestingData.getQuestingData(player).addLives(player, amount);
            sendChat(sender, "You have added " + amount + " live(s) to " + playerName + "");
            sendChat(player, "You had " + amount + " live(s) added by " + sender.getCommandSenderName() + "");
            currentLives(player);
        } else {
            QuestingData.getQuestingData(player).addLives(player, amount);
            sendChat(sender, "You can't give " + playerName + " more than " + Max + " lives.");
            sendChat(sender, "Setting " + playerName + " to " + Max + " lives instead.");
            sendChat(player, "You have got your lives set to " + Max + " by " + sender.getCommandSenderName() + "");
            currentLives(player);
        }
    }

    private void getPlayerLives(ICommandSender sender, String playerName) {
        EntityPlayer player = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(playerName);
        if (player != null)
        {
            int lives = QuestingData.getQuestingData(player).getLives();
            sendChat(sender, "" + playerName + " has " + lives + " live(s) remaining");
        } else
        {
            throw new CommandException("Not a player");
        }
    }


}
