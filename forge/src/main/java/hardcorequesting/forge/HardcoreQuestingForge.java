package hardcorequesting.forge;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.brigadier.CommandDispatcher;
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
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.SaveFormat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolType;
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
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.util.TriConsumer;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mod("hardcorequesting")
public class HardcoreQuestingForge implements AbstractPlatform {
    private final NetworkManager networkManager = new NetworkingManager();
    
    public HardcoreQuestingForge() {
        NetworkingManager.init();
        HardcoreQuestingCore.initialize(this);
        
        MinecraftForge.EVENT_BUS.<LivingDropsEvent>addListener(event -> {
            if (event.getEntityLiving() instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) event.getEntityLiving();
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
                    if (!stack.isEmpty() && stack.getItem().equals(ModItems.book)) {
                        player.inventory.add(stack);
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
            
            if (event.getOriginal().inventory.contains(new ItemStack(ModItems.book))) {
                ItemStack bookStack = new ItemStack(ModItems.book);
                for (ItemStack stack : event.getOriginal().inventory.armor) {
                    if (bookStack.sameItem(stack)) {
                        bookStack = stack.copy();
                        break;
                    }
                }
                event.getPlayer().inventory.add(bookStack);
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
    public void registerOnCommandRegistration(Consumer<CommandDispatcher<CommandSource>> consumer) {
        MinecraftForge.EVENT_BUS.<RegisterCommandsEvent>addListener(event -> consumer.accept(event.getDispatcher()));
    }
    
    @Override
    public void registerOnWorldLoad(BiConsumer<RegistryKey<World>, ServerWorld> biConsumer) {
        MinecraftForge.EVENT_BUS.<WorldEvent.Load>addListener(load -> {
            if (load.getWorld() instanceof ServerWorld)
                biConsumer.accept(((World) load.getWorld()).dimension(), (ServerWorld) load.getWorld());
        });
    }
    
    @Override
    public void registerOnWorldSave(Consumer<ServerWorld> consumer) {
        MinecraftForge.EVENT_BUS.<WorldEvent.Save>addListener(save -> {
            if (save.getWorld() instanceof ServerWorld)
                consumer.accept((ServerWorld) save.getWorld());
        });
    }
    
    @Override
    public void registerOnPlayerJoin(Consumer<ServerPlayerEntity> consumer) {
        MinecraftForge.EVENT_BUS.<PlayerEvent.PlayerLoggedInEvent>addListener(event -> consumer.accept((ServerPlayerEntity) event.getPlayer()));
    }
    
    @Override
    public void registerOnServerTick(Consumer<MinecraftServer> consumer) {
        MinecraftForge.EVENT_BUS.<TickEvent.ServerTickEvent>addListener(event -> consumer.accept(getServer()));
    }
    
    @Override
    public void registerOnClientTick(Consumer<Minecraft> consumer) {
        MinecraftForge.EVENT_BUS.<TickEvent.ClientTickEvent>addListener(event -> consumer.accept(Minecraft.getInstance()));
    }
    
    @Override
    public void registerOnWorldTick(Consumer<World> consumer) {
        MinecraftForge.EVENT_BUS.<TickEvent.WorldTickEvent>addListener(event -> consumer.accept(event.world));
    }
    
    @Override
    public void registerOnHudRender(BiConsumer<MatrixStack, Float> biConsumer) {
        MinecraftForge.EVENT_BUS.<RenderGameOverlayEvent.Post>addListener(event -> {
            if (event.getType() == RenderGameOverlayEvent.ElementType.ALL)
                biConsumer.accept(event.getMatrixStack(), event.getPartialTicks());
        });
    }
    
    @Override
    public void registerOnUseItem(TriConsumer<PlayerEntity, World, Hand> triConsumer) {
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
    public void registerOnItemPickup(BiConsumer<PlayerEntity, ItemStack> biConsumer) {
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
    public void registerOnCrafting(BiConsumer<PlayerEntity, ItemStack> triConsumer) {
        MinecraftForge.EVENT_BUS.<PlayerEvent.ItemCraftedEvent>addListener(event -> {
            triConsumer.accept(event.getPlayer(), event.getCrafting());
        });
    }
    
    @Override
    public void registerOnAnvilCrafting(BiConsumer<PlayerEntity, ItemStack> triConsumer) {
        MinecraftForge.EVENT_BUS.<AnvilRepairEvent>addListener(event -> {
            triConsumer.accept(event.getPlayer(), event.getItemResult());
        });
    }
    
    @Override
    public void registerOnSmelting(BiConsumer<PlayerEntity, ItemStack> triConsumer) {
        MinecraftForge.EVENT_BUS.<PlayerEvent.ItemSmeltedEvent>addListener(event -> {
            triConsumer.accept(event.getPlayer(), event.getSmelting());
        });
    }
    
    @Override
    public void registerOnAdvancement(BiConsumer<ServerPlayerEntity, Advancement> biConsumer) {
        MinecraftForge.EVENT_BUS.<AdvancementEvent>addListener(event -> {
            if (event.getPlayer() instanceof ServerPlayerEntity)
                biConsumer.accept((ServerPlayerEntity) event.getPlayer(), event.getAdvancement());
        });
    }
    
    @Override
    public void registerOnAnimalTame(BiConsumer<PlayerEntity, Entity> biConsumer) {
        MinecraftForge.EVENT_BUS.<AnimalTameEvent>addListener(event -> {
            biConsumer.accept(event.getTamer(), event.getAnimal());
        });
    }
    
    @Override
    public CompoundNBT getPlayerExtraTag(PlayerEntity playerEntity) {
        return playerEntity.getPersistentData();
    }
    
    @Override
    public ItemGroup createTab(ResourceLocation resourceLocation, Supplier<ItemStack> supplier) {
        return new ItemGroup(String.format("%s.%s", resourceLocation.getNamespace(), resourceLocation.getPath())) {
            @Nonnull
            @Override
            public ItemStack makeIcon() {
                return supplier.get();
            }
        };
    }
    
    @Override
    public AbstractBlock.Properties createDeliveryBlockProperties() {
        return AbstractBlock.Properties.of(Material.WOOD).requiresCorrectToolForDrops().strength(1.0F).harvestTool(ToolType.AXE).harvestLevel(0);
    }
    
    @Override
    public AbstractBarrelBlockEntity createBarrelBlockEntity() {
        return new BarrelBlockEntity();
    }
    
    @Override
    public void setCraftingRemainingItem(Item item, Item item1) {
        item.craftingRemainingItem = item1;
    }
    
    @Override
    public SaveFormat.LevelSave getStorageSourceOfServer(MinecraftServer minecraftServer) {
        return minecraftServer.storageSource;
    }
    
    @Override
    public FluidStack createEmptyFluidStack() {
        return new ForgeFluidStack();
    }
    
    @Override
    public FluidStack createFluidStack(Fluid fluid, Fraction fraction) {
        return new ForgeFluidStack(new net.minecraftforge.fluids.FluidStack(fluid, fraction.intValue()));
    }
    
    @OnlyIn(Dist.CLIENT)
    public static RenderType createFluid(ResourceLocation location) {
        return RenderType.create(
                HardcoreQuestingCore.ID + ":fluid_type",
                DefaultVertexFormats.POSITION_TEX_COLOR, 7, 256, true, false,
                RenderType.State.builder()
                        .setShadeModelState(RenderState.SMOOTH_SHADE)
                        .setLightmapState(RenderState.LIGHTMAP)
                        .setTextureState(new RenderState.TextureState(location, false, false))
                        .setTransparencyState(RenderState.TRANSLUCENT_TRANSPARENCY)
                        .createCompositeState(false));
    }
    
    @OnlyIn(Dist.CLIENT)
    @Override
    public void renderFluidStack(FluidStack fluidStack, MatrixStack matrices, int x1, int y1, int x2, int y2) {
        ForgeFluidStack stack = (ForgeFluidStack) fluidStack;
        FluidAttributes attributes = stack.getFluid().getAttributes();
        ResourceLocation texture = attributes.getStillTexture(stack._stack);
        RenderMaterial blockMaterial = ForgeHooksClient.getBlockMaterial(texture);
        TextureAtlasSprite sprite = blockMaterial.sprite();
        int color = attributes.getColor(Minecraft.getInstance().level, BlockPos.ZERO);
        int a = 255;
        int r = (color >> 16 & 255);
        int g = (color >> 8 & 255);
        int b = (color & 255);
        IRenderTypeBuffer.Impl source = Minecraft.getInstance().renderBuffers().bufferSource();
        IVertexBuilder builder = blockMaterial.buffer(source, HardcoreQuestingForge::createFluid);
        Matrix4f matrix = matrices.last().pose();
        builder.vertex(matrix, x2, y1, 0).uv(sprite.getU1(), sprite.getV0()).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x1, y1, 0).uv(sprite.getU0(), sprite.getV0()).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x1, y2, 0).uv(sprite.getU0(), sprite.getV1()).color(r, g, b, a).endVertex();
        builder.vertex(matrix, x2, y2, 0).uv(sprite.getU1(), sprite.getV1()).color(r, g, b, a).endVertex();
        source.endBatch();
    }
    
    @Override
    public Fraction getBucketAmount() {
        return Fraction.ofWhole(1000);
    }
}
