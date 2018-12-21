package hardcorequesting.blocks;

import hardcorequesting.ModInformation;
import hardcorequesting.crafting.Recipes;
import hardcorequesting.items.ItemBlockPortal;
import hardcorequesting.items.ModItems;
import hardcorequesting.tileentity.TileEntityBarrel;
import hardcorequesting.tileentity.TileEntityPortal;
import hardcorequesting.tileentity.TileEntityTracker;
import hardcorequesting.util.RegisterHelper;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModBlocks {

    public static Block itemBarrel = new BlockDelivery().setTranslationKey(BlockInfo.LOCALIZATION_START + BlockInfo.ITEMBARREL_UNLOCALIZED_NAME);
    public static Block itemTracker = new BlockTracker().setTranslationKey(BlockInfo.LOCALIZATION_START + BlockInfo.QUEST_TRACKER_UNLOCALIZED_NAME);
    public static Block itemPortal = new BlockPortal().setTranslationKey(BlockInfo.LOCALIZATION_START + BlockInfo.QUEST_PORTAL_UNLOCALIZED_NAME);

    private ModBlocks() {
    }

    public static void init() {
        RegisterHelper.registerBlock(itemBarrel, ItemBlock.class);
        RegisterHelper.registerBlock(itemTracker, ItemBlock.class);
        RegisterHelper.registerBlock(itemPortal, ItemBlockPortal.class);
    }

    public static void registerTileEntities() {
        GameRegistry.registerTileEntity(TileEntityBarrel.class, new ResourceLocation(ModInformation.ID, BlockInfo.ITEMBARREL_TE_KEY));
        GameRegistry.registerTileEntity(TileEntityTracker.class, new ResourceLocation(ModInformation.ID, BlockInfo.QUEST_TRACKER_TE_KEY));
        GameRegistry.registerTileEntity(TileEntityPortal.class, new ResourceLocation(ModInformation.ID, BlockInfo.QUEST_PORTAL_TE_KEY));
    }

    public static void registerRecipes() {
        Recipes.addShapedRecipe(new ItemStack(ModBlocks.itemBarrel),
                "wgw",
                "gqg",
                "wgw",
                'w', "plankWood", 'q', ModItems.book.setContainerItem(ModItems.book), 'g', "blockGlassColorless");
    }
}
