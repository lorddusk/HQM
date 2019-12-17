package hqm.quest;

import java.util.List;

/**
 * @author canitzp
 */
public class TeamList {

    private final List<Team> teams;

    public TeamList(List<Team> teams) {
        this.teams = teams;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public TeamList addTeam(Team team){
        this.teams.add(team);
        return this;
    }
}
