package hardcorequesting.common.io.adapter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import hardcorequesting.common.death.DeathStat;
import hardcorequesting.common.death.DeathType;
import net.minecraft.util.GsonHelper;

import java.util.Map;
import java.util.UUID;

public class DeathAdapter {
    
    public static final Adapter<DeathStat> DEATH_STATS_ADAPTER = new Adapter<DeathStat>() {
        private static final String DEATHS = "deaths";
        private static final String NAME = "name";
        
        @Override
        public JsonElement serialize(DeathStat src) {
            return object()
                    .add(src.getUuid().toString(), object()
                            .add(DEATHS, array()
                                    .use(builder -> {
                                        for (DeathType type : DeathType.values())
                                            builder.add(src.getDeaths(type.ordinal()));
                                    })
                                    .build())
                            .add(NAME, src.getCachedName())
                            .build())
                    .build();
        }
        
        @Override
        public DeathStat deserialize(JsonElement json) throws JsonParseException {
            JsonObject object = json.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                DeathStat stat = new DeathStat(UUID.fromString(entry.getKey()));
                JsonArray array = new JsonArray();
                if (entry.getValue().isJsonArray()) array = entry.getValue().getAsJsonArray();
                if (entry.getValue().isJsonObject()) {
                    JsonObject jsonObject = entry.getValue().getAsJsonObject();
                    stat.setCachedName(GsonHelper.getAsString(jsonObject, NAME, null));
                    array = GsonHelper.getAsJsonArray(jsonObject, DEATHS, array);
                }
                int i = 0;
                for (JsonElement element : array) {
                    stat.increaseDeath(i++, element.getAsInt(), false);
                }
                return stat;
            }
            throw new NullPointerException("Failed to get DeathStat!");
        }
    };
}
