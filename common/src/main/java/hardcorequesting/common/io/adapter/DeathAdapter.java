package hardcorequesting.common.io.adapter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.death.DeathStat;
import hardcorequesting.common.death.DeathType;
import net.minecraft.util.GsonHelper;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public class DeathAdapter {
    
    public static final Adapter<DeathStat> DEATH_STATS_ADAPTER = new Adapter<DeathStat>() {
        private static final String DEATHS = "deaths";
        private static final String NAME = "name";
        
        @Override
        public JsonElement serialize(DeathStat src){
            return object()
                .add(src.getUuid().toString(), object()
                    .add(DEATHS, array().use(builder -> Arrays.stream(DeathType.values()).forEach(deathType -> builder.add(src.getDeaths(deathType.ordinal())))).build())
                    .add(NAME, src.getCachedName())
                    .build())
                .build();
        }
        
        @Override
        public DeathStat deserialize(JsonElement jsonElement){
            if(!jsonElement.isJsonObject()){
                HardcoreQuestingCore.LOGGER.error(new JsonParseException("JsonElement for 'Death Stat' is not a JsonObject but '" + jsonElement.getClass().getName() + "'!"));
                return null;
            }
            JsonObject json = jsonElement.getAsJsonObject();
            
            // the loop is needed, because we don't know the key name yet, since it is the uuid.
            // Normally this should never loop more than once, except the first entry is not valid.
            for(Map.Entry<String, JsonElement> entry : json.entrySet()){
                UUID uuid;
                try{
                    uuid = UUID.fromString(entry.getKey());
                } catch(IllegalArgumentException e){
                    HardcoreQuestingCore.LOGGER.error("Json key for 'Death Stat' can't be parsed to UUID!", e);
                    continue; // continue instead of return so we can check if the next key-value pair is valid
                }
                DeathStat deathStat = new DeathStat(uuid);
                
                JsonArray array;
                if(entry.getValue().isJsonArray()){
                    array = entry.getValue().getAsJsonArray();
                } else if(entry.getValue().isJsonObject()){
                    JsonObject jsonObject = entry.getValue().getAsJsonObject();
                    deathStat.setCachedName(GsonHelper.getAsString(jsonObject, NAME, null));
                    array = GsonHelper.getAsJsonArray(jsonObject, DEATHS);
                } else {
                    HardcoreQuestingCore.LOGGER.error("Json value for 'Death Stat' with uuid '" + deathStat.getUuid() + "' isn't a JsonObject or JsonArray!");
                    continue; // continue instead of return so we can check if the next key-value pair is valid
                }
    
                int i = 0;
                for(JsonElement element : array){
                    if(GsonHelper.isNumberValue(element)){
                        deathStat.increaseDeath(i++, element.getAsInt(), false);
                    } else {
                        HardcoreQuestingCore.LOGGER.error("JsonArray for 'Death Stat' with uuid '" + deathStat.getUuid() + "' does contain a invalid non-integer type!");
                    }
                }
                return deathStat;
            }
            
            HardcoreQuestingCore.LOGGER.error("Can't parse 'DeathStat' from json file. No valid key-value pair found!");
            return null;
        }
    };
}