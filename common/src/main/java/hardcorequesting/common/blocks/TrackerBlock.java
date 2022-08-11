package hardcorequesting.common.blocks;

import hardcorequesting.common.items.ModItems;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.tileentity.TrackerBlockEntity;
import hardcorequesting.common.util.Translator;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class TrackerBlock extends BaseEntityBlock {
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    
    public TrackerBlock() {
        super(BlockBehaviour.Properties.of(Material.WOOD).strength(10.0F));
        registerDefaultState(this.stateDefinition.any().setValue(POWER, 0));
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
    
    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player != null) {
            if (!player.getItemInHand(hand).isEmpty() && player.getItemInHand(hand).getItem() == ModItems.book.get()) {
                if (!world.isClientSide) {
                    BlockEntity tile = world.getBlockEntity(pos);
                    if (tile instanceof TrackerBlockEntity) {
                        if (!Quest.canQuestsBeEdited()) {
                            player.sendMessage(Translator.translatable("tile.hqm:quest_tracker.offLimit"), Util.NIL_UUID);
                        } else {
                            ((TrackerBlockEntity) tile).setCurrentQuest();
                            if (((TrackerBlockEntity) tile).getCurrentQuest() != null) {
                                player.sendMessage(Translator.translatable("tile.hqm:quest_tracker.bindTo", ((TrackerBlockEntity) tile).getCurrentQuest().getName()), Util.NIL_UUID);
                            } else {
                                player.sendMessage(Translator.translatable("hqm.message.noTaskSelected"), Util.NIL_UUID);
                            }
                        }
                        
                    }
                }
            } else {
                if (!world.isClientSide) {
                    BlockEntity tile = world.getBlockEntity(pos);
                    if (tile instanceof TrackerBlockEntity) {
                        if (!Quest.canQuestsBeEdited()) {
                            player.sendMessage(Translator.translatable("tile.hqm:quest_tracker.offLimit"), Util.NIL_UUID);
                        } else {
                            ((TrackerBlockEntity) tile).openInterface(player);
                        }
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }
    
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public int getSignal(BlockState state, BlockGetter blockGetter, BlockPos pos, Direction direction) {
        return state.getValue(POWER);
    }
}
