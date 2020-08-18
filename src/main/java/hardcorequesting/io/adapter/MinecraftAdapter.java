package hardcorequesting.io.adapter;

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.IOException;

/**
 * Created by lang2 on 10/12/2015.
 */
public class MinecraftAdapter {
    
    public static final TypeAdapter<CompoundTag> NBT_TAG_COMPOUND = new TypeAdapter<CompoundTag>() {
        @Override
        public void write(JsonWriter out, CompoundTag value) throws IOException {
            Streams.write(Dynamic.convert(NbtOps.INSTANCE, JsonOps.INSTANCE, value), out);
        }
        
        @Override
        public CompoundTag read(JsonReader in) {
            return (CompoundTag) Dynamic.convert(JsonOps.INSTANCE, NbtOps.INSTANCE, Streams.parse(in));
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
            Streams.write(Dynamic.convert(NbtOps.INSTANCE, JsonOps.INSTANCE, stack.toTag(new CompoundTag())), out);
        }
        
        @Override
        public ItemStack read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return ItemStack.EMPTY;
            }
            return ItemStack.fromTag((CompoundTag) Dynamic.convert(JsonOps.INSTANCE, NbtOps.INSTANCE, Streams.parse(in)));
        }
    };
    public static final TypeAdapter<FluidVolume> FLUID = new TypeAdapter<FluidVolume>() {
        @Override
        public void write(JsonWriter out, FluidVolume value) throws IOException {
            JsonObject object = new JsonObject();
            object.addProperty("fluid", Registry.FLUID.getId(value.getRawFluid()).toString());
            object.add("volume", Dynamic.convert(NbtOps.INSTANCE, JsonOps.INSTANCE, value.getAmount_F().toNbt()));
            Streams.write(object, out);
        }
        
        @Override
        public FluidVolume read(JsonReader in) throws IOException {
            JsonObject object = Streams.parse(in).getAsJsonObject();
            Fluid fluid = Registry.FLUID.get(new Identifier(object.get("fluid").getAsString()));
            FluidAmount amount = FluidAmount.fromNbt((CompoundTag) Dynamic.convert(JsonOps.INSTANCE, NbtOps.INSTANCE, object.get("volume")));
            return FluidKeys.get(fluid).withAmount(amount);
        }
    };
}
