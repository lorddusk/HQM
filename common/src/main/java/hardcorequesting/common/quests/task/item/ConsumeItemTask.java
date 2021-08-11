package hardcorequesting.common.quests.task.item;

import hardcorequesting.common.platform.FluidStack;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.data.QuestDataTaskItems;
import hardcorequesting.common.util.Fraction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class ConsumeItemTask extends ItemRequirementTask {
    
    public ConsumeItemTask(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
    }
    
    public boolean increaseFluid(FluidStack fluidVolume, QuestDataTaskItems data, UUID playerId, boolean action) {
        boolean updated = false;
        
        for (int i = 0; i < elements.size(); i++) {
            Part item = elements.get(i);
            if (item.fluid == null || item.required == data.progress[i]) {
                continue;
            }
            
            if (fluidVolume != null && fluidVolume.getFluid() != null && fluidVolume.getFluid() == item.fluid.getFluid()) {
                Fraction amount = fluidVolume.getAmount().isLessThan(Fraction.ofWhole(item.required - data.progress[i])) ? fluidVolume.getAmount() : Fraction.ofWhole(item.required - data.progress[i]);
                if (action)
                    data.progress[i] += amount.intValue();
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
        if (increaseItems(player.inventory.items, (QuestDataTaskItems) getData(player), player.getUUID())) {
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