package hardcorequesting.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import hardcorequesting.HardcoreQuesting;
import hardcorequesting.config.ModConfig;
import hardcorequesting.items.ModItems;
import hardcorequesting.quests.Quest;
import hardcorequesting.tileentity.TileEntityTracker;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;


public class BlockTracker extends BlockContainer {

    public BlockTracker() {
        super(Material.wood);
        setBlockName(BlockInfo.LOCALIZATION_START + BlockInfo.QUEST_TRACKER_UNLOCALIZED_NAME);
        setCreativeTab(HardcoreQuesting.HQMTab);
        setHardness(10f);
    }

    @SideOnly(Side.CLIENT)
    private IIcon activeIcon;
    @SideOnly(Side.CLIENT)
    private IIcon emptyIcon;

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister icon) {
        pickIcons(icon);
    }

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
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
        if (player != null) {
            if (player.inventory.getCurrentItem() != null && player.inventory.getCurrentItem().getItem() == ModItems.book) {
                if (!world.isRemote) {
                    TileEntity te = world.getTileEntity(x, y, z);
                    if (te != null && te instanceof TileEntityTracker) {
                        if (!Quest.isEditing) {
                            player.addChatMessage(new ChatComponentText("You're not in edit mode. This block is off limit."));
                        } else {
                            ((TileEntityTracker) te).setCurrentQuest();
                            if (((TileEntityTracker) te).getCurrentQuest() != null) {
                                player.addChatMessage(new ChatComponentText("You bound '" + ((TileEntityTracker) te).getCurrentQuest().getName() + "' to the QTS"));
                            } else {
                                player.addChatMessage(new ChatComponentText("You currently have not selected any quest"));
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
                            player.addChatMessage(new ChatComponentText("You're not in edit mode. This block is off limit."));
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
