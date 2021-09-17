package hardcorequesting.common.client;

import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.graphic.Graphic;
import hardcorequesting.common.client.interfaces.graphic.QuestGraphic;
import hardcorequesting.common.client.interfaces.graphic.QuestSetMapGraphic;
import hardcorequesting.common.client.interfaces.graphic.QuestSetsGraphic;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestSet;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class BookPage {
    
    public abstract Graphic createGraphic(GuiQuestBook gui);
    
    @Nullable
    public abstract BookPage getParent();
    
    public static class SetsPage extends BookPage {
        @Override
        public Graphic createGraphic(GuiQuestBook gui) {
            return new QuestSetsGraphic(this, gui);
        }
    
        @Override
        public @Nullable BookPage getParent() {
            return null;
        }
        
        public BookPage forSet(QuestSet set) {
            return new SetMapPage(this, set);
        }
    }
    
    public static class SetMapPage extends BookPage {
        private final SetsPage parent;
        private final QuestSet set;
    
        private SetMapPage(SetsPage parent, QuestSet set) {
            this.parent = parent;
            this.set = Objects.requireNonNull(set);
        }
    
        @Override
        public Graphic createGraphic(GuiQuestBook gui) {
            return new QuestSetMapGraphic(gui, set, this);
        }
    
        @Override
        public BookPage getParent() {
            return parent;
        }
        
        public BookPage forQuest(Quest quest) {
            return new QuestPage(this, quest);
        }
    }
    
    public static class QuestPage extends BookPage {
        private final SetMapPage parent;
        private final Quest quest;
    
        private QuestPage(SetMapPage parent, Quest quest) {
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
