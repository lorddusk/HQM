package hardcorequesting.network;

import net.fabricmc.fabric.api.network.PacketContext;

public interface IMessageHandler<T extends IMessage, R extends IMessage> {
    R onMessage(T message, PacketContext context);
}
