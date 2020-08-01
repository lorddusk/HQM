package hardcorequesting.mixin;

import com.mojang.authlib.GameProfile;
import hardcorequesting.config.HQMConfig;
import hardcorequesting.items.QuestBookItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity extends PlayerEntity {
    @Shadow @Final public ServerPlayerInteractionManager interactionManager;
    
    public MixinServerPlayerEntity(World world, BlockPos blockPos, GameProfile gameProfile) {
        super(world, blockPos, gameProfile);
    }
    
    @Inject(method = "copyFrom",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/data/DataTracker;set(Lnet/minecraft/entity/data/TrackedData;Ljava/lang/Object;)V",
                     ordinal = 0))
    private void copyFrom(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        if (!HQMConfig.getInstance().LOSE_QUEST_BOOK) return;
        if (!alive && !oldPlayer.isSpectator() && !this.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
            int invSize = oldPlayer.inventory.getInvSize();
            for (int i = 0; i < invSize; i++) {
                ItemStack stack = oldPlayer.inventory.getInvStack(i);
                if (stack.getItem() instanceof QuestBookItem) {
                    this.inventory.setInvStack(i, stack);
                }
            }
        }
    }
}
