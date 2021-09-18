package hardcorequesting.common.client.interfaces.edit;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.widget.ArrowSelectionHelper;
import hardcorequesting.common.client.interfaces.widget.NumberTextBox;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.RepeatInfo;
import hardcorequesting.common.quests.RepeatType;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;

import java.util.UUID;

public class GuiEditMenuRepeat extends GuiEditMenu {
    
    private Quest quest;
    private RepeatType type;
    private int days;
    private int hours;
    private final ArrowSelectionHelper selectionHelper;
    
    public GuiEditMenuRepeat(GuiQuestBook gui, UUID playerId, Quest quest) {
        super(gui, playerId, true);
        this.quest = quest;
        this.type = quest.getRepeatInfo().getType();
        days = quest.getRepeatInfo().getDays();
        hours = quest.getRepeatInfo().getHours();
        
        textBoxes.add(new TextBoxHidden(gui, 25, 100, "hqm.repeatMenu.days") {
            @Override
            protected int getValue() {
                return days;
            }
            
            @Override
            protected void setValue(int number) {
                days = number;
            }
        });
        
        textBoxes.add(new TextBoxHidden(gui, 25, 100 + BOX_OFFSET, "hqm.repeatMenu.hours") {
            @Override
            protected void draw(PoseStack matrices, boolean selected) {
                super.draw(matrices, selected);
    
                this.gui.drawString(matrices, this.gui.getLinesFromText(Translator.translatable("hqm.repeatMenu.mcDaysHours"), 0.7F, 150), x, y + BOX_OFFSET + TEXT_OFFSET, 0.7F, 0x404040);
            }
            
            @Override
            protected int getValue() {
                return hours;
            }
            
            @Override
            protected void setValue(int number) {
                hours = number;
            }
        });
        
        selectionHelper = new ArrowSelectionHelper(gui, 25, 20) {
            @Override
            protected void onArrowClick(boolean left) {
                if (left) {
                    type = RepeatType.values()[(type.ordinal() + RepeatType.values().length - 1) % RepeatType.values().length];
                } else {
                    type = RepeatType.values()[(type.ordinal() + 1) % RepeatType.values().length];
                }
            }
    
            @Override
            protected String getArrowText() {
                return type.getName();
            }
    
            @Override
            protected String getArrowDescription() {
                return type.getDescription();
            }
        };
    }
    
    @Override
    public void draw(PoseStack matrices, int mX, int mY) {
        super.draw(matrices, mX, mY);
        
        selectionHelper.render(matrices, mX, mY);
    }
    
    @Override
    public void onClick(int mX, int mY, int b) {
        super.onClick(mX, mY, b);
        
        selectionHelper.onClick(mX, mY);
    }
    
    @Override
    public void onRelease(int mX, int mY) {
        super.onRelease(mX, mY);
        
        selectionHelper.onRelease();
    }
    
    @Override
    public void save() {
        quest.setRepeatInfo(new RepeatInfo(type, days, hours));
        SaveHelper.add(EditType.REPEATABILITY_CHANGED);
    }
    
    private abstract class TextBoxHidden extends NumberTextBox {
        
        public TextBoxHidden(GuiQuestBook gui, int x, int y, String title) {
            super(gui, x, y, title);
        }
        
        @Override
        protected boolean isVisible() {
            return type.isUseTime();
        }
    }
}
