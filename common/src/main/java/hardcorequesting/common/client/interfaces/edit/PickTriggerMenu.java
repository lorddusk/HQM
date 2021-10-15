package hardcorequesting.common.client.interfaces.edit;

import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.widget.ArrowSelectionHelper;
import hardcorequesting.common.client.interfaces.widget.NumberTextBox;
import hardcorequesting.common.quests.TriggerType;
import hardcorequesting.common.util.HQMUtil;
import net.minecraft.network.chat.FormattedText;

import java.util.function.Consumer;

public class PickTriggerMenu extends GuiEditMenu {
    
    private final Consumer<Result> resultConsumer;
    private TriggerType type;
    private int count;
    
    public PickTriggerMenu(GuiQuestBook gui, TriggerType typeIn, int countIn, Consumer<Result> resultConsumer) {
        super(gui, true);
    
        this.resultConsumer = resultConsumer;
        this.type = typeIn;
        this.count = countIn;
        
        addTextBox(new NumberTextBox(gui, 25, 135, "hqm.menuTrigger.taskCount") {
            @Override
            protected int getValue() {
                return count;
            }
            
            @Override
            protected void setValue(int number) {
                count = number;
            }
            
            @Override
            protected boolean isVisible() {
                return type.isUseTaskCount();
            }
        });
        
        addClickable(new ArrowSelectionHelper(gui, 25, 20) {
    
            @Override
            protected void onArrowClick(boolean left) {
                if (left) {
                    type = HQMUtil.cyclePrev(TriggerType.values(), type);
                } else {
                    type = HQMUtil.cycleNext(TriggerType.values(), type);
                }
            }
    
            @Override
            protected FormattedText getArrowText() {
                return type.getName();
            }
    
            @Override
            protected FormattedText getArrowDescription() {
                return type.getDescription();
            }
        });
    }
    
    @Override
    public void save() {
        resultConsumer.accept(new Result(type, count));
    }
    
    public static record Result(TriggerType type, int count) {}
}
