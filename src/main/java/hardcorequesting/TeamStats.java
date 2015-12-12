package hardcorequesting;


import hardcorequesting.network.*;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class TeamStats {
    private String name;
    private int players;
    private int lives;
    private int progress;


    public TeamStats(String name, int players, int lives, int progress) {
        this.name = name;
        this.players = players;
        this.lives = lives;
        this.progress = progress;
    }

    private static Map<String, TeamStats> clientTeams;
    private static TeamStats[] clientTeamsList;

    public static void save(DataWriter dw) {
        dw.writeData(QuestingData.getTeams().size(), DataBitHelper.TEAMS);
        for (Team team : QuestingData.getTeams()) {
            saveTeam(dw, team);
        }
    }

    private static void saveTeam(DataWriter dw, Team team) {
        dw.writeString(team.getName(), DataBitHelper.NAME_LENGTH);
        dw.writeData(team.getPlayerCount(), DataBitHelper.PLAYERS);
        dw.writeData(team.getSharedLives(), DataBitHelper.TEAM_LIVES);
        int progress = (int) (team.getProgress() * 100);
        dw.writeData(progress, DataBitHelper.TEAM_PROGRESS);
    }

    public static void load(DataReader dr) {
        clientTeams = new HashMap<String, TeamStats>();
        int count = dr.readData(DataBitHelper.TEAMS);
        for (int i = 0; i < count; i++) {
            loadTeam(dr);
        }


        updateTeams();
    }


    private static void loadTeam(DataReader dr) {
        String name = dr.readString(DataBitHelper.NAME_LENGTH);
        int players = dr.readData(DataBitHelper.PLAYERS);
        int lives = dr.readData(DataBitHelper.TEAM_LIVES);
        int progress = dr.readData(DataBitHelper.TEAM_PROGRESS);
        clientTeams.put(name, new TeamStats(name, players, lives, progress));
    }


    public static void refreshTeam(Team team) {
        if (!team.isSingle()) {
            DataWriter dw = PacketHandler.getWriter(PacketId.TEAM_STATS_UPDATE);
            saveTeam(dw, team);
            PacketHandler.sendToAllPlayersWithOpenBook(dw);
        }
    }

    public static void handlePacket(EntityPlayer player, DataReader dr) {
        loadTeam(dr);
        updateTeams();
    }


    private static TeamComparator teamComparator = new TeamComparator();

    private static class TeamComparator implements Comparator<TeamStats> {

        @Override
        public int compare(TeamStats o1, TeamStats o2) {
            return ((Integer) o2.progress).compareTo(o1.progress);
        }
    }

    private static void updateTeams() {
        clientTeamsList = new TeamStats[clientTeams.size()];
        int id = 0;
        for (TeamStats teamStats : clientTeams.values()) {
            clientTeamsList[id++] = teamStats;
        }

        Arrays.sort(clientTeamsList, teamComparator);
    }


    public String getName() {
        return name;
    }

    public int getPlayers() {
        return players;
    }

    public int getLives() {
        return lives;
    }

    public int getProgress() {
        return progress;
    }

    public static TeamStats[] getTeamStats() {
        return clientTeamsList;
    }
}
