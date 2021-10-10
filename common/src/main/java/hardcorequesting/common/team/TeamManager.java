package hardcorequesting.common.team;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.io.SaveHandler;
import hardcorequesting.common.io.adapter.TeamAdapter;
import hardcorequesting.common.quests.QuestLine;
import hardcorequesting.common.quests.QuestingData;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.quests.SimpleSerializable;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class TeamManager extends SimpleSerializable {
    public static final String FILE_PATH = "teams.json";
    
    private final Set<Team> teams = Sets.newConcurrentHashSet();
    
    public TeamManager(QuestLine parent) {
        super(parent);
    }
    
    public static TeamManager getInstance() {
        return QuestLine.getActiveQuestLine().teamManager;
    }
    
    @Override
    public String filePath() {
        return FILE_PATH;
    }
    
    @Override
    public boolean isData() {
        return true;
    }
    
    @Override
    public void loadFromString(Optional<String> string) {
        teams.clear();
        TeamAdapter.clearInvitesMap();
        List<Team> teams = string
                .flatMap(s -> SaveHandler.<List<Team>>load(s, new TypeToken<List<Team>>() {}.getType()))
                .orElseGet(Lists::newArrayList);
        teams.stream().filter(team -> !team.isSingle()).forEach(TeamManager.getInstance()::addTeam);
        TeamAdapter.commitInvitesMap();
        if (HardcoreQuestingCore.platform.isClient())
            TeamLiteStat.updateTeams(teams.stream().map(Team::toLiteStat).collect(Collectors.toList()));
    }
    
    @Override
    public String saveToString() {
        return SaveHandler.save(teams, new TypeToken<Collection<Team>>() {}.getType());
    }
    
    public String saveToString(Player player) {
        Team team = QuestingDataManager.getInstance().getQuestingData(player).getTeam();
        if (team.isSingle()) return "[]";
        return "[" + SaveHandler.save(team, Team.class) + "]";
    }
    
    @Nullable
    public Team getByTeamId(UUID uuid) {
        for (Team team : teams) {
            if (team.getId().equals(uuid))
                return team;
        }
        
        return null;
    }
    
    @NotNull
    public Team getByPlayer(UUID playerId) {
        for (Team team : teams) {
            for (PlayerEntry player : team.getPlayers()) {
                if (player.getUUID().equals(playerId))
                    return team;
            }
        }
        
        return Team.single(playerId);
    }
    
    public Iterable<Team> getNamedTeams() {
        return teams;
    }
    
    public Iterable<Team> getTeams() {
        //Collect any teams that are not managed by the TeamManager
        Iterable<Team> singleTeams = QuestingDataManager.getInstance().getQuestingData().values().stream().map(QuestingData::getTeam)
                .filter(Team::isSingle).collect(Collectors.toSet());
        return Iterables.concat(teams, singleTeams);
    }
    
    public void deactivate() {
        this.teams.clear();
    }
    
    public void addTeam(Team team) {
        if (team.isSingle())
            throw new IllegalArgumentException("Single teams can not be added!");
        this.teams.add(team);
    }
    
    public void removeTeam(Team team) {
        if (team == null) return;
        if (team.isSingle())
            throw new IllegalArgumentException("Single teams can not be deleted!");
        this.teams.removeIf(t -> t.getId().equals(team.getId()));
    }
}
