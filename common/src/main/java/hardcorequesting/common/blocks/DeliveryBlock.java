package hardcorequesting.common.blocks;

import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.items.QuestBookItem;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.tileentity.AbstractBarrelBlockEntity;
import hardcorequesting.common.util.Translator;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class DeliveryBlock extends BaseEntityBlock {
    
    public static final BooleanProperty BOUND = BooleanProperty.create("bound");
    
    public DeliveryBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(BOUND, false));
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return HardcoreQuestingCore.platform.createBarrelBlockEntity(pos, state);
    }
    
    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!world.isClientSide) {
            ItemStack hold = player.getItemInHand(hand);
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof AbstractBarrelBlockEntity) {
                if (!hold.isEmpty() && hold.getItem() instanceof QuestBookItem) {
                    ((AbstractBarrelBlockEntity) tile).storeSettings(player);
                    if (((AbstractBarrelBlockEntity) tile).getCurrentTask() != null) {
                        player.sendSystemMessage(Translator.translatable("tile.hqm:item_barrel.bindTo", Quest.getQuest(((AbstractBarrelBlockEntity) tile).getQuestUUID()).getName()));
                    } else {
                        player.sendSystemMessage(Translator.translatable("hqm.message.noTaskSelected"));
                    }
                } else {
                    if (((AbstractBarrelBlockEntity) tile).getCurrentTask() != null) {
                        player.sendSystemMessage(Translator.translatable("tile.hqm:item_barrel.boundTo", Quest.getQuest(((AbstractBarrelBlockEntity) tile).getQuestUUID()).getName()));
                    } else {
                        player.sendSystemMessage(Translator.translatable("tile.hqm:item_barrel.nonBound"));
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
