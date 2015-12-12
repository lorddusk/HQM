package hardcorequesting.commands;

import hardcorequesting.ModInformation;
import hardcorequesting.Translator;
import net.minecraft.command.ICommandSender;

public class CommandVersion extends CommandBase {
    public CommandVersion() {
        super("version");
        permissionLevel = 0;
    }

    @Override
    public void handleCommand(ICommandSender sender, String[] arguments) {
        sendChat(sender, "\u00A7a" + Translator.translate("hqm.message.version", ModInformation.VERSION));
    }
}
