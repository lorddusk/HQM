package hardcorequesting.common.client.interfaces.widget;

import com.mojang.blaze3d.vertex.PoseStack;

public interface Drawable {
    void render(PoseStack matrices, int mX, int mY);
}
