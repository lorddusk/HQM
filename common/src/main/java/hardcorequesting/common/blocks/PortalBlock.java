package hardcorequesting.common.blocks;

public class PortalBlock {}
/*
public class PortalBlock extends BlockWithEntity {
    
    public PortalBlock() {
        super(FabricBlockSettings.of(Material.WOOD)
                .hardness(10.0F)
                .strength(-1.0F, 6000000.0F)
        );
    }
    
    @Override
    public BlockEntity createBlockEntity(BlockView view) {
        return new PortalBlockEntity();
    }
    
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, EntityContext context) {
        BlockEntity te = view.getBlockEntity(pos);
        if (te instanceof PortalBlockEntity && !((PortalBlockEntity) te).hasCollision((PlayerEntity) entity))
            return VoxelShapes.empty();
        return super.getOutlineShape(state, view, pos, context);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void addCollisionBoxToList(IBlockState state,
            @NotNull World world,
            @NotNull BlockPos pos, @NotNull AxisAlignedBB entityBox, @NotNull List<AxisAlignedBB> collidingBoxes, Entity entity, boolean b) {
        TileEntity te = world.getTileEntity(pos);
        if (entity instanceof PlayerEntity && te instanceof PortalBlockEntity && !((PortalBlockEntity) te).hasCollision((PlayerEntity) entity))
            return;
        super.addCollisionBoxToList(state, world, pos, entityBox, collidingBoxes, entity, b);
    }
    
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, PlayerEntity player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (player != null && Quest.canQuestsBeEdited()) {
            if (!player.inventory.getCurrentItem().isEmpty() && player.inventory.getCurrentItem().getItem() == ModItems.book) {
                if (!world.isRemote) {
                    TileEntity tile = world.getTileEntity(pos);
                    if (tile instanceof PortalBlockEntity) {
                        ((PortalBlockEntity) tile).setCurrentQuest();
                        if (((PortalBlockEntity) tile).getCurrentQuest() != null)
                            player.sendMessage(Translator.translateToIChatComponent("tile.hqm:quest_portal_0.bindTo", ((PortalBlockEntity) tile).getCurrentQuest().getName()));
                        else
                            player.sendMessage(Translator.translateToIChatComponent("hqm.message.noTaskSelected"));
                    }
                }
                return true;
            } else {
                if (!world.isRemote) {
                    TileEntity tile = world.getTileEntity(pos);
                    if (tile instanceof PortalBlockEntity)
                        ((PortalBlockEntity) tile).openInterface(player);
                }
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof PortalBlockEntity) {
            PortalBlockEntity manager = (PortalBlockEntity) te;
            if (stack.hasTagCompound() && stack.getTagCompound().hasKey("Portal", Constants.NBT.TAG_COMPOUND))
                manager.readContentFromNBT(stack.getTagCompound().getCompoundTag("Portal"));
        }
    }
    
    @NotNull
    @Override
    public ItemStack getPickBlock(@NotNull IBlockState state, RayTraceResult target, @NotNull World world, @NotNull BlockPos pos, PlayerEntity player) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof PortalBlockEntity) {
            PortalBlockEntity portal = (PortalBlockEntity) te;
            ItemStack stack = super.getPickBlock(state, target, world, pos, player);
            if (!stack.isEmpty()) {
                CompoundTag tagCompound = stack.getTagCompound();
                if (tagCompound == null) {
                    tagCompound = new CompoundTag();
                    stack.setTagCompound(tagCompound);
                }
                
                CompoundTag info = new CompoundTag();
                tagCompound.setTag("Portal", info);
                portal.writeContentToNBT(info);
            }
            return stack;
        }
        return ItemStack.EMPTY;
    }
    
    @SuppressWarnings("deprecation")
    @NotNull
    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }
}

 */