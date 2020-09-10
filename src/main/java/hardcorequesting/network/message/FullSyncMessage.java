package hardcorequesting.network.message;

import hardcorequesting.io.SaveHandler;
import hardcorequesting.network.IMessage;
import hardcorequesting.network.IMessageHandler;
import hardcorequesting.quests.QuestLine;
import hardcorequesting.quests.QuestingDataManager;
import hardcorequesting.util.SyncUtil;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.network.FriendlyByteBuf;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class FullSyncMessage implements IMessage {
    
    private boolean local, questing, hardcore, remote;
    private String timestamp, reputations, bags, teams, data, mainDescription;
    private String[] questsSets, questSetNames;
    
    public FullSyncMessage() {
    }
    
    public FullSyncMessage(boolean local, boolean remote) {
        this.local = local;
        this.remote = remote;
//        this.questing = QuestingData.isQuestActive();
//        this.hardcore = QuestingData.isHardcoreActive();
    }
    
    // TODO: this can't be one packet
    public FullSyncMessage(boolean remote) {
//        this.remote = remote;
//        this.mainDesc = QuestLine.getActiveQuestLine().mainDescription;
//        this.questing = QuestingData.isQuestActive();
//        this.hardcore = QuestingData.isHardcoreActive();
//        this.reputations = SaveHandler.saveReputations();
//        this.bags = SaveHandler.saveBags();
//        this.teams = SaveHandler.saveTeams();
//        this.data = SaveHandler.saveQuestingData();
        List<String> names = new ArrayList<>();
        List<String> questSets = new ArrayList<>();
//        this.setOrder = SaveHandler.saveAllQuestSets(names, questSets);
        this.questSetNames = names.toArray(new String[0]);
        this.questsSets = questSets.toArray(new String[0]);
    }
    
    @Override
    public void fromBytes(FriendlyByteBuf buf, PacketContext context) {
        this.local = buf.readBoolean();
        this.remote = buf.readBoolean();
        this.questing = buf.readBoolean();
        this.hardcore = buf.readBoolean();
        if (local) return;
        mainDescription = SyncUtil.readLargeString(buf);
        reputations = SyncUtil.readLargeString(buf);
        bags = SyncUtil.readLargeString(buf);
        teams = SyncUtil.readLargeString(buf);
        data = SyncUtil.readLargeString(buf);
        
        int size = buf.readInt();
        this.questSetNames = new String[size];
        this.questsSets = new String[size];
        for (int i = 0; i < size; i++) {
            questSetNames[i] = buf.readUtf(32767);
            questsSets[i] = buf.readUtf(32767);
        }
        
    }
    
    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(this.local);
        buf.writeBoolean(this.remote);
        buf.writeBoolean(this.questing);
        buf.writeBoolean(this.hardcore);
        if (this.local) return;
        SyncUtil.writeLargeString(mainDescription, buf);
        SyncUtil.writeLargeString(reputations, buf);
        SyncUtil.writeLargeString(bags, buf);
        SyncUtil.writeLargeString(teams, buf);
        SyncUtil.writeLargeString(data, buf);
        
        buf.writeInt(this.questsSets.length);
        for (int i = 0; i < this.questsSets.length; i++) {
            buf.writeUtf(questSetNames[i]);
            buf.writeUtf(questsSets[i]);
        }
    }
    
    public static class Handler implements IMessageHandler<FullSyncMessage, IMessage> {
        
        @Override
        public IMessage onMessage(FullSyncMessage message, PacketContext ctx) {
            ctx.getTaskQueue().execute(() -> handle(message, ctx));
            return null;
        }
        
        private void handle(FullSyncMessage message, PacketContext ctx) {
//            try {
                if (!message.local) {
                    QuestLine questLine = QuestLine.getActiveQuestLine();
                    questLine.provideTemp("description.txt", message.mainDescription);
//                    try (PrintWriter out = new PrintWriter(SaveHandler.getRemoteFile("reputations"))) {
//                        out.print(message.reputations);
//                    }
//                    try (PrintWriter out = new PrintWriter(SaveHandler.getRemoteFile("bags"))) {
//                        out.print(message.bags);
//                    }
//                    try (PrintWriter out = new PrintWriter(SaveHandler.getRemoteFile("teams"))) {
//                        out.print(message.teams);
//                    }
//                    try (PrintWriter out = new PrintWriter(SaveHandler.getRemoteFile("data"))) {
//                        out.print(message.data);
//                    }
//                    try (PrintWriter out = new PrintWriter(SaveHandler.getRemoteFile("state"))) {
//                        out.print(QuestingDataManager.saveQuestingState(message.questing, message.hardcore));
//                    }
//                    try (PrintWriter out = new PrintWriter(SaveHandler.getRemoteFile("sets"))) {
//                        out.print(message.setOrder);
//                    }
//                    SaveHandler.removeQuestSetFiles(SaveHandler.getRemoteFolder());
//                    for (int i = 0; i < message.questsSets.length; i++)
//                        try (PrintWriter out = new PrintWriter(new File(SaveHandler.getRemoteFolder(), message.questSetNames[i]))) {
//                            out.print(message.questsSets[i]);
//                        }
                }
                QuestLine.receiveDataFromServer(ctx.getPlayer(), message.remote);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }
}
