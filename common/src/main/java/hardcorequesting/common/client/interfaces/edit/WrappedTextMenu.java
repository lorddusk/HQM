package hardcorequesting.common.client.interfaces.edit;

import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.widget.SimpleCheckBox;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import hardcorequesting.common.util.WrappedText;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.I18n;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class WrappedTextMenu extends AbstractTextMenu {
    
    private final SimpleCheckBox translatedCheckbox;
    
    private final Consumer<WrappedText> resultConsumer;
    private final int limit;
    private final boolean isName;
    
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
        
        addClickable(translatedCheckbox = new SimpleCheckBox(gui, Translator.translatable("hqm.textEditor.translationKey"), 180, 20, text.shouldTranslate()));
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
    
        resultConsumer.accept(translatedCheckbox.getValue()
                ? WrappedText.createTranslated(text)
                : WrappedText.create(text));
        
        if (isName) {
            SaveHelper.add(EditType.NAME_CHANGE);
        } else {
            SaveHelper.add(EditType.DESCRIPTION_CHANGE);
        }
    }
}
