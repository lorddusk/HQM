package hardcorequesting.event;

import hardcorequesting.quests.QuestingData;
import hardcorequesting.quests.task.QuestTask;
import me.shedaniel.cloth.api.common.events.v1.BlockBreakCallback;
import me.shedaniel.cloth.api.common.events.v1.BlockPlaceCallback;
import me.shedaniel.cloth.api.common.events.v1.ItemPickupCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.event.server.ServerTickCallback;
import net.fabricmc.fabric.api.event.world.WorldTickCallback;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventTrigger {
    
    private static EventTrigger instance;
    private List<QuestTask>[] registeredTasks;
    
    public EventTrigger() {
        registeredTasks = new List[Type.values().length];
        for (int i = 0; i < registeredTasks.length; i++) {
            registeredTasks[i] = new CopyOnWriteArrayList<>();
        }
        ServerTickCallback.EVENT.register(this::onServerTick);
        WorldTickCallback.EVENT.register(world -> {
            for (PlayerEntity player : world.getPlayers()) {
                if (player instanceof ServerPlayerEntity) {
                    onPlayerTick((ServerPlayerEntity) player);
                }
            }
        });
        UseItemCallback.EVENT.register(this::onItemUsed);
        UseBlockCallback.EVENT.register(this::onBlockUsed);
        BlockPlaceCallback.EVENT.register(this::onBlockPlaced);
        BlockBreakCallback.EVENT.register(this::onBlockBreak);
        ItemPickupCallback.EVENT.register(this::onItemPickUp);
        instance = this;
    }
    
    public static EventTrigger instance() {
        return instance;
    }
    
    public void clear() {
        for (List<QuestTask> registeredTask : registeredTasks) {
            registeredTask.clear();
        }
    }
    
    public void add(QuestTask task, Type... types) {
        for (Type type : types) {
            registeredTasks[type.ordinal()].add(task);
        }
    }
    
    public void remove(QuestTask task) {
        for (List<QuestTask> registeredTask : registeredTasks) {
            registeredTask.remove(task);
        }
    }
    
    public void onPlayerLogin(ServerPlayerEntity entity) {
        for (List<QuestTask> list : registeredTasks) {
            list.removeIf((q) -> !q.isValid());
        }
        if (QuestingData.isQuestActive()) {
            QuestingData.spawnBook(entity);
        }
    }
    
    public void onServerTick(MinecraftServer server) {
        for (QuestTask task : getTasks(Type.SERVER)) {
            task.onServerTick(server);
        }
    }
    
    public void onPlayerTick(ServerPlayerEntity playerEntity) {
        for (QuestTask task : getTasks(Type.PLAYER)) {
            task.onPlayerTick(playerEntity);
        }
    }
    
    public void onLivingDeath(LivingEntity entity, DamageSource source) {
        for (QuestTask task : getTasks(Type.DEATH)) {
            task.onLivingDeath(entity, source);
        }
    }
    
    public void onCrafting(PlayerEntity player, ItemStack stack, CraftingInventory craftingInv) {
        for (QuestTask task : getTasks(Type.CRAFTING)) {
            task.onCrafting(player, stack, craftingInv);
        }
    }
    
    // TODO Anvil
//    @SubscribeEvent
//    public void onEvent(AnvilRepairEvent event) {
//        CraftEventWrapper wrapper = new CraftEventWrapper(event.getEntityPlayer(), event);
//        
//        for (QuestTask task : getTasks(Type.CRAFTING)) {
//            task.onCrafting(wrapper);
//        }
//    }
    
    // TODO Furnace
//    @SubscribeEvent
//    public void onEvent(PlayerEvent.ItemSmeltedEvent event) {
//        CraftEventWrapper wrapper = new CraftEventWrapper(event.player, event);
//        
//        for (QuestTask task : getTasks(Type.CRAFTING)) {
//            task.onCrafting(wrapper);
//        }
//    }
    
    public void onItemPickUp(PlayerEntity playerEntity, ItemStack stack) {
        for (QuestTask task : getTasks(Type.PICK_UP)) {
            task.onItemPickUp(playerEntity, stack);
        }
    }
    
    public void onEvent(BookOpeningEvent event) {
        for (QuestTask task : getTasks(Type.OPEN_BOOK)) {
            task.onOpenBook(event);
        }
    }
    
    public void onEvent(QuestCompletedEvent event) {
        for (QuestTask task : getTasks(Type.QUEST_COMPLETED)) {
            task.onQuestCompleted(event);
        }
    }
    
    public void onEvent(QuestSelectedEvent event) {
        for (QuestTask task : getTasks(Type.QUEST_SELECTED)) {
            task.onQuestSelected(event);
        }
    }
    
    public void onEvent(ReputationEvent event) {
        for (QuestTask task : getTasks(Type.REPUTATION_CHANGE)) {
            task.onReputationChange(event);
        }
    }
    
    public void onAnimalTame(PlayerEntity tamer, Entity entity) {
        for (QuestTask task : getTasks(Type.ANIMAL_TAME)) {
            task.onAnimalTame(tamer, entity);
        }
    }
    
    public void onAdvancement(ServerPlayerEntity playerEntity) {
        for (QuestTask task : getTasks(Type.ADVANCEMENT)) {
            task.onAdvancement(playerEntity);
        }
    }
    
    public void onBlockBreak(IWorld world, BlockPos pos, BlockState state, PlayerEntity player) {
        for (QuestTask task : getTasks(Type.BLOCK_BROKEN)) {
            task.onBlockBroken(pos, state, player);
        }
    }
    
    public ActionResult onBlockPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        for (QuestTask task : getTasks(Type.BLOCK_PLACED)) {
            task.onBlockPlaced(itemStack, world, state, placer);
        }
        return ActionResult.PASS;
    }
    
    private TypedActionResult<ItemStack> onItemUsed(PlayerEntity playerEntity, World world, Hand hand) {
        for (QuestTask task : getTasks(Type.ITEM_USED)) {
            task.onItemUsed(playerEntity, world, hand);
        }
        return TypedActionResult.pass(playerEntity.getStackInHand(hand));
    }
    
    private ActionResult onBlockUsed(PlayerEntity playerEntity, World world, Hand hand, BlockHitResult blockHitResult) {
        for (QuestTask task : getTasks(Type.ITEM_USED)) {
            task.onBlockUsed(playerEntity, world, hand, blockHitResult);
        }
        return ActionResult.PASS;
    }
    
    private List<QuestTask> getTasks(Type type) {
        registeredTasks[type.ordinal()].removeIf((task) -> !task.isValid());
        return registeredTasks[type.ordinal()];
    }
    
    public enum Type {
        SERVER,
        PLAYER,
        DEATH,
        CRAFTING,
        PICK_UP,
        OPEN_BOOK,
        REPUTATION_CHANGE,
        ANIMAL_TAME,
        ADVANCEMENT,
        QUEST_COMPLETED,
        QUEST_SELECTED,
        BLOCK_PLACED,
        BLOCK_BROKEN,
        ITEM_USED
    }
    
    public static class BookOpeningEvent {
        
        private String playerName;
        private boolean isOpBook;
        private boolean isRealName;
        
        public BookOpeningEvent(String playerName, boolean isOpBook, boolean isRealName) {
            this.playerName = playerName;
            this.isOpBook = isOpBook;
            this.isRealName = isRealName;
        }
        
        public String getPlayerName() {
            return playerName;
        }
        
        public boolean isOpBook() {
            return isOpBook;
        }
        
        public boolean isRealName() {
            return isRealName;
        }
        
        public PlayerEntity getPlayer() {
            return QuestingData.getPlayerFromUsername(playerName);
        }
    }
    
    public static class QuestCompletedEvent {
        private UUID questCompleted;
        private PlayerEntity player;
        
        public QuestCompletedEvent(PlayerEntity player, UUID questCompleted) {
            this.player = player;
            this.questCompleted = questCompleted;
        }
        
        public UUID getQuestCompleted() { return questCompleted; }
        
        public PlayerEntity getPlayer() {
            return player;
        }
    }
    
    public static class QuestSelectedEvent extends QuestCompletedEvent {
        public QuestSelectedEvent(PlayerEntity player, UUID questSelected) {
            super(player, questSelected);
        }
        
        public UUID getQuestSelected() { return getQuestCompleted(); }
    }
    
    public static class ReputationEvent {
        private PlayerEntity player;
        
        public ReputationEvent(PlayerEntity player) {
            this.player = player;
        }
        
        public PlayerEntity getPlayer() {
            return player;
        }
    }
}
