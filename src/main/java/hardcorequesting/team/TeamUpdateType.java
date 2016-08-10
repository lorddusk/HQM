package hardcorequesting.team;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import hardcorequesting.io.adapter.TeamAdapter;
import hardcorequesting.network.message.TeamUpdateMessage;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestData;
import hardcorequesting.quests.reward.ReputationReward;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

@SuppressWarnings("unchecked")
public enum TeamUpdateType {
    FULL {
        @Override
        public void update(Team team, String data) {
            try {
                team.update(TeamAdapter.TEAM_ADAPTER.fromJson(data));
            } catch (IOException ignored) {
            }
        }

        @Override
        public TeamUpdateMessage build(Team team, Object... data) {
            try {
                return new TeamUpdateMessage(this, TeamAdapter.TEAM_ADAPTER.toJson(team));
            } catch (IOException ignored) {
            }
            return null;
        }
    },
    LIVES {
        @Override
        public void update(Team team, String data) {
            team.setClientTeamLives(Integer.parseInt(data));
        }

        @Override
        public TeamUpdateMessage build(Team team, Object... data) {
            return new TeamUpdateMessage(this, "" + team.getSharedLives());
        }
    },
    REPUTATION_RECEIVED {
        private static final String QUEST = "quest";
        private static final String REPUTATIONS = "reputations";
        private static final String ID = "id";
        private static final String VAL = "val";

        @Override
        public void update(Team team, String data) {
            JsonParser parser = new JsonParser();
            JsonObject object = parser.parse(data).getAsJsonObject();
            Quest quest = Quest.getQuest(object.get(QUEST).getAsString());
            if (quest != null) {
                QuestData questData = team.getQuestData(quest.getId());
                if (questData != null) {
                    questData.claimed = true;
                    for (JsonElement element : object.get(REPUTATIONS).getAsJsonArray()) {
                        String id = element.getAsJsonObject().get(ID).getAsString();
                        int val = element.getAsJsonObject().get(VAL).getAsInt();
                        team.setReputation(id, val);
                    }
                }
            }
        }

        @Override
        public TeamUpdateMessage build(Team team, Object... data) {
            StringWriter out = new StringWriter();
            try {
                JsonWriter writer = new JsonWriter(out);
                writer.beginObject();
                writer.name(QUEST).value(((Quest) data[0]).getId());
                writer.name(REPUTATIONS).beginArray();
                for (ReputationReward reward : ((List<ReputationReward>) data[1])) {
                    writer.beginObject();
                    writer.name(ID).value(reward.getReward().getId());
                    writer.name(VAL).value(team.getReputation(reward.getReward()));
                    writer.endObject();
                }
                writer.endArray();
                writer.endObject();
                writer.close();
            } catch (IOException ignored) {
            }

            return new TeamUpdateMessage(this, out.toString());
        }
    };

    public abstract void update(Team team, String data);

    public abstract TeamUpdateMessage build(Team team, Object... data);
}
