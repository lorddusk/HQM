package hardcorequesting.util;


import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;

import java.util.IllegalFormatException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Translator {
    private static Pattern pluralPattern = Pattern.compile("\\[\\[(.*)\\|\\|(.*)\\]\\]");

    public static String translate(String id) {
        return I18n.translateToLocal(id).replace("\\n", "\n");
    }

    public static String translate(String id, Object... args) {
        return translate(false, id, args);
    }

    public static String translate(boolean plural, String id, Object... args) {
        return format(translate(id), plural, args);
    }

    public static ITextComponent translateToIChatComponent(String id, Object... args) {
        return translateToIChatComponent(TextFormatting.WHITE, id, args);
    }

    public static ITextComponent translateToIChatComponent(TextFormatting colour, String id, Object... args) {
        return translateToIChatComponent(colour, false, id, args);
    }

    public static ITextComponent translateToIChatComponent(TextFormatting colour, boolean plural, String id, Object... args) {
        ITextComponent iChatComponent = new TextComponentString(Translator.translate(plural, id, args));
        iChatComponent.getStyle().setColor(colour);
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
