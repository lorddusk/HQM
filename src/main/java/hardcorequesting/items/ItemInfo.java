package hardcorequesting.items;

public abstract class ItemInfo {
    
    public static final String LOCALIZATION_START = "hqm:";
    //--Information for the Questing Book
    
    public static final String BOOK_UNLOCALIZED_NAME = "quest_book";
    
    //--Information for the Hearts
    public static final String HEART_UNLOCALIZED_NAME = "hearts";
    public static final String[] HEART_ICONS = {"quarterheart", "halfheart", "threequarts", "heart", "rottenheart"};
    
    //--Information for the Bags
    public static final String BAG_UNLOCALIZED_NAME = "bags";
    
    
    public static final String INVALID_UNLOCALIZED_NAME = "hqm_invalid_item";
    
    
    private ItemInfo() {
    }
}
