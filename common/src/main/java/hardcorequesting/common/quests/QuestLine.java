package hardcorequesting.common.quests;

import com.google.common.collect.Lists;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.bag.GroupTier;
import hardcorequesting.common.bag.GroupTierManager;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.graphic.QuestSetsGraphic;
import hardcorequesting.common.client.sounds.SoundHandler;
import hardcorequesting.common.death.DeathStatsManager;
import hardcorequesting.common.io.DataManager;
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

import java.util.List;
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
    private final DataManager dataManager;
    private final List<Serializable> serializables = Lists.newArrayList();
    
    private QuestLine(DataManager dataManager) {
        if (HardcoreQuestingCore.platform.isClient()) {
            resetClient();
        }
        this.dataManager = dataManager;
        this.reputationManager = new ReputationManager(this);
        this.groupTierManager = new GroupTierManager(this);
        this.questingDataManager = new QuestingDataManager(this);
        this.deathStatsManager = new DeathStatsManager(this);
        this.questSetsManager = new QuestSetsManager(this);
        this.teamManager = new TeamManager(this);
        GroupTier.initBaseTiers(this);
        
        add(new Serializable() {
            @Override
            public void save(DataManager dataManager) {
                dataManager.resolve("description.txt").set(mainDescription);
            }
            
            @Override
            public void load(DataManager dataManager) {
                setMainDescription(dataManager.resolve("description.txt").get().orElse("No description"));
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
        
        QuestSetsGraphic.loginReset();
    }
    
    public void add(Serializable serializable) {
        this.serializables.add(serializable);
    }
    
    public static QuestLine getActiveQuestLine() {
        return activeQuestLine;
    }
    
    @Environment(EnvType.CLIENT)
    public static void receiveDataFromServer(DataManager dataManager) {
        if (!hasLoadedMainSound) {
            SoundHandler.loadLoreReading(HardcoreQuestingCore.configDir);
            hasLoadedMainSound = true;
        }
        QuestLine questLine = reset(dataManager);
        questLine.loadAll();
        SoundHandler.loadLoreReading(HardcoreQuestingCore.configDir);
    }
    
    public static QuestLine reset(DataManager dataManager) {
        return activeQuestLine = new QuestLine(dataManager);
    }
    
    public static void sendDataToClient(ServerPlayer player) {
        // Sync various data, but not for the player that hosts the server (if any), as they already share the same data as the server
        if (!player.getGameProfile().getName().equals(player.server.getSingleplayerName())) {
            boolean side = !HardcoreQuestingCore.platform.isClient();
            QuestLine questLine = getActiveQuestLine();
            NetworkManager.sendToPlayer(new PlayerDataSyncMessage(questLine, !side, side, player), player);
    
            NetworkManager.sendToPlayer(new QuestLineSyncMessage(questLine), player);
    
            NetworkManager.sendToPlayer(new DeathStatsMessage(side), player);
            NetworkManager.sendToPlayer(new TeamStatsMessage(StreamSupport.stream(questLine.teamManager.getNamedTeams().spliterator(), false)), player);
        }
    }
    
    public void setMainDescription(String mainDescription) {
        this.mainDescription = mainDescription;
        this.cachedMainDescription = null;
    }
    
    public void saveAll() {
        for (Serializable serializable : serializables) {
            serializable.save(dataManager);
        }
        SaveHelper.onSave();
    }
    
    public void saveData() {
        for (Serializable serializable : serializables) {
            if (serializable.isData())
                serializable.save(dataManager);
        }
    }
    
    public void loadAll() {
        HardcoreQuestingCore.LOGGER.info("[HQM] Loading Quest Line, with data: %s", dataManager);
        for (Serializable serializable : serializables) {
            serializable.load(dataManager);
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
}
