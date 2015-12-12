package hardcorequesting;


import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import hardcorequesting.quests.QuestTask;
import hardcorequesting.reputation.Reputation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;

import java.util.ArrayList;
import java.util.List;

public class EventHandler {

    private static EventHandler instance;
    private List<QuestTask>[] registeredTasks;

    public EventHandler() {
        registeredTasks = new List[Type.values().length];
        for (int i = 0; i < registeredTasks.length; i++) {
            registeredTasks[i] = new ArrayList<QuestTask>();
        }

        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
        instance = this;
    }

    public static EventHandler instance() {
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
            return QuestingData.getPlayer(playerName);
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
