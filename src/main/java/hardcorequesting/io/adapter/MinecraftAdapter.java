package hardcorequesting.io.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import java.io.IOException;

/**
 * Created by lang2 on 10/12/2015.
 */
public class MinecraftAdapter {

    public static final TypeAdapter<NBTTagCompound> NBT_TAG_COMPOUND = new TypeAdapter<NBTTagCompound>() {
        @Override
        public void write(JsonWriter out, NBTTagCompound value) throws IOException {
            out.value(value.toString());
        }

        @Override
        public NBTTagCompound read(JsonReader in) throws IOException {
            try {
                return JsonToNBT.getTagFromJson(in.nextString());
            } catch (NBTException e) {
                throw new IOException("Failed to read NBT", e);
            }
        }
    };
    public static final TypeAdapter<ItemStack> ITEM_STACK = new TypeAdapter<ItemStack>() {
        private static final String ID = "id";
        private static final String DAMAGE = "damage";
        private static final String STACK_SIZE = "amount";
        private static final String NBT = "nbt";

        @Override
        public void write(JsonWriter out, ItemStack stack) throws IOException {
            if (stack.isEmpty()) {
                out.nullValue();
                return;
            }
            String id = stack.getItem().getRegistryName().toString();
            out.beginObject();
            out.name(ID).value(id);
            if (stack.getItemDamage() != 0) {
                out.name(DAMAGE).value(stack.getItemDamage());
            }
            if (stack.getCount() != 1) {
                out.name(STACK_SIZE).value(stack.getCount());
            }
            if (stack.hasTagCompound() && !stack.getTagCompound().isEmpty()) {
                NBT_TAG_COMPOUND.write(out.name(NBT), stack.getTagCompound());
            }
            out.endObject();
        }

        @Override
        public ItemStack read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                return ItemStack.EMPTY;
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

            Item item = Item.getByNameOrId(id);
            if (item == null) {
                return ItemStack.EMPTY;
            }
            ItemStack stack = new ItemStack(item, size, damage);
            stack.setTagCompound(tag);
            return stack;
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
