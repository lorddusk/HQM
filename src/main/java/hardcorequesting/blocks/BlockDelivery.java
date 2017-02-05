package hardcorequesting.blocks;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.items.ModItems;
import hardcorequesting.quests.Quest;
import hardcorequesting.tileentity.TileEntityBarrel;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class BlockDelivery extends BlockContainer {

    public BlockDelivery() {
        super(Material.WOOD);
        setRegistryName(BlockInfo.ITEMBARREL_UNLOCALIZED_NAME);
        setCreativeTab(HardcoreQuesting.HQMTab);
        setHardness(1f);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return new TileEntityBarrel();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (player != null) {
            if (player.inventory.getCurrentItem().isEmpty()) {
                if (!world.isRemote) {
                    TileEntity te = world.getTileEntity(pos);
                    if (te != null && te instanceof TileEntityBarrel) {
                        if (((TileEntityBarrel) te).getCurrentTask() != null/* && te.getBlockMetadata() == 1*/) // TODO: uncomment once states (with meta) are in
                            player.sendMessage(new TextComponentTranslation("tile.hqm:item_barrel.boundTo", Quest.getQuest(((TileEntityBarrel) te).selectedQuest).getName()));
                        else
                            player.sendMessage(new TextComponentTranslation("tile.hqm:item_barrel.nonBound"));
                    }
                }
                return true;
            } else if (player.inventory.getCurrentItem().getItem() == ModItems.book) {
                if (!world.isRemote) {
                    TileEntity te = world.getTileEntity(pos);
                    if (te != null && te instanceof TileEntityBarrel) {
                        ((TileEntityBarrel) te).storeSettings(player);

                        if (((TileEntityBarrel) te).getCurrentTask() != null)
                            player.sendMessage(new TextComponentTranslation("tile.hqm:item_barrel.bindTo", Quest.getQuest(((TileEntityBarrel) te).selectedQuest).getName()));
                        else
                            player.sendMessage(new TextComponentTranslation("hqm.message.noTaskSelected"));
                    }
                }
                return true;
            }
        }

        return false;
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

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }
}
