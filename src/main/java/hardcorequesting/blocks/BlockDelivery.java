package hardcorequesting.blocks;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.items.ModItems;
import hardcorequesting.quests.Quest;
import hardcorequesting.tileentity.TileEntityBarrel;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockDelivery extends BlockContainer {

    public static final PropertyBool BOUND = PropertyBool.create("bound");

    public BlockDelivery() {
        super(Material.WOOD);
        setRegistryName(BlockInfo.ITEMBARREL_UNLOCALIZED_NAME);
        setCreativeTab(HardcoreQuesting.HQMTab);
        setHardness(1f);
        this.setDefaultState(getBlockState().getBaseState().withProperty(BOUND, false));
    }

    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return new TileEntityBarrel();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack stack, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (player != null) {
            if (player.inventory.getCurrentItem() == null) {
                if (!world.isRemote) {
                    TileEntity te = world.getTileEntity(pos);
                    if (te != null && te instanceof TileEntityBarrel) {
                        if (((TileEntityBarrel) te).getCurrentTask() != null && state.getValue(BOUND))
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

                        if (((TileEntityBarrel) te).getCurrentTask() != null){
                            player.addChatComponentMessage(new TextComponentTranslation("tile.hqm:item_barrel.bindTo", Quest.getQuest(((TileEntityBarrel) te).selectedQuest).getName()));
                            world.setBlockState(pos, this.getDefaultState().withProperty(BOUND, true), 3);
                        }
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

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(BOUND, meta == 1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(BOUND) ? 1 : 0;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BOUND);
    }

//    @Override
//    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
//        return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(BOUND, false);
//    }
}
