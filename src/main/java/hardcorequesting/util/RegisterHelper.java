package hardcorequesting.util;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.blocks.ModBlocks;
import hardcorequesting.items.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.function.Function;
import java.util.function.Supplier;

public class RegisterHelper {
    public static void registerBlock(Block block, String id, Function<Block, BlockItem> itemFunction) {
        Registry.register(Registry.BLOCK, new Identifier(HardcoreQuesting.ID, id), block);
        registerItem(itemFunction.apply(block), id);
    }
    
    public static void registerItem(Item item, String id) {
        Registry.register(Registry.ITEM, new Identifier(HardcoreQuesting.ID, id), item);
    }
    
    public static void register() {
        ModBlocks.init();
        ModBlocks.registerTileEntities();
        ModItems.init();
    }
    
    public static <T extends BlockEntity> BlockEntityType<T> registerTileEntity(Supplier<T> blockEntitySupplier, Identifier identifier) {
        return Registry.register(Registry.BLOCK_ENTITY_TYPE, identifier, BlockEntityType.Builder.create(blockEntitySupplier).build(null));
    }
}
