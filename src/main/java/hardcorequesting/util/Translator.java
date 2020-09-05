package hardcorequesting.util;


import com.google.common.collect.Lists;
import hardcorequesting.client.interfaces.GuiColor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.*;

import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Translator {
    private static Pattern pluralPattern = Pattern.compile("\\[\\[(.*)\\|\\|(.*)]]");
    
    @SuppressWarnings("Convert2MethodRef")
    private static final BiFunction<String, Object[], String> storageTranslator = Executor.call(() -> () -> (s, args) -> I18n.get(s, args), () -> () -> (s, args) -> {
        String s1 = Language.getInstance().getOrDefault(s);
        if (s1 == null) s1 = s;
        
        try {
            return String.format(s1, args);
        } catch (IllegalFormatException var5) {
            return "Format error: " + s1;
        }
    });
    
    public static String get(String id, Object... args) {
        return storageTranslator.apply(id, args).replace("\\n", "\n");
    }
    
    public static MutableComponent pluralTranslated(boolean plural, String id, Object... args) {
        return pluralFormat(text(get(id, args)), null, plural);
    }
    
    public static MutableComponent pluralTranslated(boolean plural, String id, GuiColor color, Object... args) {
        return pluralFormat(text(get(id, args)), color, plural);
    }
    
    private static MutableComponent pluralFormat(Component text, GuiColor color, boolean plural) {
        if (text == null) return new TextComponent("");
        try {
            TextCollector collector = new TextCollector();
            text.visit(asString -> {
                Matcher matcher = pluralPattern.matcher(asString);
                while (matcher.find()) {
                    asString = matcher.replaceFirst(matcher.group(plural ? 2 : 1));
                    matcher = pluralPattern.matcher(asString);
                }
                collector.add(color == null ? Translator.text(asString) : Translator.text(asString, color));
                return Optional.empty();
            });
            return collector.getCombined();
        } catch (IllegalFormatException e) {
            return TextCollector.concat(Translator.text("Format Exception: "), text);
        }
    }
    
    private static class TextCollector {
        private final List<Component> texts = Lists.newArrayList();
        
        public void add(Component text) {
            this.texts.add(text);
        }
        
        public MutableComponent getRawCombined() {
            if (this.texts.isEmpty()) {
                return null;
            } else {
                return this.texts.size() == 1 ? this.texts.get(0).copy() : concat(this.texts);
            }
        }
        
        public MutableComponent getCombined() {
            MutableComponent text = this.getRawCombined();
            return text != null ? text : new TextComponent("");
        }
        
        private static BaseComponent concat(final Component... texts) {
            return concat(Arrays.asList(texts));
        }
        
        private static BaseComponent concat(final List<Component> texts) {
            return new BaseComponent() {
                @Override
                public BaseComponent plainCopy() {
                    return concat(texts);
                }
                
                @Override
                public <T> Optional<T> visit(FormattedText.ContentConsumer<T> visitor) {
                    Iterator<Component> var2 = texts.iterator();
                    
                    Optional<T> optional;
                    do {
                        if (!var2.hasNext()) {
                            return Optional.empty();
                        }
                        
                        optional = var2.next().visit(visitor);
                    } while (!optional.isPresent());
                    
                    return optional;
                }
                
                @Environment(EnvType.CLIENT)
                @Override
                public <T> Optional<T> visit(StyledContentConsumer<T> styledVisitor, Style style) {
                    return Optional.empty();
                }
            };
        }
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
