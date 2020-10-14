package hardcorequesting.common.io.adapter;

import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import hardcorequesting.common.HardcoreQuestingCore;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.function.Consumer;

public abstract class Adapter<T> extends TypeAdapter<T> {
    public abstract JsonElement serialize(T src);
    
    @Nullable
    public abstract T deserialize(JsonElement json);
    
    @Override
    public void write(JsonWriter out, T value) throws IOException {
        Streams.write(serialize(value), out);
    }
    
    @Override
    public T read(JsonReader in){
        try{
            JsonElement jsonElement = Streams.parse(in);
            return deserialize(jsonElement);
        } catch(JsonParseException e){
            HardcoreQuestingCore.LOGGER.error("Can't parse JsonReader to JsonElement!", e);
            return null;
        }
    }
    
    public static JsonObjectBuilder object() {
        return new JsonObjectBuilder(new JsonObject());
    }
    
    public static JsonArrayBuilder array() {
        return new JsonArrayBuilder(new JsonArray());
    }
    
    @SafeVarargs
    public static <T> JsonArrayBuilder array(T... objects) {
        JsonArrayBuilder array = array();
        for (Object o : objects) {
            array.smartInsert(o);
        }
        return array;
    }
    
    public static JsonArrayBuilder array(boolean... objects) {
        JsonArrayBuilder array = array();
        for (boolean o : objects) {
            array.add(o);
        }
        return array;
    }
    
    public static JsonArrayBuilder array(int... objects) {
        JsonArrayBuilder array = array();
        for (int o : objects) {
            array.add(o);
        }
        return array;
    }
    
    public static JsonArrayBuilder array(float... objects) {
        JsonArrayBuilder array = array();
        for (float o : objects) {
            array.add(o);
        }
        return array;
    }
    
    
    public static JsonArrayBuilder array(double... objects) {
        JsonArrayBuilder array = array();
        for (double o : objects) {
            array.add(o);
        }
        return array;
    }
    
    
    public static JsonArrayBuilder array(long... objects) {
        JsonArrayBuilder array = array();
        for (long o : objects) {
            array.add(o);
        }
        return array;
    }
    
    
    public static JsonArrayBuilder array(char... objects) {
        JsonArrayBuilder array = array();
        for (char o : objects) {
            array.add(o);
        }
        return array;
    }
    
    public static JsonNull nullVal() {
        return JsonNull.INSTANCE;
    }
    
    public static class JsonObjectBuilder {
        private JsonObject object;
        
        public JsonObjectBuilder(JsonObject object) {
            this.object = object;
        }
        
        public JsonObjectBuilder add(String key, JsonElement element) {
            object.add(key, element);
            return this;
        }
        
        public JsonObjectBuilder add(String key, Number element) {
            object.addProperty(key, element);
            return this;
        }
        
        public JsonObjectBuilder add(String key, String element) {
            object.addProperty(key, element);
            return this;
        }
        
        public JsonObjectBuilder add(String key, boolean element) {
            object.addProperty(key, element);
            return this;
        }
        
        public JsonObjectBuilder add(String key, char element) {
            object.addProperty(key, element);
            return this;
        }
        
        public JsonObjectBuilder use(Consumer<JsonObjectBuilder> consumer) {
            consumer.accept(this);
            return this;
        }
        
        public JsonObject build() {
            return object;
        }
    }
    
    public static class JsonArrayBuilder {
        private JsonArray array;
        
        public JsonArrayBuilder(JsonArray array) {
            this.array = array;
        }
        
        public JsonArrayBuilder add(JsonElement element) {
            array.add(element);
            return this;
        }
        
        public JsonArrayBuilder add(Number element) {
            array.add(element);
            return this;
        }
        
        public JsonArrayBuilder add(String element) {
            array.add(element);
            return this;
        }
        
        public JsonArrayBuilder add(boolean element) {
            array.add(element);
            return this;
        }
        
        public JsonArrayBuilder add(char element) {
            array.add(element);
            return this;
        }
        
        public JsonArrayBuilder smartInsert(Object o) {
            if (o instanceof JsonElement)
                return add((JsonElement) o);
            if (o instanceof Number)
                return add((Number) o);
            if (o instanceof String)
                return add((String) o);
            if (o instanceof Boolean)
                return add((Boolean) o);
            if (o instanceof Character)
                return add((Character) o);
            return this;
        }
        
        public JsonArrayBuilder use(Consumer<JsonArrayBuilder> consumer) {
            consumer.accept(this);
            return this;
        }
        
        public JsonArray build() {
            return array;
        }
    }
}
