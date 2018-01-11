package hqm.item;


import hqm.client.gui.GuiQuestBook;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
}
