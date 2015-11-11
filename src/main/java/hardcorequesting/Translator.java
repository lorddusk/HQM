package hardcorequesting;

import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StatCollector;

public class Translator
{
    public static String translate(String id)
    {
        return StatCollector.translateToLocal(id);
    }

    public static String translate(String id, Object... args)
    {
        return StatCollector.translateToLocalFormatted(id, args);
    }

    public static IChatComponent translateToIChatComponent(String id, Object... args)
    {
        return translateToIChatComponent(EnumChatFormatting.WHITE, id, args);
    }

    public static IChatComponent translateToIChatComponent(EnumChatFormatting colour, String id, Object... args)
    {
        IChatComponent iChatComponent = new ChatComponentText(Translator.translate(id, args));
        iChatComponent.getChatStyle().setColor(colour);
        return iChatComponent;
    }
}
