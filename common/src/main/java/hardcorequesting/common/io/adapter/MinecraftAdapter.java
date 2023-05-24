package hardcorequesting.common.io.adapter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import dev.architectury.fluid.FluidStack;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.util.FluidUtils;
import hardcorequesting.common.util.Fraction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
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
    public static final Adapter<ItemStack> ITEM_STACK = new Adapter<>() {
        
        @Override
        public JsonElement serialize(ItemStack src) {
            if (src.isEmpty())
                return nullVal();
            
            JsonObject jsonObj = new JsonObject();
            jsonObj.addProperty("id", BuiltInRegistries.ITEM.getKey(src.getItem()).toString());
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
                Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(GsonHelper.getAsString(jsonObj, "id")));
                int count = GsonHelper.getAsByte(jsonObj, "Count");
                ItemStack stack = new ItemStack(item, count);
                
                if (jsonObj.has("tag")) {
                    stack.setTag(COMPOUND_TAG.deserialize(jsonObj.get("tag")));
                }
                
                return stack;
            }
        }
    };
    // A more restrictive version of ITEM_STACK that caps stack sizes at 1
    public static final Adapter<ItemStack> ICON_ITEM_STACK = new Adapter<>() {
        
        @Override
        public JsonElement serialize(ItemStack src) {
            if (src.getCount() > 1) {
                src = src.copy();
                src.setCount(1);
            }
            return ITEM_STACK.serialize(src);
        }
        
        @Override
        public ItemStack deserialize(JsonElement json) {
            ItemStack stack = ITEM_STACK.deserialize(json);
            if (stack.getCount() > 1)
                stack.setCount(1);
            return stack;
        }
    };
    public static final Adapter<FluidStack> FLUID = new Adapter<>() {
        private static final String FLUID = "fluid";
        private static final String VOLUME = "volume";
        
        @Override
        public JsonElement serialize(FluidStack src) {
            return object()
                    .add(FLUID, BuiltInRegistries.FLUID.getKey(src.getFluid()).toString())
                    .add(VOLUME, Fraction.CODEC.encodeStart(JsonOps.INSTANCE, FluidUtils.getAmount(src)).result().orElseThrow())
                    .build();
        }
        
        @Override
        public FluidStack deserialize(JsonElement json) {
            JsonObject object = json.getAsJsonObject();
            
            Fluid fluid = BuiltInRegistries.FLUID.get(new ResourceLocation(GsonHelper.getAsString(object, FLUID)));
            Fraction amount = Fraction.CODEC.parse(JsonOps.INSTANCE, object.get(VOLUME)).result().orElseThrow();
            return FluidStack.create(fluid, amount.intValue());
        }
    };
    
    // Converting a json element to a nbt tag using dynamic ops is unsafe because json does not cover the type system that nbt has.
    // Use this adapter as a safer alternative. It stores the entire tag as a json string, ensuring that nbt types are kept.
    // Imitates the serialization method used by SetNbtFunction.
    public static final Adapter<CompoundTag> COMPOUND_TAG =  new Adapter<>() {
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
