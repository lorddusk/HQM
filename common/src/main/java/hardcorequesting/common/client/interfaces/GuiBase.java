package hardcorequesting.common.client.interfaces;

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
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
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
    
    public void drawRect(int x, int y, int u, int v, int w, int h) {
        drawRect(x, y, u, v, w, h, RenderRotation.NORMAL);
    }
    
    public void drawRect(int x, int y, int u, int v, int w, int h, RenderRotation rotation) {
        boolean rotate = rotation == RenderRotation.ROTATE_90 || rotation == RenderRotation.ROTATE_270 || rotation == RenderRotation.ROTATE_90_FLIP || rotation == RenderRotation.ROTATE_270_FLIP;
        
        int targetW = rotate ? h : w;
        int targetH = rotate ? w : h;
        
        x += left;
        y += top;
        
        float fw = 0.00390625F;
        float fy = 0.00390625F;
        
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
        
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(x, y + targetH, this.getBlitOffset()).uv((float) pt1[0], (float) pt1[1]).endVertex();
        bufferBuilder.vertex(x + targetW, y + targetH, this.getBlitOffset()).uv((float) pt2[0], (float) pt2[1]).endVertex();
        bufferBuilder.vertex(x + targetW, y, this.getBlitOffset()).uv((float) pt3[0], (float) pt3[1]).endVertex();
        bufferBuilder.vertex(x, y, this.getBlitOffset()).uv((float) pt4[0], (float) pt4[1]).endVertex();
        bufferBuilder.end();
        RenderSystem.enableAlphaTest();
        BufferUploader.end(bufferBuilder);
    }
    
    public void renderTooltip(PoseStack matrices, FormattedText stringRenderable, int x, int y) {
        renderTooltipL(matrices, font.getSplitter().splitLines(stringRenderable, Integer.MAX_VALUE, Style.EMPTY), x, y);
    }
    
    public void renderTooltipL(PoseStack matrices, List<FormattedText> stringRenderables, int x, int y) {
        renderTooltip(matrices, Language.getInstance().getVisualOrder(stringRenderables), x, y);
    }
    
    public void drawLine(int x1, int y1, int x2, int y2, int thickness, int color) {
        RenderSystem.disableTexture();
        applyColor(color);
        
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(1 + thickness * this.width / 500F);
        
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3f(x1, y1, 0);
        GL11.glVertex3f(x2, y2, 0);
        GL11.glEnd();
        
        RenderSystem.enableTexture();
    }
    
    @Override
    public void renderTooltip(PoseStack matrices, ItemStack stack, int x, int y) {
        super.renderTooltip(matrices, stack, x, y);
    }
    
    public void drawFluid(FluidStack fluid, int x, int y, int mX, int mY) {
        drawItemBackground(x, y, mX, mY, false);
        if (fluid != null) {
            drawFluid(fluid, x + 1, y + 1);
        }
    }
    
    public void drawFluid(FluidStack fluid, int x, int y) {
        HardcoreQuestingCore.platform.renderFluidStack(fluid, getLeft() + x, getTop() + y, getLeft() + x + 16, getTop() + y + 16);
        /*//IIcon icon = fluid.getIconStack();
        Item stack = null;

        if (icon == null) {
            if (FluidRegistry.WATER.equals(fluid)) {
                icon = Blocks.water.getIconStack(0, 0);
            } else if (FluidRegistry.LAVA.equals(fluid)) {
                icon = Blocks.water.getIconStack(0, 0);
            }
        }

        if (icon != null) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            ResourceHelper.bindResource(MAP_TEXTURE);

            drawRect(x, y, 256 - 16, 256 - 16, 16, 16);

            ResourceHelper.bindResource(TERRAIN);
            setColor(fluid.getColor());
            drawIcon(icon, x, y);

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }*/
    }
    
    public void applyColor(int color) {
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        
        RenderSystem.color4f(r, g, b, a);
    }
    
    public void drawIcon(ItemStack stack, int x, int y) {
        itemRenderer.renderGuiItemDecorations(font, stack, x, y, null);
        //drawTexturedModelRectFromIcon(left + x, top + y, icon, 16, 16);
    }
    
    public void drawItemBackground(int x, int y, int mX, int mY, boolean selected) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        
        ResourceHelper.bindResource(MAP_TEXTURE);
        
        drawRect(x, y, inBounds(x, y, ITEM_SIZE, ITEM_SIZE, mX, mY) ? ITEM_SIZE : 0, ITEM_SRC_Y, ITEM_SIZE, ITEM_SIZE);
        if (selected) {
            drawRect(x, y, ITEM_SIZE * 2, ITEM_SRC_Y, ITEM_SIZE, ITEM_SIZE);
        }
    }
    
    public void drawItemStack(ItemStack stack, int x, int y, int mX, int mY, boolean selected) {
        drawItemBackground(x, y, mX, mY, selected);
        
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
        RenderSystem.color4f(colorComponents[2], colorComponents[1], colorComponents[0], 1F);
    }
    
    
    public void drawItemStack(@NotNull ItemStack stack, int x, int y, boolean renderEffect) {
        try {
            RenderSystem.pushMatrix();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            RenderSystem.enableDepthTest();
            RenderSystem.enableRescaleNormal();
            RenderSystem.translatef(getLeft() + x, getTop() + y, 0);
            
            Minecraft.getInstance().getItemRenderer().renderAndDecorateFakeItem(stack, 0, 0);
            Minecraft.getInstance().getItemRenderer().renderGuiItemDecorations(font, stack, 0, 0, "");
            
            RenderSystem.popMatrix();
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
        matrices.pushPose();
        matrices.scale(mult, mult, 1F);
        font.draw(matrices, Language.getInstance().getVisualOrder(str), (int) ((x + left) / mult), (int) ((y + top) / mult), color);
        matrices.popPose();
    }
    
    public void drawStringWithShadow(PoseStack matrices, FormattedText str, int x, int y, float mult, int color) {
        matrices.pushPose();
        matrices.scale(mult, mult, 1F);
        font.drawShadow(matrices, Language.getInstance().getVisualOrder(str), (int) ((x + left) / mult), (int) ((y + top) / mult), color);
        
        matrices.popPose();
    }
    
    public void drawString(PoseStack matrices, String str, int x, int y, int color) {
        drawString(matrices, str, x, y, 1F, color);
    }
    
    public void drawString(PoseStack matrices, String str, int x, int y, float mult, int color) {
        matrices.pushPose();
        matrices.scale(mult, mult, 1F);
        font.draw(matrices, str, (int) ((x + left) / mult), (int) ((y + top) / mult), color);
        matrices.popPose();
    }
    
    public void drawStringWithShadow(PoseStack matrices, String str, int x, int y, float mult, int color) {
        matrices.pushPose();
        matrices.scale(mult, mult, 1F);
        font.drawShadow(matrices, str, (int) ((x + left) / mult), (int) ((y + top) / mult), color);
        
        matrices.popPose();
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
//        List<StringVisitable> lst = new ArrayList<>();
        if (str == null) {
            str = Translator.plain("Missing info");
        }
//        String[] lines = str.split("\n");
//        for (String line : lines) {
//            
//            List<String> words = new ArrayList<String>();
//            Collections.addAll(words, line.split(" "));
//            
//            if (line.endsWith(" ")) {
//                String spaceTail = ""; //the split removes tailing spaces
//                for (int i = line.length() - 1; i >= 0; i--) {
//                    char c = line.charAt(i);
//                    if (c == ' ') {
//                        spaceTail += c;
//                    } else {
//                        break;
//                    }
//                }
//                words.add(spaceTail);
//            }
//            
//            for (int i = 0; i < words.size(); i++) {
//                String word = words.get(i);
//                StringBuilder other = new StringBuilder();
//                while (textRenderer.getStringWidth(word + " ") * mult >= width) {
//                    other.insert(0, word.charAt(word.length() - 1));
//                    word = word.substring(0, word.length() - 1);
//                }
//                
//                if (other.length() > 0) {
//                    words.set(i, word);
//                    words.add(i + 1, other.toString());
//                } else {
//                    words.set(i, word + " ");
//                }
//                
//            }
//            
//            
//            String currentLine = null;
//            for (String word : words) {
//                
//                String newLine;
//                if (currentLine == null) {
//                    newLine = word;
//                } else {
//                    newLine = currentLine + word;
//                }
//                if (textRenderer.getStringWidth(newLine) * mult < width) {
//                    currentLine = newLine;
//                } else {
//                    lst.add(Translator.plain(currentLine));
//                    currentLine = word;
//                }
//            }
//            lst.add(Translator.plain(currentLine));
//        }
//        
//        
        return font.getSplitter().splitLines(str, (int) (width / mult), Style.EMPTY);
    }
}
