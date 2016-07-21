package hardcorequesting.util;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class RegisterHelper {
    public static void registerBlock(Block block) {
        GameRegistry.registerBlock(block, block.getUnlocalizedName());
    }

    public static void registerBlock(Block block, Class<? extends ItemBlock> blockClass) {
        GameRegistry.registerBlock(block, block.getUnlocalizedName());
    }

    private static ItemBlock createItemBlock(Block block, Class<? extends ItemBlock> itemBlockClass) {
        try {
            Class<?>[] ctorArgClasses = new Class<?>[1];
            ctorArgClasses[0] = Block.class;
            Constructor<? extends ItemBlock> itemCtor = itemBlockClass.getConstructor(ctorArgClasses);
            return itemCtor.newInstance(block);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }


    public static void registerItem(Item item) {
        GameRegistry.registerItem(item, item.getUnlocalizedName());
    }



}
