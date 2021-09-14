package hardcorequesting.common.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.network.IMessage;
import hardcorequesting.common.network.NetworkManager;
import hardcorequesting.common.network.message.OpActionMessage;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestingData;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.quests.task.item.ItemRequirementTask;
import net.minecraft.world.entity.player.Player;

import java.io.IOException;
import java.io.StringWriter;
import java.util.UUID;

public final class OPBookHelper {
    
    private OPBookHelper() {
    }
    
    public static void reverseQuestCompletion(Quest quest, UUID subject) {
        NetworkManager.sendToServer(OpAction.QUEST_COMPLETION.build(quest, null, -1, subject));
    }
    
    public static void reverseTaskCompletion(QuestTask<?> task, UUID subject) {
        NetworkManager.sendToServer(OpAction.TASK_COMPLETION.build(task.getParent(), task, -1, subject));
    }
    
    public static void reverseRequirementCompletion(QuestTask<?> task, int requirement, UUID subject) {
        NetworkManager.sendToServer(OpAction.REQUIREMENT_COMPLETION.build(task.getParent(), task, requirement, subject));
    }
    
    public static void reset(UUID player) {
        NetworkManager.sendToServer(OpAction.RESET.build(null, null, -1, player));
    }
    
    public enum OpAction {
        RESET {
            @Override
            public void process(String data) {
                fromJson(data);
                QuestingDataManager.getInstance().getQuestingData(subject).getTeam().clearProgress();
            }
        },
        QUEST_COMPLETION {
            @Override
            public void process(String data) {
                fromJson(data);
                if (quest != null) {
                    if (quest.isCompleted(subject)) {
                        QuestingDataManager.getInstance().getQuestingData(subject).getTeam().resetProgress(quest);
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
                    if (task.isCompleted(subject)) {
                        task.resetData(subject.getUUID());
                        QuestingDataManager.getInstance().getQuestingData(subject).getTeam().resetCompletion(quest); // automatically reset progress
                    } else {
                        task.completeTask(subject.getUUID());
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
                
                if (task instanceof ItemRequirementTask) {
                    ItemRequirementTask itemTask = (ItemRequirementTask) task;
                    itemTask.switchPartStatus(requirement, subject.getUUID());
                }
            }
        };
        
        private static final String QUEST = "quest";
        private static final String SUBJECT = "subject";
        private static final String TASK = "task";
        private static final String REQUIREMENT = "requirement";
        protected int requirement;
        protected Quest quest;
        protected Player subject;
        protected QuestTask task;
        
        private static String toJson(Quest quest, QuestTask task, int requirement, UUID subject) {
            StringWriter stringWriter = new StringWriter();
            try {
                JsonWriter writer = new JsonWriter(stringWriter);
                writer.beginObject();
                if (quest != null)
                    writer.name(QUEST).value(quest.getQuestId().toString());
                if (subject != null)
                    writer.name(SUBJECT).value(subject.toString());
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
        
        public IMessage build(Quest quest, QuestTask task, int requirement, UUID subject) {
            return new OpActionMessage(this, toJson(quest, task, requirement, subject));
        }
        
        public void process(Player player, String data) {
            if (HardcoreQuestingCore.getServer().getProfilePermissions(player.getGameProfile()) >= 4)
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
