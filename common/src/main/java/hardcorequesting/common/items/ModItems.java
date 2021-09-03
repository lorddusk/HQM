package hardcorequesting.common.items;


import hardcorequesting.common.bag.BagTier;
import hardcorequesting.common.util.RegisterHelper;

import java.util.function.Supplier;

public class ModItems {
    public static Supplier<QuestBookItem> book;
    public static Supplier<QuestBookItem> enabledBook;
    public static Supplier<BagItem> basicBag;
    public static Supplier<BagItem> goodBag;
    public static Supplier<BagItem> greaterBag;
    public static Supplier<BagItem> epicBag;
    public static Supplier<BagItem> legendaryBag;
    public static Supplier<InvalidItem> invalidItem;
    
    public static Supplier<ItemHeart> quarterHeart;
    public static Supplier<ItemHeart> halfHeart;
    public static Supplier<ItemHeart> threeQuartsHeart;
    public static Supplier<ItemHeart> heart;
    public static Supplier<ItemHeart> rottenHeart;
    
    public static void init() {
        book = RegisterHelper.registerItem("quest_book", () -> new QuestBookItem(false));
        enabledBook = RegisterHelper.registerItem("enabled_quest_book", () -> new QuestBookItem(true));
        basicBag = RegisterHelper.registerItem("basic_bag", () -> new BagItem(BagTier.BASIC));
        goodBag = RegisterHelper.registerItem("good_bag", () -> new BagItem(BagTier.GOOD));
        greaterBag = RegisterHelper.registerItem("greater_bag", () -> new BagItem(BagTier.GREATER));
        epicBag = RegisterHelper.registerItem("epic_bag", () -> new BagItem(BagTier.EPIC));
        legendaryBag = RegisterHelper.registerItem("legendary_bag", () -> new BagItem(BagTier.LEGENDARY));
        invalidItem = RegisterHelper.registerItem("hqm_invalid_item", InvalidItem::new);
        
        quarterHeart = RegisterHelper.registerItem("quarterheart", () -> new ItemHeart(0));
        halfHeart = RegisterHelper.registerItem("halfheart", () -> new ItemHeart(1));
        threeQuartsHeart = RegisterHelper.registerItem("threequartsheart", () -> new ItemHeart(2));
        heart = RegisterHelper.registerItem("heart", () -> new ItemHeart(3));
        rottenHeart = RegisterHelper.registerItem("rottenheart", () -> new ItemHeart(4));
    }
}
