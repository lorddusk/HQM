package hardcorequesting.common.client.interfaces;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenu;
import hardcorequesting.common.platform.FluidStack;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import java.util.List;

@Environment(EnvType.CLIENT)
public class GuiBase extends Screen {
    public static final ResourceLocation MAP_TEXTURE = ResourceHelper.getResource("questmap");
    public static final int ITEM_SIZE = 18;
    protected static final int ITEM_SRC_Y = 235;
    protected static ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
    protected int left, top;
    
    protected GuiBase(Component title) {
        super(title);
    }
    
    public void setEditMenu(GuiEditMenu menu) {
    }
    
    public void drawRect(PoseStack matrices, int x, int y, int u, int v, int w, int h) {
        blit(matrices, x + left, y + top, u, v, w, h);
    }
    
    public void drawRect(PoseStack matrices, int x, int y, int u, int v, int w, int h, RenderRotation rotation) {
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
        bufferBuilder.vertex(matrices.last().pose(), x, y + targetH, this.getBlitOffset()).uv((float) pt1[0], (float) pt1[1]).endVertex();
        bufferBuilder.vertex(matrices.last().pose(), x + targetW, y + targetH, this.getBlitOffset()).uv((float) pt2[0], (float) pt2[1]).endVertex();
        bufferBuilder.vertex(matrices.last().pose(), x + targetW, y, this.getBlitOffset()).uv((float) pt3[0], (float) pt3[1]).endVertex();
        bufferBuilder.vertex(matrices.last().pose(), x, y, this.getBlitOffset()).uv((float) pt4[0], (float) pt4[1]).endVertex();
        tesselator.end();
    }
    
    public void renderTooltip(PoseStack matrices, FormattedText stringRenderable, int x, int y) {
        renderTooltipL(matrices, font.getSplitter().splitLines(stringRenderable, Integer.MAX_VALUE, Style.EMPTY), x, y);
    }
    
    @SuppressWarnings("unchecked")
    public void renderTooltipL(PoseStack matrices, List<? extends FormattedText> stringRenderables, int x, int y) {
        renderTooltip(matrices, Language.getInstance().getVisualOrder((List<FormattedText>) stringRenderables), x, y);
    }
    
    public void drawLine(PoseStack matrices, int x1, int y1, int x2, int y2, int thickness, int color) {
        // This block was written with insufficient knowledge on normals, adapted so that the line shows up correctly
        // Feel free to have the normals changed if you feel more knowledgeable on the subject
        if (y2 < y1) {
            drawLine(matrices, x2, y2, x1, y1, thickness, color);
            return;
        }
        int dx = x2 - x1, dy = y2 - y1;
        
        applyColor(color);
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        RenderSystem.disableTexture();
        float scale = (float) this.minecraft.getWindow().getGuiScale();
        RenderSystem.lineWidth(1 + thickness * scale / 2F);
    
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        builder.vertex(matrices.last().pose(), x1, y1, getBlitOffset()).color(255, 255, 255, 255).normal(dx, dy, 0.0F).endVertex();
        builder.vertex(matrices.last().pose(), x2, y2, getBlitOffset()).color(255, 255, 255, 255).normal(dx, dy, 0.0F).endVertex();
        tesselator.end();
        
        RenderSystem.enableTexture();
    }
    
    @Override
    public void renderTooltip(PoseStack matrices, ItemStack stack, int x, int y) {
        super.renderTooltip(matrices, stack, x, y);
    }
    
    public void drawFluid(FluidStack fluid, PoseStack stack, int x, int y, int mX, int mY) {
        drawItemBackground(stack, x, y, mX, mY, false);
        if (fluid != null) {
            drawFluid(fluid, stack, x + 1, y + 1);
        }
    }
    
    public void drawFluid(FluidStack fluid, PoseStack stack, int x, int y) {
        HardcoreQuestingCore.platform.renderFluidStack(fluid, stack, getLeft() + x, getTop() + y, getLeft() + x + 16, getTop() + y + 16);
    }
    
    public void applyColor(int color) {
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        
        RenderSystem.setShaderColor(r, g, b, a);
    }
    
    public void drawIcon(ItemStack stack, int x, int y) {
        itemRenderer.renderGuiItemDecorations(font, stack, x, y, null);
        //drawTexturedModelRectFromIcon(left + x, top + y, icon, 16, 16);
    }
    
    public void drawItemBackground(PoseStack matrices, int x, int y, int mX, int mY, boolean selected) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        
        ResourceHelper.bindResource(MAP_TEXTURE);
        
        drawRect(matrices, x, y, inBounds(x, y, ITEM_SIZE, ITEM_SIZE, mX, mY) ? ITEM_SIZE : 0, ITEM_SRC_Y, ITEM_SIZE, ITEM_SIZE);
        if (selected) {
            drawRect(matrices, x, y, ITEM_SIZE * 2, ITEM_SRC_Y, ITEM_SIZE, ITEM_SIZE);
        }
    }
    
    public void drawItemStack(PoseStack matrices, ItemStack stack, int x, int y, int mX, int mY, boolean selected) {
        drawItemBackground(matrices, x, y, mX, mY, selected);
        
        if (!stack.isEmpty()) {
            drawItemStack(stack, x + 1, y + 1, true);
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
    
    
    public void drawItemStack(@NotNull ItemStack stack, int x, int y, boolean renderEffect) {
        try {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            RenderSystem.enableDepthTest();
            
            ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
            renderer.renderAndDecorateFakeItem(stack, getLeft() + x, getTop() + y);
            renderer.renderGuiItemDecorations(font, stack, getLeft() + x, getTop() + y);
            
        } catch (Exception ignored) {
        }
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
    
    public void drawString(PoseStack matrices, FormattedText str, int x, int y, int color) {
        drawString(matrices, str, x, y, 1F, color);
    }
    
    public void drawString(PoseStack matrices, FormattedText str, int x, int y, float mult, int color) {
        drawString(matrices, str, x, y, false, mult, color);
    }
    
    public void drawString(PoseStack matrices, FormattedText str, int x, int y, boolean shadow, float mult, int color) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        matrices.pushPose();
        matrices.scale(mult, mult, 1F);
        
        MultiBufferSource.BufferSource immediate = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        font.drawInBatch(str.getString().isEmpty() ? FormattedCharSequence.EMPTY : Language.getInstance().getVisualOrder(str), (int) ((x + left) / mult), (int) ((y + top) / mult), color, shadow, matrices.last().pose(), immediate, false, 0, 15728880);
        immediate.endBatch();
        
        matrices.popPose();
    }
    
    public void drawStringWithShadow(PoseStack matrices, FormattedText str, int x, int y, float mult, int color) {
        drawString(matrices, str, x, y, true, mult, color);
    }
    
    public void drawString(PoseStack matrices, String str, int x, int y, int color) {
        drawString(matrices, str, x, y, 1F, color);
    }
    
    public void drawString(PoseStack matrices, String str, int x, int y, float mult, int color) {
        drawString(matrices, str == null ? FormattedText.EMPTY : FormattedText.of(str), x, y, mult, color);
    }
    
    public void drawStringWithShadow(PoseStack matrices, String str, int x, int y, float mult, int color) {
        drawStringWithShadow(matrices, str == null ? FormattedText.EMPTY : FormattedText.of(str), x, y, mult, color);
    }
    
    public boolean inBounds(int x, int y, int w, int h, double mX, double mY) {
        return x <= mX && mX <= x + w && y <= mY && mY <= y + h;
    }
    
    public void drawCursor(PoseStack matrices, int x, int y, int z, float size, int color) {
        matrices.pushPose();
        matrices.translate(0, 0, z);
        x += left;
        y += top;
        matrices.translate(x, y, 0);
        matrices.scale(size, size, 0);
        matrices.translate(-x, -y, 0);
        GuiComponent.fill(matrices, x, y + 1, x + 1, y + 10, color);
        matrices.popPose();
    }
    
    public void drawString(PoseStack matrices, List<FormattedText> str, int x, int y, float mult, int color) {
        drawString(matrices, str, 0, str.size(), x, y, mult, color);
    }
    
    public void drawString(PoseStack matrices, List<FormattedText> str, int start, int length, int x, int y, float mult, int color) {
        matrices.pushPose();
        matrices.scale(mult, mult, 1F);
        start = Math.max(start, 0);
        int end = Math.min(start + length, str.size());
        for (int i = start; i < end; i++) {
            font.draw(matrices, Language.getInstance().getVisualOrder(str.get(i)), (int) ((x + left) / mult), (int) ((y + top) / mult), color);
            y += font.lineHeight;
        }
        matrices.popPose();
    }
    
    public void drawCenteredString(PoseStack matrices, FormattedText str, int x, int y, float mult, int width, int height, int color) {
        drawString(matrices, str, x + (width - (int) (font.width(str) * mult)) / 2, y + (height - (int) ((font.lineHeight - 2) * mult)) / 2, mult, color);
    }
    
    public List<FormattedText> getLinesFromText(FormattedText str, float mult, int width) {
        if (str == null) {
            str = Translator.plain("Missing info");
        }
        return font.getSplitter().splitLines(str, (int) (width / mult), Style.EMPTY);
    }
}
