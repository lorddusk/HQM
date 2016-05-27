package hardcorequesting.items;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.client.interfaces.GuiColor;
import hardcorequesting.client.interfaces.GuiType;
import hardcorequesting.commands.CommandHandler;
import hardcorequesting.network.NetworkManager;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.util.Translator;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.UUID;

public class ItemQuestBook extends Item {

    public ItemQuestBook() {
        super();
        setCreativeTab(HardcoreQuesting.HQMTab);
        setMaxStackSize(1);
        setRegistryName(ItemInfo.BOOK_UNLOCALIZED_NAME);
        setUnlocalizedName(ItemInfo.LOCALIZATION_START + ItemInfo.BOOK_UNLOCALIZED_NAME);
    }



    @Override
    public String getUnlocalizedName(ItemStack itemStack) {
        return super.getUnlocalizedName(itemStack) + "_" + itemStack.getItemDamage();
    }

    private static final String NBT_PLAYER = "UseAsPlayer";

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(this.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(this, 1, new ModelResourceLocation(this.getRegistryName(), "inventory"));
    }

    @SuppressWarnings("unchecked")
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemStack, EntityPlayer player, List tooltip, boolean extraInfo) {
        if (itemStack.getItemDamage() == 1) {
            NBTTagCompound compound = itemStack.getTagCompound();
            if (compound != null && compound.hasKey(NBT_PLAYER)) {
                EntityPlayer useAsPlayer = QuestingData.getPlayer(compound.getString(NBT_PLAYER));
                tooltip.add(Translator.translate("item.hqm:quest_book_1.useAs", useAsPlayer == null ? "INVALID" : useAsPlayer.getDisplayNameString()));
            }
            else
                tooltip.add(GuiColor.RED + Translator.translate("item.hqm:quest_book_1.invalid"));
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack item, World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote && player instanceof EntityPlayerMP) {

            if (!QuestingData.isQuestActive()) {
                player.addChatComponentMessage(Translator.translateToIChatComponent("hqm.message.noQuestYet"));
            } else {
                if (item.getItemDamage() == 1) {
                    NBTTagCompound compound = item.getTagCompound();
                    if (compound != null && compound.hasKey(NBT_PLAYER)) {
                        String uuidS = compound.getString(NBT_PLAYER);
                        UUID uuid;
                        try {
                            uuid = UUID.fromString(uuidS);
                        } catch (IllegalArgumentException e) {
                            compound.removeTag(NBT_PLAYER);
                            return new ActionResult<>(EnumActionResult.FAIL, item);
                        }
                        if (QuestingData.hasData(uuid) && CommandHandler.isOwnerOrOp(player)) {
                            EntityPlayer subject = QuestingData.getPlayer(uuid);
                            if (subject instanceof EntityPlayerMP)
                                NetworkManager.sendToPlayer(GuiType.BOOK.build(Boolean.TRUE.toString()), (EntityPlayerMP) subject);
                            //player.addChatComponentMessage(Translator.translateToIChatComponent("hqm.message.alreadyEditing"));
                        } else {
                            player.addChatComponentMessage(Translator.translateToIChatComponent("hqm.message.bookNoPermission"));
                        }
                    }
                } else {
                    NetworkManager.sendToPlayer(GuiType.BOOK.build(Boolean.FALSE.toString()), (EntityPlayerMP) player);
                }
            }

        }

        return new ActionResult<>(EnumActionResult.SUCCESS, item);
    }

    @Override
    public boolean hasEffect(ItemStack itemStack) {
        return itemStack.getItemDamage() == 1;
    }

    public static ItemStack getOPBook(EntityPlayer player) {
        ItemStack itemStack = new ItemStack(ModItems.book, 1, 1);
        itemStack.setTagCompound(new NBTTagCompound());
        itemStack.getTagCompound().setString(NBT_PLAYER, player.getPersistentID().toString());
        return itemStack;
    }
}
