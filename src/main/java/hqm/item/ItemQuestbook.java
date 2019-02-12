package hqm.item;

import hqm.HQM;
import hqm.api.DefaultQuestbook;
import hqm.api.IQuestbook;
import hqm.io.IOHandler;
import hqm.net.NetManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ItemQuestbook extends Item{
    
    public ItemQuestbook(){
        this.setRegistryName(HQM.MODID, "questbook");
        this.setTranslationKey(this.getRegistryName().toString());
        this.setCreativeTab(HQM.HQM_TAB);
        this.setMaxStackSize(1);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn){
        NBTTagCompound nbt = stack.getTagCompound();
        if(nbt != null && nbt.hasKey("QuestbookMost", Constants.NBT.TAG_LONG) && nbt.hasKey("QuestbookLeast", Constants.NBT.TAG_LONG)){
            IQuestbook questbook = IOHandler.getQuestbook(nbt.getUniqueId("Questbook"));
            if(questbook != null){
                if(questbook.getTooltipTranslationKey() != null && !questbook.getTooltipTranslationKey().isEmpty()){
                    tooltip.add(I18n.format(questbook.getTooltipTranslationKey()));
                } else {
                    tooltip.add("UUID: '" + questbook.getUUID() + "'");
                    if(questbook.getAuthor() != null){
                        tooltip.add("Author: " + questbook.getAuthor());
                    }
                }
            } else {
                tooltip.add("Invalid questbook! There is no data for '" + nbt.getUniqueId("Questbook") + "'. Maybe you removed the questfiles.");
            }
        } else {
            tooltip.add("Blank questbook. A UUID is assigned when you open the book and so is a new Story created <3.");
        }
    }
    
    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand){
        ItemStack held = player.getHeldItem(hand);
        if(!world.isRemote){
            NBTTagCompound nbt = held.getTagCompound();
            if(nbt != null && nbt.hasKey("QuestbookMost", Constants.NBT.TAG_LONG) && nbt.hasKey("QuestbookLeast", Constants.NBT.TAG_LONG)){
                IQuestbook questbook = IOHandler.getQuestbook(nbt.getUniqueId("Questbook"));
                if(questbook != null){
                    NetManager.requestQuestbookOpening(player, questbook.getUUID());
                } else {
                    player.sendMessage(new TextComponentString("The UUID '" + nbt.getUniqueId("Questbook") + "' cannot be associated with any data."));
                }
            } else {
                nbt = new NBTTagCompound();
                IQuestbook questbook = this.createQuestbook();
                nbt.setUniqueId("Questbook", questbook.getUUID());
                held.setTagCompound(nbt);
                IOHandler.addQuestbook(questbook);
                IOHandler.writeQuestbook(questbook);
                player.sendMessage(new TextComponentString("A new story was created. UUID: '" + questbook.getUUID() + "'"));
            }
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, held);
    }
    
    protected IQuestbook createQuestbook(){
        DefaultQuestbook questbook = new DefaultQuestbook();
        questbook.onCreation(UUID.randomUUID(), new NBTTagCompound(), new ArrayList<>());
        return questbook;
    }
    
    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items){}
}
