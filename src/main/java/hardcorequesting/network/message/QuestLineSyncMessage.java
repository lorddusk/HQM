package hardcorequesting.network.message;

import hardcorequesting.io.SaveHandler;
import hardcorequesting.network.IMessage;
import hardcorequesting.network.IMessageHandler;
import hardcorequesting.quests.QuestLine;
import hardcorequesting.util.SyncUtil;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.network.PacketByteBuf;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

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
        this.questSetNames = names.toArray(new String[0]);
        this.questsSets = questSets.toArray(new String[0]);
    }
    
    @Override
    public void fromBytes(PacketByteBuf buf, PacketContext context) {
        mainDesc = SyncUtil.readLargeString(buf);
        reputations = SyncUtil.readLargeString(buf);
        bags = SyncUtil.readLargeString(buf);
        setOrder = SyncUtil.readLargeString(buf);
        
        int size = buf.readInt();
        this.questSetNames = new String[size];
        this.questsSets = new String[size];
        for (int i = 0; i < size; i++) {
            questSetNames[i] = SyncUtil.readLargeString(buf);
            questsSets[i] = SyncUtil.readLargeString(buf);
        }
    }
    
    @Override
    public void toBytes(PacketByteBuf buf) {
        SyncUtil.writeLargeString(mainDesc, buf);
        SyncUtil.writeLargeString(reputations, buf);
        SyncUtil.writeLargeString(bags, buf);
        SyncUtil.writeLargeString(setOrder, buf);
        
        buf.writeInt(this.questsSets.length);
        for (int i = 0; i < this.questsSets.length; i++) {
            SyncUtil.writeLargeString(questSetNames[i], buf);
            SyncUtil.writeLargeString(questsSets[i], buf);
        }
    }
    
    public static class Handler implements IMessageHandler<QuestLineSyncMessage, IMessage> {
        
        @Override
        public IMessage onMessage(QuestLineSyncMessage message, PacketContext ctx) {
            ctx.getTaskQueue().execute(() -> handle(message, ctx));
            return null;
        }
        
        private void handle(QuestLineSyncMessage message, PacketContext ctx) {
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
