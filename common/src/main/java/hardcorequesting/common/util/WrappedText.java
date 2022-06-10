package hardcorequesting.common.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.GsonHelper;

import java.util.Objects;

/**
 * A text wrapper that can optionally be translatable.
 * @author kirderf1
 */
public final class WrappedText {
    private static final String TEXT = "text", TRANSLATED = "isTranslationKey";
    
    
    private final String text;
    private final boolean shouldTranslate;
    
    public static WrappedText create(String text) {
        return new WrappedText(text, false);
    }
    
    public static WrappedText createTranslated(String key) {
        return new WrappedText(key, true);
    }
    
    public static WrappedText fromJson(JsonElement element, String fallback, boolean isDefaultTranslated) {
        if (element == null)
            return new WrappedText(fallback, isDefaultTranslated);
        else return fromJson(element, isDefaultTranslated);
    }
    
    public static WrappedText fromJson(JsonElement element, boolean isDefaultTranslated) {
        if (GsonHelper.isStringValue(element)) {    // Format used before migration to WrappedText
            return new WrappedText(element.getAsString(), isDefaultTranslated);
        } else {
            JsonObject json = GsonHelper.convertToJsonObject(element, "wrapped text");
            return new WrappedText(GsonHelper.getAsString(json, TEXT), GsonHelper.getAsBoolean(json, TRANSLATED));
        }
    }
    
    public JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty(TEXT, text);
        json.addProperty(TRANSLATED, shouldTranslate);
        return json;
    }
    
    private WrappedText(String text, boolean shouldTranslate) {
        this.text = Objects.requireNonNull(text);
        this.shouldTranslate = shouldTranslate;
    }
    
    public MutableComponent getText() {
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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WrappedText that = (WrappedText) o;
        return shouldTranslate == that.shouldTranslate && text.equals(that.text);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(text, shouldTranslate);
    }
}
