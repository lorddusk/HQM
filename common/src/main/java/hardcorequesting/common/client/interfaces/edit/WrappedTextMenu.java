package hardcorequesting.common.client.interfaces.edit;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.widget.AbstractCheckBox;
import hardcorequesting.common.client.interfaces.widget.LargeButton;
import hardcorequesting.common.client.interfaces.widget.MultilineTextBox;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import hardcorequesting.common.util.WrappedText;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class WrappedTextMenu extends AbstractTextMenu {
    
    private final Consumer<Optional<WrappedText>> resultConsumer;
    private final int limit;
    private final boolean isName;
    private boolean isTranslated;
    @Nullable
    private List<FormattedText> translatedLines;
    @Nullable
    private String translatedText;
    
    public static void display(GuiQuestBook gui, WrappedText text, boolean isName, Consumer<WrappedText> resultConsumer) {
        gui.setEditMenu(new WrappedTextMenu(gui, text, isName, -1, unnamedDefault(resultConsumer)));
    }
    
    public static void displayWithOptionalResult(GuiQuestBook gui, WrappedText text, boolean isName, Consumer<Optional<WrappedText>> resultConsumer) {
        gui.setEditMenu(new WrappedTextMenu(gui, text, isName, -1, resultConsumer));
    }
    
    public static void display(GuiQuestBook gui, WrappedText text, int limit, Consumer<WrappedText> resultConsumer) {
        gui.setEditMenu(new WrappedTextMenu(gui, text, true, limit, unnamedDefault(resultConsumer)));
    }
    
    /**
     * If no text was entered, default to the "Unnamed" text
     */
    private static Consumer<Optional<WrappedText>> unnamedDefault(Consumer<WrappedText> consumer) {
        return optional -> consumer.accept(optional.orElseGet(() -> WrappedText.create(I18n.get("hqm.textEditor.unnamed"))));
    }
    
    private WrappedTextMenu(GuiQuestBook gui, WrappedText text, boolean isName, int limit, Consumer<Optional<WrappedText>> resultConsumer) {
        super(gui, text.getRawText(), !isName);
    
        this.resultConsumer = resultConsumer;
        this.limit = limit;
        
        this.isName = isName;
        this.isTranslated = text.shouldTranslate();
        
        addClickable(new AbstractCheckBox(gui, Translator.translatable("hqm.textEditor.translationKey"), 180, 20) {
            @Override
            public boolean getValue() {
                return isTranslated;
            }
    
            @Override
            public void setValue(boolean val) {
                isTranslated = val;
                onTextChanged(WrappedTextMenu.this.textLogic.getText());
            }
        });
        addClickable(new LargeButton(gui, "hqm.textEditor.asRawText", 180, 40) {
            @Override
            public boolean isEnabled() {
                return translatedText != null;
            }
    
            @Override
            public boolean isVisible() {
                return isTranslated;
            }
    
            @Override
            public void onClick() {
                setRawTextFromTranslation();
            }
        });
        this.textLogic.setListener(this::onTextChanged);
    
        this.onTextChanged(this.textLogic.getText());
    }
    
    private void onTextChanged(String text) {
        if (this.isTranslated && !I18n.exists(text)) {
            this.textLogic.setTextColor(0xAA0000);
        } else {
            this.textLogic.setTextColor(MultilineTextBox.DEFAULT_TEXT_COLOR);
        }
        if (this.isTranslated && I18n.exists(text)) {
            this.translatedText = Objects.requireNonNull(I18n.get(text));
            this.translatedLines = gui.getFont().getSplitter().splitLines(this.translatedText, 140, Style.EMPTY);
        } else {
            this.translatedText = null;
            this.translatedLines = null;
        }
    }
    
    private void setRawTextFromTranslation() {
        this.isTranslated = false;
        this.textLogic.setTextAndCursor(this.translatedText);   //This will indirectly call onTextChanged(String)
    }
    
    @Override
    public void draw(PoseStack matrices, int mX, int mY) {
        super.draw(matrices, mX, mY);
        
        if (this.translatedLines != null) {
            this.gui.drawString(matrices, this.translatedLines, 20, 100, 1F, MultilineTextBox.DEFAULT_TEXT_COLOR);
        }
    }
    
    @Override
    public void save() {
        String text = textLogic.getText();
        if (limit >= 0) {
            while (gui.getStringWidth(text) > limit) {
                text = text.substring(0, text.length() - 1);
            }
        }
    
        if (text.isEmpty()) {
            resultConsumer.accept(Optional.empty());
        } else {
            resultConsumer.accept(Optional.of(this.isTranslated
                    ? WrappedText.createTranslated(text)
                    : WrappedText.create(text)));
        }
        
        if (isName) {
            SaveHelper.add(EditType.NAME_CHANGE);
        } else {
            SaveHelper.add(EditType.DESCRIPTION_CHANGE);
        }
    }
}
