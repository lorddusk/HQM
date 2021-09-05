package hardcorequesting.common.quests.task.client;

import hardcorequesting.common.client.interfaces.Graphic;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public abstract class TaskGraphic extends Graphic {
    protected static final int START_X = 180;
    protected static final int START_Y = 95;
}