package hqm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import hqm.quest.Quest;
import hqm.quest.QuestLine;
import hqm.quest.Questbook;
import net.minecraft.nbt.*;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author canitzp
 */
public class HQMJson {

    public static final TypeAdapter<Questbook> QUESTBOOK = new TypeAdapter<Questbook>() {
        @Override
        public void write(JsonWriter out, Questbook value) throws IOException {
            out.beginObject();
            out.name("name").value(value.getName());
            out.name("uuid").value(value.getId().toString());
            out.name("icon").value(value.getImage().toString());
            out.name("desc").beginArray();
            for(String line : value.getDescription()){
                out.value(line);
            }
            out.endArray();
            out.name("dim").beginArray();
            for(int dim : value.getDimensions()){
                out.value(dim);
            }
            out.endArray();
            out.endObject();
        }

        @Override
        public Questbook read(JsonReader in) throws IOException {
            String name = null;
            UUID id = null;
            List<String> desc = new ArrayList<>();
            List<Integer> dims = new ArrayList<>();
            ResourceLocation icon = null;
            in.beginObject();
            while (in.hasNext()){
                switch (in.nextName().toLowerCase()){
                    case "name": {
                        name = in.nextString();
                        break;
                    }
                    case "uuid": {
                        try {
                            id = UUID.fromString(in.nextString());
                            break;
                        } catch (IllegalArgumentException e){
                            System.out.println("Can't read Questbook file! " + e.getMessage());
                            return null;
                        }
                    }
                    case "icon": {
                        icon = new ResourceLocation(in.nextString());
                        break;
                    }
                    case "desc": {
                        in.beginArray();
                        while (in.hasNext()){
                            desc.add(in.nextString());
                        }
                        in.endArray();
                        break;
                    }
                    case "dim": {
                        in.beginArray();
                        while (in.hasNext()){
                            dims.add(in.nextInt());
                        }
                        in.endArray();
                        break;
                    }
                }
            }
            in.endObject();
            return new Questbook(name, id, desc, icon, dims);
        }
    };

    public static final TypeAdapter<QuestLine> QUEST_LINE = new TypeAdapter<QuestLine>() {
        @Override
        public void write(JsonWriter out, QuestLine value) throws IOException {
            out.beginObject();
            out.name("name").value(value.getName());
            out.name("index").value(value.getIndex());
            out.endArray();
            out.name("desc").beginArray();
            for(String dim : value.getDescription()){
                out.value(dim);
            }
            out.endArray();
            out.endArray();
            out.name("quests").beginArray();
            for(Quest quest : value.getQuests()){
                // TODO write quest
            }
            out.endArray();
            out.endObject();
        }

        @Override
        public QuestLine read(JsonReader in) throws IOException {
            String name = null;
            int index = Integer.MAX_VALUE;
            List<String> desc = new ArrayList<>();
            List<Quest> quests = new ArrayList<>();
            in.beginObject();
            while (in.hasNext()){
                switch (in.nextName().toLowerCase()){
                    case "name": {
                        name = in.nextString();
                        break;
                    }
                    case "index": {
                        index = in.nextInt();
                    }
                    case "desc": {
                        in.beginArray();
                        while (in.hasNext()){
                            desc.add(in.nextString());
                        }
                        in.endArray();
                        break;
                    }
                    case "quests": {
                        in.beginArray();
                        while (in.hasNext()){
                            quests.add(null); // TODO read quests
                        }
                        in.endArray();
                        break;
                    }
                }
            }
            in.endObject();
            return new QuestLine(name, index, desc, quests);
        }
    };

    public static final TypeAdapter<Quest> QUEST = new TypeAdapter<Quest>() {
        @Override
        public void write(JsonWriter out, Quest value) throws IOException {
            // We don't write anything
        }

        @Override
        public Quest read(JsonReader in) throws IOException {
            return null;
        }
    };

    public static final TypeAdapter<NBTTagCompound> NBT_TAG_COMPOUND = new TypeAdapter<NBTTagCompound>() {
        @Override
        public void write(JsonWriter out, NBTTagCompound value) throws IOException {
            // We don't write anything
        }

        @Override
        public NBTTagCompound read(JsonReader in) throws IOException {
            NBTTagCompound nbt = new NBTTagCompound();
            in.beginObject();
            while (in.hasNext()){
                String key = in.nextName();
                NBTBase nbtBase = null;
                in.beginObject();
                in.skipValue(); // skipping the name "id" here
                int index = in.nextInt();
                in.skipValue(); // skipping the name "value" here
                switch (index){
                    case 1: {
                        nbtBase = new NBTTagByte((byte) in.nextInt());
                        break;
                    }
                    case 2: {
                        nbtBase = new NBTTagShort((short) in.nextInt());
                        break;
                    }
                    case 3: {
                        nbtBase = new NBTTagInt(in.nextInt());
                        break;
                    }
                    case 4: {
                        nbtBase = new NBTTagLong(in.nextLong());
                        break;
                    }
                    case 5: {
                        nbtBase = new NBTTagFloat((float) in.nextDouble());
                        break;
                    }
                    case 6: {
                        nbtBase = new NBTTagDouble(in.nextDouble());
                        break;
                    }
                    case 7: {
                        nbtBase = new NBTTagByteArray((byte[]) gson.fromJson(in, byte[].class));
                        break;
                    } // TODO add more
                }
                in.endObject();
                if(nbtBase != null){
                    nbt.setTag(key, nbtBase);
                } else {
                    System.out.println("Couldn't read NBTBase " + key);
                }
            }
            in.endObject();
            return nbt;
        }
    };

    public static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Questbook.class, QUESTBOOK)
            .registerTypeAdapter(QuestLine.class, QUEST_LINE)
            .registerTypeAdapter(NBTTagCompound.class, NBT_TAG_COMPOUND)
            .setPrettyPrinting()
            .create();

    public static <T> T readFromFile(File file, Class<T> clazz){
        try {
            FileReader reader = new FileReader(file);
            T instance = gson.fromJson(reader, clazz);
            reader.close();
            return instance;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
