package hardcorequesting.common.client;

import hardcorequesting.common.bag.LootGroup;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.graphic.*;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestSet;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A page is associated with a menu in the quest book screen.
 * Unlike Graphic instances that is thrown away with the screen,
 * the page is kept to track which menu is considered open.
 * A page is responsible for creating the associated graphic,
 * and to point to its parent page.
 * @author kirderf1
 */
public abstract class BookPage {
    
    private final BookPage parent;
    
    private BookPage(BookPage parent) {
        this.parent = parent;
    }
    
    public abstract Graphic createGraphic(GuiQuestBook gui);
    
    @NotNull
    public final BookPage getParent() {
        return Objects.requireNonNull(parent, "This page does not have a parent!");
    }
    
    public boolean canGoBack() {
        return parent != null;
    }
    
    public boolean hasGoToMenuButton() {
        return true;
    }
    
    /**
     * This is the first page, and is shown when first opening the quest book.
     */
    public static class MainPage extends BookPage {
        public static final MainPage INSTANCE = new MainPage();
    
        private MainPage() {
            super(null);
        }
    
        @Override
        public Graphic createGraphic(GuiQuestBook gui) {
            return new MainPageGraphic(gui);
        }
    
        @Override
        public boolean hasGoToMenuButton() {
            return false;
        }
    }
    
    public static class MenuPage extends BookPage {
        public static final MenuPage INSTANCE = new MenuPage();
        
        private MenuPage() {
            super(MainPage.INSTANCE);
        }
    
        @Override
        public Graphic createGraphic(GuiQuestBook gui) {
            return new MenuPageGraphic(gui);
        }
    
        @Override
        public boolean hasGoToMenuButton() {
            return false;
        }
    }
    
    public static class SetsPage extends BookPage {
        public static final SetsPage INSTANCE = new SetsPage();
        
        private SetsPage() {
            super(MenuPage.INSTANCE);
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
        public static final ReputationPage INSTANCE = new ReputationPage();
        
        private ReputationPage() {
            super(MenuPage.INSTANCE);
        }
        
        @Override
        public Graphic createGraphic(GuiQuestBook gui) {
            return new EditReputationGraphic(gui);
        }
    }
    
    public static class BagsPage extends BookPage {
        public static final BagsPage INSTANCE = new BagsPage();
        
        private BagsPage() {
            super(MenuPage.INSTANCE);
        }
    
        @Override
        public Graphic createGraphic(GuiQuestBook gui) {
            return new EditBagsGraphic(this, gui);
        }
        
        public BookPage forGroup(LootGroup group) {
            return new GroupPage(this, group);
        }
    }
    
    public static class GroupPage extends BookPage {
        private final LootGroup group;
    
        private GroupPage(BagsPage parent, LootGroup group) {
            super(parent);
            this.group = group;
        }
    
        @Override
        public Graphic createGraphic(GuiQuestBook gui) {
            return new EditGroupGraphic(gui, group);
        }
    }
    
    public static class TeamPage extends BookPage {
        public static final TeamPage INSTANCE = new TeamPage();
        
        private TeamPage() {
            super(MenuPage.INSTANCE);
        }
    
        @Override
        public Graphic createGraphic(GuiQuestBook gui) {
            return new TeamMenuGraphic(gui, gui.getPlayer().getUUID());
        }
    }
    
    public static class TeamListPage extends BookPage {
        public static final TeamListPage INSTANCE = new TeamListPage();
    
        private TeamListPage() {
            super(TeamPage.INSTANCE);
        }
    
        @Override
        public Graphic createGraphic(GuiQuestBook gui) {
            return new TeamListGraphic(gui);
        }
    }
    
    public static class DeathInfoPage extends BookPage {
        public static final DeathInfoPage INSTANCE = new DeathInfoPage();
        
        private DeathInfoPage() {
            super(MenuPage.INSTANCE);
        }
        
        @Override
        public Graphic createGraphic(GuiQuestBook gui) {
            return new DeathStatsGraphic(gui);
        }
    }
}
