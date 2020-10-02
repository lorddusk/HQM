package hardcorequesting.common.blocks;

import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.items.ModCreativeTabs;
import hardcorequesting.common.tileentity.AbstractBarrelBlockEntity;
import hardcorequesting.common.tileentity.TrackerBlockEntity;
import hardcorequesting.common.util.RegisterHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlocks {
    public static Block blockBarrel = new DeliveryBlock(HardcoreQuestingCore.platform.createDeliveryBlockProperties());
    public static Block blockTracker = new TrackerBlock();
    //    public static Block blockPortal = new PortalBlock();
    public static BlockEntityType<AbstractBarrelBlockEntity> typeBarrel;
    public static BlockEntityType<TrackerBlockEntity> typeTracker;
//    public static BlockEntityType<PortalBlockEntity> typePortal;
    
    private ModBlocks() {
    }
    
    public static void init() {
        RegisterHelper.registerBlock(blockBarrel, BlockInfo.ITEMBARREL_UNLOCALIZED_NAME, block -> new BlockItem(block, new Item.Properties().tab(ModCreativeTabs.HQMTab)));
        RegisterHelper.registerBlock(blockTracker, BlockInfo.QUEST_TRACKER_UNLOCALIZED_NAME, block -> new BlockItem(block, new Item.Properties().tab(ModCreativeTabs.HQMTab)));
//        RegisterHelper.registerBlock(blockPortal, BlockInfo.QUEST_PORTAL_UNLOCALIZED_NAME, block -> new PortalBlockItem(block, new Item.Settings().group(HardcoreQuesting.HQMTab)));
    }
    
    public static void registerTileEntities() {
        typeBarrel = RegisterHelper.registerTileEntity(() -> HardcoreQuestingCore.platform.createBarrelBlockEntity(), new ResourceLocation(HardcoreQuestingCore.ID, BlockInfo.ITEMBARREL_UNLOCALIZED_NAME));
        typeTracker = RegisterHelper.registerTileEntity(TrackerBlockEntity::new, new ResourceLocation(HardcoreQuestingCore.ID, BlockInfo.QUEST_TRACKER_UNLOCALIZED_NAME));
//        typePortal = RegisterHelper.registerTileEntity(PortalBlockEntity::new, new Identifier(HardcoreQuesting.ID, BlockInfo.QUEST_PORTAL_TE_KEY));
    }
}
