package hardcorequesting.items;

import hardcorequesting.client.interfaces.GuiColor;
import hardcorequesting.quests.Quest;
import hardcorequesting.tileentity.PortalType;
import hardcorequesting.tileentity.TileEntityPortal;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;


public class ItemBlockPortal extends ItemBlock {

    public ItemBlockPortal(Block block) {
        super(block);
        block.setBlockUnbreakable();
        block.setResistance(6000000.0F);
    }

    private String formatBoolean(boolean val) {
        return (val ? GuiColor.GREEN : GuiColor.GRAY) + String.valueOf(val);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return super.getUnlocalizedName(stack) + "_" + stack.getItemDamage();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean useExtraInfo) {
        try {
            if (stack != null && stack.hasTagCompound() && stack.getTagCompound().hasKey("Portal", Constants.NBT.TAG_COMPOUND)) {
                NBTTagCompound compound = stack.getTagCompound().getCompoundTag("Portal");
                if (compound.hasKey(TileEntityPortal.NBT_QUEST)) {
                    Quest quest = Quest.getQuest(compound.getString(TileEntityPortal.NBT_QUEST));
                    if (quest != null) {
                        tooltip.add(GuiColor.GREEN + "Quest: " + quest.getName());
                    } else {
                        tooltip.add(GuiColor.GRAY + "No quest selected.");
                    }
                } else {
                    tooltip.add(GuiColor.GRAY + "No quest selected.");
                }

                PortalType type = PortalType.values()[compound.getByte(TileEntityPortal.NBT_TYPE)];
                tooltip.add(GuiColor.ORANGE + "Type: " + type.getName());
                if (!type.isPreset()) {
                    if (compound.hasKey(TileEntityPortal.NBT_ID)) {
                        int id = compound.getShort(TileEntityPortal.NBT_ID);
                        int dmg = compound.getShort(TileEntityPortal.NBT_DMG);

                        tooltip.add(GuiColor.YELLOW + "Item: " + new ItemStack(Item.getItemById(id), 1, dmg).getDisplayName());
                    } else {
                        tooltip.add(GuiColor.GRAY + "No fluidStack selected.");
                    }
                }

                boolean completedCollision, completedTexture, uncompletedCollision, uncompletedTexture;
                if (compound.hasKey(TileEntityPortal.NBT_COLLISION)) {
                    completedCollision = compound.getBoolean(TileEntityPortal.NBT_COLLISION);
                    completedTexture = compound.getBoolean(TileEntityPortal.NBT_COLLISION);
                    uncompletedCollision = compound.getBoolean(TileEntityPortal.NBT_NOT_COLLISION);
                    uncompletedTexture = compound.getBoolean(TileEntityPortal.NBT_NOT_TEXTURES);
                } else {
                    completedCollision = completedTexture = false;
                    uncompletedCollision = uncompletedTexture = true;
                }

                tooltip.add(" ");
                tooltip.add("Completed collision: " + formatBoolean(completedCollision));
                tooltip.add("Completed textures: " + formatBoolean(completedTexture));
                tooltip.add("Uncompleted collision: " + formatBoolean(uncompletedCollision));
                tooltip.add("Uncompleted textures: " + formatBoolean(uncompletedTexture));

            }
        } catch (Exception ignored) {
        } //just to make sure it doesn't crash because it tries to get some weird quests or items or whatever
    }
}
