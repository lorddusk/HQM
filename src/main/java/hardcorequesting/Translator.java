package hardcorequesting;

import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StatCollector;

import java.util.IllegalFormatException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Translator {
    private static Pattern pluralPattern = Pattern.compile("\\[\\[(.*)\\|\\|(.*)\\]\\]");

    public static String translate(String id) {
        return StatCollector.translateToLocal(id).replace("\\n", "\n");
    }

    public static String translate(String id, Object... args) {
        return translate(false, id, args);
    }

    public static String translate(boolean plural, String id, Object... args) {
        return format(translate(id), plural, args);
    }

    public static IChatComponent translateToIChatComponent(String id, Object... args) {
        return translateToIChatComponent(EnumChatFormatting.WHITE, id, args);
    }

    public static IChatComponent translateToIChatComponent(EnumChatFormatting colour, String id, Object... args) {
        return translateToIChatComponent(colour, false, id, args);
    }

    public static IChatComponent translateToIChatComponent(EnumChatFormatting colour, boolean plural, String id, Object... args) {
        IChatComponent iChatComponent = new ChatComponentText(Translator.translate(plural, id, args));
        iChatComponent.getChatStyle().setColor(colour);
        return iChatComponent;
    }

    public static String format(String s, boolean plural, Object... args) {
        if (s == null) return s;
        try {
            Matcher matcher = pluralPattern.matcher(s);
            while (matcher.find()) {
                s = matcher.replaceFirst(matcher.group(plural ? 2 : 1));
                matcher = pluralPattern.matcher(s);
            }
            return String.format(s, args);
        } catch (IllegalFormatException e) {
            return "Format Exception: " + s;
        }
    }
}
