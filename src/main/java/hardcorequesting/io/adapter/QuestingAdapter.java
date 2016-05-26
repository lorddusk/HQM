package hardcorequesting.io.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import hardcorequesting.bag.GroupData;
import hardcorequesting.death.DeathStats;
import hardcorequesting.quests.QuestingData;
import net.minecraft.entity.player.EntityPlayer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class QuestingAdapter
{
    public static final TypeAdapter<QuestingData> QUESTING_DATA_ADAPTER = new TypeAdapter<QuestingData>()
    {
        public static final String TEAM = "team";
        public static final String LIVES = "lives";
        public static final String UUID = "uuid";
        public static final String NAME = "name";
        public static final String GROUP_DATA = "groupData";
        public static final String SELECTED_QUEST = "selectedQuest";
        public static final String PLAYER_LORE = "playedLore";
        public static final String RECEIVED_BOOK = "receivedBook";
        public static final String DEATHS = "deaths";

        @Override
        public void write(JsonWriter out, QuestingData value) throws IOException
        {
            out.beginObject();
            out.name(UUID).value(value.getUuid());
            out.name(NAME).value(value.getName());
            out.name(LIVES).value(value.getRawLives());
            out.name(TEAM).value(value.getTeam().getId());
            out.name(SELECTED_QUEST).value(value.selectedQuest);
            out.name(PLAYER_LORE).value(value.playedLore);
            out.name(RECEIVED_BOOK).value(value.receivedBook);
            out.name(GROUP_DATA).beginObject();
            for (Map.Entry<String, GroupData> entry : value.getGroupData().entrySet())
                if (entry.getKey() != null)
                    out.name(entry.getKey()).value(entry.getValue().retrieved);
            out.endObject();
            out.name(DEATHS);
            DeathAdapter.DEATH_STATS_ADAPTER.write(out, value.getDeathStat());
            out.endObject();
        }

        @Override
        public QuestingData read(JsonReader in) throws IOException
        {
            boolean playerLore = false, receivedBook = false;
            String uuid = null, selectedQuest = null;
            int lives = 0, team = -1;
            Map<String, GroupData> data = new HashMap<>();
            DeathStats deathStats = null;

            in.beginObject();
            while (in.hasNext())
            {
                switch (in.nextName())
                {
                    case UUID:
                        uuid = in.nextString();
                        break;
                    case NAME:
                        in.nextString();
                        break;
                    case LIVES:
                        lives = in.nextInt();
                        break;
                    case TEAM:
                        team = in.nextInt();
                        break;
                    case SELECTED_QUEST:
                        selectedQuest = in.nextString();
                        break;
                    case PLAYER_LORE:
                        playerLore = in.nextBoolean();
                        break;
                    case RECEIVED_BOOK:
                        receivedBook = in.nextBoolean();
                        break;
                    case GROUP_DATA:
                        in.beginObject();
                        while (in.hasNext())
                            data.put(in.nextName(), new GroupData(in.nextInt()));
                        in.endObject();
                        break;
                    case DEATHS:
                        deathStats = DeathAdapter.DEATH_STATS_ADAPTER.read(in);
                    default:
                        break;
                }
            }
            in.endObject();

            QuestingData questingData = new QuestingData(uuid, lives, team, data, deathStats);
            questingData.playedLore = playerLore;
            questingData.receivedBook = receivedBook;
            questingData.selectedQuest = selectedQuest;
            return questingData;
        }
    };
}
