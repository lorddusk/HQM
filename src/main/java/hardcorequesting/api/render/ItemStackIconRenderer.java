package hardcorequesting.api.render;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class ItemStackIconRenderer implements ICustomIconRenderer{
    
    private static final Map<ItemStack, ItemStackIconRenderer> STACK_CACHE = new HashMap<>();
    
    @Nonnull private ItemStack stack = ItemStack.EMPTY;
    
    @SideOnly(Side.CLIENT)
    @Override
    public void render(){
        // todo write rendering code
    }
    
    @Override
    public String getClassName(){
        return this.getClass().getName();
    }
    
    @Override
    public NBTTagCompound getData(){
        NBTTagCompound data = new NBTTagCompound();
        NBTTagCompound stack = new NBTTagCompound();
        this.stack.writeToNBT(stack);
        data.setTag("Stack", data);
        return data;
    }
    
    @Override
    public void onLoad(NBTTagCompound nbt){
        this.stack = new ItemStack(nbt.getCompoundTag("Stack"));
    }
    
    public void setItemStack(@Nonnull ItemStack stack){
        this.stack = stack;
    }
    
    @Nonnull
    public static ItemStackIconRenderer getRendererForItemStack(@Nonnull ItemStack stack){
        for(Map.Entry<ItemStack, ItemStackIconRenderer> entry : STACK_CACHE.entrySet()){
            if(ItemStack.areItemStacksEqual(entry.getKey(), stack)){
                return entry.getValue();
            }
        }
        ItemStackIconRenderer renderer = new ItemStackIconRenderer();
        renderer.setItemStack(stack);
        STACK_CACHE.put(stack, renderer);
        return renderer;
    }
}
