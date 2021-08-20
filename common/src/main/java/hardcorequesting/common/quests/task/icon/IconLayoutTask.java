package hardcorequesting.common.quests.task.icon;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.data.TaskData;
import hardcorequesting.common.quests.task.PartList;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.quests.task.client.TaskGraphic;
import hardcorequesting.common.util.EditType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * A base class for tasks with sub-elements that uses item icons for display.
 * Provides the layout of a vertical list of elements, where each entry has an item icon, a name, and any extra info.
 */
public abstract class IconLayoutTask<T extends IconLayoutTask.Part, Data extends TaskData> extends QuestTask<Data> {
    
    protected static final int LIMIT = 4;
    
    public final PartList<T> parts;
    private final TaskGraphic graphic;
    
    public IconLayoutTask(Class<Data> dataType, EditType.Type type, Quest parent, String description, String longDescription) {
        super(dataType, parent, description, longDescription);
        parts = new PartList<>(this::createEmpty, type, LIMIT);
        graphic = createGraphic();
    }
    
    protected abstract TaskGraphic createGraphic();
    
    protected abstract T createEmpty();
    
    public void setIcon(int id, ItemStack stack) {
        parts.getOrCreateForModify(id).setIconStack(stack);
    }
    
    public void setName(int id, String str) {
        parts.getOrCreateForModify(id).setName(str);
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void draw(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY) {
        graphic.draw(matrices, gui, player, mX, mY);
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void onClick(GuiQuestBook gui, Player player, int mX, int mY, int b) {
        graphic.onClick(gui, player, mX, mY, b);
    }
    
    public abstract static class Part {
        private ItemStack iconStack = ItemStack.EMPTY;
        private String name = "New";
        
        public ItemStack getIconStack() {
            return iconStack;
        }
    
        public void setIconStack(@NotNull ItemStack iconStack) {
            this.iconStack = iconStack;
        }
    
        public String getName() {
            return name;
        }
    
        public void setName(String name) {
            this.name = name;
        }
    }
}
