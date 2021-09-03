package hardcorequesting.forge;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.math.Matrix4f;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.config.HQMConfig;
import hardcorequesting.common.items.ModItems;
import hardcorequesting.common.platform.AbstractPlatform;
import hardcorequesting.common.platform.FluidStack;
import hardcorequesting.common.platform.NetworkManager;
import hardcorequesting.common.tileentity.AbstractBarrelBlockEntity;
import hardcorequesting.common.util.Fraction;
import hardcorequesting.forge.tileentity.BarrelBlockEntity;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.AnimalTameEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fmllegacy.server.ServerLifecycleHooks;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mod("hardcorequesting")
public class HardcoreQuestingForge implements AbstractPlatform {
    private final NetworkManager networkManager = new NetworkingManager();
    private final DeferredRegister<SoundEvent> sounds = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, HardcoreQuestingCore.ID);
    private final DeferredRegister<Block> block = DeferredRegister.create(ForgeRegistries.BLOCKS, HardcoreQuestingCore.ID);
    private final DeferredRegister<Item> item = DeferredRegister.create(ForgeRegistries.ITEMS, HardcoreQuestingCore.ID);
    private final DeferredRegister<BlockEntityType<?>> tileEntityType = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, HardcoreQuestingCore.ID);
    
    public HardcoreQuestingForge() {
        NetworkingManager.init();
        HardcoreQuestingCore.initialize(this);
    
        sounds.register(FMLJavaModLoadingContext.get().getModEventBus());
        block.register(FMLJavaModLoadingContext.get().getModEventBus());
        item.register(FMLJavaModLoadingContext.get().getModEventBus());
        tileEntityType.register(FMLJavaModLoadingContext.get().getModEventBus());
        MinecraftForge.EVENT_BUS.<LivingDropsEvent>addListener(event -> {
            if (event.getEntityLiving() instanceof Player) {
                Player player = (Player) event.getEntityLiving();
                if (player instanceof FakePlayer
                    || event.isCanceled()
                    || player.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)
                    || HQMConfig.getInstance().LOSE_QUEST_BOOK) {
                    return;
                }
                
                Iterator<ItemEntity> iter = event.getDrops().iterator();
                while (iter.hasNext()) {
                    ItemEntity entityItem = iter.next();
                    ItemStack stack = entityItem.getItem();
                    if (!stack.isEmpty() && stack.getItem().equals(ModItems.book.get())) {
                        player.getInventory().add(stack);
                        iter.remove();
                    }
                }
            }
        });
        MinecraftForge.EVENT_BUS.<PlayerEvent.Clone>addListener(event -> {
            if (event.getPlayer() == null || event.getPlayer() instanceof FakePlayer
                || !event.isWasDeath() || event.isCanceled()
                || event.getPlayer().level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)
                || HQMConfig.getInstance().LOSE_QUEST_BOOK) {
                return;
            }
            
            if (event.getOriginal().getInventory().contains(new ItemStack(ModItems.book.get()))) {
                ItemStack bookStack = new ItemStack(ModItems.book.get());
                for (ItemStack stack : event.getOriginal().getInventory().armor) {
                    if (bookStack.sameItem(stack)) {
                        bookStack = stack.copy();
                        break;
                    }
                }
                event.getPlayer().getInventory().add(bookStack);
            }
        });
    }
    
    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }
    
    @Override
    public NetworkManager getNetworkManager() {
        return networkManager;
    }
    
    @Override
    public MinecraftServer getServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }
    
    @Override
    public String getModVersion() {
        return ModList.get().getModContainerById(HardcoreQuestingCore.ID).get().getModInfo().getVersion().toString();
    }
    
    @Override
    public boolean isClient() {
        return FMLEnvironment.dist == Dist.CLIENT;
    }
    
    @Override
    public void registerOnCommandRegistration(Consumer<CommandDispatcher<CommandSourceStack>> consumer) {
        MinecraftForge.EVENT_BUS.<RegisterCommandsEvent>addListener(event -> consumer.accept(event.getDispatcher()));
    }
    
    @Override
    public void registerOnWorldLoad(BiConsumer<ResourceKey<Level>, ServerLevel> biConsumer) {
        MinecraftForge.EVENT_BUS.<WorldEvent.Load>addListener(load -> {
            if (load.getWorld() instanceof ServerLevel)
                biConsumer.accept(((Level) load.getWorld()).dimension(), (ServerLevel) load.getWorld());
        });
    }
    
    @Override
    public void registerOnWorldSave(Consumer<ServerLevel> consumer) {
        MinecraftForge.EVENT_BUS.<WorldEvent.Save>addListener(save -> {
            if (save.getWorld() instanceof ServerLevel)
                consumer.accept((ServerLevel) save.getWorld());
        });
    }
    
    @Override
    public void registerOnPlayerJoin(Consumer<ServerPlayer> consumer) {
        MinecraftForge.EVENT_BUS.<PlayerEvent.PlayerLoggedInEvent>addListener(event -> consumer.accept((ServerPlayer) event.getPlayer()));
    }
    
    @Override
    public void registerOnServerTick(Consumer<MinecraftServer> consumer) {
        MinecraftForge.EVENT_BUS.<TickEvent.ServerTickEvent>addListener(event -> {
            if(event.phase == TickEvent.Phase.END)
                consumer.accept(getServer());
        });
    }
    
    @Override
    public void registerOnClientTick(Consumer<Minecraft> consumer) {
        MinecraftForge.EVENT_BUS.<TickEvent.ClientTickEvent>addListener(event -> {
            if(event.phase == TickEvent.Phase.END)
                consumer.accept(Minecraft.getInstance());
        });
    }
    
    @Override
    public void registerOnWorldTick(Consumer<Level> consumer) {
        MinecraftForge.EVENT_BUS.<TickEvent.WorldTickEvent>addListener(event -> {
            if(event.phase == TickEvent.Phase.END)
                consumer.accept(event.world);
        });
    }
    
    @Override
    public void registerOnHudRender(BiConsumer<PoseStack, Float> biConsumer) {
        MinecraftForge.EVENT_BUS.<RenderGameOverlayEvent.Post>addListener(event -> {
            if (event.getType() == RenderGameOverlayEvent.ElementType.ALL)
                biConsumer.accept(event.getMatrixStack(), event.getPartialTicks());
        });
    }
    
    @Override
    public void registerOnUseItem(TriConsumer<Player, Level, InteractionHand> triConsumer) {
        MinecraftForge.EVENT_BUS.<PlayerInteractEvent.RightClickItem>addListener(event -> triConsumer.accept(event.getPlayer(), event.getWorld(), event.getHand()));
    }
    
    @Override
    public void registerOnBlockPlace(BlockPlaced blockPlaced) {
        MinecraftForge.EVENT_BUS.<BlockEvent.EntityPlaceEvent>addListener(event -> {
            if (event.getEntity() instanceof LivingEntity)
                blockPlaced.onBlockPlaced(event.getEntity().getCommandSenderWorld(), event.getPos(), event.getPlacedBlock(), (LivingEntity) event.getEntity());
        });
    }
    
    @Override
    public void registerOnBlockUse(BlockUsed blockUsed) {
        MinecraftForge.EVENT_BUS.<PlayerInteractEvent.RightClickBlock>addListener(event -> {
            blockUsed.onBlockUsed(event.getPlayer(), event.getWorld(), event.getHand(), event.getPos(), event.getFace());
        });
    }
    
    @Override
    public void registerOnBlockBreak(BlockBroken blockBroken) {
        MinecraftForge.EVENT_BUS.<BlockEvent.BreakEvent>addListener(event -> {
            blockBroken.onBlockBroken(event.getWorld(), event.getPos(), event.getState(), event.getPlayer());
        });
    }
    
    @Override
    public void registerOnItemPickup(BiConsumer<Player, ItemStack> biConsumer) {
        MinecraftForge.EVENT_BUS.<PlayerEvent.ItemPickupEvent>addListener(event -> {
            biConsumer.accept(event.getPlayer(), event.getStack());
        });
    }
    
    @Override
    public void registerOnLivingDeath(BiConsumer<LivingEntity, DamageSource> biConsumer) {
        MinecraftForge.EVENT_BUS.<LivingDeathEvent>addListener(event -> {
            biConsumer.accept(event.getEntityLiving(), event.getSource());
        });
    }
    
    @Override
    public void registerOnCrafting(BiConsumer<Player, ItemStack> triConsumer) {
        MinecraftForge.EVENT_BUS.<PlayerEvent.ItemCraftedEvent>addListener(event -> {
            triConsumer.accept(event.getPlayer(), event.getCrafting());
        });
    }
    
    @Override
    public void registerOnAnvilCrafting(BiConsumer<Player, ItemStack> triConsumer) {
        MinecraftForge.EVENT_BUS.<AnvilRepairEvent>addListener(event -> {
            triConsumer.accept(event.getPlayer(), event.getItemResult());
        });
    }
    
    @Override
    public void registerOnSmelting(BiConsumer<Player, ItemStack> triConsumer) {
        MinecraftForge.EVENT_BUS.<PlayerEvent.ItemSmeltedEvent>addListener(event -> {
            triConsumer.accept(event.getPlayer(), event.getSmelting());
        });
    }
    
    @Override
    public void registerOnAdvancement(BiConsumer<ServerPlayer, Advancement> biConsumer) {
        MinecraftForge.EVENT_BUS.<AdvancementEvent>addListener(event -> {
            if (event.getPlayer() instanceof ServerPlayer)
                biConsumer.accept((ServerPlayer) event.getPlayer(), event.getAdvancement());
        });
    }
    
    @Override
    public void registerOnAnimalTame(BiConsumer<Player, Entity> biConsumer) {
        MinecraftForge.EVENT_BUS.<AnimalTameEvent>addListener(event -> {
            biConsumer.accept(event.getTamer(), event.getAnimal());
        });
    }
    
    @Override
    public CompoundTag getPlayerExtraTag(Player playerEntity) {
        return playerEntity.getPersistentData();
    }
    
    @Override
    public CreativeModeTab createTab(ResourceLocation resourceLocation, Supplier<ItemStack> supplier) {
        return new CreativeModeTab(String.format("%s.%s", resourceLocation.getNamespace(), resourceLocation.getPath())) {
            @Nonnull
            @Override
            public ItemStack makeIcon() {
                return supplier.get();
            }
        };
    }
    
    @Override
    public BlockBehaviour.Properties createDeliveryBlockProperties() {
        return BlockBehaviour.Properties.of(net.minecraft.world.level.material.Material.WOOD).requiresCorrectToolForDrops().strength(1.0F);
    }
    
    @Override
    public AbstractBarrelBlockEntity createBarrelBlockEntity(BlockPos pos, BlockState state) {
        return new BarrelBlockEntity(pos, state);
    }
    
    @Override
    public void setCraftingRemainingItem(Item item, Item item1) {
        item.craftingRemainingItem = item1;
    }
    
    @Override
    public FluidStack createEmptyFluidStack() {
        return new ForgeFluidStack();
    }
    
    @Override
    public FluidStack createFluidStack(Fluid fluid, Fraction fraction) {
        return new ForgeFluidStack(new net.minecraftforge.fluids.FluidStack(fluid, fraction.intValue()));
    }
    
    @Override
    public List<FluidStack> findFluidsIn(ItemStack stack) {
        return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
                .map(HardcoreQuestingForge::getAllFluidsIn)
                .orElse(Collections.emptyList());
    }
    
    @NotNull
    private static List<FluidStack> getAllFluidsIn(IFluidHandlerItem handler) {
        List<FluidStack> fluids = new ArrayList<>();
        for (int tank = 0; tank < handler.getTanks(); tank++) {
            net.minecraftforge.fluids.FluidStack fluid = handler.getFluidInTank(tank);
            if (!fluid.isEmpty())
                fluids.add(new ForgeFluidStack(fluid));
        }
        return fluids;
    }
    
    // Private class extending RenderType as a simple workaround to access protected fields.
    @OnlyIn(Dist.CLIENT)
    private static class CustomRenderTypes extends RenderType {
        private CustomRenderTypes(String string, VertexFormat arg, VertexFormat.Mode arg2, int i, boolean bl, boolean bl2, Runnable runnable, Runnable runnable2) {
            super(string, arg, arg2, i, bl, bl2, runnable, runnable2);
            throw new IllegalStateException("This class is not meant to be constructed!");
        }
        
        private static RenderType createFluid(ResourceLocation location) {
            return RenderType.create(
                    HardcoreQuestingCore.ID + ":fluid_type",
                    DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 256, true, false,
                    RenderType.CompositeState.builder()
                            .setShaderState(RenderStateShard.POSITION_COLOR_TEX_SHADER)
                            .setLightmapState(RenderStateShard.LIGHTMAP)
                            .setTextureState(new RenderStateShard.TextureStateShard(location, false, false))
                            .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                            .createCompositeState(false));
        }
    
    }
    @OnlyIn(Dist.CLIENT)
    @Override
    public void renderFluidStack(FluidStack fluidStack, PoseStack matrices, int x1, int y1, int x2, int y2) {
        ForgeFluidStack stack = (ForgeFluidStack) fluidStack;
        FluidAttributes attributes = stack.getFluid().getAttributes();
        ResourceLocation texture = attributes.getStillTexture(stack._stack);
        Material blockMaterial = ForgeHooksClient.getBlockMaterial(texture);
        TextureAtlasSprite sprite = blockMaterial.sprite();
        int color = attributes.getColor(Minecraft.getInstance().level, BlockPos.ZERO);
        int a = 255;
        int r = (color >> 16 & 255);
        int g = (color >> 8 & 255);
        int b = (color & 255);
        MultiBufferSource.BufferSource source = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer builder = blockMaterial.buffer(source, HardcoreQuestingForge.CustomRenderTypes::createFluid);
        Matrix4f matrix = matrices.last().pose();
        builder.vertex(matrix, x2, y1, 0).color(r, g, b, a).uv(sprite.getU1(), sprite.getV0()).endVertex();
        builder.vertex(matrix, x1, y1, 0).color(r, g, b, a).uv(sprite.getU0(), sprite.getV0()).endVertex();
        builder.vertex(matrix, x1, y2, 0).color(r, g, b, a).uv(sprite.getU0(), sprite.getV1()).endVertex();
        builder.vertex(matrix, x2, y2, 0).color(r, g, b, a).uv(sprite.getU1(), sprite.getV1()).endVertex();
        source.endBatch();
    }
    
    @Override
    public Fraction getBucketAmount() {
        return Fraction.ofWhole(FluidAttributes.BUCKET_VOLUME);
    }
    
    @Override
    public <T extends Block> Supplier<T> registerBlock(String id, Supplier<T> supplier) {
        return block.register(id, supplier);
    }
    
    @Override
    public Supplier<SoundEvent> registerSound(String id, Supplier<SoundEvent> supplier) {
        return sounds.register(id, supplier);
    }
    
    @Override
    public <T extends Item> Supplier<T> registerItem(String id, Supplier<T> supplier) {
        return item.register(id, supplier);
    }
    
    @Override
    public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(String id, BiFunction<BlockPos, BlockState, T> constructor) {
        return tileEntityType.register(id, () -> BlockEntityType.Builder.of(constructor::apply).build(null));
    }
}
