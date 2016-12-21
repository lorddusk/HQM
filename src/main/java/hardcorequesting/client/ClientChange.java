package hardcorequesting.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import hardcorequesting.client.sounds.SoundHandler;
import hardcorequesting.client.sounds.Sounds;
import hardcorequesting.network.message.ClientUpdateMessage;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.quests.task.QuestTask;
import hardcorequesting.tileentity.TileEntityTracker;
import hardcorequesting.tileentity.TrackerType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.io.IOException;
import java.io.StringWriter;

@SuppressWarnings("Duplicates")
public enum ClientChange {
    SELECT_QUEST(new ClientUpdater<QuestTask>() {
        private static final String PARENT = "parent";
        private static final String TASK = "task";

        @Override
        public IMessage build(QuestTask data) throws IOException {
            StringWriter sWriter = new StringWriter();
            JsonWriter writer = new JsonWriter(sWriter);
            writer.beginObject();
            writer.name(PARENT).value(data.getParent().getId());
            writer.name(TASK).value(data.getId());
            writer.endObject();
            writer.close();
            return new ClientUpdateMessage(SELECT_QUEST, sWriter.toString());
        }

        @Override
        public void parse(EntityPlayer player, String data) {
            JsonParser parser = new JsonParser();
            JsonObject root = parser.parse(data).getAsJsonObject();
            QuestingData.getQuestingData(player).selectedQuest = root.get(PARENT).getAsString();
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
            writer.name(QUEST).value(questTask.getParent().getId());
            writer.name(TASK).value(questTask.getId());
            writer.endObject();
            writer.close();
            return new ClientUpdateMessage(UPDATE_TASK, sWriter.toString());
        }

        @Override
        public void parse(EntityPlayer player, String data) {
            JsonParser parser = new JsonParser();
            JsonObject root = parser.parse(data).getAsJsonObject();
            Quest quest = Quest.getQuest(root.get(QUEST).getAsString());
            int task = root.get(TASK).getAsInt();
            if (quest != null && task > -1 && task < quest.getTasks().size())
                quest.getTasks().get(task).onUpdate(player);
        }
    }),
    CLAIM_QUEST(new ClientUpdater<Tuple<String, Integer>>() {
        private static final String QUEST = "quest";
        private static final String REWARD = "reward";

        @Override
        public IMessage build(Tuple<String, Integer> data) throws IOException {
            StringWriter sWriter = new StringWriter();
            JsonWriter writer = new JsonWriter(sWriter);
            writer.beginObject();
            writer.name(QUEST).value(data.getFirst());
            writer.name(REWARD).value(data.getSecond());
            writer.endObject();
            writer.close();
            return new ClientUpdateMessage(CLAIM_QUEST, sWriter.toString());
        }

        @Override
        public void parse(EntityPlayer player, String data) {
            JsonParser parser = new JsonParser();
            JsonObject root = parser.parse(data).getAsJsonObject();
            Quest quest = Quest.getQuest(root.get(QUEST).getAsString());
            if (quest != null)
                quest.claimReward(player, root.get(REWARD).getAsInt());
        }
    }),
    TRACKER_UPDATE(new ClientUpdater<TileEntityTracker>() {
        private static final String BLOCK_POS = "blockPos";
        private static final String RADIUS = "radius";
        private static final String TYPE = "trackerType";

        @Override
        public IMessage build(TileEntityTracker data) throws IOException {
            StringWriter sWriter = new StringWriter();
            JsonWriter writer = new JsonWriter(sWriter);
            writer.beginObject();
            writer.name(BLOCK_POS).value(data.getPos().toLong());
            writer.name(RADIUS).value(data.getRadius());
            writer.name(TYPE).value(data.getType().ordinal());
            writer.endObject();
            return new ClientUpdateMessage(TRACKER_UPDATE, sWriter.toString());
        }

        @Override
        public void parse(EntityPlayer player, String data) {
            JsonParser parser = new JsonParser();
            JsonObject root = parser.parse(data).getAsJsonObject();
            BlockPos pos = BlockPos.fromLong(root.get(BLOCK_POS).getAsLong());
            int radius = root.get(RADIUS).getAsInt();
            TrackerType type = TrackerType.values()[root.get(TYPE).getAsInt()];
            TileEntityTracker.saveToServer(player, pos, radius, type);
        }
    }),
    SOUND(new ClientUpdater<Sounds>() {
        @Override
        public IMessage build(Sounds data) throws IOException {
            return new ClientUpdateMessage(SOUND, data.ordinal() + "");
        }

        @Override
        public void parse(EntityPlayer player, String data) {
            SoundHandler.handleSoundPacket(Sounds.values()[Integer.parseInt(data)]);
        }
    }),
    LORE(new ClientUpdater() {
        @Override
        public IMessage build(Object data) throws IOException {
            return new ClientUpdateMessage(LORE, "nothing");
        }

        @Override
        public void parse(EntityPlayer player, String data) {
            SoundHandler.handleLorePacket(player);
        }
    });

    private ClientUpdater updater;

    ClientChange(ClientUpdater updater) {
        this.updater = updater;
    }

    public void parse(EntityPlayer player, String data) {
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

        void parse(EntityPlayer player, String data);
    }
}
