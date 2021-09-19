package hardcorequesting.common.client.interfaces.graphic.task;

import hardcorequesting.common.client.ClientChange;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.graphic.Graphic;
import hardcorequesting.common.client.interfaces.widget.LargeButton;
import hardcorequesting.common.network.NetworkManager;
import hardcorequesting.common.quests.task.QuestTask;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public abstract class TaskGraphic extends Graphic {
    protected static final int START_X = 180;
    protected static final int START_Y = 95;
    protected final UUID playerId;
    protected final GuiQuestBook gui;
    
    protected TaskGraphic(UUID playerId, GuiQuestBook gui) {
        this.playerId = playerId;
        this.gui = gui;
    }
    
    protected void addSubmitButton(QuestTask<?> task) {
        addButton(new LargeButton(gui, "hqm.quest.manualSubmit", 185, 200) {
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
    
    protected void addDetectButton(QuestTask<?> task) {
        addButton(new LargeButton(gui, "hqm.quest.manualDetect", 185, 200) {
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