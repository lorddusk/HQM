package hardcorequesting.common.network.message;

import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
import hardcorequesting.common.death.DeathStat;
import hardcorequesting.common.death.DeathStatsManager;
import hardcorequesting.common.io.SaveHandler;
import hardcorequesting.common.network.IMessage;
import hardcorequesting.common.network.IMessageHandler;
import hardcorequesting.common.network.PacketContext;
import hardcorequesting.common.quests.QuestLine;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DeathStatsMessage implements IMessage {
    private boolean local;
    private Map<UUID, DeathStat> _deathMap;
    
    public DeathStatsMessage() {
    }
    
    public DeathStatsMessage(boolean local) {
        this.local = local;
        if (local) DeathStatsManager.getInstance().save();
    }
    
    @Override
    public void fromBytes(FriendlyByteBuf buf, PacketContext context) {
        this.local = buf.readBoolean();
        if (this.local) return;
        _deathMap = DeathStatsManager.getInstance().readSimplified(buf);
    }
    
    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(this.local);
        if (local) return;
        DeathStatsManager.getInstance().writeSimplified(buf);
    }
    
    public static class Handler implements IMessageHandler<DeathStatsMessage, IMessage> {
        @Override
        public IMessage onMessage(DeathStatsMessage message, PacketContext ctx) {
            ctx.getTaskQueue().accept(() -> handle(message, ctx));
            return null;
        }
        
        private void handle(DeathStatsMessage message, PacketContext ctx) {
            if (!message.local) {
                if (message._deathMap != null) {
                    List<DeathStat> stats = Lists.newArrayList(message._deathMap.values());
                    QuestLine.getActiveQuestLine().provideTemp(DeathStatsManager.getInstance(), SaveHandler.save(stats, new TypeToken<List<DeathStat>>() {}.getType()));
                }
            }
            DeathStatsManager.getInstance().load();
        }
    }
}
