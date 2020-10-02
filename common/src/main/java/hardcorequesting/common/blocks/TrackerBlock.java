package hardcorequesting.common.blocks;

import hardcorequesting.common.items.ModItems;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.tileentity.TrackerBlockEntity;
import hardcorequesting.common.util.Translator;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

public class TrackerBlock extends BaseEntityBlock {
    
    public TrackerBlock() {
        super(BlockBehaviour.Properties.of(Material.WOOD).strength(10.0F));
    }
    
    @Override
    public BlockEntity newBlockEntity(BlockGetter view) {
        return new TrackerBlockEntity();
    }
    
    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player != null) {
            if (!player.getItemInHand(hand).isEmpty() && player.getItemInHand(hand).getItem() == ModItems.book) {
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

//    @Override
//    public int isProvidingStrongPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {
//        return state.getBlock().getMetaFromState(state);
//    }
//
//    @Override
//    public int isProvidingWeakPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {
//        return state.getBlock().getMetaFromState(state);
//    }
}
