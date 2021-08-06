package hardcorequesting.common.quests;

import com.google.common.collect.Iterables;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.bag.Group;
import hardcorequesting.common.bag.GroupData;
import hardcorequesting.common.client.sounds.SoundHandler;
import hardcorequesting.common.client.sounds.Sounds;
import hardcorequesting.common.config.HQMConfig;
import hardcorequesting.common.network.NetworkManager;
import hardcorequesting.common.network.message.LivesUpdate;
import hardcorequesting.common.team.*;
import hardcorequesting.common.util.Translator;
import net.minecraft.Util;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.UserBanListEntry;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class QuestingData {
    
    public UUID selectedQuestId = null;
    public int selectedTask = -1;
    public boolean playedLore;
    public boolean receivedBook;
    private Team team;
    private int lives;
    private UUID playerId;
    private String name;
    private Map<UUID, GroupData> groupData;
    
    public QuestingData(QuestingDataManager manager, UUID playerId) {
        this.lives = manager.getDefaultLives();
        this.playerId = playerId;
        this.team = Team.single(playerId);
        createGroupData();
    }
    
    public QuestingData(QuestingDataManager manager, UUID playerId, int lives, Map<UUID, GroupData> groupData) {
        this.playerId = playerId;
        this.lives = lives;
        this.team = TeamManager.getInstance().getByPlayer(playerId);
        createGroupData();
        this.groupData.putAll(groupData);
        manager.questingData.put(playerId, this);
    }
    
    public static Player getPlayerFromUsername(String playerName) {
        return HardcoreQuestingCore.getServer().getPlayerList().getPlayerByName(playerName);
    }
    
    public static Player getPlayer(String uuid) {
        if (uuid.split("-").length == 5) {
            return getPlayer(UUID.fromString(uuid));
        }
        return getPlayerFromUsername(uuid);
    }
    
    public static Player getPlayer(UUID uuid) {
        MinecraftServer server = HardcoreQuestingCore.getServer();
        return server == null ? null : server.getPlayerList().getPlayer(uuid);
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
    
    public void setName(String name) {
        this.name = name;
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
        
        this.lives = Math.min(i, max);
        
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
        QuestingDataManager manager = QuestingDataManager.getInstance();
        boolean shareLives = getTeam().isSharingLives();
        
        if (shareLives) {
            int dif = Math.min(this.lives - 1, amount);
            amount -= dif;
            this.lives -= dif;
            
            while (amount > 0) {
                int players = 0;
                for (PlayerEntry entry : manager.getQuestingData(player).getTeam().getPlayers()) {
                    if (!entry.getUUID().equals(player.getUUID()) && manager.getQuestingData(entry.getUUID()).getRawLives() > 1) {
                        players++;
                    }
                }
                if (players == 0) {
                    break;
                } else {
                    int id = (int) (Math.random() * players);
                    for (PlayerEntry entry : manager.getQuestingData(player).getTeam().getPlayers()) {
                        if (!entry.getUUID().equals(player.getUUID()) && manager.getQuestingData(entry.getUUID()).getRawLives() > 1) {
                            if (id == 0) {
                                manager.getQuestingData(entry.getUUID()).lives--;
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
            TeamLiteStat.refreshTeam(team);
        }
    }
    
    public void die(@NotNull Player player) {
        if (QuestingDataManager.getInstance().isHardcoreActive()) {
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
        QuestingDataManager manager = QuestingDataManager.getInstance();
        QuestingData data = manager.getQuestingData(player);
        Team team = data.getTeam();
        if (!team.isSingle() && !Iterables.isEmpty(TeamManager.getInstance().getNamedTeams())) {
            team.removePlayer(player);
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
            
            UserBanListEntry entry = new UserBanListEntry(player.getGameProfile(), null, setBannedBy, null, setBanReason);
            mcServer.getPlayerList().getBans().add(entry);
            
            //mcServer.getConfigurationManager().getBannedPlayers().put(banentry);
            ((ServerPlayer) player).connection.disconnect(Translator.translatable("hqm.message.gameOver"));
            SoundHandler.playToAll(Sounds.DEATH);
        }
        
        
    }
    
    @NotNull
    public Team getTeam() {
        if (!this.team.isSingle()) {
            Team t = TeamManager.getInstance().getByTeamId(this.team.getId());
            if (t != null) {
                this.team = t;
            }
        }
        return Objects.requireNonNull(this.team);
    }
    
    public void setTeam(@Nullable Team team) {
        if (team == null) {
            this.team = Team.single(this.playerId);
        } else if (team.isSingle()) {
            this.team = team;
        } else {
            Team t = TeamManager.getInstance().getByTeamId(team.getId());
            if (t != null) {
                this.team = t;
            } else {
                setTeam(null);
            }
        }
    }
    
    public Map<UUID, GroupData> getGroupData() {
        return groupData;
    }
}
