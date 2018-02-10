package hqm.item;


import hqm.client.gui.GuiQuestBook;
import hqm.quest.Questbook;
import hqm.quest.SaveHandler;
import net.minecraft.client.Minecraft;
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

import javax.annotation.Nonnull;

/**
 * @author canitzp
 */
public class ItemQuestBook extends ItemBase<ItemQuestBook> {

    public ItemQuestBook(){
        super("quest_book");
        this.setMaxStackSize(1);
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(world.isRemote){
            Minecraft.getMinecraft().displayGuiScreen(new GuiQuestBook());
        }
        return EnumActionResult.SUCCESS;
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
