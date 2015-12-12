package hardcorequesting.client.interfaces;

import hardcorequesting.SaveHelper;
import hardcorequesting.Translator;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.RepeatInfo;
import hardcorequesting.quests.RepeatType;
import net.minecraft.entity.player.EntityPlayer;

public class GuiEditMenuRepeat extends GuiEditMenuExtended {
    private Quest quest;
    private RepeatType type;
    private int days;
    private int hours;

    public GuiEditMenuRepeat(GuiQuestBook gui, EntityPlayer player, Quest quest) {
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
            protected int getValue() {
                return hours;
            }

            @Override
            protected void setValue(int number) {
                hours = number;
            }

            @Override
            protected void draw(GuiBase gui, boolean selected) {
                super.draw(gui, selected);

                gui.drawString(gui.getLinesFromText(Translator.translate("hqm.repeatMenu.mcDaysHours"), 0.7F, 150), BOX_X, BOX_Y + BOX_OFFSET * 2 + TEXT_OFFSET, 0.7F, 0x404040);
            }
        });
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

    @Override
    protected void save(GuiBase gui) {
        quest.setRepeatInfo(new RepeatInfo(type, days, hours));
        SaveHelper.add(SaveHelper.EditType.REPEATABILITY_CHANGED);
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
}
