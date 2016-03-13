package hardcorequesting;

import hardcorequesting.items.ItemBlockPortal;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class RegisterHelper
{
    public static void registerBlock(Block block) {
        GameRegistry.registerBlock(block);
    }

    public static void registerBlock(Block block, Class<ItemBlockPortal> blockClass) {
        GameRegistry.registerBlock(block, blockClass);
    }

    public static void registerItem(Item item) {
        GameRegistry.registerItem(item, item.getUnlocalizedName().substring(5));
    }

    public static void registerItemRenderer(Item item) {
        ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
        mesher.register(item, 0, new ModelResourceLocation(item.getUnlocalizedName().substring(5), "inventory"));
    }

    public static void registerBlockRenderer(Block block) {
        ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
        mesher.register(Item.getItemFromBlock(block), 0, new ModelResourceLocation(block.getUnlocalizedName().substring(5), "inventory"));
    }


}
