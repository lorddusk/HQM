package hardcorequesting.common.quests.task.item;

import hardcorequesting.common.platform.FluidStack;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.data.ItemsTaskData;
import hardcorequesting.common.util.Fraction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class ConsumeItemTask extends ItemRequirementTask {
    
    public ConsumeItemTask(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
    }
    
    public boolean increaseFluid(FluidStack fluidVolume, ItemsTaskData data, UUID playerId, boolean action) {
        boolean updated = false;
        
        for (int i = 0; i < parts.size(); i++) {
            Part item = parts.get(i);
            if (item.fluid == null || data.isDone(i, item)) {
                continue;
            }
            
            if (fluidVolume != null && fluidVolume.getFluid() != null && fluidVolume.getFluid() == item.fluid.getFluid()) {
                Fraction amount = fluidVolume.getAmount().isLessThan(Fraction.ofWhole(item.required - data.getValue(i))) ? fluidVolume.getAmount() : Fraction.ofWhole(item.required - data.getValue(i));
                if (action)
                    data.setValue(i, data.getValue(i) + amount.intValue());
                fluidVolume.split(amount);
                updated = true;
                break;
            }
        }
        
        
        if (action && updated) {
            doCompletionCheck(data, playerId);
        }
        
        return updated;
    }
    
    @Override
    public void onUpdate(Player player) {
        if (increaseItems(player.inventory.items, getData(player), player.getUUID())) {
            player.inventory.setChanged();
        }
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    protected boolean mayUseFluids() {
        return true;
    }
    
    @Override
    public boolean allowManual() {
        return true;
    }
}
