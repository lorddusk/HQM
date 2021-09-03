package hardcorequesting.common.util;

import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.blocks.ModBlocks;
import hardcorequesting.common.items.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class RegisterHelper {
    
    public static <T extends Block> Supplier<T> registerBlock(String id, Supplier<T> block, Function<T, BlockItem> itemFunction) {
        Supplier<T> supplier = HardcoreQuestingCore.platform.registerBlock(id, block);
        registerItem(id, () -> itemFunction.apply(supplier.get()));
        return supplier;
    }
    
    public static <T extends Item> Supplier<T> registerItem(String id, Supplier<T> item) {
        return HardcoreQuestingCore.platform.registerItem(id, item);
    }
    
    public static void register() {
        ModBlocks.init();
        ModBlocks.registerTileEntities();
        ModItems.init();
    }
    
    public static <T extends BlockEntity> Supplier<BlockEntityType<T>> registerTileEntity(String id, BiFunction<BlockPos, BlockState, T> blockEntitySupplier) {
        return HardcoreQuestingCore.platform.registerBlockEntity(id, blockEntitySupplier);
    }
}