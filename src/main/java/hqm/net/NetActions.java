package hqm.net;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants;

/**
 * @author canitzp
 */
public enum NetActions {

    STACK_ADD_NBT((data, sender, receiver) -> {
        if(receiver.getUniqueID() == sender.getUniqueID()){
            if(data.contains("CurrentSlot", Constants.NBT.TAG_INT) && data.contains("Data", Constants.NBT.TAG_COMPOUND)){
                int slot = data.getInt("CurrentSlot");
                ItemStack stack = receiver.inventory.getStackInSlot(slot);
                if(stack.hasTag()){
                    stack.getTag().merge(data.getCompound("Data"));
                } else {
                    stack.setTag(data.getCompound("Data"));
                }
            }
        }
    });

    private TriConsumer<CompoundNBT, PlayerEntity, PlayerEntity> action;

    NetActions(TriConsumer<CompoundNBT, PlayerEntity, PlayerEntity> action){
        this.action = action;
    }

    public void action(CompoundNBT data, PlayerEntity sender, PlayerEntity receiver){
        this.action.accept(data, sender, receiver);
    }

    public static NetActions get(int i){
        if(NetActions.values().length >= i){
            return NetActions.values()[i];
        }
        return null;
    }

}
