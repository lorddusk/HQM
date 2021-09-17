package hardcorequesting.common.client;

import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.graphic.Graphic;
import hardcorequesting.common.client.interfaces.graphic.QuestGraphic;
import hardcorequesting.common.client.interfaces.graphic.QuestSetMapGraphic;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestSet;
import org.jetbrains.annotations.Nullable;

public abstract class BookPage {
    
    public abstract Graphic createGraphic(GuiQuestBook gui);
    
    @Nullable
    public abstract BookPage getParent();
    
    public static class SetMapPage extends BookPage {
        private final QuestSet set;
    
        public SetMapPage(QuestSet set) {
            this.set = set;
        }
    
        @Override
        public Graphic createGraphic(GuiQuestBook gui) {
            return new QuestSetMapGraphic(gui, set, this);
        }
    
        @Override
        public BookPage getParent() {
            return null;
        }
        
        public BookPage forQuest(Quest quest) {
            return new QuestPage(this, quest);
        }
    }
    
    public static class QuestPage extends BookPage {
        private final SetMapPage parent;
        private final Quest quest;
    
        public QuestPage(SetMapPage parent, Quest quest) {
            this.parent = parent;
            this.quest = quest;
        }
    
        @Override
        public Graphic createGraphic(GuiQuestBook gui) {
            return new QuestGraphic(gui.getPlayer().getUUID(), quest, gui);
        }
    
        @Override
        public BookPage getParent() {
            return parent;
        }
    }
}
