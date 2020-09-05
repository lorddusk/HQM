package hardcorequesting.quests;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.bag.Group;
import hardcorequesting.bag.GroupData;
import hardcorequesting.capabilities.ModCapabilities;
import hardcorequesting.client.sounds.SoundHandler;
import hardcorequesting.client.sounds.Sounds;
import hardcorequesting.config.HQMConfig;
import hardcorequesting.death.DeathStats;
import hardcorequesting.event.PlayerTracker;
import hardcorequesting.io.SaveHandler;
import hardcorequesting.items.ModItems;
import hardcorequesting.network.NetworkManager;
import hardcorequesting.network.message.LivesUpdate;
import hardcorequesting.team.PlayerEntry;
import hardcorequesting.team.Team;
import hardcorequesting.team.TeamStats;
import hardcorequesting.team.TeamUpdateSize;
import hardcorequesting.util.Translator;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.UserBanListEntry;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

public class QuestingData {
    
    // private static boolean debugActive = false;
    public static int defaultLives;
    public static boolean autoHardcoreActivate;
    public static boolean autoQuestActivate;
    private static boolean hardcoreActive;
    private static boolean questActive;
    private static HashMap<UUID, QuestingData> data = new HashMap<>();
    private static List<Team> teams = new ArrayList<>();
    public UUID selectedQuestId = null;
    public int selectedTask = -1;
    public boolean playedLore;
    public boolean receivedBook;
    private Team team;
    private int lives;
    private UUID playerId;
    private String name;
    private Map<UUID, GroupData> groupData;
    private DeathStats deathStat;
    
    private QuestingData(UUID playerId) {
        this.lives = defaultLives;
        this.playerId = playerId;
        this.team = new Team(playerId);
        createGroupData();
        deathStat = new DeathStats(playerId);
        data.put(playerId, this);
    }
    
    public QuestingData(UUID playerId, int lives, int teamId, Map<UUID, GroupData> groupData, DeathStats deathStat) {
        this.playerId = playerId;
        this.lives = lives;
        if (teamId > -1 && teamId < QuestingData.getTeams().size())
            this.team = QuestingData.getTeams().get(teamId);
        if (team == null) team = new Team(playerId);
        createGroupData();
        this.groupData.putAll(groupData);
        if (deathStat == null) this.deathStat = new DeathStats(playerId);
        else this.deathStat = deathStat;
        data.put(playerId, this);
    }
    
    public static HashMap<UUID, QuestingData> getData() {
        return data;
    }
    
    //keep all the red line code in one spot
    public static boolean isSinglePlayer() {
        return HardcoreQuesting.getServer().isSingleplayer();
    }
    
    public static boolean isHardcoreActive() {
        return hardcoreActive;
    }
    
    public static boolean isQuestActive() {
        return questActive;
    }
    
    public static void activateHardcore() {
        Boolean hardCore = null;
        try {
            hardCore = HardcoreQuesting.getServer().isHardcore();
        } catch (Exception e) {
            e.printStackTrace(); // todo exception handling
        }
        if (hardCore != null) {
            if (!hardcoreActive && !hardCore) {
                hardcoreActive = true;
            }
        }
    }
    
    public static void disableHardcore() {
        hardcoreActive = false;
    }
    
    public static void activateQuest(boolean giveBooks) {
        if (!questActive) {
            questActive = true;
            if (giveBooks) {
                for (Player player : HardcoreQuesting.getServer().getPlayerList().getPlayers()) {
                    if (player != null) {
                        spawnBook(player);
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
            HardcoreQuesting.LOGGER.error("Failed to save questing state!", e);
        }
    }
    
    public static void loadState(boolean remote) {
        try {
            SaveHandler.loadQuestingState(SaveHandler.getFile("state", remote));
        } catch (IOException e) {
            HardcoreQuesting.LOGGER.error("Failed to load questing state!", e);
        }
    }
    
    public static void saveQuestingData() {
        try {
            SaveHandler.saveQuestingData(SaveHandler.getLocalFile("data"));
        } catch (IOException e) {
            HardcoreQuesting.LOGGER.error("Failed to save questing data!", e);
        }
    }
    
    public static String saveQuestingData(Player entity) {
        return SaveHandler.saveQuestingData(QuestingData.getQuestingData(entity));
    }
    
    public static void loadQuestingData(boolean remote) {
        try {
            data.clear();
            SaveHandler.loadQuestingData(SaveHandler.getFile("data", remote)).forEach(qData -> data.put(qData.getPlayerId(), qData));
        } catch (IOException e) {
            HardcoreQuesting.LOGGER.error("Failed to load questing data!", e);
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
    
    public static QuestingData getQuestingData(Player player) {
        return getQuestingData(player.getUUID());
    }
    
    public static QuestingData getQuestingData(UUID uuid) {
        if (!data.containsKey(uuid))
            data.put(uuid, new QuestingData(uuid));
        
        return data.get(uuid);
    }
    
    public static void spawnBook(Player player) {
        if (!Quest.canQuestsBeEdited() && !player.level.isClientSide && HQMConfig.getInstance().SPAWN_BOOK && !QuestingData.getQuestingData(player).receivedBook && QuestingData.isQuestActive()) {
            QuestingData.getQuestingData(player).receivedBook = true;
            CompoundTag hqmTag = new CompoundTag();
            CompoundTag extraTag = ModCapabilities.PLAYER_EXTRA_DATA.get(player).tag;
            if (extraTag.contains(PlayerTracker.HQ_TAG))
                hqmTag = extraTag.getCompound(PlayerTracker.HQ_TAG);
            hqmTag.putBoolean(PlayerTracker.RECEIVED_BOOK, true);
            extraTag.put(PlayerTracker.HQ_TAG, hqmTag);
            ItemStack stack = new ItemStack(ModItems.book);
            if (!player.inventory.add(stack)) {
                spawnItemAtPlayer(player, stack);
            }
        }
    }
    
    private static void spawnItemAtPlayer(Player player, ItemStack stack) {
        ItemEntity item = new ItemEntity(player.level, player.getX() + 0.5D, player.getY() + 0.5D, player.getZ() + 0.5D, stack);
        player.level.addFreshEntity(item);
        item.playerTouch(player);
    }
    
    public static void addTeam(Team team) {
        team.setId(teams.size());
        teams.add(team);
    }
    
    public static Player getPlayerFromUsername(String playerName) {
        return HardcoreQuesting.getServer().getPlayerList().getPlayerByName(playerName);
    }
    
    public static Player getPlayer(String uuid) {
        if (uuid.split("-").length == 5) {
            return getPlayer(UUID.fromString(uuid));
        }
        return getPlayerFromUsername(uuid);
    }
    
    public static Player getPlayer(UUID uuid) {
        MinecraftServer server = HardcoreQuesting.getServer();
        return server == null ? null : server.getPlayerList().getPlayer(uuid);
    }
    
    public static void remove(Player player) {
        data.remove(player.getUUID());
    }
    
    public static boolean hasData(Player player) {
        return data.containsKey(player.getGameProfile().getId());
    }
    
    public static boolean hasData(UUID uuid) {
        return data.containsKey(uuid);
    }
    
    public DeathStats getDeathStat() {
        return deathStat;
    }
    
    public UUID getPlayerId() {
        return playerId;
    }
    
    public String getName() throws IllegalArgumentException {
        if (this.name == null) {
            // possible fix for #238
            Player player = QuestingData.getPlayer(this.playerId);
            if (player != null) {
                this.name = player.getScoreboardName();
            } else {
                this.name = "";
            }
        }
        return this.name;
    }
    
    private void createGroupData() {
        groupData = new HashMap<>();
        Group.getGroups().keySet().forEach(this::createGroupData);
    }
    
    private void createGroupData(UUID groupId) {
        groupData.put(groupId, new GroupData());
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
    
    public void setRawLives(int lives) {
        this.lives = lives;
    }
    
    public QuestData getQuestData(UUID questId) {
        return getTeam().getQuestData(questId);
    }
    
    public void setQuestData(UUID questId, QuestData data) {
        getTeam().setQuestData(questId, data);
    }
    
    public GroupData getGroupData(UUID groupId) {
        if (!groupData.containsKey(groupId))
            createGroupData(groupId);
        return groupData.get(groupId);
    }
    
    public int addLives(Player player, int amount) {
        int max = HQMConfig.getInstance().Hardcore.MAX_LIVES;
        int i = getRawLives() + amount;
        
        if (i <= max) {
            this.lives = i;
        } else {
            this.lives = max;
        }
        
        getTeam().refreshTeamLives();
        return this.lives;
    }
    
    public void removeLifeAndSendMessage(@NotNull Player player) {
        boolean isDead = !removeLives(player, 1);
        if (!isDead) {
            player.sendMessage(Translator.pluralTranslated(getLives() != 1, "hqm.message.lostLife", getLives()), Util.NIL_UUID);
        }
        if (getTeam().isSharingLives()) {
            for (PlayerEntry entry : getTeam().getPlayers()) {
                if (entry.isInTeam() && !entry.getUUID().equals(player.getUUID())) {
                    Player other = getPlayer(entry.getUUID());
                    if (other != null) {
                        other.sendMessage(
                                Translator.pluralTranslated(getLives() != 1, "hqm.message.lostTeamLife", player.getScoreboardName(), (isDead ? " " + Translator.get("hqm.message.andBanned") : ""), getLives()), Util.NIL_UUID);
                    }
                }
            }
        }
        
    }
    
    public boolean removeLives(@NotNull Player player, int amount) {
        boolean shareLives = getTeam().isSharingLives();
        
        if (shareLives) {
            int dif = Math.min(this.lives - 1, amount);
            amount -= dif;
            this.lives -= dif;
            
            while (amount > 0) {
                int players = 0;
                for (PlayerEntry entry : QuestingData.getQuestingData(player).getTeam().getPlayers()) {
                    if (!entry.getUUID().equals(player.getUUID()) && QuestingData.getQuestingData(entry.getUUID()).getRawLives() > 1) {
                        players++;
                    }
                }
                if (players == 0) {
                    break;
                } else {
                    int id = (int) (Math.random() * players);
                    for (PlayerEntry entry : QuestingData.getQuestingData(player).getTeam().getPlayers()) {
                        if (!entry.getUUID().equals(player.getUUID()) && QuestingData.getQuestingData(entry.getUUID()).getRawLives() > 1) {
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
        
        if (player instanceof ServerPlayer) {
            NetworkManager.sendToPlayer(new LivesUpdate(this.playerId, this.lives), (ServerPlayer) player);
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
    
    public void die(@NotNull Player player) {
        if (QuestingData.isHardcoreActive()) {
            removeLifeAndSendMessage(player);
        }
    }
    
    /**
     * Deletes the world or bans the player from the server. This is handled on the server side
     *
     * @param player The player that should be banned
     */
    //todo reinspect this whole behaviour
    private void outOfLives(Player player) {
        QuestingData data = QuestingData.getQuestingData(player);
        Team team = data.getTeam();
        if (!team.isSingle() && !teams.isEmpty()) {
            team.removePlayer(player.getUUID());
            if (team.getPlayerCount() == 0) {
                team.deleteTeam();
            } else {
                team.refreshTeamData(TeamUpdateSize.ALL);
            }
        }
        
        player.inventory.clearContent(); //had some problem with tconstruct, clear all items to prevent it
        
        MinecraftServer mcServer = player.getServer();
        
        if (mcServer.isSingleplayer()) {
            player.displayClientMessage(new TranslatableComponent("hqm.message.singlePlayerHardcore"), true);
        } else {
            String setBanReason = "Out of lives in Hardcore Questing mode";
            String setBannedBy = "HQM";
            
            UserBanListEntry userlistbansentry = new UserBanListEntry(player.getGameProfile(), null, setBannedBy, null, setBanReason);
            mcServer.getPlayerList().getBans().add(userlistbansentry);
            
            //mcServer.getConfigurationManager().getBannedPlayers().put(banentry);
            ((ServerPlayer) player).connection.disconnect(Translator.translatable("hqm.message.gameOver"));
            SoundHandler.playToAll(Sounds.DEATH);
        }
        
        
    }
    
    @NotNull
    public Team getTeam() {
        if (!this.team.isSingle()) {
            List<Team> teams = getTeams();
            if (teams.size() > this.team.getId()) {
                this.team = teams.get(this.team.getId());
            }
        }
        return this.team;
    }
    
    public void setTeam(@Nullable Team team) {
        if (team == null) {
            team = new Team(this.playerId);
        }
        this.team = team;
    }
    
    public Map<UUID, GroupData> getGroupData() {
        return groupData;
    }
}
