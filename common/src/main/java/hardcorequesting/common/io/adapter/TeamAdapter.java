package hardcorequesting.common.io.adapter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.data.QuestData;
import hardcorequesting.common.reputation.Reputation;
import hardcorequesting.common.reputation.ReputationManager;
import hardcorequesting.common.team.*;
import net.minecraft.Util;
import net.minecraft.util.GsonHelper;

import java.util.*;
import java.util.stream.StreamSupport;

public class TeamAdapter {
    
    private static Map<Team, List<UUID>> invitesMap = new HashMap<>();
    public static final Adapter<Team> TEAM_ADAPTER = new Adapter<Team>() {
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
        public JsonElement serialize(Team src) {
            return object()
                    .add(ID, src.getId().toString())
                    .add(NAME, src.getName())
                    .add(LIFE_SETTING, src.getLifeSetting().name())
                    .add(REWARD_SETTING, src.getRewardSetting().name())
                    .add(PLAYERS, array()
                            .use(builder -> {
                                for (PlayerEntry entry : src.getPlayers())
                                    builder.add(entry.toJson());
                            })
                            .build())
                    .add(REPUTATIONS, array()
                            .use(builder -> {
                                for (Reputation reputation : ReputationManager.getInstance().getReputations().values()) {
                                    builder.add(object()
                                            .add(REP_ID, reputation.getId())
                                            .add(REP_VAL, src.getReputation(reputation))
                                            .build());
                                }
                            })
                            .build())
                    .add(QUEST_DATA_LIST, array()
                            .use(builder -> {
                                for (Map.Entry<UUID, QuestData> data : src.getQuestData().entrySet()) {
                                    builder.add(object()
                                            .add(QUEST_ID, data.getKey().toString())
                                            .add(QUEST_DATA, QuestDataAdapter.serialize(data.getValue()))
                                            .build());
                                }
                            })
                            .build())
                    .add(INVITES, array()
                            .use(builder -> {
                                for (Team team : src.getInvites())
                                    builder.add(team.getId().toString());
                            })
                            .build())
                    .build();
        }
        
        @Override
        public Team deserialize(JsonElement json) throws JsonParseException {
            List<UUID> invites = new ArrayList<>();
            JsonObject object = json.getAsJsonObject();
            Team team = Team.empty();
            if (object.has(ID) && object.get(ID).getAsJsonPrimitive().isString())
                team.setId(UUID.fromString(GsonHelper.getAsString(object, ID)));
            if (!team.getId().equals(Util.NIL_UUID))
                team.setName(GsonHelper.getAsString(object, NAME));
            team.setLifeSetting(LifeSetting.valueOf(GsonHelper.getAsString(object, LIFE_SETTING)));
            team.setRewardSetting(RewardSetting.valueOf(GsonHelper.getAsString(object, REWARD_SETTING)));
            for (JsonElement element : GsonHelper.getAsJsonArray(object, PLAYERS)) {
                team.addPlayer(PlayerEntry.read(element));
            }
            for (JsonElement element : GsonHelper.getAsJsonArray(object, REPUTATIONS)) {
                JsonObject reputationObject = element.getAsJsonObject();
                team.setReputation(
                        GsonHelper.getAsString(reputationObject, REP_ID),
                        GsonHelper.getAsInt(reputationObject, REP_VAL)
                );
            }
            for (JsonElement element : GsonHelper.getAsJsonArray(object, QUEST_DATA_LIST)) {
                JsonObject questDataObject = element.getAsJsonObject();
                UUID questId = UUID.fromString(GsonHelper.getAsString(questDataObject, QUEST_ID));
                Quest quest = Quest.getQuest(questId);
                team.getQuestData().put(
                        questId,
                        QuestDataAdapter.deserialize(GsonHelper.getAsJsonObject(questDataObject, QUEST_DATA), quest)
                );
            }
            for (JsonElement element : GsonHelper.getAsJsonArray(object, INVITES)) {
                if (element.getAsJsonPrimitive().isString())
                    invites.add(UUID.fromString(element.getAsString()));
            }
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
            Map<UUID, Team> tempMap = new HashMap<>();
            StreamSupport.stream(TeamManager.getInstance().getNamedTeams().spliterator(), false).filter(Objects::nonNull).forEach(team -> tempMap.put(team.getId(), team));
            for (Team team : TeamManager.getInstance().getTeams()) {
                List<UUID> invites = invitesMap.get(team);
                if (invites != null)
                    invites.forEach(id -> {
                        if (tempMap.containsKey(id))
                            team.getInvites().add(tempMap.get(id));
                    });
            }
        }
        clearInvitesMap();
    }
}
