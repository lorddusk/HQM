package hardcorequesting.common.items;


import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.bag.BagTier;
import hardcorequesting.common.util.RegisterHelper;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

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
    
    public static Supplier<Item> quarterHeart;
    public static Supplier<Item> halfHeart;
    public static Supplier<Item> threeQuartsHeart;
    public static Supplier<Item> heart;
    public static Supplier<Item> rottenHeart;
    
    public static void init() {
        book = RegisterHelper.registerItem("quest_book", () -> new QuestBookItem(false));
        enabledBook = RegisterHelper.registerItem("enabled_quest_book", () -> new QuestBookItem(true));
        basicBag = RegisterHelper.registerItem("basic_bag", () -> new BagItem(BagTier.BASIC));
        goodBag = RegisterHelper.registerItem("good_bag", () -> new BagItem(BagTier.GOOD));
        greaterBag = RegisterHelper.registerItem("greater_bag", () -> new BagItem(BagTier.GREATER));
        epicBag = RegisterHelper.registerItem("epic_bag", () -> new BagItem(BagTier.EPIC));
        legendaryBag = RegisterHelper.registerItem("legendary_bag", () -> new BagItem(BagTier.LEGENDARY));
        invalidItem = RegisterHelper.registerItem("hqm_invalid_item", InvalidItem::new);
        
        quarterHeart = RegisterHelper.registerItem("quarterheart", () -> new Item(new Item.Properties()));
        halfHeart = RegisterHelper.registerItem("halfheart", () -> new Item(new Item.Properties()));
        threeQuartsHeart = RegisterHelper.registerItem("threequartsheart", () -> new Item(new Item.Properties()));
        heart = RegisterHelper.registerItem("heart", () -> new HeartItem(new Item.Properties()));
        rottenHeart = RegisterHelper.registerItem("rottenheart", () -> new RottenHeartItem(new Item.Properties()));
    }

    public static final class DataComponents {
        public static final DeferredRegister<DataComponentType<?>> REGISTER = DeferredRegister.create(HardcoreQuestingCore.ID, Registries.DATA_COMPONENT_TYPE);

        public static final RegistrySupplier<DataComponentType<QuestBookItem.UseAsPlayer>> USE_AS_PLAYER = REGISTER.register("use_as_player",
                () -> DataComponentType.<QuestBookItem.UseAsPlayer>builder().persistent(QuestBookItem.UseAsPlayer.CODEC)
                        .networkSynchronized(QuestBookItem.UseAsPlayer.STREAM_CODEC).build());
    }
}
