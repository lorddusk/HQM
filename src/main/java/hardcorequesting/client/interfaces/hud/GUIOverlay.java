package hardcorequesting.client.interfaces.hud;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import hardcorequesting.DeathStats;
import hardcorequesting.QuestingData;
import hardcorequesting.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GUIOverlay extends Gui {

    private Minecraft mc;


    public GUIOverlay(Minecraft mc) {
        super();
        this.mc = mc;
    }

    @SubscribeEvent
    public void onRenderExperienceBar(RenderGameOverlayEvent event) {
        if (event.isCancelable() || event.type != ElementType.EXPERIENCE) {
            return;
        }

        int xPos = ModConfig.OVERLAY_XPOS;
        int yPos = ModConfig.OVERLAY_YPOS;
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        //String s = " Lives: " + QuestingData.getQuestingData(mc.thePlayer).getLives();
        String s = " Lives: " + getLives();
        String d = " Deaths: " + getDeaths();
        yPos += 10;

        if (QuestingData.isHardcoreActive()) {
            if (getLives() <= 2) {
                this.mc.fontRenderer.drawString(s, xPos + 1, yPos, 0xf50505);
            } else {
                this.mc.fontRenderer.drawString(s, xPos + 1, yPos, 0xffffff);
            }
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(true);
        }
        else{
            this.mc.fontRenderer.drawString(d, xPos + 1, yPos, 0xffffff);
        }
    }

    public int getLives(){
        return QuestingData.getQuestingData(mc.thePlayer.getGameProfile().getName()).getLives();
    }

    public int getDeaths(){
        return QuestingData.getQuestingData(mc.thePlayer.getGameProfile().getName()).getDeathStat().getTotalDeaths();
    }

}

	