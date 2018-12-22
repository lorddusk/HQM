package hardcorequesting.event;

import hardcorequesting.quests.QuestingData;
import hardcorequesting.quests.task.QuestTask;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.AnimalTameEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Mod.EventBusSubscriber
public class EventTrigger{

    private static EventTrigger instance;
    private List<QuestTask>[] registeredTasks;

    public EventTrigger() {
        registeredTasks = new List[Type.values().length];
        for (int i = 0; i < registeredTasks.length; i++) {
            registeredTasks[i] = new CopyOnWriteArrayList<>();
        }
        MinecraftForge.EVENT_BUS.register(this);
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

    @SubscribeEvent
    public void onEvent(TickEvent.ServerTickEvent event) {
        for (QuestTask task : getTasks(Type.SERVER)) {
            task.onServerTick(event);
        }
    }

    @SubscribeEvent
    public void onEvent(TickEvent.PlayerTickEvent event) {
        for (QuestTask task : getTasks(Type.PLAYER)) {
            task.onPlayerTick(event);
        }
    }

    @SubscribeEvent
    public void onEvent(LivingDeathEvent event) {
        for (QuestTask task : getTasks(Type.DEATH)) {
            task.onLivingDeath(event);
        }
    }

    @SubscribeEvent
    public void onEvent(PlayerEvent.PlayerLoggedInEvent event) {
        if(QuestingData.isQuestActive()) {
            QuestingData.spawnBook(event.player);
        }
    }

    @SubscribeEvent
    public void onEvent(PlayerEvent.ItemCraftedEvent event) {
        for (QuestTask task : getTasks(Type.CRAFTING)) {
            task.onCrafting(event);
        }
    }

    @SubscribeEvent
    public void onEvent(EntityItemPickupEvent event) {
        for (QuestTask task : getTasks(Type.PICK_UP)) {
            task.onItemPickUp(event);
        }
    }

    public void onEvent(BookOpeningEvent event) {
        for (QuestTask task : getTasks(Type.OPEN_BOOK)) {
            task.onOpenBook(event);
        }
    }

    public void onEvent(ReputationEvent event) {
        for (QuestTask task : getTasks(Type.REPUTATION_CHANGE)) {
            task.onReputationChange(event);
        }
    }

    @SubscribeEvent
    public void onEvent(AnimalTameEvent event) {
        for (QuestTask task : getTasks(Type.ANIMAL_TAME)) {
            task.onAnimalTame(event);
        }
    }


    private List<QuestTask> getTasks(Type type) {
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

        public EntityPlayer getPlayer() {
            return QuestingData.getPlayerFromUsername(playerName);
        }
    }

    public static class ReputationEvent {

        private EntityPlayer player;


        public ReputationEvent(EntityPlayer player) {
            this.player = player;
        }

        public EntityPlayer getPlayer() {
            return player;
        }
    }

}
