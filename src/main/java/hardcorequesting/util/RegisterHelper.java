package hardcorequesting.util;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.blocks.ModBlocks;
import hardcorequesting.items.ModItems;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Function;
import java.util.function.Supplier;

public class RegisterHelper {
    public static void registerBlock(Block block, String id, Function<Block, BlockItem> itemFunction) {
        Registry.register(Registry.BLOCK, new ResourceLocation(HardcoreQuesting.ID, id), block);
        registerItem(itemFunction.apply(block), id);
    }
    
    public static void registerItem(Item item, String id) {
        Registry.register(Registry.ITEM, new ResourceLocation(HardcoreQuesting.ID, id), item);
    }
    
    public static void register() {
        ModBlocks.init();
        ModBlocks.registerTileEntities();
        ModItems.init();
    }
    
    public static <T extends BlockEntity> BlockEntityType<T> registerTileEntity(Supplier<T> blockEntitySupplier, ResourceLocation identifier) {
        return Registry.register(Registry.BLOCK_ENTITY_TYPE, identifier, BlockEntityType.Builder.of(blockEntitySupplier).build(null));
    }
}
