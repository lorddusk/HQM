package hardcorequesting.util;

import net.minecraft.client.util.ITooltipFlag;

public class TooltipFlag implements ITooltipFlag
{
    private boolean isAdvanced;
    
    public TooltipFlag(boolean flag)
    {
        isAdvanced = flag;
    }
    
    @Override
    public boolean isAdvanced()
    {
        return isAdvanced;
    }
}
