package hardcorequesting;


import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import hardcorequesting.blocks.ModBlocks;
import hardcorequesting.tileentity.TileEntityPortal;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.MinecraftForge;


@SideOnly(Side.CLIENT)
public class BlockHighlightRemover {
    public BlockHighlightRemover() {
        MinecraftForge.EVENT_BUS.register(this);
    }


    @SubscribeEvent
    public void onRender(DrawBlockHighlightEvent event) {
        if (event.target != null && event.player != null) {
            if (ModBlocks.itemPortal == event.player.worldObj.getBlock(event.target.blockX, event.target.blockY, event.target.blockZ)) {
                TileEntity te = event.player.worldObj.getTileEntity(event.target.blockX, event.target.blockY, event.target.blockZ);
                if (te instanceof TileEntityPortal) {
                    TileEntityPortal portal = (TileEntityPortal) te;
                    if (!portal.hasCollision(event.player)) {
                        event.setCanceled(true);
                    }
                }
            }
        }
    }
}
