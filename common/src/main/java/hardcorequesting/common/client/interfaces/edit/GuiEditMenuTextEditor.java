package hardcorequesting.common.client.interfaces.edit;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.LargeButton;
import hardcorequesting.common.client.interfaces.TextBoxLogic;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class GuiEditMenuTextEditor extends GuiEditMenu {
    
    private static final int TEXT_HEIGHT = 9;
    private static final int START_X = 20;
    private static final int START_Y = 20;
    private static final int LINES_PER_PAGE = 21;
    
    private final Consumer<String> resultConsumer;
    private final int limit;
    protected TextBoxLogic text;
    private final boolean isName;
    
    public static void display(GuiQuestBook gui, Player player, String txt, boolean isName, Consumer<String> resultConsumer) {
        gui.setEditMenu(new GuiEditMenuTextEditor(gui, player, txt, isName, -1, resultConsumer));
    }
    
    public static void display(GuiQuestBook gui, Player player, String txt, int limit, Consumer<String> resultConsumer) {
        gui.setEditMenu(new GuiEditMenuTextEditor(gui, player, txt, true, limit, resultConsumer));
    }
    
    protected GuiEditMenuTextEditor(GuiQuestBook gui, Player player, String txt, boolean isName, int limit, Consumer<String> resultConsumer) {
        super(gui, player, false);
    
        this.resultConsumer = resultConsumer;
        this.limit = limit;
        
        if (txt != null && !txt.isEmpty()) {
            txt = txt.replace("\n", "\\n");
        }
        
        this.text = new TextBoxLogic(gui, txt, 140, true);
        this.isName = isName;
        buttons.add(new LargeButton("hqm.textEditor.copyAll", 185, 20) {
            @Override
            public boolean isEnabled(GuiBase gui, Player player) {
                return true;
            }
            
            @Override
            public boolean isVisible(GuiBase gui, Player player) {
                return true;
            }
            
            @Override
            public void onClick(GuiBase gui, Player player) {
                Minecraft.getInstance().keyboardHandler.setClipboard(text.getText());
            }
        });
        
        buttons.add(new LargeButton("hqm.textEditor.paste", 245, 20) {
            @Override
            public boolean isEnabled(GuiBase gui, Player player) {
                return true;
            }
            
            @Override
            public boolean isVisible(GuiBase gui, Player player) {
                return true;
            }
            
            @Override
            public void onClick(GuiBase gui, Player player) {
                String clip = Minecraft.getInstance().keyboardHandler.getClipboard();
                if (!clip.isEmpty()) {
                    clip = clip.replace("\n", "\\n");
                }
                text.addText(gui, clip);
            }
        });
        
        buttons.add(new LargeButton("hqm.textEditor.clear", 185, 40) {
            @Override
            public boolean isEnabled(GuiBase gui, Player player) {
                return true;
            }
            
            @Override
            public boolean isVisible(GuiBase gui, Player player) {
                return true;
            }
            
            @Override
            public void onClick(GuiBase gui, Player player) {
                text.setTextAndCursor(gui, "");
            }
        });
        
        buttons.add(new LargeButton("hqm.textEditor.clearPaste", 245, 40) {
            @Override
            public boolean isEnabled(GuiBase gui, Player player) {
                return true;
            }
            
            @Override
            public boolean isVisible(GuiBase gui, Player player) {
                return true;
            }
            
            @Override
            public void onClick(GuiBase gui, Player player) {
                String clip = Minecraft.getInstance().keyboardHandler.getClipboard();
                if (!clip.isEmpty()) {
                    clip = clip.replace("\n", "\\n");
                }
                text.setTextAndCursor(gui, clip);
            }
        });
    }
    
    @Override
    public void draw(PoseStack matrices, GuiBase gui, int mX, int mY) {
        super.draw(matrices, gui, mX, mY);
        int page = text.getCursorLine(gui) / LINES_PER_PAGE;
        gui.drawString(matrices, text.getLines().stream().map(FormattedText::of).collect(Collectors.toList()), page * LINES_PER_PAGE, LINES_PER_PAGE, START_X, START_Y, 1F, 0x404040);
        gui.drawCursor(matrices, START_X + text.getCursorPositionX(gui) - 1, START_Y + text.getCursorPositionY(gui) - 3 - page * LINES_PER_PAGE * TEXT_HEIGHT, 10, 1F, 0xFF909090);
    }
    
    @Override
    public void onKeyStroke(GuiBase gui, char c, int k) {
        super.onKeyStroke(gui, c, k);
        text.onKeyStroke(gui, c, k);
    }
    
    @Override
    public void save(GuiBase gui) {
        String str = text.getText();
        if (str == null || str.isEmpty()) {
            str = I18n.get("hqm.textEditor.unnamed");
        }
        
        if (!isName) {
            str = str.replace("\\n", "\n");
        }
    
        if (limit >= 0) {
            while (gui.getStringWidth(str) > limit) {
                str = str.substring(0, str.length() - 1);
            }
        }
    
        resultConsumer.accept(str);
        
        if (isName) {
            SaveHelper.add(EditType.NAME_CHANGE);
        } else {
            SaveHelper.add(EditType.DESCRIPTION_CHANGE);
        }
    }
}
