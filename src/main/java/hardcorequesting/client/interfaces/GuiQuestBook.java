package hardcorequesting.client.interfaces;

import codechicken.nei.NEIClientConfig;
import codechicken.nei.NEIClientUtils;
import codechicken.nei.recipe.GuiCraftingRecipe;
import codechicken.nei.recipe.GuiUsageRecipe;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import hardcorequesting.*;
import hardcorequesting.bag.Group;
import hardcorequesting.bag.GroupTier;
import hardcorequesting.client.EditButton;
import hardcorequesting.client.EditMode;
import hardcorequesting.client.KeyboardHandler;
import hardcorequesting.client.sounds.SoundHandler;
import hardcorequesting.items.ModItems;
import hardcorequesting.network.DataBitHelper;
import hardcorequesting.network.FileHelper;
import hardcorequesting.network.PacketHandler;
import hardcorequesting.quests.*;
import hardcorequesting.reputation.Reputation;
import hardcorequesting.reputation.ReputationBar;
import hardcorequesting.reputation.ReputationMarker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiQuestBook extends GuiBase {

    public static void resetBookPosition() {
        selectedSet = null;
        isSetOpened = false;
        selectedQuest = null;
        isMainPageOpen = true;
        isBagPage = false;
        isReputationPage = false;
        isMenuPageOpen = true;

        selectedGroup = null;
        selectedReputation = null;
    }

    //these are static to keep the same page loaded when the book is reopened
    public static QuestSet selectedSet;
    private static boolean isSetOpened;
    public static Quest selectedQuest;
    private static boolean isMainPageOpen = true;
    private static boolean isMenuPageOpen = true;
    private static boolean isBagPage;
    private static boolean isReputationPage;
    public static Group selectedGroup;
    public static Reputation selectedReputation;
    private static boolean isNEIActive = Loader.isModLoaded("NotEnoughItems");
    private static ItemStack selected;

    private final EntityPlayer player;
    public final boolean isOpBook;

    private ScrollBar setScroll;
    private ScrollBar descriptionScroll;
    private ScrollBar mainDescriptionScroll;
    private ScrollBar groupScroll;
    private ScrollBar tierScroll;
    public ScrollBar reputationDisplayScroll;
    public ScrollBar reputationScroll;
    public ScrollBar reputationTierScroll;
    private List<ScrollBar> scrollBars;

    {
        scrollBars = new ArrayList<ScrollBar>();
        scrollBars.add(descriptionScroll = new ScrollBar(312, 18, 64, 249, 102, DESCRIPTION_X) {
            @Override
            public boolean isVisible(GuiBase gui) {
                return !isReputationPage && !isBagPage && !isMainPageOpen && selectedSet != null && !isSetOpened && selectedSet.getDescription(gui).size() > VISIBLE_DESCRIPTION_LINES;
            }
        });

        scrollBars.add(setScroll = new ScrollBar(160, 18, 186, 171, 69, LIST_X) {
            @Override
            public boolean isVisible(GuiBase gui) {
                return !isReputationPage && !isBagPage && !isMainPageOpen && (selectedSet == null || !isSetOpened) && Quest.getQuestSets().size() > VISIBLE_SETS;
            }
        });

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
                return !isReputationPage && isBagPage && selectedGroup == null && GroupTier.getTiers().size() > VISIBLE_TIERS;
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
                return isReputationPage && !isBagPage && (getCurrentMode() != EditMode.CREATE || selectedReputation == null) && Reputation.getReputationList().size() > VISIBLE_REPUTATIONS;
            }
        });

        scrollBars.add(reputationDisplayScroll = new ScrollBar(160, 125, 87, 164, 69, INFO_LEFT_X) {
            @Override
            public boolean isVisible(GuiBase gui) {
                return isMenuPageOpen && !isMainPageOpen && Reputation.getReputationList().size() > VISIBLE_DISPLAY_REPUTATIONS;
            }
        });
    }


    private TextBoxGroup.TextBox textBoxGroupAmount;

    public TextBoxGroup.TextBox getTextBoxGroupAmount() {
        return textBoxGroupAmount;
    }

    private TextBoxGroup textBoxes;

    {
        textBoxes = new TextBoxGroup();
        textBoxes.add(textBoxGroupAmount = new TextBoxGroup.TextBox(this, "0", 180, 30, false) {
            @Override
            protected boolean isCharacterValid(char c) {
                return getText().length() < 3 && Character.isDigit(c);
            }

            @Override
            protected void textChanged(GuiBase gui) {
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

    private int tick;

    public int getTick() {
        return tick;
    }

    private GuiEditMenu editMenu;

    public static Group getSelectedGroup() {
        return selectedGroup;
    }

    public void setEditMenu(GuiEditMenu editMenu) {
        this.editMenu = editMenu;
    }

    private GuiQuestBook(EntityPlayer player, boolean isOpBook) {
        this.player = player;
        this.isOpBook = isOpBook;

        if (Quest.isEditing) {
            Keyboard.enableRepeatEvents(true);
        }
        QuestingData data = QuestingData.getQuestingData(player);
        if (!data.playedLore && SoundHandler.hasLoreMusic()) {
            SoundHandler.triggerFirstLore();
            data.playedLore = true;
        }
    }

    private FileHelper.SaveResult saveResult = null;

    private LargeButton saveButton;
    private List<LargeButton> buttons = new ArrayList<LargeButton>();

    {
        buttons.add(saveButton = new LargeButton("hqm.questBook.saveAll", 360, 10) {
            @Override
            public boolean isEnabled(GuiBase gui, EntityPlayer player) {
                return true;
            }

            @Override
            public boolean isVisible(GuiBase gui, EntityPlayer player) {
                return Quest.isEditing && SaveHelper.isLarge();
            }

            @Override
            public void onClick(GuiBase gui, EntityPlayer player) {
                save();
            }
        });

        buttons.add(new LargeButton("hqm.questBook.open", 245, 190) {
            @Override
            public boolean isEnabled(GuiBase gui, EntityPlayer player) {
                return true;
            }

            @Override
            public boolean isVisible(GuiBase gui, EntityPlayer player) {
                return editMenu == null && selectedSet != null && !isBagPage && !isSetOpened && !isMainPageOpen && !isMenuPageOpen && !isReputationPage;
            }

            @Override
            public void onClick(GuiBase gui, EntityPlayer player) {
                isSetOpened = true;
            }
        });

        buttons.add(new LargeButton("hqm.questBook.createSet", 185, 50) {
            @Override
            public boolean isEnabled(GuiBase gui, EntityPlayer player) {
                return Quest.getQuestSets().size() < DataBitHelper.QUEST_SETS.getMaximum();
            }

            @Override
            public boolean isVisible(GuiBase gui, EntityPlayer player) {
                return editMenu == null && Quest.isEditing && currentMode == EditMode.CREATE && !isBagPage && !isSetOpened && !isMainPageOpen && !isMenuPageOpen && !isReputationPage;
            }

            @Override
            public void onClick(GuiBase gui, EntityPlayer player) {
                int i = 0;
                for (QuestSet set : Quest.getQuestSets()) {
                    if (set.getName().startsWith("Unnamed set")) i++;
                }
                Quest.getQuestSets().add(new QuestSet("Unnamed set" + (i == 0 ? "" : i), "No description"));
                SaveHelper.add(SaveHelper.EditType.SET_CREATE);
            }
        });

        buttons.add(new LargeButton("hqm.questBook.createGroup", 100, 175) {
            @Override
            public boolean isEnabled(GuiBase gui, EntityPlayer player) {
                return GroupTier.getTiers().size() > 0 && Group.getGroups().size() < DataBitHelper.GROUP_COUNT.getMaximum();
            }

            @Override
            public boolean isVisible(GuiBase gui, EntityPlayer player) {
                return editMenu == null && isBagPage && currentMode == EditMode.CREATE && selectedGroup == null && !isMainPageOpen && !isMenuPageOpen && !isReputationPage;
            }

            @Override
            public void onClick(GuiBase gui, EntityPlayer player) {
                Group.add(new Group());
                SaveHelper.add(SaveHelper.EditType.GROUP_CREATE);
            }
        });

        buttons.add(new LargeButton("hqm.questBook.createTier", 100, 200) {
            @Override
            public boolean isEnabled(GuiBase gui, EntityPlayer player) {
                return GroupTier.getTiers().size() < DataBitHelper.TIER_COUNT.getMaximum();
            }

            @Override
            public boolean isVisible(GuiBase gui, EntityPlayer player) {
                return editMenu == null && isBagPage && currentMode == EditMode.CREATE && selectedGroup == null && !isMainPageOpen && !isMenuPageOpen && !isReputationPage;
            }

            @Override
            public void onClick(GuiBase gui, EntityPlayer player) {
                GroupTier.getTiers().add(new GroupTier("New Tier", GuiColor.BLACK, 0, 0, 0, 0, 0));
                SaveHelper.add(SaveHelper.EditType.TIER_CREATE);
            }
        });

        buttons.add(new LargeButton("Reset", 90, 190) {
            @Override
            public boolean isEnabled(GuiBase gui, EntityPlayer player) {
                return GuiScreen.isCtrlKeyDown() && GuiScreen.isShiftKeyDown();
            }

            @Override
            public boolean isVisible(GuiBase gui, EntityPlayer player) {
                return editMenu == null && !isBagPage && !isMainPageOpen && isOpBook && isMenuPageOpen && !isReputationPage;
            }

            @Override
            public void onClick(GuiBase gui, EntityPlayer player) {
                OPBookHelper.reset(player);
            }
        });

        buttons.add(new LargeButton("Create New", 180, 20) {
            @Override
            public boolean isEnabled(GuiBase gui, EntityPlayer player) {
                return Reputation.size() < DataBitHelper.REPUTATION.getMaximum();
            }

            @Override
            public boolean isVisible(GuiBase gui, EntityPlayer player) {
                return editMenu == null && !isBagPage && currentMode == EditMode.CREATE && selectedReputation == null && !isMainPageOpen && !isMenuPageOpen && isReputationPage;
            }

            @Override
            public void onClick(GuiBase gui, EntityPlayer player) {
                new Reputation("Unnamed", "Neutral");
                SaveHelper.add(SaveHelper.EditType.REPUTATION_ADD);
            }
        });

        buttons.add(new LargeButton("hqm.questBook.createTier", 20, 20) {
            @Override
            public boolean isEnabled(GuiBase gui, EntityPlayer player) {
                return selectedReputation.getMarkerCount() < DataBitHelper.REPUTATION_MARKER.getMaximum();
            }

            @Override
            public boolean isVisible(GuiBase gui, EntityPlayer player) {
                return editMenu == null && !isBagPage && currentMode == EditMode.CREATE && selectedReputation != null && !isMainPageOpen && !isMenuPageOpen && isReputationPage;
            }

            @Override
            public void onClick(GuiBase gui, EntityPlayer player) {
                selectedReputation.add(new ReputationMarker("Unnamed", 0, false));
                SaveHelper.add(SaveHelper.EditType.REPUTATION_MARKER_CREATE);
            }
        });
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(true);
        SoundHandler.stopLoreMusic();
        PacketHandler.closeInterface();
    }

    public static void displayGui(EntityPlayer player, boolean isOpBook) {
        if (player != null) {
            if (Minecraft.getMinecraft().currentScreen == null || !(Minecraft.getMinecraft().currentScreen instanceof GuiQuestBook)) {
                Minecraft.getMinecraft().displayGuiScreen(new GuiQuestBook(player, isOpBook));
            }
        }
    }

    private static final String FRONT_KEY = "hqm_front_texture";
    private static final int TEXTURE_WIDTH = 170 * 2;
    public static final int PAGE_WIDTH = 170;
    private static final int TEXTURE_HEIGHT = 234;

    @Override
    public void updateScreen() {
        ++tick;

        super.updateScreen();
    }

    //region pixel info for all the things
    public static final int VISIBLE_REPUTATION_TIERS = 9;
    public static final int VISIBLE_REPUTATIONS = 10;
    public static final int VISIBLE_DISPLAY_REPUTATIONS = 4;

    public static final int LIST_X = 25;
    public static final int LIST_Y = 20;
    public static final int TEXT_HEIGHT = 9;
    public static final int TEXT_SPACING = 20;
    public static final int DESCRIPTION_X = 180;
    public static final int DESCRIPTION_Y = 20;
    public static final int VISIBLE_DESCRIPTION_LINES = 7;
    public static final int VISIBLE_MAIN_DESCRIPTION_LINES = 21;
    public static final int VISIBLE_SETS = 7;

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

    @Override
    public void drawScreen(int x0, int y0, float f) {
        selected = null;
        left = (width - TEXTURE_WIDTH) / 2;
        top = (height - TEXTURE_HEIGHT) / 2;

        int x = x0 - left;
        int y = y0 - top;

        applyColor(0xFFFFFFFF);
        ResourceHelper.bindResource(BG_TEXTURE);

        drawRect(0, 0, 0, 0, PAGE_WIDTH, TEXTURE_HEIGHT);
        drawRect(PAGE_WIDTH, 0, 0, 0, PAGE_WIDTH, TEXTURE_HEIGHT, RenderRotation.FLIP_HORIZONTAL);

        if (Quest.isEditing) {
            applyColor(0xFFFFFFFF);
            ResourceHelper.bindResource(MAP_TEXTURE);
            SaveHelper.render(this, x, y);
        }


        for (LargeButton button : buttons) {
            button.draw(this, player, x, y);
        }

        applyColor(0xFFFFFFFF);
        ResourceHelper.bindResource(MAP_TEXTURE);


        if (shouldDisplayControlArrow(false)) {
            drawRect(BACK_ARROW_X, BACK_ARROW_Y, BACK_ARROW_SRC_X + (inArrowBounds(false, x, y) ? BACK_ARROW_WIDTH : 0), BACK_ARROW_SRC_Y, BACK_ARROW_WIDTH, BACK_ARROW_HEIGHT);
        }
        if (shouldDisplayControlArrow(true)) {
            drawRect(MENU_ARROW_X, MENU_ARROW_Y, MENU_ARROW_SRC_X + (inArrowBounds(true, x, y) ? MENU_ARROW_WIDTH : 0), MENU_ARROW_SRC_Y, MENU_ARROW_WIDTH, MENU_ARROW_HEIGHT);
        }

        if (editMenu == null) {
            if (Quest.isEditing) {
                for (EditButton button : getButtons()) {
                    button.draw(x, y);
                }
            }
            for (ScrollBar scrollBar : scrollBars) {
                scrollBar.draw(this);
            }

            if (isMainPageOpen) {
                drawMainPage();
            } else if (isMenuPageOpen) {
                drawMenuPage(x, y);
            } else if (isBagPage) {
                drawBagPage(x, y);
            } else if (isReputationPage) {
                Reputation.drawEditPage(this, x, y);
            } else if (selectedSet == null || !isSetOpened) {
                QuestSet.drawOverview(this, setScroll, descriptionScroll, x, y);
            } else if (selectedQuest == null) {
                selectedSet.draw(this, x0, y0, x, y);
            } else {
                selectedQuest.drawMenu(this, player, x, y);
            }

            if (Quest.isEditing) {
                for (EditButton button : getButtons()) {
                    button.drawInfo(x, y);
                }
            }

            if (currentMode == EditMode.DELETE) {
                GL11.glPushMatrix();
                GL11.glTranslatef(0, 0, 200);
                drawCenteredString(Translator.translate("hqm.questBook.warning"), 0, 0, 2F, TEXTURE_WIDTH, TEXTURE_HEIGHT, 0xFF0000);
                drawCenteredString(Translator.translate("hqm.questBook.deleteOnClick"), 0, fontRendererObj.FONT_HEIGHT * 2, 1F, TEXTURE_WIDTH, TEXTURE_HEIGHT, 0xFF0000);
                applyColor(0xFFFFFFFF);
                ResourceHelper.bindResource(MAP_TEXTURE);
                GL11.glPopMatrix();
            }

        } else {
            editMenu.draw(this, x, y);
            editMenu.drawMouseOver(this, x, y);
        }

        for (LargeButton button : buttons) {
            button.drawMouseOver(this, player, x, y);
        }

        if (Quest.isEditing) {
            if (saveResult != null) {
                if (saveButton.inButtonBounds(this, x, y) || SaveHelper.inSaveBounds(this, x, y)) {
                    String str = (saveResult == FileHelper.SaveResult.SUCCESS ? GuiColor.GREEN : GuiColor.RED).toString();

                    str += saveResult.getName() + "\n\n";
                    str += GuiColor.WHITE;
                    str += saveResult.getText();

                    drawMouseOver(getLinesFromText(str, 1F, 130), x + left, y + top);
                } else {
                    saveResult = null;
                }
            }
        }

        if (shouldDisplayAndIsInArrowBounds(false, x, y)) {
            drawMouseOver(Translator.translate("hqm.questBook.goBack") + "\n" + GuiColor.GRAY + Translator.translate("hqm.questBook.rightClick"), x + left, y + top);
        } else if (shouldDisplayAndIsInArrowBounds(true, x, y)) {
            drawMouseOver(Translator.translate("hqm.questBook.backToMenu"), x + left, y + top);
        }
    }

    private void drawBagPage(int x, int y) {
        if (selectedGroup != null) {
            selectedGroup.draw(this, x, y);
            textBoxes.draw(this);
        } else {
            Group.drawOverview(this, tierScroll, groupScroll, x, y);
        }
    }

    private void drawMenuPage(int x, int y) {
        drawString(Translator.translate("hqm.questBook.lives"), INFO_RIGHT_X, INFO_LIVES_Y, 0x404040);
        drawString(Translator.translate("hqm.questBook.party"), INFO_RIGHT_X, INFO_TEAM_Y, 0x404040);
        drawString(Translator.translate("hqm.questBook.quests"), INFO_LEFT_X, INFO_QUESTS_Y, 0x404040);
        drawString(Translator.translate("hqm.questBook.reputation"), INFO_LEFT_X, INFO_REPUTATION_Y, 0x404040);

        QuestSet.drawQuestInfo(this, null, INFO_LEFT_X, INFO_QUESTS_Y + (int) (TEXT_HEIGHT * 1.5F));
        drawString(Translator.translate("hqm.questBook.showQuests"), INFO_LEFT_X, INFO_QUESTS_Y + QUEST_CLICK_TEXT_Y, 0.7F, 0x707070);

        if (QuestingData.isHardcoreActive()) {
            boolean almostOut = QuestingData.getQuestingData(player).getLives() == QuestingData.getQuestingData(player).getLivesToStayAlive();
            if (almostOut) {
                drawString(GuiColor.RED + Translator.translate("hqm.questBook.deadOut"), INFO_RIGHT_X + 50, INFO_LIVES_Y + 2, 0.7F, 0x404040);
            }

            GL11.glColor4f(1, 1, 1, 1);
            int lives = QuestingData.getQuestingData(player).getLives();
            int count, spacing, heartX;
            if (lives < 8) {
                heartX = INFO_RIGHT_X + INFO_HEARTS_X;
                count = lives;
                spacing = INFO_HEARTS_SPACING;
            } else {
                heartX = INFO_RIGHT_X + INFO_HEARTS_X + 20;
                count = 3;
                spacing = 3;
                drawString(lives + " x", INFO_RIGHT_X + 5, INFO_LIVES_Y + INFO_HEARTS_Y + 5, 0.7F, 0x404040);
            }

            for (int i = 0; i < count; i++) {
                drawItem(new ItemStack(ModItems.hearts, 1, 3), heartX + spacing * i, INFO_LIVES_Y + INFO_HEARTS_Y, almostOut);
            }
        } else {
            drawString(getLinesFromText(Translator.translate("hqm.questBook.infiniteLives"), 0.5F, PAGE_WIDTH - 30), INFO_RIGHT_X, INFO_LIVES_Y + 12, 0.5F, 0x707070);
        }


        int deaths = DeathStats.getDeathStats(QuestingData.getUserName(player)).getTotalDeaths();
        drawString(Translator.translate(deaths != 1, "hqm.questBook.deaths", deaths), INFO_RIGHT_X, INFO_DEATHS_Y + DEATH_TEXT_Y, 0.7F, 0x404040);
        drawString(Translator.translate("hqm.questBook.moreInfo"), INFO_RIGHT_X, INFO_DEATHS_Y + DEATH_CLICK_TEXT_Y, 0.7F, 0x707070);


        String str;
        Team team = QuestingData.getQuestingData(player).getTeam();
        if (team.isSingle()) {
            int invites = team.getInvites() == null ? 0 : team.getInvites().size();
            if (invites > 0) {
                str = Translator.translate(invites != 1, "hqm.questBook.invites", invites);
            } else {
                str = Translator.translate("hqm.questBook.notInParty");
            }
        } else {
            int players = 0;
            for (Team.PlayerEntry player : team.getPlayers()) {
                if (player.isInTeam()) {
                    players++;
                }
            }
            str = Translator.translate(players != 1, "hqm.questBook.inParty", players);
        }

        drawString(str, INFO_RIGHT_X, INFO_TEAM_Y + TEAM_TEXT_Y, 0.7F, 0x404040);
        drawString(Translator.translate("hqm.questBook.openParty"), INFO_RIGHT_X, INFO_TEAM_Y + TEAM_CLICK_TEXT_Y, 0.7F, 0x707070);

        if (isOpBook) {
            drawString(Translator.translate("hqm.questBook.resetParty"), 22, 182, 0.6F, 0x404040);
            drawString(getLinesFromText(Translator.translate("hqm.questBook.shiftCtrlConfirm"), 0.6F, 70), 22, 192, 0.6F, GuiColor.RED.getHexColor());
        }


        Reputation.drawAll(this, INFO_LEFT_X + INFO_REPUTATION_OFFSET_X, INFO_REPUTATION_Y + INFO_REPUTATION_OFFSET_Y, x, y, player);
    }

    private void drawMainPage() {
        int startLine = mainDescriptionScroll.isVisible(this) ? Math.round((Quest.getMainDescription(this).size() - VISIBLE_MAIN_DESCRIPTION_LINES) * mainDescriptionScroll.getScroll()) : 0;
        drawString(Quest.getMainDescription(this), startLine, VISIBLE_MAIN_DESCRIPTION_LINES, DESCRIPTION_X, DESCRIPTION_Y, 0.7F, 0x404040);
        drawCenteredString(Translator.translate("hqm.questBook.start"), 0, 195, 0.7F, PAGE_WIDTH, TEXTURE_HEIGHT - 195, 0x707070);
        if (SoundHandler.hasLoreMusic() && !SoundHandler.isLorePlaying()) {
            drawCenteredString(Translator.translate("hqm.questBook.playAgain"), PAGE_WIDTH, 195, 0.7F, PAGE_WIDTH - 10, TEXTURE_HEIGHT - 195, 0x707070);
        }
        if (QuestLine.getActiveQuestLine().front == null && QuestLine.getActiveQuestLine().mainPath != null) {
            File file = new File(QuestLine.getActiveQuestLine().mainPath + "front.png");
            if (file.exists()) {
                try {
                    BufferedImage img = ImageIO.read(file);
                    DynamicTexture dm = new DynamicTexture(img);
                    QuestLine.getActiveQuestLine().front = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(FRONT_KEY, dm);
                } catch (IOException ignored) {
                    QuestLine.getActiveQuestLine().front = ResourceHelper.getResource("front");
                }
            } else {
                QuestLine.getActiveQuestLine().front = ResourceHelper.getResource("front");
            }
        }

        if (QuestLine.getActiveQuestLine().front != null) {
            ResourceHelper.bindResource(QuestLine.getActiveQuestLine().front);
            applyColor(0xFFFFFFFF);
            drawRect(20, 20, 0, 0, 140, 180);
        }
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();

        int x = (Mouse.getEventX() * this.width / this.mc.displayWidth) - left;
        int y = (this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1) - top;

        int scroll = Mouse.getEventDWheel();
        if (scroll != 0) {
            if (editMenu != null) {
                editMenu.onScroll(this, x, y, scroll);
            } else if (selectedQuest != null) {
                selectedQuest.onScroll(this, x, y, scroll);
            } else {
                for (ScrollBar scrollBar : scrollBars) {
                    scrollBar.onScroll(this, x, y, scroll);
                }
            }
        }
    }

    @Override
    protected void keyTyped(char c, int k) {
        super.keyTyped(c, k);
        if (editMenu != null) {
            editMenu.onKeyTyped(this, c, k);
        } else if (isBagPage && selectedGroup != null) {
            textBoxes.onKeyStroke(this, c, k);
        } else if (KeyboardHandler.pressedHotkey(this, k, getButtons())) {
            onButtonClicked();
        } else if (isNEIActive()) {
            handleNEI(k);
        }
    }

    private boolean isNEIActive() {
        return isNEIActive;
    }

    public static void setSelected(ItemStack stack) {
        selected = stack;
    }

    private void handleNEI(int k) {
        ItemStack stackover = selected;
        if (stackover != null) {
            if (k == NEIClientConfig.getKeyBinding("gui.usage") || (k == NEIClientConfig.getKeyBinding("gui.recipe") && NEIClientUtils.shiftKey())) {
                GuiUsageRecipe.openRecipeGui("item", stackover.copy());
            }

            if (k == NEIClientConfig.getKeyBinding("gui.recipe")) {
                GuiCraftingRecipe.openRecipeGui("item", stackover.copy());
            }
        }
    }

    @Override
    protected void mouseClicked(int x0, int y0, int button) {
        super.mouseClicked(x0, y0, button);

        int x = x0 - left;
        int y = y0 - top;

        if (shouldDisplayAndIsInArrowBounds(false, x, y)) {
            button = 1;
            if (editMenu != null) {
                editMenu.save(this);
                editMenu.close(this);
                return;
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
            return;
        }

        for (LargeButton largeButton : buttons) {
            if (largeButton.isVisible(this, player) && largeButton.isEnabled(this, player) && largeButton.inButtonBounds(this, x, y)) {
                largeButton.onClick(this, player);
            }
        }

        if (Quest.isEditing) {
            SaveHelper.onClick(this, x, y);
        }

        if (editMenu == null) {
            if (Quest.isEditing) {
                for (EditButton editButton : getButtons()) {
                    if (editButton.onClick(x, y)) {
                        onButtonClicked();
                        break;
                    }
                }
            }
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
                    Reputation.onClick(this, x, y, player);
                }
            } else if (selectedSet == null || !isSetOpened) {
                if (button == 1) {
                    isMenuPageOpen = true;
                    return;
                }
                QuestSet.mouseClickedOverview(this, setScroll, x, y);
            } else {
                if (selectedQuest == null) {
                    if (button == 1) {
                        isSetOpened = false;
                    } else {
                        selectedSet.mouseClicked(this, x, y);
                    }
                } else {
                    selectedQuest.onClick(this, player, x, y, button);
                }
            }
        } else {
            editMenu.onClick(this, x, y, button);
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
        } else if (selectedSet == null || !isSetOpened) {
            isMenuPageOpen = true;
        } else if (selectedQuest == null) {
            isSetOpened = false;
        }

    }

    private void onButtonClicked() {
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
            if (inBounds(INFO_RIGHT_X, INFO_TEAM_Y + TEAM_CLICK_TEXT_Y, PAGE_WIDTH, (int) (TEXT_HEIGHT * 0.7F), x, y)) {
                editMenu = new GuiEditMenuTeam(this, player);
            } else if (inBounds(INFO_RIGHT_X, INFO_DEATHS_Y + DEATH_CLICK_TEXT_Y, PAGE_WIDTH, (int) (TEXT_HEIGHT * 0.7F), x, y)) {
                editMenu = new GuiEditMenuDeath(this, player);
            } else if (inBounds(INFO_LEFT_X, INFO_QUESTS_Y + QUEST_CLICK_TEXT_Y, PAGE_WIDTH, (int) (TEXT_HEIGHT * 0.7F), x, y)) {
                isMenuPageOpen = false;
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
            if (Quest.isEditing && currentMode == EditMode.RENAME && inBounds(DESCRIPTION_X, DESCRIPTION_Y, 130, (int) (VISIBLE_MAIN_DESCRIPTION_LINES * TEXT_HEIGHT * 0.7F), x, y)) {
                editMenu = new GuiEditMenuTextEditor(this, player);
            }
        }
    }

    @Override
    protected void mouseMovedOrUp(int x0, int y0, int button) {
        super.mouseMovedOrUp(x0, y0, button);

        int x = x0 - left;
        int y = y0 - top;

        updatePosition(x, y);
        if (currentMode == EditMode.MOVE) {
            modifyingQuest = null;
            modifyingBar = null;
        }
        if (editMenu != null) {
            editMenu.onRelease(this, x, y);
        } else if (selectedQuest != null) {
            selectedQuest.onRelease(this, player, x, y, button);
        } else {
            for (ScrollBar scrollBar : scrollBars) {
                scrollBar.onRelease(this, x, y);
            }
        }
    }

    @Override
    protected void mouseClickMove(int x0, int y0, int button, long ticks) {
        super.mouseClickMove(x0, y0, button, ticks);

        int x = x0 - left;
        int y = y0 - top;

        updatePosition(x, y);
        if (editMenu != null) {
            editMenu.onDrag(this, x, y);
        } else if (selectedQuest != null) {
            selectedQuest.onDrag(this, player, x, y, button);
        } else {
            for (ScrollBar scrollBar : scrollBars) {
                scrollBar.onDrag(this, x, y);
            }
        }
    }

    public Group modifyingGroup;
    public QuestSet modifyingQuestSet;
    public Quest modifyingQuest;
    public ReputationBar modifyingBar;

    private void updatePosition(int x, int y) {
        if (Quest.isEditing && currentMode == EditMode.MOVE) {
            if (modifyingQuest != null) {
                modifyingQuest.setGuiCenterX(x);
                modifyingQuest.setGuiCenterY(y);
            }
            if (modifyingBar != null) {
                modifyingBar.moveTo(x, y);
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public void loadMap() {
        selectedQuest = null;
    }


    private EditMode currentMode = EditMode.NORMAL;

    public EditMode getCurrentMode() {
        return currentMode;
    }

    public void setCurrentMode(EditMode mode) {
        currentMode = mode;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    public void save() {
        saveResult = Quest.FILE_HELPER.saveData(null);
        if (saveResult == FileHelper.SaveResult.SUCCESS) {
            SaveHelper.onSave();
        }
    }

    private EditButton[] getButtons() {
        return isMainPageOpen ? mainButtons : isMenuPageOpen ? menuButtons : isReputationPage ? reputationButtons : isBagPage ? selectedGroup != null ? groupButtons : bagButtons : selectedSet == null || !isSetOpened ? overviewButtons : selectedQuest == null ? setButtons : questButtons;
    }

    private EditButton[] groupButtons = EditButton.createButtons(this, EditMode.NORMAL, EditMode.ITEM, EditMode.DELETE);
    private EditButton[] bagButtons = EditButton.createButtons(this, EditMode.NORMAL, EditMode.CREATE, EditMode.RENAME, EditMode.TIER, EditMode.DELETE);
    private EditButton[] reputationButtons = EditButton.createButtons(this, EditMode.NORMAL, EditMode.CREATE, EditMode.RENAME, EditMode.REPUTATION_VALUE, EditMode.DELETE);
    private EditButton[] mainButtons = EditButton.createButtons(this, EditMode.NORMAL, EditMode.RENAME);
    private EditButton[] menuButtons = EditButton.createButtons(this, EditMode.NORMAL, EditMode.BAG, EditMode.REPUTATION);
    private EditButton[] overviewButtons = EditButton.createButtons(this, EditMode.NORMAL, EditMode.CREATE, EditMode.RENAME, EditMode.SWAP_SELECT, EditMode.DELETE);
    private EditButton[] setButtons = EditButton.createButtons(this, EditMode.NORMAL, EditMode.MOVE, EditMode.CREATE, EditMode.REQUIREMENT, EditMode.SIZE, EditMode.ITEM, EditMode.REPEATABLE, EditMode.TRIGGER, EditMode.REQUIRED_PARENTS, EditMode.QUEST_SELECTION, EditMode.QUEST_OPTION, EditMode.SWAP, EditMode.REP_BAR_CREATE, EditMode.REP_BAR_CHANGE, EditMode.DELETE);
    private EditButton[] questButtons = EditButton.createButtons(this, EditMode.NORMAL, EditMode.RENAME, EditMode.TASK, EditMode.CHANGE_TASK, EditMode.ITEM, EditMode.LOCATION, EditMode.MOB, EditMode.REPUTATION_TASK, EditMode.REPUTATION_REWARD, EditMode.COMMAND_CREATE, EditMode.COMMAND_CHANGE, EditMode.DELETE);

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
}
