package hardcorequesting;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class RegisterHelper
{
    public static void registerBlock(Block block) {
        GameRegistry.register(block);
    }

    public static void registerBlock(Block block, Class<? extends ItemBlock> blockClass) {
        GameRegistry.register(block);
        GameRegistry.register(createItemBlock(block, blockClass), block.getRegistryName());
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
        GameRegistry.register(item);
    }

    public static void registerItemRenderer(Item item) {
        ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
        mesher.register(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }

    public static void registerBlockRenderer(Block block) {
        ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
        mesher.register(Item.getItemFromBlock(block), 0, new ModelResourceLocation(block.getRegistryName(), "inventory"));
    }


}
