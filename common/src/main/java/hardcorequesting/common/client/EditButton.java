package hardcorequesting.common.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.ResourceHelper;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.ComponentCollector;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class EditButton {
    
    private static final int BUTTON_SIZE = 16;
    private static final int BUTTON_ICON_SIZE = 12;
    private static final int BUTTON_ICON_SRC_X = 0;
    private static final int BUTTON_ICON_SRC_Y = 0;
    private static final int EDIT_BUTTONS_PER_ROW = 2;
    private static final int EDIT_BUTTONS_SRC_PER_ROW = 8;
    
    private final Runnable onClick;
    private final int x;
    private final int y;
    private final EditMode mode;
    private List<FormattedText> text;
    
    public EditButton(EditMode mode, int id, Runnable onClick) {
        this.mode = mode;
        this.onClick = onClick;
    
        int x = id % EDIT_BUTTONS_PER_ROW;
        int y = id / EDIT_BUTTONS_PER_ROW;
        
        this.x = -38 + x * 20;
        this.y = 5 + y * 20;
    }
    
    public static EditButton[] createButtons(Consumer<EditMode> setter, EditMode... modes) {
        EditButton[] ret = new EditButton[modes.length];
        for (int i = 0; i < modes.length; i++) {
            EditMode mode = modes[i];
            ret[i] = new EditButton(mode, i, () -> setter.accept(mode));
        }
        return ret;
    }
    
    @Environment(EnvType.CLIENT)
    public void draw(GuiQuestBook gui, GuiGraphics graphics, int mX, int mY) {
        int srcY = gui.getCurrentMode() == mode ? 2 : gui.inBounds(x, y, BUTTON_SIZE, BUTTON_SIZE, mX, mY) ? 1 : 0;
        gui.drawRect(graphics, GuiBase.MAP_TEXTURE, x, y, 256 - BUTTON_SIZE, srcY * BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE);
        gui.drawRect(graphics, GuiBase.MAP_TEXTURE, x + 2, y + 2,
                BUTTON_ICON_SRC_X + (mode.ordinal() % EDIT_BUTTONS_SRC_PER_ROW) * BUTTON_ICON_SIZE,
                BUTTON_ICON_SRC_Y + (mode.ordinal() / EDIT_BUTTONS_SRC_PER_ROW) * BUTTON_ICON_SIZE,
                BUTTON_ICON_SIZE, BUTTON_ICON_SIZE);
    }
    
    @Environment(EnvType.CLIENT)
    public void drawInfo(GuiQuestBook gui, GuiGraphics graphics, int mX, int mY) {
        if (gui.inBounds(x, y, BUTTON_SIZE, BUTTON_SIZE, mX, mY)) {
            if (this.text == null) {
                this.text = new ArrayList<>();
                if (KeyboardHandler.keyMap.containsValue(mode)) {
                    List<String> builder = new ArrayList<>();
                    for (Map.Entry<Integer, EditMode> entry : KeyboardHandler.keyMap.entries()) {
                        if (entry.getValue() == mode)
                            builder.add("§7" + StringUtils.capitalize(InputConstants.Type.KEYSYM.getOrCreate(entry.getKey()).getDisplayName().getString()));
                    }
                    this.text.add(Translator.translatable("hqm.editMode.keybind", mode.getName(), String.join(", ", builder)));
                } else {
                    this.text.add(FormattedText.of(mode.getName()));
                }
                this.text.addAll(gui.getLinesFromText(Translator.plain("\n" + mode.getDescription()), 1F, 150));
                for (int i = 1; i < this.text.size(); i++) {
                    ComponentCollector collector = new ComponentCollector();
                    this.text.get(i).visit((style, string) -> {
                        collector.append(FormattedText.of(string, style));
                        return Optional.empty();
                    }, Style.EMPTY);
                    this.text.set(i, collector.getResultOrEmpty());
                }
            }
            gui.renderTooltipL(graphics, this.text, mX + gui.getLeft(), mY + gui.getTop());
        }
    }
    
    @Environment(EnvType.CLIENT)
    public boolean onClick(GuiQuestBook gui, int mX, int mY) {
        if (gui.inBounds(x, y, BUTTON_SIZE, BUTTON_SIZE, mX, mY)) {
            click();
            return true;
        }
        
        return false;
    }
    
    @Environment(EnvType.CLIENT)
    public void click() {
        onClick.run();
    }
    
    public boolean matchesMode(EditMode mode) {
        return this.mode == mode;
    }
}
