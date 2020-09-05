package hardcorequesting.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import hardcorequesting.client.sounds.SoundHandler;
import hardcorequesting.client.sounds.Sounds;
import hardcorequesting.network.IMessage;
import hardcorequesting.network.message.ClientUpdateMessage;
import hardcorequesting.network.message.SoundMessage;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.quests.task.QuestTask;
import hardcorequesting.tileentity.TrackerBlockEntity;
import hardcorequesting.tileentity.TrackerType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import java.io.IOException;
import java.io.StringWriter;
import java.util.UUID;

@SuppressWarnings("Duplicates")
public enum ClientChange {
    @Deprecated
    SELECT_QUEST(new ClientUpdater<QuestTask>() {
        private static final String PARENT = "parent";
        private static final String TASK = "task";
        
        @Override
        public IMessage build(QuestTask data) throws IOException {
            StringWriter sWriter = new StringWriter();
            JsonWriter writer = new JsonWriter(sWriter);
            writer.beginObject();
            writer.name(PARENT).value(data.getParent().getQuestId().toString());
            writer.name(TASK).value(data.getId());
            writer.endObject();
            writer.close();
            return new ClientUpdateMessage(SELECT_QUEST, sWriter.toString());
        }
        
        @Override
        public void parse(Player player, String data) {
            JsonParser parser = new JsonParser();
            JsonObject root = parser.parse(data).getAsJsonObject();
            QuestingData.getQuestingData(player).selectedQuestId = UUID.fromString(root.get(PARENT).getAsString());
            QuestingData.getQuestingData(player).selectedTask = root.get(TASK).getAsInt();
        }
    }),
    UPDATE_TASK(new ClientUpdater<QuestTask>() {
        private static final String QUEST = "quest";
        private static final String TASK = "task";
        
        @Override
        public IMessage build(QuestTask questTask) throws IOException {
            StringWriter sWriter = new StringWriter();
            JsonWriter writer = new JsonWriter(sWriter);
            writer.beginObject();
            writer.name(QUEST).value(questTask.getParent().getQuestId().toString());
            writer.name(TASK).value(questTask.getId());
            writer.endObject();
            writer.close();
            return new ClientUpdateMessage(UPDATE_TASK, sWriter.toString());
        }
        
        @Override
        public void parse(Player player, String data) {
            JsonParser parser = new JsonParser();
            JsonObject root = parser.parse(data).getAsJsonObject();
            Quest quest = Quest.getQuest(UUID.fromString(root.get(QUEST).getAsString()));
            int task = root.get(TASK).getAsInt();
            if (quest != null && task > -1 && task < quest.getTasks().size())
                quest.getTasks().get(task).onUpdate(player);
        }
    }),
    CLAIM_QUEST(new ClientUpdater<Tuple<UUID, Integer>>() {
        private static final String QUEST = "quest";
        private static final String REWARD = "reward";
        
        @Override
        public IMessage build(Tuple<UUID, Integer> data) throws IOException {
            StringWriter sWriter = new StringWriter();
            JsonWriter writer = new JsonWriter(sWriter);
            writer.beginObject();
            writer.name(QUEST).value(data.getFirst().toString());
            writer.name(REWARD).value(data.getSecond());
            writer.endObject();
            writer.close();
            return new ClientUpdateMessage(CLAIM_QUEST, sWriter.toString());
        }
        
        @Override
        public void parse(Player player, String data) {
            JsonParser parser = new JsonParser();
            JsonObject root = parser.parse(data).getAsJsonObject();
            Quest quest = Quest.getQuest(UUID.fromString(root.get(QUEST).getAsString()));
            if (quest != null)
                quest.claimReward(player, root.get(REWARD).getAsInt());
        }
    }),
    TRACKER_UPDATE(new ClientUpdater<TrackerBlockEntity>() {
        private static final String BLOCK_POS = "blockPos";
        private static final String RADIUS = "radius";
        private static final String TYPE = "trackerType";
        
        @Override
        public IMessage build(TrackerBlockEntity data) throws IOException {
            StringWriter sWriter = new StringWriter();
            JsonWriter writer = new JsonWriter(sWriter);
            writer.beginObject();
            writer.name(BLOCK_POS).value(data.getBlockPos().asLong());
            writer.name(RADIUS).value(data.getRadius());
            writer.name(TYPE).value(data.getTrackerType().ordinal());
            writer.endObject();
            return new ClientUpdateMessage(TRACKER_UPDATE, sWriter.toString());
        }
        
        @Override
        public void parse(Player player, String data) {
            JsonParser parser = new JsonParser();
            JsonObject root = parser.parse(data).getAsJsonObject();
            BlockPos pos = BlockPos.of(root.get(BLOCK_POS).getAsLong());
            int radius = root.get(RADIUS).getAsInt();
            TrackerType type = TrackerType.values()[root.get(TYPE).getAsInt()];
            TrackerBlockEntity.saveToServer(player, pos, radius, type);
        }
    }),
    SOUND(new ClientUpdater<Sounds>() {
        @Override
        public IMessage build(Sounds data) throws IOException {
            return new SoundMessage(SOUND, data.ordinal() + "");
        }
    
        @Override
        public void parse(Player player, String data) {
            SoundHandler.handleSoundPacket(Sounds.values()[Integer.parseInt(data)]);
        }
    }),
    LORE(new ClientUpdater() {
        @Override
        public IMessage build(Object data) throws IOException {
            return new SoundMessage(LORE, "nothing");
        }
        
        @Override
        public void parse(Player player, String data) {
            SoundHandler.handleLorePacket(player);
        }
    });
    
    private ClientUpdater updater;
    
    ClientChange(ClientUpdater updater) {
        this.updater = updater;
    }
    
    public void parse(Player player, String data) {
        updater.parse(player, data);
    }
    
    @SuppressWarnings("unchecked")
    public IMessage build(Object data) {
        try {
            return updater.build(data);
        } catch (IOException e) {
            return null;
        }
    }
    
    public interface ClientUpdater<T> {
        
        IMessage build(T data) throws IOException;
        
        void parse(Player player, String data);
    }
    
    public static class Tuple<A, B> {
        private A first;
        private B second;
        
        public Tuple(A first, B second) {
            this.first = first;
            this.second = second;
        }
        
        public A getFirst() {
            return first;
        }
        
        public B getSecond() {
            return second;
        }
    }
}
