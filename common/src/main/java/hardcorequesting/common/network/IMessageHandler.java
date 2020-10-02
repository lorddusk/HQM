package hardcorequesting.common.network;

public interface IMessageHandler<T extends IMessage, R extends IMessage> {
    R onMessage(T message, PacketContext context);
}
