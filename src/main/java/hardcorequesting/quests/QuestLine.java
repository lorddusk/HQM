package hardcorequesting.quests;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import hardcorequesting.bag.Group;
import hardcorequesting.bag.GroupTier;
import hardcorequesting.client.interfaces.GuiQuestBook;
import hardcorequesting.client.interfaces.edit.GuiEditMenuItem;
import hardcorequesting.client.sounds.SoundHandler;
import hardcorequesting.death.DeathStats;
import hardcorequesting.io.SaveHandler;
import hardcorequesting.network.NetworkManager;
import hardcorequesting.network.message.DeathStatsMessage;
import hardcorequesting.network.message.FullSyncMessage;
import hardcorequesting.reputation.Reputation;
import hardcorequesting.team.Team;
import hardcorequesting.util.SaveHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QuestLine {
    public QuestLine() {
        GroupTier.initBaseTiers(this);
    }

    public List<QuestSet> questSets;
    public Map<String, Quest> quests;
    public String mainDescription = "No description";
    public List<String> cachedMainDescription;
    public final List<GroupTier> tiers = new ArrayList<>();
    public final Map<String, Group> groups = new ConcurrentHashMap<>();
    public String mainPath;
    @SideOnly(Side.CLIENT)
    public ResourceLocation front;

    private static QuestLine config = new QuestLine();
    private static QuestLine server;
    private static QuestLine world;

    public static QuestLine getActiveQuestLine() {
        return server != null ? server : world != null ? world : config;
    }

    private static boolean hasLoadedMainSound;
    public static boolean doServerSync;

    public static void receiveServerSync(boolean local) {
        if (!hasLoadedMainSound) {
            SoundHandler.loadLoreReading(config.mainPath);
            hasLoadedMainSound = true;
        }
        GuiQuestBook.resetBookPosition();
        if (!local) {
            reset();
            server = new QuestLine();
            server.mainPath = config.mainPath;
            server.quests = new ConcurrentHashMap<>();
            server.questSets = new ArrayList<>();
        }
        loadAll(true);
        SoundHandler.loadLoreReading(getActiveQuestLine().mainPath);
    }

    public static void reset() {
        server = null;
        world = null;
    }

    public static void sendServerSync(EntityPlayer player) {
        MinecraftServer server = MinecraftServer.getServer();
        if (player instanceof EntityPlayerMP) {
            if (player.getCommandSenderName().equals(server.getServerOwner())) // Integrated server
                NetworkManager.sendToPlayer(new FullSyncMessage(true), (EntityPlayerMP) player);
            else {
                NetworkManager.sendToPlayer(new FullSyncMessage("TIMESTAMP"), (EntityPlayerMP) player);
                NetworkManager.sendToPlayer(new DeathStatsMessage("TIMESTAMP"), (EntityPlayerMP) player);
            }
        }
    }

    public static void loadWorldData(File worldPath, boolean isClient) {
        String path = new File(worldPath, "hqm").getAbsolutePath() + File.separator;
        File pathFile = new File(path);
        if (!pathFile.exists()) pathFile.mkdirs();
        world = new QuestLine();
        init(path, isClient);
    }

    public static void copyDefaults(File worldPath) {
        File path = new File(worldPath, "hqm");
        if (!path.exists()) path.mkdirs();
        SaveHandler.copyFolder(SaveHandler.getDefaultFolder(), path);
    }

    public static void saveDescription() {
        try {
            SaveHandler.saveDescription(SaveHandler.getLocalFile("description.txt"), QuestLine.getActiveQuestLine().mainDescription);
            if (Quest.saveDefault)
                SaveHandler.saveDescription(SaveHandler.getDefaultFile("description.txt"), QuestLine.getActiveQuestLine().mainDescription);
        } catch (IOException e) {
            FMLLog.log("HQM", Level.INFO, "Failed to load questing state");
        }
    }

    public static void loadDescription() {
        try {
            QuestLine.getActiveQuestLine().mainDescription = SaveHandler.loadDescription(SaveHandler.getLocalFile("description.txt"));
            QuestLine.getActiveQuestLine().cachedMainDescription = null;
        } catch (IOException e) {
            FMLLog.log("HQM", Level.INFO, "Failed to load questing state");
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
        SaveHelper.onSave();
    }

    public static void loadAll(boolean isClient) {
        QuestingData.loadState();
        QuestLine.loadDescription();
        DeathStats.loadAll(isClient);
        Reputation.loadAll();
        GroupTier.loadAll();
        Team.loadAll(isClient);
        QuestSet.loadAll();
        QuestingData.loadQuestingData();
        SaveHelper.onLoad();
        if (isClient)
            GuiEditMenuItem.Search.initItems();
    }

    public static void init(String path, boolean isClient) {
        QuestLine.getActiveQuestLine().mainPath = path;
        QuestLine.getActiveQuestLine().quests = new ConcurrentHashMap<>();
        QuestLine.getActiveQuestLine().questSets = new ArrayList<>();

        loadAll(isClient);
    }
}
