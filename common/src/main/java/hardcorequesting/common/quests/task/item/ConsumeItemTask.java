package hardcorequesting.common.quests.task.item;

import dev.architectury.fluid.FluidStack;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.data.ItemsTaskData;
import hardcorequesting.common.quests.task.TaskType;
import hardcorequesting.common.util.FluidUtils;
import hardcorequesting.common.util.Fraction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import java.util.UUID;

public class ConsumeItemTask extends ItemRequirementTask {
    
    public ConsumeItemTask(Quest parent) {
        this(TaskType.CONSUME.get(), parent);
    }
    
    protected ConsumeItemTask(TaskType<? extends ConsumeItemTask> type, Quest parent) {
        super(type, parent);
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
                Fraction amount = FluidUtils.getAmount(fluidVolume).isLessThan(Fraction.ofWhole(item.required - data.getValue(i))) ? FluidUtils.getAmount(fluidVolume) : Fraction.ofWhole(item.required - data.getValue(i));
                if (action)
                    data.setValue(i, data.getValue(i) + amount.intValue());
                FluidUtils.split(fluidVolume, amount);
                updated = true;
                break;
            }
        }
        
        return action && updated;
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
        boolean updated = increaseItems(player.getInventory().items, player.getUUID());
        updated |= increaseFluids(player);
        if (updated) {
            player.getInventory().setChanged();
        }
    }
    
    /**
     * Hands in the fluids held by the items in the player's inventory, for example buckets.
     */
    private boolean increaseFluids(Player player) {
        UUID playerId = player.getUUID();
        if (!parent.isAvailable(playerId)) return false;
        
        ItemsTaskData data = getData(playerId);
        NonNullList<ItemStack> items = player.getInventory().items;
        boolean updated = false;
        
        for (int i = 0; i < parts.size(); i++) {
            Part part = parts.get(i);
            if (part.hasItem() || data.isDone(i, part)) {
                continue;
            }
            
            Fluid fluid = part.stack.right().orElseThrow().getFluid();
            for (int slot = 0; slot < items.size() && !data.isDone(i, part); slot++) {
                ItemStack stack = items.get(slot);
                if (stack.isEmpty()) {
                    continue;
                }
                
                int drained = drainFluid(player, slot, fluid, part.required - data.getValue(i));
                if (drained > 0) {
                    data.setValue(i, Math.min(part.required, data.getValue(i) + drained));
                    updated = true;
                }
            }
        }
        
        if (updated) {
            doCompletionCheck(data, playerId);
        }
        return updated;
    }
    
    /**
     * Drains at most the needed amount from the item. A container that cannot give a partial amount (a bucket)
     * is emptied completely if it holds more than needed, the surplus is lost.
     */
    private static int drainFluid(Player player, int slot, Fluid fluid, int needed) {
        int drained = HardcoreQuestingCore.platform.drainFluidFrom(player, slot, fluid, needed);
        if (drained > 0) {
            return drained;
        }
        for (FluidStack held : HardcoreQuestingCore.platform.findFluidsIn(player.getInventory().items.get(slot))) {
            if (held.getFluid() == fluid && held.getAmount() > needed) {
                return HardcoreQuestingCore.platform.drainFluidFrom(player, slot, fluid, (int) held.getAmount());
            }
        }
        return 0;
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public boolean mayUseFluids() {
        return true;
    }
}
