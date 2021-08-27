package hardcorequesting.fabric;

import alexiil.mc.lib.attributes.fluid.FluidItemUtil;
import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.CommandDispatcher;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.platform.AbstractPlatform;
import hardcorequesting.common.platform.FluidStack;
import hardcorequesting.common.platform.NetworkManager;
import hardcorequesting.common.tileentity.AbstractBarrelBlockEntity;
import hardcorequesting.common.util.Fraction;
import hardcorequesting.fabric.capabilities.ModCapabilities;
import hardcorequesting.fabric.tileentity.BarrelBlockEntity;
import me.shedaniel.cloth.api.common.events.v1.*;
import me.shedaniel.cloth.api.utils.v1.GameInstanceUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.logging.log4j.util.TriConsumer;

import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class HardcoreQuestingFabric implements ModInitializer, AbstractPlatform {
    public static final List<BiConsumer<LivingEntity, DamageSource>> LIVING_DEATH = Lists.newArrayList();
    public static final List<BiConsumer<Player, ItemStack>> CRAFTING = Lists.newArrayList();
    public static final List<BiConsumer<ServerPlayer, Advancement>> ADVANCEMENT = Lists.newArrayList();
    public static final List<BiConsumer<Player, Entity>> ANIMAL_TAME = Lists.newArrayList();
    private final NetworkManager networkManager = new FabricNetworkManager();
    
    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }
    
    @Override
    public void onInitialize() {
        HardcoreQuestingCore.initialize(this);
        ModCapabilities.init();
    }
    
    @Override
    public NetworkManager getNetworkManager() {
        return networkManager;
    }
    
    @Override
    public MinecraftServer getServer() {
        return GameInstanceUtils.getServer();
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
        CommandRegistrationCallback.EVENT.register((commandDispatcher, b) -> dispatcherConsumer.accept(commandDispatcher));
    }
    
    @Override
    public void registerOnWorldLoad(BiConsumer<ResourceKey<Level>, ServerLevel> consumer) {
        WorldLoadCallback.EVENT.register(consumer::accept);
    }
    
    @Override
    public void registerOnWorldSave(Consumer<ServerLevel> consumer) {
        WorldSaveCallback.EVENT.register((world, listener, flush) -> consumer.accept(world));
    }
    
    @Override
    public void registerOnPlayerJoin(Consumer<ServerPlayer> consumer) {
        PlayerJoinCallback.EVENT.register((connection, playerEntity) -> consumer.accept(playerEntity));
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
    public void registerOnHudRender(BiConsumer<PoseStack, Float> consumer) {
        HudRenderCallback.EVENT.register(consumer::accept);
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
        BlockPlaceCallback.EVENT.register((world, pos, state, placer, itemStack) -> {
            consumer.onBlockPlaced(world, pos, state, placer);
            return InteractionResult.PASS;
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
        BlockBreakCallback.EVENT.register(consumer::onBlockBroken);
    }
    
    @Override
    public void registerOnItemPickup(BiConsumer<Player, ItemStack> consumer) {
        ItemPickupCallback.EVENT.register(consumer::accept);
    }
    
    @Override
    public void registerOnLivingDeath(BiConsumer<LivingEntity, DamageSource> consumer) {
        LIVING_DEATH.add(consumer);
    }
    
    @Override
    public void registerOnCrafting(BiConsumer<Player, ItemStack> consumer) {
        CRAFTING.add(consumer);
    }
    
    @Override
    public void registerOnAnvilCrafting(BiConsumer<Player, ItemStack> consumer) {
        CRAFTING.add(consumer);
    }
    
    @Override
    public void registerOnSmelting(BiConsumer<Player, ItemStack> consumer) {
        CRAFTING.add(consumer);
    }
    
    @Override
    public void registerOnAdvancement(BiConsumer<ServerPlayer, Advancement> consumer) {
        ADVANCEMENT.add(consumer);
    }
    
    @Override
    public void registerOnAnimalTame(BiConsumer<Player, Entity> consumer) {
        ANIMAL_TAME.add(consumer);
    }
    
    @Override
    public CompoundTag getPlayerExtraTag(Player player) {
        return ModCapabilities.PLAYER_EXTRA_DATA.get(player).tag;
    }
    
    @Override
    public CreativeModeTab createTab(ResourceLocation name, Supplier<ItemStack> icon) {
        return FabricItemGroupBuilder.create(name).icon(icon).build();
    }
    
    @Override
    public BlockBehaviour.Properties createDeliveryBlockProperties() {
        return FabricBlockSettings.of(Material.WOOD).requiresTool().hardness(1.0F).breakByTool(FabricToolTags.AXES, 0);
    }
    
    @Override
    public AbstractBarrelBlockEntity createBarrelBlockEntity() {
        return new BarrelBlockEntity();
    }
    
    @Override
    public void setCraftingRemainingItem(Item item, Item craftingRemainingItem) {
        item.craftingRemainingItem = craftingRemainingItem;
    }
    
    @Override
    public LevelStorageSource.LevelStorageAccess getStorageSourceOfServer(MinecraftServer server) {
        return server.storageSource;
    }
    
    @Override
    public FluidStack createEmptyFluidStack() {
        return new FabricFluidStack(FluidVolumeUtil.EMPTY);
    }
    
    @Override
    public FluidStack createFluidStack(Fluid fluid, Fraction amount) {
        return new FabricFluidStack(FluidKeys.get(fluid).withAmount(FluidAmount.of(amount.getNumerator(), amount.getDenominator())));
    }
    
    @Override
    public FluidStack findFluidIn(ItemStack stack) {
        FluidKey fluid = FluidItemUtil.getContainedFluid(stack);
        return new FabricFluidStack(fluid.withAmount(fluid.entry.isEmpty() ? FluidAmount.ZERO : FluidAmount.ONE));
    }
    
    @Override
    public void renderFluidStack(FluidStack fluid, PoseStack stack, int x1, int y1, int x2, int y2) {
        ((FabricFluidStack) fluid)._volume.renderGuiRect(x1, y1, x2, y2);
    }
    
    @Override
    public Fraction getBucketAmount() {
        return Fraction.ofWhole(1);
    }
    
    @Override
    public Block getBlock(ResourceLocation location) {
        return Registry.BLOCK.get(location);
    }
    
    @Override
    public SoundEvent getSoundEvent(ResourceLocation location) {
        return Registry.SOUND_EVENT.get(location);
    }
    
    @Override
    public Item getItem(ResourceLocation location) {
        return Registry.ITEM.get(location);
    }
    
    @Override
    public BlockEntityType<?> getBlockEntity(ResourceLocation location) {
        return Registry.BLOCK_ENTITY_TYPE.get(location);
    }
    
    @Override
    public void registerBlock(ResourceLocation location, Supplier<Block> block) {
        Registry.register(Registry.BLOCK, location, block.get());
    }
    
    @Override
    public void registerSound(ResourceLocation location, Supplier<SoundEvent> sound) {
        Registry.register(Registry.SOUND_EVENT, location, sound.get());
    }
    
    @Override
    public void registerItem(ResourceLocation location, Supplier<Item> item) {
        Registry.register(Registry.ITEM, location, item.get());
    }
    
    @Override
    public void registerBlockEntity(ResourceLocation location, Supplier<BlockEntityType<?>> type) {
        Registry.register(Registry.BLOCK_ENTITY_TYPE, location, type.get());
    }
}
