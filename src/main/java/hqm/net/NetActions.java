package hqm.net;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import java.util.function.BiConsumer;

/**
 * @author canitzp
 */
public enum NetActions {

    STACK_ADD_NBT((data, sender, receiver) -> {
        if(receiver.getUniqueID() == sender.getUniqueID()){
            if(data.hasKey("CurrentSlot", Constants.NBT.TAG_INT) && data.hasKey("Data", Constants.NBT.TAG_COMPOUND)){
                int slot = data.getInteger("CurrentSlot");
                ItemStack stack = receiver.inventory.getStackInSlot(slot);
                if(stack.hasTagCompound()){
                    stack.getTagCompound().merge(data.getCompoundTag("Data"));
                } else {
                    stack.setTagCompound(data.getCompoundTag("Data"));
                }
            }
        }
    });

    private TriConsumer<NBTTagCompound, EntityPlayer, EntityPlayer> action;

    NetActions(TriConsumer<NBTTagCompound, EntityPlayer, EntityPlayer> action){
        this.action = action;
    }

    public void action(NBTTagCompound data, EntityPlayer sender, EntityPlayer receiver){
        this.action.accept(data, sender, receiver);
    }

    public static NetActions get(int i){
        if(NetActions.values().length >= i){
            return NetActions.values()[i];
        }
        return null;
    }

}
