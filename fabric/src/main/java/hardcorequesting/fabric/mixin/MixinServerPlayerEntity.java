package hardcorequesting.fabric.mixin;

import com.mojang.authlib.GameProfile;
import hardcorequesting.common.config.HQMConfig;
import hardcorequesting.common.items.QuestBookItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayerEntity extends Player {
    @Shadow @Final public ServerPlayerGameMode gameMode;
    
    public MixinServerPlayerEntity(Level world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }
    
    @Inject(method = "restoreFrom",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/network/syncher/SynchedEntityData;set(Lnet/minecraft/network/syncher/EntityDataAccessor;Ljava/lang/Object;)V",
                     ordinal = 0))
    private void copyFrom(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
        if (!HQMConfig.getInstance().LOSE_QUEST_BOOK) return;
        if (!alive && !oldPlayer.isSpectator() && !this.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            int invSize = oldPlayer.inventory.getContainerSize();
            for (int i = 0; i < invSize; i++) {
                ItemStack stack = oldPlayer.inventory.getItem(i);
                if (stack.getItem() instanceof QuestBookItem) {
                    this.inventory.setItem(i, stack);
                }
            }
        }
    }
}
