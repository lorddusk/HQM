package hardcorequesting.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import hardcorequesting.commands.CommandHandler;
import hardcorequesting.network.NetworkManager;
import hardcorequesting.network.message.OpActionMessage;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.quests.data.QuestDataTaskItems;
import hardcorequesting.quests.task.QuestTask;
import hardcorequesting.quests.task.QuestTaskItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.UUID;

public final class OPBookHelper {

    private OPBookHelper() {
    }

    public static void reverseQuestCompletion(Quest quest, EntityPlayer subject) {
        NetworkManager.sendToServer(OpAction.QUEST_COMPLETION.build(quest, null, -1, subject));
    }

    public static void reverseTaskCompletion (QuestTask task, EntityPlayer subject) {
        NetworkManager.sendToServer(OpAction.TASK_COMPLETION.build(task.getParent(), task, -1, subject));
    }

    public static void reverseRequirementCompletion (QuestTask task, int requirement, EntityPlayer subject) {
        NetworkManager.sendToServer(OpAction.REQUIREMENT_COMPLETION.build(task.getParent(), task, requirement, subject));
    }

    public static void reset(EntityPlayer player) {
        NetworkManager.sendToServer(OpAction.RESET.build(null, null, -1, player));
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
        },
        TASK_COMPLETION {
            @Override
            public void process(String data) {
                fromJson(data);
                if (quest == null) return;

                if (task != null) {
                    List<QuestTask> task_list = quest.getTasks();
                    if (task.isCompleted(subject)) {
                        task.getData(subject).completed = false;
                        QuestingData.getQuestingData(subject).getTeam().resetProgress(quest); // automatically reset progress
                    } else {
                        task.completeTask(subject.getPersistentID());
                    }
                }
                quest.sendUpdatedDataToTeam(subject);
            }
        },
        REQUIREMENT_COMPLETION {
            @Override
            public void process(String data) {
                fromJson(data);
                if (quest == null || task == null) return;

                if (task instanceof QuestTaskItems) {
                    QuestTaskItems itemTask = (QuestTaskItems) task;
                    QuestTaskItems.ItemRequirement[] requirements = itemTask.getItems();
                    if (requirement >= 0 && requirement < requirements.length) {
                        QuestDataTaskItems qData = (QuestDataTaskItems) task.getData(subject.getUniqueID());
                        if (qData.progress[requirement] == requirements[requirement].required) {
                            qData.progress[requirement] = 0;
                            itemTask.getData(subject.getUniqueID()).completed = false;
                            QuestingData.getQuestingData(subject).getTeam().refreshData();
                        } else {
                            qData.progress[requirement] = requirements[requirement].required;
                            itemTask.doCompletionCheck(qData, subject.getUniqueID());
                        }
                        quest.sendUpdatedDataToTeam(subject);
                    }
                }
            }
        };

        private static final String QUEST = "quest";
        private static final String SUBJECT = "subject";
        private static final String TASK = "task";
        private static final String REQUIREMENT = "requirement";
        protected int requirement;
        protected Quest quest;
        protected EntityPlayer subject;
        protected QuestTask task;

        private static String toJson(Quest quest, QuestTask task, int requirement, EntityPlayer subject) {
            StringWriter stringWriter = new StringWriter();
            try {
                JsonWriter writer = new JsonWriter(stringWriter);
                writer.beginObject();
                if (quest != null)
                    writer.name(QUEST).value(quest.getQuestId().toString());
                if (subject != null)
                    writer.name(SUBJECT).value(subject.getPersistentID().toString());
                if (task != null)
                    writer.name(TASK).value(task.getId());
                if (requirement != -1)
                    writer.name(REQUIREMENT).value(requirement);
                writer.endObject();
                writer.close();
            } catch (IOException ignored) {
            }
            return stringWriter.toString();
        }

        public abstract void process(String data);

        public IMessage build(Quest quest, QuestTask task, int requirement, EntityPlayer subject) {
            return new OpActionMessage(this, toJson(quest, task, requirement, subject));
        }

        public void process(EntityPlayer player, String data) {
            if (CommandHandler.isOwnerOrOp(player))
                process(data);
        }

        protected void fromJson(String data) {
            JsonParser parser = new JsonParser();
            JsonObject root = parser.parse(data).getAsJsonObject();
            if (root.has(QUEST))
                quest = Quest.getQuest(UUID.fromString(root.get(QUEST).getAsString()));
            if (root.has(SUBJECT))
                subject = QuestingData.getPlayer(root.get(SUBJECT).getAsString());
            if (root.has(TASK) && quest != null)
                task = quest.getTasks().get(root.get(TASK).getAsInt());
            if (root.has(REQUIREMENT) && quest != null && task != null)
                requirement = root.get(REQUIREMENT).getAsInt();
        }
    }
}
