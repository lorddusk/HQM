package hqm.api.page.part;

import hqm.api.page.IRenderPart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayList;
import java.util.List;

public class RenderPartText implements IRenderPart{
    
    private int x, y, width, height;
    private List<String> displayText;
    private float scale = 1.0F;
    
    @Override
    public void onCreation(int x, int y, int width, int height){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    @Override
    public int getX(){
        return this.x;
    }
    
    @Override
    public int getY(){
        return this.y;
    }
    
    @Override
    public int getWidth(){
        return this.width;
    }
    
    @Override
    public int getHeight(){
        return this.height;
    }
    
    public void setText(String s, boolean wrapLines){
        List<String> lines = new ArrayList<>();
        
        List<String> neededLines = Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(s, this.getWidth());
        if(wrapLines){
            float heightIndex = 0.0F;
            for(String line : neededLines){
                heightIndex += (Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT * this.scale);
                if(heightIndex <= this.getHeight()){
                    lines.add(line);
                }
            }
        } else {
            lines.add(neededLines.stream().findFirst().orElse(""));
        }
        
        this.displayText = lines;
    }
    
    public void setScale(float scale){
        this.scale = scale;
    }
    
    @Override
    public void render(){
        float heightIndex = 0;
        for(String s : this.displayText){
            GlStateManager.pushMatrix();
            GlStateManager.scale(this.scale, this.scale, 1.0F);
            Minecraft.getMinecraft().fontRenderer.drawString(s, this.getX(), Math.round(this.getY() + heightIndex), 0xFFFFFF);
            GlStateManager.popMatrix();
            heightIndex += Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT * this.scale;
        }
    }
}
