package hardcorequesting.util;

import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

public class RegisterHelper {
    static Set<Block> blocks = Sets.newHashSet();
    static Set<Item> items = Sets.newHashSet();
    
    public static void registerBlock(Block block) {
        blocks.add(block);
    }

    public static void registerBlock(Block block, Class<? extends ItemBlock> blockClass) {
        blocks.add(block);
        items.add(createItemBlock(block, blockClass));
    }

    private static ItemBlock createItemBlock(Block block, Class<? extends ItemBlock> itemBlockClass) {
        try {
            Class<?>[] ctorArgClasses = new Class<?>[1];
            ctorArgClasses[0] = Block.class;
            Constructor<? extends ItemBlock> itemCtor = itemBlockClass.getConstructor(ctorArgClasses);
            
            ItemBlock itemblock = itemCtor.newInstance(block);
            itemblock.setRegistryName(block.getRegistryName());
            return itemblock;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }


    public static void registerItem(Item item) {
        items.add(item);
    }

    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        blocks.forEach(block -> event.getRegistry().register(block));
    }

    public static void registerItems(RegistryEvent.Register<Item> event) {
        items.forEach(item -> event.getRegistry().register(item));
    }
}
