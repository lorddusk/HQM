package hqm.api.page;

import hqm.api.IQuestbook;
import hqm.client.gui.GuiQuestbook;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public interface ILayout{
    
    void onCreation(IQuestbook questbook, @Nonnull GuiQuestbook gui);
    
    IQuestbook getQuestbook();
    
    @Nonnull
    GuiQuestbook getQuestbookGui();
    
    //todo
    @SideOnly(Side.CLIENT)
    void render();
    
}
