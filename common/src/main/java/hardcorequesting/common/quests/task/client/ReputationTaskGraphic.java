package hardcorequesting.common.quests.task.client;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.ResourceHelper;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenuReputationSetting;
import hardcorequesting.common.quests.task.reputation.ReputationTask;
import hardcorequesting.common.reputation.Reputation;
import hardcorequesting.common.util.Positioned;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Environment(EnvType.CLIENT)
public class ReputationTaskGraphic extends ListTaskGraphic<ReputationTask.Part> {
    private static final int OFFSET_Y = 27;
    private final int startOffsetY;
    
    private final ReputationTask<?> task;
    
    public ReputationTaskGraphic(ReputationTask<?> task) {
        super(task.parts);
        this.task = task;
        startOffsetY = 0;
    }
    
    protected ReputationTaskGraphic(ReputationTask<?> task, int startOffsetY) {
        super(task.parts);
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
    
    protected Player getPlayerForRender(Player player) {
        return player;
    }
    
    @Override
    protected List<FormattedText> drawPart(PoseStack matrices, GuiQuestBook gui, Player player, ReputationTask.Part part, int id, int x, int y, int mX, int mY) {
        gui.applyColor(0xFFFFFFFF);
        ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
    
        if (part.getReputation() == null) {
            gui.drawRect(x + Reputation.BAR_X, y + Reputation.BAR_Y, Reputation.BAR_SRC_X, Reputation.BAR_SRC_Y, Reputation.BAR_WIDTH, Reputation.BAR_HEIGHT);
            return null;
        } else {
            String text = part.getReputation().draw(matrices, gui, x, y, mX, mY, null, getPlayerForRender(player), true, part.getLower(), part.getUpper(), part.isInverted(), null, null, task.getData(player).completed);
            return Collections.singletonList(Translator.plain(text));
        }
    }
    
    @Override
    protected boolean isInPartBounds(GuiQuestBook gui, int mX, int mY, Positioned<ReputationTask.Part> pos) {
        return gui.inBounds(pos.getX(), pos.getY(), Reputation.BAR_WIDTH, 20, mX, mY);
    }
    
    @Override
    protected boolean handlePartClick(GuiQuestBook gui, Player player, EditMode mode, ReputationTask.Part part, int id) {
        if (gui.getCurrentMode() == EditMode.REPUTATION_TASK) {
            gui.setEditMenu(new GuiEditMenuReputationSetting(gui, player, task, id, part));
            return true;
        } else {
            return super.handlePartClick(gui, player, mode, part, id);
        }
    }
}