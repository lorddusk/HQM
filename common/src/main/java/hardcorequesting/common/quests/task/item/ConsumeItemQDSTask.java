package hardcorequesting.common.quests.task.item;

import hardcorequesting.common.client.interfaces.graphic.task.ItemTaskGraphic;
import hardcorequesting.common.client.interfaces.graphic.task.TaskGraphic;
import hardcorequesting.common.quests.Quest;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.UUID;

public class ConsumeItemQDSTask extends ConsumeItemTask {
    
    public ConsumeItemQDSTask(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public TaskGraphic createGraphic(UUID playerId) {
        return ItemTaskGraphic.createConsumeGraphic(this, parts, playerId, false);
    }
}
