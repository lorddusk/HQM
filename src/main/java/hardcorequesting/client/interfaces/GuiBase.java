package hardcorequesting.client.interfaces;

import hardcorequesting.client.interfaces.edit.GuiEditMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiBase extends GuiScreen {
    protected int left, top;

    public void setEditMenu(GuiEditMenu menu) {
    }

    public static final ResourceLocation MAP_TEXTURE = ResourceHelper.getResource("questmap");

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

        double a = (double) ((float) (u) * fw);
        double b = (double) ((float) (u + w) * fw);
        double c = (double) ((float) (v + h) * fy);
        double d = (double) ((float) (v) * fy);

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

        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer worldRenderer = tessellator.getBuffer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldRenderer.pos((double) (x), (double) (y + targetH), (double) this.zLevel).tex(pt1[0], pt1[1]).endVertex();
        worldRenderer.pos((double) (x + targetW), (double) (y + targetH), (double) this.zLevel).tex(pt2[0], pt2[1]).endVertex();
        worldRenderer.pos((double) (x + targetW), (double) (y), (double) this.zLevel).tex(pt3[0], pt3[1]).endVertex();
        worldRenderer.pos((double) (x), (double) (y), (double) this.zLevel).tex(pt4[0], pt4[1]).endVertex();
        tessellator.draw();
    }

    public void drawMouseOver(String str, int x, int y) {
        List<String> lst = new ArrayList<String>();
        Collections.addAll(lst, str.split("\n"));
        drawMouseOver(lst, x, y);
    }

    public void drawMouseOver(List<String> str, int x, int y) {
        float oldZ = this.zLevel;
        GlStateManager.disableDepth();

        int w = 0;

        for (String line : str) {
            int l = fontRenderer.getStringWidth(line);

            if (l > w) {
                w = l;
            }
        }

        x += 12;
        y -= 12;
        int h = 8;

        if (str.size() > 1) {
            h += 2 + (str.size() - 1) * 10;
        }

        if (x + w > this.width) {
            x -= 28 + w;
        }

        if (y + h + 6 > this.height) {
            y = this.height - h - 6;
        }

        this.zLevel = 300.0F;
        int bg = -267386864;
        this.drawGradientRect(x - 3, y - 4, x + w + 3, y - 3, bg, bg);
        this.drawGradientRect(x - 3, y + h + 3, x + w + 3, y + h + 4, bg, bg);
        this.drawGradientRect(x - 3, y - 3, x + w + 3, y + h + 3, bg, bg);
        this.drawGradientRect(x - 4, y - 3, x - 3, y + h + 3, bg, bg);
        this.drawGradientRect(x + w + 3, y - 3, x + w + 4, y + h + 3, bg, bg);
        int border1 = 1347420415;
        int border2 = (border1 & 16711422) >> 1 | border1 & -16777216;
        this.drawGradientRect(x - 3, y - 3 + 1, x - 3 + 1, y + h + 3 - 1, border1, border2);
        this.drawGradientRect(x + w + 2, y - 3 + 1, x + w + 3, y + h + 3 - 1, border1, border2);
        this.drawGradientRect(x - 3, y - 3, x + w + 3, y - 3 + 1, border1, border1);
        this.drawGradientRect(x - 3, y + h + 2, x + w + 3, y + h + 3, border2, border2);

        for (int i = 0; i < str.size(); i++) {
            String line = str.get(i);
            fontRenderer.drawStringWithShadow(line, x, y, -1);

            if (i == 0) {
                y += 2;
            }

            y += 10;
        }

        this.zLevel = oldZ;
        GlStateManager.enableDepth();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public void drawLine(int x1, int y1, int x2, int y2, int thickness, int color) {
        GlStateManager.disableTexture2D();
        applyColor(color);

        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(1 + thickness * this.width / 500F);

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3f(x1, y1, 0);
        GL11.glVertex3f(x2, y2, 0);
        GL11.glEnd();

        GlStateManager.enableTexture2D();
    }

    public void applyColor(int color) {
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;

        GlStateManager.color(r, g, b, a);
    }


    protected static RenderItem itemRenderer = Minecraft.getMinecraft().getRenderItem();

    public void drawIcon(ItemStack item, int x, int y) {
        itemRenderer.renderItemOverlayIntoGUI(fontRenderer, item, x, y, null);
        //drawTexturedModelRectFromIcon(left + x, top + y, icon, 16, 16);
    }

//TODO Fix Fluid drawing
//    public void drawFluid(Fluid fluid, int x, int y, int mX, int mY) {
//        drawItemBackground(x, y, mX, mY, false);
//        if (fluid != null) {
//            drawFluid(fluid, x + 1, y + 1);
//        }
//    }
//
//    public void drawFluid(Fluid fluid, int x, int y) {
//        //IIcon icon = fluid.getIcon();
//        Item item = null;
//
//        if (icon == null) {
//            if (FluidRegistry.WATER.equals(fluid)) {
//                icon = Blocks.water.getIcon(0, 0);
//            } else if (FluidRegistry.LAVA.equals(fluid)) {
//                icon = Blocks.water.getIcon(0, 0);
//            }
//        }
//
//        if (icon != null) {
//            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
//
//            ResourceHelper.bindResource(MAP_TEXTURE);
//
//            drawRect(x, y, 256 - 16, 256 - 16, 16, 16);
//
//            ResourceHelper.bindResource(TERRAIN);
//            setColor(fluid.getColor());
//            drawIcon(icon, x, y);
//
//            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
//        }
//    }

    protected static final int ITEM_SRC_Y = 235;
    public static final int ITEM_SIZE = 18;

    protected void drawItemBackground(int x, int y, int mX, int mY, boolean selected) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        ResourceHelper.bindResource(MAP_TEXTURE);

        drawRect(x, y, inBounds(x, y, ITEM_SIZE, ITEM_SIZE, mX, mY) ? ITEM_SIZE : 0, ITEM_SRC_Y, ITEM_SIZE, ITEM_SIZE);
        if (selected) {
            drawRect(x, y, ITEM_SIZE * 2, ITEM_SRC_Y, ITEM_SIZE, ITEM_SIZE);
        }
    }

    public void drawItem(ItemStack item, int x, int y, int mX, int mY, boolean selected) {
        drawItemBackground(x, y, mX, mY, selected);

        if (item != null && item.getItem() != null) {
            drawItem(item, x + 1, y + 1, true);
            itemRenderer.renderItemOverlayIntoGUI(fontRenderer, item, x + left + 1, y + +top + 1, "");
        }
        GlStateManager.disableLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }


    protected void setColor(int color) {
        float[] colorComponents = new float[3];
        for (int i = 0; i < colorComponents.length; i++) {
            colorComponents[i] = ((color & (255 << (i * 8))) >> (i * 8)) / 255F;
        }
        GlStateManager.color(colorComponents[2], colorComponents[1], colorComponents[0], 1F);
    }


    public void drawItem(ItemStack itemstack, int x, int y, boolean renderEffect) {
        if (!itemstack.isEmpty()){
            GlStateManager.pushMatrix();

            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.disableLighting();
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableColorMaterial();
            GlStateManager.enableLighting();

            float oldZ = this.zLevel;
            setZLevel(4f);
            try {
                // if (!ForgeHooksClient.renderInventoryItem(blockRenderer, this.mc.getTextureManager(), itemstack, renderEffect, zLevel, x + left, y + top)) {
                itemRenderer.renderItemAndEffectIntoGUI(itemstack, x + left, y + top);
                // }
            } finally {
                setZLevel(oldZ);

                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.disableLighting();
                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GlStateManager.enableAlpha();
            }

            GlStateManager.popMatrix();
        }
    }

    public float getZLevel() {
        return zLevel;
    }

    public void setZLevel(float zLevel) {
        this.zLevel = zLevel;
    }


    public int getLeft() {
        return left;
    }

    public int getTop() {
        return top;
    }


    public int getStringWidth(String txt) {
        return fontRenderer.getStringWidth(txt);
    }


    public void drawString(String str, int x, int y, int color) {
        drawString(str, x, y, 1F, color);
    }

    public void drawString(String str, int x, int y, float mult, int color) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(mult, mult, 1F);
        fontRenderer.drawString(str, (int) ((x + left) / mult), (int) ((y + top) / mult), color);

        GlStateManager.popMatrix();
    }

    public void drawStringWithShadow(String str, int x, int y, float mult, int color) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(mult, mult, 1F);
        fontRenderer.drawStringWithShadow(str, (int) ((x + left) / mult), (int) ((y + top) / mult), color);

        GlStateManager.popMatrix();
    }

    public boolean inBounds(int x, int y, int w, int h, int mX, int mY) {
        return x <= mX && mX <= x + w && y <= mY && mY <= y + h;
    }

    public void drawCursor(int x, int y, int z, float size, int color) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, z);
        x += left;
        y += top;
        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(size, size, 0);
        GlStateManager.translate(-x, -y, 0);
        Gui.drawRect(x, y + 1, x + 1, y + 10, color);
        GlStateManager.popMatrix();
    }

    public void drawString(List<String> str, int x, int y, float mult, int color) {
        drawString(str, 0, str.size(), x, y, mult, color);
    }

    public void drawString(List<String> str, int start, int length, int x, int y, float mult, int color) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(mult, mult, 1F);
        start = Math.max(start, 0);
        int end = Math.min(start + length, str.size());
        for (int i = start; i < end; i++) {
            fontRenderer.drawString(str.get(i), (int) ((x + left) / mult), (int) ((y + top) / mult), color);
            y += fontRenderer.FONT_HEIGHT;
        }
        GlStateManager.popMatrix();
    }

    public void drawCenteredString(String str, int x, int y, float mult, int width, int height, int color) {
        drawString(str, x + (width - (int) (fontRenderer.getStringWidth(str) * mult)) / 2, y + (height - (int) ((fontRenderer.FONT_HEIGHT - 2) * mult)) / 2, mult, color);
    }

    public List<String> getLinesFromText(String str, float mult, int width) {
        List<String> lst = new ArrayList<String>();
        if (str == null) {
            str = "Missing info";
        }
        String[] lines = str.split("\n");
        for (String line : lines) {

            List<String> words = new ArrayList<String>();
            Collections.addAll(words, line.split(" "));

            if (line.endsWith(" ")) {
                String spaceTail = ""; //the split removes tailing spaces
                for (int i = line.length() - 1; i >= 0; i--) {
                    char c = line.charAt(i);
                    if (c == ' ') {
                        spaceTail += c;
                    } else {
                        break;
                    }
                }
                words.add(spaceTail);
            }

            for (int i = 0; i < words.size(); i++) {
                String word = words.get(i);
                String other = "";
                while (fontRenderer.getStringWidth(word + " ") * mult >= width) {
                    other = word.charAt(word.length() - 1) + other;
                    word = word.substring(0, word.length() - 1);
                }

                if (!other.isEmpty()) {
                    words.set(i, word);
                    words.add(i + 1, other);
                } else {
                    words.set(i, word + " ");
                }

            }


            String currentLine = null;
            for (String word : words) {

                String newLine;
                if (currentLine == null) {
                    newLine = word;
                } else {
                    newLine = currentLine + word;
                }
                if (fontRenderer.getStringWidth(newLine) * mult < width) {
                    currentLine = newLine;
                } else {
                    lst.add(currentLine);
                    currentLine = word;
                }
            }
            lst.add(currentLine);
        }


        return lst;
    }
}
