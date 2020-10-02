package hardcorequesting.common.network.message;

import hardcorequesting.common.death.DeathStatsManager;
import hardcorequesting.common.network.IMessage;
import hardcorequesting.common.network.IMessageHandler;
import hardcorequesting.common.network.PacketContext;
import hardcorequesting.common.quests.QuestLine;
import net.minecraft.network.FriendlyByteBuf;

public class DeathStatsMessage implements IMessage {
    
    private boolean local;
    private String deaths;
    
    public DeathStatsMessage() {
    }
    
    public DeathStatsMessage(boolean local) {
        this.local = local;
        if (local) DeathStatsManager.getInstance().save();
        this.deaths = DeathStatsManager.getInstance().saveToString();
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
            ctx.getTaskQueue().accept(() -> handle(message, ctx));
            return null;
        }
        
        private void handle(DeathStatsMessage message, PacketContext ctx) {
            if (!message.local) {
                QuestLine.getActiveQuestLine().provideTemp(DeathStatsManager.getInstance(), message.deaths);
            }
            DeathStatsManager.getInstance().load();
        }
    }
}
