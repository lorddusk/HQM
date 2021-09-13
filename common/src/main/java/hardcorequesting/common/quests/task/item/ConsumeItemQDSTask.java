package hardcorequesting.common.quests.task.item;

import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.task.client.ItemTaskGraphic;
import hardcorequesting.common.quests.task.client.TaskGraphic;
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
