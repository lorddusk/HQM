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

    private static final String NBT_PLAYER = "UseAsPlayer";

    public ItemQuestBook() {
        super();
        setCreativeTab(HardcoreQuesting.HQMTab);
        setMaxStackSize(1);
        setRegistryName(ItemInfo.BOOK_UNLOCALIZED_NAME);
        setUnlocalizedName(ItemInfo.LOCALIZATION_START + ItemInfo.BOOK_UNLOCALIZED_NAME);
    }

    public static ItemStack getOPBook(EntityPlayer player) {
        ItemStack stack = new ItemStack(ModItems.book, 1, 1);
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString(NBT_PLAYER, player.getPersistentID().toString());
        stack.setTagCompound(nbt);
        return stack;
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(this.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(this, 1, new ModelResourceLocation(this.getRegistryName(), "inventory"));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote && player instanceof EntityPlayerMP) {
            ItemStack stack = player.getHeldItem(hand);
            if (!QuestingData.isQuestActive()) {
                player.sendMessage(Translator.translateToIChatComponent("hqm.message.noQuestYet"));
            } else {
                if (stack.getItemDamage() == 1) {
                    NBTTagCompound compound = stack.getTagCompound();
                    if (compound != null && compound.hasKey(NBT_PLAYER)) {
                        String uuidS = compound.getString(NBT_PLAYER);
                        UUID uuid;
                        try {
                            uuid = UUID.fromString(uuidS);
                        } catch (IllegalArgumentException e) {
                            compound.removeTag(NBT_PLAYER);
                            return new ActionResult<>(EnumActionResult.FAIL, stack);
                        }
                        if (QuestingData.hasData(uuid) && CommandHandler.isOwnerOrOp(player)) {
                            EntityPlayer subject = QuestingData.getPlayer(uuid);
                            if (subject instanceof EntityPlayerMP) {
                                QuestingData.getQuestingData(subject).getTeam().getEntry(subject.getUniqueID().toString()).setBookOpen(true);
                                NetworkManager.sendToPlayer(GuiType.BOOK.build(Boolean.TRUE.toString()), (EntityPlayerMP) subject);
                            }
                            //player.addChatComponentMessage(Translator.translateToIChatComponent("hqm.message.alreadyEditing"));
                        } else {
                            player.sendMessage(Translator.translateToIChatComponent("hqm.message.bookNoPermission"));
                        }
                    }
                } else {
                    QuestingData.getQuestingData(player).getTeam().getEntry(player.getUniqueID().toString()).setBookOpen(true);
                    NetworkManager.sendToPlayer(GuiType.BOOK.build(Boolean.FALSE.toString()), (EntityPlayerMP) player);
                }
            }

        }
        return super.onItemRightClick(world, player, hand);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return super.getUnlocalizedName(stack) + "_" + stack.getItemDamage();
    }

    @SuppressWarnings("unchecked")
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean extraInfo) {
        if (stack.getItemDamage() == 1) {
            NBTTagCompound compound = stack.getTagCompound();
            if (compound != null && compound.hasKey(NBT_PLAYER)) {
                EntityPlayer useAsPlayer = QuestingData.getPlayer(compound.getString(NBT_PLAYER));
                tooltip.add(Translator.translate("item.hqm:quest_book_1.useAs", useAsPlayer == null ? "INVALID" : useAsPlayer.getDisplayNameString()));
            } else
                tooltip.add(GuiColor.RED + Translator.translate("item.hqm:quest_book_1.invalid"));
        }
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return stack.getItemDamage() == 1;
    }
}
