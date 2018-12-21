package hardcorequesting.io.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import hardcorequesting.quests.QuestData;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.reputation.Reputation;
import hardcorequesting.team.LifeSetting;
import hardcorequesting.team.PlayerEntry;
import hardcorequesting.team.RewardSetting;
import hardcorequesting.team.Team;

import java.io.IOException;
import java.util.*;

public class TeamAdapter {

    private static Map<Team, List<Integer>> invitesMap = new HashMap<>();
    public static final TypeAdapter<Team> TEAM_ADAPTER = new TypeAdapter<Team>() {
        private static final String ID = "id";
        private static final String NAME = "name";
        private static final String LIFE_SETTING = "lifeSetting";
        private static final String REWARD_SETTING = "rewardSetting";
        private static final String PLAYERS = "players";
        private static final String REPUTATIONS = "reputations";
        private static final String REP_ID = "reputationId";
        private static final String REP_VAL = "reputationValue";
        private static final String QUEST_DATA_LIST = "questDataList";
        private static final String QUEST_ID = "questId";
        private static final String QUEST_DATA = "questData";
        private static final String INVITES = "invites";

        @Override
        public void write(JsonWriter out, Team value) throws IOException {
        		if(value == null)
        			return;
        	
            out.beginObject();
            out.name(ID).value(value.getId());
            out.name(NAME).value(value.getName());
            out.name(LIFE_SETTING).value(value.getLifeSetting().name());
            out.name(REWARD_SETTING).value(value.getRewardSetting().name());
            out.name(PLAYERS).beginArray();
            for (PlayerEntry entry : value.getPlayers())
                entry.write(out);
            out.endArray();
            out.name(REPUTATIONS).beginArray();
            for (Reputation reputation : Reputation.getReputations().values()) {
                out.beginObject();
                out.name(REP_ID).value(reputation.getId());
                out.name(REP_VAL).value(value.getReputation(reputation));
                out.endObject();
            }
            out.endArray();
            out.name(QUEST_DATA_LIST).beginArray();
            for (Map.Entry<UUID, QuestData> data : value.getQuestData().entrySet()) {
                out.beginObject();
                out.name(QUEST_ID).value(data.getKey().toString());
                out.name(QUEST_DATA);
                QuestDataAdapter.QUEST_DATA_ADAPTER.write(out, data.getValue());
                out.endObject();
            }
            out.endArray();
            out.name(INVITES).beginArray();
            for (Team team : value.getInvites())
                out.value(team.getId());
            out.endArray();
            out.endObject();
        }

        @Override
        public Team read(JsonReader in) throws IOException {
            in.beginObject();
            Team team = new Team(null);
            List<Integer> invites = new ArrayList<>();
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case ID:
                        team.setId(in.nextInt());
                        break;
                    case NAME:
                        if (team.getId() == -1) {
                            in.nextNull();
                            team.setName(null);
                        } else {
                            team.setName(in.nextString());
                        }
                        break;
                    case LIFE_SETTING:
                        team.setLifeSetting(LifeSetting.valueOf(in.nextString()));
                        break;
                    case REWARD_SETTING:
                        team.setRewardSetting(RewardSetting.valueOf(in.nextString()));
                        break;
                    case PLAYERS:
                        in.beginArray();
                        while (in.hasNext())
                            team.addPlayer(PlayerEntry.read(in));
                        in.endArray();
                        break;
                    case REPUTATIONS:
                        in.beginArray();
                        while (in.hasNext()) {
                            in.beginObject();
                            String id = null;
                            int val = 0;
                            while (in.hasNext()) {
                                switch (in.nextName()) {
                                    case REP_ID:
                                        id = in.nextString();
                                        break;
                                    case REP_VAL:
                                        val = in.nextInt();
                                        break;
                                    default:
                                        break;
                                }
                            }
                            team.setReputation(id, val);
                            in.endObject();
                        }
                        in.endArray();
                        break;
                    case QUEST_DATA_LIST:
                        in.beginArray();
                        while (in.hasNext()) {
                            in.beginObject();
                            String id = null;
                            QuestData data = null;
                            while (in.hasNext()) {
                                switch (in.nextName()) {
                                    case QUEST_ID:
                                        id = in.nextString();
                                        break;
                                    case QUEST_DATA:
                                        data = QuestDataAdapter.QUEST_DATA_ADAPTER.read(in);
                                        break;
                                    default:
                                        break;
                                }
                            }
                            if (id != null && data != null)
                                team.getQuestData().put(UUID.fromString(id), data);
                            in.endObject();
                        }
                        in.endArray();
                        break;
                    case INVITES:
                        in.beginArray();
                        while (in.hasNext())
                            invites.add(in.nextInt());
                        in.endArray();
                        break;
                    default:
                        break;
                }
            }
            in.endObject();
            if (invites.size() > 0)
                invitesMap.put(team, invites);
            return team;
        }
    };

    public static void clearInvitesMap() {
        invitesMap.clear();
    }

    public static void commitInvitesMap() {
        if (invitesMap.size() > 0) {
            Map<Integer, Team> tempMap = new HashMap<>();
            QuestingData.getTeams().stream().filter(Objects::nonNull).forEach(team -> tempMap.put(team.getId(), team));
            for (Team team : QuestingData.getTeams()) {
                List<Integer> invites = invitesMap.get(team);
                if (invites != null)
                    invites.forEach(id ->
                    {
                        if (tempMap.containsKey(id))
                            team.getInvites().add(tempMap.get(id));
                    });
            }
        }
        clearInvitesMap();
    }
}
