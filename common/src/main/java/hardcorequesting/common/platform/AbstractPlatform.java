package hardcorequesting.common.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.CommandDispatcher;
import dev.architectury.fluid.FluidStack;
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
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.apache.logging.log4j.util.TriConsumer;

import java.nio.file.Path;
import java.util.List;
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
    
    AbstractBarrelBlockEntity createBarrelBlockEntity(BlockPos pos, BlockState state);

    /**
     * Obtains any fluids contained in the item stack.
     * If there is no fluid in the item stack, an empty list will be returned.
     */
    List<FluidStack> findFluidsIn(ItemStack stack);

    Fraction getBucketAmount();
    
    <T extends Block> Supplier<T> registerBlock(String id, Supplier<T> block);
    
    Supplier<SoundEvent> registerSound(String id, Supplier<SoundEvent> sound);
    
    <T extends Item> Supplier<T> registerItem(String id, Supplier<T> item);
    
    <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(String id, BiFunction<BlockPos, BlockState, T> constructor, Supplier<Block> validBlock);
    
    Supplier<RecipeSerializer<?>> registerBookRecipeSerializer(String id);
    
}
