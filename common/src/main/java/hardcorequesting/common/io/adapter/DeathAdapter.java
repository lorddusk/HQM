package hardcorequesting.common.io.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import hardcorequesting.common.death.DeathStat;
import hardcorequesting.common.death.DeathType;

import java.io.IOException;
import java.util.UUID;

public class DeathAdapter {
    
    public static final TypeAdapter<DeathStat> DEATH_STATS_ADAPTER = new TypeAdapter<DeathStat>() {
        @Override
        public void write(JsonWriter out, DeathStat value) throws IOException {
            out.beginObject();
            out.name(value.getUuid().toString());
            out.beginArray();
            for (DeathType type : DeathType.values())
                out.value(value.getDeaths(type.ordinal()));
            out.endArray();
            out.endObject();
        }
        
        @Override
        public DeathStat read(JsonReader in) throws IOException {
            in.beginObject();
            DeathStat stats = null;
            if (in.hasNext()) {
                String uuid = in.nextName();
                stats = new DeathStat(UUID.fromString(uuid));
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
