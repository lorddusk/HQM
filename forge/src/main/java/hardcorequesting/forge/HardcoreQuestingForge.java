package hardcorequesting.forge;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.brigadier.CommandDispatcher;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.platform.AbstractPlatform;
import hardcorequesting.common.platform.FluidStack;
import hardcorequesting.common.platform.NetworkManager;
import hardcorequesting.common.tileentity.AbstractBarrelBlockEntity;
import hardcorequesting.common.util.Fraction;
import hardcorequesting.forge.tileentity.BarrelBlockEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.SaveFormat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.util.TriConsumer;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mod("hardcorequesting")
public class HardcoreQuestingForge implements AbstractPlatform {
    private final NetworkManager networkManager = new NetworkingManager();
    
    public HardcoreQuestingForge() {
        NetworkingManager.init();
        HardcoreQuestingCore.initialize(this);
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
    
    @Override
    public void renderFluidStack(FluidStack fluidStack, int i, int i1, int i2, int i3) {
        
    }
    
    @Override
    public Fraction getBucketAmount() {
        return Fraction.ofWhole(1000);
    }
}
