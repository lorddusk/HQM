package hqm.item;


import hqm.HQM;
import hqm.client.gui.GuiQuestBook;
import hqm.quest.Questbook;
import hqm.quest.SaveHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * @author canitzp
 */
public class ItemQuestBook extends ItemBase<ItemQuestBook> {

    public ItemQuestBook(){
        super("quest_book", () -> new Properties().maxStackSize(1));
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context){
        if(context.getWorld().isRemote()){
            ItemStack stack = context.getPlayer().getHeldItem(context.getHand());
            if(stack.hasTag()){
                UUID uuid = stack.getTag().getUniqueId("QuestbookId");
                if(SaveHandler.QUEST_DATA.containsKey(uuid)){
                    GuiQuestBook gui = new GuiQuestBook(uuid, context.getPlayer(), stack);
                    Minecraft.getInstance().displayGuiScreen(gui);
                    try {
                        gui.tryToLoadPage(stack);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return ActionResultType.SUCCESS;
    }
    
    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag){
        if(stack.hasTag()){
            CompoundNBT nbt = stack.getTag();
            UUID uuid = nbt.getUniqueId("QuestbookId");
            if(SaveHandler.QUEST_DATA.containsKey(uuid)){
                Questbook questbook = SaveHandler.QUEST_DATA.get(uuid);
                //tooltip.addAll(questbook.getTooltip());
                if(flag.isAdvanced()){
                    tooltip.add(new StringTextComponent("Questbook ID: " + uuid));
                    if(nbt.contains("PageClass", Constants.NBT.TAG_STRING)){
                        tooltip.add(new StringTextComponent("Current Page: " + nbt.getString("PageClass")));
                    }
                }
            }
        }
    }
    
    @Override
    public void fillItemGroup(ItemGroup tab, NonNullList<ItemStack> items){
        if(this.isInGroup(tab)){
            if(!SaveHandler.QUEST_DATA.isEmpty()){
                for(Questbook questbook : SaveHandler.QUEST_DATA.values()){
                    CompoundNBT itemBookNbt = new CompoundNBT();
                    itemBookNbt.putString("DisplayName", questbook.getName());
                    itemBookNbt.putUniqueId("QuestbookId", questbook.getId());
                    ItemStack book = new ItemStack(this);
                    book.setTag(itemBookNbt);
                    items.add(book);
                }
            } else {
                super.fillItemGroup(tab, items);
            }
        }
    }
    
    @Override
    public ITextComponent getDisplayName(ItemStack stack){
        if(stack.hasTag()){
            CompoundNBT nbt = stack.getTag();
            if(nbt.contains("DisplayName", Constants.NBT.TAG_STRING)){
                return new StringTextComponent(nbt.getString("DisplayName"));
            }
        }
        return super.getDisplayName(stack);
    }
    
}
