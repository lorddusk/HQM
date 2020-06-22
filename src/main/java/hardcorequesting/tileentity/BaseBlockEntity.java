package hardcorequesting.tileentity;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public class BaseBlockEntity extends BlockEntity implements BlockEntityClientSerializable {
    
    public BaseBlockEntity(BlockEntityType<?> type) {
        super(type);
    }
    
    @Override
    public final void fromTag(BlockState state, CompoundTag compound) {
        super.fromTag(state, compound);
        this.readTile(compound, NBTType.SAVE);
    }
    
    @Override
    public final CompoundTag toTag(CompoundTag compound) {
        compound = super.toTag(compound);
        this.writeTile(compound, NBTType.SAVE);
        return compound;
    }
    
    public void writeTile(CompoundTag nbt, NBTType type) {}
    
    public void readTile(CompoundTag nbt, NBTType type) {}
    
    @NotNull
    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        return this.toTag(tag);
    }
    
    @Override
    public void fromClientTag(@NotNull CompoundTag tag) {
        this.fromTag(null, tag);
    }
    
    protected void receiveSyncPacket(@NotNull CompoundTag nbt) {
        this.readTile(nbt, NBTType.SYNC);
    }
    
    public void syncToClientsNearby() {
        this.sync();
    }
    
    public enum NBTType {
        SAVE,
        SYNC
    }
}
