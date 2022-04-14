package hardcorequesting.common.io.adapter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.platform.FluidStack;
import hardcorequesting.common.util.Fraction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by lang2 on 10/12/2015.
 */
public class MinecraftAdapter {
    public static final Adapter<ItemStack> ITEM_STACK = new Adapter<ItemStack>() {
        @Override
        public JsonElement serialize(ItemStack src) {
            if (src.isEmpty())
                return nullVal();
            
            JsonObject jsonObj = new JsonObject();
            jsonObj.addProperty("id", Registry.ITEM.getKey(src.getItem()).toString());
            jsonObj.addProperty("Count", src.getCount());
            
            CompoundTag tag = src.getTag();
            if (tag != null) {
                jsonObj.add("tag", COMPOUND_TAG.serialize(tag));
            }
            
            return jsonObj;
        }
        
        @Override
        @NotNull
        public ItemStack deserialize(JsonElement json) {
            if (json.isJsonNull())
                return ItemStack.EMPTY;
            else {
                JsonObject jsonObj = json.getAsJsonObject();
                Item item = Registry.ITEM.get(new ResourceLocation(GsonHelper.getAsString(jsonObj, "id")));
                int count = GsonHelper.getAsInt(jsonObj, "Count", 1);
                ItemStack stack = new ItemStack(item, count);
                
                if (jsonObj.has("tag")) {
                    stack.setTag(COMPOUND_TAG.deserialize(jsonObj.get("tag")));
                }
                
                return stack;
            }
        }
    };
    public static final Adapter<FluidStack> FLUID = new Adapter<FluidStack>() {
        private static final String FLUID = "fluid";
        private static final String VOLUME = "volume";
        
        @Override
        public JsonElement serialize(FluidStack src) {
            return object()
                    .add(FLUID, Registry.FLUID.getKey(src.getFluid()).toString())
                    .add(VOLUME, Dynamic.convert(NbtOps.INSTANCE, JsonOps.INSTANCE, src.getAmount().toNbt()))
                    .build();
        }
        
        @Override
        public FluidStack deserialize(JsonElement json) {
            JsonObject object = json.getAsJsonObject();
            
            Fluid fluid = Registry.FLUID.get(new ResourceLocation(GsonHelper.getAsString(object, FLUID)));
            Fraction amount = Fraction.fromNbt((CompoundTag) Dynamic.convert(JsonOps.INSTANCE, NbtOps.INSTANCE, object.get(VOLUME)));
            return HardcoreQuestingCore.platform.createFluidStack(fluid, amount);
        }
    };
    
    // Converting a json element to a nbt tag using dynamic ops is unsafe because json does not cover the type system that nbt has.
    // Use this adapter as a safer alternative. It stores the entire tag as a json string, ensuring that nbt types are kept.
    // Imitates the serialization method used by SetNbtFunction.
    public static final Adapter<CompoundTag> COMPOUND_TAG =  new Adapter<CompoundTag>() {
        @Override
        public JsonElement serialize(CompoundTag src) {
            return new JsonPrimitive(src.toString());
        }
    
        @Override
        public @Nullable CompoundTag deserialize(JsonElement json) {
            if (json.isJsonObject()) {   //Backwards-compatibility with HQM-1.18.2-5.10.0 and earlier
                return (CompoundTag) Dynamic.convert(JsonOps.INSTANCE, PatchedNbtOps.INSTANCE, json);
            } else {
                try {
                    return TagParser.parseTag(GsonHelper.convertToString(json, "tag"));
                } catch (CommandSyntaxException e) {
                    throw new JsonSyntaxException(e.getMessage());
                }
            }
        }
    };
}
