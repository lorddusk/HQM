package hardcorequesting.fabric.test;

import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.team.PlayerEntry;
import hardcorequesting.common.team.RewardSetting;
import hardcorequesting.common.team.Team;
import hardcorequesting.common.team.TeamAction;
import hardcorequesting.common.team.TeamManager;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Helpers to set up players and teams.
 */
public final class TestTeams {
    private TestTeams() {
    }
    
    /**
     * A new player that is part of the server, with quest data. All mock players have the same name.
     */
    public static ServerPlayer player(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        QuestingDataManager.getInstance().getQuestingData(player);
        return player;
    }
    
    public static Team teamOf(ServerPlayer player) {
        return QuestingDataManager.getInstance().getQuestingData(player).getTeam();
    }
    
    /**
     * Creates a team with the given members, as if it was created by the owner and the others accepted the invitation.
     * The members have to be in the quest line of the test already.
     */
    public static Team squad(String name, RewardSetting setting, UUID owner, UUID... members) {
        Team squad = Team.single(owner);
        squad.setId(UUID.nameUUIDFromBytes(("hqm-test-team:" + name).getBytes()));
        TeamManager.getInstance().addTeam(squad);
        squad.setName(name);
        squad.setRewardSetting(setting);
        QuestingDataManager.getInstance().getQuestingData(owner).setTeam(squad);
        for (UUID member : members) {
            squad.addPlayer(new PlayerEntry(member, true, false));
            QuestingDataManager.getInstance().getQuestingData(member).setTeam(squad);
        }
        squad.clearProgress();   // makes room for all members in the rewards of the quests
        return squad;
    }
    
    /**
     * Invites the player to the team of the owner, the way the invite button does. The server finds the invited player by name,
     * but all mock players have the same name, so only the invited player is kept in the player list while inviting.
     */
    public static void invite(ServerPlayer owner, ServerPlayer invited) {
        PlayerList list = owner.server.getPlayerList();
        List<ServerPlayer> all = new ArrayList<>(list.getPlayers());
        list.getPlayers().removeIf(player -> player != invited);
        try {
            TeamAction.INVITE.process(owner, invited.getName().getString());
        } finally {
            list.getPlayers().clear();
            list.getPlayers().addAll(all);
        }
    }
}
