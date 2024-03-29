package hardcorequesting.forge.tileentity;

import hardcorequesting.common.quests.data.ItemsTaskData;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.quests.task.item.ConsumeItemTask;
import hardcorequesting.common.tileentity.AbstractBarrelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class BarrelBlockEntity extends AbstractBarrelBlockEntity {
    private final LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> new IFluidHandler() {
        @Override
        public int getTanks() {
            return 1;
        }
        
        @NotNull
        @Override
        public FluidStack getFluidInTank(int tank) {
            return FluidStack.EMPTY;
        }
        
        @Override
        public int getTankCapacity(int tank) {
            return Integer.MAX_VALUE;
        }
        
        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            QuestTask<?> task = getCurrentTask();
            if (task instanceof ConsumeItemTask) {
                ConsumeItemTask consumeTask = (ConsumeItemTask) task;
                
                UUID playerUUID = BarrelBlockEntity.this.getPlayerUUID();
                return consumeTask.canTakeFluid(stack.getFluid(), playerUUID);
            }
            
            return false;
        }
        
        @Override
        public int fill(FluidStack resource, FluidAction action) {
            QuestTask<?> task = getCurrentTask();
            if (task instanceof ConsumeItemTask consumeItemTask) {
                UUID playerUUID = BarrelBlockEntity.this.getPlayerUUID();
                FluidStack duplicate = resource.copy();
                if (consumeItemTask.increaseFluid(dev.architectury.fluid.FluidStack.create(duplicate.getFluid(), duplicate.getAmount()), playerUUID, action.execute()) && action.execute()) {
                    ItemsTaskData data = consumeItemTask.getData(getPlayerUUID());
                    consumeItemTask.doCompletionCheck(data, getPlayerUUID());
                    BarrelBlockEntity.this.updateState();
                    BarrelBlockEntity.this.doSync();
                }
                
                return resource.getAmount() - duplicate.getAmount();
            }
            
            return 0;
        }
        
        @NotNull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }
        
        @NotNull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    });
    
    public BarrelBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }
    
    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            return fluidHandler.cast();
        return super.getCapability(cap, side);
    }
}
