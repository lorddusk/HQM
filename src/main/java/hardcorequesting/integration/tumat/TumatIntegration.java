package hardcorequesting.integration.tumat;

import de.canitzp.tumat.IconRenderer;
import de.canitzp.tumat.InfoUtil;
import de.canitzp.tumat.api.IWorldRenderer;
import de.canitzp.tumat.api.TooltipComponent;
import de.canitzp.tumat.api.components.DescriptionComponent;
import hardcorequesting.quests.task.QuestTask;
import hardcorequesting.tileentity.TileEntityBarrel;
import hardcorequesting.tileentity.TileEntityPortal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class TumatIntegration implements IWorldRenderer{
    
    @Override
    public TooltipComponent renderTileEntity(WorldClient world, EntityPlayerSP player, TileEntity tile, EnumFacing side, TooltipComponent component, boolean shouldCalculate){
        if(tile instanceof TileEntityBarrel){
            QuestTask currentTask = ((TileEntityBarrel) tile).getCurrentTask();
            if(currentTask != null && tile.getBlockMetadata() == 1){
                List<String> lines = new ArrayList<>();
                
                lines.add(((TileEntityBarrel) tile).getPlayerUUID().toString());
                lines.add(currentTask.getParent().getName());
                lines.add(currentTask.getDescription());
                lines.add((int) (currentTask.getCompletedRatio(((TileEntityBarrel) tile).getPlayerUUID()) * 100) + "% completed");
                
                component.add(new DescriptionComponent(lines), TooltipComponent.Priority.LOW);
            }
        }
        return component;
    }
    
    @Nullable
    @Override
    public IconRenderer getIconRenderObject(WorldClient world, EntityPlayerSP player, BlockPos pos, EnumFacing side, RayTraceResult trace, boolean shouldCalculate){
        TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof TileEntityPortal){
            if(((TileEntityPortal) tile).hasTexture(player)) {
                ItemStack ret = ((TileEntityPortal) tile).getType().createItemStack();
                if(ret.isEmpty()){
                    ret = ((TileEntityPortal) tile).getStack();
                }
                return new IconRenderer(ret);
            }
        }
        IBlockState state = world.getBlockState(pos);
        return new IconRenderer(InfoUtil.newStackFromBlock(world, pos, state, player, trace));
    }
}
