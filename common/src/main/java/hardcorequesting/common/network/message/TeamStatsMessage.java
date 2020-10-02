package hardcorequesting.common.network.message;

import hardcorequesting.common.network.IMessage;
import hardcorequesting.common.network.IMessageHandler;
import hardcorequesting.common.network.PacketContext;
import hardcorequesting.common.team.Team;
import hardcorequesting.common.team.TeamLiteStat;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TeamStatsMessage implements IMessage {
    
    private List<TeamLiteStat> stats;
    
    public TeamStatsMessage() {
    }
    
    public TeamStatsMessage(Team team) {
        stats = Collections.singletonList(team.toLiteStat());
    }
    
    public TeamStatsMessage(List<Team> teams) {
        stats = teams.stream().map(Team::toLiteStat).collect(Collectors.toList());
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
            stats.add(new TeamLiteStat(name, players, lives, progress));
        }
    }
    
    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(stats.size());
        for (TeamLiteStat teamLiteStat : stats) {
            if (teamLiteStat.getName() != null) {
                buf.writeUtf(teamLiteStat.getName());
            } else
                buf.writeUtf("NULL");
            buf.writeInt(teamLiteStat.getPlayers());
            buf.writeInt(teamLiteStat.getLives());
            buf.writeFloat(teamLiteStat.getProgress());
        }
    }
    
    public static class Handler implements IMessageHandler<TeamStatsMessage, IMessage> {
        
        @Override
        public IMessage onMessage(TeamStatsMessage message, PacketContext ctx) {
            ctx.getTaskQueue().accept(() -> handle(message, ctx));
            return null;
        }
        
        private void handle(TeamStatsMessage message, PacketContext ctx) {
            if (message.stats.size() == 1)
                TeamLiteStat.updateTeam(message.stats.get(0));
            else
                TeamLiteStat.updateTeams(message.stats);
        }
    }
}
