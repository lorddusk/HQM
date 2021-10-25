package hardcorequesting.common.client.interfaces.edit;

import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.widget.ArrowSelectionHelper;
import hardcorequesting.common.client.interfaces.widget.NumberTextBox;
import hardcorequesting.common.util.Translator;
import net.minecraft.network.chat.FormattedText;

import java.util.function.IntConsumer;

/**
 * A menu for picking the number of quest requirements that need to be completed before a quest is unlocked.
 */
public class RequiredParentCountMenu extends GuiEditMenu {
    
    private final IntConsumer resultConsumer;
    private boolean requiresSpecified;
    private int requiredParents;
    
    public static void display(GuiQuestBook gui, boolean requiresSpecified, int requiredParents, IntConsumer resultConsumer) {
        gui.setEditMenu(new RequiredParentCountMenu(gui, requiresSpecified, requiredParents, resultConsumer));
    }
    
    private RequiredParentCountMenu(GuiQuestBook gui, boolean requiresSpecified, int requiredParents, IntConsumer resultConsumer) {
        super(gui, true);
   
        this.resultConsumer = resultConsumer;
        this.requiredParents = requiredParents;
        this.requiresSpecified = requiresSpecified;
        
        addTextBox(new NumberTextBox(gui, 25, 105, Translator.translatable("hqm.parentCount.count"),
                () -> RequiredParentCountMenu.this.requiredParents, value -> RequiredParentCountMenu.this.requiredParents = value) {
            @Override
            protected boolean isVisible() {
                return RequiredParentCountMenu.this.requiresSpecified;
            }
        });
        
        addClickable(new ArrowSelectionHelper(gui,  25, 20) {
            @Override
            protected void onArrowClick(boolean left) {
                RequiredParentCountMenu.this.requiresSpecified = !RequiredParentCountMenu.this.requiresSpecified;
            }
    
            @Override
            protected FormattedText getArrowText() {
                return Translator.translatable("hqm.parentCount.req" + (RequiredParentCountMenu.this.requiresSpecified ? "Count" : "All") + ".title");
            }
    
            @Override
            protected FormattedText getArrowDescription() {
                return Translator.translatable("hqm.parentCount.req" + (RequiredParentCountMenu.this.requiresSpecified ? "Count" : "All") + ".desc");
            }
    
        });
    }
    
    @Override
    public void save() {
        resultConsumer.accept(requiresSpecified ? requiredParents : -1);
    }
}
