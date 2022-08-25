package hardcorequesting.fabric.tileentity;

import hardcorequesting.common.tileentity.AbstractBarrelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BarrelBlockEntity extends AbstractBarrelBlockEntity {

    public final QDSFluidTank fluidTank = new QDSFluidTank(this);

    public BarrelBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }
}
