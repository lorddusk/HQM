package hardcorequesting.common.client.interfaces.graphic.task;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.ClientChange;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.TextMenu;
import hardcorequesting.common.client.interfaces.graphic.Graphic;
import hardcorequesting.common.client.interfaces.widget.LargeButton;
import hardcorequesting.common.client.interfaces.widget.ScrollBar;
import hardcorequesting.common.network.NetworkManager;
import hardcorequesting.common.quests.task.QuestTask;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.FormattedText;

import java.util.List;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public abstract class TaskGraphic extends Graphic {
    protected static final int START_X = 180;
    protected static final int START_Y = 95;
    private static final int VISIBLE_DESCRIPTION_LINES = 7;
    private static final int TASK_DESCRIPTION_X = 180;
    private static final int TASK_DESCRIPTION_Y = 20;
    
    private final ScrollBar taskDescriptionScroll;
    
    protected final UUID playerId;
    protected final GuiQuestBook gui;
    private final QuestTask<?> task;
    private List<FormattedText> cachedDescription;
    
    protected TaskGraphic(UUID playerId, GuiQuestBook gui, QuestTask<?> task) {
        this.playerId = playerId;
        this.gui = gui;
        this.task = task;
        
        addScrollBar(taskDescriptionScroll = new ScrollBar(gui, ScrollBar.Size.SMALL, 312, 18, TASK_DESCRIPTION_X) {
            @Override
            public boolean isVisible() {
                return getCachedLongDescription().size() > VISIBLE_DESCRIPTION_LINES;
            }
        });
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
    
    @Override
    public void draw(PoseStack matrices, int mX, int mY) {
        super.draw(matrices, mX, mY);
        
        List<FormattedText> description = taskDescriptionScroll.getVisibleEntries(getCachedLongDescription(), VISIBLE_DESCRIPTION_LINES);
        gui.drawString(matrices, description, TASK_DESCRIPTION_X, TASK_DESCRIPTION_Y, 0.7F, 0x404040);
    }
    
    @Override
    public void onClick(int mX, int mY, int b) {
        super.onClick(mX, mY, b);
        if (gui.getCurrentMode() == EditMode.RENAME && gui.inBounds(TASK_DESCRIPTION_X, TASK_DESCRIPTION_Y, 130, (int) (VISIBLE_DESCRIPTION_LINES * GuiBase.TEXT_HEIGHT * 0.7), mX, mY)) {
            TextMenu.display(gui, playerId, task.getLangKeyLongDescription(), false, this::setLongDescription);
        }
    }
    
    private List<FormattedText> getCachedLongDescription() {
        if (cachedDescription == null) {
            cachedDescription = gui.getLinesFromText(task.getLongDescription(), 0.7F, 130);
        }
        
        return cachedDescription;
    }
    
    private void setLongDescription(String description) {
        task.setLongDescription(description);
        cachedDescription = null;
    }
}