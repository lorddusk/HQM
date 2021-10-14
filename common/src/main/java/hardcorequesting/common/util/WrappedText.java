package hardcorequesting.common.util;

import net.minecraft.network.chat.FormattedText;

/**
 * A text wrapper that can optionally be translatable
 */
public class WrappedText {
    
    private final String text;
    private final boolean shouldTranslate;
    
    public static WrappedText create(String text) {
        return new WrappedText(text, false);
    }
    
    public static WrappedText createTranslated(String key) {
        return new WrappedText(key, true);
    }
    
    private WrappedText(String text, boolean shouldTranslate) {
        this.text = text;
        this.shouldTranslate = shouldTranslate;
    }
    
    public FormattedText getText() {
        if (shouldTranslate)
            return Translator.translatable(text);
        else
            return Translator.text(text);
    }
    
    public String getRawText() {
        return text;
    }
    
    public boolean shouldTranslate() {
        return shouldTranslate;
    }
}
