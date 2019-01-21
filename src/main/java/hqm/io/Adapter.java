package hqm.io;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.nbt.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author canitzp
 * @since HQM 6
 */
public class Adapter{
    
    public static final TypeAdapter<NBTBase> NBT_BASE = new TypeAdapter<NBTBase>() {
    
        @Override
        public void write(JsonWriter out, NBTBase value) throws IOException{
            Gson gson = new Gson();
            out.beginObject();
            out.name("id").value(value.getId());
            out.name("value");
            switch(value.getId()){
                case 1: {
                    out.value(((NBTTagByte) value).getByte());
                    break;
                }
                case 2: {
                    out.value(((NBTTagShort) value).getShort());
                    break;
                }
                case 3: {
                    out.value(((NBTTagInt) value).getInt());
                    break;
                }
                case 4: {
                    out.value(((NBTTagLong) value).getLong());
                    break;
                }
                case 5: {
                    out.value(((NBTTagFloat) value).getFloat());
                    break;
                }
                case 6: {
                    out.value(((NBTTagDouble) value).getDouble());
                    break;
                }
                case 7: {
                    gson.toJson(((NBTTagByteArray) value).getByteArray(), byte[].class, out);
                    break;
                }
                case 8: {
                    out.value(((NBTTagString) value).getString());
                    break;
                }
                case 9: {
                    out.beginArray();
                    NBTTagList list = ((NBTTagList) value);
                    for(int i = 0; i < list.tagCount(); i++){
                        write(out, list.get(i));
                    }
                    out.endArray();
                    break;
                }
                case 10: {
                    NBT_TAG_COMPOUND.write(out, (NBTTagCompound) value);
                    break;
                }
                case 11: {
                    gson.toJson(((NBTTagIntArray) value).getIntArray(), int[].class, out);
                    break;
                }
                case 12: {
                    long[] data = new long[0];
                    Field field = NBTTagLongArray.class.getDeclaredFields()[0];
                    field.setAccessible(true);
                    try{
                        Object o = field.get(value);
                        if(o instanceof long[]){
                            data = (long[]) o;
                        }
                    }catch(IllegalAccessException ignored){}
                    gson.toJson(data, long[].class, out);
                    break;
                }
            }
            out.endObject();
        }
    
        @Override
        public NBTBase read(JsonReader in) throws IOException{
            Gson gson = new Gson();
            NBTBase nbtBase = null;
            in.beginObject();
            in.skipValue(); // skipping the name "id" here
            int index = in.nextInt();
            in.skipValue(); // skipping the name "value" here
            switch(index){
                case 1:{
                    nbtBase = new NBTTagByte((byte) in.nextInt());
                    break;
                }
                case 2:{
                    nbtBase = new NBTTagShort((short) in.nextInt());
                    break;
                }
                case 3:{
                    nbtBase = new NBTTagInt(in.nextInt());
                    break;
                }
                case 4:{
                    nbtBase = new NBTTagLong(in.nextLong());
                    break;
                }
                case 5:{
                    nbtBase = new NBTTagFloat((float) in.nextDouble());
                    break;
                }
                case 6:{
                    nbtBase = new NBTTagDouble(in.nextDouble());
                    break;
                }
                case 7:{
                    nbtBase = new NBTTagByteArray((byte[]) gson.fromJson(in, byte[].class));
                    break;
                }
                case 8:{
                    nbtBase = new NBTTagString(in.nextString());
                    break;
                }
                case 9:{
                    nbtBase = new NBTTagList();
                    List<NBTBase> content = new ArrayList<>();
                    in.beginArray();
                    while(in.hasNext()){
                        content.add(read(in));
                    }
                    in.endArray();
                    content.forEach(((NBTTagList) nbtBase)::appendTag);
                    break;
                }
                case 10:{
                    nbtBase = NBT_TAG_COMPOUND.read(in);
                    break;
                }
                case 11:{
                    nbtBase = new NBTTagIntArray((int[]) gson.fromJson(in, int[].class));
                    break;
                }
                case 12:{
                    nbtBase = new NBTTagLongArray((long[]) gson.fromJson(in, long[].class));
                    break;
                }
            }
            in.endObject();
            return nbtBase;
        }
    };
    
    public static final TypeAdapter<NBTTagCompound> NBT_TAG_COMPOUND = new TypeAdapter<NBTTagCompound>() {
        @Override
        public void write(JsonWriter out, NBTTagCompound nbt) throws IOException{
            out.beginObject();
            for(String key : nbt.getKeySet()){
                NBTBase value = nbt.getTag(key);
                out.name(key);
                NBT_BASE.write(out, value);
            }
            out.endObject();
        }
        
        @Override
        public NBTTagCompound read(JsonReader in) throws IOException{
            NBTTagCompound nbt = new NBTTagCompound();
            in.beginObject();
            while(in.hasNext()){
                String key = in.nextName();
                NBTBase value = NBT_BASE.read(in);
                if(value != null){
                    nbt.setTag(key, value);
                } else {
                    System.out.println("Can't read value for key '" + key + "'");
                }
            }
            in.endObject();
            return nbt;
        }
    };
    
}
