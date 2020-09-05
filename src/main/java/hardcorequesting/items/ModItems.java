package hardcorequesting.items;


import hardcorequesting.bag.BagTier;
import hardcorequesting.util.RegisterHelper;

public class ModItems {
    public static QuestBookItem book = new QuestBookItem(false);
    public static QuestBookItem enabledBook = new QuestBookItem(true);
    public static BagItem basicBag = new BagItem(BagTier.BASIC);
    public static BagItem goodBag = new BagItem(BagTier.GOOD);
    public static BagItem greaterBag = new BagItem(BagTier.GREATER);
    public static BagItem epicBag = new BagItem(BagTier.EPIC);
    public static BagItem legendaryBag = new BagItem(BagTier.LEGENDARY);
    public static InvalidItem invalidItem = new InvalidItem();
    
    public static ItemHeart quarterHeart = new ItemHeart(0);
    public static ItemHeart halfHeart = new ItemHeart(1);
    public static ItemHeart threeQuartsHeart = new ItemHeart(2);
    public static ItemHeart heart = new ItemHeart(3);
    public static ItemHeart rottenHeart = new ItemHeart(4);
    
    public static void init() {
        RegisterHelper.registerItem(book, "quest_book");
        book.craftingRemainingItem = book;
        RegisterHelper.registerItem(enabledBook, "enabled_quest_book");
        enabledBook.craftingRemainingItem = enabledBook;
        RegisterHelper.registerItem(basicBag, "basic_bag");
        RegisterHelper.registerItem(goodBag, "good_bag");
        RegisterHelper.registerItem(greaterBag, "greater_bag");
        RegisterHelper.registerItem(epicBag, "epic_bag");
        RegisterHelper.registerItem(legendaryBag, "legendary_bag");
        RegisterHelper.registerItem(invalidItem, "hqm_invalid_item");
        
        RegisterHelper.registerItem(quarterHeart, "quarterheart");
        RegisterHelper.registerItem(halfHeart, "halfheart");
        RegisterHelper.registerItem(threeQuartsHeart, "threequartsheart");
        RegisterHelper.registerItem(heart, "heart");
        RegisterHelper.registerItem(rottenHeart, "rottenheart");
    }
}
