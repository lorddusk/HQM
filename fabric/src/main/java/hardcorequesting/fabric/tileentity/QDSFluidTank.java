package hardcorequesting.fabric.tileentity;

import com.ibm.icu.impl.Pair;
import dev.architectury.fluid.FluidStack;
import hardcorequesting.common.quests.data.ItemsTaskData;
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
/**
 * This fluid tank acts in much the same way that the fluid capability does in forge
 * The insert method is run whenever the mod attempts to insert a fluid into the tank.
 * Because the tank itself does not store any fluids, any method that tries to retrieve the data
 * from the tank will result in empties, just like forge. Before any breaking changes are made
 * to the tank, a snapshot of the current state of the tank is made. Obviously since the tank itself
 * has no data to take a snapshot of, the snapshot is not of the tank itself but rather of the
 * data held in the Quest Task. If the insertion task is ever aborted at any time, the tank will read
 * the last state of the tank and override the current information to revert it back to the state before
 * the task.
 */
@SuppressWarnings("UnstableApiUsage")
public class QDSFluidTank extends SnapshotParticipant<QDSFluidTank.FluidQuestData> implements InsertionOnlyStorage<FluidVariant> {
    private final List<StorageView<FluidVariant>> blankView = List.of(new BlankVariantView<>(FluidVariant.blank(), FluidConstants.BUCKET));
    private final BarrelBlockEntity blockEntity;

    public QDSFluidTank(BarrelBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    /**
     * Inserts fluids into the current quest task's fluid storage
     * @param resource    The resource to insert. May not be blank.
     * @param maxAmount   The maximum amount of resource to insert. May not be negative.
     * @param transaction The transaction this operation is part of.
     * @return
     */
    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        QuestTask<?> questTask = blockEntity.getCurrentTask();
        if (questTask instanceof ConsumeItemTask task) {
            FluidStack fluidStack = FluidStack.create(resource.getFluid(), maxAmount);
            updateSnapshots(transaction);
            if(task.increaseFluid(fluidStack, blockEntity.getPlayerUUID(), true)) return maxAmount - fluidStack.getAmount();
        }
        return 0;
    }

    /**
     * Gets the stored values of the fluids stored in the tank. Since the tank doesn't store
     * any fluids, this will return an empty iterator.
     * @return an iterator with no storage values
     */
    @Override
    public Iterator<StorageView<FluidVariant>> iterator() {
        return blankView.iterator();
    }


    /**
     * Creates a snapshot of the current block entity, storing all the fluidstacks
     * and their completion values as an object.
     * @return an object representation of the current state of the block entity
     */
    @Override
    protected FluidQuestData createSnapshot() {
        return new FluidQuestData(this.blockEntity);
    }

    /**
     * Reads the state of the data held in the snapshot and overrides
     * the state of the quest task held by the block entity
     * @param snapshot Quest data to override the current quest data
     */
    @Override
    protected void readSnapshot(FluidQuestData snapshot) {
        if(blockEntity.getCurrentTask() instanceof ConsumeItemTask task) {
            ItemsTaskData data = task.getData(blockEntity.getPlayerUUID());
            snapshot.getFluidData().forEach((integer, pair) -> {
                task.getParts().get(integer).setFluidStack(pair.first);
                data.setValue(integer, pair.second);
            });
        }
    }


    /**
     * Finalizes the data. This is run when an insertion is completed
     */
    @Override
    protected void onFinalCommit() {
        blockEntity.updateState();
        blockEntity.doSync();
        if(blockEntity.getCurrentTask() instanceof ConsumeItemTask task) {
            ItemsTaskData data = task.getData(blockEntity.getPlayerUUID());
            task.doCompletionCheck(data, blockEntity.getPlayerUUID());
        }
    }

    /**
     * Class representation of a quest task's fluid data
     */
    public static class FluidQuestData {
        private final Map<Integer, Pair<FluidStack, Integer>> fluids = new HashMap<>();
        public FluidQuestData(BarrelBlockEntity blockEntity) {
            if(blockEntity.getCurrentTask() instanceof ConsumeItemTask task) {
                for (int i = 0; i < task.getParts().size(); i++) {
                    ItemRequirementTask.Part part = task.getParts().get(i);
                    ItemsTaskData data = task.getData(blockEntity.getPlayerUUID());
                    int finalI = i;
                    part.stack.right().ifPresent(fluid -> {
                        fluids.put(finalI, Pair.of(fluid, data.getValue(finalI)));
                    });
                }
            }
        }

        public Map<Integer, Pair<FluidStack, Integer>> getFluidData() {
            return fluids;
        }
    }
}
