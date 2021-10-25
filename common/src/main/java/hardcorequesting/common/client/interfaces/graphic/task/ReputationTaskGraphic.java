package hardcorequesting.common.client.interfaces.graphic.task;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.ResourceHelper;
import hardcorequesting.common.client.interfaces.edit.ReputationTaskPartMenu;
import hardcorequesting.common.quests.task.reputation.ReputationTask;
import hardcorequesting.common.reputation.Reputation;
import hardcorequesting.common.util.Positioned;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.FormattedText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class ReputationTaskGraphic extends ListTaskGraphic<ReputationTask.Part> {
    private static final int OFFSET_Y = 27;
    private final int startOffsetY;
    
    private final ReputationTask<?> task;
    
    public ReputationTaskGraphic(ReputationTask<?> task, UUID playerId, GuiQuestBook gui) {
        this(task, playerId, gui, 0);
    }
    
    protected ReputationTaskGraphic(ReputationTask<?> task, UUID playerId, GuiQuestBook gui, int startOffsetY) {
        super(task, task.getParts(), playerId, gui);
        this.task = task;
        this.startOffsetY = startOffsetY;
    }
    
    @Override
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
    
    protected boolean shouldShowPlayer() {
        return true;
    }
    
    @Override
    protected void drawPart(PoseStack matrices, ReputationTask.Part part, int id, int x, int y, int mX, int mY) {
        gui.applyColor(0xFFFFFFFF);
        ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
    
        if (part.getReputation() == null) {
            gui.drawRect(matrices, x + Reputation.BAR_X, y + Reputation.BAR_Y, Reputation.BAR_SRC_X, Reputation.BAR_SRC_Y, Reputation.BAR_WIDTH, Reputation.BAR_HEIGHT);
        } else {
            part.getReputation().draw(matrices, gui, x, y, mX, mY, shouldShowPlayer() ? playerId : null, true, part.getLower(), part.getUpper(), part.isInverted(), null, null, task.isCompleted(playerId));
        }
    }
    
    @Override
    protected List<FormattedText> getPartTooltip(Positioned<ReputationTask.Part> pos, int id, int mX, int mY) {
        ReputationTask.Part part = pos.getElement();
        if (part.getReputation() != null) {
            String text = part.getReputation().getTooltip(gui, pos.getX(), pos.getY(), mX, mY, playerId);
            if (text != null)
                return Collections.singletonList(Translator.plain(text));
        }
        return null;
    }
    
    @Override
    protected boolean isInPartBounds(int mX, int mY, Positioned<ReputationTask.Part> pos) {
        return gui.inBounds(pos.getX(), pos.getY(), Reputation.BAR_WIDTH, 20, mX, mY);
    }
    
    @Override
    protected boolean handlePartClick(EditMode mode, ReputationTask.Part part, int id) {
        if (gui.getCurrentMode() == EditMode.REPUTATION_TASK) {
            ReputationTaskPartMenu.display(gui, playerId, part, newPart -> task.setSetting(id, newPart));
            return true;
        } else {
            return super.handlePartClick(mode, part, id);
        }
    }
}