package hardcorequesting.common.team;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import hardcorequesting.common.io.adapter.TeamAdapter;
import hardcorequesting.common.network.message.TeamUpdateMessage;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestData;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.quests.reward.ReputationReward;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
            return new TeamUpdateMessage(this, TeamAdapter.TEAM_ADAPTER.toJson(team));
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
            Quest quest = Quest.getQuest(UUID.fromString(object.get(QUEST).getAsString()));
            if (quest != null) {
                QuestData questData = team.getQuestData(quest.getQuestId());
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
                writer.name(QUEST).value(((Quest) data[0]).getQuestId().toString());
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
    },
    CREATE_TEAM {
        @Override
        public void update(Team team, String data) {
            try {
                Team newTeam = TeamAdapter.TEAM_ADAPTER.fromJson(data);
                TeamManager.getInstance().addTeam(newTeam);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        @Override
        public TeamUpdateMessage build(Team team, Object... data) {
            return new TeamUpdateMessage(this, TeamAdapter.TEAM_ADAPTER.toJson(team));
        }
    },
    JOIN_TEAM {
        @Override
        public void update(Team team, String data) {
            String uuid = data.substring(0, 36);
            String teamJson = data.substring(36);
            try {
                Team joinedTeam = TeamAdapter.TEAM_ADAPTER.fromJson(teamJson);
                TeamManager.getInstance().removeTeam(joinedTeam);
                TeamManager.getInstance().addTeam(joinedTeam);
                QuestingDataManager.getInstance().getQuestingData(UUID.fromString(uuid)).setTeam(joinedTeam);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        @Override
        public TeamUpdateMessage build(Team team, Object... data) {
            return new TeamUpdateMessage(this, data[0].toString() + TeamAdapter.TEAM_ADAPTER.toJson(team));
        }
    },
    LEAVE_TEAM {
        @Override
        public void update(Team team, String data) {
            String uuid = data.substring(0, 36);
            String teamJson = data.substring(36);
            try {
                QuestingDataManager.getInstance().getQuestingData(UUID.fromString(uuid)).setTeam(TeamAdapter.TEAM_ADAPTER.fromJson(teamJson));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        @Override
        public TeamUpdateMessage build(Team team, Object... data) {
            return new TeamUpdateMessage(this, data[0].toString() + TeamAdapter.TEAM_ADAPTER.toJson((Team) data[1]));
        }
    },
    REMOVE_TEAM {
        @Override
        public void update(Team clientTeam, String data) {
            UUID id = UUID.fromString(data);
            TeamManager.getInstance().removeTeam(TeamManager.getInstance().getByTeamId(id));
        }
        
        @Override
        public TeamUpdateMessage build(Team team, Object... data) {
            return new TeamUpdateMessage(this, "" + team.getId());
        }
    },
    INVITE {
        @Override
        public void update(Team clientTeam, String data) {
            try {
                Team team = TeamAdapter.TEAM_ADAPTER.fromJson(data);
                TeamManager.getInstance().removeTeam(team);
                TeamManager.getInstance().addTeam(team);
                clientTeam.getInvites().add(team);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        @Override
        public TeamUpdateMessage build(Team team, Object... data) {
            return new TeamUpdateMessage(this, TeamAdapter.TEAM_ADAPTER.toJson(team));
        }
    };
    
    public abstract void update(Team team, String data);
    
    public abstract TeamUpdateMessage build(Team team, Object... data);
}
