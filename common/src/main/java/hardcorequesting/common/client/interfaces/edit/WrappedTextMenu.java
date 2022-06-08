package hardcorequesting.common.client.interfaces.edit;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.widget.AbstractCheckBox;
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
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class WrappedTextMenu extends AbstractTextMenu {
    
    private final Consumer<WrappedText> resultConsumer;
    private final int limit;
    private final boolean isName;
    private boolean isTranslated;
    @Nullable
    private List<FormattedText> translatedLines;
    
    public static void display(GuiQuestBook gui, WrappedText text, boolean isName, Consumer<WrappedText> resultConsumer) {
        gui.setEditMenu(new WrappedTextMenu(gui, text, isName, -1, resultConsumer));
    }
    
    public static void display(GuiQuestBook gui, WrappedText text, int limit, Consumer<WrappedText> resultConsumer) {
        gui.setEditMenu(new WrappedTextMenu(gui, text, true, limit, resultConsumer));
    }
    
    private WrappedTextMenu(GuiQuestBook gui, WrappedText text, boolean isName, int limit, Consumer<WrappedText> resultConsumer) {
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
                updateTextColor(WrappedTextMenu.this.textLogic.getText());
            }
        });
        this.textLogic.setListener(this::updateTextColor);
    
        this.updateTextColor(this.textLogic.getText());
    }
    
    private void updateTextColor(String text) {
        if (this.isTranslated && !I18n.exists(text)) {
            this.textLogic.setTextColor(0xAA0000);
        } else {
            this.textLogic.setTextColor(MultilineTextBox.DEFAULT_TEXT_COLOR);
        }
        if (this.isTranslated && I18n.exists(text)) {
            this.translatedLines = gui.getFont().getSplitter().splitLines(I18n.get(text), 140, Style.EMPTY);
        } else {
            this.translatedLines = null;
        }
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
        if (text == null || text.isEmpty()) {
            text = I18n.get("hqm.textEditor.unnamed");
        }
        
        if (limit >= 0) {
            while (gui.getStringWidth(text) > limit) {
                text = text.substring(0, text.length() - 1);
            }
        }
    
        resultConsumer.accept(this.isTranslated
                ? WrappedText.createTranslated(text)
                : WrappedText.create(text));
        
        if (isName) {
            SaveHelper.add(EditType.NAME_CHANGE);
        } else {
            SaveHelper.add(EditType.DESCRIPTION_CHANGE);
        }
    }
}
