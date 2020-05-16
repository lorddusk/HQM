package hardcorequesting.blocks;

import hardcorequesting.items.QuestBookItem;
import hardcorequesting.quests.Quest;
import hardcorequesting.tileentity.BarrelBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricMaterialBuilder;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class DeliveryBlock extends BlockWithEntity {
    
    public static final BooleanProperty BOUND = BooleanProperty.of("bound");
    
    public DeliveryBlock() {
        super(FabricBlockSettings.of(new FabricMaterialBuilder(MaterialColor.WOOD).burnable().requiresTool().build()).hardness(1.0F).breakByTool(FabricToolTags.AXES, 0));
        this.setDefaultState(this.getStateManager().getDefaultState().with(BOUND, false));
    }
    
    @Override
    public BlockEntity createBlockEntity(BlockView view) {
        return new BarrelBlockEntity();
    }
    
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            ItemStack hold = player.getStackInHand(hand);
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof BarrelBlockEntity) {
                if (!hold.isEmpty() && hold.getItem() instanceof QuestBookItem) {
                    ((BarrelBlockEntity) tile).storeSettings(player);
                    if (((BarrelBlockEntity) tile).getCurrentTask() != null) {
                        player.sendMessage(new TranslatableText("tile.hqm:item_barrel.bindTo", Quest.getQuest(((BarrelBlockEntity) tile).getQuestUUID()).getName()));
                    } else {
                        player.sendMessage(new TranslatableText("hqm.message.noTaskSelected"));
                    }
                } else {
                    if (((BarrelBlockEntity) tile).getCurrentTask() != null) {
                        player.sendMessage(new TranslatableText("tile.hqm:item_barrel.boundTo", Quest.getQuest(((BarrelBlockEntity) tile).getQuestUUID()).getName()));
                    } else {
                        player.sendMessage(new TranslatableText("tile.hqm:item_barrel.nonBound"));
                    }
                }
            }
        }
        return ActionResult.SUCCESS;
    }
    
    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }
    
    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        if (state.get(BOUND)) {
            return 15;
        } else {
            return 0;
        }
    }
    
    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }
    
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return super.getPlacementState(ctx).with(BOUND, false);
    }
    
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(BOUND);
    }
}
