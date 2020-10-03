package hardcorequesting.common.io.adapter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.platform.FluidStack;
import hardcorequesting.common.util.Fraction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

/**
 * Created by lang2 on 10/12/2015.
 */
public class MinecraftAdapter {
    public static final Adapter<ItemStack> ITEM_STACK = new Adapter<ItemStack>() {
        private static final String ID = "id";
        private static final String DAMAGE = "damage";
        private static final String STACK_SIZE = "amount";
        private static final String NBT = "nbt";
        
        @Override
        public JsonElement serialize(ItemStack src) {
            if (src.isEmpty())
                return nullVal();
            
            return Dynamic.convert(NbtOps.INSTANCE, JsonOps.INSTANCE, src.save(new CompoundTag()));
        }
        
        @Override
        public ItemStack deserialize(JsonElement json) {
            if (json.isJsonNull())
                return ItemStack.EMPTY;
            return ItemStack.of((CompoundTag) Dynamic.convert(JsonOps.INSTANCE, NbtOps.INSTANCE, json));
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
}
