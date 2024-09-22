package hardcorequesting.common.blocks;

import com.mojang.serialization.MapCodec;
import hardcorequesting.common.items.ModItems;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.tileentity.TrackerBlockEntity;
import hardcorequesting.common.util.Translator;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

@MethodsReturnNonnullByDefault
public final class TrackerBlock extends BaseEntityBlock {
    public static final MapCodec<TrackerBlock> CODEC = MapCodec.unit(TrackerBlock::new);

    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    
    public TrackerBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(10.0F));
        registerDefaultState(this.stateDefinition.any().setValue(POWER, 0));
    }

    @Override
    protected MapCodec<TrackerBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWER);
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TrackerBlockEntity(pos, state);
    }
    
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlocks.typeTracker.get(), TrackerBlockEntity::tick);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (player != null) {
            if (itemStack.is(ModItems.book.get())) {
                if (!level.isClientSide) {
                    BlockEntity tile = level.getBlockEntity(blockPos);
                    if (tile instanceof TrackerBlockEntity tracker) {
                        if (!Quest.canQuestsBeEdited()) {
                            player.sendSystemMessage(Translator.translatable("tile.hqm:quest_tracker.offLimit"));
                        } else {
                           tracker.setCurrentQuest();
                            if (tracker.getCurrentQuest() != null) {
                                player.sendSystemMessage(Translator.translatable("tile.hqm:quest_tracker.bindTo", tracker.getCurrentQuest().getName()));
                            } else {
                                player.sendSystemMessage(Translator.translatable("hqm.message.noTaskSelected"));
                            }
                        }
                    }
                }
                return ItemInteractionResult.SUCCESS;
            } else {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
        }
        return ItemInteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (player != null) {
            if (!level.isClientSide) {
                BlockEntity tile = level.getBlockEntity(blockPos);
                if (tile instanceof TrackerBlockEntity) {
                    if (!Quest.canQuestsBeEdited()) {
                        player.sendSystemMessage(Translator.translatable("tile.hqm:quest_tracker.offLimit"));
                    } else {
                        ((TrackerBlockEntity) tile).openInterface(player);
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }
    
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
    
    @Override
    protected int getSignal(BlockState state, BlockGetter blockGetter, BlockPos pos, Direction direction) {
        return state.getValue(POWER);
    }
}
