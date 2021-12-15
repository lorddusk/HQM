package hardcorequesting.common.tileentity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractBaseBlockEntity extends BlockEntity {
    
    public AbstractBaseBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
    
    public final void syncToClientsNearby() {
        if (!getLevel().isClientSide()) {
            ServerLevel world = (ServerLevel) getLevel();
            world.getChunkSource().blockChanged(getBlockPos());
        }
    }
}