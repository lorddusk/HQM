package hardcorequesting.forge.tileentity;

import hardcorequesting.common.quests.data.QuestDataTaskItems;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.quests.task.QuestTaskItems;
import hardcorequesting.common.quests.task.QuestTaskItemsConsume;
import hardcorequesting.common.tileentity.AbstractBarrelBlockEntity;
import hardcorequesting.forge.ForgeFluidStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.util.Direction;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.UUID;

public class BarrelBlockEntity extends AbstractBarrelBlockEntity {
    private LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> new IFluidHandler() {
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
            QuestTask task = getCurrentTask();
            if (task instanceof QuestTaskItemsConsume) {
                UUID playerUUID = BarrelBlockEntity.this.getPlayerUUID();
                QuestDataTaskItems data = (QuestDataTaskItems) task.getData(playerUUID);
                QuestTaskItems.ItemRequirement[] items = ((QuestTaskItemsConsume) task).getItems();
                for (int i = 0; i < items.length; i++) {
                    QuestTaskItems.ItemRequirement item = items[i];
                    if (item.fluid == null || item.required == data.progress[i]) {
                        continue;
                    }
                    
                    if (stack.getFluid() == item.fluid.getFluid()) {
                        return true;
                    }
                }
            }
            
            return false;
        }
        
        @Override
        public int fill(FluidStack resource, FluidAction action) {
            QuestTask task = getCurrentTask();
            if (task instanceof QuestTaskItemsConsume) {
                UUID playerUUID = BarrelBlockEntity.this.getPlayerUUID();
                FluidStack duplicate = resource.copy();
                if (((QuestTaskItemsConsume) task).increaseFluid(new ForgeFluidStack(duplicate), (QuestDataTaskItems) task.getData(playerUUID), playerUUID, action.execute()) && action.execute()) {
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
    
    @Override
    public void syncToClientsNearby() {
        if (!getLevel().isClientSide()) {
            ServerWorld world = (ServerWorld) getLevel();
            world.getChunkSource().blockChanged(getBlockPos());
        }
    }
    
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(worldPosition, 10, getUpdateTag());
    }
    
    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        return save(new CompoundNBT());
    }
    
    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            return fluidHandler.cast();
        return super.getCapability(cap, side);
    }
}
