package hardcorequesting.common.client;

import hardcorequesting.common.bag.Group;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.graphic.*;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestSet;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class BookPage {
    
    private final BookPage parent;
    
    private BookPage(BookPage parent) {
        this.parent = parent;
    }
    
    public abstract Graphic createGraphic(GuiQuestBook gui);
    
    @Nullable
    public final BookPage getParent() {
        return parent;
    }
    
    public boolean hasGoToMenuButton() {
        return true;
    }
    
    
    public static class MenuPage extends BookPage {
        public static final BookPage INSTANCE = new MenuPage();
        public final BookPage questSets = new SetsPage(this);
        public final BookPage reputations = new ReputationPage(this);
        public final BookPage bags = new BagsPage(this);
        
        private MenuPage() {
            super(null);
        }
    
        @Override
        public Graphic createGraphic(GuiQuestBook gui) {
            return new MenuPageGraphic(this, gui);
        }
    
        @Override
        public boolean hasGoToMenuButton() {
            return false;
        }
    }
    
    public static class SetsPage extends BookPage {
        private SetsPage(MenuPage parent) {
            super(parent);
        }
    
        @Override
        public Graphic createGraphic(GuiQuestBook gui) {
            return new QuestSetsGraphic(this, gui);
        }
        
        public BookPage forSet(QuestSet set) {
            return new SetMapPage(this, set);
        }
    }
    
    public static class SetMapPage extends BookPage {
        private final QuestSet set;
    
        private SetMapPage(SetsPage parent, QuestSet set) {
            super(parent);
            this.set = Objects.requireNonNull(set);
        }
    
        @Override
        public Graphic createGraphic(GuiQuestBook gui) {
            return new QuestSetMapGraphic(gui, set, this);
        }
    
        public BookPage forQuest(Quest quest) {
            return new QuestPage(this, quest);
        }
    }
    
    public static class QuestPage extends BookPage {
        private final Quest quest;
    
        private QuestPage(SetMapPage parent, Quest quest) {
            super(parent);
            this.quest = quest;
        }
    
        @Override
        public Graphic createGraphic(GuiQuestBook gui) {
            return new QuestGraphic(gui.getPlayer().getUUID(), quest, gui);
        }
    }
    
    public static class ReputationPage extends BookPage {
        private ReputationPage(MenuPage parent) {
            super(parent);
        }
        
        @Override
        public Graphic createGraphic(GuiQuestBook gui) {
            return new EditReputationGraphic(gui);
        }
    }
    
    public static class BagsPage extends BookPage {
        private BagsPage(MenuPage parent) {
            super(parent);
        }
    
        @Override
        public Graphic createGraphic(GuiQuestBook gui) {
            return new EditBagsGraphic(this, gui);
        }
        
        public BookPage forGroup(Group group) {
            return new GroupPage(this, group);
        }
    }
    
    public static class GroupPage extends BookPage {
        private final Group group;
    
        private GroupPage(BagsPage parent, Group group) {
            super(parent);
            this.group = group;
        }
    
        @Override
        public Graphic createGraphic(GuiQuestBook gui) {
            return new EditGroupGraphic(gui, group);
        }
    }
}
