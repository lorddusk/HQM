package hardcorequesting.common.quests;

import com.google.common.collect.Lists;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.bag.GroupTier;
import hardcorequesting.common.bag.GroupTierManager;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.graphic.QuestSetsGraphic;
import hardcorequesting.common.client.sounds.SoundHandler;
import hardcorequesting.common.death.DeathStatsManager;
import hardcorequesting.common.io.DataReader;
import hardcorequesting.common.io.DataWriter;
import hardcorequesting.common.network.NetworkManager;
import hardcorequesting.common.network.message.DeathStatsMessage;
import hardcorequesting.common.network.message.PlayerDataSyncMessage;
import hardcorequesting.common.network.message.QuestLineSyncMessage;
import hardcorequesting.common.network.message.TeamStatsMessage;
import hardcorequesting.common.reputation.ReputationManager;
import hardcorequesting.common.team.TeamManager;
import hardcorequesting.common.util.SaveHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

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
    public final Serializable descriptionManager;
    
    private String mainDescription = "No description";
    @Environment(EnvType.CLIENT)
    public ResourceLocation front;
    private final List<Serializable> serializables = Lists.newArrayList();
    
    private QuestLine() {
        if (HardcoreQuestingCore.platform.isClient()) {
            resetClient();
        }
        this.reputationManager = new ReputationManager();
        this.groupTierManager = new GroupTierManager();
        this.questingDataManager = new QuestingDataManager();
        this.deathStatsManager = new DeathStatsManager();
        this.questSetsManager = new QuestSetsManager();
        this.teamManager = new TeamManager();
        this.descriptionManager = new SimpleSerializable() {
            @Override
            public String filePath() {
                return "description.txt";
            }
        
            @Override
            public String saveToString() {
                return getMainDescription();
            }
    
            @Override
            public void clear() {
                setMainDescription("No description");
            }
    
            @Override
            public void loadFromString(String string) {
                setMainDescription(string);
            }
        
            @Override
            public boolean isData() {
                return false;
            }
        };
        GroupTier.initBaseTiers(this);
    
        add(descriptionManager);
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
    public static void receiveDataFromServer(DataReader reader) {
        if (!hasLoadedMainSound) {
            SoundHandler.loadLoreReading(HardcoreQuestingCore.configDir);
            hasLoadedMainSound = true;
        }
        QuestLine questLine = reset();
        questLine.loadAll(reader, reader);
        SoundHandler.loadLoreReading(HardcoreQuestingCore.configDir);
    }
    
    public static QuestLine reset() {
        return activeQuestLine = new QuestLine();
    }
    
    public static void sendDataToClient(ServerPlayer player) {
        QuestLine questLine = getActiveQuestLine();
        // Sync various data, but not for the player that hosts the server (if any), as they already share the same data as the server
        if (!player.server.isSingleplayerOwner(player.getGameProfile())) {
            boolean side = !HardcoreQuestingCore.platform.isClient();
            NetworkManager.sendToPlayer(new PlayerDataSyncMessage(questLine, !side, side, player), player);
    
            NetworkManager.sendToPlayer(new QuestLineSyncMessage(questLine), player);
    
            NetworkManager.sendToPlayer(new DeathStatsMessage(side), player);
        }
        NetworkManager.sendToPlayer(new TeamStatsMessage(StreamSupport.stream(questLine.teamManager.getNamedTeams().spliterator(), false)), player);
    }
    
    public String getMainDescription() {
        return mainDescription;
    }
    
    public void setMainDescription(String mainDescription) {
        this.mainDescription = mainDescription;
    }
    
    public void saveQuests(@NotNull DataWriter cfqWriter) {
        for (Serializable serializable : serializables) {
            if (!serializable.isData())
                serializable.save(cfqWriter);
        }
    }
    
    public void saveData(@NotNull DataWriter dataWriter) {
        for (Serializable serializable : serializables) {
            if (serializable.isData())
                serializable.save(dataWriter);
        }
    }
    
    public void loadAll(DataReader cfgReader, DataReader dataReader) {
        HardcoreQuestingCore.LOGGER.info("[HQM] Loading Quest Line, with data: %s", cfgReader);
        
        for (Serializable serializable : serializables) {
            serializable.load(serializable.isData() ? dataReader : cfgReader);
        }
        
        SaveHelper.onLoad();
        
        if (HardcoreQuestingCore.platform.isClient()) {
            resetClient();
        }
    }
}
