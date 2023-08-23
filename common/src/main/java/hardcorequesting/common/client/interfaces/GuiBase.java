package hardcorequesting.common.client.interfaces;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.FluidStackHooks;
import hardcorequesting.common.util.FluidUtils;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.Collection;
import java.util.List;

@Environment(EnvType.CLIENT)
public class GuiBase extends Screen {
    public static final ResourceLocation MAP_TEXTURE = ResourceHelper.getResource("questmap");
    public static final int TEXT_HEIGHT = 9;
    public static final int ITEM_SIZE = 18;
    protected static final int ITEM_SRC_Y = 235;
    protected int left, top;
    
    protected GuiBase(Component title) {
        super(title);
    }

    public void drawRect(GuiGraphics graphics, ResourceLocation texture, int x, int y, int u, int v, int w, int h) {
        graphics.blit(texture, x + left, y + top, u, v, w, h);
    }
    
    public void drawRect(GuiGraphics graphics, int x, int y, int u, int v, int w, int h, RenderRotation rotation) {
        boolean rotate = rotation == RenderRotation.ROTATE_90 || rotation == RenderRotation.ROTATE_270 || rotation == RenderRotation.ROTATE_90_FLIP || rotation == RenderRotation.ROTATE_270_FLIP;
        
        int targetW = rotate ? h : w;
        int targetH = rotate ? w : h;
        
        x += left;
        y += top;
        
        float fw = 1/256F;
        float fy = 1/256F;
        
        double a = (float) (u) * fw;
        double b = (float) (u + w) * fw;
        double c = (float) (v + h) * fy;
        double d = (float) (v) * fy;
        
        double[] ptA = new double[]{a, c};
        double[] ptB = new double[]{b, c};
        double[] ptC = new double[]{b, d};
        double[] ptD = new double[]{a, d};
        
        
        double[] pt1, pt2, pt3, pt4;
        
        switch (rotation) {
            default:
            case NORMAL:
                pt1 = ptA;
                pt2 = ptB;
                pt3 = ptC;
                pt4 = ptD;
                break;
            case ROTATE_90:
                pt1 = ptB;
                pt2 = ptC;
                pt3 = ptD;
                pt4 = ptA;
                break;
            case ROTATE_180:
                pt1 = ptC;
                pt2 = ptD;
                pt3 = ptA;
                pt4 = ptB;
                break;
            case ROTATE_270:
                pt1 = ptD;
                pt2 = ptA;
                pt3 = ptB;
                pt4 = ptC;
                break;
            
            case FLIP_HORIZONTAL:
                pt1 = ptB;
                pt2 = ptA;
                pt3 = ptD;
                pt4 = ptC;
                break;
            case ROTATE_90_FLIP:
                pt1 = ptA;
                pt2 = ptD;
                pt3 = ptC;
                pt4 = ptB;
                break;
            case FLIP_VERTICAL:
                pt1 = ptD;
                pt2 = ptC;
                pt3 = ptB;
                pt4 = ptA;
                break;
            case ROTATE_270_FLIP:
                pt1 = ptC;
                pt2 = ptB;
                pt3 = ptA;
                pt4 = ptD;
                break;
        }
        
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(graphics.pose().last().pose(), x, y + targetH, 0).uv((float) pt1[0], (float) pt1[1]).endVertex();
        bufferBuilder.vertex(graphics.pose().last().pose(), x + targetW, y + targetH, 0).uv((float) pt2[0], (float) pt2[1]).endVertex();
        bufferBuilder.vertex(graphics.pose().last().pose(), x + targetW, y, 0).uv((float) pt3[0], (float) pt3[1]).endVertex();
        bufferBuilder.vertex(graphics.pose().last().pose(), x, y, 0).uv((float) pt4[0], (float) pt4[1]).endVertex();
        tesselator.end();
    }
    
    public void renderTooltip(GuiGraphics graphics, FormattedText stringRenderable, int x, int y) {
        renderTooltipL(graphics, font.getSplitter().splitLines(stringRenderable, Integer.MAX_VALUE, Style.EMPTY), x, y);
    }
    
    @SuppressWarnings("unchecked")
    public void renderTooltipL(GuiGraphics graphics, List<? extends FormattedText> stringRenderables, int x, int y) {
        graphics.renderTooltip(font, Language.getInstance().getVisualOrder((List<FormattedText>) stringRenderables), x, y);
    }
    
    public void drawLine(GuiGraphics graphics, int x1, int y1, int x2, int y2, int thickness, int color) {
        // This block was written with insufficient knowledge on normals, adapted so that the line shows up correctly
        // Feel free to have the normals changed if you feel more knowledgeable on the subject
        if (y2 < y1) {
            drawLine(graphics, x2, y2, x1, y1, thickness, color);
            return;
        }
        int dx = x2 - x1, dy = y2 - y1;
        
        applyColor(color);
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        float scale = (float) this.minecraft.getWindow().getGuiScale();
        RenderSystem.lineWidth(1 + thickness * scale / 2F);
    
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        builder.vertex(graphics.pose().last().pose(), x1, y1, 0).color(255, 255, 255, 255).normal(dx, dy, 0.0F).endVertex();
        builder.vertex(graphics.pose().last().pose(), x2, y2, 0).color(255, 255, 255, 255).normal(dx, dy, 0.0F).endVertex();
        tesselator.end();
    }
    
    public void drawFluid(FluidStack fluid, GuiGraphics graphics, int x, int y, int mX, int mY) {
        drawItemBackground(graphics, x, y, mX, mY, false);
        if (fluid != null) {
            drawFluid(fluid, graphics.pose(), x + 1, y + 1);
        }
    }
    
    public void drawFluid(FluidStack fluid, PoseStack stack, int x, int y) {
        stack.pushPose();
        stack.translate(0, 0, 0);
        int x1 = getLeft() + x;
        int y1 = getTop() + y;
        int x2 = getLeft() + x + 16;
        int y2 = getTop() + y + 16;
        TextureAtlasSprite sprite = FluidStackHooks.getStillTexture(fluid);
        int color = FluidStackHooks.getColor(fluid);
        int a = 255;
        int r = (color >> 16 & 255);
        int g = (color >> 8 & 255);
        int b = (color & 255);
        MultiBufferSource.BufferSource source = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer builder = FluidUtils.getBlockMaterial(sprite.atlasLocation()).buffer(source, FluidUtils.CustomRenderTypes::createFluid);
        Matrix4f matrix = stack.last().pose();
        builder.vertex(matrix, x2, y1, 0).color(r, g, b, a).uv(sprite.getU1(), sprite.getV0()).endVertex();
        builder.vertex(matrix, x1, y1, 0).color(r, g, b, a).uv(sprite.getU0(), sprite.getV0()).endVertex();
        builder.vertex(matrix, x1, y2, 0).color(r, g, b, a).uv(sprite.getU0(), sprite.getV1()).endVertex();
        builder.vertex(matrix, x2, y2, 0).color(r, g, b, a).uv(sprite.getU1(), sprite.getV1()).endVertex();
        source.endBatch();
        stack.popPose();
    }
    
    public void applyColor(int color) {
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        
        RenderSystem.setShaderColor(r, g, b, a);
    }
    
    public void drawItemBackground(GuiGraphics graphics, int x, int y, int mX, int mY, boolean selected) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        drawRect(graphics, MAP_TEXTURE, x, y, inBounds(x, y, ITEM_SIZE, ITEM_SIZE, mX, mY) ? ITEM_SIZE : 0, ITEM_SRC_Y, ITEM_SIZE, ITEM_SIZE);
        if (selected) {
            drawRect(graphics, MAP_TEXTURE, x, y, ITEM_SIZE * 2, ITEM_SRC_Y, ITEM_SIZE, ITEM_SIZE);
        }
    }
    
    public void drawItemStack(GuiGraphics graphics, ItemStack stack, int x, int y, int mX, int mY, boolean selected) {
        drawItemBackground(graphics, x, y, mX, mY, selected);
        
        if (!stack.isEmpty()) {
            drawItemStack(graphics, stack, x + 1, y + 1, true);
            //itemRenderer.renderItemOverlayIntoGUI(fontRenderer, stack, x + left + 1, y + +top + 1, "");
        }
        //GlStateManager.disableLighting();
        //GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
    
    
    protected void setColor(int color) {
        float[] colorComponents = new float[3];
        for (int i = 0; i < colorComponents.length; i++) {
            colorComponents[i] = ((color & (255 << (i * 8))) >> (i * 8)) / 255F;
        }
        RenderSystem.setShaderColor(colorComponents[2], colorComponents[1], colorComponents[0], 1F);
    }
    
    
    public void drawItemStack(GuiGraphics graphics, @NotNull ItemStack stack, int x, int y, boolean renderEffect) {
        graphics.renderItem(stack, getLeft() + x, getTop() + y);
        graphics.renderItemDecorations(font, stack, getLeft() + x, getTop() + y);
    }
    
    public int getLeft() {
        return left;
    }
    
    public int getTop() {
        return top;
    }
    
    public int getStringWidth(String txt) {
        return font.width(txt);
    }
    
    public int getStringWidth(FormattedText txt) {
        return font.width(txt);
    }
    
    public void drawString(GuiGraphics graphics, FormattedText str, int x, int y, int color) {
        drawString(graphics, str, x, y, 1F, color);
    }
    
    public void drawString(GuiGraphics graphics, FormattedText str, int x, int y, float mult, int color) {
        drawString(graphics, str, x, y, false, mult, color);
    }
    
    public void drawString(GuiGraphics graphics, FormattedText str, int x, int y, boolean shadow, float mult, int color) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        graphics.pose().pushPose();
        graphics.pose().scale(mult, mult, 1F);
        
        FormattedCharSequence text = str.getString().isEmpty() ? FormattedCharSequence.EMPTY : Language.getInstance().getVisualOrder(str);
        float drawX = (x + this.left) / mult;
        float drawY = (y + this.top) / mult;
        MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        this.font.drawInBatch(text, drawX, drawY, color, shadow, graphics.pose().last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
        bufferSource.endBatch();

        graphics.pose().popPose();
    }
    
    public void drawStringWithShadow(GuiGraphics graphics, FormattedText str, int x, int y, float mult, int color) {
        drawString(graphics, str, x, y, true, mult, color);
    }
    
    public void drawString(GuiGraphics graphics, String str, int x, int y, int color) {
        drawString(graphics, str, x, y, 1F, color);
    }
    
    public void drawString(GuiGraphics graphics, String str, int x, int y, float mult, int color) {
        drawString(graphics, str == null ? FormattedText.EMPTY : FormattedText.of(str), x, y, mult, color);
    }
    
    public void drawStringWithShadow(GuiGraphics graphics, String str, int x, int y, float mult, int color) {
        drawStringWithShadow(graphics, str == null ? FormattedText.EMPTY : FormattedText.of(str), x, y, mult, color);
    }
    
    public boolean inBounds(int x, int y, int w, int h, double mX, double mY) {
        return x <= mX && mX <= x + w && y <= mY && mY <= y + h;
    }
    
    public void drawCursor(GuiGraphics graphics, int x, int y, int z, float size, int color) {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(0, 0, z);
        x += left;
        y += top;
        pose.translate(x, y, 0);
        pose.scale(size, size, 0);
        pose.translate(-x, -y, 0);
        graphics.fill(x, y + 1, x + 1, y + 10, color);
        pose.popPose();
    }
    
    public void drawSelection(GuiGraphics graphics, Collection<Rect2i> areas) {
        PoseStack matrices = graphics.pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(0.0F, 0.0F, 255.0F, 255.0F);
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        
        for (Rect2i area : areas) {
            int x0 = area.getX() + left;
            int y0 = area.getY() + top;
            int x1 = x0 + area.getWidth();
            int y1 = y0 + area.getHeight();
            bufferBuilder.vertex(matrices.last().pose(), x0, y1, 0F).endVertex();
            bufferBuilder.vertex(matrices.last().pose(), x1, y1, 0F).endVertex();
            bufferBuilder.vertex(matrices.last().pose(), x1, y0, 0F).endVertex();
            bufferBuilder.vertex(matrices.last().pose(), x0, y0, 0F).endVertex();
        }
        
        tesselator.end();
        RenderSystem.disableColorLogicOp();
    }
    
    public void drawString(GuiGraphics graphics, List<FormattedText> str, int x, int y, float mult, int color) {
        drawString(graphics, str, 0, str.size(), x, y, mult, color);
    }
    
    public void drawString(GuiGraphics graphics, List<FormattedText> str, int start, int length, int x, int y, float mult, int color) {
        graphics.pose().pushPose();
        graphics.pose().scale(mult, mult, 1F);
        start = Math.max(start, 0);
        int end = Math.min(start + length, str.size());
        for (int i = start; i < end; i++) {
            graphics.drawString(font, Language.getInstance().getVisualOrder(str.get(i)), (int) ((x + left) / mult), (int) ((y + top) / mult), color, false);
            y += font.lineHeight;
        }
        graphics.pose().popPose();
    }
    
    public void drawCenteredString(GuiGraphics graphics, FormattedText str, int x, int y, float mult, int width, int height, int color) {
        drawString(graphics, str, x + (width - (int) (font.width(str) * mult)) / 2, y + (height - (int) ((font.lineHeight - 2) * mult)) / 2, mult, color);
    }
    
    public List<FormattedText> getLinesFromText(FormattedText str, float mult, int width) {
        if (str == null) {
            str = Translator.plain("Missing info");
        }
        return font.getSplitter().splitLines(str, (int) (width / mult), Style.EMPTY);
    }
    
    public Font getFont() {
        return font;
    }
}
