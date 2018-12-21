package hardcorequesting.io.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import hardcorequesting.bag.GroupData;
import hardcorequesting.death.DeathStats;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.team.Team;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class QuestingAdapter{
    
    public static final TypeAdapter<QuestingData> QUESTING_DATA_ADAPTER = new TypeAdapter<QuestingData>(){
        public static final String KEY_TEAM = "team";
        public static final String KEY_LIVES = "lives";
        public static final String KEY_UUID = "uuid";
        public static final String KEY_NAME = "name";
        public static final String KEY_GROUP_DATA = "groupData";
        public static final String KEY_SELECTED_QUEST = "selectedQuest";
        public static final String KEY_PLAYER_LORE = "playedLore";
        public static final String KEY_RECEIVED_BOOK = "receivedBook";
        public static final String KEY_DEATHS = "deaths";
        
        @Override
        public void write(JsonWriter out, QuestingData value) throws IOException{
            out.beginObject();
            out.name(KEY_UUID).value(value.getPlayerId().toString());
            out.name(KEY_NAME).value(value.getName());
            out.name(KEY_LIVES).value(value.getRawLives());
            out.name(KEY_TEAM);
            if(value.getTeam().getId() == -1)
                TeamAdapter.TEAM_ADAPTER.write(out, value.getTeam());
            else
                out.value(value.getTeam().getId());
            out.name(KEY_SELECTED_QUEST).value(value.selectedQuestId != null ? value.selectedQuestId.toString() : null);
            out.name(KEY_PLAYER_LORE).value(value.playedLore);
            out.name(KEY_RECEIVED_BOOK).value(value.receivedBook);
            out.name(KEY_GROUP_DATA).beginObject();
            for(Map.Entry<UUID, GroupData> entry : value.getGroupData().entrySet())
                if(entry.getKey() != null)
                    out.name(entry.getKey().toString()).value(entry.getValue().retrieved);
            out.endObject();
            out.name(KEY_DEATHS);
            DeathAdapter.DEATH_STATS_ADAPTER.write(out, value.getDeathStat());
            out.endObject();
        }
        
        @Override
        public QuestingData read(JsonReader in) throws IOException{
            boolean playerLore = false, receivedBook = false;
            String uuid = null, selectedQuest = null;
            int lives = 0, teamId = -1;
            Team team = null;
            Map<UUID, GroupData> data = new HashMap<>();
            DeathStats deathStats = null;
            
            in.beginObject();
            while(in.hasNext()){
                switch(in.nextName()){
                    case KEY_UUID:
                        uuid = in.nextString();
                        break;
                    case KEY_NAME:
                        in.nextString();
                        break;
                    case KEY_LIVES:
                        lives = in.nextInt();
                        break;
                    case KEY_TEAM:
                        if(in.peek() == JsonToken.NUMBER)
                            teamId = in.nextInt();
                        else
                            team = TeamAdapter.TEAM_ADAPTER.read(in);
                        break;
                    case KEY_SELECTED_QUEST:
                        selectedQuest = in.nextString();
                        break;
                    case KEY_PLAYER_LORE:
                        playerLore = in.nextBoolean();
                        break;
                    case KEY_RECEIVED_BOOK:
                        receivedBook = in.nextBoolean();
                        break;
                    case KEY_GROUP_DATA:
                        in.beginObject();
                        while(in.hasNext())
                            data.put(UUID.fromString(in.nextName()), new GroupData(in.nextInt()));
                        in.endObject();
                        break;
                    case KEY_DEATHS:
                        deathStats = DeathAdapter.DEATH_STATS_ADAPTER.read(in);
                    default:
                        break;
                }
            }
            in.endObject();
            
            QuestingData questingData = new QuestingData(UUID.fromString(uuid), lives, teamId, data, deathStats);
            questingData.playedLore = playerLore;
            questingData.receivedBook = receivedBook;
            if(selectedQuest != null){
                questingData.selectedQuestId = UUID.fromString(selectedQuest);
            }
            if(teamId == -1)
                questingData.setTeam(team);
            return questingData;
        }
    };
}