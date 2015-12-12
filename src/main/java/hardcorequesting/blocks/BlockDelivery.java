package hardcorequesting.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import hardcorequesting.HardcoreQuesting;
import hardcorequesting.Translator;
import hardcorequesting.items.ModItems;
import hardcorequesting.quests.Quest;
import hardcorequesting.tileentity.TileEntityBarrel;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockDelivery extends BlockContainer {

    @SideOnly(Side.CLIENT)
    private IIcon activeIcon;
    @SideOnly(Side.CLIENT)
    private IIcon emptyIcon;
    public BlockDelivery() {
        super(Material.wood);
        setBlockName(BlockInfo.LOCALIZATION_START + BlockInfo.ITEMBARREL_UNLOCALIZED_NAME);
        setCreativeTab(HardcoreQuesting.HQMTab);
        setHardness(1f);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister icon) {
        pickIcons(icon);
    }

    public void pickIcons(IIconRegister icon) {
        activeIcon = icon.registerIcon(BlockInfo.TEXTURE_LOCATION + ":" + BlockInfo.ITEMBARREL_ICON);
        emptyIcon = icon.registerIcon(BlockInfo.TEXTURE_LOCATION + ":" + BlockInfo.ITEMBARREL_ICON_EMPTY);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return getIconFromSideAndMeta(side, 1); //pretend we have meta data 1 (it being active)
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
        return getIconFromSideAndMeta(side, world.getBlockMetadata(x, y, z));
    }

    @SideOnly(Side.CLIENT)
    private IIcon getIconFromSideAndMeta(int side, int meta) {
        return side == 0 || side == 1 || meta == 0 ? emptyIcon : activeIcon;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return new TileEntityBarrel();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {

        if (player != null) {
            if (player.inventory.getCurrentItem() == null) {
                if (!world.isRemote) {
                    TileEntity te = world.getTileEntity(x, y, z);
                    if (te != null && te instanceof TileEntityBarrel) {
                        if (((TileEntityBarrel) te).getCurrentTask() != null && te.getBlockMetadata() == 1)
                            player.addChatComponentMessage(Translator.translateToIChatComponent("tile.hqm:item_barrel.boundTo", Quest.getQuest(((TileEntityBarrel) te).selectedQuest).getName()));
                        else
                            player.addChatComponentMessage(Translator.translateToIChatComponent("tile.hqm:item_barrel.nonBound"));
                    }
                }
                return true;
            } else if (player.inventory.getCurrentItem().getItem() == ModItems.book) {
                if (!world.isRemote) {
                    TileEntity te = world.getTileEntity(x, y, z);
                    if (te != null && te instanceof TileEntityBarrel) {
                        ((TileEntityBarrel) te).storeSettings(player);

                        if (((TileEntityBarrel) te).getCurrentTask() != null)
                            player.addChatComponentMessage(Translator.translateToIChatComponent("tile.hqm:item_barrel.bindTo", Quest.getQuest(((TileEntityBarrel) te).selectedQuest).getName()));
                        else
                            player.addChatComponentMessage(Translator.translateToIChatComponent("hqm.message.noTaskSelected"));
                    }
                }
                return true;
            }
        }

        return false;
    }


    public boolean hasComparatorInputOverride() {
        return true;
    }


    public int getComparatorInputOverride(World world, int x, int y, int z, int side) {
        return world.getBlockMetadata(x, y, z) == 1 ? 15 : 0;
    }
}
