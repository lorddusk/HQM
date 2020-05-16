package hardcorequesting.network.message;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hardcorequesting.HardcoreQuesting;
import hardcorequesting.network.IMessage;
import hardcorequesting.network.IMessageHandler;
import hardcorequesting.tileentity.IBlockSync;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

public class BlockSyncMessageClient implements IMessage {
    
    private long pos;
    private int type;
    private String data;
    
    public BlockSyncMessageClient() {
    }
    
    public BlockSyncMessageClient(BlockEntity te, int type, String data) {
        this.pos = te.getPos().asLong();
        this.type = type;
        this.data = data;
    }
    
    @Override
    public void fromBytes(PacketByteBuf buf, PacketContext context) {
        this.pos = buf.readLong();
        this.type = buf.readInt();
        this.data = buf.readString(32767);
    }
    
    @Override
    public void toBytes(PacketByteBuf buf) {
        buf.writeLong(this.pos);
        buf.writeInt(this.type);
        buf.writeString(this.data);
    }
    
    public static class Handler implements IMessageHandler<BlockSyncMessageClient, IMessage> {
        
        @Override
        public IMessage onMessage(BlockSyncMessageClient message, PacketContext ctx) {
            ctx.getTaskQueue().execute(() -> handle(message, ctx));
            return null;
        }
        
        private void handle(BlockSyncMessageClient message, PacketContext ctx) {
            PlayerEntity player = HardcoreQuesting.proxy.getPlayer(ctx);
            if (player == null) return;
            BlockEntity te = player.world.getBlockEntity(BlockPos.fromLong(message.pos));
            JsonObject data = new JsonParser().parse(message.data).getAsJsonObject();
            if (te instanceof IBlockSync)
                ((IBlockSync) te).readData(player, ctx.getPacketEnvironment() == EnvType.SERVER, message.type, data);
        }
    }
}
