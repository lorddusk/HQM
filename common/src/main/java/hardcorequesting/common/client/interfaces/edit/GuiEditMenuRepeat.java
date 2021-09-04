package hardcorequesting.common.client.interfaces.edit;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.RepeatInfo;
import hardcorequesting.common.quests.RepeatType;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.minecraft.world.entity.player.Player;

public class GuiEditMenuRepeat extends GuiEditMenuExtended {
    
    private Quest quest;
    private RepeatType type;
    private int days;
    private int hours;
    
    public GuiEditMenuRepeat(GuiQuestBook gui, Player player, Quest quest) {
        super(gui, player, true, 25, 20, 25, 100);
        this.quest = quest;
        this.type = quest.getRepeatInfo().getType();
        days = quest.getRepeatInfo().getDays();
        hours = quest.getRepeatInfo().getHours();
        
        textBoxes.add(new TextBoxHidden(gui, 0, "hqm.repeatMenu.days") {
            @Override
            protected int getValue() {
                return days;
            }
            
            @Override
            protected void setValue(int number) {
                days = number;
            }
        });
        
        textBoxes.add(new TextBoxHidden(gui, 1, "hqm.repeatMenu.hours") {
            @Override
            protected void draw(PoseStack matrices, GuiBase gui, boolean selected) {
                super.draw(matrices, gui, selected);
                
                gui.drawString(matrices, gui.getLinesFromText(Translator.translatable("hqm.repeatMenu.mcDaysHours"), 0.7F, 150), x, y + BOX_OFFSET + TEXT_OFFSET, 0.7F, 0x404040);
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
    }
    
    @Override
    public void save(GuiBase gui) {
        quest.setRepeatInfo(new RepeatInfo(type, days, hours));
        SaveHelper.add(EditType.REPEATABILITY_CHANGED);
    }
    
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
    
    private abstract class TextBoxHidden extends TextBoxNumber {
        
        public TextBoxHidden(GuiQuestBook gui, int id, String title) {
            super(gui, id, title);
        }
        
        @Override
        protected boolean isVisible() {
            return type.isUseTime();
        }
    }
}
