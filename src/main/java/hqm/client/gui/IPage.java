package hqm.client.gui;

import net.minecraft.util.ResourceLocation;

/**
 * @author canitzp
 */
public interface IPage {

    ResourceLocation getId();

    default void init(GuiQuestBook gui){}

    void render(GuiQuestBook gui, int pageLeft, int pageTop, double mouseX, double mouseY, Side side);

    enum Side {
        RIGHT,
        LEFT
    }

}
