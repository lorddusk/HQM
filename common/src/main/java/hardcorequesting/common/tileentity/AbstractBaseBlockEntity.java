package hardcorequesting.common.tileentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractBaseBlockEntity extends BlockEntity {
    
    public AbstractBaseBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
    
    @Override
    public final void load(CompoundTag compound) {
        super.load(compound);
        this.readTile(compound, NBTType.SAVE);
    }
    
    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        this.writeTile(compoundTag, NBTType.SAVE);
    }
    
    public void writeTile(CompoundTag nbt, NBTType type) {}
    
    public void readTile(CompoundTag nbt, NBTType type) {}
    
    protected void receiveSyncPacket(@NotNull CompoundTag nbt) {
        this.readTile(nbt, NBTType.SYNC);
    }
    
    public final void syncToClientsNearby() {
        if (!getLevel().isClientSide()) {
            ServerLevel world = (ServerLevel) getLevel();
            world.getChunkSource().blockChanged(getBlockPos());
        }
    }
    
    public enum NBTType {
        SAVE,
        SYNC
    }
}
