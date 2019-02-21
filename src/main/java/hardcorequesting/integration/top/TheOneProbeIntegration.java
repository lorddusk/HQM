package hardcorequesting.integration.top;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.blocks.BlockDelivery;
import hardcorequesting.quests.task.QuestTask;
import hardcorequesting.tileentity.TileEntityBarrel;
import mcjty.theoneprobe.api.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.function.Function;

public class TheOneProbeIntegration implements Function<ITheOneProbe, Void>, IProbeInfoProvider{
    
    @Override
    public Void apply(ITheOneProbe iTheOneProbe){
        init(iTheOneProbe);
        return null;
    }
    
    public void init(ITheOneProbe top){
        top.registerProvider(this);
    }
    
    @Override
    public String getID(){
        return new ResourceLocation(HardcoreQuesting.ID, "top_integration").toString();
    }
    
    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState state, IProbeHitData data){
        if(state.getBlock() instanceof BlockDelivery){
            TileEntity tile = world.getTileEntity(data.getPos());
            if(tile instanceof TileEntityBarrel){
                if(true) return;
                QuestTask currentTask = ((TileEntityBarrel) tile).getCurrentTask();
                if(currentTask != null && tile.getBlockMetadata() == 1){
                    probeInfo.text(((TileEntityBarrel) tile).getPlayerUUID().toString());
                    probeInfo.text(currentTask.getParent().getName());
                    probeInfo.text(currentTask.getDescription());
                    probeInfo.text((int) (currentTask.getCompletedRatio(((TileEntityBarrel) tile).getPlayerUUID()) * 100) + "% completed");
                }
            }
        }
    }
}
