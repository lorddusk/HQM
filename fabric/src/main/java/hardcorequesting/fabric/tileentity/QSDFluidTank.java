package hardcorequesting.fabric.tileentity;

import dev.architectury.fluid.FluidStack;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.quests.task.item.ConsumeItemTask;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.impl.transfer.TransferApiImpl;

import java.util.Iterator;

@SuppressWarnings("UnstableApiUsage")
public class QSDFluidTank implements SingleSlotStorage<FluidVariant>, InsertionOnlyStorage<FluidVariant> {
    private final BarrelBlockEntity blockEntity;

    public QSDFluidTank(BarrelBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        QuestTask<?> questTask = blockEntity.getCurrentTask();
        if (questTask instanceof ConsumeItemTask task) {
            FluidStack fluidStack = FluidStack.create(resource.getFluid(), maxAmount);
            if (task.increaseFluid(fluidStack, blockEntity.getPlayerUUID(), true)) {
                blockEntity.updateState();
                blockEntity.doSync();
            }
            return maxAmount - fluidStack.getAmount();
        }
        return 0;
    }

    @Override
    public boolean isResourceBlank() {
        return true;
    }

    @Override
    public FluidVariant getResource() {
        return FluidVariant.blank();
    }

    @Override
    public long getAmount() {
        return 0;
    }

    @Override
    public long getCapacity() {
        return Integer.MAX_VALUE;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public boolean supportsExtraction() {
        return false;
    }
}
