package hqm.client;

import hqm.api.render.ICustomIconRenderer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class EmptyIconRenderer implements ICustomIconRenderer{
    
    private static final EmptyIconRenderer INSTANCE = new EmptyIconRenderer();
    
    @SideOnly(Side.CLIENT)
    @Override
    public void render(){}
    
    @Override
    public String getClassName(){
        return this.getClass().getName();
    }
    
    @Override
    public NBTTagCompound getData(){
        return new NBTTagCompound();
    }
    
    @Override
    public void onLoad(NBTTagCompound nbt){}
    
    @Nonnull
    public static EmptyIconRenderer get(){
        return INSTANCE;
    }
}
