package hardcorequesting.io.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import java.io.IOException;

/**
 * Created by lang2 on 10/12/2015.
 */
public class MinecraftAdapter {
    public static final TypeAdapter<ItemStack> ITEM_STACK = new TypeAdapter<ItemStack>() {
        private static final String ID = "id";
        private static final String DAMAGE = "damage";
        private static final String STACK_SIZE = "amount";
        private static final String NBT = "nbt";

        @Override
        public void write(JsonWriter out, ItemStack value) throws IOException {
            if (value == null || value.getItem() == null) {
                out.nullValue();
                return;
            }
            String id = Item.itemRegistry.getNameForObject(value.getItem());
            out.beginObject();
            out.name(ID).value(id);
            if (value.getMetadata() != 0) {
                out.name(DAMAGE).value(value.getMetadata());
            }
            if (value.stackSize != 1) {
                out.name(STACK_SIZE).value(value.stackSize);
            }
            if (value.hasTagCompound() && !value.getTagCompound().hasNoTags()) {
                NBT_TAG_COMPOUND.write(out.name(NBT), value.getTagCompound());
            }
            out.endObject();
        }

        @Override
        public ItemStack read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                return null;
            }
            String id = "";
            int damage = 0, size = 1;
            NBTTagCompound tag = null;
            in.beginObject();
            while (in.hasNext()) {
                String name = in.nextName();
                if (name.equalsIgnoreCase(ID)) {
                    id = in.nextString();
                } else if (name.equalsIgnoreCase(DAMAGE)) {
                    damage = in.nextInt();
                } else if (name.equalsIgnoreCase(STACK_SIZE)) {
                    size = in.nextInt();
                } else if (name.equalsIgnoreCase(NBT)) {
                    tag = NBT_TAG_COMPOUND.read(in);
                }
            }
            in.endObject();

            Item item = (Item) Item.itemRegistry.getObject(id);
            if (item == null) {
                return null;
            }
            ItemStack result = new ItemStack(item, size, damage);
            result.setTagCompound(tag);
            return result;
        }
    };

    public static final TypeAdapter<NBTTagCompound> NBT_TAG_COMPOUND = new TypeAdapter<NBTTagCompound>() {
        @Override
        public void write(JsonWriter out, NBTTagCompound value) throws IOException {
            out.value(value.toString());
        }

        @Override
        public NBTTagCompound read(JsonReader in) throws IOException {
            try {
                NBTBase nbtBase = JsonToNBT.func_150315_a(in.nextString());
                if (nbtBase instanceof NBTTagCompound) {
                    return (NBTTagCompound) nbtBase;
                }
            } catch (Exception ignored) {
            }
            throw new IOException("Failed to read NBT");
        }
    };

    public static final TypeAdapter<Fluid> FLUID = new TypeAdapter<Fluid>() {
        @Override
        public void write(JsonWriter out, Fluid value) throws IOException {
            out.value(value.getName());
        }

        @Override
        public Fluid read(JsonReader in) throws IOException {
            return FluidRegistry.getFluid(in.nextString());
        }
    };
}
