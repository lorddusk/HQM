package hardcorequesting.common.team;

import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.io.SaveHandler;
import hardcorequesting.common.network.NetworkManager;
import hardcorequesting.common.network.message.TeamMessage;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.quests.data.QuestData;
import hardcorequesting.common.quests.reward.ReputationReward;
import hardcorequesting.common.reputation.Reputation;
import hardcorequesting.common.reputation.ReputationManager;
import net.minecraft.Util;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class Team {
    public static boolean reloadedInvites;
    @Nullable
    private UUID id = null;
    private List<PlayerEntry> players = new ArrayList<>();
    private final List<Team> invites;
    private String name;
    private Map<String, Integer> reputation;
    private Map<UUID, QuestData> questData;
    private int clientTeamLives = -1;
    private RewardSetting rewardSetting = RewardSetting.getDefault();
    private LifeSetting lifeSetting = LifeSetting.SHARE;
    
    private Team(UUID uuid) {
        if (uuid != null)
            players.add(new PlayerEntry(uuid, true, true));
        createQuestData();
        createReputation();
        this.invites = new ArrayList<>();
    }
    
    public static Team single(UUID player) {
        return new Team(player);
    }
    
    public static Team empty() {
        return new Team(null);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return isSingle() || team.isSingle() ? Objects.equals(players.get(0), team.players.get(0)) : Objects.equals(id, team.id);
    }
    
    @Override
    public int hashCode() {
        return isSingle() ? Objects.hash(players.get(0)) : Objects.hash(id);
    }
    
    public static void declineAll(UUID playerID) {
        for (Team team : TeamManager.getInstance().getTeams()) {
            for (Iterator<PlayerEntry> iterator = team.getPlayers().iterator(); iterator.hasNext(); ) {
                PlayerEntry playerEntry = iterator.next();
                if (!playerEntry.isInTeam() && playerEntry.getUUID().equals(playerID)) {
                    iterator.remove();
                    team.refreshTeamData(TeamUpdateSize.ONLY_OWNER);
                    break;
                }
            }
        }
    }
    
    public void resetProgress(Quest quest) {
        questData.put(quest.getQuestId(), Quest.getQuest(quest.getQuestId()).createData(getPlayerCount()));
        refreshData();
    }
    
    public void resetCompletion(Quest quest) {
        QuestData data = getQuestData(quest.getQuestId());
        data.completed = false;
        data.teamRewardClaimed = false;
        data.available = true;
        refreshData();
    }
    
    public Map<UUID, QuestData> getQuestData() {
        return this.questData;
    }
    
    public TeamLiteStat toLiteStat() {
        return new TeamLiteStat(name, getPlayerCount(), getSharedLives(), getProgress());
    }
    
    public float getProgress() {
        int completed = 0;
        int total = 0;
        for (QuestData data : questData.values()) {
            if (data != null) {
                total++;
                if (data.completed) completed++;
            }
        }
        return (float) completed / total;
    }
    
    public void receiveAndSyncReputation(Quest quest, List<ReputationReward> reputationList) {
        for (ReputationReward reputationReward : reputationList)
            setReputation(reputationReward.getReward(), getReputation(reputationReward.getReward()) + reputationReward.getValue());
        
        for (PlayerEntry entry : getPlayers())
            if (entry.isInTeam())
                NetworkManager.sendToPlayer(TeamUpdateType.REPUTATION_RECEIVED.build(this, quest, reputationList), entry.getPlayerMP());
    }
    
    public RewardSetting getRewardSetting() {
        return rewardSetting;
    }
    
    public void setRewardSetting(RewardSetting rewardSetting) {
        this.rewardSetting = rewardSetting;
    }
    
    public LifeSetting getLifeSetting() {
        return lifeSetting;
    }
    
    public void setLifeSetting(LifeSetting lifeSetting) {
        this.lifeSetting = lifeSetting;
    }
    
    public UUID getId() {
        return id == null ? Util.NIL_UUID : id;
    }
    
    public void setId(@Nullable UUID id) {
        this.id = Objects.equals(id, Util.NIL_UUID) ? null : id;
    }
    
    public boolean isSharingLives() {
        return lifeSetting == LifeSetting.SHARE;
    }
    
    public int getSharedLives() {
        if (clientTeamLives != -1) {
            return clientTeamLives;
        }
        
        int lives = 0;
        for (PlayerEntry entry : getPlayers()) {
            if (entry.isInTeam()) {
                lives += QuestingDataManager.getInstance().getQuestingData(entry.getUUID()).getRawLives();
            }
        }
        return lives;
    }
    
    public int getPlayerCount() {
        return (int) players.stream().filter(PlayerEntry::isInTeam).count();
    }
    
    public void addPlayer(PlayerEntry entry) {
        players.add(entry);
    }
    
    public void removePlayer(Player toBeRemoved) {
        int id = 0;
        Iterator<PlayerEntry> iterator = players.iterator();
        while (iterator.hasNext()) {
            PlayerEntry player = iterator.next();
            
            if (player.getUUID().equals(toBeRemoved.getUUID())) {
                Team newSingleTeam = Team.single(toBeRemoved.getUUID());
                for (UUID i : questData.keySet()) {
                    QuestData leaveData = newSingleTeam.questData.get(i);
                    QuestData data = questData.get(i);
                    if (data != null) {
                        leaveData.setCanClaimReward(0, data.canClaimPlayerReward(id));
                        
                        data.removePlayer(id);
                    }
                }
                
                iterator.remove();
                
                for (UUID i : questData.keySet()) {
                    QuestData leaveData = newSingleTeam.questData.get(i);
                    QuestData data = questData.get(i);
                    if (data != null && Quest.getQuest(i) != null) {
                        Quest.getQuest(i).copyProgress(leaveData, data);
                    }
                }
                
                ReputationManager reputationManager = ReputationManager.getInstance();
                for (String i : reputationManager.getReputations().keySet()) {
                    Reputation reputation = reputationManager.getReputation(i);
                    if (reputation != null) {
                        newSingleTeam.setReputation(reputation, this.getReputation(reputation));
                    }
                }
                
                QuestingDataManager.getInstance().getQuestingData(toBeRemoved).setTeam(newSingleTeam);
                refreshTeamData(player, TeamUpdateSize.ONLY_MEMBERS);
                newSingleTeam.refreshTeamData(player, TeamUpdateSize.ONLY_MEMBERS);
                NetworkManager.sendToPlayer(TeamUpdateType.LEAVE_TEAM.build(this, player.getUUID(), newSingleTeam), player.getPlayerMP());
                break;
            }
            id++;
        }
    }
    
    public void refreshTeamData(TeamUpdateSize type) {
        for (PlayerEntry entry : getPlayers())
            refreshTeamData(entry, type);
    }
    
    private void refreshTeamData(PlayerEntry entry, TeamUpdateSize type) {
        boolean valid = false;
        switch (type) {
            case ALL:
                valid = true;
                break;
            case ONLY_MEMBERS:
                valid = entry.isInTeam();
                break;
            case ONLY_OWNER:
                valid = entry.isOwner();
                break;
        }
        
        if (valid) {
            NetworkManager.sendToPlayer(TeamUpdateType.FULL.build(this), entry.getPlayerMP());
        }
    }
    
    public void refreshTeamLives() {
        if (!isSingle() && isSharingLives()) {
            for (PlayerEntry entry : getPlayers()) {
                NetworkManager.sendToPlayer(TeamUpdateType.LIVES.build(this), entry.getPlayerMP());
            }
        }
    }
    
    public void refreshData() {
        for (PlayerEntry entry : getPlayers())
            refreshTeamData(entry, TeamUpdateSize.ALL);
    }
    
    public void clearProgress() {
        questData.clear();
        createQuestData();
        int playerCount = getPlayerCount();
        for (Quest quest : Quest.getQuests().values()) {
            if (quest != null && questData.get(quest.getQuestId()) != null) {
                questData.get(quest.getQuestId()).clearRewardClaims(playerCount);
            }
        }
        refreshData();
    }
    
    public void deleteTeam() {
        if (isSingle())
            throw new IllegalStateException("Tried to delete a single team.");
        
        for (int i = players.size() - 1; i >= 0; i--) {
            PlayerEntry player = players.get(i);
            if (player.isInTeam()) {
                removePlayer(player.getPlayerMP());
            } else {
                players.remove(i);
            }
        }
        
        QuestingDataManager questingDataManager = QuestingDataManager.getInstance();
        TeamManager.getInstance().removeTeam(this);
        
        NetworkManager.sendToAllPlayers(TeamUpdateType.REMOVE_TEAM.build(this));
        
        //refresh all clients with open books
        for (Player player : HardcoreQuestingCore.getServer().getPlayerList().getPlayers()) {
            Team team = questingDataManager.getQuestingData(player).getTeam();
            PlayerEntry entry = team.getEntry(player.getUUID());
            if (entry != null) {
                team.refreshTeamData(entry, TeamUpdateSize.ALL);
            }
        }
    }
    
    @NotNull
    public List<Team> getInvites() {
        return invites;
    }
    
    /**
     * Returns all player entries in the team. This includes both team members and invited players.
     */
    public List<PlayerEntry> getPlayers() {
        return players;
    }
    
    public List<PlayerEntry> getTeamMembers() {
        return players.stream().filter(PlayerEntry::isInTeam).collect(Collectors.toList());
    }
    
    public void create(String name) {
        NetworkManager.sendToServer(new TeamMessage(TeamAction.CREATE, name));
    }
    
    public void invite(String name) {
        NetworkManager.sendToServer(new TeamMessage(TeamAction.INVITE, name));
    }
    
    public void accept() {
        NetworkManager.sendToServer(new TeamMessage(TeamAction.ACCEPT, "" + getId()));
    }
    
    public void decline() {
        NetworkManager.sendToServer(new TeamMessage(TeamAction.DECLINE, "" + getId()));
    }
    
    public void kick(UUID playerID) {
        NetworkManager.sendToServer(new TeamMessage(TeamAction.KICK, playerID.toString()));
    }
    
    public void leave() {
        NetworkManager.sendToServer(new TeamMessage(TeamAction.LEAVE, ""));
    }
    
    public void disband() {
        NetworkManager.sendToServer(new TeamMessage(TeamAction.DISBAND, ""));
    }
    
    public void nextLifeSetting() {
        NetworkManager.sendToServer(new TeamMessage(TeamAction.NEXT_LIFE_SETTING, ""));
    }
    
    public void nextRewardSetting() {
        NetworkManager.sendToServer(new TeamMessage(TeamAction.NEXT_REWARD_SETTING, ""));
    }
    
    public boolean isOwner(@NotNull Player player) {
        return isOwner(player.getUUID());
    }
    
    public boolean isOwner(@Nullable UUID uuid) {
        PlayerEntry entry = getEntry(uuid);
        return entry != null && entry.isOwner();
    }
    
    @Nullable
    public PlayerEntry getEntry(@Nullable UUID uuid) {
        for (PlayerEntry playerEntry : getPlayers()) {
            if (playerEntry.getUUID().equals(uuid)) {
                return playerEntry;
            }
        }
        return null;
    }
    
    private void createReputation() {
        reputation = new HashMap<>();
        for (Reputation reputation : ReputationManager.getInstance().getReputations().values())
            createReputation(reputation.getId());
    }
    
    public int getReputation(Reputation reputation) {
        return getReputation(reputation.getId());
    }
    
    public void setReputation(Reputation reputation, int value) {
        setReputation(reputation.getId(), value);
    }
    
    private void createReputation(String id) {
        reputation.put(id, 0);
    }
    
    public int getReputation(String id) {
        Integer value = reputation.get(id);
        return value == null ? 0 : value;
    }
    
    public void setReputation(String id, Integer value) {
        reputation.put(id, value);
    }
    
    private void createQuestData() {
        questData = new HashMap<>();
        Quest.getQuests().keySet().forEach(this::createQuestData);
    }
    
    private void createQuestData(UUID questId) {
        this.questData.put(questId, Quest.getQuest(questId).createData(1));
    }
    
    public QuestData getQuestData(UUID questId) {
        if (!this.questData.containsKey(questId)) {
            createQuestData(questId);
        }
        return this.questData.get(questId);
    }
    
    public void setQuestData(UUID questId, QuestData data) {
        questData.put(questId, data);
    }
    
    public boolean isSingle() {
        return id == null;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setClientTeamLives(int lives) {
        this.clientTeamLives = lives;
    }
    
    public void update(Team team) {
        this.name = team.name;
        this.questData = team.questData;
        this.reputation = team.reputation;
        this.lifeSetting = team.lifeSetting;
        this.rewardSetting = team.rewardSetting;
        this.players = team.players;
    }
    
    public static String saveTeam(Player entity) {
        Team team = QuestingDataManager.getInstance().getQuestingData(entity).getTeam();
        if (team.isSingle()) return ""; // return an empty string when the team is single
        return SaveHandler.saveTeam(team);
    }
}
