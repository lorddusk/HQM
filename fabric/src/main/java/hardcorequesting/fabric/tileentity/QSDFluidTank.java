package hardcorequesting.fabric.tileentity;

import dev.architectury.fluid.FluidStack;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.quests.task.item.ConsumeItemTask;
import hardcorequesting.common.quests.task.item.ItemRequirementTask;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.BlankVariantView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class QSDFluidTank extends SnapshotParticipant<QSDFluidTank.FluidQuestData> implements InsertionOnlyStorage<FluidVariant> {
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
            updateSnapshots(transaction);
            task.increaseFluid(fluidStack, blockEntity.getPlayerUUID(), true);
            return maxAmount - fluidStack.getAmount();
        }
        return 0;
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator() {
        return blankView.iterator();
    }

    @Override
    protected FluidQuestData createSnapshot() {
        return new FluidQuestData(this.blockEntity);
    }

    @Override
    protected void readSnapshot(FluidQuestData snapshot) {
        if(blockEntity.getCurrentTask() instanceof ConsumeItemTask task) {
            snapshot.getFluids().forEach((integer, stack) -> task.getParts().get(integer).setFluidStack(stack));
        }
    }

    @Override
    protected void onFinalCommit() {
        blockEntity.updateState();
        blockEntity.doSync();
    }

    public static class FluidQuestData {
        private Map<Integer, FluidStack> fluids;
        public FluidQuestData(BarrelBlockEntity blockEntity) {
            if(blockEntity.getCurrentTask() instanceof ConsumeItemTask task) {
                Map<Integer, FluidStack> fluidStacks = new HashMap<>();
                for (int i = 0; i < task.getParts().size(); i++) {
                    ItemRequirementTask.Part part = task.getParts().get(i);
                    int finalI = i;
                    part.stack.right().ifPresent(fluids -> fluidStacks.put(finalI, fluids));
                }
                fluids = fluidStacks;
            }
        }

        public Map<Integer, FluidStack> getFluids() {
            return fluids;
        }
    }
}
