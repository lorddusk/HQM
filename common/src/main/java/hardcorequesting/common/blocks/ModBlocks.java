package hardcorequesting.common.blocks;

import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.items.ModCreativeTabs;
import hardcorequesting.common.tileentity.AbstractBarrelBlockEntity;
import hardcorequesting.common.tileentity.TrackerBlockEntity;
import hardcorequesting.common.util.RegisterHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Material;

import java.util.function.Supplier;

public class ModBlocks {
    public static Supplier<Block> blockBarrel;
    public static Supplier<Block> blockTracker;
    //    public static Block blockPortal = new PortalBlock();
    public static Supplier<BlockEntityType<AbstractBarrelBlockEntity>> typeBarrel;
    public static Supplier<BlockEntityType<TrackerBlockEntity>> typeTracker;
//    public static BlockEntityType<PortalBlockEntity> typePortal;
    
    private ModBlocks() {
    }
    
    public static void init() {
        blockBarrel = RegisterHelper.registerBlock(BlockInfo.ITEMBARREL_UNLOCALIZED_NAME,
                () -> new DeliveryBlock(Block.Properties.of(Material.WOOD).strength(1.0F)),
                block -> new BlockItem(block, new Item.Properties().arch$tab(ModCreativeTabs.HQMTab)));
        blockTracker = RegisterHelper.registerBlock(BlockInfo.QUEST_TRACKER_UNLOCALIZED_NAME,
                TrackerBlock::new,
                block -> new BlockItem(block, new Item.Properties().arch$tab(ModCreativeTabs.HQMTab)));
//        RegisterHelper.registerBlock(blockPortal, BlockInfo.QUEST_PORTAL_UNLOCALIZED_NAME, block -> new PortalBlockItem(block, new Item.Settings().group(HardcoreQuesting.HQMTab)));
    }
    
    public static void registerTileEntities() {
        typeBarrel = RegisterHelper.registerTileEntity(BlockInfo.ITEMBARREL_UNLOCALIZED_NAME, HardcoreQuestingCore.platform::createBarrelBlockEntity, blockBarrel);
        typeTracker = RegisterHelper.registerTileEntity(BlockInfo.QUEST_TRACKER_UNLOCALIZED_NAME, TrackerBlockEntity::new, blockTracker);
//        typePortal = RegisterHelper.registerTileEntity(PortalBlockEntity::new, new Identifier(HardcoreQuesting.ID, BlockInfo.QUEST_PORTAL_TE_KEY));
    }
}
