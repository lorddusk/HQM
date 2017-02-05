package hardcorequesting.io.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import hardcorequesting.death.DeathStats;
import hardcorequesting.death.DeathType;

import java.io.IOException;

public class DeathAdapter {

    public static final TypeAdapter<DeathStats> DEATH_STATS_ADAPTER = new TypeAdapter<DeathStats>() {
        @Override
        public void write(JsonWriter out, DeathStats value) throws IOException {
            out.beginObject();
            out.name(value.getUuid());
            out.beginArray();
            for (DeathType type : DeathType.values())
                out.value(value.getDeaths(type.ordinal()));
            out.endArray();
            out.endObject();
        }

        @Override
        public DeathStats read(JsonReader in) throws IOException {
            in.beginObject();
            DeathStats stats = null;
            if (in.hasNext()) {
                String uuid = in.nextName();
                stats = new DeathStats(uuid);
                in.beginArray();
                int i = 0;
                while (in.hasNext())
                    stats.increaseDeath(i++, in.nextInt(), false);
                in.endArray();
            }
            in.endObject();
            return stats; // Should never be null
        }
    };
}
