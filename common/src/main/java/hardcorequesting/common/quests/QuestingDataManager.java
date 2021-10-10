package hardcorequesting.common.quests;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.config.HQMConfig;
import hardcorequesting.common.event.PlayerTracker;
import hardcorequesting.common.io.SaveHandler;
import hardcorequesting.common.items.ModItems;
import hardcorequesting.common.team.TeamManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.io.StringReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class QuestingDataManager {
    public static final String STATE_FILE_PATH = "state.json";
    public static final String DATA_FILE_PATH = "data.json";
    private final QuestLine parent;
    public final State state;
    public final Data data;
    private boolean hardcoreActive;
    private boolean questActive;
    public Map<UUID, QuestingData> questingData = new ConcurrentHashMap<>();
    
    public QuestingDataManager(QuestLine parent) {
        this.parent = parent;
        this.state = new State(parent);
        this.data = new Data(parent);
    }
    
    public static QuestingDataManager getInstance() {
        return QuestLine.getActiveQuestLine().questingDataManager;
    }
    
    public QuestingData getQuestingData(Player player) {
        return getQuestingData(player.getUUID());
    }
    
    public QuestingData getQuestingData(UUID playerUuid) {
        return questingData.computeIfAbsent(playerUuid, u -> new QuestingData(this, u));
    }
    
    public Map<UUID, QuestingData> getQuestingData() {
        return questingData;
    }
    
    public boolean isHardcoreActive() {
        return hardcoreActive;
    }
    
    public boolean isQuestActive() {
        return questActive;
    }
    
    public int getDefaultLives() {
        HQMConfig instance = HQMConfig.getInstance();
        return Math.min(instance.Hardcore.MAX_LIVES, instance.Hardcore.DEFAULT_LIVES);
    }
    
    public void activateHardcore() {
        MinecraftServer server = HardcoreQuestingCore.getServer();
        
        if (server != null) {
            if (!hardcoreActive && !server.isHardcore()) {
                hardcoreActive = true;
            }
        }
    }
    
    public void disableHardcore() {
        hardcoreActive = false;
    }
    
    public void activateQuest(boolean giveBooks) {
        if (!questActive) {
            questActive = true;
            if (giveBooks) {
                for (Player player : HardcoreQuestingCore.getServer().getPlayerList().getPlayers()) {
                    if (player != null) {
                        spawnBook(player);
                    }
                }
            }
        }
    }
    
    public void deactivate() {
        if (hardcoreActive || questActive) {
            hardcoreActive = false;
            questActive = false;
            questingData = new HashMap<>();
        }
    }
    
    public class State extends SimpleSerializable {
        public State(QuestLine parent) {
            super(parent);
        }
        
        @Override
        public String saveToString() {
            return saveQuestingState(isQuestActive(), isHardcoreActive());
        }
        
        @Override
        public String filePath() {
            return STATE_FILE_PATH;
        }
        
        @Override
        public boolean isData() {
            return true;
        }
        
        @Override
        public void loadFromString(Optional<String> string) {
            boolean autoQuesting = HQMConfig.getInstance().Starting.AUTO_QUESTING;
            boolean autoHardcore = HQMConfig.getInstance().Starting.AUTO_HARDCORE;
            if (string.isPresent()) {
                JsonObject object = new JsonParser().parse(new StringReader(string.get())).getAsJsonObject();
                deactivate();
                TeamManager.getInstance().deactivate();
                if (object.get(SaveHandler.QUESTING).getAsBoolean() || autoQuesting) activateQuest(false);
                if (object.get(SaveHandler.HARDCORE).getAsBoolean() || autoHardcore) activateHardcore();
            } else {
                if (autoQuesting) activateQuest(false);
                if (autoHardcore) activateHardcore();
            }
        }
    }
    
    public class Data extends SimpleSerializable {
        public Data(QuestLine parent) {
            super(parent);
        }
        
        @Override
        public String saveToString() {
            return SaveHandler.save(Lists.newArrayList(getQuestingData().values()), new TypeToken<List<QuestingData>>() {}.getType());
        }
        
        public String saveToString(Player player) {
            return "[" + SaveHandler.save(getQuestingData(player), QuestingData.class) + "]";
        }
        
        @Override
        public String filePath() {
            return DATA_FILE_PATH;
        }
        
        @Override
        public boolean isData() {
            return true;
        }
        
        @Override
        public void loadFromString(Optional<String> string) {
            questingData.clear();
            string.flatMap(s -> SaveHandler.<List<QuestingData>>load(s, new TypeToken<List<QuestingData>>() {}.getType()))
                    .ifPresent(list -> list.forEach(d -> questingData.put(d.getPlayerId(), d)));
        }
    }
    
    public static String saveQuestingState(boolean questing, boolean hardcore) {
        JsonObject object = new JsonObject();
        object.addProperty(SaveHandler.QUESTING, questing);
        object.addProperty(SaveHandler.HARDCORE, hardcore);
        return object.toString();
    }
    
    public void spawnBook(Player player) {
        if (!Quest.canQuestsBeEdited() && !player.level.isClientSide && HQMConfig.getInstance().SPAWN_BOOK && !getQuestingData(player).receivedBook && isQuestActive()) {
            getQuestingData(player).receivedBook = true;
            CompoundTag hqmTag = new CompoundTag();
            CompoundTag extraTag = HardcoreQuestingCore.platform.getPlayerExtraTag(player);
            if (extraTag.contains(PlayerTracker.HQ_TAG))
                hqmTag = extraTag.getCompound(PlayerTracker.HQ_TAG);
            hqmTag.putBoolean(PlayerTracker.RECEIVED_BOOK, true);
            extraTag.put(PlayerTracker.HQ_TAG, hqmTag);
            ItemStack stack = new ItemStack(ModItems.book.get());
            if (!player.getInventory().add(stack)) {
                spawnItemAtPlayer(player, stack);
            }
        }
    }
    
    private static void spawnItemAtPlayer(Player player, ItemStack stack) {
        ItemEntity item = new ItemEntity(player.level, player.getX() + 0.5D, player.getY() + 0.5D, player.getZ() + 0.5D, stack);
        player.level.addFreshEntity(item);
        item.playerTouch(player);
    }
    
    public void remove(Player player) {
        questingData.remove(player.getUUID());
    }
    
    public boolean hasData(Player player) {
        return questingData.containsKey(player.getGameProfile().getId());
    }
    
    public boolean hasData(UUID uuid) {
        return questingData.containsKey(uuid);
    }
}
