package hardcorequesting.api.reward;

import hardcorequesting.api.IQuest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class ItemStackReward extends DefaultReward{
    
    @Nonnull private ItemStack stack = ItemStack.EMPTY;
    
    @Override
    public void onCreation(@Nonnull IQuest quest, @Nonnull NBTTagCompound additionalData){
        super.onCreation(quest, additionalData);
        // todo get itemstack from nbt
    }
    
    @Nonnull
    @Override
    public NBTTagCompound getAdditionalData(){
        NBTTagCompound nbt = super.getAdditionalData();
        // todo itemstack to nbt
        return nbt;
    }
    
    @Nonnull
    public ItemStack getStack(){
        return stack;
    }
    
    @Override
    public void giveToPlayer(@Nonnull EntityPlayer player){
        if(!player.inventory.addItemStackToInventory(this.getStack())){ // eg a full inventory
            player.entityDropItem(this.getStack(), 0.0F);
        }
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void render(){
        //todo rendering code
    }
}
