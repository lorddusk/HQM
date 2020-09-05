package hardcorequesting.network.message;

import hardcorequesting.death.DeathStats;
import hardcorequesting.io.SaveHandler;
import hardcorequesting.network.IMessage;
import hardcorequesting.network.IMessageHandler;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.network.FriendlyByteBuf;

import java.io.IOException;
import java.io.PrintWriter;

public class DeathStatsMessage implements IMessage {
    
    private boolean local;
    private String deaths;
    
    public DeathStatsMessage() {
    }
    
    public DeathStatsMessage(boolean local) {
        this.local = local;
        if (local) DeathStats.saveAll();
        this.deaths = SaveHandler.saveDeaths();
    }
    
    @Override
    public void fromBytes(FriendlyByteBuf buf, PacketContext context) {
        this.local = buf.readBoolean();
        if (this.local) return;
        deaths = buf.readUtf(32767);
    }
    
    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(this.local);
        if (local) return;
        buf.writeUtf(deaths);
    }
    
    public static class Handler implements IMessageHandler<DeathStatsMessage, IMessage> {
        
        @Override
        public IMessage onMessage(DeathStatsMessage message, PacketContext ctx) {
            ctx.getTaskQueue().execute(() -> handle(message, ctx));
            return null;
        }
        
        private void handle(DeathStatsMessage message, PacketContext ctx) {
            if (!message.local) {
                try (PrintWriter out = new PrintWriter(SaveHandler.getRemoteFile("deaths"))) {
                    out.print(message.deaths);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            DeathStats.loadAll(true, !message.local);
        }
    }
}
