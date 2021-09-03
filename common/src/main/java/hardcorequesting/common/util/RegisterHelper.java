package hardcorequesting.common.util;

import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.blocks.ModBlocks;
import hardcorequesting.common.items.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
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
        Supplier<Block> supplier = HardcoreQuestingCore.platform.registerBlock(new ResourceLocation(HardcoreQuestingCore.ID, id), (Supplier<Block>) block);
        registerItem(id, () -> itemFunction.apply((T) supplier.get()));
        return (Supplier<T>) supplier;
    }
    
    public static <T extends Item> Supplier<T> registerItem(String id, Supplier<T> item) {
        return (Supplier<T>) HardcoreQuestingCore.platform.registerItem(new ResourceLocation(HardcoreQuestingCore.ID, id), (Supplier<Item>) item);
    }
    
    public static void register() {
        ModBlocks.init();
        ModBlocks.registerTileEntities();
        ModItems.init();
    }
    
    public static <T extends BlockEntity> Supplier<BlockEntityType<T>> registerTileEntity(String id, BiFunction<BlockPos, BlockState, T> blockEntitySupplier) {
        return (Supplier) HardcoreQuestingCore.platform.registerBlockEntity(new ResourceLocation(HardcoreQuestingCore.ID, id), blockEntitySupplier);
    }
}
