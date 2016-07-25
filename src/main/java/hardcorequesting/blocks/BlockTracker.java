package hardcorequesting.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import hardcorequesting.HardcoreQuesting;
import hardcorequesting.items.ModItems;
import hardcorequesting.quests.Quest;
import hardcorequesting.tileentity.TileEntityTracker;
import hardcorequesting.util.Translator;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockTracker extends BlockContainer {

    public BlockTracker() {
        super(Material.wood);
        setUnlocalizedName(BlockInfo.QUEST_TRACKER_UNLOCALIZED_NAME);
        setCreativeTab(HardcoreQuesting.HQMTab);
        setHardness(10f);
    }

    @SideOnly(Side.CLIENT)
    private IIcon activeIcon;
    @SideOnly(Side.CLIENT)
    private IIcon emptyIcon;

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister icon) {
        pickIcons(icon);
    }

    @SideOnly(Side.CLIENT)
    private void pickIcons(IIconRegister icon) {
        activeIcon = icon.registerIcon(BlockInfo.TEXTURE_LOCATION + ":" + BlockInfo.QUEST_TRACKER_ICON);
        emptyIcon = icon.registerIcon(BlockInfo.TEXTURE_LOCATION + ":" + BlockInfo.QUEST_TRACKER_ICON_EMPTY);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return side == 0 || side == 1 ? emptyIcon : activeIcon;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int var2) {
        return new TileEntityTracker();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (player != null) {
            if (player.inventory.getCurrentItem() != null && player.inventory.getCurrentItem().getItem() == ModItems.book) {
                if (!world.isRemote) {
                    TileEntity te = world.getTileEntity(x, y, z);
                    if (te != null && te instanceof TileEntityTracker) {
                        if (!Quest.isEditing) {
                            player.addChatMessage(Translator.translateToIChatComponent("tile.hqm:quest_tracker.offLimit"));
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
                    TileEntity te = world.getTileEntity(x, y, z);
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

    @Override
    public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int side) {
        return world.getBlockMetadata(x, y, z);
    }

    @Override
    public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side) {
        return world.getBlockMetadata(x, y, z);
    }

}
