package hardcorequesting.common.blocks;

import com.mojang.serialization.MapCodec;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.items.QuestBookItem;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.tileentity.AbstractBarrelBlockEntity;
import hardcorequesting.common.util.Translator;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
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

@MethodsReturnNonnullByDefault
public final class DeliveryBlock extends BaseEntityBlock {
    public static final MapCodec<DeliveryBlock> CODEC = simpleCodec(DeliveryBlock::new);

    public static final BooleanProperty BOUND = BooleanProperty.create("bound");
    
    public DeliveryBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(BOUND, false));
    }

    @Override
    protected MapCodec<DeliveryBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return HardcoreQuestingCore.platform.createBarrelBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (itemStack.getItem() instanceof QuestBookItem) {
            if (!level.isClientSide) {
                BlockEntity tile = level.getBlockEntity(blockPos);
                if (tile instanceof AbstractBarrelBlockEntity barrel) {
                    barrel.storeSettings(player);
                    if (barrel.getCurrentTask() != null) {
                        player.sendSystemMessage(Translator.translatable("tile.hqm:item_barrel.bindTo", Quest.getQuest(barrel.getQuestUUID()).getName()));
                    } else {
                        player.sendSystemMessage(Translator.translatable("hqm.message.noTaskSelected"));
                    }
                }
            }
            return ItemInteractionResult.SUCCESS;
        } else {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (!level.isClientSide) {
            BlockEntity tile = level.getBlockEntity(blockPos);
            if (tile instanceof AbstractBarrelBlockEntity barrel) {
                if (barrel.getCurrentTask() != null) {
                    player.sendSystemMessage(Translator.translatable("tile.hqm:item_barrel.boundTo", Quest.getQuest(barrel.getQuestUUID()).getName()));
                } else {
                    player.sendSystemMessage(Translator.translatable("tile.hqm:item_barrel.nonBound"));
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }
    
    @Override
    protected int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        if (state.getValue(BOUND)) {
            return 15;
        } else {
            return 0;
        }
    }
    
    @Override
    protected RenderShape getRenderShape(BlockState state) {
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
