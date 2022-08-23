package hardcorequesting.common.client.interfaces.graphic.task;

import hardcorequesting.common.client.ClientChange;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.widget.AbstractCheckBox;
import hardcorequesting.common.network.NetworkManager;
import hardcorequesting.common.quests.task.CheckBoxTask;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.CommonComponents;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public class CheckBoxTaskGraphic extends TaskGraphic {
    private boolean completed;

    public CheckBoxTaskGraphic(CheckBoxTask task, UUID playerId, GuiQuestBook gui) {
        super(playerId, gui, task);

        completed = task.isCompleted(playerId);

        // TODO custom checkbox label?
        addClickable(new AbstractCheckBox(gui, CommonComponents.EMPTY, START_X, START_Y) {
            @Override
            public boolean getValue() {
                return completed;
            }

            @Override
            public void setValue(boolean val) {
                if (val && !completed) {
                    completed = true;
                    NetworkManager.sendToServer(ClientChange.COMPLETE_CHECKBOX_TASK.build(task));
                }
            }
        });
    }
}
