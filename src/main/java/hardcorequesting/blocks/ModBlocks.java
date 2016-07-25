package hardcorequesting.blocks;

import cpw.mods.fml.common.registry.GameRegistry;
import hardcorequesting.items.ItemBlockPortal;
import hardcorequesting.items.ModItems;
import hardcorequesting.tileentity.TileEntityBarrel;
import hardcorequesting.tileentity.TileEntityPortal;
import hardcorequesting.tileentity.TileEntityTracker;
import hardcorequesting.util.RegisterHelper;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ModBlocks {
    public static Block itemBarrel = new BlockDelivery().setUnlocalizedName(BlockInfo.LOCALIZATION_START + BlockInfo.ITEMBARREL_UNLOCALIZED_NAME);
    public static Block itemTracker = new BlockTracker().setUnlocalizedName(BlockInfo.LOCALIZATION_START + BlockInfo.QUEST_TRACKER_UNLOCALIZED_NAME);
    public static Block itemPortal = new BlockPortal().setUnlocalizedName(BlockInfo.LOCALIZATION_START + BlockInfo.QUEST_PORTAL_UNLOCALIZED_NAME);

    public static void init() {
        GameRegistry.registerBlock(itemBarrel, BlockInfo.ITEMBARREL_UNLOCALIZED_NAME);
        GameRegistry.registerBlock(itemTracker, BlockInfo.QUEST_TRACKER_UNLOCALIZED_NAME);
        GameRegistry.registerBlock(itemPortal, BlockInfo.QUEST_PORTAL_UNLOCALIZED_NAME);
    }

    public static void initRender() {
    }

    public static void registerTileEntities() {
        GameRegistry.registerTileEntity(TileEntityBarrel.class, BlockInfo.TILEENTITY_PREFIX + BlockInfo.ITEMBARREL_TE_KEY);
        GameRegistry.registerTileEntity(TileEntityTracker.class, BlockInfo.TILEENTITY_PREFIX + BlockInfo.QUEST_TRACKER_TE_KEY);
        GameRegistry.registerTileEntity(TileEntityPortal.class, BlockInfo.TILEENTITY_PREFIX + BlockInfo.QUEST_PORTAL_TE_KEY);
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
