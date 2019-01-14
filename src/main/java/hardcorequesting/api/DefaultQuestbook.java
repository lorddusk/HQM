package hardcorequesting.api;

import hardcorequesting.api.page.ILayout;
import hardcorequesting.util.HQMUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DefaultQuestbook implements IQuestbook{
    
    private UUID questbookId;
    private String nameTranslationKey, descTranslationKey, tooltipTranslationKey;
    private List<Integer> allowedDimensions = new ArrayList<>();
    private List<IQuestline> questlines;
    
    @Override
    public void onCreation(UUID questbookID, NBTTagCompound additionalData, List<IQuestline> questlines){
        this.questbookId = questbookID;
        this.nameTranslationKey = additionalData.getString("Name");
        this.descTranslationKey = additionalData.getString("Desc");
        this.tooltipTranslationKey = additionalData.getString("Tooltip");
        this.allowedDimensions = HQMUtil.getIntListFromNBT(additionalData, "AllowedDimensions");
        this.questlines = questlines;
    }
    
    @Override
    public UUID getUUID(){
        return this.questbookId;
    }
    
    @Override
    public String getNameTranslationKey(){
        return this.nameTranslationKey;
    }
    
    @Override
    public String getDescTranslationKey(){
        return this.descTranslationKey;
    }
    
    @Override
    public String getTooltipTranslationKey(){
        return this.tooltipTranslationKey;
    }
    
    @Override
    public List<IQuestline> getQuestlines(){
        return this.questlines;
    }
    
    @Override
    public NBTTagCompound getAdditionalData(){
        NBTTagCompound data = new NBTTagCompound();
        data.setString("Name", this.getNameTranslationKey());
        if(this.getDescTranslationKey() != null){
            data.setString("Desc", this.getDescTranslationKey());
        }
        if(this.getTooltipTranslationKey() != null){
            data.setString("Tooltip", this.getTooltipTranslationKey());
        }
        return data;
    }
    
    @Override
    public boolean canExist(World world){
        return this.allowedDimensions.isEmpty() ? true : this.allowedDimensions.contains(world.provider.getDimension());
    }
    
    @Nullable
    @Override
    public ILayout openBook(@Nonnull ItemStack stack){
        return null;
    }
    
    @Nonnull
    @Override
    public NonNullList<ItemStack> getItemStacks(){
        return NonNullList.create();
    }
}
