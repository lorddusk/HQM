package hardcorequesting.common.team;

import hardcorequesting.common.network.NetworkManager;
import hardcorequesting.common.network.message.TeamStatsMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TeamLiteStat {
    private static final Logger LOGGER = LogManager.getLogger();
    
    private static final Map<String, TeamLiteStat> clientTeams = new HashMap<>();
    private static TeamLiteStat[] clientTeamsList;
    private static Comparator<TeamLiteStat> teamComparator = Comparator.comparingDouble(TeamLiteStat::getProgress).reversed();
    private String name;
    private int players;
    private int lives;
    private float progress;
    
    public TeamLiteStat(String name, int players, int lives, float progress) {
        this.name = name;
        this.players = players;
        this.lives = lives;
        this.progress = progress;
    }
    
    public static void refreshTeam(Team team) {
        NetworkManager.sendToAllPlayers(new TeamStatsMessage(team));
    }
    
    public static void updateTeams(List<TeamLiteStat> stats) {
        clientTeams.clear();
        for (TeamLiteStat stat : stats) {
            if (stat.getPlayers() > 0)
                clientTeams.put(stat.name, stat);
        }
        updateTeams();
    }
    
    public static void updateTeam(TeamLiteStat stat) {
        if (stat.getPlayers() > 0)
            clientTeams.put(stat.name, stat);
        else
            clientTeams.remove(stat.name);
        updateTeams();
    }
    
    private static void updateTeams() {
        clientTeamsList = new TeamLiteStat[clientTeams.size()];
        int id = 0;
        for (TeamLiteStat teamLiteStat : clientTeams.values())
            clientTeamsList[id++] = teamLiteStat;
        
        Arrays.sort(clientTeamsList, teamComparator);
    }
    
    @NotNull
    public static TeamLiteStat[] getTeamStats() {
        if (clientTeamsList == null) {
            LOGGER.warn("Tried getting client teams list before getting them from the server. The initial packet might have failed to be sent!");
            clientTeamsList = new TeamLiteStat[0];
        }
        
        return clientTeamsList;
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
}
