package hqm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import hqm.quest.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
        public void write(JsonWriter out, Questbook value) {} // We don't write anything

        @Override
        public Questbook read(JsonReader in) throws IOException {
            String name = null;
            UUID id = null;
            List<String> desc = new ArrayList<>();
            List<String> tooltip = new ArrayList<>();
            List<Integer> dims = new ArrayList<>();
            ResourceLocation icon = null;
            CompoundNBT data = null;
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
                    case "tooltip": {
                        in.beginArray();
                        while (in.hasNext()){
                            tooltip.add(in.nextString());
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
                    case "data": {
                        data = gson.fromJson(in, CompoundNBT.class);
                        break;
                    }
                }
            }
            in.endObject();
            return new Questbook(name, id, desc, tooltip, icon, dims).setData(data);
        }
    };

    public static final TypeAdapter<QuestLine> QUEST_LINE = new TypeAdapter<QuestLine>() {
        @Override
        public void write(JsonWriter out, QuestLine value) {} // We don't write anything

        @Override
        public QuestLine read(JsonReader in) throws IOException {
            String name = null;
            int index = Integer.MAX_VALUE;
            CompoundNBT data = null;
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
                        break;
                    }
                    case "data": {
                        data = gson.fromJson(in, CompoundNBT.class);
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
                    case "quests": {
                        in.beginArray();
                        while (in.hasNext()){
                            quests.add(gson.fromJson(in, Quest.class));
                        }
                        in.endArray();
                        break;
                    }
                }
            }
            in.endObject();
            return new QuestLine(name, index, desc, quests).setData(data);
        }
    };

    public static final TypeAdapter<Quest> QUEST = new TypeAdapter<Quest>() {
        @Override
        public void write(JsonWriter out, Quest value) {} // We don't write anything

        @Override
        public Quest read(JsonReader in) throws IOException {
            String name = null;
            UUID id = null, parentId = null;
            int posX = 0, posY = 0;
            ItemStack stack = ItemStack.EMPTY;
            List<String> desc = new ArrayList<>();
            List<ITask> tasks = new ArrayList<>();
            in.beginObject();
            while (in.hasNext()) {
                switch (in.nextName().toLowerCase()) {
                    case "name": {
                        name = in.nextString();
                        break;
                    }
                    case "id": {
                        try {
                            id = UUID.fromString(in.nextString());
                            break;
                        } catch (IllegalArgumentException e){
                            System.out.println("Can't read quests! " + e.getMessage());
                            return null;
                        }
                    }
                    case "parent": {
                        try {
                            parentId = UUID.fromString(in.nextString());
                            break;
                        } catch (IllegalArgumentException e){
                            System.out.println("Can't read quests! " + e.getMessage());
                            return null;
                        }
                    }
                    case "posX": {
                        posX = in.nextInt();
                        break;
                    }
                    case "posY": {
                        posY = in.nextInt();
                        break;
                    }
                    case "icon": {
                        stack = gson.fromJson(in, ItemStack.class);
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
                    case "tasks": {
                        in.beginArray();
                        while (in.hasNext()){
                            tasks.add(gson.fromJson(in, ITask.class));
                        }
                        in.endArray();
                        break;
                    }
                }
            }
            in.endObject();
            return new Quest(name, id, parentId, posX, posY, stack, desc, tasks);
        }
    };

    public static final TypeAdapter<CompoundNBT> NBT_TAG_COMPOUND = new TypeAdapter<CompoundNBT>() {
        @Override
        public void write(JsonWriter out, CompoundNBT value) {} // We don't write anything

        @Override
        public CompoundNBT read(JsonReader in) throws IOException {
            CompoundNBT nbt = new CompoundNBT();
            in.beginObject();
            while (in.hasNext()){
                String key = in.nextName();
                INBT nbtBase = null;
                in.beginObject();
                in.skipValue(); // skipping the name "id" here
                int index = in.nextInt();
                in.skipValue(); // skipping the name "value" here
                switch (index){
                    case 1: {
                        nbtBase = ByteNBT.func_229671_a_((byte) in.nextInt());
                        break;
                    }
                    case 2: {
                        nbtBase = ShortNBT.func_229701_a_((short) in.nextInt());
                        break;
                    }
                    case 3: {
                        nbtBase = IntNBT.func_229692_a_(in.nextInt());
                        break;
                    }
                    case 4: {
                        nbtBase = LongNBT.func_229698_a_(in.nextLong());
                        break;
                    }
                    case 5: {
                        nbtBase = FloatNBT.func_229689_a_((float) in.nextDouble());
                        break;
                    }
                    case 6: {
                        nbtBase = DoubleNBT.func_229684_a_(in.nextDouble());
                        break;
                    }
                    case 7: {
                        nbtBase = new ByteArrayNBT((byte[]) gson.fromJson(in, byte[].class));
                        break;
                    } // TODO add more
                }
                in.endObject();
                if(nbtBase != null){
                    nbt.put(key, nbtBase);
                } else {
                    System.out.println("Couldn't read NBTBase " + key);
                }
            }
            in.endObject();
            return nbt;
        }
    };

    public static final TypeAdapter<TeamList> TEAMLIST = new TypeAdapter<TeamList>() {
        @Override
        public void write(JsonWriter out, TeamList value) throws IOException{
            out.beginObject();
            for(Team team : value.getTeams()){
                out.name(team.name).beginObject();
                out.name("color").value(team.color);
                out.name("lives").value(team.lives);
                out.name("member").beginArray();
                for(UUID memberId : team.teamMembers){
                    out.value(memberId.toString());
                }
                out.endArray();
                out.name("tasks").beginArray();
                for(UUID taskId : team.currentlyFinishedTasks){
                    out.value(taskId.toString());
                }
                out.endArray();
                out.name("quests").beginArray();
                for(UUID questId : team.finishedQuests){
                    out.value(questId.toString());
                }
                out.endArray();
                out.endObject();
            }
            out.endObject();
        }

        @SuppressWarnings("Duplicates")
        @Override
        public TeamList read(JsonReader in) throws IOException {
            TeamList teamList = new TeamList(new ArrayList<>());
            in.beginObject();
            while (in.hasNext()){
                String teamName = in.nextName();
                int color = 0, lives = 0;
                List<UUID> member = new ArrayList<>();
                List<UUID> tasks = new ArrayList<>();
                List<UUID> quests = new ArrayList<>();
                in.beginObject();
                while (in.hasNext()){
                    switch (in.nextName().toLowerCase()){
                        case "color": {
                            color = in.nextInt();
                            break;
                        }
                        case "lives": {
                            lives = in.nextInt();
                            break;
                        }
                        case "member": {
                            try {
                                in.beginArray();
                                while (in.hasNext()){
                                    member.add(UUID.fromString(in.nextString()));
                                }
                                in.endArray();
                                break;
                            } catch (IllegalArgumentException e){
                                System.out.println("Can't read Team data file! " + e.getMessage());
                                return null;
                            }
                        }
                        case "tasks": {
                            try {
                                in.beginArray();
                                while (in.hasNext()){
                                    tasks.add(UUID.fromString(in.nextString()));
                                }
                                in.endArray();
                                break;
                            } catch (IllegalArgumentException e){
                                System.out.println("Can't read Team data file! " + e.getMessage());
                                return null;
                            }
                        }
                        case "quests": {
                            try {
                                in.beginArray();
                                while (in.hasNext()){
                                    quests.add(UUID.fromString(in.nextString()));
                                }
                                in.endArray();
                                break;
                            } catch (IllegalArgumentException e){
                                System.out.println("Can't read Team data file! " + e.getMessage());
                                return null;
                            }
                        }
                    }
                }
                in.endObject();
                teamList.addTeam(new Team(teamName, color, lives, member).setLists(tasks, quests));
            }
            in.endObject();
            return teamList;
        }
    };

    public static final TypeAdapter<ItemStack> ITEMSTACK = new TypeAdapter<ItemStack>() {
        @Override
        public void write(JsonWriter out, ItemStack value) {}

        @Override
        public ItemStack read(JsonReader in) throws IOException {
            Item item = null;
            int meta = 0;
            int amount = 1;
            CompoundNBT nbt = new CompoundNBT();
            in.beginObject();
            while (in.hasNext()){
                switch (in.nextName().toLowerCase()){
                    case "item": {
                        item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(in.nextString()));
                        break;
                    }
                    case "meta": { // todo remove, cause of deprecation
                        meta = in.nextInt();
                        break;
                    }
                    case "amount": {
                        amount = in.nextInt();
                        break;
                    }
                    case "nbt": {
                        nbt = gson.fromJson(in, CompoundNBT.class);
                        break;
                    }
                }
            }
            in.endObject();
            if(item != null){
                ItemStack stack = new ItemStack(item, amount);
                stack.setTag(nbt);
                return stack;
            }
            return null;
        }
    };

    public static final TypeAdapter<ITask> ITASK = new TypeAdapter<ITask>() {
        @Override
        public void write(JsonWriter out, ITask value) {}

        @Override
        public ITask read(JsonReader in) throws IOException {
            String className = null;
            UUID id = null;
            CompoundNBT nbt = new CompoundNBT();
            in.beginObject();
            while (in.hasNext()){
                switch (in.nextName().toLowerCase()){
                    case "class": {
                        className = in.nextString();
                        break;
                    }
                    case "id": {
                        try {
                            id = UUID.fromString(in.nextString());
                            break;
                        } catch (IllegalArgumentException e){
                            System.out.println("Can't read task! " + e.getMessage());
                            return null;
                        }
                    }
                    case "nbt": {
                        nbt = gson.fromJson(in, CompoundNBT.class);
                        break;
                    }
                }
            }
            in.endObject();
            if(className != null && id != null){
                try {
                    Class<?> taskClass = Class.forName(className);
                    if(ITask.class.isAssignableFrom(taskClass)){
                        ITask task = (ITask) taskClass.newInstance();
                        task.init(nbt);
                        return task;
                    }
                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Can't read task!");
            return null;
        }
    };

    public static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Questbook.class, QUESTBOOK)
            .registerTypeAdapter(QuestLine.class, QUEST_LINE)
            .registerTypeAdapter(CompoundNBT.class, NBT_TAG_COMPOUND)
            .registerTypeAdapter(TeamList.class, TEAMLIST)
            .registerTypeAdapter(ItemStack.class, ITEMSTACK)
            .registerTypeAdapter(ITask.class, ITASK)
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

    public static void writeToFile(File file, Object object){
        try {
            FileWriter writer = new FileWriter(file);
            gson.toJson(object, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
