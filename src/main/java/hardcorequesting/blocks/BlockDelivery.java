package hardcorequesting.blocks;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.items.ItemQuestBook;
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
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockDelivery extends BlockContainer {

    public static final PropertyBool BOUND = PropertyBool.create("bound");

    public BlockDelivery() {
        super(Material.WOOD);
        this.setRegistryName(BlockInfo.ITEMBARREL_UNLOCALIZED_NAME);
        this.setCreativeTab(HardcoreQuesting.HQMTab);
        this.setHardness(1.0F);
        this.setHarvestLevel("axe", 0);
        this.setDefaultState(getBlockState().getBaseState().withProperty(BOUND, false));
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(@Nonnull World world, int i) {
        return new TileEntityBarrel();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if(!world.isRemote){
            ItemStack hold = player.inventory.getCurrentItem();
            TileEntity tile = world.getTileEntity(pos);
            if(tile instanceof TileEntityBarrel){
                if(!hold.isEmpty() && hold.getItem() instanceof ItemQuestBook){
                    ((TileEntityBarrel) tile).storeSettings(player);
                    if(((TileEntityBarrel) tile).getCurrentTask() != null){
                        player.sendMessage(new TextComponentTranslation("tile.hqm:item_barrel.bindTo", Quest.getQuest(((TileEntityBarrel) tile).getQuestUUID()).getName()));
                    } else {
                        player.sendMessage(new TextComponentTranslation("hqm.message.noTaskSelected"));
                    }
                } else {
                    if(((TileEntityBarrel) tile).getCurrentTask() != null){
                        player.sendMessage(new TextComponentTranslation("tile.hqm:item_barrel.boundTo", Quest.getQuest(((TileEntityBarrel) tile).getQuestUUID()).getName()));
                    } else {
                        player.sendMessage(new TextComponentTranslation("tile.hqm:item_barrel.nonBound"));
                    }
                }
            }
        }
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getComparatorInputOverride(IBlockState blockState, World world, BlockPos pos) {
        if (world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos)) == 1) {
            return 15;
        } else {
            return 0;
        }
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(BOUND, meta == 1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(BOUND) ? 1 : 0;
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BOUND);
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(BOUND, false);
    }
}
