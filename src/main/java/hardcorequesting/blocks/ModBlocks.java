package hardcorequesting.blocks;

import hardcorequesting.RegisterHelper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.LanguageRegistry;
import hardcorequesting.items.ItemBlockPortal;
import hardcorequesting.items.ModItems;
import hardcorequesting.tileentity.TileEntityBarrel;
import hardcorequesting.tileentity.TileEntityPortal;
import hardcorequesting.tileentity.TileEntityTracker;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class ModBlocks {
    public static Block itemBarrel = new BlockDelivery().setUnlocalizedName(BlockInfo.LOCALIZATION_START + BlockInfo.ITEMBARREL_UNLOCALIZED_NAME);
    public static Block itemTracker = new BlockTracker().setUnlocalizedName(BlockInfo.LOCALIZATION_START + BlockInfo.QUEST_TRACKER_UNLOCALIZED_NAME);
    public static Block itemPortal = new BlockPortal().setUnlocalizedName(BlockInfo.LOCALIZATION_START + BlockInfo.QUEST_PORTAL_UNLOCALIZED_NAME);

    public static void init() {
        RegisterHelper.registerBlock(itemBarrel);
        RegisterHelper.registerBlock(itemTracker);
        RegisterHelper.registerBlock(itemPortal, ItemBlockPortal.class);
    }

    public static void initRender() {
        RegisterHelper.registerBlockRenderer(itemBarrel);
        RegisterHelper.registerBlockRenderer(itemTracker);
        RegisterHelper.registerBlockRenderer(itemPortal);
    }

    public static void registerTileEntities() {
        GameRegistry.registerTileEntity(TileEntityBarrel.class, BlockInfo.ITEMBARREL_TE_KEY);
        GameRegistry.registerTileEntity(TileEntityTracker.class, BlockInfo.QUEST_TRACKER_TE_KEY);
        GameRegistry.registerTileEntity(TileEntityPortal.class, BlockInfo.QUEST_PORTAL_TE_KEY);
    }

    public static void registerRecipes() {
        GameRegistry.addRecipe(new ItemStack(ModBlocks.itemBarrel),
                "wgw",
                "gqg",
                "wgw",
                'w', Blocks.planks, 'q', ModItems.book.setContainerItem(ModItems.book), 'g', Blocks.glass);
    }


    private ModBlocks() {
    }
}
