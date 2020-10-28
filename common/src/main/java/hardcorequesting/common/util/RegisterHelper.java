package hardcorequesting.common.util;

import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.blocks.ModBlocks;
import hardcorequesting.common.items.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Function;
import java.util.function.Supplier;

public class RegisterHelper {
    public static Supplier<Block> delegateBlock(String id) {
        ResourceLocation location = new ResourceLocation(HardcoreQuestingCore.ID, id);
        return () -> HardcoreQuestingCore.platform.getBlock(location);
    }
    
    public static Supplier<Item> delegateItem(String id) {
        ResourceLocation location = new ResourceLocation(HardcoreQuestingCore.ID, id);
        return () -> HardcoreQuestingCore.platform.getItem(location);
    }
    
    public static Supplier<BlockEntityType<?>> delegateBlockEntity(String id) {
        ResourceLocation location = new ResourceLocation(HardcoreQuestingCore.ID, id);
        return () -> HardcoreQuestingCore.platform.getBlockEntity(location);
    }
    
    public static <T extends Block> Supplier<T> registerBlock(String id, Supplier<T> block, Function<T, BlockItem> itemFunction) {
        HardcoreQuestingCore.platform.registerBlock(new ResourceLocation(HardcoreQuestingCore.ID, id), (Supplier<Block>) block);
        registerItem(id, () -> itemFunction.apply((T) delegateBlock(id).get()));
        return (Supplier<T>) delegateBlock(id);
    }
    
    public static <T extends Item> Supplier<T> registerItem(String id, Supplier<T> item) {
        HardcoreQuestingCore.platform.registerItem(new ResourceLocation(HardcoreQuestingCore.ID, id), (Supplier<Item>) item);
        return (Supplier<T>) delegateItem(id);
    }
    
    public static void register() {
        ModBlocks.init();
        ModBlocks.registerTileEntities();
        ModItems.init();
    }
    
    public static <T extends BlockEntity> Supplier<BlockEntityType<T>> registerTileEntity(String id, Supplier<T> blockEntitySupplier) {
        HardcoreQuestingCore.platform.registerBlockEntity(new ResourceLocation(HardcoreQuestingCore.ID, id), () -> BlockEntityType.Builder.of(blockEntitySupplier).build(null));
        return (Supplier) delegateBlockEntity(id);
    }
}
