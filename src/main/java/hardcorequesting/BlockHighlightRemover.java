//package hardcorequesting;
//
//
//import net.minecraft.util.BlockPos;
//import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
//import net.minecraftforge.fml.relauncher.Side;
//import net.minecraftforge.fml.relauncher.SideOnly;
//import hardcorequesting.block.ModBlocks;
//import hardcorequesting.tileentity.TileEntityPortal;
//import net.minecraft.tileentity.TileEntity;
//import net.minecraftforge.client.event.DrawBlockHighlightEvent;
//import net.minecraftforge.common.MinecraftForge;
//
//@SideOnly(Side.CLIENT)
//public class BlockHighlightRemover {
//    public BlockHighlightRemover() {
//        MinecraftForge.EVENT_BUS.register(this);
//    }
//
//    @SubscribeEvent
//    public void onRender(DrawBlockHighlightEvent event) {
//        if (event.target != null && event.player != null) {
//            if (ModBlocks.itemPortal == event.player.worldObj.getBlockState(new BlockPos(event.target.getBlockPos().getX(),event.target.getBlockPos().getY(),event.target.getBlockPos().getZ()))) {
//                TileEntity te = event.player.worldObj.getTileEntity(new BlockPos(event.target.getBlockPos().getX(),event.target.getBlockPos().getY(),event.target.getBlockPos().getZ()));
//                if (te instanceof TileEntityPortal) {
//                    TileEntityPortal portal = (TileEntityPortal) te;
//                    if (!portal.hasCollision(event.player)) {
//                        event.setCanceled(true);
//                    }
//                }
//            }
//        }
//    }
//}
