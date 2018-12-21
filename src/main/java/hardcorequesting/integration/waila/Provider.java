package hardcorequesting.integration.waila;


import hardcorequesting.blocks.BlockDelivery;
import hardcorequesting.blocks.BlockPortal;
import hardcorequesting.blocks.ModBlocks;
import hardcorequesting.quests.task.QuestTask;
import hardcorequesting.tileentity.TileEntityBarrel;
import hardcorequesting.tileentity.TileEntityPortal;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;

public class Provider implements IWailaDataProvider {

    private static final String MOD_NAME = "HQM";
    private static final String IS_REMOTE_AVAILABLE = MOD_NAME + ".showQDS";

    public static void callbackRegister(IWailaRegistrar registrar) {
        Provider instance = new Provider();
        registrar.registerStackProvider(instance, BlockPortal.class);
        registrar.registerBodyProvider(instance, BlockDelivery.class);
        registrar.addConfigRemote(MOD_NAME, IS_REMOTE_AVAILABLE, "Show QDS data");
    }

    @SideOnly(Side.CLIENT)
    @Nonnull
    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (accessor.getBlock() == ModBlocks.itemPortal) {
            TileEntity te = accessor.getTileEntity();
            if (te instanceof TileEntityPortal) {
                TileEntityPortal portal = (TileEntityPortal) te;
                if (portal.hasTexture(Minecraft.getMinecraft().player)) {
                    ItemStack ret = portal.getType().createItemStack();
                    if(ret.isEmpty()){
                        ret = portal.getStack();
                    }
                    return ret;
                } else {
                    return ItemStack.EMPTY;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public List<String> getWailaBody(ItemStack stack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (config.getConfig(IS_REMOTE_AVAILABLE)) {
            if (!stack.isEmpty() && stack.getItem() == Item.getItemFromBlock(accessor.getBlock())) {
                TileEntity te = accessor.getTileEntity();
                if (te != null) {
                    if (te instanceof TileEntityBarrel) {
                        TileEntityBarrel qds = (TileEntityBarrel) te;
                        //qds.readFromNBT(accessor.getNBTData());
                        QuestTask task = qds.getCurrentTask();
                        if (task != null && te.getBlockMetadata() == 1) {
                            currenttip.add(qds.getPlayerUUID().toString());
                            currenttip.add(task.getParent().getName());
                            currenttip.add(task.getDescription());
                            currenttip.add((int) (task.getCompletedRatio(qds.getPlayerUUID()) * 100) + "% completed");
                        }
                    }
                }
            }
        }
        return currenttip;
    }

}
