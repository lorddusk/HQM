package hardcorequesting.api;

import hardcorequesting.api.page.ISite;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * This is the underlying implimentation for a Questbook.
 * Every questbook (not the item/itemstack) has to implement this interface,
 * otherwise the general HQM logic won't work.
 *
 * It can be created from a questbook.json file via {@link hardcorequesting.io.QuestbookData}
 *
 * @author canitzp
 * @since 11.01.2019 HQM 6(?)
 */
public interface IQuestbook{
    
    void onCreation(UUID questbookID, NBTTagCompound additionalData, List<IQuestline> questlines);
    
    UUID getUUID();
    
    String getNameTranslationKey();
    
    String getDescTranslationKey();
    
    String getTooltipTranslationKey();
    
    List<IQuestline> getQuestlines();
    
    NBTTagCompound getAdditionalData();
    
    /**
     * This method defines whether a book is valid in a specified world.
     * If it isn't true then the book can't exist as item nor can be opened.
     *
     * @param world The world to check if it is valid
     * @return True to allow book usage or false to reject it.
     */
    boolean canExist(World world);
    
    /**
     * This method is called whenever someone tries to open a questbook item via right click.
     *
     * @param stack A never null and never {@link ItemStack#EMPTY} ItemStack
     * @return The {@link ISite} instance to open
     */
    @Nullable
    ISite openBook(@Nonnull ItemStack stack);
    
}
