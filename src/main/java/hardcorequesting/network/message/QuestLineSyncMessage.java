package hardcorequesting.network.message;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import hardcorequesting.io.SaveHandler;
import hardcorequesting.quests.QuestLine;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class QuestLineSyncMessage implements IMessage {

    private String reputations, bags, setOrder, mainDesc;
    private String[] questsSets, questSetNames;

    public QuestLineSyncMessage() {
        this.mainDesc = QuestLine.getActiveQuestLine().mainDescription;
        this.reputations = SaveHandler.saveReputations();
        this.bags = SaveHandler.saveBags();
        List<String> names = new ArrayList<>();
        List<String> questSets = new ArrayList<>();
        this.setOrder = SaveHandler.saveAllQuestSets(names, questSets);
        this.questSetNames = names.toArray(new String[names.size()]);
        this.questsSets = questSets.toArray(new String[questSets.size()]);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        mainDesc = ByteBufUtils.readUTF8String(buf);
        reputations = ByteBufUtils.readUTF8String(buf);
        bags = ByteBufUtils.readUTF8String(buf);
        setOrder = ByteBufUtils.readUTF8String(buf);

        int size = buf.readInt();
        this.questSetNames = new String[size];
        this.questsSets = new String[size];
        for (int i = 0; i < size; i++) {
            questSetNames[i] = ByteBufUtils.readUTF8String(buf);
            questsSets[i] = ByteBufUtils.readUTF8String(buf);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, mainDesc);
        ByteBufUtils.writeUTF8String(buf, reputations);
        ByteBufUtils.writeUTF8String(buf, bags);
        ByteBufUtils.writeUTF8String(buf, setOrder);
        
        buf.writeInt(this.questsSets.length);
        for (int i = 0; i < this.questsSets.length; i++) {
            ByteBufUtils.writeUTF8String(buf, questSetNames[i]);
            ByteBufUtils.writeUTF8String(buf, questsSets[i]);
        }
    }

    public static class Handler implements IMessageHandler<QuestLineSyncMessage, IMessage> {

        @Override
        public IMessage onMessage(QuestLineSyncMessage message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(QuestLineSyncMessage message, MessageContext ctx) {
            try {
                try (PrintWriter out = new PrintWriter(SaveHandler.getRemoteFile("description.txt"))) {
                    out.print(message.mainDesc);
                }
                try (PrintWriter out = new PrintWriter(SaveHandler.getRemoteFile("reputations"))) {
                    out.print(message.reputations);
                }
                try (PrintWriter out = new PrintWriter(SaveHandler.getRemoteFile("bags"))) {
                    out.print(message.bags);
                }
                try (PrintWriter out = new PrintWriter(SaveHandler.getRemoteFile("sets"))) {
                    out.print(message.setOrder);
                }
                SaveHandler.removeQuestSetFiles(SaveHandler.getRemoteFolder());
                for (int i = 0; i < message.questsSets.length; i++) {
                    try (PrintWriter out = new PrintWriter(new File(SaveHandler.getRemoteFolder(), message.questSetNames[i]))) {
                        out.print(message.questsSets[i]);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
