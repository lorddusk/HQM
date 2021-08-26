package hardcorequesting.forge.tileentity;

import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.quests.task.item.ConsumeItemTask;
import hardcorequesting.common.tileentity.AbstractBarrelBlockEntity;
import hardcorequesting.forge.ForgeFluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
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
            if (task instanceof ConsumeItemTask) {
    
                UUID playerUUID = BarrelBlockEntity.this.getPlayerUUID();
                FluidStack duplicate = resource.copy();
                if (((ConsumeItemTask) task).increaseFluid(new ForgeFluidStack(duplicate), playerUUID, action.execute()) && action.execute()) {
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
    
    @Override
    public void syncToClientsNearby() {
        if (!getLevel().isClientSide()) {
            ServerLevel world = (ServerLevel) getLevel();
            world.getChunkSource().blockChanged(getBlockPos());
        }
    }
    
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(worldPosition, 10, getUpdateTag());
    }
    
    @Nonnull
    @Override
    public CompoundTag getUpdateTag() {
        return save(new CompoundTag());
    }
    
    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            return fluidHandler.cast();
        return super.getCapability(cap, side);
    }
}
