package hardcorequesting.common.quests.task;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A base class for tasks with sub-elements that uses item icons for display.
 */
public abstract class IconQuestTask<T extends IconQuestTask.IconTask> extends QuestTask {
    private static final int Y_OFFSET = 30;
    private static final int X_TEXT_OFFSET = 23;
    private static final int X_TEXT_INDENT = 0;
    private static final int Y_TEXT_OFFSET = 0;
    
    public final List<T> elements;
    private final List<T> elementsWithEmpty;
    
    public IconQuestTask(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
        
        List<T> list = new ArrayList<>();
        list.add(createEmpty());
        elements = list.subList(0, 0);
        elementsWithEmpty = Collections.unmodifiableList(list);
    }
    
    protected abstract T createEmpty();
    
    protected abstract void onAddElement(Player player);
    
    protected abstract void onModifyElement(Player player);
    
    @Environment(EnvType.CLIENT)
    protected abstract void drawElementText(PoseStack matrices, GuiQuestBook gui, Player player, T element, int index, int x, int y);
    
    protected final List<T> getShownElements() {
        if (Quest.canQuestsBeEdited()) {
            return elementsWithEmpty;
        } else {
            return elements;
        }
    }
    
    protected final T getOrCreateForModify(int id, Player player) {
        if (id >= elements.size()) {
            T element = createEmpty();
            elements.add(element);
            onAddElement(player);
            return element;
        } else {
            onModifyElement(player);
            return elements.get(id);
        }
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public final void draw(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY) {
        List<T> renderElements = getShownElements();
        for (int i = 0; i < renderElements.size(); i++) {
            T element = renderElements.get(i);
            
            int x = START_X;
            int y = START_Y + i * Y_OFFSET;
            gui.drawItemStack(element.getIconStack(), x, y, mX, mY, false);
            gui.drawString(matrices, Translator.plain(element.getName()), x + X_TEXT_OFFSET, y + Y_TEXT_OFFSET, 0x404040);
            
            drawElementText(matrices, gui, player, element, i, x + X_TEXT_OFFSET + X_TEXT_INDENT, y + Y_TEXT_OFFSET + 9);
        }
    }
    
    protected abstract static class IconTask {
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
