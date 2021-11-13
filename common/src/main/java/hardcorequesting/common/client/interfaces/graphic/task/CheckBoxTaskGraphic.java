package hardcorequesting.common.client.interfaces.graphic.task;

import hardcorequesting.common.client.ClientChange;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.widget.AbstractCheckBox;
import hardcorequesting.common.network.NetworkManager;
import hardcorequesting.common.quests.task.CheckBoxTask;
import net.minecraft.network.chat.TextComponent;

import java.util.UUID;

public class CheckBoxTaskGraphic extends TaskGraphic {
    private boolean completed;

    public CheckBoxTaskGraphic(CheckBoxTask task, UUID playerId, GuiQuestBook gui) {
        super(playerId, gui, task);

        completed = task.isCompleted(playerId);

        // TODO custom checkbox label?
        addClickable(new AbstractCheckBox(gui, TextComponent.EMPTY, START_X, START_Y) {
            @Override
            public boolean getValue() {
                return completed;
            }

            @Override
            public void setValue(boolean val) {
                if (val && !completed) {
                    completed = true;
                    task.markCompleted(playerId); // sets local completed flag
                                                  //  this doesn't actually do much - the server handles everything else
                    NetworkManager.sendToServer(ClientChange.UPDATE_TASK.build(task));
                }
            }
        });
    }
}
