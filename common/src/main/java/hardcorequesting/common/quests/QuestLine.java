package hardcorequesting.common.quests;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.bag.GroupTier;
import hardcorequesting.common.bag.GroupTierManager;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.sounds.SoundHandler;
import hardcorequesting.common.death.DeathStatsManager;
import hardcorequesting.common.io.SaveHandler;
import hardcorequesting.common.network.NetworkManager;
import hardcorequesting.common.network.message.DeathStatsMessage;
import hardcorequesting.common.network.message.PlayerDataSyncMessage;
import hardcorequesting.common.network.message.QuestLineSyncMessage;
import hardcorequesting.common.network.message.TeamStatsMessage;
import hardcorequesting.common.reputation.ReputationManager;
import hardcorequesting.common.team.TeamManager;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class QuestLine {
    
    private static QuestLine activeQuestLine;
    private static boolean hasLoadedMainSound;
    
    public final ReputationManager reputationManager;
    public final GroupTierManager groupTierManager;
    public final QuestingDataManager questingDataManager;
    public final DeathStatsManager deathStatsManager;
    public final QuestSetsManager questSetsManager;
    public final TeamManager teamManager;
    public String mainDescription = "No description";
    private List<FormattedText> cachedMainDescription;
    @Environment(EnvType.CLIENT)
    public ResourceLocation front;
    @Deprecated
    public final Optional<Path> basePath;
    @Deprecated
    public final Optional<Path> dataPath;
    private final List<Serializable> serializables = Lists.newArrayList();
    private final Map<String, FileProvider> tempPaths = Maps.newHashMap();
    
    private QuestLine(Optional<Path> basePath, Optional<Path> dataPath) {
        if (HardcoreQuestingCore.platform.isClient()) {
            resetClient();
        }
        this.basePath = basePath;
        this.dataPath = dataPath;
        this.reputationManager = new ReputationManager(this);
        this.groupTierManager = new GroupTierManager(this);
        this.questingDataManager = new QuestingDataManager(this);
        this.deathStatsManager = new DeathStatsManager(this);
        this.questSetsManager = new QuestSetsManager(this);
        this.teamManager = new TeamManager(this);
        GroupTier.initBaseTiers(this);
        
        try {
            if (this.basePath.isPresent()) {
                if (!Files.exists(this.basePath.get())) {
                    Files.createDirectories(this.basePath.get());
                }
            }
            if (this.dataPath.isPresent()) {
                if (!Files.exists(this.dataPath.get())) {
                    Files.createDirectories(this.dataPath.get());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        add(new Serializable() {
            @Override
            public void save() {
                resolve("description.txt", provider -> provider.set(mainDescription));
            }
            
            @Override
            public void load() {
                if (!resolve("description.txt", provider -> setMainDescription(provider.get().orElse("No description")))) {
                    setMainDescription("No description");
                }
            }
            
            @Override
            public boolean isData() {
                return false;
            }
        });
        add(this.questingDataManager.state);
        add(this.deathStatsManager);
        add(this.reputationManager);
        add(this.groupTierManager);
        add(this.questSetsManager);
        add(this.teamManager);
        add(this.questingDataManager.data);
    }
    
    @Environment(EnvType.CLIENT)
    private void resetClient() {
        GuiQuestBook.resetBookPosition();
    }
    
    public void add(Serializable serializable) {
        this.serializables.add(serializable);
    }
    
    public static QuestLine getActiveQuestLine() {
        return activeQuestLine;
    }
    
    @Environment(EnvType.CLIENT)
    public static void receiveDataFromServer(Player receiver, boolean remote) {
        if (!hasLoadedMainSound) {
            SoundHandler.loadLoreReading(HardcoreQuestingCore.configDir);
            hasLoadedMainSound = true;
        }
        QuestLine questLine = reset(Optional.empty(), Optional.empty());
        questLine.loadAll();
        SoundHandler.loadLoreReading(HardcoreQuestingCore.configDir);
    }
    
    public static QuestLine reset(Optional<Path> basePath, Optional<Path> dataPath) {
        return activeQuestLine = new QuestLine(basePath, dataPath);
    }
    
    public static void sendDataToClient(ServerPlayer player) {
        QuestLine questLine = getActiveQuestLine();
        // Sync various data, but not for the player that hosts the server (if any), as they already share the same data as the server
        if (!player.getGameProfile().getName().equals(player.server.getSingleplayerName())) {
            boolean side = !HardcoreQuestingCore.platform.isClient();
            NetworkManager.sendToPlayer(new PlayerDataSyncMessage(questLine, !side, side, player), player);
    
            NetworkManager.sendToPlayer(new QuestLineSyncMessage(questLine), player);
    
            NetworkManager.sendToPlayer(new DeathStatsMessage(side), player);
        }
        NetworkManager.sendToPlayer(new TeamStatsMessage(StreamSupport.stream(questLine.teamManager.getNamedTeams().spliterator(), false)), player);
    }
    
    public void setMainDescription(String mainDescription) {
        this.mainDescription = mainDescription;
        this.cachedMainDescription = null;
    }
    
    public void saveAll() {
        for (Serializable serializable : serializables) {
            serializable.save();
        }
        SaveHelper.onSave();
    }
    
    public void saveData() {
        for (Serializable serializable : serializables) {
            if (serializable.isData())
                serializable.save();
        }
    }
    
    public void loadAll() {
        HardcoreQuestingCore.LOGGER.info("[HQM] Loading Quest Line, with %d temp paths. (%s)", tempPaths.size(), tempPaths.keySet().stream().sorted().collect(Collectors.joining(", ")));
        for (Serializable serializable : serializables) {
            serializable.load();
        }
        SaveHelper.onLoad();
        
        if (HardcoreQuestingCore.platform.isClient()) {
            resetClient();
        }
    }
    
    @Environment(EnvType.CLIENT)
    public List<FormattedText> getMainDescription(GuiBase gui) {
        if (cachedMainDescription == null) {
            cachedMainDescription = gui.getLinesFromText(Translator.plain(mainDescription), 0.7F, 130);
        }
        
        return cachedMainDescription;
    }
    
    public boolean resolve(String name, Consumer<FileProvider> pathConsumer) {
        Optional<FileProvider> provider = resolve(name);
        provider.ifPresent(pathConsumer);
        return provider.isPresent();
    }
    
    public boolean resolveData(String name, Consumer<FileProvider> pathConsumer) {
        Optional<FileProvider> provider = resolveData(name);
        provider.ifPresent(pathConsumer);
        return provider.isPresent();
    }
    
    public Optional<FileProvider> resolve(String name) {
        Optional<FileProvider> provider = basePath.map(path -> new PathProvider(path.resolve(name)));
        if (!provider.isPresent() && tempPaths.containsKey(name))
            provider = Optional.of(tempPaths.get(name));
        return provider;
    }
    
    public Optional<FileProvider> resolveData(String name) {
        Optional<FileProvider> provider = dataPath.map(path -> new PathProvider(path.resolve(name)));
        if (!provider.isPresent() && tempPaths.containsKey(name))
            provider = Optional.of(tempPaths.get(name));
        return provider;
    }
    
    public void provideTemp(SimpleSerializable serializable, String str) {
        provideTemp(serializable.filePath(), str);
    }
    
    public void provideTemp(String path, String str) {
        tempPaths.put(path, new FileProvider() {
            String s = str;
            
            @Override
            public Optional<String> get() {
                return Optional.of(s);
            }
            
            @Override
            public void set(String str) {
                s = str;
            }
        });
    }
    
    public interface FileProvider {
        Optional<String> get();
        
        void set(String str);
    }
    
    public static class PathProvider implements FileProvider {
        private final Path path;
        
        public PathProvider(Path path) {
            this.path = path;
        }
        
        @Override
        public Optional<String> get() {
            return SaveHandler.load(path);
        }
        
        @Override
        public void set(String str) {
            SaveHandler.save(path, str);
        }
    }
}
