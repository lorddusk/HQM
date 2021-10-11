package hardcorequesting.common.util;


import hardcorequesting.common.client.interfaces.GuiColor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;

public class Translator {
    
    public static MutableComponent quest(int quests) {
        return plural("hqm.quest", quests);
    }
    
    public static MutableComponent player(int players) {
        return plural("hqm.player", players);
    }
    
    public static MutableComponent lives(int lives) {
        return plural("hqm.life", lives);
    }
    
    public static MutableComponent plural(String id, int thing) {
        String key = thing != 1 ? id + ".plural" : id;
        return translatable(key, thing);
    }
    
    public static String rawString(FormattedText text) {
        return ChatFormatting.stripFormatting(text.getString());
    }
    
    public static FormattedText plain(String s) {
        return s != null ? FormattedText.of(s) : FormattedText.EMPTY;
    }
    
    public static MutableComponent text(String s) {
        return s != null ? new TextComponent(s) : new TextComponent("");
    }
    
    public static MutableComponent text(String s, ChatFormatting formatting) {
        return text(s).withStyle(Style.EMPTY.withColor(formatting));
    }
    
    public static MutableComponent text(String s, TextColor color) {
        return text(s).withStyle(Style.EMPTY.withColor(color));
    }
    
    public static MutableComponent text(String s, GuiColor color) {
        return text(s, TextColor.fromRgb(color.getHexColor() & 0xFFFFFF));
    }
    
    public static MutableComponent translatable(String id, Object... args) {
        return new TranslatableComponent(id, args);
    }
    
    public static MutableComponent translatable(String id, ChatFormatting formatting, Object... args) {
        return translatable(id, args).withStyle(formatting);
    }
    
    public static MutableComponent translatable(String id, TextColor color, Object... args) {
        return translatable(id, args).withStyle(Style.EMPTY.withColor(color));
    }
    
    public static MutableComponent translatable(String id, GuiColor color, Object... args) {
        return translatable(id, TextColor.fromRgb(color.getHexColor() & 0xFFFFFF), args);
    }
}
