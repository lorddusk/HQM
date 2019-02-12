package hardcorequesting.client.interfaces.hud;

import hardcorequesting.config.HQMConfig;
import hardcorequesting.quests.QuestingData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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
        if (event.isCancelable() || event.getType() != ElementType.EXPERIENCE) {
            return;
        }

        int xPos = HQMConfig.OVERLAY_XPOS;
        int yPos = HQMConfig.OVERLAY_YPOS;
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableAlpha();
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
            GlStateManager.disableBlend();
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
        } else {
            this.mc.fontRenderer.drawString(d, xPos + 1, yPos, 0xffffff);
        }
    }

    public int getLives() {
        return QuestingData.getQuestingData(mc.player).getLives();
    }

    public int getDeaths() {
        return QuestingData.getQuestingData(mc.player).getDeathStat().getTotalDeaths();
    }

}

	