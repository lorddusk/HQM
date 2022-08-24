package hardcorequesting.fabric.tileentity;

import dev.architectury.fluid.FluidStack;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.quests.task.item.ConsumeItemTask;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.BlankVariantView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.impl.transfer.TransferApiImpl;

import java.util.Iterator;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class QSDFluidTank implements InsertionOnlyStorage<FluidVariant> {
    private final List<StorageView<FluidVariant>> blankView = List.of(new BlankVariantView<>(FluidVariant.blank(), FluidConstants.BUCKET));
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
    public Iterator<StorageView<FluidVariant>> iterator() {
        return blankView.iterator();
    }
}
