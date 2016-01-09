package hardcorequesting.blocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import hardcorequesting.HardcoreQuesting;
import hardcorequesting.Translator;
import hardcorequesting.config.ModConfig;
import hardcorequesting.items.ModItems;
import hardcorequesting.quests.Quest;
import hardcorequesting.tileentity.TileEntityTracker;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
//import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
//import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;


public class BlockTracker extends BlockContainer {

    public BlockTracker() {
        super(Material.wood);
        setRegistryName(BlockInfo.LOCALIZATION_START + BlockInfo.QUEST_TRACKER_UNLOCALIZED_NAME);
        setCreativeTab(HardcoreQuesting.HQMTab);
        setHardness(10f);
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
//    private void pickIcons(IIconRegister icon) {
//        activeIcon = icon.registerIcon(BlockInfo.TEXTURE_LOCATION + ":" + BlockInfo.QUEST_TRACKER_ICON);
//        emptyIcon = icon.registerIcon(BlockInfo.TEXTURE_LOCATION + ":" + BlockInfo.QUEST_TRACKER_ICON_EMPTY);
//    }
//
//    @Override
//    @SideOnly(Side.CLIENT)
//    public IIcon getIcon(int side, int meta) {
//        return side == 0 || side == 1 ? emptyIcon : activeIcon;
//    }

    @Override
    public TileEntity createNewTileEntity(World world, int var2) {
        return new TileEntityTracker();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float par7, float par8, float par9) {
        if (player != null) {
            if (player.inventory.getCurrentItem() != null && player.inventory.getCurrentItem().getItem() == ModItems.book) {
                if (!world.isRemote) {
                    TileEntity te = world.getTileEntity(pos);
                    if (te != null && te instanceof TileEntityTracker) {
                        if (!Quest.isEditing) {
                            player.addChatMessage(Translator.translateToIChatComponent("tile.hqm:quest_tracker.offLimit"));
                            ;
                        } else {
                            ((TileEntityTracker) te).setCurrentQuest();
                            if (((TileEntityTracker) te).getCurrentQuest() != null) {
                                player.addChatMessage(Translator.translateToIChatComponent("tile.hqm:quest_tracker.bindTo", ((TileEntityTracker) te).getCurrentQuest().getName()));
                            } else {
                                player.addChatMessage(Translator.translateToIChatComponent("hqm.message.noTaskSelected"));
                            }
                        }

                    }
                }
                return true;
            } else {
                if (!world.isRemote) {
                    TileEntity te = world.getTileEntity(pos);
                    if (te != null && te instanceof TileEntityTracker) {
                        if (!Quest.isEditing) {
                            player.addChatMessage(Translator.translateToIChatComponent("tile.hqm:quest_tracker.offLimit"));
                        } else {
                            ((TileEntityTracker) te).openInterface(player);
                        }
                    }
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean canProvidePower() {
        return true;
    }

//    @Override
//    public int isProvidingStrongPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {
//        return state.getBlock().getMetaFromState(state);
//    }
//
//    @Override
//    public int isProvidingWeakPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {
//        return state.getBlock().getMetaFromState(state);
//    }
}
