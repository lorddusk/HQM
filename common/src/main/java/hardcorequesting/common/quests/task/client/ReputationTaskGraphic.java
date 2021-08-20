package hardcorequesting.common.quests.task.client;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.ResourceHelper;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenuReputationSetting;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.task.reputation.ReputationTask;
import hardcorequesting.common.reputation.Reputation;
import hardcorequesting.common.util.Positioned;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class ReputationTaskGraphic implements TaskGraphic {
    private static final int OFFSET_Y = 27;
    private final int startOffsetY;
    
    private final ReputationTask<?> task;
    
    public ReputationTaskGraphic(ReputationTask<?> task) {
        this.task = task;
        startOffsetY = 0;
    }
    
    protected ReputationTaskGraphic(ReputationTask<?> task, int startOffsetY) {
        this.task = task;
        this.startOffsetY = startOffsetY;
    }
    
    protected List<Positioned<ReputationTask.Part>> positionParts(List<ReputationTask.Part> parts) {
        List<Positioned<ReputationTask.Part>> list = new ArrayList<>(parts.size());
        int x = START_X;
        int y = START_Y + startOffsetY;
        for (ReputationTask.Part part : parts) {
            list.add(new Positioned<>(x, y, part));
            y += OFFSET_Y;
        }
        return list;
    }
    
    protected Player getPlayerForRender(Player player) {
        return player;
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void draw(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY) {
        String info = null;
        List<Positioned<ReputationTask.Part>> renderSettings = positionParts(task.parts.getShownElements());
        for (Positioned<ReputationTask.Part> pos : renderSettings) {
            ReputationTask.Part part = pos.getElement();
        
            gui.applyColor(0xFFFFFFFF);
            ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
        
            if (part.getReputation() == null) {
                gui.drawRect(pos.getX() + Reputation.BAR_X, pos.getY() + Reputation.BAR_Y, Reputation.BAR_SRC_X, Reputation.BAR_SRC_Y, Reputation.BAR_WIDTH, Reputation.BAR_HEIGHT);
            } else {
                info = part.getReputation().draw(matrices, gui, pos.getX(), pos.getY(), mX, mY, info, getPlayerForRender(player), true, part.getLower(), part.getUpper(), part.isInverted(), null, null, task.getData(player).completed);
            }
        }
    
        if (info != null) {
            gui.renderTooltip(matrices, Translator.plain(info), mX + gui.getLeft(), mY + gui.getTop());
        }
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void onClick(GuiQuestBook gui, Player player, int mX, int mY, int b) {
        if (Quest.canQuestsBeEdited() && gui.getCurrentMode() != EditMode.NORMAL) {
            List<Positioned<ReputationTask.Part>> renderSettings = positionParts(task.parts.getShownElements());
            for (int i = 0; i < renderSettings.size(); i++) {
                Positioned<ReputationTask.Part> pos = renderSettings.get(i);
                ReputationTask.Part part = pos.getElement();
            
                if (gui.inBounds(pos.getX(), pos.getY(), Reputation.BAR_WIDTH, 20, mX, mY)) {
                    if (gui.getCurrentMode() == EditMode.REPUTATION_TASK) {
                        gui.setEditMenu(new GuiEditMenuReputationSetting(gui, player, task, i, part));
                    } else if (gui.getCurrentMode() == EditMode.DELETE) {
                        task.parts.remove(i);
                    }
                    break;
                }
            }
        }
    }
}
