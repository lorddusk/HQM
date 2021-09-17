package hardcorequesting.common.client;

import hardcorequesting.common.client.interfaces.Graphic;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.QuestGraphic;
import hardcorequesting.common.quests.Quest;

public abstract class BookPage {
    
    public abstract Graphic createGraphic(GuiQuestBook gui);
    
    public abstract BookPage getParent();
    
    public static class QuestPage extends BookPage {
        private final Quest quest;
    
        public QuestPage(Quest quest) {
            this.quest = quest;
        }
    
        @Override
        public Graphic createGraphic(GuiQuestBook gui) {
            return new QuestGraphic(gui.getPlayer().getUUID(), quest, gui);
        }
    
        @Override
        public BookPage getParent() {
            return null;
        }
    }
}
