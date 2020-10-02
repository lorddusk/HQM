package hardcorequesting.common.io.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import hardcorequesting.common.reputation.Reputation;
import hardcorequesting.common.reputation.ReputationMarker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReputationAdapter {
    
    private static final TypeAdapter<ReputationMarker> REPUTATION_MARKER_ADAPTER = new TypeAdapter<ReputationMarker>() {
        private static final String NAME = "name";
        private static final String VALUE = "value";
        
        @Override
        public void write(JsonWriter out, ReputationMarker value) throws IOException {
            out.beginObject();
            out.name(NAME).value(value.getName());
            out.name(VALUE).value(value.getValue());
            out.endObject();
        }
        
        @Override
        public ReputationMarker read(JsonReader in) throws IOException {
            in.beginObject();
            String name = "Unnamed";
            int value = 0;
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case NAME:
                        name = in.nextString();
                        break;
                    case VALUE:
                        value = in.nextInt();
                        break;
                }
            }
            in.endObject();
            return new ReputationMarker(name, value, false);
        }
    };
    
    public static final TypeAdapter<Reputation> REPUTATION_ADAPTER = new TypeAdapter<Reputation>() {
        private static final String ID = "id";
        private static final String NAME = "name";
        private static final String NEUTRAL = "neutral";
        private static final String MARKERS = "markers";
        
        @Override
        public void write(JsonWriter out, Reputation value) throws IOException {
            out.beginObject();
            out.name(ID).value(value.getId());
            out.name(NAME).value(value.getName());
            out.name(NEUTRAL).value(value.getNeutralName());
            if (value.getMarkerCount() > 0) {
                out.name(MARKERS).beginArray();
                for (int i = 0; i < value.getMarkerCount(); i++) {
                    REPUTATION_MARKER_ADAPTER.write(out, value.getMarker(i));
                }
                out.endArray();
            }
            out.endObject();
        }
        
        @Override
        public Reputation read(JsonReader in) throws IOException {
            in.beginObject();
            String name = "Unnamed", neutral = "Neutral", id = null;
            List<ReputationMarker> markers = new ArrayList<>();
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case ID:
                        id = in.nextString();
                        break;
                    case NAME:
                        name = in.nextString();
                        break;
                    case NEUTRAL:
                        neutral = in.nextString();
                        break;
                    case MARKERS:
                        in.beginArray();
                        while (in.hasNext()) {
                            markers.add(REPUTATION_MARKER_ADAPTER.read(in));
                        }
                        in.endArray();
                        break;
                }
            }
            in.endObject();
            Reputation reputation = new Reputation(id, name, neutral);
            markers.forEach(reputation::add);
            return reputation;
        }
    };
}
