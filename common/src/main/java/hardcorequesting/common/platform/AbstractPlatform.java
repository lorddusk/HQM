package hardcorequesting.common.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.CommandDispatcher;
import hardcorequesting.common.tileentity.AbstractBarrelBlockEntity;
import hardcorequesting.common.util.Fraction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.logging.log4j.util.TriConsumer;

import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface AbstractPlatform {
    Path getConfigDir();
    
    NetworkManager getNetworkManager();
    
    MinecraftServer getServer();
    
    String getModVersion();
    
    boolean isClient();
    
    void registerOnCommandRegistration(Consumer<CommandDispatcher<CommandSourceStack>> dispatcherConsumer);
    
    void registerOnWorldLoad(BiConsumer<ResourceKey<Level>, ServerLevel> consumer);
    
    void registerOnWorldSave(Consumer<ServerLevel> consumer);
    
    void registerOnPlayerJoin(Consumer<ServerPlayer> consumer);
    
    void registerOnServerTick(Consumer<MinecraftServer> consumer);
    
    @Environment(EnvType.CLIENT)
    void registerOnClientTick(Consumer<Minecraft> consumer);
    
    void registerOnWorldTick(Consumer<Level> consumer);
    
    @Environment(EnvType.CLIENT)
    void registerOnHudRender(BiConsumer<PoseStack, Float> consumer);
    
    void registerOnUseItem(TriConsumer<Player, Level, InteractionHand> consumer);
    
    void registerOnBlockPlace(BlockPlaced consumer);
    
    interface BlockPlaced {
        void onBlockPlaced(Level world, BlockPos pos, BlockState state, LivingEntity placer);
    }
    
    void registerOnBlockUse(BlockUsed consumer);
    
    interface BlockUsed {
        void onBlockUsed(Player playerEntity, Level world, InteractionHand hand, BlockPos pos, Direction face);
    }
    
    void registerOnBlockBreak(BlockBroken consumer);
    
    interface BlockBroken {
        
        void onBlockBroken(LevelAccessor world, BlockPos pos, BlockState state, Player player);
    }
    
    void registerOnItemPickup(BiConsumer<Player, ItemStack> consumer);
    
    void registerOnLivingDeath(BiConsumer<LivingEntity, DamageSource> consumer);
    
    void registerOnCrafting(BiConsumer<Player, ItemStack> consumer);
    
    void registerOnAnvilCrafting(BiConsumer<Player, ItemStack> consumer);
    
    void registerOnSmelting(BiConsumer<Player, ItemStack> consumer);
    
    void registerOnAdvancement(BiConsumer<ServerPlayer, Advancement> consumer);
    
    void registerOnAnimalTame(BiConsumer<Player, Entity> consumer);
    
    CompoundTag getPlayerExtraTag(Player player);
    
    CreativeModeTab createTab(ResourceLocation name, Supplier<ItemStack> icon);
    
    BlockBehaviour.Properties createDeliveryBlockProperties();
    
    AbstractBarrelBlockEntity createBarrelBlockEntity(BlockPos pos, BlockState state);
    
    void setCraftingRemainingItem(Item item, Item craftingRemainingItem);
    
    LevelStorageSource.LevelStorageAccess getStorageSourceOfServer(MinecraftServer server);
    
    FluidStack createEmptyFluidStack();
    
    FluidStack createFluidStack(Fluid fluid, Fraction amount);
    
    /**
     * Obtains the fluid contained in the item stack.
     * If there is no fluid in the item stack, an empty fluid stack will be returned.
     * Only the fluid type is guaranteed to match. The amount returned might not match the amount in the item stack.
     */
    FluidStack findFluidIn(ItemStack stack);
    
    @Environment(EnvType.CLIENT)
    void renderFluidStack(FluidStack fluid, PoseStack stack, int x1, int y1, int x2, int y2);
    
    Fraction getBucketAmount();
    
    Block getBlock(ResourceLocation location);
    
    SoundEvent getSoundEvent(ResourceLocation location);
    
    Item getItem(ResourceLocation location);
    
    BlockEntityType<?> getBlockEntity(ResourceLocation location);
    
    void registerBlock(ResourceLocation location, Supplier<Block> block);
    
    void registerSound(ResourceLocation location, Supplier<SoundEvent> sound);
    
    void registerItem(ResourceLocation location, Supplier<Item> item);
    
    void registerBlockEntity(ResourceLocation location, BiFunction<BlockPos, BlockState, ? extends BlockEntity> constructor);
}
