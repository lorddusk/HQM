package hardcorequesting.items;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import hardcorequesting.HardcoreQuesting;
import hardcorequesting.QuestingData;
import hardcorequesting.Translator;
import hardcorequesting.client.interfaces.GuiColor;
import hardcorequesting.commands.CommandHandler;
import hardcorequesting.network.PacketHandler;
//import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
//import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.List;

public class ItemQuestBook extends Item {

    public ItemQuestBook() {
        super();
        setCreativeTab(HardcoreQuesting.HQMTab);
        setMaxStackSize(1);
        setUnlocalizedName(ItemInfo.LOCALIZATION_START + ItemInfo.BOOK_UNLOCALIZED_NAME);
    }

    @Override
    public String getUnlocalizedName(ItemStack itemStack) {
        return super.getUnlocalizedName(itemStack) + "_" + itemStack.getItemDamage();
    }

//    @SideOnly(Side.CLIENT)
//    private IIcon opIcon;
//
//    @Override
//    @SideOnly(Side.CLIENT)
//    public void registerIcons(IIconRegister register) {
//        pickIcons(register);
//
//    }
//
//    private void pickIcons(IIconRegister register) {
//        itemIcon = register.registerIcon(ItemInfo.TEXTURE_LOCATION + ":" + ItemInfo.BOOK_ICON);
//        opIcon = register.registerIcon(ItemInfo.TEXTURE_LOCATION + ":" + ItemInfo.BOOK_OP_ICON);
//    }
//
//
//    @Override
//    @SideOnly(Side.CLIENT)
//    public IIcon getIconFromDamage(int dmg) {
//        return dmg == 1 ? opIcon : itemIcon;
//    }

    private static final String NBT_PLAYER = "UseAsPlayer";

    @SuppressWarnings("unchecked")
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemStack, EntityPlayer player, List tooltip, boolean extraInfo) {
        if (itemStack.getItemDamage() == 1) {
            NBTTagCompound compound = itemStack.getTagCompound();
            if (compound != null && compound.hasKey(NBT_PLAYER))
                tooltip.add(Translator.translate("item.hqm:quest_book_1.useAs"));
            else
                tooltip.add(GuiColor.RED + Translator.translate("item.hqm:quest_book_1.invalid"));
        }
    }

    @Override
    public ItemStack onItemRightClick(ItemStack item, World world, EntityPlayer player) {
        if (!world.isRemote) {

            if (!QuestingData.isQuestActive()) {
                player.addChatComponentMessage(Translator.translateToIChatComponent("hqm.message.noQuestYet"));
            } else {
                if (item.getItemDamage() == 1) {
                    NBTTagCompound compound = item.getTagCompound();
                    if (compound != null && compound.hasKey(NBT_PLAYER)) {
                        String name = compound.getString(NBT_PLAYER);
                        if (QuestingData.hasData(name) && CommandHandler.isOwnerOrOp(player)) {
                            if (PacketHandler.canOverride(name))
                                QuestingData.getQuestingData(name).sendDataToClientAndOpenInterface(player, name);
                            else
                                player.addChatComponentMessage(Translator.translateToIChatComponent("hqm.message.alreadyEditing"));
                        } else {
                            player.addChatComponentMessage(Translator.translateToIChatComponent("hqm.message.bookNoPermission"));
                        }
                    }
                } else {
                    QuestingData.getQuestingData(player).sendDataToClientAndOpenInterface(player, null);
                }
            }

        }

        return item;
    }

    @Override
    public boolean hasEffect(ItemStack itemStack) {
        return itemStack.getItemDamage() == 1;
    }

    public static ItemStack getOPBook(String name) {
        ItemStack itemStack = new ItemStack(ModItems.book, 1, 1);
        itemStack.setTagCompound(new NBTTagCompound());
        itemStack.getTagCompound().setString(NBT_PLAYER, name);
        return itemStack;
    }
}
