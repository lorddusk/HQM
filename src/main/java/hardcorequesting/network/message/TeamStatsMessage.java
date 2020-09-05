package hardcorequesting.network.message;

import hardcorequesting.network.IMessage;
import hardcorequesting.network.IMessageHandler;
import hardcorequesting.team.Team;
import hardcorequesting.team.TeamStats;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TeamStatsMessage implements IMessage {
    
    private List<TeamStats> stats;
    
    public TeamStatsMessage() {
    }
    
    public TeamStatsMessage(Team team) {
        stats = new ArrayList<>();
        stats.add(team.toStat());
    }
    
    public TeamStatsMessage(List<Team> teams) {
        stats = teams.stream().map(Team::toStat).collect(Collectors.toList());
    }
    
    @Override
    public void fromBytes(FriendlyByteBuf buf, PacketContext context) {
        int size = buf.readInt();
        stats = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            String name = buf.readUtf(32767);
            if (name.equals("NULL"))
                name = null;
            
            int players = buf.readInt();
            int lives = buf.readInt();
            float progress = buf.readFloat();
            stats.add(new TeamStats(name, players, lives, progress));
        }
    }
    
    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(stats.size());
        for (TeamStats teamStats : stats) {
            if (teamStats.getName() != null) {
                buf.writeUtf(teamStats.getName());
            } else
                buf.writeUtf("NULL");
            buf.writeInt(teamStats.getPlayers());
            buf.writeInt(teamStats.getLives());
            buf.writeFloat(teamStats.getProgress());
        }
    }
    
    public static class Handler implements IMessageHandler<TeamStatsMessage, IMessage> {
        
        @Override
        public IMessage onMessage(TeamStatsMessage message, PacketContext ctx) {
            ctx.getTaskQueue().execute(() -> handle(message, ctx));
            return null;
        }
        
        private void handle(TeamStatsMessage message, PacketContext ctx) {
            if (message.stats.size() == 1)
                TeamStats.updateTeam(message.stats.get(0));
            else
                TeamStats.updateTeams(message.stats);
        }
    }
}
