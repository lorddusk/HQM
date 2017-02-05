package hardcorequesting.blocks;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.items.ModItems;
import hardcorequesting.quests.Quest;
import hardcorequesting.tileentity.TileEntityPortal;
import hardcorequesting.util.Translator;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.List;

public class BlockPortal extends BlockContainer {

    public BlockPortal() {
        super(Material.WOOD);
        setRegistryName(BlockInfo.QUEST_PORTAL_UNLOCALIZED_NAME);
        setCreativeTab(HardcoreQuesting.HQMTab);
        setHardness(10f);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return new TileEntityPortal();
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entity, boolean b) {
        TileEntity te = world.getTileEntity(pos);
        if (entity instanceof EntityPlayer && te instanceof TileEntityPortal && !((TileEntityPortal) te).hasCollision((EntityPlayer) entity))
            return;
        super.addCollisionBoxToList(state, world, pos, entityBox, collidingBoxes, entity, b);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (player != null && Quest.isEditing) {
            if (!player.inventory.getCurrentItem().isEmpty() && player.inventory.getCurrentItem().getItem() == ModItems.book) {
                if (!world.isRemote) {
                    TileEntity tile = world.getTileEntity(pos);
                    if (tile != null && tile instanceof TileEntityPortal) {
                        ((TileEntityPortal) tile).setCurrentQuest();
                        if (((TileEntityPortal) tile).getCurrentQuest() != null)
                            player.sendMessage(Translator.translateToIChatComponent("tile.hqm:quest_portal_0.bindTo", ((TileEntityPortal) tile).getCurrentQuest().getName()));
                        else
                            player.sendMessage(Translator.translateToIChatComponent("hqm.message.noTaskSelected"));
                    }
                }
                return true;
            } else {
                if (!world.isRemote) {
                    TileEntity tile = world.getTileEntity(pos);
                    if (tile != null && tile instanceof TileEntityPortal)
                        ((TileEntityPortal) tile).openInterface(player);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
        TileEntity te = world.getTileEntity(pos);
        if (te != null && te instanceof TileEntityPortal) {
            TileEntityPortal manager = (TileEntityPortal) te;
            if (stack.hasTagCompound() && stack.getTagCompound().hasKey("Portal", Constants.NBT.TAG_COMPOUND))
                manager.readContentFromNBT(stack.getTagCompound().getCompoundTag("Portal"));
        }
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        TileEntity te = world.getTileEntity(pos);
        if (te != null && te instanceof TileEntityPortal) {
            TileEntityPortal portal = (TileEntityPortal) te;
            ItemStack stack = super.getPickBlock(state, target, world, pos, player);
            if (!stack.isEmpty()) {
                NBTTagCompound tagCompound = stack.getTagCompound();
                if (tagCompound == null) {
                    tagCompound = new NBTTagCompound();
                    stack.setTagCompound(tagCompound);
                }

                NBTTagCompound info = new NBTTagCompound();
                tagCompound.setTag("Portal", info);
                portal.writeContentToNBT(info);
            }
            return stack;
        }
        return null;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }
}
