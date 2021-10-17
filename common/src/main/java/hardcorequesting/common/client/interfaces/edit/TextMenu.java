package hardcorequesting.common.client.interfaces.edit;

import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.I18n;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class TextMenu extends AbstractTextMenu {
    
    private final Consumer<String> resultConsumer;
    private final int limit;
    private final boolean isName;
    
    public static void display(GuiQuestBook gui, String txt, boolean isName, Consumer<String> resultConsumer) {
        gui.setEditMenu(new TextMenu(gui, txt, isName, -1, resultConsumer));
    }
    
    public static void display(GuiQuestBook gui, String txt, int limit, Consumer<String> resultConsumer) {
        gui.setEditMenu(new TextMenu(gui, txt, true, limit, resultConsumer));
    }
    
    private TextMenu(GuiQuestBook gui, String txt, boolean isName, int limit, Consumer<String> resultConsumer) {
        super(gui, txt, !isName);
    
        this.resultConsumer = resultConsumer;
        this.limit = limit;
        
        this.isName = isName;
    }
    
    @Override
    public void save() {
        String str = textLogic.getText();
        if (str == null || str.isEmpty()) {
            str = I18n.get("hqm.textEditor.unnamed");
        }
        
        if (limit >= 0) {
            while (gui.getStringWidth(str) > limit) {
                str = str.substring(0, str.length() - 1);
            }
        }
    
        resultConsumer.accept(str);
        
        if (isName) {
            SaveHelper.add(EditType.NAME_CHANGE);
        } else {
            SaveHelper.add(EditType.DESCRIPTION_CHANGE);
        }
    }
}
