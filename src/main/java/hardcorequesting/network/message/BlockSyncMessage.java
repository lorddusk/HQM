package hardcorequesting.network.message;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import hardcorequesting.HardcoreQuesting;
import hardcorequesting.tileentity.IBlockSync;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;

public class BlockSyncMessage implements IMessage {
//    private long pos;
    private int x;
    private int y;
    private int z;
    private int type;
    private String data;

    public BlockSyncMessage() {
    }

    public BlockSyncMessage(TileEntity te, int type, String data) {
        this.x = te.xCoord;
        this.y = te.yCoord;
        this.z = te.zCoord;
        this.type = type;
        this.data = data;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.type = buf.readInt();
        int size = buf.readInt();
        this.data = new String(buf.readBytes(size).array());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
        buf.writeInt(this.type);
        buf.writeInt(this.data.getBytes().length);
        buf.writeBytes(this.data.getBytes());
    }

    public static class Handler implements IMessageHandler<BlockSyncMessage, IMessage> {
        @Override
        public IMessage onMessage(BlockSyncMessage message, MessageContext ctx) {
//            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            handle(message, ctx);
            return null;
        }

        private void handle(BlockSyncMessage message, MessageContext ctx) {
            EntityPlayer player = HardcoreQuesting.proxy.getPlayer(ctx);
            if (player == null) return;
            TileEntity te = player.worldObj.getTileEntity(message.x, message.y, message.z);
            JsonObject data = new JsonParser().parse(message.data).getAsJsonObject();
            if (te != null && te instanceof IBlockSync)
                ((IBlockSync) te).readData(player, ctx.side == Side.SERVER, message.type, data);
        }
    }
}
