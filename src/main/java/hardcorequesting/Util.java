package hardcorequesting;

import net.minecraft.util.ChatComponentStyle;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

/**
 * Created by Tim on 6/23/2014.
 */
public class Util {

    public static ChatComponentStyle getChatComponent(Object message, EnumChatFormatting formatting) {
        ChatComponentText componentText = new ChatComponentText(message.toString());
        componentText.getChatStyle().setColor(formatting);
        return componentText;
    }
}
