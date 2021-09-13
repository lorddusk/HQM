package hardcorequesting.common.quests.task.item;

import hardcorequesting.common.platform.FluidStack;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.data.ItemsTaskData;
import hardcorequesting.common.quests.task.client.ItemTaskGraphic;
import hardcorequesting.common.quests.task.client.TaskGraphic;
import hardcorequesting.common.util.Fraction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import java.util.UUID;

public class ConsumeItemTask extends ItemRequirementTask {
    
    public ConsumeItemTask(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public TaskGraphic createGraphic(UUID playerId) {
        return ItemTaskGraphic.createConsumeGraphic(this, parts, playerId, true);
    }
    
    public boolean increaseFluid(FluidStack fluidVolume, UUID playerId, boolean action) {
        boolean updated = false;
        ItemsTaskData data = getData(playerId);
        
        for (int i = 0; i < parts.size(); i++) {
            Part item = parts.get(i);
            if (data.isDone(i, item)) {
                continue;
            }
            
            if (fluidVolume != null && item.isFluid(fluidVolume.getFluid())) {
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
    
    public boolean canTakeItem(ItemStack stack, UUID playerId) {
        ItemsTaskData data = getData(playerId);
        for (int i = 0; i < parts.size(); i++) {
            ItemRequirementTask.Part part = parts.get(i);
            if (part.isStack(stack) && !data.isDone(i, part)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean canTakeFluid(Fluid fluid, UUID playerId) {
        ItemsTaskData data = getData(playerId);
        for (int i = 0; i < parts.size(); i++) {
            ItemRequirementTask.Part part = parts.get(i);
            if (part.isFluid(fluid) && !data.isDone(i, part)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void onUpdate(Player player) {
        if (increaseItems(player.getInventory().items, player.getUUID())) {
            player.getInventory().setChanged();
        }
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public boolean mayUseFluids() {
        return true;
    }
}
