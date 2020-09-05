package hardcorequesting.tileentity;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class BaseBlockEntity extends BlockEntity implements BlockEntityClientSerializable {
    
    public BaseBlockEntity(BlockEntityType<?> type) {
        super(type);
    }
    
    @Override
    public final void load(BlockState state, CompoundTag compound) {
        super.load(state, compound);
        this.readTile(compound, NBTType.SAVE);
    }
    
    @Override
    public final CompoundTag save(CompoundTag compound) {
        compound = super.save(compound);
        this.writeTile(compound, NBTType.SAVE);
        return compound;
    }
    
    public void writeTile(CompoundTag nbt, NBTType type) {}
    
    public void readTile(CompoundTag nbt, NBTType type) {}
    
    @NotNull
    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        return this.save(tag);
    }
    
    @Override
    public void fromClientTag(@NotNull CompoundTag tag) {
        this.load(null, tag);
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
