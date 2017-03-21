package hardcorequesting.team;

import hardcorequesting.io.SaveHandler;
import hardcorequesting.io.adapter.TeamAdapter;
import hardcorequesting.network.NetworkManager;
import hardcorequesting.network.message.FullSyncMessage;
import hardcorequesting.network.message.TeamMessage;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestData;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.quests.reward.ReputationReward;
import hardcorequesting.reputation.Reputation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Team {

    public static boolean reloadedInvites;
    private int id = -1;
    private List<PlayerEntry> players = new ArrayList<>();
    private List<Team> invites;
    private String name;
    private Map<String, Integer> reputation;
    private Map<String, QuestData> questData;
    private int clientTeamLives = -1;
    private RewardSetting rewardSetting = RewardSetting.getDefault();
    private LifeSetting lifeSetting = LifeSetting.SHARE;

    public Team(String playerUuid) {
        if (playerUuid != null)
            players.add(new PlayerEntry(playerUuid, true, true));
        createQuestData();
        createReputation();
        this.invites = new ArrayList<>();
    }

    public static void loadAll(boolean isClient, boolean remote) {
        try {
            QuestingData.getTeams().clear();
            TeamAdapter.clearInvitesMap();
            List<Team> teams = SaveHandler.loadTeams(SaveHandler.getFile("teams", remote));
            for (int i = 0; i < teams.size(); i++)
                QuestingData.getTeams().add(null);
            teams.stream().filter(team -> !team.isSingle()).forEach(team -> QuestingData.getTeams().set(team.getId(), team));
            TeamAdapter.commitInvitesMap();
            if (isClient)
                TeamStats.updateTeams(QuestingData.getTeams().stream().map(Team::toStat).collect(Collectors.toList()));
        } catch (IOException e) {
            FMLLog.log("HQM", Level.INFO, "Can't load teams");
        }
    }

    public static void saveAll() {
        try {
            SaveHandler.saveTeams(SaveHandler.getLocalFile("teams"));
        } catch (IOException e) {
            FMLLog.log("HQM", Level.INFO, "Failed saving teams");
        }
    }

    public static void declineAll(String playerName) {
        for (Team team : QuestingData.getTeams()) {
            for (Iterator<PlayerEntry> iterator = team.getPlayers().iterator(); iterator.hasNext(); ) {
                PlayerEntry playerEntry = iterator.next();
                if (!playerEntry.isInTeam() && playerEntry.getUUID().equals(playerName)) {
                    iterator.remove();
                    team.refreshTeamData(TeamUpdateSize.ONLY_OWNER);
                    break;
                }
            }
        }
    }

    public void resetProgress(Quest quest) {
        questData.put(quest.getId(), Quest.getQuest(quest.getId()).createData(getPlayerCount()));
        refreshData();
    }

    public Map<String, QuestData> getQuestData() {
        return this.questData;
    }

    public TeamStats toStat() {
        return new TeamStats(name, getPlayerCount(), getSharedLives(), getProgress());
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
            if (entry.shouldRefreshData() && entry.isInTeam())
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
                lives += QuestingData.getQuestingData(entry.getUUID()).getRawLives();
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

    public void removePlayer(EntityPlayer player) {
        removePlayer(QuestingData.getUserUUID(player));
    }

    public void removePlayer(String uuid) {
        int id = 0;
        for (PlayerEntry player : players) {
            if (player.isInTeam()) {
                if (player.getUUID().equals(uuid)) {
                    Team leaveTeam = new Team(uuid);
                    leaveTeam.getPlayers().get(0).setBookOpen(player.isBookOpen());
                    for (String i : questData.keySet()) {
                        QuestData leaveData = leaveTeam.questData.get(i);
                        QuestData data = questData.get(i);
                        if (data != null) {
                            boolean[] old = data.reward;
                            data.reward = new boolean[old.length - 1];
                            for (int j = 0; j < data.reward.length; j++) {
                                if (j < id) {
                                    data.reward[j] = old[j];
                                } else {
                                    data.reward[j] = old[j + 1];
                                }
                            }

                            leaveData.reward[0] = old[id];
                        }
                    }

                    players.remove(id);

                    for (String i : questData.keySet()) {
                        QuestData leaveData = leaveTeam.questData.get(i);
                        QuestData data = questData.get(i);
                        if (data != null && Quest.getQuest(i) != null) {
                            Quest.getQuest(i).copyProgress(leaveData, data);
                        }
                    }

                    for (String i : Reputation.getReputations().keySet()) {
                        Reputation reputation = Reputation.getReputation(i);
                        if (reputation != null) {
                            leaveTeam.setReputation(reputation, this.getReputation(reputation));
                        }
                    }

                    QuestingData.getQuestingData(uuid).setTeam(leaveTeam);
                    refreshTeamData(player, TeamUpdateSize.ONLY_MEMBERS);
                    leaveTeam.refreshTeamData(player, TeamUpdateSize.ONLY_MEMBERS);
                    NetworkManager.sendToPlayer(TeamUpdateType.LEAVE_TEAM.build(this, player.getUUID()), player.getPlayerMP());
                    break;
                }
                id++;
            }
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
                valid = entry.shouldRefreshData();
                break;
            case ONLY_MEMBERS:
                valid = entry.shouldRefreshData() && entry.isInTeam();
                break;
            case ONLY_OWNER:
                valid = entry.shouldRefreshData() && entry.isOwner();
                break;
        }

        if (valid) {
            NetworkManager.sendToPlayer(TeamUpdateType.FULL.build(this), entry.getPlayerMP());
        }
    }

    public void refreshTeamLives() {
        if (!isSingle() && isSharingLives()) {
            for (PlayerEntry entry : getPlayers()) {
                if (entry.shouldRefreshData()) {
                    NetworkManager.sendToPlayer(TeamUpdateType.LIVES.build(this), entry.getPlayerMP());
                }
            }
        }
    }

    public void refreshData() {
        for (PlayerEntry entry : getPlayers())
            if (entry.shouldRefreshData())
                NetworkManager.sendToPlayer(new FullSyncMessage(true), entry.getPlayerMP());
    }

    public void clearProgress() {
        questData.clear();
        createQuestData();
        int playerCount = getPlayerCount();
        for (Quest quest : Quest.getQuests().values()) {
            if (quest != null && questData.get(quest.getId()) != null) {
                quest.initRewards(playerCount, questData.get(quest.getId()));
            }
        }
        refreshData();
    }

    public void deleteTeam() {
        for (int i = players.size() - 1; i >= 0; i--) {
            PlayerEntry player = players.get(i);
            if (player.isInTeam()) {
                removePlayer(player.getUUID());
            } else {
                players.remove(i);
            }
        }

        List<Team> teams = QuestingData.getTeams();
        teams.remove(id);

        for (int i = id; i < teams.size(); i++) {
            Team team = teams.get(i);
            team.id--;
        }

        NetworkManager.sendToAllPlayers(TeamUpdateType.REMOVE_TEAM.build(this));

        //refresh all clients with open books
        for (EntityPlayer player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerList()) {
            Team team = QuestingData.getQuestingData(player).getTeam();
            PlayerEntry entry = team.getEntry(player.getUniqueID().toString());
            if (entry != null) {
                team.refreshTeamData(entry, TeamUpdateSize.ALL);
            }
        }
    }

    public List<Team> getInvites() {
        return invites;
    }

    public List<PlayerEntry> getPlayers() {
        return players;
    }

    public void create(String name) {
        NetworkManager.sendToServer(new TeamMessage(TeamAction.CREATE, name));
    }

    public void invite(String name) {
        NetworkManager.sendToServer(new TeamMessage(TeamAction.INVITE, name));
    }

    public void accept() {
        NetworkManager.sendToServer(new TeamMessage(TeamAction.ACCEPT, "" + id));
    }

    public void decline() {
        NetworkManager.sendToServer(new TeamMessage(TeamAction.DECLINE, "" + id));
    }

    public void kick(String name) {
        NetworkManager.sendToServer(new TeamMessage(TeamAction.KICK, name));
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

    public boolean isOwner(EntityPlayer player) {
        return isOwner(QuestingData.getUserUUID(player));
    }

    public boolean isOwner(String uuid) {
        PlayerEntry entry = getEntry(uuid);
        return entry != null && entry.isOwner();
    }

    public PlayerEntry getEntry(String uuid) {
        for (PlayerEntry playerEntry : getPlayers()) {
            if (playerEntry.getUUID().equals(uuid)) {
                return playerEntry;
            }
        }

        return null;
    }

    private void createReputation() {
        reputation = new HashMap<>();
        for (Reputation reputation : Reputation.getReputations().values())
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

    private void createQuestData(String id) {
        questData.put(id, Quest.getQuest(id).createData(1));
    }

    public QuestData getQuestData(String id) {
        if (!questData.containsKey(id)) {
            createQuestData(id);
        }
        return questData.get(id);
    }

    public void setQuestData(String id, QuestData data) {
        questData.put(id, data);
    }

    public boolean isSingle() {
        return id == -1;
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
}
