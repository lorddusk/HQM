package hardcorequesting.common.client.interfaces;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.client.BookPage;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenu;
import hardcorequesting.common.client.interfaces.graphic.EditReputationGraphic;
import hardcorequesting.common.client.interfaces.graphic.Graphic;
import hardcorequesting.common.client.interfaces.widget.LargeButton;
import hardcorequesting.common.client.sounds.SoundHandler;
import hardcorequesting.common.quests.*;
import hardcorequesting.common.reputation.ReputationBar;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public class GuiQuestBook extends GuiBase {
    
    public static final int PAGE_WIDTH = 170;
    //region pixel info for all the things
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
    //the page is static to keep the same page loaded when the book is reopened
    @NotNull
    private static BookPage page = BookPage.MainPage.INSTANCE;
    private Graphic pageGraphic;
    public final boolean isOpBook;
    private final Player player;
    public QuestSet modifyingQuestSet;
    public ReputationBar modifyingBar;
    private int tick;
    private GuiEditMenu editMenu;
    private final LargeButton saveButton;
    private EditMode currentMode = EditMode.NORMAL;
    
    {
        saveButton = new LargeButton(this, "hqm.questBook.saveAll", 360, 10) {
            @Override
            public boolean isVisible() {
                return Quest.canQuestsBeEdited() && SaveHelper.isLarge();
            }
            
            @Override
            public void onClick() {
                save();
            }
        };
    }
    
    private GuiQuestBook(Player player, boolean isOpBook) {
        super(CommonComponents.EMPTY);
        this.player = player;
        this.isOpBook = isOpBook;
    }
    
    public static void resetBookPosition() {
        page = BookPage.MainPage.INSTANCE;
        
        EditReputationGraphic.selectedReputation = null;
    }
    
    public static void displayGui(Player player, boolean isOpBook) {
        if (player != null) {
            Minecraft mc = Minecraft.getInstance();
            if (!(mc.screen instanceof GuiQuestBook)) {
                mc.setScreen(new GuiQuestBook(player, isOpBook));
            }
        }
    }
    
    @Override
    protected void init() {
        if (pageGraphic == null)
            pageGraphic = page.createGraphic(this);
    
        if (Quest.canQuestsBeEdited()) {
            minecraft.keyboardHandler.setSendRepeatsToGui(true);
        }
        QuestingData data = QuestingDataManager.getInstance().getQuestingData(player);
        if (!data.playedLore && SoundHandler.hasLoreMusic()) {
            SoundHandler.triggerFirstLore();
            data.playedLore = true;
        }
    }
    
    @Override
    public void removed() {
        minecraft.keyboardHandler.setSendRepeatsToGui(false);
        SoundHandler.stopLoreMusic();
    }
    
    public int getTick() {
        return tick;
    }
    
    public void setEditMenu(GuiEditMenu editMenu) {
        this.editMenu = editMenu;
    }
    
    @Override
    public void render(PoseStack matrices, int x0, int y0, float f) {
        setBlitOffset(0);
    
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
        
        saveButton.render(matrices, x, y);
        
        applyColor(0xFFFFFFFF);
        ResourceHelper.bindResource(MAP_TEXTURE);
        
        
        if (shouldDisplayBackArrow()) {
            drawRect(matrices, BACK_ARROW_X, BACK_ARROW_Y, BACK_ARROW_SRC_X + (inBackArrowBounds(x, y) ? BACK_ARROW_WIDTH : 0), BACK_ARROW_SRC_Y, BACK_ARROW_WIDTH, BACK_ARROW_HEIGHT);
        }
        if (shouldDisplayMenuArrow()) {
            drawRect(matrices, MENU_ARROW_X, MENU_ARROW_Y, MENU_ARROW_SRC_X + (inMenuArrowBounds(x, y) ? MENU_ARROW_WIDTH : 0), MENU_ARROW_SRC_Y, MENU_ARROW_WIDTH, MENU_ARROW_HEIGHT);
        }
        
        if (editMenu == null) {
    
            pageGraphic.drawFull(matrices, x, y);
    
            if (currentMode == EditMode.DELETE) {
                matrices.pushPose();
                matrices.translate(0, 0, 200);
                drawCenteredString(matrices, Translator.translatable("hqm.questBook.warning"), 0, 0, 2F, TEXTURE_WIDTH, TEXTURE_HEIGHT, 0xFF0000);
                drawCenteredString(matrices, Translator.translatable("hqm.questBook.deleteOnClick"), 0, font.lineHeight * 2, 1F, TEXTURE_WIDTH, TEXTURE_HEIGHT, 0xFF0000);
                matrices.popPose();
            }
    
        } else {
            editMenu.drawFull(matrices, x, y);
        }
    
        saveButton.renderTooltip(matrices, x, y);
        
        if (shouldDisplayBackArrow() && inBackArrowBounds(x, y)) {
            renderTooltip(matrices, FormattedText.composite(
                    Translator.translatable("hqm.questBook.goBack"),
                    Translator.plain("\n"),
                    Translator.translatable("hqm.questBook.rightClick").withStyle(ChatFormatting.DARK_GRAY)
            ), x + left, y + top);
        } else if (shouldDisplayMenuArrow() && inMenuArrowBounds(x, y)) {
            renderTooltip(matrices, Translator.translatable("hqm.questBook.backToMenu"), x + left, y + top);
        }
    }
    
    @Override
    public boolean charTyped(char c, int k) {
        if (super.charTyped(c, k)) {
            return true;
        }
        if (editMenu != null) {
            editMenu.charTyped(c);
        } else {
            return pageGraphic.charTyped(c);
        }
        return true;
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        
        if (editMenu != null) {
            return editMenu.keyPressed(keyCode);
        } else if (pageGraphic.keyPressed(keyCode)) {
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
        
        if (shouldDisplayBackArrow() && inBackArrowBounds(x, y)) {
            button = 1;
            if (editMenu != null) {
                editMenu.save();
                editMenu.close();
                return true;
            }
        } else if (shouldDisplayMenuArrow() && inMenuArrowBounds(x, y)) {
            if (editMenu != null) {
                editMenu.save();
                editMenu.close();
                editMenu = null;
            }
            setPage(BookPage.MenuPage.INSTANCE);
            return true;
        }
        
        boolean buttonClicked = saveButton.onClick(x, y);
        
        if (Quest.canQuestsBeEdited()) {
            SaveHelper.onClick(this, x, y);
        }
        
        if (buttonClicked) return true;
        
        if (editMenu == null) {
            if (button == 1) {
                goBack();
                return true;
            } else {
                pageGraphic.onClick(x, y, button);
            }
        } else {
            editMenu.onClick(x, y, button);
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
            modifyingBar = null;
        }
        if (editMenu != null) {
            editMenu.onRelease(x, y, button);
        } else {
            pageGraphic.onRelease(x, y, button);
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
            editMenu.onDrag(x, y, button);
        } else {
            pageGraphic.onDrag(x, y, button);
        }
        return true;
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        double mX = mouseX - left;
        double mY = mouseY - top;
        
        if (editMenu != null) {
            editMenu.onScroll(mX, mY, scroll);
        } else {
            pageGraphic.onScroll(mX, mY, scroll);
        }
        return true;
    }
    
    @Override
    public void tick() {
        ++tick;
        super.tick();
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    public void goBack() {
        if (page.canGoBack()) {
            setPage(page.getParent());
        }
    }
    
    private void updatePosition(int x, int y) {
        if (Quest.canQuestsBeEdited() && currentMode == EditMode.MOVE) {
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
        modifyingBar = null;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public void save() {
        QuestLine.getActiveQuestLine().saveQuests(HardcoreQuestingCore.packManager);
        SaveHelper.onSave();
    }
    
    private boolean shouldDisplayMenuArrow() {
        return shouldDisplayBackArrow() && page.hasGoToMenuButton();
    }
    
    private boolean shouldDisplayBackArrow() {
        return page.canGoBack() && editMenu == null;
    }
    
    private boolean inMenuArrowBounds(int mX, int mY) {
        return inBounds(MENU_ARROW_X, MENU_ARROW_Y, MENU_ARROW_WIDTH, MENU_ARROW_HEIGHT, mX, mY);
    }
    
    private boolean inBackArrowBounds(int mX, int mY) {
        return inBounds(BACK_ARROW_X, BACK_ARROW_Y, BACK_ARROW_WIDTH, BACK_ARROW_HEIGHT, mX, mY);
    }
    
    public void setPage(BookPage page) {
        GuiQuestBook.page = Objects.requireNonNull(page);
        pageGraphic = page.createGraphic(this);
    }
}