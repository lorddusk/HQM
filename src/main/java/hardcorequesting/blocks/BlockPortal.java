package hardcorequesting.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import hardcorequesting.HardcoreQuesting;
import hardcorequesting.items.ModItems;
import hardcorequesting.quests.Quest;
import hardcorequesting.tileentity.PortalType;
import hardcorequesting.tileentity.TileEntityPortal;
import hardcorequesting.util.Translator;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

//import net.minecraft.client.renderer.texture.IIconRegister;
//import net.minecraft.util.IIcon;


public class BlockPortal extends BlockContainer {
    public BlockPortal() {
        super(Material.wood);
        setUnlocalizedName(BlockInfo.QUEST_PORTAL_UNLOCALIZED_NAME);
        setCreativeTab(HardcoreQuesting.HQMTab);
        setHardness(10f);
    }


    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return new TileEntityPortal();
    }

    @SideOnly(Side.CLIENT)
    private IIcon transparentIcon;
    @SideOnly(Side.CLIENT)
    private IIcon emptyIcon;
    @SideOnly(Side.CLIENT)
    private IIcon techIcon;
    @SideOnly(Side.CLIENT)
    private IIcon techEmptyIcon;
    @SideOnly(Side.CLIENT)
    private IIcon magicIcon;

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister icon) {
        pickIcons(icon);
    }

    @SideOnly(Side.CLIENT)
    private void pickIcons(IIconRegister icon) {
        blockIcon = icon.registerIcon(BlockInfo.TEXTURE_LOCATION + ":" + BlockInfo.QUEST_PORTAL_ICON);
        emptyIcon = icon.registerIcon(BlockInfo.TEXTURE_LOCATION + ":" + BlockInfo.QUEST_PORTAL_EMPTY_ICON);
        techIcon = icon.registerIcon(BlockInfo.TEXTURE_LOCATION + ":" + BlockInfo.QUEST_PORTAL_TECH_ICON);
        techEmptyIcon = icon.registerIcon(BlockInfo.TEXTURE_LOCATION + ":" + BlockInfo.QUEST_PORTAL_TECH_EMPTY_ICON);
        magicIcon = icon.registerIcon(BlockInfo.TEXTURE_LOCATION + ":" + BlockInfo.QUEST_PORTAL_MAGIC_ICON);
        transparentIcon = icon.registerIcon(BlockInfo.TEXTURE_LOCATION + ":" + BlockInfo.QUEST_PORTAL_TRANSPARENT_ICON);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (player != null && Quest.isEditing) {
            if (player.inventory.getCurrentItem() != null && player.inventory.getCurrentItem().getItem() == ModItems.book) {
                if (!world.isRemote) {
                    TileEntity te = world.getTileEntity(x, y, z);
                    if (te != null && te instanceof TileEntityPortal) {
                        ((TileEntityPortal) te).setCurrentQuest();
                        if (((TileEntityPortal) te).getCurrentQuest() != null)
                            player.addChatComponentMessage(Translator.translateToIChatComponent("tile.hqm:quest_portal_0.bindTo", ((TileEntityPortal) te).getCurrentQuest().getName()));
                        else
                            player.addChatComponentMessage(Translator.translateToIChatComponent("hqm.message.noTaskSelected"));
                    }
                }
                return true;
            } else {
                if (!world.isRemote) {
                    TileEntity te = world.getTileEntity(x, y, z);
                    if (te != null && te instanceof TileEntityPortal)
                        ((TileEntityPortal) te).openInterface(player);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB entityBox, List collidingBoxes, Entity entity) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (entity instanceof EntityPlayer && te instanceof TileEntityPortal && !((TileEntityPortal) te).hasCollision((EntityPlayer) entity))
            return;
        super.addCollisionBoxesToList(world, x, y, z, entityBox, collidingBoxes, entity);
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean isBlockNormalCube() {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public final IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityPortal) {
            TileEntityPortal portal = ((TileEntityPortal) te);
            if (!portal.hasTexture(Minecraft.getMinecraft().thePlayer))
                return transparentIcon;
            else if (portal.getType().isPreset())
                return getPresetIcon(portal.getType(), side);
            else {
                IIcon icon = portal.getBlockIcon(side);
                if (icon != null)
                    return icon;
            }
        }
        return getIcon(side, 0);
    }

    @SideOnly(Side.CLIENT)
    private IIcon getPresetIcon(PortalType preset, int side) {
        return preset == PortalType.TECH ? side == 0 || side == 1 ? techEmptyIcon : techIcon : magicIcon;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int meta) {
        if (meta == 1 || meta == 2)
            return getPresetIcon(meta == 1 ? PortalType.TECH : PortalType.MAGIC, side);
        return side == 0 || side == 1 ? emptyIcon : blockIcon;
    }


    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te != null && te instanceof TileEntityPortal) {
            TileEntityPortal portal = (TileEntityPortal) te;
            ItemStack itemStack = super.getPickBlock(target, world, x, y, z, player);
            if (itemStack != null) {
                NBTTagCompound tagCompound = itemStack.getTagCompound();
                if (tagCompound == null) {
                    tagCompound = new NBTTagCompound();
                    itemStack.setTagCompound(tagCompound);
                }

                NBTTagCompound info = new NBTTagCompound();
                tagCompound.setTag("Portal", info);
                portal.writeContentToNBT(info);
            }
            return itemStack;
        }
        return null;
    }


    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack itemStack) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te != null && te instanceof TileEntityPortal) {
            TileEntityPortal manager = (TileEntityPortal) te;
            if (itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("Portal"))
                manager.readContentFromNBT(itemStack.getTagCompound().getCompoundTag("Portal"));
        }
    }
}
