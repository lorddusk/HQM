package hardcorequesting.common.io.adapter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import hardcorequesting.common.reputation.Reputation;
import hardcorequesting.common.reputation.ReputationMarker;
import hardcorequesting.common.util.WrappedText;
import net.minecraft.util.GsonHelper;

public class ReputationAdapter {
    private static final JsonArray EMPTY_ARRAY = new JsonArray();
    private static final Adapter<ReputationMarker> REPUTATION_MARKER_ADAPTER = new Adapter<ReputationMarker>() {
        private static final String NAME = "name";
        private static final String VALUE = "value";
        
        @Override
        public JsonElement serialize(ReputationMarker src) {
            return object()
                    .add(NAME, src.getName())
                    .add(VALUE, src.getValue())
                    .build();
        }
        
        @Override
        public ReputationMarker deserialize(JsonElement json) throws JsonParseException {
            JsonObject object = json.getAsJsonObject();
            
            return new ReputationMarker(
                    GsonHelper.getAsString(object, NAME, "Unnamed"),
                    GsonHelper.getAsInt(object, VALUE, 0),
                    false
            );
        }
    };
    
    public static final Adapter<Reputation> REPUTATION_ADAPTER = new Adapter<Reputation>() {
        private static final String ID = "id";
        private static final String NAME = "name";
        private static final String NEUTRAL = "neutral";
        private static final String MARKERS = "markers";
        
        @Override
        public JsonElement serialize(Reputation src) {
            return object()
                    .add(ID, src.getId())
                    .add(NAME, src.getName().toJson())
                    .add(NEUTRAL, src.getNeutralName())
                    .add(MARKERS, array()
                            .use(builder -> {
                                for (int i = 0; i < src.getMarkerCount(); i++) {
                                    builder.add(REPUTATION_MARKER_ADAPTER.serialize(src.getMarker(i)));
                                }
                            })
                            .build())
                    .build();
        }
        
        @Override
        public Reputation deserialize(JsonElement json) throws JsonParseException {
            JsonObject object = json.getAsJsonObject();
            Reputation reputation = new Reputation(
                    GsonHelper.getAsString(object, ID, null),
                    WrappedText.fromJson(object.get(NAME), "Unnamed", false),
                    GsonHelper.getAsString(object, NEUTRAL, "Neutral")
            );
            for (JsonElement element : GsonHelper.getAsJsonArray(object, MARKERS, EMPTY_ARRAY)) {
                reputation.add(REPUTATION_MARKER_ADAPTER.deserialize(element));
            }
            
            return reputation;
        }
    };
}
