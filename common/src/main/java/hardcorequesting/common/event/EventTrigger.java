package hardcorequesting.common.event;

import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.quests.QuestingData;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.quests.task.QuestTask;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

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
        HardcoreQuestingCore.platform.registerOnServerTick(this::onServerTick);
        HardcoreQuestingCore.platform.registerOnWorldTick(world -> {
            for (Player player : world.players()) {
                if (player instanceof ServerPlayer) {
                    onPlayerTick((ServerPlayer) player);
                }
            }
        });
        HardcoreQuestingCore.platform.registerOnUseItem(this::onItemUsed);
        HardcoreQuestingCore.platform.registerOnBlockUse(this::onBlockUsed);
        HardcoreQuestingCore.platform.registerOnBlockPlace(this::onBlockPlaced);
        HardcoreQuestingCore.platform.registerOnBlockBreak(this::onBlockBreak);
        HardcoreQuestingCore.platform.registerOnItemPickup(this::onItemPickUp);
        HardcoreQuestingCore.platform.registerOnCrafting(this::onCrafting);
        HardcoreQuestingCore.platform.registerOnAnvilCrafting(this::onAnvilCrafting);
        HardcoreQuestingCore.platform.registerOnSmelting(this::onSmelting);
        HardcoreQuestingCore.platform.registerOnAdvancement(this::onAdvancement);
        HardcoreQuestingCore.platform.registerOnAnimalTame(this::onAnimalTame);
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
    
    public void onPlayerLogin(ServerPlayer entity) {
        for (List<QuestTask> list : registeredTasks) {
            list.removeIf((q) -> !q.isValid());
        }
        QuestingDataManager questingData = QuestingDataManager.getInstance();
        if (questingData.isQuestActive()) {
            questingData.spawnBook(entity);
        }
    }
    
    public void onServerTick(MinecraftServer server) {
        for (QuestTask task : getTasks(Type.SERVER)) {
            task.onServerTick(server);
        }
    }
    
    public void onPlayerTick(ServerPlayer playerEntity) {
        for (QuestTask task : getTasks(Type.PLAYER)) {
            task.onPlayerTick(playerEntity);
        }
    }
    
    public void onLivingDeath(LivingEntity entity, DamageSource source) {
        if (entity.level.isClientSide)
            return;
        for (QuestTask task : getTasks(Type.DEATH)) {
            task.onLivingDeath(entity, source);
        }
    }
    
    public void onCrafting(Player player, ItemStack stack) {
        if (player.level.isClientSide)
            return;
        for (QuestTask task : getTasks(Type.CRAFTING)) {
            task.onCrafting(player, stack);
        }
    }
    
    public void onAnvilCrafting(Player player, ItemStack stack) {
        if (player.level.isClientSide)
            return;
        for (QuestTask task : getTasks(Type.CRAFTING)) {
            task.onCrafting(player, stack);
        }
    }
    
    public void onSmelting(Player player, ItemStack stack) {
        if (player.level.isClientSide)
            return;
        for (QuestTask task : getTasks(Type.CRAFTING)) {
            task.onCrafting(player, stack);
        }
    }
    
    public void onItemPickUp(Player playerEntity, ItemStack stack) {
        if (playerEntity.level.isClientSide)
            return;
        for (QuestTask task : getTasks(Type.PICK_UP)) {
            task.onItemPickUp(playerEntity, stack);
        }
    }
    
    public void onBookOpening(BookOpeningEvent event) {
        if (event.getPlayer().level.isClientSide)
            return;
        for (QuestTask task : getTasks(Type.OPEN_BOOK)) {
            task.onOpenBook(event);
        }
    }
    
    public void onQuestComplete(QuestCompletedEvent event) {
        if (event.player.level.isClientSide)
            return;
        for (QuestTask task : getTasks(Type.QUEST_COMPLETED)) {
            task.onQuestCompleted(event);
        }
    }
    
    public void onQuestSelected(QuestSelectedEvent event) {
        if (event.getPlayer().level.isClientSide)
            return;
        for (QuestTask task : getTasks(Type.QUEST_SELECTED)) {
            task.onQuestSelected(event);
        }
    }
    
    public void onReputationChange(ReputationEvent event) {
        if (event.getPlayer().level.isClientSide)
            return;
        for (QuestTask task : getTasks(Type.REPUTATION_CHANGE)) {
            task.onReputationChange(event);
        }
    }
    
    public void onAnimalTame(Player tamer, Entity entity) {
        if (entity.level.isClientSide)
            return;
        for (QuestTask task : getTasks(Type.ANIMAL_TAME)) {
            task.onAnimalTame(tamer, entity);
        }
    }
    
    public void onAdvancement(ServerPlayer playerEntity, Advancement advancement) {
        for (QuestTask task : getTasks(Type.ADVANCEMENT)) {
            task.onAdvancement(playerEntity);
        }
    }
    
    public void onBlockBreak(LevelAccessor world, BlockPos pos, BlockState state, Player player) {
        if (player.level.isClientSide)
            return;
        for (QuestTask task : getTasks(Type.BLOCK_BROKEN)) {
            task.onBlockBroken(pos, state, player);
        }
    }
    
    public void onBlockPlaced(Level world, BlockPos pos, BlockState state, LivingEntity placer) {
        if (world.isClientSide)
            return;
        for (QuestTask task : getTasks(Type.BLOCK_PLACED)) {
            task.onBlockPlaced(world, state, placer);
        }
    }
    
    private void onItemUsed(Player playerEntity, Level world, InteractionHand hand) {
        if (world.isClientSide)
            return;
        for (QuestTask task : getTasks(Type.ITEM_USED)) {
            task.onItemUsed(playerEntity, world, hand);
        }
    }
    
    private void onBlockUsed(Player playerEntity, Level world, InteractionHand hand, BlockPos pos, Direction face) {
        if (world.isClientSide)
            return;
        for (QuestTask task : getTasks(Type.ITEM_USED)) {
            task.onBlockUsed(playerEntity, world, hand);
        }
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
        
        private UUID playerUUID;
        private boolean isOpBook;
        private boolean isRealName;
        
        public BookOpeningEvent(UUID playerUUID, boolean isOpBook, boolean isRealName) {
            this.playerUUID = playerUUID;
            this.isOpBook = isOpBook;
            this.isRealName = isRealName;
        }
        
        public UUID getPlayerUUID() {
            return playerUUID;
        }
        
        public boolean isOpBook() {
            return isOpBook;
        }
        
        public boolean isRealName() {
            return isRealName;
        }
        
        public Player getPlayer() {
            return QuestingData.getPlayer(playerUUID);
        }
    }
    
    public static class QuestCompletedEvent {
        private UUID questCompleted;
        private Player player;
        
        public QuestCompletedEvent(Player player, UUID questCompleted) {
            this.player = player;
            this.questCompleted = questCompleted;
        }
        
        public UUID getQuestCompleted() {
            return questCompleted;
        }
        
        public Player getPlayer() {
            return player;
        }
    }
    
    public static class QuestSelectedEvent extends QuestCompletedEvent {
        public QuestSelectedEvent(Player player, UUID questSelected) {
            super(player, questSelected);
        }
        
        public UUID getQuestSelected() {
            return getQuestCompleted();
        }
    }
    
    public static class ReputationEvent {
        private Player player;
        
        public ReputationEvent(Player player) {
            this.player = player;
        }
        
        public Player getPlayer() {
            return player;
        }
    }
}
