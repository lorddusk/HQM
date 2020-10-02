package hardcorequesting.common.io.adapter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import hardcorequesting.common.bag.GroupData;
import hardcorequesting.common.quests.QuestingData;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.team.Team;
import net.minecraft.util.GsonHelper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class QuestingAdapter {
    
    public static final TypeAdapter<QuestingData> QUESTING_DATA_ADAPTER = new TypeAdapter<QuestingData>() {
        public static final String KEY_TEAM = "team";
        public static final String KEY_LIVES = "lives";
        public static final String KEY_UUID = "uuid";
        public static final String KEY_NAME = "name";
        public static final String KEY_GROUP_DATA = "groupData";
        public static final String KEY_SELECTED_QUEST = "selectedQuest";
        public static final String KEY_PLAYER_LORE = "playedLore";
        public static final String KEY_RECEIVED_BOOK = "receivedBook";
        
        @Override
        public void write(JsonWriter out, QuestingData value) throws IOException {
            out.beginObject();
            out.name(KEY_UUID).value(value.getPlayerId().toString());
            out.name(KEY_NAME).value(value.getName());
            out.name(KEY_LIVES).value(value.getRawLives());
            out.name(KEY_TEAM);
            if (value.getTeam().getId() == -1)
                TeamAdapter.TEAM_ADAPTER.write(out, value.getTeam());
            else
                out.value(value.getTeam().getId());
            out.name(KEY_SELECTED_QUEST).value(value.selectedQuestId != null ? value.selectedQuestId.toString() : null);
            out.name(KEY_PLAYER_LORE).value(value.playedLore);
            out.name(KEY_RECEIVED_BOOK).value(value.receivedBook);
            out.name(KEY_GROUP_DATA);
            out.beginObject();
            for (Map.Entry<UUID, GroupData> entry : value.getGroupData().entrySet())
                if (entry.getKey() != null)
                    out.name(entry.getKey().toString()).value(entry.getValue().retrieved);
            out.endObject();
            out.endObject();
        }
        
        @Override
        public QuestingData read(JsonReader in) {
            JsonObject object = Streams.parse(in).getAsJsonObject();
            String uuid = object.get(KEY_UUID).getAsString();
            int lives = GsonHelper.getAsInt(object, KEY_LIVES, 0);
            int teamId = object.get(KEY_TEAM).isJsonPrimitive() ? object.get(KEY_TEAM).getAsInt() : -1;
            Team team = teamId != -1 ? null : TeamAdapter.TEAM_ADAPTER.fromJsonTree(object.get(KEY_TEAM));
            String selectedQuest = GsonHelper.getAsString(object, KEY_SELECTED_QUEST, null);
            boolean playerLore = GsonHelper.getAsBoolean(object, KEY_PLAYER_LORE, false);
            boolean receivedBook = GsonHelper.getAsBoolean(object, KEY_RECEIVED_BOOK, false);
            Map<UUID, GroupData> data = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : GsonHelper.getAsJsonObject(object, KEY_GROUP_DATA, new JsonObject()).entrySet()) {
                data.put(UUID.fromString(entry.getKey()), new GroupData(entry.getValue().getAsInt()));
            }
            
            QuestingData questingData = new QuestingData(QuestingDataManager.getInstance(), UUID.fromString(uuid), lives, teamId, data);
            questingData.playedLore = playerLore;
            questingData.receivedBook = receivedBook;
            if (selectedQuest != null) {
                questingData.selectedQuestId = UUID.fromString(selectedQuest);
            }
            if (teamId == -1)
                questingData.setTeam(team);
            return questingData;
        }
    };
}