package hardcorequesting.blocks;

import hardcorequesting.items.ModItems;
import hardcorequesting.quests.Quest;
import hardcorequesting.tileentity.TrackerBlockEntity;
import hardcorequesting.util.Translator;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class TrackerBlock extends BlockWithEntity {
    
    public TrackerBlock() {
        super(FabricBlockSettings.of(Material.WOOD)
                .hardness(10.0F));
    }
    
    @Override
    public BlockEntity createBlockEntity(BlockView view) {
        return new TrackerBlockEntity();
    }
    
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (player != null) {
            if (!player.getStackInHand(hand).isEmpty() && player.getStackInHand(hand).getItem() == ModItems.book) {
                if (!world.isClient) {
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
                if (!world.isClient) {
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
            return ActionResult.SUCCESS;
        }
        return ActionResult.CONSUME;
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }
    
    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
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
