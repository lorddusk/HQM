package hardcorequesting.common.client.interfaces.graphic;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.client.BookPage;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.ResourceHelper;
import hardcorequesting.common.client.interfaces.edit.TextMenu;
import hardcorequesting.common.client.interfaces.widget.ExtendedScrollBar;
import hardcorequesting.common.client.interfaces.widget.ScrollBar;
import hardcorequesting.common.client.sounds.SoundHandler;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestLine;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.FormattedText;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * A graphic element for displaying the main page of the quest book,
 * which contains an initial description and the quest line logo.
 */
@Environment(EnvType.CLIENT)
public class MainPageGraphic extends EditableGraphic {
    private static final String FRONT_KEY = "hqm_front_texture";
    private static final int DESCRIPTION_X = 180;
    private static final int DESCRIPTION_Y = 20;
    public static final int VISIBLE_MAIN_DESCRIPTION_LINES = 21;
    
    private final ExtendedScrollBar<FormattedText> mainDescriptionScroll;
    private List<FormattedText> cachedMainDescription;
    
    public MainPageGraphic(GuiQuestBook gui) {
        super(gui, EditMode.NORMAL, EditMode.RENAME);
        addScrollBar(mainDescriptionScroll = new ExtendedScrollBar<>(gui, ScrollBar.Size.LONG, 312, 18, DESCRIPTION_X,
                VISIBLE_MAIN_DESCRIPTION_LINES, this::getDescriptionLines));
    }
    
    @Override
    public void draw(PoseStack matrices, int mX, int mY) {
        super.draw(matrices, mX, mY);
        
        QuestLine questLine = QuestLine.getActiveQuestLine();
        gui.drawString(matrices, mainDescriptionScroll.getVisibleEntries(), DESCRIPTION_X, DESCRIPTION_Y, 0.7F, 0x404040);
        gui.drawCenteredString(matrices, Translator.translatable("hqm.questBook.start"), 0, 195, 0.7F, GuiQuestBook.PAGE_WIDTH, GuiQuestBook.TEXTURE_HEIGHT - 195, 0x707070);
        if (SoundHandler.hasLoreMusic() && !SoundHandler.isLorePlaying()) {
            gui.drawCenteredString(matrices, Translator.translatable("hqm.questBook.playAgain"), GuiQuestBook.PAGE_WIDTH, 195, 0.7F, GuiQuestBook.PAGE_WIDTH - 10, GuiQuestBook.TEXTURE_HEIGHT - 195, 0x707070);
        }
        if (questLine.front == null) {
            File file = new File(HardcoreQuestingCore.configDir.toFile(), "front.png");
            if (file.exists()) {
                try {
                    NativeImage img = NativeImage.read(new FileInputStream(file));
                    DynamicTexture dm = new DynamicTexture(img);
                    questLine.front = Minecraft.getInstance().getTextureManager().register(FRONT_KEY, dm);
                } catch (IOException ignored) {
                    questLine.front = ResourceHelper.getResource("front");
                }
            } else {
                questLine.front = ResourceHelper.getResource("front");
            }
        }
    
        if (questLine.front != null) {
            ResourceHelper.bindResource(questLine.front);
            gui.applyColor(0xFFFFFFFF);
            gui.drawRect(matrices, 20, 20, 0, 0, 140, 180);
        }
    }
    
    @Override
    public void onClick(int mX, int mY, int button) {
        super.onClick(mX, mY, button);
        
        if (mX > 0 && mX < GuiQuestBook.PAGE_WIDTH && mY > 205) {
            gui.setPage(BookPage.MenuPage.INSTANCE);
            SoundHandler.stopLoreMusic();
        } else if (mX > GuiQuestBook.PAGE_WIDTH && mX < GuiQuestBook.TEXTURE_WIDTH && mY > 205) {
            if (SoundHandler.hasLoreMusic() && !SoundHandler.isLorePlaying()) {
                SoundHandler.playLoreMusic();
            }
        } else {
            if (Quest.canQuestsBeEdited() && gui.getCurrentMode() == EditMode.RENAME && gui.inBounds(DESCRIPTION_X, DESCRIPTION_Y, 130, (int) (VISIBLE_MAIN_DESCRIPTION_LINES * GuiQuestBook.TEXT_HEIGHT * 0.7F), mX, mY)) {
                TextMenu.display(gui, gui.getPlayer().getUUID(), Quest.getRawMainDescription(), false, desc -> {
                    QuestLine.getActiveQuestLine().setMainDescription(desc);
                    cachedMainDescription = null;
                });
            }
        }
    }
    
    private List<FormattedText> getDescriptionLines() {
        if (cachedMainDescription == null)
            cachedMainDescription = gui.getLinesFromText(Translator.plain(Quest.getRawMainDescription()), 0.7F, 130);
        return cachedMainDescription;
    }
}