package hardcorequesting;


import net.minecraftforge.fml.common.FMLCommonHandler;
import hardcorequesting.network.*;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestData;
import hardcorequesting.reputation.Reputation;
import hardcorequesting.reward.ReputationReward;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Team {


    public void resetProgress(Quest quest) {
        questData.set(quest.getId(), Quest.getQuest(quest.getId()).createData(getPlayerCount()));
    }

    public float getProgress() {
        int completed = 0;
        int total = 0;
        for (QuestData data : questData) {
            if (data != null) {
                total++;
                if (data.completed) completed++;
            }
        }
        return (float) completed / total;
    }

    public void receiveAndSyncReputation(Quest quest, List<ReputationReward> reputationList) {
        for (ReputationReward reputationReward : reputationList) {
            setReputation(reputationReward.getReward(), getReputation(reputationReward.getReward()) + reputationReward.getValue());
        }

        DataWriter dw = getRefreshDataWriter(RefreshType.REPUTATION_RECEIVED);
        dw.writeData(quest.getId(), DataBitHelper.QUESTS);
        dw.writeData(reputationList.size(), DataBitHelper.REPUTATION);
        for (ReputationReward reputationReward : reputationList) {
            dw.writeData(reputationReward.getReward().getId(), DataBitHelper.REPUTATION);
            dw.writeData(getReputation(reputationReward.getReward()), DataBitHelper.REPUTATION_VALUE);
        }

        for (PlayerEntry entry : getPlayers()) {
            if (entry.shouldRefreshData() && entry.isInTeam()) {
                PacketHandler.sendToPlayer(entry.getName(), dw);
            }
        }
    }


    private void readReceivedReputationData(DataReader dr) {
        int questId = dr.readData(DataBitHelper.QUESTS);
        Quest quest = Quest.getQuest(questId);
        if (quest != null) {
            QuestData data = getQuestData(questId);
            if (data != null) {
                data.claimed = true;
                int count = dr.readData(DataBitHelper.REPUTATION);
                for (int i = 0; i < count; i++) {
                    int id = dr.readData(DataBitHelper.REPUTATION);
                    int val = dr.readData(DataBitHelper.REPUTATION_VALUE);
                    setReputation(id, val);
                }
            }
        }
    }


    public enum LifeSetting {
        SHARE("hqm.team.sharedLives.title", "hqm.team.sharedLives.desc"),
        INDIVIDUAL("hqm.team.individualLives.title", "hqm.team.individualLives.desc");

        private String title;
        private String description;

        LifeSetting(String title, String description) {
            this.title = title;
            this.description = description;
        }

        public String getTitle() {
            return Translator.translate(title);
        }

        public String getDescription() {
            return Translator.translate(description);
        }
    }

    public enum RewardSetting {
        ALL("hqm.team.allReward.title", "hqm.team.allReward.desc"),
        ANY("hqm.team.anyReward.title", "hqm.team.anyReward.desc"),
        RANDOM("hqm.team.randomReward.title", "hqm.team.randomReward.desc");

        private static RewardSetting getDefault() {
            return isAllModeEnabled ? ALL : ANY;
        }

        private String title;
        private String description;

        RewardSetting(String title, String description) {
            this.title = title;
            this.description = description;
        }

        public String getTitle() {
            return Translator.translate(title);
        }

        public String getDescription() {
            return Translator.translate(description);
        }

        public static boolean isAllModeEnabled;
    }

    private RewardSetting rewardSetting = RewardSetting.getDefault();
    private LifeSetting lifeSetting = LifeSetting.SHARE;

    public RewardSetting getRewardSetting() {
        return rewardSetting;
    }

    public LifeSetting getLifeSetting() {
        return lifeSetting;
    }

    private int clientTeamLives = -1;

    public int getId() {
        return id;
    }

    public boolean isSharingLives() {
        return lifeSetting == LifeSetting.SHARE;
    }

    public int getSharedLives() {
        if (clientTeamLives != -1) {
            return clientTeamLives;
        }

        int lives = 0;
        for (Team.PlayerEntry entry : getPlayers()) {
            if (entry.inTeam) {
                lives += QuestingData.getQuestingData(entry.getName()).getRawLives();
            }
        }
        return lives;
    }

    public int getPlayerCount() {
        int count = 0;
        for (PlayerEntry player : players) {
            if (player.inTeam) {
                count++;
            }
        }
        return count;
    }

    public void removePlayer(String playerName) {
        int id = 0;
        for (PlayerEntry player : players) {
            if (player.inTeam) {
                if (player.getName().equals(playerName)) {
                    Team leaveTeam = new Team(playerName);
                    leaveTeam.getPlayers().get(0).setBookOpen(player.bookOpen);
                    for (int i = 0; i < questData.size(); i++) {
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

                    for (int i = 0; i < questData.size(); i++) {
                        QuestData leaveData = leaveTeam.questData.get(i);
                        QuestData data = questData.get(i);
                        if (data != null && Quest.getQuest(i) != null) {
                            Quest.getQuest(i).copyProgress(leaveData, data);
                        }
                    }

                    for (int i = 0; i < Reputation.size(); i++) {
                        Reputation reputation = Reputation.getReputation(i);
                        if (reputation != null) {
                            leaveTeam.setReputation(reputation, this.getReputation(reputation));
                        }
                    }

                    QuestingData.getQuestingData(playerName).setTeam(leaveTeam);
                    break;
                }
                id++;
            }
        }


    }

    public enum UpdateType {
        ALL,
        ONLY_MEMBERS,
        ONLY_INVITES,
        ONLY_OWNER
    }

    public void refreshTeamData(UpdateType type) {
        for (PlayerEntry entry : getPlayers()) {
            refreshTeamData(entry, type);
        }
    }

    private void refreshTeamData(PlayerEntry entry, UpdateType type) {
        Team team = this;
        boolean valid = false;
        switch (type) {
            case ALL:
                if (entry.shouldRefreshData()) {
                    valid = true;
                    break; //the break is here on purpose
                }
            case ONLY_INVITES:
                //refresh that team instead
                team = QuestingData.getQuestingData(entry.getName()).getTeam();
                valid = !entry.isInTeam() && team.getEntry(entry.getName()).shouldRefreshData();
                break;
            case ONLY_MEMBERS:
                valid = entry.shouldRefreshData();
                break;
            case ONLY_OWNER:
                valid = entry.shouldRefreshData() && entry.isOwner();
                break;
        }

        if (valid) {
            DataWriter dw = getRefreshDataWriter(RefreshType.FULL);
            team.writeTeamData(dw, true);
            PacketHandler.sendToPlayer(entry.getName(), dw);
        }
    }

    public enum RefreshType {
        FULL,
        LIVES,
        REPUTATION_RECEIVED
    }

    private DataWriter getRefreshDataWriter(RefreshType type) {
        DataWriter dw = PacketHandler.getWriter(PacketId.REFRESH_TEAM);
        dw.writeEnum(type);
        return dw;
    }

    public void refreshTeamLives() {
        if (!isSingle() && isSharingLives()) {
            for (PlayerEntry entry : getPlayers()) {
                if (entry.shouldRefreshData()) {
                    DataWriter dw = getRefreshDataWriter(RefreshType.LIVES);
                    dw.writeData(getSharedLives(), DataBitHelper.TEAM_LIVES);
                    PacketHandler.sendToPlayer(entry.getName(), dw);
                }
            }
        }
    }


    public void refreshData() {
        for (PlayerEntry entry : getPlayers()) {
            if (entry.shouldRefreshData()) {
                QuestingData.getQuestingData(entry.getName()).refreshClientData(entry.getName());
            }
        }
    }

    public void clearProgress() {
        questData.clear();
        createQuestData();
        int playerCount = getPlayerCount();
        for (int i = 0; i < Quest.size(); i++) {
            if (Quest.getQuest(i) != null && questData.get(i) != null) {
                Quest.getQuest(i).preRead(playerCount, questData.get(i));
            }
        }
        refreshData();
    }

    public void onPacket(DataReader dr) {
        RefreshType type = dr.readEnum(RefreshType.class);
        switch (type) {
            case FULL:
                readTeamData(QuestingData.FILE_VERSION, dr, true);
                break;
            case LIVES:
                clientTeamLives = dr.readData(DataBitHelper.TEAM_LIVES);
                break;
            case REPUTATION_RECEIVED:
                readReceivedReputationData(dr);
                break;
        }

    }

    public void deleteTeam() {
        for (int i = players.size() - 1; i >= 0; i--) {
            PlayerEntry player = players.get(i);
            if (player.isInTeam()) {
                removePlayer(player.getName());
            } else {
                players.remove(i);
            }
            QuestingData.getQuestingData(player.getName()).getTeam().refreshTeamData(player, UpdateType.ONLY_MEMBERS);
        }


        List<Team> teams = QuestingData.getTeams();
        teams.remove(id);

        for (int i = id; i < teams.size(); i++) {
            Team team = teams.get(i);
            team.id--;
        }

        //refresh all clients with open books,
        for (String username : FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getAllUsernames()) {
            Team team = QuestingData.getQuestingData(username).getTeam();
            PlayerEntry entry = team.getEntry(username);
            if (entry != null) {
                team.refreshTeamData(entry, UpdateType.ONLY_MEMBERS);
            }
        }
    }


    private enum TeamAction {
        CREATE,
        INVITE,
        ACCEPT,
        DECLINE,
        KICK,
        LEAVE,
        DISBAND,
        NEXT_LIFE_SETTING,
        NEXT_REWARD_SETTING
    }

    public List<Team> getInvites() {
        return invites;
    }

    public List<PlayerEntry> getPlayers() {
        return players;
    }

    private DataWriter getWriter(TeamAction action) {
        DataWriter dw = PacketHandler.getWriter(PacketId.TEAM);
        dw.writeData(action.ordinal(), DataBitHelper.TEAM_ACTION_ID);
        return dw;
    }

    public void create(String name) {
        DataWriter dw = getWriter(TeamAction.CREATE);
        dw.writeString(name, DataBitHelper.NAME_LENGTH);
        PacketHandler.sendToServer(dw);
    }

    public void invite(String name) {
        DataWriter dw = getWriter(TeamAction.INVITE);
        dw.writeString(name, DataBitHelper.NAME_LENGTH);
        PacketHandler.sendToServer(dw);
    }

    public void accept() {
        DataWriter dw = getWriter(TeamAction.ACCEPT);
        dw.writeData(id, DataBitHelper.TEAMS);
        PacketHandler.sendToServer(dw);
    }

    public void decline() {
        DataWriter dw = getWriter(TeamAction.DECLINE);
        dw.writeData(id, DataBitHelper.TEAMS);
        PacketHandler.sendToServer(dw);
    }

    public void kick(String name) {
        DataWriter dw = getWriter(TeamAction.KICK);
        dw.writeString(name, DataBitHelper.NAME_LENGTH);
        PacketHandler.sendToServer(dw);
    }

    public void leave() {
        PacketHandler.sendToServer(getWriter(TeamAction.LEAVE));
    }

    public void disband() {
        PacketHandler.sendToServer(getWriter(TeamAction.DISBAND));
    }

    public void nextLifeSetting() {
        PacketHandler.sendToServer(getWriter(TeamAction.NEXT_LIFE_SETTING));
    }

    public void nextRewardSetting() {
        PacketHandler.sendToServer(getWriter(TeamAction.NEXT_REWARD_SETTING));
    }

    //slightly ugly but there's no real way of getting hold of the interface, this works perfectly fine
    public static ErrorMessage latestError;

    public enum ErrorMessage {
        INVALID_PLAYER("hqm.team.invalidPlayer.title", "hqm.team.invalidPlayer.desc"),
        IN_PARTY("hqm.team.playerInParty.title", "hqm.team.playerInParty.desc"),
        USED_NAME("hqm.team.usedTeamName.title", "hqm.team.usedTeamName.desc");

        private String header;
        private String message;

        ErrorMessage(String header, String message) {
            this.message = message;
            this.header = header;
        }

        public String getMessage() {
            return Translator.translate(message);
        }

        public String getHeader() {
            return Translator.translate(header);
        }

        public void sendToClient(EntityPlayer player) {
            DataWriter dw = PacketHandler.getWriter(PacketId.TEAM);
            dw.writeData(ordinal(), DataBitHelper.TEAM_ERROR);
            PacketHandler.sendToPlayer(QuestingData.getUserName(player), dw);
        }
    }

    public static void handlePacket(EntityPlayer player, DataReader dr, boolean onServer) {
        if (onServer) {
            handleServerPacket(player, dr);
        } else {
            handleClientPacket(player, dr);
        }
    }

    private static void handleClientPacket(EntityPlayer player, DataReader dr) {
        latestError = ErrorMessage.values()[dr.readData(DataBitHelper.TEAM_ERROR)];
    }

    private static void handleServerPacket(EntityPlayer player, DataReader dr) {
        TeamAction action = TeamAction.values()[dr.readData(DataBitHelper.TEAM_ACTION_ID)];
        String playerName = QuestingData.getUserName(player);
        Team team = QuestingData.getQuestingData(player).getTeam();
        switch (action) {
            case CREATE:
                if (team.isSingle()) {
                    String teamName = dr.readString(DataBitHelper.NAME_LENGTH);

                    if (teamName.length() == 0) {
                        break;
                    }

                    for (Team t : QuestingData.getTeams()) {
                        if (t.getName().equals(teamName)) {
                            ErrorMessage.USED_NAME.sendToClient(player);
                            return;
                        }
                    }

                    QuestingData.addTeam(team);
                    team.name = teamName;
                    team.refreshTeamData(UpdateType.ONLY_MEMBERS);

                    declineAll(playerName);
                    TeamStats.refreshTeam(team);
                }
                break;
            case INVITE:
                if (!team.isSingle() && team.isOwner(playerName)) {
                    PlayerEntry entry = new PlayerEntry(dr.readString(DataBitHelper.NAME_LENGTH), false, false);

                    if (!QuestingData.hasData(entry.getName())) {
                        ErrorMessage.INVALID_PLAYER.sendToClient(player);
                        break;
                    }

                    if (!QuestingData.getQuestingData(entry.getName()).getTeam().isSingle()) {
                        ErrorMessage.IN_PARTY.sendToClient(player);
                        break;
                    }

                    if (!team.players.contains(entry)) {
                        team.players.add(entry);
                        team.refreshTeamData(UpdateType.ONLY_MEMBERS);
                        QuestingData.getQuestingData(entry.getName()).getTeam().refreshTeamData(UpdateType.ONLY_MEMBERS);
                    }
                }
                break;
            case ACCEPT:
                if (team.isSingle()) {

                    int acceptId = dr.readData(DataBitHelper.TEAMS);
                    if (acceptId >= 0 && acceptId < QuestingData.getTeams().size()) {
                        Team inviteTeam = QuestingData.getTeams().get(acceptId);
                        int id = 0;
                        for (PlayerEntry entry : inviteTeam.getPlayers()) {
                            if (entry.inTeam) {
                                id++;
                            } else if (entry.name.equals(playerName)) {
                                entry.setBookOpen(team.getPlayers().get(0).bookOpen);
                                entry.inTeam = true;
                                QuestingData.getQuestingData(entry.name).setTeam(inviteTeam);
                                team.setId(inviteTeam.getId());

                                for (int i = 0; i < inviteTeam.questData.size(); i++) {
                                    QuestData joinData = team.questData.get(i);
                                    QuestData questData = inviteTeam.questData.get(i);
                                    if (questData != null) {
                                        boolean old[] = questData.reward;
                                        questData.reward = new boolean[old.length + 1];
                                        for (int j = 0; j < questData.reward.length; j++) {
                                            if (j == id) {
                                                questData.reward[j] = joinData.reward[0]; //you keep your reward
                                            } else if (j < id) {
                                                questData.reward[j] = old[j];
                                            } else {
                                                questData.reward[j] = old[j - 1];
                                            }

                                        }
                                    }
                                }

                                for (int i = 0; i < inviteTeam.questData.size(); i++) {
                                    QuestData joinData = team.questData.get(i);
                                    QuestData questData = inviteTeam.questData.get(i);
                                    if (questData != null && Quest.getQuest(i) != null) {
                                        Quest.getQuest(i).mergeProgress(playerName, questData, joinData);
                                    }
                                }

                                for (int i = 0; i < Reputation.size(); i++) {
                                    Reputation reputation = Reputation.getReputation(i);
                                    if (reputation != null) {
                                        int joinValue = team.getReputation(reputation);
                                        int teamValue = inviteTeam.getReputation(reputation);
                                        int targetValue;
                                        if (Math.abs(joinValue) > Math.abs(teamValue)) {
                                            targetValue = joinValue;
                                        } else {
                                            targetValue = teamValue;
                                        }
                                        team.setReputation(reputation, targetValue);
                                    }
                                }

                                inviteTeam.refreshData();
                                inviteTeam.refreshTeamData(UpdateType.ALL);
                                declineAll(playerName);
                                TeamStats.refreshTeam(team);
                                break;
                            }
                        }
                    }
                }
                break;
            case DECLINE:
                if (team.isSingle()) {
                    int declineId = dr.readData(DataBitHelper.TEAMS);
                    if (declineId >= 0 && declineId < QuestingData.getTeams().size()) {
                        Team inviteTeam = QuestingData.getTeams().get(declineId);
                        inviteTeam.getPlayers().remove(new PlayerEntry(playerName, false, false));
                        inviteTeam.refreshTeamData(UpdateType.ONLY_OWNER);
                        team.refreshTeamData(UpdateType.ONLY_MEMBERS);
                    }
                }
                break;
            case KICK:
                if (!team.isSingle() && team.isOwner(playerName)) {
                    String playerToRemove = dr.readString(DataBitHelper.NAME_LENGTH);
                    PlayerEntry entryToRemove = team.getEntry(playerToRemove);
                    if (!entryToRemove.isOwner()) {
                        if (entryToRemove.inTeam) {
                            team.removePlayer(playerToRemove);
                            team.refreshTeamData(UpdateType.ALL);
                            TeamStats.refreshTeam(team);
                        } else {
                            team.getPlayers().remove(entryToRemove);
                            team.refreshTeamData(UpdateType.ONLY_OWNER);
                        }

                        QuestingData.getQuestingData(playerToRemove).getTeam().refreshTeamData(UpdateType.ONLY_MEMBERS);
                    }
                }
                break;
            case LEAVE:
                if (!team.isSingle() && !team.isOwner(playerName)) {
                    team.removePlayer(playerName);
                    team.refreshTeamData(UpdateType.ALL);
                    QuestingData.getQuestingData(playerName).getTeam().refreshTeamData(UpdateType.ONLY_MEMBERS);
                    TeamStats.refreshTeam(team);
                }
                break;
            case DISBAND:
                if (!team.isSingle() && team.isOwner(playerName)) {
                    team.deleteTeam();
                    TeamStats.refreshTeam(team);
                }
                break;
            case NEXT_LIFE_SETTING:
                if (!team.isSingle() && team.isOwner(playerName)) {
                    team.lifeSetting = LifeSetting.values()[(team.lifeSetting.ordinal() + 1) % LifeSetting.values().length];
                    team.refreshTeamData(UpdateType.ALL);
                }
                break;
            case NEXT_REWARD_SETTING:
                if (!team.isSingle() && team.isOwner(playerName)) {
                    team.rewardSetting = RewardSetting.values()[(team.rewardSetting.ordinal() + 1) % RewardSetting.values().length];
                    if (team.rewardSetting == RewardSetting.ALL) {
                        team.rewardSetting = RewardSetting.getDefault();
                    }
                    team.refreshTeamData(UpdateType.ALL);
                }

        }

    }

    private boolean isOwner(String playerName) {
        PlayerEntry entry = getEntry(playerName);
        return entry != null && entry.isOwner();
    }

    public PlayerEntry getEntry(String playerName) {
        for (Team.PlayerEntry playerEntry : getPlayers()) {
            if (playerEntry.getName().equals(playerName)) {
                return playerEntry;
            }
        }

        return null;
    }

    private static void declineAll(String playerName) {
        for (Team team : QuestingData.getTeams()) {
            for (Iterator<PlayerEntry> iterator = team.getPlayers().iterator(); iterator.hasNext(); ) {
                PlayerEntry playerEntry = iterator.next();
                if (!playerEntry.isInTeam() && playerEntry.getName().equals(playerName)) {
                    iterator.remove();
                    team.refreshTeamData(UpdateType.ONLY_OWNER);
                    break;
                }
            }
        }
    }

    public static class PlayerEntry {
        private String name;
        private boolean inTeam;
        private boolean owner;
        private boolean bookOpen;

        public PlayerEntry(String name, boolean inTeam, boolean owner) {
            this.name = name;
            this.inTeam = inTeam;
            this.owner = owner;
            this.bookOpen = false;
        }

        public String getName() {
            return name;
        }

        public boolean isInTeam() {
            return inTeam;
        }

        public boolean isOwner() {
            return owner;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PlayerEntry entry = (PlayerEntry) o;

            return name != null ? name.equals(entry.name) : entry.name == null;
        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }

        public boolean shouldRefreshData() {
            return bookOpen || PacketHandler.getOverriddenBy(name) != null;
        }

        public boolean isBookOpen() {
            return bookOpen;
        }

        public void setBookOpen(boolean bookOpen) {
            this.bookOpen = bookOpen;
        }
    }

    private int id = -1;
    private List<PlayerEntry> players = new ArrayList<PlayerEntry>();
    private List<Team> invites;
    private String name;
    private List<Integer> reputation;

    public Team(String player) {
        if (player != null) {
            players.add(new PlayerEntry(player, true, true));
        }
        createQuestData();
        createReputation();
    }

    private void createReputation() {
        reputation = new ArrayList<Integer>();
        for (int i = 0; i < Reputation.size(); i++) {
            createReputation(i);
        }
    }

    public int getReputation(Reputation reputation) {
        return getReputation(reputation.getId());
    }

    public void setReputation(Reputation reputation, int value) {
        setReputation(reputation.getId(), value);
    }

    private void createReputation(int id) {
        if (Reputation.getReputation(id) == null) {
            reputation.add(null);
        } else {
            reputation.add(0);
        }
    }

    private void createMissingReputation(int id) {
        while (id >= reputation.size() && id < Reputation.size()) {
            createReputation(reputation.size());
        }
    }

    public int getReputation(int id) {
        createMissingReputation(id);
        if (id >= reputation.size()) {
            return 0;
        } else {
            Integer value = reputation.get(id);
            return value == null ? 0 : value;
        }
    }

    public void setReputation(int id, Integer value) {
        createMissingReputation(id);
        if (id < reputation.size()) {
            reputation.set(id, value);
        }
    }

    private List<QuestData> questData;

    private void createQuestData() {
        questData = new ArrayList<QuestData>();
        for (int i = 0; i < Quest.size(); i++) {
            createQuestData(i);
        }
    }

    private void createQuestData(int id) {
        if (id > questData.size()) {
            createQuestData(id - 1);
        }

        if (Quest.getQuest(id) != null) {
            questData.add(id, Quest.getQuest(id).createData(1));
        } else {
            questData.add(id, null);
        }
    }

    public QuestData getQuestData(int id) {
        if (id >= questData.size()) {
            createQuestData(id);
        }
        return questData.get(id);
    }

    public void loadData(FileVersion version, DataReader dr, boolean light) {
        readTeamData(version, dr, light);

        int playerCount = getPlayerCount();

        //quest progress
        if (light) {
            for (int i = 0; i < questData.size(); i++) {
                if (questData.get(i) != null) {
                    Quest.getQuest(i).preRead(playerCount, questData.get(i));
                    Quest.getQuest(i).read(dr, questData.get(i), version, true);
                }
            }

            for (int i = 0; i < Reputation.size(); i++) {
                if (Reputation.getReputation(i) != null) {
                    setReputation(i, dr.readData(DataBitHelper.REPUTATION_VALUE));
                }
            }
        } else {

            for (int i = 0; i < Quest.size(); i++) {
                Quest quest = Quest.getQuest(i);
                QuestData data = questData.get(i);
                if (quest != null && data != null) {
                    quest.preRead(playerCount, data);
                }
            }

            int count = dr.readData(DataBitHelper.QUESTS);
            for (int i = 0; i < count; i++) {
                int id = dr.readData(DataBitHelper.QUESTS);
                Quest quest = Quest.getQuest(id);
                int bits = -1;
                if (version.contains(FileVersion.REMOVED_QUESTS)) {
                    bits = dr.readData(DataBitHelper.INT);
                }
                if (quest != null && questData.get(id) != null) {
                    quest.read(dr, questData.get(id), version, false);
                } else if (version.contains(FileVersion.REMOVED_QUESTS)) {
                    dr.readData(bits); //Clear
                }
            }

            createReputation();
            if (version.contains(FileVersion.REPUTATION)) {
                int reputationCount = dr.readData(DataBitHelper.REPUTATION);
                for (int i = 0; i < reputationCount; i++) {
                    int id = dr.readData(DataBitHelper.REPUTATION);
                    int value = dr.readData(DataBitHelper.REPUTATION_VALUE);
                    if (Reputation.getReputation(id) != null) {
                        setReputation(id, value);
                    }
                }
            }
        }
    }

    public static boolean reloadedInvites;

    private void readTeamData(FileVersion version, DataReader dr, boolean light) {
        if (light) {
            setId(dr.readBoolean() ? -1 : 0);
            reloadedInvites = true;
            invites = null;
            if (isSingle()) {
                int count = dr.readData(DataBitHelper.TEAMS);
                if (count != 0) {
                    invites = new ArrayList<Team>();
                    for (int i = 0; i < count; i++) {
                        Team team = new Team(null);
                        team.loadTeamData(version, dr, true);
                        invites.add(team);
                    }
                }
            }
        }

        if (version.contains(FileVersion.TEAMS)) {
            if (!isSingle()) {
                loadTeamData(version, dr, light);
            }
        }

        if (light && !isSingle() && isSharingLives()) {
            clientTeamLives = dr.readData(DataBitHelper.TEAM_LIVES);
        }
    }

    private void loadTeamData(FileVersion version, DataReader dr, boolean light) {
        name = dr.readString(DataBitHelper.NAME_LENGTH);
        id = dr.readData(DataBitHelper.TEAMS);

        if (version.contains(FileVersion.TEAM_SETTINGS)) {
            lifeSetting = LifeSetting.values()[dr.readData(DataBitHelper.TEAM_LIVES_SETTING)];
            rewardSetting = RewardSetting.values()[dr.readData(DataBitHelper.TEAM_REWARD_SETTING)];
            if (rewardSetting == RewardSetting.ALL) {
                rewardSetting = RewardSetting.getDefault();
            }
        }

        players.clear();
        int count = dr.readData(DataBitHelper.PLAYERS);
        for (int i = 0; i < count; i++) {
            String name = dr.readString(DataBitHelper.NAME_LENGTH);
            if (name == null) {
                name = "Unknown";
            }
            boolean inTeam = dr.readBoolean();
            boolean owner = inTeam && dr.readBoolean();
            players.add(new PlayerEntry(name, inTeam, owner));
        }
    }


    public boolean isSingle() {
        return id == -1;
    }

    public void saveData(DataWriter dw, boolean light) {
        writeTeamData(dw, light);


        //quest progress
        if (light) {
            for (int i = 0; i < questData.size(); i++) {
                if (questData.get(i) != null && Quest.getQuest(i) != null) {
                    Quest.getQuest(i).save(dw, questData.get(i), true);
                }
            }

            for (int i = 0; i < Reputation.size(); i++) {
                if (Reputation.getReputation(i) != null) {
                    dw.writeData(getReputation(i), DataBitHelper.REPUTATION_VALUE);
                }
            }
        } else {
            int count = 0;
            for (Quest quest : Quest.getQuests()) {
                if (quest != null) {
                    count++;
                }
            }
            dw.writeData(count, DataBitHelper.QUESTS);
            for (int i = 0; i < questData.size(); i++) {
                if (questData.get(i) != null && Quest.getQuest(i) != null) {
                    dw.writeData(i, DataBitHelper.QUESTS);
                    dw.createBuffer(DataBitHelper.INT);
                    Quest.getQuest(i).save(dw, questData.get(i), false);
                    dw.flushBuffer();
                }
            }

            int reputationCount = 0;
            for (int i = 0; i < Reputation.size(); i++) {
                if (Reputation.getReputation(i) != null) {
                    reputationCount++;
                }
            }
            dw.writeData(reputationCount, DataBitHelper.REPUTATION);
            for (int i = 0; i < Reputation.size(); i++) {
                if (Reputation.getReputation(i) != null) {
                    dw.writeData(i, DataBitHelper.REPUTATION);
                    dw.writeData(getReputation(i), DataBitHelper.REPUTATION_VALUE);
                }
            }
        }
    }

    private void writeTeamData(DataWriter dw, boolean light) {
        if (light) {
            dw.writeBoolean(isSingle());

            if (isSingle()) {
                String name = players.get(0).name;
                List<Team> invitingTeams = new ArrayList<Team>();
                for (Team team : QuestingData.getTeams()) {
                    for (PlayerEntry player : team.players) {
                        if (!player.inTeam && player.name.equals(name)) {
                            invitingTeams.add(team);
                            break;
                        }
                    }
                }


                dw.writeData(invitingTeams.size(), DataBitHelper.TEAMS);
                for (Team invitingTeam : invitingTeams) {
                    invitingTeam.saveTeamData(dw, true);
                }
            }
        }

        if (!isSingle()) {
            saveTeamData(dw, light);
        }

        if (light && !isSingle() && isSharingLives()) {
            dw.writeData(getSharedLives(), DataBitHelper.TEAM_LIVES);
        }
    }

    private void saveTeamData(DataWriter dw, boolean light) {
        dw.writeString(name, DataBitHelper.NAME_LENGTH);
        dw.writeData(id, DataBitHelper.TEAMS);

        dw.writeData(lifeSetting.ordinal(), DataBitHelper.TEAM_LIVES_SETTING);
        dw.writeData(rewardSetting.ordinal(), DataBitHelper.TEAM_REWARD_SETTING);


        dw.writeData(players.size(), DataBitHelper.PLAYERS);
        for (PlayerEntry player : players) {
            dw.writeString(player.name, DataBitHelper.NAME_LENGTH);
            dw.writeBoolean(player.inTeam);
            if (player.inTeam) {
                dw.writeBoolean(player.owner);
            }
        }
    }

    public void postRead(QuestingData data, FileVersion version) {
        for (int i = 0; i < questData.size(); i++) {
            if (Quest.getQuest(i) != null && questData.get(i) != null) {
                Quest.getQuest(i).postRead(data, questData.get(i), version);
            }
        }
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
}
