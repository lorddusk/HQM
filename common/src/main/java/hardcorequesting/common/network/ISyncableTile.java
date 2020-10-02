package hardcorequesting.common.network;

import net.minecraft.nbt.CompoundTag;

public interface ISyncableTile {
    
    CompoundTag getSyncData();
    
    void onData(CompoundTag data);
    
}
