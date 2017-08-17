package hardcorequesting.network.message;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hardcorequesting.HardcoreQuesting;
import hardcorequesting.tileentity.IBlockSync;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class BlockSyncMessage implements IMessage {

    private long pos;
    private int type;
    private String data;

    public BlockSyncMessage() {
    }

    public BlockSyncMessage(TileEntity te, int type, String data) {
        this.pos = te.getPos().toLong();
        this.type = type;
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = buf.readLong();
        this.type = buf.readInt();
        this.data = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.pos);
        buf.writeInt(this.type);
        ByteBufUtils.writeUTF8String(buf, data);
    }

    public static class Handler implements IMessageHandler<BlockSyncMessage, IMessage> {

        @Override
        public IMessage onMessage(BlockSyncMessage message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(BlockSyncMessage message, MessageContext ctx) {
            EntityPlayer player = HardcoreQuesting.proxy.getPlayer(ctx);
            if (player == null) return;
            TileEntity te = player.world.getTileEntity(BlockPos.fromLong(message.pos));
            JsonObject data = new JsonParser().parse(message.data).getAsJsonObject();
            if (te != null && te instanceof IBlockSync)
                ((IBlockSync) te).readData(player, ctx.side == Side.SERVER, message.type, data);
        }
    }
}
