package hardcorequesting.common.quests.task.client;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiColor;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.task.CompleteQuestTask;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Player;

import java.util.List;

@Environment(EnvType.CLIENT)
public class CompleteQuestTaskGraphic implements TaskGraphic {
    private static final int Y_OFFSET = 30;
    private static final int X_TEXT_OFFSET = 23;
    private static final int X_TEXT_INDENT = 0;
    private static final int Y_TEXT_OFFSET = 0;
    private static final int ITEM_SIZE = 18;
    
    private final CompleteQuestTask task;
    
    public CompleteQuestTaskGraphic(CompleteQuestTask task) {
        this.task = task;
    }
    
    
    @Override
    public void draw(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY) {
        List<CompleteQuestTask.Part> quests = task.parts.getShownElements();
        for (int i = 0; i < quests.size(); i++) {
            CompleteQuestTask.Part completed = quests.get(i);
        
            int x = START_X;
            int y = START_Y + i * Y_OFFSET;
            gui.drawItemStack(completed.getIconStack(), x, y, mX, mY, false);
            if (completed.getQuest() != null) {
                gui.drawString(matrices, Translator.plain(completed.getName()), x + X_TEXT_OFFSET, y + Y_TEXT_OFFSET, 0x404040);
            } else {
                gui.drawString(matrices, Translator.translatable("hqm.completionTask.firstline", GuiColor.RED), x + X_TEXT_OFFSET, y + Y_TEXT_OFFSET, 0x404040);
                gui.drawString(matrices, Translator.translatable("hqm.completionTask.secondline", GuiColor.RED), x + X_TEXT_OFFSET, y + Y_TEXT_OFFSET + 9, 0x404040);
                gui.drawString(matrices, Translator.translatable("hqm.completionTask.thirdline", GuiColor.RED), x + X_TEXT_OFFSET, y + Y_TEXT_OFFSET + 18, 0x404040);
            }
        
            if (task.completed(i, player)) {
                gui.drawString(matrices, Translator.translatable("hqm.completedMenu.visited", GuiColor.GREEN), x + X_TEXT_OFFSET + X_TEXT_INDENT, y + Y_TEXT_OFFSET + 9, 0.7F, 0x404040);
            }
        }
    }
    
    @Override
    public void onClick(GuiQuestBook gui, Player player, int mX, int mY, int b) {
        if (Quest.canQuestsBeEdited()) {
            List<CompleteQuestTask.Part> quests = task.parts.getShownElements();
            for (int i = 0; i < quests.size(); i++) {
                CompleteQuestTask.Part completed = quests.get(i);
            
                int x = START_X;
                int y = START_Y + i * Y_OFFSET;
            
                if (gui.inBounds(x, y, ITEM_SIZE, ITEM_SIZE, mX, mY)) {
                    if (gui.getCurrentMode() == EditMode.DELETE) {
                        task.parts.remove(i);
                    } else if (completed.getQuest() == null) {
                        task.parts.getOrCreateForModify(i).setQuest(Quest.speciallySelectedQuestId);
                        SaveHelper.add(EditType.COMPLETE_CHECK_CHANGE);
                    }
                
                    break;
                }
            }
        }
    }
}
