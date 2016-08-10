package hardcorequesting.network.message;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import hardcorequesting.team.Team;
import hardcorequesting.team.TeamStats;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;

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
    public void fromBytes(ByteBuf buf) {
        int size = buf.readInt();
        stats = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            int ssize = buf.readInt();
            String name = null;
            if (ssize != -1)
                name = new String(buf.readBytes(ssize).array());
            int players = buf.readInt();
            int lives = buf.readInt();
            float progress = buf.readFloat();
            stats.add(new TeamStats(name, players, lives, progress));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(stats.size());
        for (TeamStats teamStats : stats) {
            if (teamStats.getName() != null) {
                buf.writeInt(teamStats.getName().getBytes().length);
                buf.writeBytes(teamStats.getName().getBytes());
            } else buf.writeInt(-1);
            buf.writeInt(teamStats.getPlayers());
            buf.writeInt(teamStats.getLives());
            buf.writeFloat(teamStats.getProgress());
        }
    }

    public static class Handler implements IMessageHandler<TeamStatsMessage, IMessage> {
        @Override
        public IMessage onMessage(TeamStatsMessage message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(TeamStatsMessage message, MessageContext ctx) {
            if (message.stats.size() == 1)
                TeamStats.updateTeam(message.stats.get(0));
            else
                TeamStats.updateTeams(message.stats);
        }
    }
}
