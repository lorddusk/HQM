package hardcorequesting.common.client.interfaces;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.bag.Group;
import hardcorequesting.common.client.BookPage;
import hardcorequesting.common.client.EditButton;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenu;
import hardcorequesting.common.client.interfaces.graphic.Graphic;
import hardcorequesting.common.client.interfaces.graphic.MainPageGraphic;
import hardcorequesting.common.client.interfaces.widget.LargeButton;
import hardcorequesting.common.client.sounds.SoundHandler;
import hardcorequesting.common.network.NetworkManager;
import hardcorequesting.common.network.message.CloseBookMessage;
import hardcorequesting.common.quests.*;
import hardcorequesting.common.reputation.Reputation;
import hardcorequesting.common.reputation.ReputationBar;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class GuiQuestBook extends GuiBase {
    
    public static final int PAGE_WIDTH = 170;
    //region pixel info for all the things
    public static final int VISIBLE_DISPLAY_REPUTATIONS = 4;
    public static final int TEXT_HEIGHT = 9;
    public static final int TEXT_SPACING = 20;
    public static final int GROUP_ITEMS_X = 20;
    public static final int GROUP_ITEMS_Y = 40;
    public static final int GROUP_ITEMS_SPACING = 20;
    public static final int ITEMS_PER_LINE = 7;
    public static final int TEXTURE_WIDTH = 170 * 2;
    public static final int TEXTURE_HEIGHT = 234;
    private static final int BACK_ARROW_X = 9;
    private static final int BACK_ARROW_Y = 219;
    private static final int BACK_ARROW_SRC_X = 0;
    private static final int BACK_ARROW_SRC_Y = 113;
    private static final int BACK_ARROW_WIDTH = 15;
    private static final int BACK_ARROW_HEIGHT = 10;
    private static final int MENU_ARROW_X = 161;
    private static final int MENU_ARROW_Y = 217;
    private static final int MENU_ARROW_SRC_X = 0;
    private static final int MENU_ARROW_SRC_Y = 104;
    private static final int MENU_ARROW_WIDTH = 14;
    private static final int MENU_ARROW_HEIGHT = 9;
    //endregion
    private static final ResourceLocation BG_TEXTURE = ResourceHelper.getResource("book");
    //these are static to keep the same page loaded when the book is reopened
    private static BookPage page;
    private Graphic pageGraphic;
    private final MainPageGraphic mainPageGraphic = new MainPageGraphic(this);
    public static Reputation selectedReputation;
    private static ItemStack selectedStack;
    public final boolean isOpBook;
    private final Player player;
    public Group modifyingGroup;
    public QuestSet modifyingQuestSet;
    public Quest modifyingQuest;
    public ReputationBar modifyingBar;
    private int tick;
    private GuiEditMenu editMenu;
    private final LargeButton saveButton;
    private EditMode currentMode = EditMode.NORMAL;
    
    {
        saveButton = new LargeButton("hqm.questBook.saveAll", 360, 10) {
            @Override
            public boolean isEnabled(GuiBase gui) {
                return true;
            }
            
            @Override
            public boolean isVisible(GuiBase gui) {
                return Quest.canQuestsBeEdited() && SaveHelper.isLarge();
            }
            
            @Override
            public void onClick(GuiBase gui) {
                save();
            }
        };
    }
    
    private GuiQuestBook(Player player, boolean isOpBook) {
        super(NarratorChatListener.NO_TITLE);
        this.player = player;
        this.isOpBook = isOpBook;
        
        if (page != null)
            pageGraphic = page.createGraphic(this);
        
        if (Quest.canQuestsBeEdited()) {
            Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true);
        }
        QuestingData data = QuestingDataManager.getInstance().getQuestingData(player);
        if (!data.playedLore && SoundHandler.hasLoreMusic()) {
            SoundHandler.triggerFirstLore();
            data.playedLore = true;
        }
    }
    
    public static void resetBookPosition() {
        page = null;
        
        selectedReputation = null;
    }
    
    public static void displayGui(Player player, boolean isOpBook) {
        if (player != null) {
            Minecraft mc = Minecraft.getInstance();
            if (!(mc.screen instanceof GuiQuestBook)) {
                mc.setScreen(new GuiQuestBook(player, isOpBook));
            }
        }
    }
    
    public static void setSelectedStack(ItemStack stack) {
        selectedStack = stack;
    }
    
    public int getTick() {
        return tick;
    }
    
    @Override
    public void setEditMenu(GuiEditMenu editMenu) {
        this.editMenu = editMenu;
    }
    
    @Override
    public void render(PoseStack matrices, int x0, int y0, float f) {
        setBlitOffset(0);
        
        selectedStack = null;
        left = (width - TEXTURE_WIDTH) / 2;
        top = (height - TEXTURE_HEIGHT) / 2;
        
        int x = x0 - left;
        int y = y0 - top;
        
        applyColor(0xFFFFFFFF);
        ResourceHelper.bindResource(BG_TEXTURE);
        
        drawRect(matrices, 0, 0, 0, 0, PAGE_WIDTH, TEXTURE_HEIGHT);
        drawRect(matrices, PAGE_WIDTH, 0, 0, 0, PAGE_WIDTH, TEXTURE_HEIGHT, RenderRotation.FLIP_HORIZONTAL);
        
        if (Quest.canQuestsBeEdited()) {
            applyColor(0xFFFFFFFF);
            ResourceHelper.bindResource(MAP_TEXTURE);
            SaveHelper.render(matrices, this, x, y);
        }
        
        saveButton.draw(matrices, this, x, y);
        
        applyColor(0xFFFFFFFF);
        ResourceHelper.bindResource(MAP_TEXTURE);
        
        
        if (shouldDisplayControlArrow(false)) {
            drawRect(matrices, BACK_ARROW_X, BACK_ARROW_Y, BACK_ARROW_SRC_X + (inArrowBounds(false, x, y) ? BACK_ARROW_WIDTH : 0), BACK_ARROW_SRC_Y, BACK_ARROW_WIDTH, BACK_ARROW_HEIGHT);
        }
        if (shouldDisplayControlArrow(true)) {
            drawRect(matrices, MENU_ARROW_X, MENU_ARROW_Y, MENU_ARROW_SRC_X + (inArrowBounds(true, x, y) ? MENU_ARROW_WIDTH : 0), MENU_ARROW_SRC_Y, MENU_ARROW_WIDTH, MENU_ARROW_HEIGHT);
        }
        
        if (editMenu == null) {
            
            if (pageGraphic == null) {
                mainPageGraphic.drawFull(matrices, this, x, y);
            } else {
                pageGraphic.drawFull(matrices, this, x, y);
            }
    
    
            if (currentMode == EditMode.DELETE) {
                matrices.pushPose();
                matrices.translate(0, 0, 200);
                drawCenteredString(matrices, Translator.translatable("hqm.questBook.warning"), 0, 0, 2F, TEXTURE_WIDTH, TEXTURE_HEIGHT, 0xFF0000);
                drawCenteredString(matrices, Translator.translatable("hqm.questBook.deleteOnClick"), 0, font.lineHeight * 2, 1F, TEXTURE_WIDTH, TEXTURE_HEIGHT, 0xFF0000);
                matrices.popPose();
            }
    
        } else {
            editMenu.draw(matrices, this, x, y);
            editMenu.renderTooltip(matrices, this, x, y);
        }
    
        saveButton.renderTooltip(matrices, this, x, y);
        
        if (shouldDisplayAndIsInArrowBounds(false, x, y)) {
            renderTooltip(matrices, FormattedText.composite(
                    Translator.translatable("hqm.questBook.goBack"),
                    Translator.plain("\n"),
                    Translator.translatable("hqm.questBook.rightClick", GuiColor.GRAY)
            ), x + left, y + top);
        } else if (shouldDisplayAndIsInArrowBounds(true, x, y)) {
            renderTooltip(matrices, Translator.translatable("hqm.questBook.backToMenu"), x + left, y + top);
        }
    }
    
    @Override
    public boolean charTyped(char c, int k) {
        if (super.charTyped(c, k)) {
            return true;
        }
        if (editMenu != null) {
            editMenu.onKeyStroke(this, c, -1);
        } else if (pageGraphic != null) {
            return pageGraphic.charTyped(this, c);
        } else {
            return false;
        }
        return true;
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        
        if (editMenu != null) {
            editMenu.onKeyStroke(this, Character.MIN_VALUE, keyCode);
        } else if (pageGraphic != null && pageGraphic.keyPressed(this, keyCode)) {
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            goBack();
            return true;
        }
        return false;
    }
    
    @Override
    public boolean mouseClicked(double x0, double y0, int button) {
        if (super.mouseClicked(x0, y0, button)) {
            return true;
        }
        
        int x = (int) (x0 - left);
        int y = (int) (y0 - top);
        
        if (shouldDisplayAndIsInArrowBounds(false, x, y)) {
            button = 1;
            if (editMenu != null) {
                editMenu.save(this);
                editMenu.close(this);
                return true;
            }
        } else if (shouldDisplayAndIsInArrowBounds(true, x, y)) {
            if (editMenu != null) {
                editMenu.save(this);
                editMenu.close(this);
                editMenu = null;
            }
            setPage(null);
            return true;
        }
        
        boolean buttonClicked = false;
    
        if (saveButton.isVisible(this) && saveButton.isEnabled(this) && saveButton.inButtonBounds(this, x, y)) {
            saveButton.onClick(this);
            buttonClicked = true;
        }
        
        if (Quest.canQuestsBeEdited()) {
            SaveHelper.onClick(this, x, y);
        }
        
        if (buttonClicked) return true;
        
        if (editMenu == null) {
            if (pageGraphic == null) {
                mainPageGraphic.onClick(this, x, y, button);
            } else {
                if (button == 1) {
                    goBack();
                    return true;
                } else {
                    pageGraphic.onClick(this, x, y, button);
                }
            }
        } else {
            editMenu.onClick(this, x, y, button);
        }
        return true;
    }
    
    @Override
    public boolean mouseReleased(double x0, double y0, int button) {
        if (super.mouseReleased(x0, y0, button)) {
            return true;
        }
        
        int x = (int) (x0 - left);
        int y = (int) (y0 - top);
        
        updatePosition(x, y);
        if (currentMode == EditMode.MOVE) {
            modifyingQuest = null;
            modifyingBar = null;
        }
        if (editMenu != null) {
            editMenu.onRelease(this, x, y);
        } else if (pageGraphic != null) {
            pageGraphic.onRelease(this, x, y, button);
        }
        return true;
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
        }
        int x = (int) (mouseX - left);
        int y = (int) (mouseY - top);
        
        updatePosition(x, y);
        if (editMenu != null) {
            editMenu.onDrag(this, x, y);
        } else if (pageGraphic != null) {
            pageGraphic.onDrag(this, x, y, button);
        }
        return true;
    }
    
    @Override
    public boolean mouseScrolled(double x, double y, double scroll) {
        if (editMenu != null) {
            editMenu.onScroll(this, x, y, scroll);
        } else if (pageGraphic != null) {
            pageGraphic.onScroll(this, x, y, scroll);
        }
        return true;
    }
    
    @Override
    public void tick() {
        ++tick;
        super.tick();
    }
    
    @Override
    public void removed() {
        NetworkManager.sendToServer(new CloseBookMessage(player.getUUID()));
        minecraft.keyboardHandler.setSendRepeatsToGui(true);
        SoundHandler.stopLoreMusic();
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    public void goBack() {
        if (page != null) {
            setPage(page.getParent());
        }
    }
    
    private void updatePosition(int x, int y) {
        if (Quest.canQuestsBeEdited() && currentMode == EditMode.MOVE) {
            if (modifyingQuest != null) {
                modifyingQuest.setGuiCenterX(x);
                modifyingQuest.setGuiCenterY(y);
            }
            if (modifyingBar != null) {
                modifyingBar.moveTo(x, y);
            }
        }
    }
    
    public EditMode getCurrentMode() {
        return currentMode;
    }
    
    public void setCurrentMode(EditMode mode) {
        currentMode = mode;
        modifyingQuest = null;
        modifyingBar = null;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public void save() {
        QuestLine.getActiveQuestLine().saveAll();
        SaveHelper.onSave();
    }
    
    public void drawEditButtons(PoseStack matrices, int mX, int mY, EditButton[] editButtons) {
        if (Quest.canQuestsBeEdited()) {
            for (EditButton button : editButtons) {
                button.draw(this, matrices, mX, mY);
            }
        }
    }
    
    public void drawEditButtonTooltip(PoseStack matrices, int mX, int mY, EditButton[] editButtons) {
        if (Quest.canQuestsBeEdited()) {
            for (EditButton button : editButtons) {
                button.drawInfo(this, matrices, mX, mY);
            }
        }
    }
    
    public void handleEditButtonClick(int mX, int mY, EditButton[] editButtons) {
        if (Quest.canQuestsBeEdited()) {
            for (EditButton editButton : editButtons) {
                if (editButton.onClick(this, mX, mY)) {
                    break;
                }
            }
        }
    }
    
    private boolean shouldDisplayControlArrow(boolean isMenuArrow) {
        return page != null && (
                editMenu == null && (!isMenuArrow || page.hasGoToMenuButton())
                        || editMenu != null && !editMenu.hasButtons());
    }
    
    private boolean inArrowBounds(boolean isMenuArrow, int mX, int mY) {
        if (isMenuArrow) {
            return inBounds(MENU_ARROW_X, MENU_ARROW_Y, MENU_ARROW_WIDTH, MENU_ARROW_HEIGHT, mX, mY);
        } else {
            return inBounds(BACK_ARROW_X, BACK_ARROW_Y, BACK_ARROW_WIDTH, BACK_ARROW_HEIGHT, mX, mY);
        }
    }
    
    private boolean shouldDisplayAndIsInArrowBounds(boolean isMenuArrow, int mX, int mY) {
        return shouldDisplayControlArrow(isMenuArrow) && inArrowBounds(isMenuArrow, mX, mY);
    }
    
    public void setPage(BookPage page) {
        GuiQuestBook.page = page;
        pageGraphic = page == null ? null : page.createGraphic(this);
    }
}