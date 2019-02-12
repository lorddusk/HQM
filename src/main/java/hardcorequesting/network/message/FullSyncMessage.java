package hardcorequesting.network.message;

import hardcorequesting.io.SaveHandler;
import hardcorequesting.quests.QuestLine;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.util.SyncUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class FullSyncMessage implements IMessage {

    private boolean local, questing, hardcore, serverWorld;
    private String timestamp, reputations, bags, teams, data, setOrder, mainDesc;
    private String[] questsSets, questSetNames;

    public FullSyncMessage() {
    }

    public FullSyncMessage(boolean local, boolean serverWorld) {
        this.local = local;
        this.serverWorld = serverWorld;
        this.questing = QuestingData.isQuestActive();
        this.hardcore = QuestingData.isHardcoreActive();
    }

    // TODO: this can't be one packet
    public FullSyncMessage(boolean serverWorld) {
        this.serverWorld = serverWorld;
        this.mainDesc = QuestLine.getActiveQuestLine().mainDescription;
        this.questing = QuestingData.isQuestActive();
        this.hardcore = QuestingData.isHardcoreActive();
        this.reputations = SaveHandler.saveReputations();
        this.bags = SaveHandler.saveBags();
        this.teams = SaveHandler.saveTeams();
        this.data = SaveHandler.saveQuestingData();
        List<String> names = new ArrayList<>();
        List<String> questSets = new ArrayList<>();
        this.setOrder = SaveHandler.saveAllQuestSets(names, questSets);
        this.questSetNames = names.toArray(new String[0]);
        this.questsSets = questSets.toArray(new String[0]);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.local = buf.readBoolean();
        this.serverWorld = buf.readBoolean();
        this.questing = buf.readBoolean();
        this.hardcore = buf.readBoolean();
        if (local) return;
        mainDesc = SyncUtil.readLargeString(buf);
        reputations = SyncUtil.readLargeString(buf);
        bags = SyncUtil.readLargeString(buf);
        teams = SyncUtil.readLargeString(buf);
        data = SyncUtil.readLargeString(buf);
        setOrder = SyncUtil.readLargeString(buf);

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
        buf.writeBoolean(this.local);
        buf.writeBoolean(this.serverWorld);
        buf.writeBoolean(this.questing);
        buf.writeBoolean(this.hardcore);
        if (this.local) return;
        SyncUtil.writeLargeString(mainDesc, buf);
        SyncUtil.writeLargeString(reputations, buf);
        SyncUtil.writeLargeString(bags, buf);
        SyncUtil.writeLargeString(teams, buf);
        SyncUtil.writeLargeString(data, buf);
        SyncUtil.writeLargeString(setOrder, buf);

        buf.writeInt(this.questsSets.length);
        for (int i = 0; i < this.questsSets.length; i++) {
            ByteBufUtils.writeUTF8String(buf, questSetNames[i]);
            ByteBufUtils.writeUTF8String(buf, questsSets[i]);
        }
    }

    public static class Handler implements IMessageHandler<FullSyncMessage, IMessage> {

        @Override
        public IMessage onMessage(FullSyncMessage message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(FullSyncMessage message, MessageContext ctx) {
            try {
                if (!message.local) {
                    try (PrintWriter out = new PrintWriter(SaveHandler.getRemoteFile("description.txt"))) {
                        out.print(message.mainDesc);
                    }
                    try (PrintWriter out = new PrintWriter(SaveHandler.getRemoteFile("reputations"))) {
                        out.print(message.reputations);
                    }
                    try (PrintWriter out = new PrintWriter(SaveHandler.getRemoteFile("bags"))) {
                        out.print(message.bags);
                    }
                    try (PrintWriter out = new PrintWriter(SaveHandler.getRemoteFile("teams"))) {
                        out.print(message.teams);
                    }
                    try (PrintWriter out = new PrintWriter(SaveHandler.getRemoteFile("data"))) {
                        out.print(message.data);
                    }
                    try (PrintWriter out = new PrintWriter(SaveHandler.getRemoteFile("state"))) {
                        out.print(SaveHandler.saveQuestingState(message.questing, message.hardcore));
                    }
                    try (PrintWriter out = new PrintWriter(SaveHandler.getRemoteFile("sets"))) {
                        out.print(message.setOrder);
                    }
                    SaveHandler.removeQuestSetFiles(SaveHandler.getRemoteFolder());
                    for (int i = 0; i < message.questsSets.length; i++)
                        try (PrintWriter out = new PrintWriter(new File(SaveHandler.getRemoteFolder(), message.questSetNames[i]))) {
                            out.print(message.questsSets[i]);
                        }
                }
                QuestLine.receiveServerSync(Minecraft.getMinecraft().player, message.local, message.serverWorld);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
