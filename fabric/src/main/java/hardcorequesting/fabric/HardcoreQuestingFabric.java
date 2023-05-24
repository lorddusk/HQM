package hardcorequesting.fabric;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.fluid.FluidStack;
import dev.architectury.utils.GameInstance;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.blocks.ModBlocks;
import hardcorequesting.common.config.HQMConfig;
import hardcorequesting.common.items.ModCreativeTabs;
import hardcorequesting.common.items.ModItems;
import hardcorequesting.common.platform.AbstractPlatform;
import hardcorequesting.common.platform.NetworkManager;
import hardcorequesting.common.recipe.BookCatalystRecipeSerializer;
import hardcorequesting.common.tileentity.AbstractBarrelBlockEntity;
import hardcorequesting.common.util.Fraction;
import hardcorequesting.fabric.capabilities.ModCapabilities;
import hardcorequesting.fabric.tileentity.BarrelBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.util.TriConsumer;

import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class HardcoreQuestingFabric implements ModInitializer, AbstractPlatform {
    public static final List<BiConsumer<Player, ItemStack>> ANVIL_CRAFTING = Lists.newArrayList();
    private final NetworkManager networkManager = new FabricNetworkManager();
    
    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }
    
    @Override
    public void onInitialize() {
        HardcoreQuestingCore.initialize(this);

        //As of writing, architectury has misnamed these player parameters, with the first one called oldPlayer, while it actually is the second one that is the old player
        PlayerEvent.PLAYER_CLONE.register((newPlayer, oldPlayer, wonGame) -> {
            if (HQMConfig.getInstance().LOSE_QUEST_BOOK) return;
            if (!wonGame && !oldPlayer.isSpectator() && !newPlayer.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
                int invSize = oldPlayer.getInventory().getContainerSize();
                for (int i = 0; i < invSize; i++) {
                    ItemStack stack = oldPlayer.getInventory().getItem(i);
                    if (stack.is(ModItems.book.get())) {
                        newPlayer.getInventory().setItem(i, stack);
                    }
                }
            }
        });
        //noinspection UnstableApiUsage
        FluidStorage.SIDED.registerForBlockEntity((blockEntity, direction) -> ((BarrelBlockEntity) blockEntity).fluidTank, ModBlocks.typeBarrel.get());
    }
    
    @Override
    public NetworkManager getNetworkManager() {
        return networkManager;
    }
    
    @Override
    public MinecraftServer getServer() {
        return GameInstance.getServer();
    }
    
    @Override
    public String getModVersion() {
        return FabricLoader.getInstance().getModContainer(HardcoreQuestingCore.ID).get().getMetadata().getVersion().getFriendlyString();
    }
    
    @Override
    public boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }
    
    @Override
    public void registerOnCommandRegistration(Consumer<CommandDispatcher<CommandSourceStack>> dispatcherConsumer) {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcherConsumer.accept(dispatcher));
    }
    
    @Override
    public void registerOnWorldLoad(BiConsumer<ResourceKey<Level>, ServerLevel> consumer) {
        LifecycleEvent.SERVER_LEVEL_LOAD.register(level -> consumer.accept(level.dimension(), level));
    }
    
    @Override
    public void registerOnWorldSave(Consumer<ServerLevel> consumer) {
        LifecycleEvent.SERVER_LEVEL_SAVE.register(consumer::accept);
    }
    
    @Override
    public void registerOnPlayerJoin(Consumer<ServerPlayer> consumer) {
        PlayerEvent.PLAYER_JOIN.register(consumer::accept);
    }
    
    @Override
    public void registerOnServerTick(Consumer<MinecraftServer> consumer) {
        ServerTickEvents.END_SERVER_TICK.register(consumer::accept);
    }
    
    @Override
    public void registerOnClientTick(Consumer<Minecraft> consumer) {
        ClientTickEvents.END_CLIENT_TICK.register(consumer::accept);
    }
    
    @Override
    public void registerOnWorldTick(Consumer<Level> consumer) {
        ServerTickEvents.END_WORLD_TICK.register(consumer::accept);
    }

    @Override
    public void registerOnUseItem(TriConsumer<Player, Level, InteractionHand> consumer) {
        UseItemCallback.EVENT.register((player, level, hand) -> {
            consumer.accept(player, level, hand);
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        });
    }
    
    @Override
    public void registerOnBlockPlace(BlockPlaced consumer) {
        BlockEvent.PLACE.register((level, pos, state, placer) -> {
            if (placer instanceof LivingEntity entity)
                consumer.onBlockPlaced(level, pos, state, entity);
            return EventResult.pass();
        });
    }
    
    @Override
    public void registerOnBlockUse(BlockUsed consumer) {
        UseBlockCallback.EVENT.register((player, level, interactionHand, blockHitResult) -> {
            consumer.onBlockUsed(player, level, interactionHand, blockHitResult.getBlockPos(), blockHitResult.getDirection());
            return InteractionResult.PASS;
        });
    }
    
    @Override
    public void registerOnBlockBreak(BlockBroken consumer) {
        BlockEvent.BREAK.register((level, pos, state, player, xp) -> {
            consumer.onBlockBroken(level, pos, state, player);
            return EventResult.pass();
        });
    }
    
    @Override
    public void registerOnItemPickup(BiConsumer<Player, ItemStack> consumer) {
        PlayerEvent.PICKUP_ITEM_POST.register((player, entity, stack) -> consumer.accept(player, stack));
    }
    
    @Override
    public void registerOnLivingDeath(BiConsumer<LivingEntity, DamageSource> consumer) {
        EntityEvent.LIVING_DEATH.register((entity, source) -> {
            consumer.accept(entity, source);
            return EventResult.pass();
        });
    }
    
    @Override
    public void registerOnCrafting(BiConsumer<Player, ItemStack> consumer) {
        PlayerEvent.CRAFT_ITEM.register((player, constructed, inventory) -> consumer.accept(player, constructed));
    }
    
    @Override
    public void registerOnAnvilCrafting(BiConsumer<Player, ItemStack> consumer) {
        ANVIL_CRAFTING.add(consumer);
    }
    
    @Override
    public void registerOnSmelting(BiConsumer<Player, ItemStack> consumer) {
        PlayerEvent.SMELT_ITEM.register(consumer::accept);
    }
    
    @Override
    public void registerOnAdvancement(BiConsumer<ServerPlayer, Advancement> consumer) {
        PlayerEvent.PLAYER_ADVANCEMENT.register(consumer::accept);
    }
    
    @Override
    public void registerOnAnimalTame(BiConsumer<Player, Entity> consumer) {
        EntityEvent.ANIMAL_TAME.register((animal, player) -> {
            consumer.accept(player, animal);
            return EventResult.pass();
        });
    }
    
    @Override
    public CompoundTag getPlayerExtraTag(Player player) {
        return ModCapabilities.PLAYER_EXTRA_DATA.get(player).tag;
    }

    @Override
    public CreativeModeTab createTab(ResourceLocation name, Supplier<ItemStack> icon) {
        return ModCreativeTabs.HQMTab.get();
    }

    @Override
    public AbstractBarrelBlockEntity createBarrelBlockEntity(BlockPos pos, BlockState state) {
        return new BarrelBlockEntity(pos, state);
    }

    @Override
    public List<FluidStack> findFluidsIn(ItemStack stack) {
        Storage<FluidVariant> storageViews = FluidStorage.ITEM.find(stack, ContainerItemContext.withConstant(stack));
        if (storageViews != null) {
            List<FluidStack> fluids = new ArrayList<>();
            for (StorageView<FluidVariant> view : storageViews) {
                fluids.add(FluidStack.create(view.getResource().getFluid(), view.getAmount()));
            }
            return fluids;
        }
        return List.of();
    }

    @Override
    public Fraction getBucketAmount() {
        return Fraction.of(1, 0, 1);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Block> Supplier<T> registerBlock(String id, Supplier<T> block) {
        ResourceLocation location = new ResourceLocation(HardcoreQuestingCore.ID, id);
        Registry.register(BuiltInRegistries.BLOCK, location, block.get());
        return () -> (T) BuiltInRegistries.BLOCK.get(location);
    }
    
    @Override
    public Supplier<SoundEvent> registerSound(String id, Supplier<SoundEvent> sound) {
        ResourceLocation location = new ResourceLocation(HardcoreQuestingCore.ID, id);
        Registry.register(BuiltInRegistries.SOUND_EVENT, location, sound.get());
        return () -> BuiltInRegistries.SOUND_EVENT.get(location);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Item> Supplier<T> registerItem(String id, Supplier<T> item) {
        ResourceLocation location = new ResourceLocation(HardcoreQuestingCore.ID, id);
        Registry.register(BuiltInRegistries.ITEM, location, item.get());
        return () -> (T) BuiltInRegistries.ITEM.get(location);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(String id, BiFunction<BlockPos, BlockState, T> constructor, Supplier<Block> validBlock) {
        ResourceLocation location = new ResourceLocation(HardcoreQuestingCore.ID, id);
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, location, FabricBlockEntityTypeBuilder.create(constructor::apply, validBlock.get()).build(null));
        return () -> (BlockEntityType<T>) BuiltInRegistries.BLOCK_ENTITY_TYPE.get(location);
    }
    
    @Override
    public Supplier<RecipeSerializer<?>> registerBookRecipeSerializer(String id) {
        ResourceLocation location = new ResourceLocation(HardcoreQuestingCore.ID, id);
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, location, new BookCatalystRecipeSerializer());
        return () -> BuiltInRegistries.RECIPE_SERIALIZER.get(location);
    }
}
