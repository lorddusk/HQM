package hardcorequesting.quests;

import com.mojang.authlib.GameProfile;
import hardcorequesting.bag.Group;
import hardcorequesting.bag.GroupData;
import hardcorequesting.client.sounds.SoundHandler;
import hardcorequesting.client.sounds.Sounds;
import hardcorequesting.config.ModConfig;
import hardcorequesting.death.DeathStats;
import hardcorequesting.event.PlayerTracker;
import hardcorequesting.io.SaveHandler;
import hardcorequesting.items.ModItems;
import hardcorequesting.team.PlayerEntry;
import hardcorequesting.team.Team;
import hardcorequesting.team.TeamStats;
import hardcorequesting.team.TeamUpdateSize;
import hardcorequesting.util.Translator;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListBansEntry;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.util.*;

public class QuestingData {

    // private static boolean debugActive = false;
    public static int defaultLives;
    public static boolean autoHardcoreActivate;
    public static boolean autoQuestActivate;
    private static boolean hardcoreActive;
    private static boolean questActive;
    private static HashMap<String, QuestingData> data = new HashMap<>();
    private static List<Team> teams = new ArrayList<>();
    public String selectedQuest = null;
    public int selectedTask = -1;
    public boolean playedLore;
    public boolean receivedBook;
    private Team team;
    private int lives;
    private String uuid;
    private String name;
    private Map<String, GroupData> groupData;
    private DeathStats deathStat;

    private QuestingData(String uuid) {
        this.lives = defaultLives;
        this.uuid = uuid;
        this.team = new Team(uuid);
        createGroupData();
        deathStat = new DeathStats(uuid);
        data.put(uuid, this);
    }

    public QuestingData(String uuid, int lives, int teamId, Map<String, GroupData> groupData, DeathStats deathStat) {
        this.uuid = uuid;
        this.lives = lives;
        if (teamId > -1 && teamId < QuestingData.getTeams().size())
            this.team = QuestingData.getTeams().get(teamId);
        if (team == null) team = new Team(uuid);
        createGroupData();
        this.groupData.putAll(groupData);
        if (deathStat == null) this.deathStat = new DeathStats(uuid);
        else this.deathStat = deathStat;
        data.put(uuid, this);
    }

    public static HashMap<String, QuestingData> getData() {
        return data;
    }

    //keep all the red line code in one spot
    public static boolean isSinglePlayer() {
        return FMLCommonHandler.instance().getMinecraftServerInstance().isSinglePlayer();
    }

    public static boolean isHardcoreActive() {
        return hardcoreActive;
    }

    public static boolean isQuestActive() {
        return questActive;
    }

    public static void activateHardcore() {
        if (!hardcoreActive && !FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getWorldInfo().isHardcoreModeEnabled()) {
            hardcoreActive = true;
        }
    }

    public static void disableHardcore() {
        hardcoreActive = false;
    }

    public static void activateQuest(boolean giveBooks) {
        if (!questActive) {
            questActive = true;
            if (giveBooks) {
                for (GameProfile profile : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getOnlinePlayerProfiles()) {
                    if (profile != null) {
                        EntityPlayer player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(profile.getId());
                        if (player != null) {
                            spawnBook(player);
                        }
                    }
                }
            }
        }
    }

    public static void deactivate() {
        if (hardcoreActive || questActive /*|| debugActive*/) {
            hardcoreActive = false;
            questActive = false;
            //debugActive = false;
            data = new HashMap<>();
            teams = new ArrayList<>();
        }
    }

    public static void saveState() {
        try {
            SaveHandler.saveQuestingState(SaveHandler.getLocalFile("state"));
        } catch (IOException e) {
            FMLLog.log("HQM", Level.INFO, "Failed to save questing state");
        }
    }

    public static void loadState() {
        try {
            SaveHandler.loadQuestingState(SaveHandler.getLocalFile("state"));
        } catch (IOException e) {
            FMLLog.log("HQM", Level.INFO, "Failed to load questing state");
        }
    }

    public static void saveQuestingData() {
        try {
            SaveHandler.saveQuestingData(SaveHandler.getLocalFile("data"));
        } catch (IOException e) {
            FMLLog.log("HQM", Level.INFO, "Failed to save questing data");
        }
    }

    public static void loadQuestingData() {
        try {
            data.clear();
            SaveHandler.loadQuestingData(SaveHandler.getLocalFile("data")).forEach(qData -> data.put(qData.getUuid(), qData));
        } catch (IOException e) {
            FMLLog.log("HQM", Level.INFO, "Failed to load questing data");
        }
    }

    public static List<Team> getTeams() {
        return teams;
    }

    public static List<Team> getAllTeams() {
        List<Team> all = new ArrayList<>();
        all.addAll(getTeams());
        data.values().stream().filter(questingData -> questingData.getTeam().isSingle()).forEach(player -> all.add(player.getTeam()));
        return all;
    }

   /* public static boolean isDebugActive() {
        return debugActive;
    }*/

    public static QuestingData getQuestingData(EntityPlayer player) {
        return getQuestingData(getUserUUID(player));
    }

    public static String getUserUUID(EntityPlayer player) {
        return player.getPersistentID().toString();
    }

    /*public static void activateDebug() {
        if(!debugActive)
            debugActive = true;
    }

    public static void deactivateDebug() {
        if(debugActive)
            debugActive = false;
    }*/

    public static QuestingData getQuestingData(String uuid) {
        if (!data.containsKey(uuid))
            new QuestingData(uuid);

        return data.get(uuid);
    }

    public static void disableVanillaHardcore(ICommandSender sender) {
        if (sender.getServer().getEntityWorld().getWorldInfo().isHardcoreModeEnabled()) {
            sender.sendMessage(new TextComponentTranslation("hqm.message.vanillaHardcore"));
            try {
                ReflectionHelper.setPrivateValue(WorldInfo.class, sender.getEntityWorld().getWorldInfo(), false, 20);
            } catch (Throwable ex) {
                ex.printStackTrace();
            }

            if (!sender.getServer().getEntityWorld().getWorldInfo().isHardcoreModeEnabled()) {
                sender.sendMessage(new TextComponentTranslation("hqm.message.vanillaHardcoreOverride"));
            }
        }
    }

    public static void spawnBook(EntityPlayer player) {
        if (!Quest.isEditing && !player.world.isRemote && ModConfig.spawnBook && !QuestingData.getQuestingData(player).receivedBook && QuestingData.isQuestActive()) {
            QuestingData.getQuestingData(player).receivedBook = true;
            NBTTagCompound hqmTag = new NBTTagCompound();
            if (player.getEntityData().hasKey(PlayerTracker.HQ_TAG))
                hqmTag = player.getEntityData().getCompoundTag(PlayerTracker.HQ_TAG);
            hqmTag.setBoolean(PlayerTracker.RECEIVED_BOOK, true);
            player.getEntityData().setTag(PlayerTracker.HQ_TAG, hqmTag);
            ItemStack stack = new ItemStack(ModItems.book);
            if (!player.inventory.addItemStackToInventory(stack)) {
                spawnItemAtPlayer(player, stack);
            }
        }
    }

    private static void spawnItemAtPlayer(EntityPlayer player, ItemStack stack) {
        EntityItem item = new EntityItem(player.world, player.posX + 0.5D, player.posY + 0.5D, player.posZ + 0.5D, stack);
        player.world.spawnEntity(item);
        if (!(player instanceof FakePlayer))
            item.onCollideWithPlayer(player);
    }

    public static void addTeam(Team team) {
        team.setId(teams.size());
        teams.add(team);
    }

    public static EntityPlayer getPlayerFromUsername(String playerName) {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(playerName);
    }

    public static EntityPlayer getPlayer(String uuid) {
        return getPlayer(UUID.fromString(uuid));
    }

    public static EntityPlayer getPlayer(UUID uuid) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        return server == null ? null : server.getPlayerList().getPlayerByUUID(uuid);
    }

    public static void remove(EntityPlayer player) {
        data.remove(getUserUUID(player));
    }

    public static boolean hasData(EntityPlayer player) {
        return data.containsKey(player.getGameProfile().getId().toString());
    }

    public static boolean hasData(UUID uuid) {
        return data.containsKey(uuid.toString());
    }

    public static boolean hasData(String uuid) {
        return data.containsKey(uuid);
    }

    public DeathStats getDeathStat() {
        return deathStat;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        if (name == null) {
            try { // possible fix for #238
                EntityPlayer player = QuestingData.getPlayer(uuid);
                if (player != null) {
                    name = player.getDisplayNameString();
                } else {
                    name = "";
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return uuid != null ? uuid : "";
            }
        }
        return name;
    }

    private void createGroupData() {
        groupData = new HashMap<>();
        Group.getGroups().keySet().forEach(this::createGroupData);
    }

    private void createGroupData(String id) {
        groupData.put(id, new GroupData());
    }

    public int getLives() {
        boolean shareLives = getTeam().isSharingLives();
        return shareLives ? getTeam().getSharedLives() : getRawLives();
    }

    public int getLivesToStayAlive() {
        boolean shareLives = getTeam().isSharingLives();
        return shareLives ? getTeam().getPlayerCount() : 1;
    }

    public int getRawLives() {
        return lives;
    }

    public QuestData getQuestData(String id) {
        return getTeam().getQuestData(id);
    }

    public void setQuestData(String id, QuestData data) {
        getTeam().setQuestData(id, data);
    }

    public GroupData getGroupData(String id) {
        if (!groupData.containsKey(id))
            createGroupData(id);
        return groupData.get(id);
    }

    public int addLives(EntityPlayer player, int amount) {
        int max = ModConfig.MAXLIVES;
        int i = getRawLives() + amount;

        if (i <= max) {
            this.lives = i;
        } else {
            this.lives = max;
        }

        getTeam().refreshTeamLives();
        return this.lives;
    }

    public void removeLifeAndSendMessage(EntityPlayer player) {
        boolean isDead = !removeLives(player, 1);
        if (!isDead) {
            player.sendMessage(new TextComponentString(Translator.translate(getLives() != 1, "hqm.message.lostLife", getLives())));
        }
        if (getTeam().isSharingLives()) {
            for (PlayerEntry entry : getTeam().getPlayers()) {
                if (entry.isInTeam() && !entry.getUUID().equals(getUserUUID(player))) {
                    EntityPlayer other = getPlayer(entry.getUUID());
                    if (other != null) {
                        other.sendMessage(new TextComponentString(
                                Translator.translate(getLives() != 1, "hqm.message.lostTeamLife", getUserUUID(player), (isDead ? " " + Translator.translate("hqm.message.andBanned") : ""), getLives())));
                    }
                }
            }
        }

    }

    public boolean removeLives(EntityPlayer player, int amount) {
        boolean shareLives = getTeam().isSharingLives();

        if (shareLives) {
            int dif = Math.min(this.lives - 1, amount);
            amount -= dif;
            this.lives -= dif;

            while (amount > 0) {
                int players = 0;
                for (PlayerEntry entry : QuestingData.getQuestingData(player).getTeam().getPlayers()) {
                    if (!entry.getUUID().equals(getUserUUID(player)) && QuestingData.getQuestingData(entry.getUUID()).getRawLives() > 1) {
                        players++;
                    }
                }
                if (players == 0) {
                    break;
                } else {
                    int id = (int) (Math.random() * players);
                    for (PlayerEntry entry : QuestingData.getQuestingData(player).getTeam().getPlayers()) {
                        if (!entry.getUUID().equals(getUserUUID(player)) && QuestingData.getQuestingData(entry.getUUID()).getRawLives() > 1) {
                            if (id == 0) {
                                QuestingData.getQuestingData(entry.getUUID()).lives--;
                                amount--;
                                break;
                            }
                            id--;
                        }
                    }
                }
            }

            this.lives -= amount;
        } else {
            this.lives = getRawLives() - amount;
        }

        getTeam().refreshTeamLives();
        try {
            if (getLives() < getLivesToStayAlive()) {
                outOfLives(player);
                return false;
            }

            return true;
        } finally {
            TeamStats.refreshTeam(team);
        }
    }

    public void die(EntityPlayer player) {
        if (!QuestingData.isHardcoreActive()) return;

        removeLifeAndSendMessage(player);
    }

    /**
     * Deletes the world or bans the player from the server. This is handled on the server side
     *
     * @param playerEntity The player that should be banned
     */
    private void outOfLives(EntityPlayer playerEntity) {
        QuestingData data = QuestingData.getQuestingData(playerEntity);
        Team team = data.getTeam();
        if (!team.isSingle() && !teams.isEmpty()) {
            team.removePlayer(QuestingData.getUserUUID(playerEntity));
            if (team.getPlayerCount() == 0) {
                team.deleteTeam();
            } else {
                team.refreshTeamData(TeamUpdateSize.ALL);
            }
        }

        playerEntity.inventory.clear(); //had some problem with tconstruct, clear all items to prevent it

        MinecraftServer mcServer = playerEntity.getServer();

        if (mcServer.isSinglePlayer() && playerEntity.getName().equals(mcServer.getServerOwner())) {
            ((EntityPlayerMP) playerEntity).connection.disconnect(Translator.translate("hqm.message.gameOver"));

            /*ReflectionHelper.setPrivateValue(MinecraftServer.class, mcServer, true, 41);
            mcServer.getActiveAnvilConverter().flushCache();

            mcServer.getActiveAnvilConverter().deleteWorldDirectory(mcServer.worldServers[0].getSaveHandler().getWorldDirectoryName());
            mcServer.initiateShutdown();*/
            // @todo: is this correct?
            mcServer.getActiveAnvilConverter().flushCache();
            mcServer.getActiveAnvilConverter().deleteWorldDirectory(mcServer.worlds[0].getSaveHandler().getWorldDirectory().getName());
            mcServer.initiateShutdown();
//            mcServer.deleteWorldAndStopServer();

        } else {
            String setBanReason = "Out of lives in Hardcore Questing mode";
            String setBannedBy = "HQM";

            UserListBansEntry userlistbansentry = new UserListBansEntry(playerEntity.getGameProfile(), (Date) null, setBannedBy, (Date) null, setBanReason);
            mcServer.getPlayerList().getBannedPlayers().addEntry(userlistbansentry);

            //mcServer.getConfigurationManager().getBannedPlayers().put(banentry);
            ((EntityPlayerMP) playerEntity).connection.disconnect(Translator.translate("hqm.message.gameOver"));
            SoundHandler.playToAll(Sounds.DEATH);
        }


    }

    public Team getTeam() {
        if (!team.isSingle() && !getTeams().isEmpty())
            team = getTeams().get(team.getId());
        return team;
    }

    public void setTeam(Team team) {
        if (team == null) team = new Team(uuid);
        this.team = team;
    }

    public Map<String, GroupData> getGroupData() {
        return groupData;
    }
}
