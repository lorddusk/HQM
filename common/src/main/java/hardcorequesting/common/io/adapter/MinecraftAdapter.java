package hardcorequesting.common.io.adapter;

import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.platform.FluidStack;
import hardcorequesting.common.util.Fraction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

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
            Streams.write(Dynamic.convert(NbtOps.INSTANCE, JsonOps.INSTANCE, stack.save(new CompoundTag())), out);
        }
        
        @Override
        public ItemStack read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return ItemStack.EMPTY;
            }
            return ItemStack.of((CompoundTag) Dynamic.convert(JsonOps.INSTANCE, NbtOps.INSTANCE, Streams.parse(in)));
        }
    };
    public static final TypeAdapter<FluidStack> FLUID = new TypeAdapter<FluidStack>() {
        @Override
        public void write(JsonWriter out, FluidStack value) throws IOException {
            JsonObject object = new JsonObject();
            object.addProperty("fluid", Registry.FLUID.getKey(value.getFluid()).toString());
            object.add("volume", Dynamic.convert(NbtOps.INSTANCE, JsonOps.INSTANCE, value.getAmount().toNbt()));
            Streams.write(object, out);
        }
        
        @Override
        public FluidStack read(JsonReader in) throws IOException {
            JsonObject object = Streams.parse(in).getAsJsonObject();
            Fluid fluid = Registry.FLUID.get(new ResourceLocation(object.get("fluid").getAsString()));
            Fraction amount = Fraction.fromNbt((CompoundTag) Dynamic.convert(JsonOps.INSTANCE, NbtOps.INSTANCE, object.get("volume")));
            return HardcoreQuestingCore.platform.createFluidStack(fluid, amount);
        }
    };
}
