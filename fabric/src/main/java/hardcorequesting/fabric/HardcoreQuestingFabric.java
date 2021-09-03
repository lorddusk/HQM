package hardcorequesting.fabric;

import alexiil.mc.lib.attributes.fluid.FluidAttributes;
import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil;
import alexiil.mc.lib.attributes.fluid.GroupedFluidInvView;
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
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;
import org.apache.logging.log4j.util.TriConsumer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
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
    public AbstractBarrelBlockEntity createBarrelBlockEntity(BlockPos pos, BlockState state) {
        return new BarrelBlockEntity(pos, state);
    }
    
    @Override
    public void setCraftingRemainingItem(Item item, Item craftingRemainingItem) {
        item.craftingRemainingItem = craftingRemainingItem;
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
    public List<FluidStack> findFluidsIn(ItemStack stack) {
        GroupedFluidInvView inv = FluidAttributes.GROUPED_INV_VIEW.get(stack);
        Set<FluidKey> fluidTypes = inv.getStoredFluids();
        if (fluidTypes.isEmpty())
            return Collections.emptyList();
        else {
            List<FluidStack> fluids = new ArrayList<>();
            for (FluidKey fluid : fluidTypes)
                fluids.add(new FabricFluidStack(fluid.withAmount(inv.getAmount_F(fluid))));
            return fluids;
        }
    }
    
    @Override
    public void renderFluidStack(FluidStack fluid, PoseStack stack, int x1, int y1, int x2, int y2) {
        ((FabricFluidStack) fluid)._volume.renderGuiRect(x1, y1, x2, y2);
    }
    
    @Override
    public Fraction getBucketAmount() {
        return Fraction.of(FluidAmount.BUCKET.whole, FluidAmount.BUCKET.numerator, FluidAmount.BUCKET.denominator);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Block> Supplier<T> registerBlock(String id, Supplier<T> block) {
        ResourceLocation location = new ResourceLocation(HardcoreQuestingCore.ID, id);
        Registry.register(Registry.BLOCK, location, block.get());
        return () -> (T) Registry.BLOCK.get(location);
    }
    
    @Override
    public Supplier<SoundEvent> registerSound(String id, Supplier<SoundEvent> sound) {
        ResourceLocation location = new ResourceLocation(HardcoreQuestingCore.ID, id);
        Registry.register(Registry.SOUND_EVENT, location, sound.get());
        return () -> Registry.SOUND_EVENT.get(location);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Item> Supplier<T> registerItem(String id, Supplier<T> item) {
        ResourceLocation location = new ResourceLocation(HardcoreQuestingCore.ID, id);
        Registry.register(Registry.ITEM, location, item.get());
        return () -> (T) Registry.ITEM.get(location);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(String id, BiFunction<BlockPos, BlockState, T> constructor) {
        ResourceLocation location = new ResourceLocation(HardcoreQuestingCore.ID, id);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, location, FabricBlockEntityTypeBuilder.create(constructor::apply).build(null));
        return () -> (BlockEntityType<T>) Registry.BLOCK_ENTITY_TYPE.get(location);
    }
    
    @Override
    public Supplier<RecipeSerializer<?>> registerBookRecipeSerializer(String id) {
        ResourceLocation location = new ResourceLocation(HardcoreQuestingCore.ID, id);
        Registry.register(Registry.RECIPE_SERIALIZER, location, new BookCatalystRecipeSerializer());
        return () -> Registry.RECIPE_SERIALIZER.get(location);
    }
}
