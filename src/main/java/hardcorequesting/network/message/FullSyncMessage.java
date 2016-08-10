package hardcorequesting.network.message;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import hardcorequesting.io.SaveHandler;
import hardcorequesting.quests.QuestLine;
import hardcorequesting.quests.QuestingData;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class FullSyncMessage implements IMessage {
    private boolean local, questing, hardcore;
    private String timestamp, reputations, bags, teams, data, setOrder, mainDesc;
    private String[] questsSets, questSetNames;

    public FullSyncMessage() {
    }

    public FullSyncMessage(boolean local) {
        this.local = local;
        this.questing = QuestingData.isQuestActive();
        this.hardcore = QuestingData.isHardcoreActive();
    }

    public FullSyncMessage(String timestamp) {
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
        this.questSetNames = names.toArray(new String[names.size()]);
        this.questsSets = questSets.toArray(new String[questSets.size()]);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.local = buf.readBoolean();
        this.questing = buf.readBoolean();
        this.hardcore = buf.readBoolean();
        if (local) return;
        int size = buf.readInt();
        this.mainDesc = new String(buf.readBytes(size).array());
        size = buf.readInt();
        this.reputations = new String(buf.readBytes(size).array());
        size = buf.readInt();
        this.bags = new String(buf.readBytes(size).array());
        size = buf.readInt();
        this.teams = new String(buf.readBytes(size).array());
        size = buf.readInt();
        this.data = new String(buf.readBytes(size).array());
        size = buf.readInt();
        this.setOrder = new String(buf.readBytes(size).array());
        size = buf.readInt();
        this.questSetNames = new String[size];
        this.questsSets = new String[size];
        for (int i = 0; i < size; i++) {
            int ssize = buf.readInt();
            this.questSetNames[i] = new String(buf.readBytes(ssize).array());
            ssize = buf.readInt();
            this.questsSets[i] = new String(buf.readBytes(ssize).array());
        }

    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.local);
        buf.writeBoolean(this.questing);
        buf.writeBoolean(this.hardcore);
        if (this.local) return;
        buf.writeInt(this.mainDesc.getBytes().length);
        buf.writeBytes(this.mainDesc.getBytes());
        buf.writeInt(this.reputations.getBytes().length);
        buf.writeBytes(this.reputations.getBytes());
        buf.writeInt(this.bags.getBytes().length);
        buf.writeBytes(this.bags.getBytes());
        buf.writeInt(this.teams.getBytes().length);
        buf.writeBytes(this.teams.getBytes());
        buf.writeInt(this.data.getBytes().length);
        buf.writeBytes(this.data.getBytes());
        buf.writeInt(this.setOrder.getBytes().length);
        buf.writeBytes(this.setOrder.getBytes());
        buf.writeInt(this.questsSets.length);
        for (int i = 0; i < this.questsSets.length; i++) {
            buf.writeInt(this.questSetNames[i].getBytes().length);
            buf.writeBytes(this.questSetNames[i].getBytes());
            buf.writeInt(this.questsSets[i].getBytes().length);
            buf.writeBytes(this.questsSets[i].getBytes());
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
                QuestLine.receiveServerSync(message.local);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
