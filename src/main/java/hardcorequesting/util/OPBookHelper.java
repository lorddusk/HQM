package hardcorequesting.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import hardcorequesting.commands.CommandHandler;
import hardcorequesting.network.NetworkManager;
import hardcorequesting.network.message.OpActionMessage;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestingData;
import net.minecraft.entity.player.EntityPlayer;

import java.io.IOException;
import java.io.StringWriter;

public final class OPBookHelper {
    private OPBookHelper() {
    }

    public static void reverseQuestCompletion(Quest quest, EntityPlayer subject) {
        NetworkManager.sendToServer(OpAction.QUEST_COMPLETION.build(quest, subject));
    }

    public enum OpAction {
        RESET {
            @Override
            public void process(String data) {
                fromJson(data);
                QuestingData.getQuestingData(subject).getTeam().clearProgress();
            }
        },
        QUEST_COMPLETION {
            @Override
            public void process(String data) {
                fromJson(data);
                if (quest != null) {
                    if (quest.isCompleted(subject)) {
                        QuestingData.getQuestingData(subject).getTeam().resetProgress(quest);
                    } else {
                        quest.completeQuest(subject);
                    }
                    quest.sendUpdatedDataToTeam(subject);
                }
            }
        };

        public abstract void process(String data);

        public IMessage build(Quest quest, EntityPlayer subject) {
            return new OpActionMessage(this, toJson(quest, subject));
        }

        public void process(EntityPlayer player, String data) {
            if (CommandHandler.isOwnerOrOp(player))
                process(data);
        }

        private static final String QUEST = "quest";
        private static final String SUBJECT = "subject";

        protected Quest quest;
        protected EntityPlayer subject;

        private static String toJson(Quest quest, EntityPlayer subject) {
            StringWriter stringWriter = new StringWriter();
            try {
                JsonWriter writer = new JsonWriter(stringWriter);
                writer.beginObject();
                if (quest != null)
                    writer.name(QUEST).value(quest.getId());
                if (subject != null)
                    writer.name(SUBJECT).value(subject.getPersistentID().toString());
                writer.endObject();
                writer.close();
            } catch (IOException ignored) {
            }
            return stringWriter.toString();
        }

        protected void fromJson(String data) {
            JsonParser parser = new JsonParser();
            JsonObject root = parser.parse(data).getAsJsonObject();
            if (root.has(QUEST))
                quest = Quest.getQuest(root.get(QUEST).getAsString());
            if (root.has(SUBJECT))
                subject = QuestingData.getPlayer(root.get(SUBJECT).getAsString());
        }
    }

    public static void reset(EntityPlayer player) {
        NetworkManager.sendToServer(OpAction.RESET.build(null, player));
    }
}
