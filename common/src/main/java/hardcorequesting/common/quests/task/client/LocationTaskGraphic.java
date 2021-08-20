package hardcorequesting.common.quests.task.client;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiColor;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.LocationMenu;
import hardcorequesting.common.quests.task.icon.VisitLocationTask;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public class LocationTaskGraphic extends IconTaskGraphic<VisitLocationTask.Part> {
    
    private final VisitLocationTask task;
    
    public LocationTaskGraphic(VisitLocationTask task) {
        super(task);
        this.task = task;
    }
    
    @Override
    protected void drawElementText(PoseStack matrices, GuiQuestBook gui, Player player, VisitLocationTask.Part part, int index, int x, int y) {
        if (task.visited(index, player)) {
            gui.drawString(matrices, Translator.translatable("hqm.locationMenu.visited", GuiColor.GREEN), x, y, 0.7F, 0x404040);
        } else if (part.getVisibility().doShowCoordinate()) {
            int row = 0;
            if (part.getRadius() >= 0) {
                gui.drawString(matrices, Translator.plain("(" + part.getPosition().toShortString() + ")"), x, y, 0.7F, 0x404040);
                row++;
            }
            
            if (Objects.equals(player.getCommandSenderWorld().dimension().location().toString(), part.getDimension())) {
                if (part.getRadius() >= 0) {
                    FormattedText str;
                    int distance = (int) player.distanceToSqr(part.getPosition().getX() + 0.5, part.getPosition().getY() + 0.5, part.getPosition().getZ() + 0.5);
                    str = Translator.translatable("hqm.locationMenu.mAway", distance);
                    if (part.getVisibility().doShowRadius()) {
                        str = FormattedText.composite(str, Translator.plain(" ["), Translator.translatable("hqm.locationMenu.mRadius", part.getRadius()), Translator.plain("]"));
                    }
                    gui.drawString(matrices, str, x, y + 6*row, 0.7F, 0x404040);
                }
                
            } else {
                gui.drawString(matrices, Translator.translatable("hqm.locationMenu.wrongDim"), x, y + 6*row, 0.7F, 0x404040);
            }
        }
    }
    
    @Override
    protected void handlePartClick(GuiQuestBook gui, Player player, EditMode mode, VisitLocationTask.Part part, int id) {
        if (mode == EditMode.LOCATION) {
            LocationMenu.display(gui, player, part.getVisibility(), part.getPosition(), part.getRadius(), part.getDimension(),
                    result -> task.setInfo(id, result.getVisibility(), result.getPos(), result.getRadius(), result.getDimension()));
        } else {
            super.handlePartClick(gui, player, mode, part, id);
        }
    }
}