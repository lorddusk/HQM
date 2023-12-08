package hardcorequesting.forge;

import com.mojang.brigadier.CommandDispatcher;
import dev.architectury.fluid.FluidStack;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.config.HQMConfig;
import hardcorequesting.common.items.ModItems;
import hardcorequesting.common.platform.AbstractPlatform;
import hardcorequesting.common.platform.NetworkManager;
import hardcorequesting.common.recipe.BookCatalystRecipeSerializer;
import hardcorequesting.common.tileentity.AbstractBarrelBlockEntity;
import hardcorequesting.common.util.Fraction;
import hardcorequesting.forge.tileentity.BarrelBlockEntity;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
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
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.living.AnimalTameEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.AnvilRepairEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.NotNull;

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
    private final DeferredRegister<SoundEvent> sounds = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, HardcoreQuestingCore.ID);

    private final DeferredRegister<CreativeModeTab> tab = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HardcoreQuestingCore.ID);
    private final DeferredRegister<Block> block = DeferredRegister.create(BuiltInRegistries.BLOCK, HardcoreQuestingCore.ID);
    private final DeferredRegister<Item> item = DeferredRegister.create(BuiltInRegistries.ITEM, HardcoreQuestingCore.ID);
    private final DeferredRegister<RecipeSerializer<?>> recipe = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, HardcoreQuestingCore.ID);
    private final DeferredRegister<BlockEntityType<?>> tileEntityType = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, HardcoreQuestingCore.ID);
    
    public HardcoreQuestingForge() {
        NetworkingManager.init();
        HardcoreQuestingCore.initialize(this);
    
        sounds.register(FMLJavaModLoadingContext.get().getModEventBus());
        tab.register(FMLJavaModLoadingContext.get().getModEventBus());
        block.register(FMLJavaModLoadingContext.get().getModEventBus());
        item.register(FMLJavaModLoadingContext.get().getModEventBus());
        recipe.register(FMLJavaModLoadingContext.get().getModEventBus());
        tileEntityType.register(FMLJavaModLoadingContext.get().getModEventBus());
        NeoForge.EVENT_BUS.<LivingDropsEvent>addListener(event -> {
            if (event.getEntity() instanceof Player player) {
                if (player instanceof FakePlayer
                    || event.isCanceled()
                    || player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)
                    || HQMConfig.getInstance().LOSE_QUEST_BOOK) {
                    return;
                }
                
                Iterator<ItemEntity> iter = event.getDrops().iterator();
                while (iter.hasNext()) {
                    ItemEntity entityItem = iter.next();
                    ItemStack stack = entityItem.getItem();
                    if (stack.is(ModItems.book.get())) {
                        player.getInventory().add(stack);
                        iter.remove();
                    }
                }
            }
        });
        NeoForge.EVENT_BUS.<PlayerEvent.Clone>addListener(event -> {
            if (event.getEntity() == null || event.getEntity() instanceof FakePlayer
                || !event.isWasDeath()
                || event.getEntity().level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)
                || HQMConfig.getInstance().LOSE_QUEST_BOOK) {
                return;
            }
    
            ItemStack bookStack = new ItemStack(ModItems.book.get());
            if (event.getOriginal().getInventory().contains(bookStack)) {
                for (ItemStack stack : event.getOriginal().getInventory().items) {
                    if (ItemStack.isSameItem(bookStack, stack)) {
                        bookStack = stack.copy();
                        break;
                    }
                }
                event.getEntity().getInventory().add(bookStack);
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
        NeoForge.EVENT_BUS.<RegisterCommandsEvent>addListener(event -> consumer.accept(event.getDispatcher()));
    }
    
    @Override
    public void registerOnWorldLoad(BiConsumer<ResourceKey<Level>, ServerLevel> biConsumer) {
        NeoForge.EVENT_BUS.<LevelEvent.Load>addListener(load -> {
            if (load.getLevel() instanceof ServerLevel)
                biConsumer.accept(((Level) load.getLevel()).dimension(), (ServerLevel) load.getLevel());
        });
    }
    
    @Override
    public void registerOnWorldSave(Consumer<ServerLevel> consumer) {
        NeoForge.EVENT_BUS.<LevelEvent.Save>addListener(save -> {
            if (save.getLevel() instanceof ServerLevel)
                consumer.accept((ServerLevel) save.getLevel());
        });
    }
    
    @Override
    public void registerOnPlayerJoin(Consumer<ServerPlayer> consumer) {
        NeoForge.EVENT_BUS.<PlayerEvent.PlayerLoggedInEvent>addListener(event -> consumer.accept((ServerPlayer) event.getEntity()));
    }
    
    @Override
    public void registerOnServerTick(Consumer<MinecraftServer> consumer) {
        NeoForge.EVENT_BUS.<TickEvent.ServerTickEvent>addListener(event -> {
            if(event.phase == TickEvent.Phase.END)
                consumer.accept(getServer());
        });
    }
    
    @Override
    public void registerOnClientTick(Consumer<Minecraft> consumer) {
        NeoForge.EVENT_BUS.<TickEvent.ClientTickEvent>addListener(event -> {
            if(event.phase == TickEvent.Phase.END)
                consumer.accept(Minecraft.getInstance());
        });
    }
    
    @Override
    public void registerOnWorldTick(Consumer<Level> consumer) {
        NeoForge.EVENT_BUS.<TickEvent.LevelTickEvent>addListener(event -> {
            if(event.phase == TickEvent.Phase.END)
                consumer.accept(event.level);
        });
    }

    @Override
    public void registerOnUseItem(TriConsumer<Player, Level, InteractionHand> triConsumer) {
        NeoForge.EVENT_BUS.<PlayerInteractEvent.RightClickItem>addListener(event -> triConsumer.accept(event.getEntity(), event.getLevel(), event.getHand()));
    }
    
    @Override
    public void registerOnBlockPlace(BlockPlaced blockPlaced) {
        NeoForge.EVENT_BUS.<BlockEvent.EntityPlaceEvent>addListener(event -> {
            if (event.getEntity() instanceof LivingEntity)
                blockPlaced.onBlockPlaced(event.getEntity().getCommandSenderWorld(), event.getPos(), event.getPlacedBlock(), (LivingEntity) event.getEntity());
        });
    }
    
    @Override
    public void registerOnBlockUse(BlockUsed blockUsed) {
        NeoForge.EVENT_BUS.<PlayerInteractEvent.RightClickBlock>addListener(event -> {
            blockUsed.onBlockUsed(event.getEntity(), event.getLevel(), event.getHand(), event.getPos(), event.getFace());
        });
    }
    
    @Override
    public void registerOnBlockBreak(BlockBroken blockBroken) {
        NeoForge.EVENT_BUS.<BlockEvent.BreakEvent>addListener(event -> {
            blockBroken.onBlockBroken(event.getLevel(), event.getPos(), event.getState(), event.getPlayer());
        });
    }
    
    @Override
    public void registerOnItemPickup(BiConsumer<Player, ItemStack> biConsumer) {
        NeoForge.EVENT_BUS.<PlayerEvent.ItemPickupEvent>addListener(event -> {
            biConsumer.accept(event.getEntity(), event.getStack());
        });
    }
    
    @Override
    public void registerOnLivingDeath(BiConsumer<LivingEntity, DamageSource> biConsumer) {
        NeoForge.EVENT_BUS.<LivingDeathEvent>addListener(event -> {
            biConsumer.accept(event.getEntity(), event.getSource());
        });
    }
    
    @Override
    public void registerOnCrafting(BiConsumer<Player, ItemStack> triConsumer) {
        NeoForge.EVENT_BUS.<PlayerEvent.ItemCraftedEvent>addListener(event -> {
            triConsumer.accept(event.getEntity(), event.getCrafting());
        });
    }
    
    @Override
    public void registerOnAnvilCrafting(BiConsumer<Player, ItemStack> triConsumer) {
        NeoForge.EVENT_BUS.<AnvilRepairEvent>addListener(event -> {
            triConsumer.accept(event.getEntity(), event.getOutput());
        });
    }
    
    @Override
    public void registerOnSmelting(BiConsumer<Player, ItemStack> triConsumer) {
        NeoForge.EVENT_BUS.<PlayerEvent.ItemSmeltedEvent>addListener(event -> {
            triConsumer.accept(event.getEntity(), event.getSmelting());
        });
    }
    
    @Override
    public void registerOnAdvancement(BiConsumer<ServerPlayer, AdvancementHolder> biConsumer) {
        NeoForge.EVENT_BUS.<AdvancementEvent.AdvancementEarnEvent>addListener(event -> {
            if (event.getEntity() instanceof ServerPlayer)
                biConsumer.accept((ServerPlayer) event.getEntity(), event.getAdvancement());
        });
    }
    
    @Override
    public void registerOnAnimalTame(BiConsumer<Player, Entity> biConsumer) {
        NeoForge.EVENT_BUS.<AnimalTameEvent>addListener(event -> {
            biConsumer.accept(event.getTamer(), event.getAnimal());
        });
    }
    
    @Override
    public CompoundTag getPlayerExtraTag(Player playerEntity) {
        return playerEntity.getPersistentData();
    }

    @Override
    public AbstractBarrelBlockEntity createBarrelBlockEntity(BlockPos pos, BlockState state) {
        return new BarrelBlockEntity(pos, state);
    }

    @Override
    public List<FluidStack> findFluidsIn(ItemStack stack) {
        return stack.getCapability(Capabilities.FLUID_HANDLER_ITEM)
                .map(HardcoreQuestingForge::getAllFluidsIn)
                .orElse(Collections.emptyList());
    }
    
    @NotNull
    private static List<FluidStack> getAllFluidsIn(IFluidHandlerItem handler) {
        List<FluidStack> fluids = new ArrayList<>();
        for (int tank = 0; tank < handler.getTanks(); tank++) {
            net.neoforged.neoforge.fluids.FluidStack fluid = handler.getFluidInTank(tank);
            if (!fluid.isEmpty())
                fluids.add(FluidStack.create(fluid.getFluid(), fluid.getAmount()));
        }
        return fluids;
    }
    
    @Override
    public Fraction getBucketAmount() {
        return Fraction.ofWhole(FluidType.BUCKET_VOLUME);
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
    public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(String id, BiFunction<BlockPos, BlockState, T> constructor, Supplier<Block> validBlock) {
        return tileEntityType.register(id, () -> BlockEntityType.Builder.of(constructor::apply, validBlock.get()).build(null));
    }
    
    @Override
    public Supplier<RecipeSerializer<?>> registerBookRecipeSerializer(String id) {
        return recipe.register(id, BookCatalystRecipeSerializer::new);
    }

    @Override
    public Supplier<CreativeModeTab> registerTab(String id, Supplier<CreativeModeTab> supplier) {
        return tab.register(id, supplier);
    }
}
