package hardcorequesting.team;

import hardcorequesting.network.NetworkManager;
import hardcorequesting.network.message.TeamStatsMessage;

import java.util.*;

public class TeamStats {
    private String name;
    private int players;
    private int lives;
    private float progress;

    public TeamStats(String name, int players, int lives, float progress) {
        this.name = name;
        this.players = players;
        this.lives = lives;
        this.progress = progress;
    }

    private static Map<String, TeamStats> clientTeams;
    private static TeamStats[] clientTeamsList;

    private static TeamComparator teamComparator = new TeamComparator();

    public static void refreshTeam(Team team) {
        NetworkManager.sendToAllPlayers(new TeamStatsMessage(team));
    }

    private static class TeamComparator implements Comparator<TeamStats> {
        @Override
        public int compare(TeamStats o1, TeamStats o2) {
            return ((Float) o2.progress).compareTo(o1.progress);
        }
    }

    public static void updateTeams(List<TeamStats> stats) {
        clientTeams = new HashMap<>();
        for (TeamStats stat : stats) {
            if (stat.getPlayers() > 0)
                clientTeams.put(stat.name, stat);
        }
        updateTeams();
    }

    public static void updateTeam(TeamStats stat) {
        if (clientTeams == null) clientTeams = new HashMap<>();
        if (stat.getPlayers() > 0)
            clientTeams.put(stat.name, stat);
        else
            clientTeams.remove(stat.name);
        updateTeams();
    }

    private static void updateTeams() {
        clientTeamsList = new TeamStats[clientTeams.size()];
        int id = 0;
        for (TeamStats teamStats : clientTeams.values())
            clientTeamsList[id++] = teamStats;

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

    public float getProgress() {
        return progress;
    }

    public static TeamStats[] getTeamStats() {
        return clientTeamsList;
    }
}
