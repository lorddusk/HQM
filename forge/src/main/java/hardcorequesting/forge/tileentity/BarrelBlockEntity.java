package hardcorequesting.forge.tileentity;

import hardcorequesting.common.tileentity.AbstractBarrelBlockEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.world.server.ServerWorld;

public class BarrelBlockEntity extends AbstractBarrelBlockEntity {
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
    
    @Override
    public CompoundNBT getUpdateTag() {
        return save(new CompoundNBT());
    }
}
