package hardcorequesting.common.client.interfaces;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.bag.Group;
import hardcorequesting.common.bag.GroupTier;
import hardcorequesting.common.bag.GroupTierManager;
import hardcorequesting.common.client.BookPage;
import hardcorequesting.common.client.EditButton;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.KeyboardHandler;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenu;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenuDeath;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenuTeam;
import hardcorequesting.common.client.interfaces.edit.TextMenu;
import hardcorequesting.common.client.interfaces.graphic.Graphic;
import hardcorequesting.common.client.interfaces.graphic.QuestSetsGraphic;
import hardcorequesting.common.client.interfaces.widget.LargeButton;
import hardcorequesting.common.client.interfaces.widget.ScrollBar;
import hardcorequesting.common.client.interfaces.widget.TextBoxGroup;
import hardcorequesting.common.client.sounds.SoundHandler;
import hardcorequesting.common.config.HQMConfig;
import hardcorequesting.common.death.DeathStatsManager;
import hardcorequesting.common.items.ModItems;
import hardcorequesting.common.network.NetworkManager;
import hardcorequesting.common.network.message.CloseBookMessage;
import hardcorequesting.common.quests.*;
import hardcorequesting.common.reputation.Reputation;
import hardcorequesting.common.reputation.ReputationBar;
import hardcorequesting.common.reputation.ReputationManager;
import hardcorequesting.common.reputation.ReputationMarker;
import hardcorequesting.common.team.PlayerEntry;
import hardcorequesting.common.team.Team;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.OPBookHelper;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class GuiQuestBook extends GuiBase {
    
    public static final int PAGE_WIDTH = 170;
    //region pixel info for all the things
    public static final int VISIBLE_REPUTATION_TIERS = 9;
    public static final int VISIBLE_REPUTATIONS = 10;
    public static final int VISIBLE_DISPLAY_REPUTATIONS = 4;
    public static final int TEXT_HEIGHT = 9;
    public static final int TEXT_SPACING = 20;
    private static final int DESCRIPTION_X = 180;
    private static final int DESCRIPTION_Y = 20;
    public static final int VISIBLE_MAIN_DESCRIPTION_LINES = 21;
    public static final int TIERS_X = 180;
    public static final int TIERS_Y = 20;
    public static final int TIERS_SPACING = 25;
    public static final int TIERS_SECOND_LINE_X = -5;
    public static final int TIERS_SECOND_LINE_Y = 12;
    public static final int WEIGHT_SPACING = 25;
    public static final int VISIBLE_TIERS = 8;
    public static final int GROUPS_X = 20;
    public static final int GROUPS_Y = 20;
    public static final int GROUPS_SPACING = 25;
    public static final int GROUPS_SECOND_LINE_X = 5;
    public static final int GROUPS_SECOND_LINE_Y = 12;
    public static final int VISIBLE_GROUPS = 8;
    public static final int GROUP_ITEMS_X = 20;
    public static final int GROUP_ITEMS_Y = 40;
    public static final int GROUP_ITEMS_SPACING = 20;
    public static final int ITEMS_PER_LINE = 7;
    private static final String FRONT_KEY = "hqm_front_texture";
    private static final int TEXTURE_WIDTH = 170 * 2;
    private static final int TEXTURE_HEIGHT = 234;
    private static final int INFO_RIGHT_X = 180;
    private static final int INFO_LIVES_Y = 20;
    private static final int INFO_DEATHS_Y = 55;
    private static final int INFO_TEAM_Y = 95;
    private static final int INFO_LEFT_X = 20;
    private static final int INFO_QUESTS_Y = 20;
    private static final int INFO_REPUTATION_Y = 110;
    private static final int INFO_HEARTS_X = 5;
    private static final int INFO_HEARTS_Y = 12;
    private static final int INFO_HEARTS_SPACING = 18;
    private static final int TEAM_TEXT_Y = 12;
    private static final int TEAM_CLICK_TEXT_Y = 30;
    private static final int DEATH_TEXT_Y = 0;
    private static final int DEATH_CLICK_TEXT_Y = 10;
    private static final int QUEST_CLICK_TEXT_Y = 67;
    private static final int INFO_REPUTATION_OFFSET_X = 5;
    private static final int INFO_REPUTATION_OFFSET_Y = 12;
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
    private Graphic graphic;
    public static Group selectedGroup;
    public static Reputation selectedReputation;
    private static boolean isMainPageOpen = true;
    private static boolean isMenuPageOpen = true;
    private static boolean isBagPage;
    private static boolean isReputationPage;
    private static ItemStack selectedStack;
    public final boolean isOpBook;
    private final Player player;
    public ScrollBar reputationDisplayScroll;
    public ScrollBar reputationScroll;
    public ScrollBar reputationTierScroll;
    public Group modifyingGroup;
    public QuestSet modifyingQuestSet;
    public Quest modifyingQuest;
    public ReputationBar modifyingBar;
    private ScrollBar mainDescriptionScroll;
    private ScrollBar groupScroll;
    private ScrollBar tierScroll;
    private List<ScrollBar> scrollBars;
    private TextBoxGroup.TextBox textBoxGroupAmount;
    private TextBoxGroup textBoxes;
    private int tick;
    private GuiEditMenu editMenu;
    private LargeButton saveButton;
    private List<LargeButton> buttons = new ArrayList<LargeButton>();
    private EditMode currentMode = EditMode.NORMAL;
    private final EditButton[] groupButtons = EditButton.createButtons(this::setCurrentMode, EditMode.NORMAL, EditMode.ITEM, EditMode.DELETE);
    private final EditButton[] bagButtons = EditButton.createButtons(this::setCurrentMode, EditMode.NORMAL, EditMode.CREATE, EditMode.RENAME, EditMode.TIER, EditMode.DELETE);
    private final EditButton[] reputationButtons = EditButton.createButtons(this::setCurrentMode, EditMode.NORMAL, EditMode.CREATE, EditMode.RENAME, EditMode.REPUTATION_VALUE, EditMode.DELETE);
    private final EditButton[] mainButtons = EditButton.createButtons(this::setCurrentMode, EditMode.NORMAL, EditMode.RENAME);
    private final EditButton[] menuButtons = EditButton.createButtons(this::setCurrentMode, EditMode.NORMAL, EditMode.BAG, EditMode.REPUTATION);
    
    {
        scrollBars = new ArrayList<>();
        scrollBars.add(mainDescriptionScroll = new ScrollBar(312, 18, 186, 171, 69, DESCRIPTION_X) {
            @Override
            public boolean isVisible(GuiBase gui) {
                return !isReputationPage && !isBagPage && isMainPageOpen && Quest.getMainDescription(gui).size() > VISIBLE_MAIN_DESCRIPTION_LINES;
            }
        });
        
        scrollBars.add(groupScroll = new ScrollBar(160, 18, 186, 171, 69, GROUPS_X) {
            @Override
            public boolean isVisible(GuiBase gui) {
                return !isReputationPage && isBagPage && selectedGroup == null && Group.getGroups().size() > VISIBLE_GROUPS;
            }
        });
        
        scrollBars.add(tierScroll = new ScrollBar(312, 18, 186, 171, 69, TIERS_X) {
            @Override
            public boolean isVisible(GuiBase gui) {
                return !isReputationPage && isBagPage && selectedGroup == null && GroupTierManager.getInstance().getTiers().size() > VISIBLE_TIERS;
            }
        });
        
        scrollBars.add(reputationTierScroll = new ScrollBar(312, 23, 186, 171, 69, Reputation.REPUTATION_MARKER_LIST_X) {
            @Override
            public boolean isVisible(GuiBase gui) {
                return isReputationPage && !isBagPage && !isMainPageOpen && selectedReputation != null && selectedReputation.getMarkerCount() > VISIBLE_REPUTATION_TIERS;
            }
        });
        
        scrollBars.add(reputationScroll = new ScrollBar(160, 23, 186, 171, 69, Reputation.REPUTATION_LIST_X) {
            @Override
            public boolean isVisible(GuiBase gui) {
                return isReputationPage && !isBagPage && (getCurrentMode() != EditMode.CREATE || selectedReputation == null) && ReputationManager.getInstance().size() > VISIBLE_REPUTATIONS;
            }
        });
        
        scrollBars.add(reputationDisplayScroll = new ScrollBar(160, 125, 87, 164, 69, INFO_LEFT_X) {
            @Override
            public boolean isVisible(GuiBase gui) {
                return isMenuPageOpen && !isMainPageOpen && ReputationManager.getInstance().size() > VISIBLE_DISPLAY_REPUTATIONS;
            }
        });
    }
    
    {
        textBoxes = new TextBoxGroup();
        textBoxes.add(textBoxGroupAmount = new TextBoxGroup.TextBox(this, "0", 180, 30, false) {
            @Override
            protected boolean isCharacterValid(char c, String rest) {
                return rest.length() < 3 && Character.isDigit(c);
            }
            
            @Override
            public void textChanged(GuiBase gui) {
                try {
                    int number;
                    if (getText().equals("")) {
                        number = 1;
                    } else {
                        number = Integer.parseInt(getText());
                    }
                    
                    if (selectedGroup != null) {
                        selectedGroup.setLimit(number);
                    }
                } catch (Exception ignored) {
                }
                
            }
        });
    }
    
    {
        buttons.add(saveButton = new LargeButton("hqm.questBook.saveAll", 360, 10) {
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
        });
        
        buttons.add(new LargeButton("hqm.questBook.createGroup", 100, 175) {
            @Override
            public boolean isEnabled(GuiBase gui) {
                return true;
            }
            
            @Override
            public boolean isVisible(GuiBase gui) {
                return editMenu == null && isBagPage && currentMode == EditMode.CREATE && selectedGroup == null && !isMainPageOpen && !isMenuPageOpen && !isReputationPage;
            }
            
            @Override
            public void onClick(GuiBase gui) {
                Group.add(new Group(null));
                SaveHelper.add(EditType.GROUP_CREATE);
            }
        });
        
        buttons.add(new LargeButton("hqm.questBook.createTier", 100, 200) {
            @Override
            public boolean isEnabled(GuiBase gui) {
                return true;
            }
            
            @Override
            public boolean isVisible(GuiBase gui) {
                return editMenu == null && isBagPage && currentMode == EditMode.CREATE && selectedGroup == null && !isMainPageOpen && !isMenuPageOpen && !isReputationPage;
            }
            
            @Override
            public void onClick(GuiBase gui) {
                GroupTierManager.getInstance().getTiers().add(new GroupTier("New Tier", GuiColor.BLACK, 0, 0, 0, 0, 0));
                SaveHelper.add(EditType.TIER_CREATE);
            }
        });
        
        buttons.add(new LargeButton("Reset", 90, 190) {
            @Override
            public boolean isEnabled(GuiBase gui) {
                return Screen.hasControlDown() && Screen.hasShiftDown();
            }
            
            @Override
            public boolean isVisible(GuiBase gui) {
                return editMenu == null && !isBagPage && !isMainPageOpen && isOpBook && isMenuPageOpen && !isReputationPage;
            }
            
            @Override
            public void onClick(GuiBase gui) {
                OPBookHelper.reset(GuiQuestBook.this.player.getUUID());
            }
        });
        
        buttons.add(new LargeButton("Create New", 180, 20) {
            @Override
            public boolean isEnabled(GuiBase gui) {
                return true;
            }
            
            @Override
            public boolean isVisible(GuiBase gui) {
                return editMenu == null && !isBagPage && currentMode == EditMode.CREATE && selectedReputation == null && !isMainPageOpen && !isMenuPageOpen && isReputationPage;
            }
            
            @Override
            public void onClick(GuiBase gui) {
                ReputationManager.getInstance().addReputation(new Reputation("Unnamed", "Neutral"));
                SaveHelper.add(EditType.REPUTATION_ADD);
            }
        });
        
        buttons.add(new LargeButton("hqm.questBook.createTier", 20, 20) {
            @Override
            public boolean isEnabled(GuiBase gui) {
                return true;
            }
            
            @Override
            public boolean isVisible(GuiBase gui) {
                return editMenu == null && !isBagPage && currentMode == EditMode.CREATE && selectedReputation != null && !isMainPageOpen && !isMenuPageOpen && isReputationPage;
            }
            
            @Override
            public void onClick(GuiBase gui) {
                selectedReputation.add(new ReputationMarker("Unnamed", 0, false));
                SaveHelper.add(EditType.REPUTATION_MARKER_CREATE);
            }
        });
    }
    
    private GuiQuestBook(Player player, boolean isOpBook) {
        super(NarratorChatListener.NO_TITLE);
        this.player = player;
        this.isOpBook = isOpBook;
        
        if (page != null)
            graphic = page.createGraphic(this);
        
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
        isMainPageOpen = true;
        isBagPage = false;
        isReputationPage = false;
        isMenuPageOpen = true;
        
        selectedGroup = null;
        selectedReputation = null;
    }
    
    public static Group getSelectedGroup() {
        return selectedGroup;
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
    
    public TextBoxGroup.TextBox getTextBoxGroupAmount() {
        return textBoxGroupAmount;
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
        
        
        for (LargeButton button : buttons) {
            button.draw(matrices, this, x, y);
        }
        
        applyColor(0xFFFFFFFF);
        ResourceHelper.bindResource(MAP_TEXTURE);
        
        
        if (shouldDisplayControlArrow(false)) {
            drawRect(matrices, BACK_ARROW_X, BACK_ARROW_Y, BACK_ARROW_SRC_X + (inArrowBounds(false, x, y) ? BACK_ARROW_WIDTH : 0), BACK_ARROW_SRC_Y, BACK_ARROW_WIDTH, BACK_ARROW_HEIGHT);
        }
        if (shouldDisplayControlArrow(true)) {
            drawRect(matrices, MENU_ARROW_X, MENU_ARROW_Y, MENU_ARROW_SRC_X + (inArrowBounds(true, x, y) ? MENU_ARROW_WIDTH : 0), MENU_ARROW_SRC_Y, MENU_ARROW_WIDTH, MENU_ARROW_HEIGHT);
        }
        
        if (editMenu == null) {
            drawEditButtons(matrices, x, y, getButtons());
            
            for (ScrollBar scrollBar : scrollBars) {
                scrollBar.draw(matrices, this);
            }
            
            if (isMainPageOpen) {
                drawMainPage(matrices);
            } else if (isMenuPageOpen) {
                drawMenuPage(matrices, x, y);
            } else if (isBagPage) {
                drawBagPage(matrices, x, y);
            } else if (isReputationPage) {
                Reputation.drawEditPage(matrices, this, x, y);
            } else if (graphic != null) {
                graphic.drawFull(matrices, this, x, y);
            }
    
            drawEditButtonTooltip(matrices, x, y, getButtons());
    
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
    
        buttons.forEach(button -> button.renderTooltip(matrices, this, x, y));
        
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
        } else if (isBagPage && selectedGroup != null) {
            textBoxes.onKeyStroke(this, c, -1);
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
        } else if (isBagPage && selectedGroup != null) {
			textBoxes.onKeyStroke(this, Character.MIN_VALUE, keyCode);
		} else if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            goBack();
            return true;
        } else if (graphic != null) {
            return graphic.keyPressed(keyCode);
        } else  {
            return KeyboardHandler.handleEditModeHotkey(keyCode, getButtons());
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
            isMenuPageOpen = true;
            if (editMenu != null) {
                editMenu.save(this);
                editMenu.close(this);
                editMenu = null;
            }
            isBagPage = false;
            isReputationPage = false;
            return true;
        }
        
        boolean buttonClicked = false;
        
        for (LargeButton largeButton : buttons) {
            if (largeButton.isVisible(this) && largeButton.isEnabled(this) && largeButton.inButtonBounds(this, x, y)) {
                largeButton.onClick(this);
                buttonClicked = true;
                break;
            }
        }
        
        if (Quest.canQuestsBeEdited()) {
            SaveHelper.onClick(this, x, y);
        }
        
        if (buttonClicked) return true;
        
        if (editMenu == null) {
            handleEditButtonClick(x, y, getButtons());
            
            for (ScrollBar scrollBar : scrollBars) {
                scrollBar.onClick(this, x, y);
            }
            
            if (isMainPageOpen) {
                mainPageMouseClicked(x, y);
            } else if (isMenuPageOpen) {
                menuPageMouseClicked(button, x, y);
            } else if (isBagPage) {
                bagPageMouseClicked(button, x, y);
            } else if (isReputationPage) {
                if (button == 1) {
                    isMenuPageOpen = true;
                    isReputationPage = false;
                } else {
                    Reputation.onClick(this, x, y, player.getUUID());
                }
            } else if (graphic != null) {
                if (button == 1) {
                    goBack();
                    return true;
                } else {
                    graphic.onClick(this, x, y, button);
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
        } else if (graphic != null) {
            graphic.onRelease(this, x, y, button);
        } else {
            for (ScrollBar scrollBar : scrollBars) {
                scrollBar.onRelease(this, x, y);
            }
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
        } else if (graphic != null) {
            graphic.onDrag(this, x, y, button);
        } else {
            for (ScrollBar scrollBar : scrollBars) {
                scrollBar.onDrag(this, x, y);
            }
        }
        return true;
    }
    
    @Override
    public boolean mouseScrolled(double x, double y, double scroll) {
        if (editMenu != null) {
            editMenu.onScroll(this, x, y, scroll);
        } else if (graphic != null) {
            graphic.onScroll(this, x, y, scroll);
        } else {
            for (ScrollBar scrollBar : scrollBars) {
                scrollBar.onScroll(this, x, y, scroll);
            }
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
    
    private void drawBagPage(PoseStack matrices, int x, int y) {
        if (selectedGroup != null) {
            selectedGroup.draw(matrices, this, x, y);
            textBoxes.draw(matrices, this);
            
        } else {
            Group.drawOverview(matrices, this, tierScroll, groupScroll, x, y);
        }
    }
    
    private void drawMenuPage(PoseStack matrices, int x, int y) {
        QuestingDataManager manager = QuestingDataManager.getInstance();
        drawString(matrices, Translator.translatable("hqm.questBook.lives"), INFO_RIGHT_X, INFO_LIVES_Y, 0x404040);
        if (HQMConfig.getInstance().ENABLE_TEAMS)
            drawString(matrices, Translator.translatable("hqm.questBook.party"), INFO_RIGHT_X, INFO_TEAM_Y, 0x404040);
        drawString(matrices, Translator.translatable("hqm.questBook.quests"), INFO_LEFT_X, INFO_QUESTS_Y, 0x404040);
        drawString(matrices, Translator.translatable("hqm.questBook.reputation"), INFO_LEFT_X, INFO_REPUTATION_Y, 0x404040);
    
        QuestSetsGraphic.drawQuestInfo(matrices, this, null, INFO_LEFT_X, INFO_QUESTS_Y + (int) (TEXT_HEIGHT * 1.5F));
        drawString(matrices, Translator.translatable("hqm.questBook.showQuests"), INFO_LEFT_X, INFO_QUESTS_Y + QUEST_CLICK_TEXT_Y, 0.7F, 0x707070);
        
        if (manager.isHardcoreActive()) {
            boolean almostOut = manager.getQuestingData(player).getLives() == manager.getQuestingData(player).getLivesToStayAlive();
            if (almostOut) {
                drawString(matrices, Translator.translatable("hqm.questBook.deadOut", GuiColor.RED), INFO_RIGHT_X + 50, INFO_LIVES_Y + 2, 0.7F, 0x404040);
            }
            
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            int lives = manager.getQuestingData(player).getLives();
            int count, spacing, heartX;
            if (lives < 8) {
                heartX = INFO_RIGHT_X + INFO_HEARTS_X;
                count = lives;
                spacing = INFO_HEARTS_SPACING;
            } else {
                heartX = INFO_RIGHT_X + INFO_HEARTS_X + 20;
                count = 3;
                spacing = 3;
                drawString(matrices, Translator.plain(lives + " x"), INFO_RIGHT_X + 5, INFO_LIVES_Y + INFO_HEARTS_Y + 5, 0.7F, 0x404040);
            }
            
            for (int i = 0; i < count; i++) {
                drawItemStack(new ItemStack(ModItems.heart.get(), 1), heartX + spacing * i, INFO_LIVES_Y + INFO_HEARTS_Y, almostOut);
            }
        } else {
            drawString(matrices, getLinesFromText(Translator.translatable("hqm.questBook.infiniteLives"), 0.5F, PAGE_WIDTH - 30), INFO_RIGHT_X, INFO_LIVES_Y + 12, 0.5F, 0x707070);
        }
        
        
        int deaths = DeathStatsManager.getInstance().getDeathStat(this.getPlayer().getUUID()).getTotalDeaths();
        drawString(matrices, Translator.pluralTranslated(deaths != 1, "hqm.questBook.deaths", deaths), INFO_RIGHT_X, INFO_DEATHS_Y + DEATH_TEXT_Y, 0.7F, 0x404040);
        drawString(matrices, Translator.translatable("hqm.questBook.moreInfo"), INFO_RIGHT_X, INFO_DEATHS_Y + DEATH_CLICK_TEXT_Y, 0.7F, 0x707070);
    
        if (!HQMConfig.getInstance().ENABLE_TEAMS) return;
        
        FormattedText str;
        Team team = manager.getQuestingData(player).getTeam();
        if (team.isSingle()) {
            int invites = team.getInvites() == null ? 0 : team.getInvites().size();
            if (invites > 0) {
                str = Translator.pluralTranslated(invites != 1, "hqm.questBook.invites", invites);
            } else {
                str = Translator.translatable("hqm.questBook.notInParty");
            }
        } else {
            int players = 0;
            for (PlayerEntry player : team.getPlayers()) {
                if (player.isInTeam()) {
                    players++;
                }
            }
            str = Translator.pluralTranslated(players != 1, "hqm.questBook.inParty", players);
        }
        
        drawString(matrices, str, INFO_RIGHT_X, INFO_TEAM_Y + TEAM_TEXT_Y, 0.7F, 0x404040);
        drawString(matrices, Translator.translatable("hqm.questBook.openParty"), INFO_RIGHT_X, INFO_TEAM_Y + TEAM_CLICK_TEXT_Y, 0.7F, 0x707070);
        
        if (isOpBook) {
            drawString(matrices, Translator.translatable("hqm.questBook.resetParty"), 22, 182, 0.6F, 0x404040);
            drawString(matrices, getLinesFromText(Translator.translatable("hqm.questBook.shiftCtrlConfirm"), 0.6F, 70), 22, 192, 0.6F, GuiColor.RED.getHexColor());
        }
        
        
        Reputation.drawAll(matrices, this, INFO_LEFT_X + INFO_REPUTATION_OFFSET_X, INFO_REPUTATION_Y + INFO_REPUTATION_OFFSET_Y, x, y, player.getUUID());
    }
    
    private void drawMainPage(PoseStack matrices) {
        QuestLine questLine = QuestLine.getActiveQuestLine();
        int startLine = mainDescriptionScroll.isVisible(this) ? Math.round((Quest.getMainDescription(this).size() - VISIBLE_MAIN_DESCRIPTION_LINES) * mainDescriptionScroll.getScroll()) : 0;
        drawString(matrices, Quest.getMainDescription(this), startLine, VISIBLE_MAIN_DESCRIPTION_LINES, DESCRIPTION_X, DESCRIPTION_Y, 0.7F, 0x404040);
        drawCenteredString(matrices, Translator.translatable("hqm.questBook.start"), 0, 195, 0.7F, PAGE_WIDTH, TEXTURE_HEIGHT - 195, 0x707070);
        if (SoundHandler.hasLoreMusic() && !SoundHandler.isLorePlaying()) {
            drawCenteredString(matrices, Translator.translatable("hqm.questBook.playAgain"), PAGE_WIDTH, 195, 0.7F, PAGE_WIDTH - 10, TEXTURE_HEIGHT - 195, 0x707070);
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
            applyColor(0xFFFFFFFF);
            drawRect(matrices, 20, 20, 0, 0, 140, 180);
        }
    }
    
    public void goBack() {
        if (isMenuPageOpen) {
            isMainPageOpen = true;
        } else if (isBagPage) {
            isBagPage = false;
            isMenuPageOpen = true;
        } else if (isReputationPage) {
            isMenuPageOpen = true;
            isReputationPage = false;
        } else if (page != null) {
            setPage(page.getParent());
        }
    }
    
    private void bagPageMouseClicked(int button, int x, int y) {
        if (selectedGroup != null) {
            if (button == 1) {
                selectedGroup = null;
            } else {
                selectedGroup.mouseClicked(this, x, y);
                textBoxes.onClick(this, x, y);
            }
        } else {
            if (button == 1) {
                isBagPage = false;
                isMenuPageOpen = true;
            } else {
                Group.mouseClickedOverview(this, groupScroll, x, y);
                GroupTier.mouseClickedOverview(this, tierScroll, x, y);
            }
        }
        
    }
    
    private void menuPageMouseClicked(int button, int x, int y) {
        if (button == 1) {
            isMainPageOpen = true;
        } else {
            if (HQMConfig.getInstance().ENABLE_TEAMS && inBounds(INFO_RIGHT_X, INFO_TEAM_Y + TEAM_CLICK_TEXT_Y, PAGE_WIDTH, (int) (TEXT_HEIGHT * 0.7F), x, y)) {
                editMenu = new GuiEditMenuTeam(this, player.getUUID());
            } else if (inBounds(INFO_RIGHT_X, INFO_DEATHS_Y + DEATH_CLICK_TEXT_Y, PAGE_WIDTH, (int) (TEXT_HEIGHT * 0.7F), x, y)) {
                editMenu = new GuiEditMenuDeath(this, player.getUUID());
            } else if (inBounds(INFO_LEFT_X, INFO_QUESTS_Y + QUEST_CLICK_TEXT_Y, PAGE_WIDTH, (int) (TEXT_HEIGHT * 0.7F), x, y)) {
                setPage(new BookPage.SetsPage());
            }
        }
    }
    
    private void mainPageMouseClicked(int x, int y) {
        if (x > 0 && x < PAGE_WIDTH && y > 205) {
            isMainPageOpen = false;
            SoundHandler.stopLoreMusic();
        } else if (x > PAGE_WIDTH && x < TEXTURE_WIDTH && y > 205) {
            if (SoundHandler.hasLoreMusic() && !SoundHandler.isLorePlaying()) {
                SoundHandler.playLoreMusic();
            }
        } else {
            if (Quest.canQuestsBeEdited() && currentMode == EditMode.RENAME && inBounds(DESCRIPTION_X, DESCRIPTION_Y, 130, (int) (VISIBLE_MAIN_DESCRIPTION_LINES * TEXT_HEIGHT * 0.7F), x, y)) {
                TextMenu.display(this, player.getUUID(), Quest.getRawMainDescription(), false, QuestLine.getActiveQuestLine()::setMainDescription);
            }
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
        
        if (currentMode == EditMode.BAG) {
            currentMode = EditMode.NORMAL;
            isBagPage = true;
            isMenuPageOpen = false;
        } else if (currentMode == EditMode.REPUTATION) {
            currentMode = EditMode.NORMAL;
            isReputationPage = true;
            isMenuPageOpen = false;
        }
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public void save() {
        QuestLine.getActiveQuestLine().saveAll();
        SaveHelper.onSave();
    }
    
    private EditButton[] getButtons() {
        return isMainPageOpen ? mainButtons : isMenuPageOpen ? menuButtons : isReputationPage ? reputationButtons : isBagPage ? selectedGroup != null ? groupButtons : bagButtons : new EditButton[0];
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
        return !isMainPageOpen && ((!(isMenuArrow && isMenuPageOpen) && editMenu == null) || (editMenu != null && !editMenu.hasButtons()));
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
        isMenuPageOpen = page == null;
        GuiQuestBook.page = page;
        graphic = page == null ? null : page.createGraphic(this);
    }
}
