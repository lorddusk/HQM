package hardcorequesting.blocks;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.items.ModItems;
import hardcorequesting.quests.Quest;
import hardcorequesting.tileentity.TileEntityBarrel;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

//import net.minecraft.client.renderer.texture.IIconRegister;
//import net.minecraft.util.IIcon;

public class BlockDelivery extends BlockContainer {

    public BlockDelivery() {
        super(Material.WOOD);
        setRegistryName(BlockInfo.LOCALIZATION_START + BlockInfo.ITEMBARREL_UNLOCALIZED_NAME);
        setCreativeTab(HardcoreQuesting.HQMTab);
        setHardness(1f);
    }

//    @SideOnly(Side.CLIENT)
//    private IIcon activeIcon;
//    @SideOnly(Side.CLIENT)
//    private IIcon emptyIcon;
//
//    @Override
//    @SideOnly(Side.CLIENT)
//    public void registerBlockIcons(IIconRegister icon) {
//        pickIcons(icon);
//    }
//
//    public void pickIcons(IIconRegister icon) {
//        activeIcon = icon.registerIcon(BlockInfo.TEXTURE_LOCATION + ":" + BlockInfo.ITEMBARREL_ICON);
//        emptyIcon = icon.registerIcon(BlockInfo.TEXTURE_LOCATION + ":" + BlockInfo.ITEMBARREL_ICON_EMPTY);
//    }
//
//    @Override
//    @SideOnly(Side.CLIENT)
//    public IIcon getIcon(int side, int meta) {
//        return getIconFromSideAndMeta(side, 1); //pretend we have meta data 1 (it being active)
//    }
//
//    @Override
//    @SideOnly(Side.CLIENT)
//    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
//        return getIconFromSideAndMeta(side, world.getBlockMetadata(x, y, z));
//    }
//
//    @SideOnly(Side.CLIENT)
//    private IIcon getIconFromSideAndMeta(int side, int meta) {
//        return side == 0 || side == 1 || meta == 0 ? emptyIcon : activeIcon;
//    }

    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return new TileEntityBarrel();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (player != null) {
            if (player.inventory.getCurrentItem() == null) {
                if (!world.isRemote) {
                    TileEntity te = world.getTileEntity(pos);
                    if (te != null && te instanceof TileEntityBarrel) {
                        if (((TileEntityBarrel) te).getCurrentTask() != null && te.getBlockMetadata() == 1)
                            player.addChatComponentMessage(new TextComponentTranslation("tile.hqm:item_barrel.boundTo", Quest.getQuest(((TileEntityBarrel) te).selectedQuest).getName()));
                        else
                            player.addChatComponentMessage(new TextComponentTranslation("tile.hqm:item_barrel.nonBound"));
                    }
                }
                return true;
            } else if (player.inventory.getCurrentItem().getItem() == ModItems.book) {
                if (!world.isRemote) {
                    TileEntity te = world.getTileEntity(pos);
                    if (te != null && te instanceof TileEntityBarrel) {
                        ((TileEntityBarrel) te).storeSettings(player);

                        if (((TileEntityBarrel) te).getCurrentTask() != null)
                            player.addChatComponentMessage(new TextComponentTranslation("tile.hqm:item_barrel.bindTo", Quest.getQuest(((TileEntityBarrel) te).selectedQuest).getName()));
                        else
                            player.addChatComponentMessage(new TextComponentTranslation("hqm.message.noTaskSelected"));
                    }
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }


    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState blockState, World world, BlockPos pos) {
        if (world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos)) == 1) {
            return 15;
        } else {
            return 0;
        }
    }
}
