package hardcorequesting.common.util;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.architectury.fluid.FluidStack;
import hardcorequesting.common.HardcoreQuestingCore;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;


public class FluidUtils {
    public static FluidStack split(FluidStack fluid, Fraction toRemove) {
        if (toRemove.isLessThan(Fraction.empty())) {
            throw new IllegalArgumentException("Cannot split off a negative amount!");
        }
        if (toRemove.equals(Fraction.empty()) || fluid.isEmpty()) {
            return FluidStack.empty();
        }
        if (toRemove.isGreaterThan(FluidUtils.getAmount(fluid))) {
            toRemove = FluidUtils.getAmount(fluid);
        }
        return performSplit(fluid, toRemove);
    }

    public static FluidStack performSplit(FluidStack stack, Fraction toTake) {
        stack.setAmount(getAmount(stack).minus(toTake).intValue());
        return FluidStack.create(stack, toTake.intValue());
    }

    public static Fraction getAmount(FluidStack stack) {
        return Fraction.ofWhole(stack.getAmount());
    }

    public static Material getBlockMaterial(ResourceLocation loc) {
        return new Material(TextureAtlas.LOCATION_BLOCKS, loc);
    }

    @Environment(EnvType.CLIENT)
    public static class CustomRenderTypes extends RenderType {
        private CustomRenderTypes(String string, VertexFormat arg, VertexFormat.Mode arg2, int i, boolean bl, boolean bl2, Runnable runnable, Runnable runnable2) {
            super(string, arg, arg2, i, bl, bl2, runnable, runnable2);
            throw new IllegalStateException("This class is not meant to be constructed!");
        }

        public static RenderType createFluid(ResourceLocation location) {
            return RenderType.create(
                    HardcoreQuestingCore.ID + ":fluid_type",
                    DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS, 256, true, false,
                    RenderType.CompositeState.builder()
                            .setShaderState(RenderStateShard.POSITION_COLOR_TEX_SHADER)
                            .setLightmapState(RenderStateShard.LIGHTMAP)
                            .setTextureState(new RenderStateShard.TextureStateShard(location, false, false))
                            .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                            .createCompositeState(false));
        }
    }
}
