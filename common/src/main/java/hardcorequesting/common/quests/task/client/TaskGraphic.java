package hardcorequesting.common.quests.task.client;

import hardcorequesting.common.client.ClientChange;
import hardcorequesting.common.client.interfaces.Graphic;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.widget.LargeButton;
import hardcorequesting.common.network.NetworkManager;
import hardcorequesting.common.quests.task.QuestTask;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public abstract class TaskGraphic extends Graphic {
    protected static final int START_X = 180;
    protected static final int START_Y = 95;
    protected final UUID playerId;
    
    protected TaskGraphic(UUID playerId) {
        this.playerId = playerId;
    }
    
    protected void addSubmitButton(QuestTask<?> task) {
        addButton(new LargeButton("hqm.quest.manualSubmit", 185, 200) {
            @Override
            public boolean isEnabled(GuiBase gui, Player player) {
                return true;
            }
        
            @Override
            public boolean isVisible(GuiBase gui, Player player) {
                return !task.isCompleted(playerId);
            }
        
            @Override
            public void onClick(GuiBase gui, Player player) {
                NetworkManager.sendToServer(ClientChange.UPDATE_TASK.build(task));
            }
        });
    }
    
    protected void addDetectButton(QuestTask<?> task) {
        addButton(new LargeButton("hqm.quest.manualDetect", 185, 200) {
            @Override
            public boolean isEnabled(GuiBase gui, Player player) {
                return true;
            }
        
            @Override
            public boolean isVisible(GuiBase gui, Player player) {
                return !task.isCompleted(playerId);
            }
        
            @Override
            public void onClick(GuiBase gui, Player player) {
                NetworkManager.sendToServer(ClientChange.UPDATE_TASK.build(task));
            }
        });
    }
}