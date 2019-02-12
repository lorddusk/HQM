package hardcorequesting.quests;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.bag.Group;
import hardcorequesting.bag.GroupTier;
import hardcorequesting.client.interfaces.GuiQuestBook;
import hardcorequesting.client.interfaces.edit.GuiEditMenuItem;
import hardcorequesting.client.sounds.SoundHandler;
import hardcorequesting.death.DeathStats;
import hardcorequesting.io.SaveHandler;
import hardcorequesting.network.NetworkManager;
import hardcorequesting.network.message.DeathStatsMessage;
import hardcorequesting.network.message.PlayerDataSyncMessage;
import hardcorequesting.network.message.QuestLineSyncMessage;
import hardcorequesting.reputation.Reputation;
import hardcorequesting.team.Team;
import hardcorequesting.util.SaveHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class QuestLine {

    public static boolean doServerSync;
    private static QuestLine config = new QuestLine();
    private static QuestLine server;
    private static QuestLine world;
    private static boolean hasLoadedMainSound;
    public final List<GroupTier> tiers = new ArrayList<>();
    public final Map<UUID, Group> groups = new ConcurrentHashMap<>();
    public List<QuestSet> questSets;
    public final Map<UUID, Quest> quests = new ConcurrentHashMap<>();
    public String mainDescription = "No description";
    public List<String> cachedMainDescription;
    public String mainPath;
    @SideOnly(Side.CLIENT)
    public ResourceLocation front;

    public QuestLine() {
        GroupTier.initBaseTiers(this);
    }

    public static QuestLine getActiveQuestLine() {
        return server != null ? server : world != null ? world : config;
    }

    // client side only
    public static void receiveServerSync(EntityPlayer receiver, boolean local, boolean remote) {
        if (!hasLoadedMainSound) {
            SoundHandler.loadLoreReading(config.mainPath);
            hasLoadedMainSound = true;
        }
        GuiQuestBook.resetBookPosition();
        if (!local) {
            reset();
            server = new QuestLine();
            server.mainPath = config.mainPath;
            server.quests.clear();
            server.questSets = new ArrayList<>();
        }
        loadAll(true, remote);
        SoundHandler.loadLoreReading(getActiveQuestLine().mainPath);
    }

    public static void reset() {
        server = null;
        world = null;
    }

    public static void sendServerSync(EntityPlayer player) {
         if (player instanceof EntityPlayerMP) {
            EntityPlayerMP playerMP = (EntityPlayerMP) player;
            boolean side = HardcoreQuesting.loadingSide.isServer();
            if (FMLCommonHandler.instance().getMinecraftServerInstance().isSinglePlayer()) // Integrated server
                NetworkManager.sendToPlayer(new PlayerDataSyncMessage(true, false, player), playerMP);
            else {
                if (QuestLine.doServerSync) // Send actual data to player on server sync
                    NetworkManager.sendToPlayer(new QuestLineSyncMessage(), playerMP);
                NetworkManager.sendToPlayer(new PlayerDataSyncMessage(false, side, player), playerMP);
            }
            NetworkManager.sendToPlayer(new DeathStatsMessage(side), playerMP);
        }
    }

    public static void loadWorldData(File worldPath, boolean isClient) {
        File pathFile = new File(worldPath, "hqm");
        String path = pathFile.getAbsolutePath() + File.separator;
        if (!pathFile.exists()) pathFile.mkdirs();
        world = new QuestLine();
        init(path, isClient);
    }

    public static void saveDescription() {
        try {
            SaveHandler.saveDescription(SaveHandler.getLocalFile("description.txt"), QuestLine.getActiveQuestLine().mainDescription);
        } catch (IOException e) {
            HardcoreQuesting.LOG.error("Failed to load local questing state from descriptions.txt");
        }
    }

    public static void saveDescriptionDefault() {
        try {
            SaveHandler.saveDescription(SaveHandler.getDefaultFile("description.txt"), QuestLine.getActiveQuestLine().mainDescription);
        } catch (IOException e) {
            HardcoreQuesting.LOG.error("Failed to load default questing state from description.txt");
        }
    }

    public static void loadDescription(boolean remote) {
        try {
            QuestLine.getActiveQuestLine().mainDescription = SaveHandler.loadDescription(SaveHandler.getFile("description.txt", remote));
            QuestLine.getActiveQuestLine().cachedMainDescription = null;
        } catch (IOException e) {
            HardcoreQuesting.LOG.error("Failed to load remote questing state from description.txt");
        }
    }

    public static void saveAll() {
        QuestingData.saveState();
        QuestLine.saveDescription();
        DeathStats.saveAll();
        Reputation.saveAll();
        GroupTier.saveAll();
        QuestSet.saveAll();
        Team.saveAll();
        QuestingData.saveQuestingData();
        if (Quest.saveDefault && Quest.isEditing) { // Save the needed defaults during edit mode
            QuestLine.saveDescriptionDefault();
            Reputation.saveAllDefault();
            GroupTier.saveAllDefault();
            QuestSet.saveAllDefault();
        }
        SaveHelper.onSave();
    }

    public static void loadAll(boolean isClient, boolean remote) {
        QuestingData.loadState(remote);
        QuestLine.loadDescription(remote);
        DeathStats.loadAll(isClient, remote);
        Reputation.loadAll(remote);
        GroupTier.loadAll(remote);
        Team.loadAll(isClient, remote);
        QuestSet.loadAll(remote);
        QuestingData.loadQuestingData(remote);
        SaveHelper.onLoad();
        if (isClient)
            GuiEditMenuItem.Search.initItems();
    }

    public static void init(String path) {
        QuestLine.getActiveQuestLine().mainPath = path;
        QuestLine.getActiveQuestLine().quests.clear();
        QuestLine.getActiveQuestLine().questSets = new ArrayList<>();
    }

    public static void init(String path, boolean isClient) {
        init(path);
        loadAll(isClient, false);
    }

    public static void copyDefaults(File worldPath) {
        File path = new File(worldPath, "hqm");
        if (!path.exists()) path.mkdirs();
        SaveHandler.copyFolder(SaveHandler.getDefaultFolder(), path);
    }
}
