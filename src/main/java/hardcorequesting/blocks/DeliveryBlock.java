package hardcorequesting.blocks;

import hardcorequesting.items.QuestBookItem;
import hardcorequesting.quests.Quest;
import hardcorequesting.tileentity.BarrelBlockEntity;
import hardcorequesting.util.Translator;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

public class DeliveryBlock extends BaseEntityBlock {
    
    public static final BooleanProperty BOUND = BooleanProperty.create("bound");
    
    public DeliveryBlock() {
        super(FabricBlockSettings.of(Material.WOOD).requiresTool().hardness(1.0F).breakByTool(FabricToolTags.AXES, 0));
        this.registerDefaultState(this.getStateDefinition().any().setValue(BOUND, false));
    }
    
    @Override
    public BlockEntity newBlockEntity(BlockGetter view) {
        return new BarrelBlockEntity();
    }
    
    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!world.isClientSide) {
            ItemStack hold = player.getItemInHand(hand);
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof BarrelBlockEntity) {
                if (!hold.isEmpty() && hold.getItem() instanceof QuestBookItem) {
                    ((BarrelBlockEntity) tile).storeSettings(player);
                    if (((BarrelBlockEntity) tile).getCurrentTask() != null) {
                        player.sendMessage(Translator.translatable("tile.hqm:item_barrel.bindTo", Quest.getQuest(((BarrelBlockEntity) tile).getQuestUUID()).getName()), Util.NIL_UUID);
                    } else {
                        player.sendMessage(Translator.translatable("hqm.message.noTaskSelected"), Util.NIL_UUID);
                    }
                } else {
                    if (((BarrelBlockEntity) tile).getCurrentTask() != null) {
                        player.sendMessage(Translator.translatable("tile.hqm:item_barrel.boundTo", Quest.getQuest(((BarrelBlockEntity) tile).getQuestUUID()).getName()), Util.NIL_UUID);
                    } else {
                        player.sendMessage(Translator.translatable("tile.hqm:item_barrel.nonBound"), Util.NIL_UUID);
                    }
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
    
    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }
    
    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        if (state.getValue(BOUND)) {
            return 15;
        } else {
            return 0;
        }
    }
    
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
    
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return super.getStateForPlacement(ctx).setValue(BOUND, false);
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BOUND);
    }
}
