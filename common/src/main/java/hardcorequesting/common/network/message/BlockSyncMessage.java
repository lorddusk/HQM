package hardcorequesting.common.network.message;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.network.IMessage;
import hardcorequesting.common.network.IMessageHandler;
import hardcorequesting.common.network.PacketContext;
import hardcorequesting.common.tileentity.IBlockSync;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BlockSyncMessage implements IMessage {
    
    private long pos;
    private int type;
    private String data;
    
    public BlockSyncMessage() {
    }
    
    public BlockSyncMessage(BlockEntity te, int type, String data) {
        this.pos = te.getBlockPos().asLong();
        this.type = type;
        this.data = data;
    }
    
    @Override
    public void fromBytes(FriendlyByteBuf buf, PacketContext context) {
        this.pos = buf.readLong();
        this.type = buf.readInt();
        this.data = buf.readUtf(32767);
    }
    
    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeLong(this.pos);
        buf.writeInt(this.type);
        buf.writeUtf(data);
    }
    
    public static class Handler implements IMessageHandler<BlockSyncMessage, IMessage> {
        
        @Override
        public IMessage onMessage(BlockSyncMessage message, PacketContext ctx) {
            ctx.getTaskQueue().accept(() -> handle(message, ctx));
            return null;
        }
        
        private void handle(BlockSyncMessage message, PacketContext ctx) {
            Player player = HardcoreQuestingCore.proxy.getPlayer(ctx);
            if (player == null) return;
            BlockEntity te = player.level().getBlockEntity(BlockPos.of(message.pos));
            JsonObject data = new JsonParser().parse(message.data).getAsJsonObject();
            if (te instanceof IBlockSync)
                ((IBlockSync) te).readData(player, !ctx.isClient(), message.type, data);
        }
    }
}
