package hardcorequesting.common.client.interfaces.graphic.task;

import hardcorequesting.common.client.ClientChange;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.widget.AbstractCheckBox;
import hardcorequesting.common.client.interfaces.widget.LargeButton;
import hardcorequesting.common.network.NetworkManager;
import hardcorequesting.common.quests.task.CheckBoxTask;
import net.minecraft.network.chat.TextComponent;

import java.util.UUID;

public class CheckBoxTaskGraphic extends TaskGraphic {
    public CheckBoxTaskGraphic(CheckBoxTask task, UUID playerId, GuiQuestBook gui) {
        super(playerId, gui, task);

        addClickable(new AbstractCheckBox(gui, TextComponent.EMPTY, START_X, START_Y) {
            @Override
            public boolean getValue() {
                return task.isToggled(playerId);
            }

            @Override
            public void setValue(boolean val) {
                task.setToggled(playerId, val);
            }
        });

        addClickable(new LargeButton(gui, "hqm.quest.manualSubmit", 185, 200) {
            @Override
            public boolean isVisible() {
                return !task.isCompleted(playerId);
            }

            @Override
            public void onClick() {
                NetworkManager.sendToServer(ClientChange.UPDATE_TASK.build(task));
            }
        });
    }
}
