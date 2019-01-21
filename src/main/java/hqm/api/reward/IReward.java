package hqm.api.reward;

import hqm.api.IQuest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public interface IReward{
    
    void onCreation(@Nonnull IQuest quest, @Nonnull NBTTagCompound additionalData);
    
    @Nonnull
    IQuest getQuest();
    
    @Nonnull
    NBTTagCompound getAdditionalData();
    
    void giveToPlayer(@Nonnull EntityPlayer player);
    
    // todo add parameter
    @SideOnly(Side.CLIENT)
    void render();
    
}
