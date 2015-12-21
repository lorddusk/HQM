package hardcorequesting.parsing;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import hardcorequesting.reputation.Reputation;
import hardcorequesting.reputation.ReputationMarker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReputationAdapter {
    private static final TypeAdapter<ReputationMarker> REPUTATION_MARKER_ADAPTER = new TypeAdapter<ReputationMarker>() {
        private final String NAME = "name";
        private final String VALUE = "value";

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
                }
            }
            in.endObject();
            return new ReputationMarker(name, value, false);
        }
    };

    public static final TypeAdapter<Reputation> REPUTATION_ADAPTER = new TypeAdapter<Reputation>() {
        private final String NAME = "name";
        private final String NEUTRAL = "neutral";
        private final String MARKERS = "markers";

        @Override
        public void write(JsonWriter out, Reputation value) throws IOException {
            out.beginObject();
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
            String name = "Unnamed", neutral = "Neutral";
            List<ReputationMarker> markers = new ArrayList<>();
            while (in.hasNext()) {
                switch (in.nextName()) {
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
            Reputation reputation = null;
            for (Reputation rep : Reputation.getReputationList()) {
                if (rep.getName().equals(name)) {
                    reputation = rep;
                    break;
                }
            }
            if (reputation == null) reputation = new Reputation(name, neutral);
            reputation.clearMarkers();
            for (ReputationMarker marker : markers) {
                reputation.add(marker);
            }
            return reputation;
        }
    };
}
