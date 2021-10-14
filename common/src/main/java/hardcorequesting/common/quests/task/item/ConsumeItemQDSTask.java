package hardcorequesting.common.quests.task.item;

import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.graphic.task.ItemTaskGraphic;
import hardcorequesting.common.client.interfaces.graphic.task.TaskGraphic;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.task.TaskType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.UUID;

public class ConsumeItemQDSTask extends ConsumeItemTask {
    
    public ConsumeItemQDSTask(Quest parent) {
        super(TaskType.CONSUME_QDS, parent);
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public TaskGraphic createGraphic(UUID playerId, GuiQuestBook gui) {
        return ItemTaskGraphic.createConsumeGraphic(this, parts, playerId, gui, false);
    }
}
