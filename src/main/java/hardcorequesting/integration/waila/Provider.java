package hardcorequesting.integration.waila;


public class Provider {}
/*
public class Provider implements IWailaPlugin {
    
    private static final String MOD_NAME = "HQM";
    private static final Identifier IS_REMOTE_AVAILABLE = new Identifier(HardcoreQuesting.ID, "show_qds");
    
    public static void callbackRegister(IWailaRegistrar registrar) {
        Provider instance = new Provider();
        registrar.registerStackProvider(instance, PortalBlock.class);
        registrar.registerBodyProvider(instance, DeliveryBlock.class);
        registrar.addConfigRemote(MOD_NAME, IS_REMOTE_AVAILABLE, "Show QDS data");
    }
    
    @Environment(EnvType.CLIENT)
    @NotNull
    @Override
    public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (accessor.getBlock() == ModBlocks.blockPortal) {
            TileEntity te = accessor.getTileEntity();
            if (te instanceof PortalBlockEntity) {
                PortalBlockEntity portal = (PortalBlockEntity) te;
                if (portal.hasTexture(MinecraftClient.getInstance().player)) {
                    ItemStack ret = portal.getType().createItemStack();
                    if (ret.isEmpty()) {
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
    
    @NotNull
    @Override
    public List<String> getWailaBody(ItemStack stack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (config.getConfig(IS_REMOTE_AVAILABLE)) {
            if (!stack.isEmpty() && stack.getItem() == Item.getItemFromBlock(accessor.getBlock())) {
                TileEntity te = accessor.getTileEntity();
                if (te != null) {
                    if (te instanceof BarrelBlockEntity) {
                        BarrelBlockEntity qds = (BarrelBlockEntity) te;
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
    
    @Override
    public void register(IRegistrar iRegistrar) {
    }
}

 */
