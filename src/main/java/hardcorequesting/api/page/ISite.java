package hardcorequesting.api.page;

import hardcorequesting.api.IQuestbook;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public interface ISite{
    
    void onCreation(IQuestbook questbook, @Nonnull ItemStack stack);
    
    IQuestbook getQuestbook();
    
    @Nonnull
    ItemStack getQuestbookStack();
    
    //todo
    @SideOnly(Side.CLIENT)
    void render();
    
}
