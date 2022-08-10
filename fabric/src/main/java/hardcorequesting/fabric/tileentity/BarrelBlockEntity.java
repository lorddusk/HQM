package hardcorequesting.fabric.tileentity;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.FluidInsertable;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.quests.task.item.ConsumeItemTask;
import hardcorequesting.common.tileentity.AbstractBarrelBlockEntity;
import hardcorequesting.fabric.FabricFluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BarrelBlockEntity extends AbstractBarrelBlockEntity implements FluidInsertable {
    public BarrelBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }
    
    @Override
    public FluidVolume attemptInsertion(FluidVolume fluidVolume, Simulation simulation) {
        QuestTask<?> task = getCurrentTask();
        if (task instanceof ConsumeItemTask) {
            if (((ConsumeItemTask) task).increaseFluid(new FabricFluidStack(fluidVolume = fluidVolume.copy()), this.getPlayerUUID(), simulation.isAction())) {
                this.updateState();
                this.doSync();
            }
        }
        return fluidVolume;
    }
}
