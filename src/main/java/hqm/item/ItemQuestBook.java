package hqm.item;


import hqm.client.gui.GuiQuestBook;
import hqm.quest.Questbook;
import hqm.quest.SaveHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * @author canitzp
 */
public class ItemQuestBook extends ItemBase<ItemQuestBook> {

    public ItemQuestBook(){
        super("quest_book");
        this.setMaxStackSize(1);
    }

    @SideOnly(Side.CLIENT)
    @Nonnull
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(world.isRemote){
            ItemStack stack = player.getHeldItem(hand);
            if(stack.hasTagCompound()){
                UUID uuid = stack.getTagCompound().getUniqueId("QuestbookId");
                if(SaveHandler.QUEST_DATA.containsKey(uuid)){
                    GuiQuestBook gui = new GuiQuestBook(uuid, player, stack);
                    Minecraft.getMinecraft().displayGuiScreen(gui);
                    try {
                        gui.tryToLoadPage(stack);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if(stack.hasTagCompound()){
            NBTTagCompound nbt = stack.getTagCompound();
            UUID uuid = nbt.getUniqueId("QuestbookId");
            if(SaveHandler.QUEST_DATA.containsKey(uuid)){
                Questbook questbook = SaveHandler.QUEST_DATA.get(uuid);
                tooltip.addAll(questbook.getTooltip());
                if(flagIn.isAdvanced()){
                    tooltip.add("Questbook ID: " + uuid);
                    if(nbt.hasKey("PageClass", Constants.NBT.TAG_STRING)){
                        tooltip.add("Current Page: " + nbt.getString("PageClass"));
                    }
                }
            }
        }
    }

    @Override
    public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
        if(this.isInCreativeTab(tab)){
            if(!SaveHandler.QUEST_DATA.isEmpty()){
                for(Questbook questbook : SaveHandler.QUEST_DATA.values()){
                    NBTTagCompound itemBookNbt = new NBTTagCompound();
                    itemBookNbt.setString("DisplayName", questbook.getName());
                    itemBookNbt.setUniqueId("QuestbookId", questbook.getId());
                    ItemStack book = new ItemStack(this);
                    book.setTagCompound(itemBookNbt);
                    items.add(book);
                }
            } else {
                super.getSubItems(tab, items);
            }
        }
    }

    @Nonnull
    @Override
    public String getItemStackDisplayName(@Nonnull ItemStack stack) {
        if(stack.hasTagCompound()){
            NBTTagCompound nbt = stack.getTagCompound();
            if(nbt.hasKey("DisplayName", Constants.NBT.TAG_STRING)){
                return nbt.getString("DisplayName");
            }
        }
        return super.getItemStackDisplayName(stack);
    }
}
