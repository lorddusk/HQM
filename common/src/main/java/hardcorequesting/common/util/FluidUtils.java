package hardcorequesting.common.util;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.architectury.fluid.FluidStack;
import hardcorequesting.common.HardcoreQuestingCore;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

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

    @Environment(EnvType.CLIENT)
    public static class CustomRenderTypes extends RenderType {
        private CustomRenderTypes(String string, VertexFormat arg, VertexFormat.Mode arg2, int i, boolean bl, boolean bl2, Runnable runnable, Runnable runnable2) {
            super(string, arg, arg2, i, bl, bl2, runnable, runnable2);
            throw new IllegalStateException("This class is not meant to be constructed!");
        }

        private static final Map<ResourceLocation, RenderType> FLUID_TYPES = new HashMap<>();
        
        /**
         * @param location the location of the texture (atlas) that the vertices' uv coordinates refer to
         */
        public static RenderType createFluid(ResourceLocation location) {
            return FLUID_TYPES.computeIfAbsent(location, CustomRenderTypes::create);
        }
        
        private static RenderType create(ResourceLocation location) {
            return RenderType.create(
                    HardcoreQuestingCore.ID + ":fluid_type",
                    DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 256, true, false,
                    RenderType.CompositeState.builder()
                            .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionTexColorShader))
                            .setLightmapState(RenderStateShard.LIGHTMAP)
                            .setTextureState(new RenderStateShard.TextureStateShard(location, false, false))
                            .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                            .createCompositeState(false));
        }
    }
}
