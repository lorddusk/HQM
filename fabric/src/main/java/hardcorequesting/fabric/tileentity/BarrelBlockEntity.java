package hardcorequesting.fabric.tileentity;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.FluidInsertable;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.quests.task.item.ConsumeItemTask;
import hardcorequesting.common.tileentity.AbstractBarrelBlockEntity;
import hardcorequesting.fabric.FabricFluidStack;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public class BarrelBlockEntity extends AbstractBarrelBlockEntity implements FluidInsertable, BlockEntityClientSerializable {
    @Override
    public FluidVolume attemptInsertion(FluidVolume fluidVolume, Simulation simulation) {
        QuestTask task = getCurrentTask();
        if (task instanceof ConsumeItemTask) {
            ConsumeItemTask consumeTask = (ConsumeItemTask) task;
            
            if (consumeTask.increaseFluid(new FabricFluidStack(fluidVolume = fluidVolume.copy()), consumeTask.getData(this.getPlayerUUID()), this.getPlayerUUID(), true)) {
                this.updateState();
                this.doSync();
            }
        }
        return fluidVolume;
    }
    
    @NotNull
    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        return this.save(tag);
    }
    
    @Override
    public void fromClientTag(@NotNull CompoundTag tag) {
        this.load(null, tag);
    }
    
    @Override
    public void syncToClientsNearby() {
        sync();
    }
}
