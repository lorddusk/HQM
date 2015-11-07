package hardcorequesting.client.interfaces;

import codechicken.nei.NEIClientConfig;
import codechicken.nei.NEIClientUtils;
import codechicken.nei.recipe.GuiCraftingRecipe;
import codechicken.nei.recipe.GuiUsageRecipe;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import hardcorequesting.DeathStats;
import hardcorequesting.OPBookHelper;
import hardcorequesting.QuestingData;
import hardcorequesting.SaveHelper;
import hardcorequesting.Team;
import hardcorequesting.bag.BagTier;
import hardcorequesting.bag.Group;
import hardcorequesting.bag.GroupTier;
import hardcorequesting.client.sounds.SoundHandler;
import hardcorequesting.items.ModItems;
import hardcorequesting.network.DataBitHelper;
import hardcorequesting.network.FileHelper;
import hardcorequesting.network.PacketHandler;
import hardcorequesting.quests.*;
import hardcorequesting.reputation.Reputation;
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
    private static QuestSet selectedSet;
    private static boolean isSetOpened;
    private static Quest selectedQuest;
    private static boolean isMainPageOpen = true;
    private static boolean isMenuPageOpen = true;
    private static boolean isBagPage;
    private static boolean isReputationPage;
    private static Group selectedGroup;
    public static Reputation selectedReputation;
    private static boolean isNEIActive = Loader.isModLoaded("NotEnoughItems");
    private static ItemStack selected;

	private final EntityPlayer player;
    private final boolean isOpBook;

    private ScrollBar setScroll;
    private ScrollBar descriptionScroll;
    private ScrollBar mainDescriptionScroll;
    private ScrollBar groupScroll;
    private ScrollBar tierScroll;
    public ScrollBar reputationDisplayScroll;
    public ScrollBar reputationScroll;
    public ScrollBar reputationTierScroll;
    private List<ScrollBar> scrollBars; {
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
                return !isReputationPage && isBagPage && selectedGroup == null && Group.getGroups().size() > VISIBLE_GROUPS ;
            }
        });

        scrollBars.add(tierScroll = new ScrollBar(312, 18, 186, 171, 69, TIERS_X) {
            @Override
            public boolean isVisible(GuiBase gui) {
                return !isReputationPage && isBagPage && selectedGroup == null && GroupTier.getTiers().size() > VISIBLE_TIERS ;
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
                return isReputationPage && !isBagPage && (getCurrentMode() != GuiQuestBook.EditMode.CREATE || selectedReputation == null) && Reputation.getReputationList().size() > VISIBLE_REPUTATIONS ;
            }
        });

        scrollBars.add(reputationDisplayScroll = new ScrollBar(160, 125, 87, 164, 69, INFO_LEFT_X) {
            @Override
            public boolean isVisible(GuiBase gui) {
                return isMenuPageOpen && !isMainPageOpen && Reputation.getReputationList().size() > VISIBLE_DISPLAY_REPUTATIONS ;
            }
        });
    }


    private TextBoxGroup.TextBox textBoxGroupAmount;
    private TextBoxGroup textBoxes; {
        textBoxes = new TextBoxGroup();
        textBoxes.add(textBoxGroupAmount = new TextBoxGroup.TextBox(this, "0", 180, 30, false) {
            @Override
            protected boolean isCharacterValid(char c) {
                return getText().length() < 3 && Character.isDigit(c);
            }

            @Override
            protected void textChanged(GuiBase gui) {
                try{
                    int number;
                    if (getText().equals("")) {
                        number = 1;
                    }else{
                        number = Integer.parseInt(getText());
                    }

                    if (selectedGroup != null) {
                        selectedGroup.setLimit(number);
                    }
                }catch (Exception ignored) {}

            }
        });
    }

	private int tick;
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
        buttons.add(saveButton = new LargeButton("Save all", 360, 10) {
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

        buttons.add(new LargeButton("Open", 245, 190) {
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

        buttons.add(new LargeButton("Create Set", 185, 50) {
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
                for (QuestSet set : Quest.getQuestSets())
                {
                    if (set.getName().startsWith("Unnamed set")) i++;
                }
                Quest.getQuestSets().add(new QuestSet("Unnamed set" + (i == 0 ? "" : i), "No description"));
                SaveHelper.add(SaveHelper.EditType.SET_CREATE);
            }
        });

        buttons.add(new LargeButton("Create Group", 100, 175) {
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

        buttons.add(new LargeButton("Create Tier", 100, 200) {
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

        buttons.add(new LargeButton("Create Tier", 20, 20) {
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
	private static final int TEXTURE_WIDTH = 170*2;
	public static final int PAGE_WIDTH = 170;
	private static final int TEXTURE_HEIGHT = 234;
	
	@Override
	public void updateScreen() {
		++tick;
		
		super.updateScreen();
	}

    public static final int VISIBLE_REPUTATION_TIERS = 9;
    public static final int VISIBLE_REPUTATIONS = 10;
    public static final int VISIBLE_DISPLAY_REPUTATIONS = 4;

    private static final int LIST_X = 25;
    private static final int LIST_Y = 20;
    private static final int TEXT_HEIGHT = 9;
    private static final int TEXT_SPACING = 20;
    private static final int LINE_2_X = 10;
    private static final int LINE_2_Y = 12;
    private static final int DESCRIPTION_X = 180;
    private static final int DESCRIPTION_Y = 20;
    private static final int VISIBLE_DESCRIPTION_LINES = 7;
    private static final int VISIBLE_MAIN_DESCRIPTION_LINES = 21;
    private static final int INFO_Y = 100;
    private static final int VISIBLE_SETS = 7;

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

    private static final int TIERS_X = 180;
    private static final int TIERS_Y = 20;
    private static final int TIERS_SPACING = 25;
    private static final int TIERS_SECOND_LINE_X = -5;
    private static final int TIERS_SECOND_LINE_Y = 12;
    private static final int WEIGHT_SPACING = 25;
    private static final int VISIBLE_TIERS = 8;

    private static final int GROUPS_X = 20;
    private static final int GROUPS_Y = 20;
    private static final int GROUPS_SPACING = 25;
    private static final int GROUPS_SECOND_LINE_X = 5;
    private static final int GROUPS_SECOND_LINE_Y = 12;
    private static final int VISIBLE_GROUPS = 8;

    private static final int GROUP_ITEMS_X = 20;
    private static final int GROUP_ITEMS_Y = 40;
    private static final int GROUP_ITEMS_SPACING = 20;
    private static final int ITEMS_PER_LINE = 7;

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
                int startLine = mainDescriptionScroll.isVisible(this) ? Math.round((Quest.getMainDescription(this).size() - VISIBLE_MAIN_DESCRIPTION_LINES) * mainDescriptionScroll.getScroll()) : 0;
                drawString(Quest.getMainDescription(this), startLine, VISIBLE_MAIN_DESCRIPTION_LINES, DESCRIPTION_X, DESCRIPTION_Y, 0.7F, 0x404040);
                drawCenteredString("Click here to start", 0, 195, 0.7F, PAGE_WIDTH, TEXTURE_HEIGHT - 195, 0x707070);
                if (SoundHandler.hasLoreMusic() && !SoundHandler.isLorePlaying()) {
                    drawCenteredString("Click to play again", PAGE_WIDTH, 195, 0.7F, PAGE_WIDTH - 10, TEXTURE_HEIGHT - 195, 0x707070);
                }
                if (QuestLine.getActiveQuestLine().front == null && QuestLine.getActiveQuestLine().mainPath != null) {
                    File file = new File(QuestLine.getActiveQuestLine().mainPath + "front.png");
                    if (file.exists()) {
                        try {
                            BufferedImage img = ImageIO.read(file);
                            DynamicTexture dm = new DynamicTexture(img);
                            QuestLine.getActiveQuestLine().front = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(FRONT_KEY, dm);
                        }catch (IOException ignored) {
                            QuestLine.getActiveQuestLine().front = ResourceHelper.getResource("front");
                        }
                    }else{
                        QuestLine.getActiveQuestLine().front = ResourceHelper.getResource("front");
                    }
                }

                if (QuestLine.getActiveQuestLine().front != null) {
                    ResourceHelper.bindResource(QuestLine.getActiveQuestLine().front);
                    applyColor(0xFFFFFFFF);
                    drawRect(20, 20, 0, 0, 140, 180);
                }
            }else if(isMenuPageOpen) {
                drawString("Lives", INFO_RIGHT_X, INFO_LIVES_Y, 0x404040);
                drawString("Party", INFO_RIGHT_X, INFO_TEAM_Y, 0x404040);
                drawString("Quests", INFO_LEFT_X, INFO_QUESTS_Y, 0x404040);
                drawString("Reputation", INFO_LEFT_X, INFO_REPUTATION_Y, 0x404040);

                drawQuestInfo(null, INFO_LEFT_X, INFO_QUESTS_Y + (int)(TEXT_HEIGHT * 1.5F));
                drawString("Click here to show quests", INFO_LEFT_X, INFO_QUESTS_Y + QUEST_CLICK_TEXT_Y, 0.7F, 0x707070);

                if (QuestingData.isHardcoreActive()) {
                    boolean almostOut = QuestingData.getQuestingData(player).getLives() == QuestingData.getQuestingData(player).getLivesToStayAlive();
                    if (almostOut) {
                        drawString(GuiColor.RED + "If you die, you're out.", INFO_RIGHT_X + 50, INFO_LIVES_Y + 2, 0.7F, 0x404040);
                    }

                    GL11.glColor4f(1, 1, 1, 1);
                    int lives = QuestingData.getQuestingData(player).getLives();
                    int count, spacing, heartX;
                    if (lives < 8) {
                        heartX = INFO_RIGHT_X + INFO_HEARTS_X;
                        count = lives;
                        spacing = INFO_HEARTS_SPACING;
                    }else{
                        heartX = INFO_RIGHT_X + INFO_HEARTS_X + 20;
                        count = 3;
                        spacing = 3;
                        drawString(lives + " x", INFO_RIGHT_X + 5, INFO_LIVES_Y + INFO_HEARTS_Y + 5, 0.7F, 0x404040);
                    }

                    for (int i = 0; i < count; i++) {
                        drawItem(new ItemStack(ModItems.hearts, 1, 3), heartX + spacing * i, INFO_LIVES_Y + INFO_HEARTS_Y, almostOut);
                    }
                }else{
                    drawString(getLinesFromText("Hardcore mode is not active, you have an infinite amount of lives.", 0.5F, PAGE_WIDTH - 30), INFO_RIGHT_X, INFO_LIVES_Y + 12, 0.5F, 0x707070);
                }


                int deaths = DeathStats.getDeathStats(QuestingData.getUserName(player)).getTotalDeaths();
                drawString("You've currently died " + deaths + " " + (deaths == 1 ? "time" : "times"), INFO_RIGHT_X, INFO_DEATHS_Y + DEATH_TEXT_Y, 0.7F, 0x404040);
                drawString("Click here for more info", INFO_RIGHT_X, INFO_DEATHS_Y + DEATH_CLICK_TEXT_Y, 0.7F, 0x707070);


                String str;
                Team team = QuestingData.getQuestingData(player).getTeam();
                if (team.isSingle()) {
                    int invites = team.getInvites() == null ? 0 : team.getInvites().size();
                    if (invites > 0) {
                        str = "You have " + invites + " party " + (invites == 1 ? "invite" : "invites");
                    }else{
                        str = "You're currently not in a party.";
                    }
                }else{
                    int players = 0;
                    for (Team.PlayerEntry player : team.getPlayers()) {
                        if (player.isInTeam()) {
                            players++;
                        }
                    }
                    str = "You're in a party with " + players + " " + (players == 1 ? "player" : "players");
                }

                drawString(str, INFO_RIGHT_X, INFO_TEAM_Y + TEAM_TEXT_Y, 0.7F, 0x404040);
                drawString("Click here to open party window", INFO_RIGHT_X, INFO_TEAM_Y + TEAM_CLICK_TEXT_Y, 0.7F, 0x707070);

                if (isOpBook) {
                    drawString("Reset the quest progress for the entire party.", 22, 182, 0.6F, 0x404040);
                    drawString(getLinesFromText("Hold shift+ctrl while clicking to confirm.", 0.6F, 70), 22, 192, 0.6F, GuiColor.RED.getHexColor());
                }


                Reputation.drawAll(this, INFO_LEFT_X + INFO_REPUTATION_OFFSET_X, INFO_REPUTATION_Y + INFO_REPUTATION_OFFSET_Y, x, y, player);
            }else if (isBagPage) {
                if (selectedGroup != null) {
                    drawString(selectedGroup.getName(), GROUPS_X, GROUPS_Y, selectedGroup.getTier().getColor().getHexColor());
                    List<ItemStack> items = selectedGroup.getItems();
                    for (int i = 0; i < Math.min(DataBitHelper.GROUP_ITEMS.getMaximum(), items.size() + 1); i++) {
                        ItemStack itemStack = i < items.size() ? items.get(i) : null;

                        int xPos = (i % ITEMS_PER_LINE) * GROUP_ITEMS_SPACING + GROUP_ITEMS_X;
                        int yPos = (i / ITEMS_PER_LINE) * GROUP_ITEMS_SPACING + GROUP_ITEMS_Y;

                        drawItem(itemStack, xPos, yPos, x, y, false);
                    }

                    for (int i = 0; i < items.size(); i++) {
                        ItemStack itemStack = items.get(i);

                        int xPos = (i % ITEMS_PER_LINE) * GROUP_ITEMS_SPACING + GROUP_ITEMS_X;
                        int yPos = (i / ITEMS_PER_LINE) * GROUP_ITEMS_SPACING + GROUP_ITEMS_Y;

                        if (inBounds(xPos, yPos, ITEM_SIZE, ITEM_SIZE, x, y)) {
                            if (itemStack != null && itemStack.getItem() != null) {
                                try {
                                    drawMouseOver(itemStack.getTooltip(Minecraft.getMinecraft().thePlayer, Minecraft.getMinecraft().gameSettings.advancedItemTooltips), x + left, y + top);
                                }catch (Exception ignored) {}
                            }
                            break;
                        }
                    }

                    drawString("Maximum retrieval count", 180, 20, 0x404040);
                    drawString("Leave at 0 for no restriction", 180, 48, 0.7F, 0x404040);
                    textBoxes.draw(this);
                }else{
                    List<GroupTier> tiers = GroupTier.getTiers();
                    int start = tierScroll.isVisible(this) ? Math.round((tiers.size() - VISIBLE_TIERS) * tierScroll.getScroll()) : 0;
                    for (int i = start; i < Math.min(start + VISIBLE_TIERS, tiers.size()); i++) {
                        GroupTier groupTier = tiers.get(i);

                        String str = groupTier.getName();
                        int yPos = TIERS_Y + TIERS_SPACING * (i - start);
                        boolean inBounds = inBounds(TIERS_X, yPos, getStringWidth(str), TEXT_HEIGHT, x, y);
                        int color = groupTier.getColor().getHexColor();
                        if (inBounds) {
                            color &= 0xFFFFFF;
                            color |= 0xBB << 24;
                            GL11.glEnable(GL11.GL_BLEND);
                        }
                        drawString(str, TIERS_X, yPos, color);
                        if (inBounds) {
                            GL11.glDisable(GL11.GL_BLEND);
                        }

                        for (int j = 0; j < BagTier.values().length; j++) {
                            BagTier bagTier = BagTier.values()[j];
                            drawCenteredString(bagTier.getColor().toString() + groupTier.getWeights()[j], TIERS_X + TIERS_SECOND_LINE_X + j * WEIGHT_SPACING, yPos + TIERS_SECOND_LINE_Y, 0.7F, WEIGHT_SPACING, 0, 0x404040);
                        }
                    }

                    List<Group> groups = Group.getGroups();
                    start = groupScroll.isVisible(this) ? Math.round((groups.size() - VISIBLE_GROUPS) * groupScroll.getScroll()) : 0;
                    for (int i = start; i < Math.min(start + VISIBLE_GROUPS, groups.size()); i++) {
                        Group group = groups.get(i);

                        String str = group.getName();
                        int yPos = GROUPS_Y + GROUPS_SPACING * (i - start);
                        boolean inBounds = inBounds(GROUPS_X, yPos, getStringWidth(str), TEXT_HEIGHT, x, y);
                        int color = group.getTier().getColor().getHexColor();
                        boolean selected = group == modifyingGroup;
                        if (inBounds || selected) {
                            color &= 0xFFFFFF;
                            GL11.glEnable(GL11.GL_BLEND);

                            if (selected) {
                                color |= 0x50 << 24;
                            }else{
                                color |= 0xBB << 24;
                            }
                        }

                        drawString(str, GROUPS_X, yPos, color);
                        if (inBounds || selected) {
                            GL11.glDisable(GL11.GL_BLEND);
                        }

                        drawString(group.getItems().size() + " items", GROUPS_X + GROUPS_SECOND_LINE_X, yPos + GROUPS_SECOND_LINE_Y, 0.7F, 0x404040);
                    }
                }
            }else if(isReputationPage) {
                Reputation.drawEditPage(this, x, y);
            }else if (selectedSet == null || !isSetOpened) {

                List<QuestSet> questSets = Quest.getQuestSets();
                int start = setScroll.isVisible(this) ? Math.round((Quest.getQuestSets().size() - VISIBLE_SETS) * setScroll.getScroll()) : 0;
                for (int i = start; i < Math.min(start + VISIBLE_SETS, questSets.size()); i++) {
                    QuestSet questSet = questSets.get(i);

                    int setY = LIST_Y + (i - start) * (TEXT_HEIGHT + TEXT_SPACING);

                    int total = questSet.getQuests().size();
                    boolean enabled = questSet.isEnabled(player);
                    int completedCount = enabled ? questSet.getCompletedCount(player) : 0; //no need to check for the completed count if it's not enabled

                    boolean completed = true;
                    int unclaimed = 0;
                    for (Quest quest : questSet.getQuests()) {
                        if (completed && !quest.isCompleted(player) && quest.isLinkFree(player)) {
                            completed = false;
                        }
                        if (quest.isCompleted(player) && quest.hasReward(player)) unclaimed++;
                    }
                    boolean selected = questSet == selectedSet;
                    boolean inBounds = inBounds(LIST_X, setY, getStringWidth(questSet.getName(i)), TEXT_HEIGHT, x, y);
                    int color = modifyingQuestSet == questSet ? 0x4040DD : enabled ? completed ? selected ? inBounds ? 0x40BB40 : 0x40A040 : inBounds ? 0x10A010 : 0x107010 : selected ? inBounds ? 0xAAAAAA : 0x888888 : inBounds ? 0x666666 : 0x404040 : 0xDDDDDD;
                    drawString(questSet.getName(i), LIST_X, setY, color);

                    String info = enabled ? completed ? "All Quests Completed" : ((completedCount * 100) / total) + "% Completed" : "Locked";
                    drawString(info, LIST_X + LINE_2_X, setY + LINE_2_Y, 0.7F, color);
                    if (enabled && unclaimed != 0)
                    {
                        String toClaim = GuiColor.PURPLE.toString() + unclaimed + (unclaimed == 1 ? " quest" : " quests") + " with unclaimed rewards";
                        drawString(toClaim, LIST_X + LINE_2_X, setY + LINE_2_Y + 8, 0.7F, 0xFFFFFFFF);
                    }
                }

                if ((Quest.isEditing && currentMode == EditMode.CREATE)) {
                    drawString(getLinesFromText("Click the button below to create a new empty quest set.", 0.7F, 130), DESCRIPTION_X, DESCRIPTION_Y, 0.7F, 0x404040);
                }else{
                    if (selectedSet != null){
                        int startLine = descriptionScroll.isVisible(this) ? Math.round((selectedSet.getDescription(this).size() - VISIBLE_DESCRIPTION_LINES) * descriptionScroll.getScroll()) : 0;
                        drawString(selectedSet.getDescription(this), startLine, VISIBLE_DESCRIPTION_LINES, DESCRIPTION_X, DESCRIPTION_Y, 0.7F, 0x404040);
                    }

                    drawQuestInfo(selectedSet, DESCRIPTION_X, selectedSet == null ? DESCRIPTION_Y : INFO_Y);
                }


            }else{
                if (selectedQuest == null)  {
                    if (isOpBook) {
                        drawString(getLinesFromText("Hold shift and click on quests to automatically complete them or reset their progress.", 0.7F, 130), 184, 192, 0.7F, 0x707070);
                    }

                    for (Quest child : selectedSet.getQuests()) {
                        if ((Quest.isEditing || child.isVisible(player))) {
                            for (Quest parent : child.getRequirement()) {
                                if ((Quest.isEditing || parent.isVisible(player))) {
                                    if (parent.hasSameSetAs(child)) {
                                        int color = Quest.isEditing && (!child.isVisible(player) || !parent.isVisible(player)) ? 0x55404040 : 0xFF404040;
                                        drawLine(left + parent.getGuiCenterX(), top + parent.getGuiCenterY(),
                                                left + child.getGuiCenterX(), top + child.getGuiCenterY(),
                                                5,
                                                color);
                                    }
                                }
                            }
                        }
                    }
                    if (Quest.isEditing) {
                        for (Quest child : selectedSet.getQuests()) {
                            for (Quest parent : child.getOptionLinks()) {
                                if (parent.hasSameSetAs(child)) {
                                    int color = !child.isVisible(player) || !parent.isVisible(player) ? 0x554040DD : 0xFF4040DD;
                                    drawLine(left + parent.getGuiCenterX(), top + parent.getGuiCenterY(),
                                            left + child.getGuiCenterX(), top + child.getGuiCenterY(),
                                            5,
                                            color);
                                }
                            }
                        }
                    }



                    for (Quest quest : selectedSet.getQuests()) {
                        if ((Quest.isEditing || quest.isVisible(player))) {

                            GL11.glPushMatrix();
                            GL11.glEnable(GL11.GL_BLEND);
                            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                            applyColor(quest == modifyingQuest ? 0xFFBBFFBB : quest.getColorFilter(player, tick));


                                ResourceHelper.bindResource(MAP_TEXTURE);

                            drawRect(quest.getGuiX(), quest.getGuiY(), quest.getGuiU(), quest.getGuiV(player, x, y), quest.getGuiW(), quest.getGuiH());


                            int iconX = quest.getGuiCenterX() - 8;
                            int iconY = quest.getGuiCenterY() - 8;

                            if (quest.useBigIcon()) {
                                iconX++;
                                iconY++;
                            }

                            drawItem(quest.getIcon(), iconX, iconY, true);
                            GL11.glPopMatrix();
                            //ResourceHelper.bindResource(QUEST_ICONS);
                            //drawRect(quest.getIconX(), quest.getIconY(), quest.getIconU(), quest.getIconV(), quest.getIconSize(), quest.getIconSize());
                        }

                    }


                    for (Quest quest : selectedSet.getQuests()) {
                        boolean editing = Quest.isEditing && !isCtrlKeyDown();
                        if ((editing || quest.isVisible(player)) && quest.isMouseInObject(x, y)) {
                            boolean shouldDrawText = false;
                            boolean enabled = quest.isEnabled(player);
                            String txt = "";

                            if (enabled || editing) {
                                txt += quest.getName();
                            }

                            if (!enabled) {
                                if (editing) {
                                    txt += "\n";
                                }
                                txt += GuiColor.GRAY + "Locked Quest";
                            }

                            if (!enabled || editing) {
                                int totalParentCount = 0;
                                int totalCompletedCount = 0;
                                int parentCount = 0;
                                int completed = 0;
                                List<Quest> externalQuests = new ArrayList<Quest>();
                                for (Quest parent : quest.getRequirement()) {
                                    totalParentCount++;
                                    boolean isCompleted = parent.isCompleted(player);
                                    if (isCompleted) {
                                        totalCompletedCount++;
                                    }
                                    if (!parent.hasSameSetAs(quest)) {
                                        externalQuests.add(parent);
                                        parentCount++;
                                        if (isCompleted) {
                                            completed++;
                                        }
                                    }

                                }

                                if (editing && totalParentCount > 0) {
                                    txt += "\n" + GuiColor.GRAY + "Requires " + (totalParentCount - totalCompletedCount) + "/" + totalParentCount + " " + (totalParentCount == 1 ? "quest" : "quests") + " to be completed.";

                                    if (Keyboard.isKeyDown(Keyboard.KEY_R)) {
                                        txt += " [Holding R]";
                                        for (Quest parent : quest.getRequirement()) {
                                            txt += "\n" + GuiColor.GRAY + parent.getName();
                                            if (parent.isCompleted(player)) {
                                                txt += " " + GuiColor.WHITE + " [Completed]";
                                            }
                                        }
                                    }else{
                                        txt += " [Hold R]";
                                    }
                                }

                                int allowedUncompleted = quest.getUseModifiedParentRequirement() ? Math.max(0, quest.getRequirement().size() - quest.getParentRequirementCount()) : 0;
                                if (parentCount - completed > allowedUncompleted || (editing && parentCount > 0)) {
                                    txt += "\n" + GuiColor.PINK + "Requires " + (parentCount - completed) + "/" + parentCount + " " + (parentCount == 1 ? "quest" : "quests") + " to be completed elsewhere.";
                                    shouldDrawText = true;
                                    if (editing) {
                                        if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
                                            txt += " [Holding E]";
                                            for (Quest parent : externalQuests) {
                                                txt += "\n" + GuiColor.PINK + parent.getName() + " (" + parent.getQuestSet().getName() + ")";
                                                if (parent.isCompleted(player)) {
                                                    txt += " " + GuiColor.WHITE + " [Completed]";
                                                }
                                            }
                                        }else{
                                            txt += " [Hold E]";
                                        }
                                    }
                                }

                                if (editing && quest.getUseModifiedParentRequirement()) {
                                    txt += "\n" + GuiColor.MAGENTA;
                                    int amount = quest.getParentRequirementCount();
                                    if (amount < quest.getRequirement().size()) {
                                        txt += "Only requires " + amount + " " + (amount == 1 ? "quest" : "quests") + " to be completed";
                                    }else if(amount > quest.getRequirement().size()) {
                                        txt += "Requires " + amount + " " + (amount == 1 ? "quest" : "quests") + " to be completed. This is more than there are, weird.";
                                    }else{
                                        txt += "Requires all " + amount + " " + (amount == 1 ? "quest" : "quests");
                                    }

                                }
                            }

                            if (enabled || editing) {
                                if (quest.isCompleted(player)) {
                                    txt += "\n" + GuiColor.GREEN + "Completed";
                                }
                                if (quest.hasReward(player)) {
                                    txt += "\n" + GuiColor.PURPLE + "Unclaimed reward";
                                }

                                String repeatMessage = enabled ? quest.getRepeatInfo().getMessage(quest, player) : quest.getRepeatInfo().getShortMessage();
                                if (repeatMessage != null) {
                                    txt += "\n" + repeatMessage;
                                }

                                if (editing) {
                                    int totalTasks = 0;
                                    int completedTasks = 0;
                                    for (QuestTask task : quest.getTasks()) {
                                        totalTasks++;
                                        if (task.isCompleted(player)) {
                                            completedTasks++;
                                        }
                                    }

                                    if (totalTasks == 0) {
                                        txt += "\n" + GuiColor.RED + "This quest has no tasks!";
                                    }else{
                                        txt += "\n" + GuiColor.CYAN + completedTasks + "/" + totalTasks + " completed tasks.";

                                        if (Keyboard.isKeyDown(Keyboard.KEY_T)) {
                                            txt += " [Holding T]";
                                            for (QuestTask task : quest.getTasks()) {
                                                txt += "\n" + GuiColor.CYAN + task.getDescription();
                                                if (task.isCompleted(player)) {
                                                    txt += GuiColor.WHITE + " [Completed]";
                                                }
                                            }
                                        }else{
                                            txt += " [Hold T]";
                                        }
                                    }

                                    String triggerMessage = quest.getTriggerType().getMessage(quest);
                                    if (triggerMessage != null) {
                                        txt += "\n" + triggerMessage;
                                    }

                                    if (!quest.isVisible(player)) {
                                        String invisibilityMessage;
                                        if (quest.isLinkFree(player)) {
                                            boolean parentInvisible = false;
                                            for (Quest parent : quest.getRequirement()) {
                                                if (!parent.isVisible(player)) {
                                                    parentInvisible = true;
                                                    break;
                                                }
                                            }


                                            switch (quest.getTriggerType()) {
                                                case ANTI_TRIGGER:
                                                    invisibilityMessage = "Invisible while locked";
                                                    break;
                                                case QUEST_TRIGGER:
                                                    invisibilityMessage = "Permanently invisible";
                                                    parentInvisible = false;
                                                    break;
                                                case TASK_TRIGGER:
                                                    invisibilityMessage = "Invisible until " + quest.getTriggerTasks() + " " + (quest.getTriggerTasks() > 1 ? "tasks have" : "task has") + " been completed.";
                                                    break;
                                                default:
                                                    invisibilityMessage = null;
                                            }

                                            if (parentInvisible) {
                                                String parentText = "Inherited invisibility";
                                                if (invisibilityMessage == null) {
                                                    invisibilityMessage = parentText;
                                                }else{
                                                    invisibilityMessage = parentText + " and " + invisibilityMessage;
                                                }
                                            }

                                        }else{
                                            invisibilityMessage = "Invisible through quest option.";
                                        }

                                        if (invisibilityMessage != null) {
                                            txt += "\n" + GuiColor.LIGHT_BLUE + invisibilityMessage;
                                        }
                                    }


                                    List<Integer> ids = new ArrayList<Integer>();
                                    for (Quest option : quest.getOptionLinks()) {
                                        ids.add((int)option.getId());
                                    }
                                    for (Quest option : quest.getReversedOptionLinks()) {
                                        int id = option.getId();
                                        if (!ids.contains(id)) {
                                            ids.add(id);
                                        }
                                    }
                                    int optionLinks = ids.size();
                                    if (optionLinks > 0) {
                                        txt += "\n" + GuiColor.BLUE + "Connected to " + optionLinks + " " + (optionLinks > 1 ? "quests" : "quest") + " through option links.";

                                        if (Keyboard.isKeyDown(Keyboard.KEY_O)) {
                                            txt += " [Holding O]";
                                            for (int id : ids) {
                                                Quest option = Quest.getQuest(id);
                                                txt += "\n" + GuiColor.BLUE + option.getName();
                                                if (!option.hasSameSetAs(quest)) {
                                                    txt += " (" + option.getQuestSet().getName() + ")";
                                                }
                                            }
                                        }else{
                                            txt += " [Hold O]";
                                        }
                                    }

                                }


                                List<Quest> externalQuests = new ArrayList<Quest>();
                                int childCount = 0;
                                for (Quest child : quest.getReversedRequirement()) {
                                    if (!quest.hasSameSetAs(child)) {
                                        childCount++;
                                        externalQuests.add(child);
                                    }
                                }

                                if (childCount > 0) {
                                    txt += "\n" + GuiColor.PINK + "Unlocks " + childCount + " " + (childCount == 1 ? "quest" : "quests") + " elsewhere.";
                                    if (editing) {
                                        if (Keyboard.isKeyDown(Keyboard.KEY_U)) {
                                            txt += " [Holding U]";
                                            for (Quest child : externalQuests) {
                                                txt += "\n" + GuiColor.PINK + child.getName() + " (" + child.getQuestSet().getName() + ")";
                                            }
                                        }else{
                                            txt += " [Hold U]";
                                        }
                                    }
                                }
                                shouldDrawText = true;

                            }




                            if (editing) {
                                txt += "\n\n" + GuiColor.GRAY + "Hold Ctrl to see this as a non-editor.";
                            }

                            if (isOpBook && GuiScreen.isShiftKeyDown()) {
                                if (quest.isCompleted(player)) {
                                    txt += "\n\n" + GuiColor.RED + "Click to reset quest";
                                }else{
                                    txt += "\n\n" + GuiColor.ORANGE + "Click to complete quest";
                                }
                            }

                            if (shouldDrawText) {
                                drawMouseOver(txt, x0, y0);
                            }
                            break;
                        }
                    }



                }else{
                    selectedQuest.drawMenu(this, player, x, y);
                }
            }


            if (Quest.isEditing) {
                for (EditButton button : getButtons()) {
                    button.drawInfo(x, y);
                }
            }



            if (currentMode == EditMode.DELETE) {
                GL11.glPushMatrix();
                GL11.glTranslatef(0, 0, 200);
                drawCenteredString("WARNING!", 0, 0, 2F, TEXTURE_WIDTH, TEXTURE_HEIGHT, 0xFF0000);
                drawCenteredString("You're now deleting everything you click on!", 0, fontRendererObj.FONT_HEIGHT * 2, 1F, TEXTURE_WIDTH, TEXTURE_HEIGHT, 0xFF0000);
                applyColor(0xFFFFFFFF);

                    ResourceHelper.bindResource(MAP_TEXTURE);

                GL11.glPopMatrix();
            }


        }else{
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
                }else{
                    saveResult = null;
                }
            }
        }

        if (shouldDisplayAndIsInArrowBounds(false, x, y)) {
            drawMouseOver("Go back\n" + GuiColor.GRAY + "You can also right click anywhere", x + left, y + top);
        }else if(shouldDisplayAndIsInArrowBounds(true, x, y)) {
            drawMouseOver("Back to menu", x + left, y + top);
        }
     }

    private void drawQuestInfo(QuestSet set, int x, int y) {
        int completed = 0;
        int reward = 0;
        int enabled = 0;
        int total = 0;
        int realTotal = 0;

        for (Quest quest : Quest.getQuests()) {
            if (set == null || quest.hasSet(set)) {
                realTotal++;
                if (quest.isVisible(player)) {
                    total++;
                    if (quest.isEnabled(player)) {
                        enabled++;
                        if (quest.isCompleted(player)) {
                            completed++;
                            if (quest.hasReward(player)) {
                                reward++;
                            }
                        }
                    }
                }
            }
        }


        List<String> info = new ArrayList<String>();
        info.add(GuiColor.GRAY.toString() + total + " " + (total == 1 ? "quest" : "quests") + " in total");
        info.add(GuiColor.CYAN.toString() + enabled + " unlocked " + (enabled == 1 ? "quest" : "quests"));
        info.add(GuiColor.GREEN.toString() + completed + " completed " + (completed == 1 ? "quest" : "quests"));
        info.add(GuiColor.LIGHT_BLUE.toString() + (enabled - completed) + " " + (enabled - completed == 1 ? "quest" : "quests") + " available for completion");
        if (reward > 0) {
            info.add(GuiColor.PURPLE.toString() + reward + " " + (reward == 1 ? "quest" : "quests") + " with unclaimed rewards");
        }
        if (Quest.isEditing && !isCtrlKeyDown()) {
            info.add(GuiColor.LIGHT_GRAY.toString() + realTotal + " " + (realTotal == 1 ? "quest" : "quests") + " including invisible ones");
        }
        drawString(info, x, y, 0.7F, 0x404040);
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
            }else if (selectedQuest != null) {
                selectedQuest.onScroll(this, x, y, scroll);
            }else{
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
        }else if(isBagPage && selectedGroup != null) {
            textBoxes.onKeyStroke(this, c, k);
        }else if (isNEIActive())
        {
            handleNEI(k);
        }
    }

    private boolean isNEIActive()
    {
        return isNEIActive;
    }

    public static void setSelected(ItemStack stack)
    {
        selected = stack;
    }

    private void handleNEI(int k)
    {
        ItemStack stackover = selected;
        if(stackover != null)
        {
            if (k == NEIClientConfig.getKeyBinding("gui.usage") || (k == NEIClientConfig.getKeyBinding("gui.recipe") && NEIClientUtils.shiftKey()))
            {
                GuiUsageRecipe.openRecipeGui("item", stackover.copy());
            }

            if (k == NEIClientConfig.getKeyBinding("gui.recipe"))
            {
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
        }else if(shouldDisplayAndIsInArrowBounds(true, x, y)) {
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
                        if (currentMode == EditMode.BAG) {
                            currentMode = EditMode.NORMAL;
                            isBagPage = true;
                            isMenuPageOpen = false;
                        }else if(currentMode == EditMode.REPUTATION) {
                            currentMode = EditMode.NORMAL;
                            isReputationPage = true;
                            isMenuPageOpen = false;
                        }
                        break;
                    }
                }
            }
            for (ScrollBar scrollBar : scrollBars) {
                scrollBar.onClick(this, x, y);
            }

            if (isMainPageOpen) {
                if (x > 0 && x < PAGE_WIDTH && y > 205) {
                    isMainPageOpen = false;
                    SoundHandler.stopLoreMusic();
                }else if(x > PAGE_WIDTH && x < TEXTURE_WIDTH && y > 205) {
                    if (SoundHandler.hasLoreMusic() && !SoundHandler.isLorePlaying()) {
                        SoundHandler.playLoreMusic();
                    }
                }else{
                    if (Quest.isEditing && currentMode == EditMode.RENAME && inBounds(DESCRIPTION_X, DESCRIPTION_Y, 130, (int)(VISIBLE_MAIN_DESCRIPTION_LINES * TEXT_HEIGHT * 0.7F), x, y)) {
                        editMenu = new GuiEditMenuTextEditor(this, player);
                    }
                }
            }else if(isMenuPageOpen) {
                if(button == 1) {
                    isMainPageOpen = true;
                }else{
                    if(inBounds(INFO_RIGHT_X, INFO_TEAM_Y + TEAM_CLICK_TEXT_Y, PAGE_WIDTH, (int)(TEXT_HEIGHT * 0.7F), x, y)) {
                        editMenu = new GuiEditMenuTeam(this , player);
                    }else if(inBounds(INFO_RIGHT_X, INFO_DEATHS_Y + DEATH_CLICK_TEXT_Y, PAGE_WIDTH, (int) (TEXT_HEIGHT * 0.7F), x, y)) {
                        editMenu = new GuiEditMenuDeath(this, player);
                    }else if(inBounds(INFO_LEFT_X, INFO_QUESTS_Y + QUEST_CLICK_TEXT_Y, PAGE_WIDTH, (int)(TEXT_HEIGHT * 0.7F), x, y)) {
                        isMenuPageOpen = false;
                    }
                }
            }else if (isBagPage) {
                if (selectedGroup != null) {
                    if (button == 1) {
                        selectedGroup = null;
                    }else{
                        List<ItemStack> items = selectedGroup.getItems();
                        for (int i = 0; i < Math.min(DataBitHelper.GROUP_ITEMS.getMaximum(), items.size() + 1); i++) {
                            int xPos = (i % ITEMS_PER_LINE) * GROUP_ITEMS_SPACING + GROUP_ITEMS_X;
                            int yPos = (i / ITEMS_PER_LINE) * GROUP_ITEMS_SPACING + GROUP_ITEMS_Y;

                            if (inBounds(xPos, yPos, ITEM_SIZE, ITEM_SIZE, x, y)) {
                                if (currentMode == EditMode.ITEM) {
                                    ItemStack itemStack = i < items.size() ? items.get(i) : null;
                                    int amount;
                                    if (itemStack != null) {
                                        itemStack = itemStack.copy();
                                        amount = itemStack.stackSize;
                                    }else{
                                        amount = 1;
                                    }

                                    editMenu = new GuiEditMenuItem(this, player, itemStack, i, GuiEditMenuItem.Type.BAG_ITEM, amount, ItemPrecision.PRECISE);
                                }else if (currentMode == EditMode.DELETE) {
                                    selectedGroup.removeItem(i);
                                    SaveHelper.add(SaveHelper.EditType.GROUP_ITEM_REMOVE);
                                }
                                break;
                            }
                        }

                        textBoxes.onClick(this, x, y);
                    }
                }else{
                    if (button == 1) {
                        isBagPage = false;
                        isMenuPageOpen = true;
                    }else{
                        List<Group> groups = Group.getGroups();
                        int start = groupScroll.isVisible(this) ? Math.round((groups.size() - VISIBLE_GROUPS) * groupScroll.getScroll()) : 0;
                        for (int i = start; i < Math.min(start + VISIBLE_GROUPS, groups.size()); i++) {
                            Group group = groups.get(i);

                            int posY = GROUPS_Y + GROUPS_SPACING * (i - start);
                            if (inBounds(GROUPS_X, posY, getStringWidth(group.getName()), TEXT_HEIGHT, x, y)) {
                                if (currentMode == EditMode.TIER) {
                                    if (group == modifyingGroup) {
                                        modifyingGroup = null;
                                    }else{
                                        modifyingGroup = group;
                                    }
                                }else if(currentMode == EditMode.NORMAL) {
                                    selectedGroup = group;
                                    textBoxGroupAmount.setTextAndCursor(this, String.valueOf(selectedGroup.getLimit()));
                                }else if(currentMode == EditMode.RENAME) {
                                    editMenu = new GuiEditMenuTextEditor(this, player, group);
                                }else if(currentMode == EditMode.DELETE) {
                                    group.remove(i);
                                    SaveHelper.add(SaveHelper.EditType.GROUP_REMOVE);
                                }
                                break;
                            }
                        }

                        List<GroupTier> tiers = GroupTier.getTiers();
                        start = tierScroll.isVisible(this) ? Math.round((tiers.size() - VISIBLE_TIERS) * tierScroll.getScroll()) : 0;
                        for (int i = start; i < Math.min(start + VISIBLE_TIERS, tiers.size()); i++) {
                            GroupTier groupTier = tiers.get(i);

                            int posY = TIERS_Y + TIERS_SPACING * (i - start);
                            if (inBounds(TIERS_X, posY, getStringWidth(groupTier.getName()), TEXT_HEIGHT, x, y)) {
                                if (currentMode == EditMode.NORMAL) {
                                    editMenu = new GuiEditMenuTier(this, player, groupTier);
                                }else if (currentMode == EditMode.TIER && modifyingGroup != null) {
                                    modifyingGroup.setTier(groupTier);
                                    SaveHelper.add(SaveHelper.EditType.GROUP_CHANGE);
                                }else if(currentMode == EditMode.RENAME) {
                                    editMenu = new GuiEditMenuTextEditor(this, player, groupTier);
                                }else if(currentMode == EditMode.DELETE) {
                                    if (tiers.size() > 1 || groups.size() == 0) {
                                        for (Group group : groups) {
                                            if (group.getTier() == groupTier) {
                                                group.setTier(i == 0 ? tiers.get(1) : tiers.get(0));
                                            }
                                        }
                                        tiers.remove(i);
                                        SaveHelper.add(SaveHelper.EditType.TIER_REMOVE);
                                    }
                                }
                                break;
                            }
                        }
                    }
                }

            }else if(isReputationPage) {
                if (button == 1) {
                    isMenuPageOpen = true;
                    isReputationPage = false;
                }else{
                    Reputation.onClick(this, x, y, player);
                }
            }else if (selectedSet == null || !isSetOpened) {
                if (button == 1) {
                    isMenuPageOpen = true;
                    return;
                }

                List<QuestSet> questSets = Quest.getQuestSets();
                int start = setScroll.isVisible(this) ? Math.round((Quest.getQuestSets().size() - VISIBLE_SETS) * setScroll.getScroll()) : 0;
                for (int i = start; i < Math.min(start + VISIBLE_SETS, questSets.size()); i++) {
                    QuestSet questSet = questSets.get(i);

                    int setY = LIST_Y + (i - start) * (TEXT_HEIGHT + TEXT_SPACING);
                    if (inBounds(LIST_X, setY, getStringWidth(questSet.getName(i)), TEXT_HEIGHT, x, y)) {
                        if (Quest.isEditing && currentMode == EditMode.DELETE && questSet.getQuests().isEmpty()) {
                            for (int j = questSet.getId() + 1; j < Quest.getQuestSets().size(); j++) {
                                Quest.getQuestSets().get(j).decreaseId();
                            }
                            Quest.getQuestSets().remove(questSet);
                            SaveHelper.add(SaveHelper.EditType.SET_REMOVE);
                        }else if (Quest.isEditing && currentMode == EditMode.SWAP_SELECT) {
                            if (modifyingQuestSet == questSet) {
                                modifyingQuestSet = null;
                            }else{
                                modifyingQuestSet = questSet;
                            }
                        }else if (Quest.isEditing && currentMode == EditMode.RENAME) {
                            editMenu = new GuiEditMenuTextEditor(this, player,  questSet, true);
                        }else if ((Quest.isEditing && currentMode == EditMode.NORMAL) || (!Quest.isEditing && questSet.isEnabled(player))) {
                            if (selectedSet == questSet) {
                                selectedSet = null;
                            }else{
                                selectedSet = questSet;
                            }
                        }
                        break;
                    }
                }


                if (Quest.isEditing && currentMode == EditMode.RENAME) {
                    if (inBounds(DESCRIPTION_X, DESCRIPTION_Y, 130, (int)(VISIBLE_DESCRIPTION_LINES * TEXT_HEIGHT * 0.7F), x, y)) {
                        editMenu = new GuiEditMenuTextEditor(this, player, selectedSet, false);
                    }
                }
            }else{
                if (selectedQuest == null) {
                    if (button == 1) {
                        isSetOpened = false;
                    }else if (Quest.isEditing && currentMode == EditMode.CREATE) {
                        if (x > 0 && Quest.size() < DataBitHelper.QUESTS.getMaximum()) {
                            int i = 0;
                            for (Quest quest : selectedSet.getQuests())
                            {
                                if (quest.getName().startsWith("Unnamed")) i++;
                            }
                            Quest newQuest = new Quest(Quest.size(), "Unnamed" + (i == 0 ? "" : i), "Unnamed quest", 0, 0, false);
                            newQuest.setGuiCenterX(x);
                            newQuest.setGuiCenterY(y);
                            newQuest.setQuestSet(selectedSet);
                            SaveHelper.add(SaveHelper.EditType.QUEST_CREATE);
                        }
                    }else{
                        for (Quest quest : selectedSet.getQuests()) {
                            if ((Quest.isEditing ||quest.isVisible(player)) && quest.isMouseInObject(x, y)) {
                                if (Quest.isEditing && currentMode != EditMode.NORMAL) {
                                    if (currentMode == EditMode.MOVE) {
                                        modifyingQuest = quest;
                                        SaveHelper.add(SaveHelper.EditType.QUEST_MOVE);
                                    }else if(currentMode == EditMode.REQUIREMENT) {
                                        if (modifyingQuest == quest) {
                                            if (GuiScreen.isShiftKeyDown()) {
                                                modifyingQuest.clearRequirements();
                                            }
                                            modifyingQuest = null;
                                        }else if(modifyingQuest == null) {
                                            modifyingQuest = quest;
                                        }else{
                                            modifyingQuest.addRequirement(quest.getId());
                                        }
                                    }else if(currentMode == EditMode.SIZE) {
                                        int cX = quest.getGuiCenterX();
                                        int cY = quest.getGuiCenterY();
                                        quest.setBigIcon(!quest.useBigIcon());
                                        quest.setGuiCenterX(cX);
                                        quest.setGuiCenterY(cY);
                                        SaveHelper.add(SaveHelper.EditType.QUEST_SIZE_CHANGE);
                                    }else if(currentMode == EditMode.ITEM) {
                                        editMenu = new GuiEditMenuItem(this, player, quest.getIcon(), quest.getId(), GuiEditMenuItem.Type.QUEST_ICON, 1, ItemPrecision.PRECISE);
                                    }else if(currentMode == EditMode.DELETE) {
                                        Quest.removeQuest(quest);
                                        SaveHelper.add(SaveHelper.EditType.QUEST_REMOVE);
                                    }else if(currentMode == EditMode.SWAP && modifyingQuestSet != null && modifyingQuestSet != selectedSet) {
                                        quest.setQuestSet(modifyingQuestSet);
                                        SaveHelper.add(SaveHelper.EditType.QUEST_CHANGE_SET);
                                    }else if(currentMode == EditMode.REPEATABLE) {
                                        editMenu = new GuiEditMenuRepeat(this, player, quest);
                                    }else if(currentMode == EditMode.TRIGGER) {
                                        editMenu = new GuiEditMenuTrigger(this, player, quest);
                                    }else if(currentMode == EditMode.REQUIRED_PARENTS) {
                                        editMenu = new GuiEditMenuParentCount(this, player, quest);
                                    }else if(currentMode == EditMode.QUEST_SELECTION) {
                                        Quest.selectedQuestId = quest.getId();
                                    }else if(currentMode == EditMode.QUEST_OPTION) {
                                        if (modifyingQuest == quest) {
                                            if (GuiScreen.isShiftKeyDown()) {
                                                modifyingQuest.clearOptionLinks();
                                            }
                                            modifyingQuest = null;
                                        }else if(modifyingQuest == null) {
                                            modifyingQuest = quest;
                                        }else{
                                            modifyingQuest.addOptionLink(quest.getId());
                                        }
                                    }
                                }else if (quest.isEnabled(player) || Quest.isEditing) {
                                    if (isOpBook && GuiScreen.isShiftKeyDown()) {
                                        OPBookHelper.reverseQuestCompletion(quest, player);
                                    }else{
                                        selectedQuest = quest;
                                        quest.onOpen(this, player);
                                    }
                                }
                                break;
                            }
                        }
                    }


                }else{
                    selectedQuest.onClick(this, player, x, y, button);
                }
            }


        }else{
            editMenu.onClick(this, x, y, button);
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
        }
        if (editMenu != null) {
            editMenu.onRelease(this, x, y);
        }else if (selectedQuest != null) {
            selectedQuest.onRelease(this, player, x, y, button);
        }else{
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
        }else if (selectedQuest != null) {
            selectedQuest.onDrag(this, player, x, y, button);
        }else{
            for (ScrollBar scrollBar : scrollBars) {
                scrollBar.onDrag(this, x, y);
            }
        }
    }

    private Group modifyingGroup;
    private QuestSet modifyingQuestSet;
    private Quest modifyingQuest;
    private void updatePosition(int x, int y) {
        if (modifyingQuest != null && currentMode == EditMode.MOVE && Quest.isEditing) {
            modifyingQuest.setGuiCenterX(x);
            modifyingQuest.setGuiCenterY(y);
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

    public Quest getSelectedQuest() {
        return selectedQuest;
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

    public enum EditMode {
        NORMAL("Cursor", "Use the book as if you would be in play mode."),
        MOVE("Move", "Click and drag to move quests"),
        CREATE("Create", "Click to create quests, quest sets, and reward groups, among other things."),
        REQUIREMENT("Quest Requirements", "Click on a quest to select it and then click on the quests you want as requirements for the selected quest.\n\nHold shift and click on the selected quest to remove its requirements."),
        SIZE("Change Size", "Click on a quest to change its size, this is purely visual and has no impact on how the quest works."),
        RENAME("Edit Text", "Click on the text you want to edit. Works with names and descriptions for quest sets, quests, tasks, main lore, reward groups and reward tiers."),
        ITEM("Change Item", "Click on an item or item slot to open the item selector. This works for quest rewards and task items to give some examples."),
        TASK("Create Task", "Opens up the task creation menu."),
        DELETE("Delete", "Be careful with this one, things you click on will be deleted. Works with quest sets, quests, tasks, rewards, task items and just about everything."),
        SWAP("Change Set", "Click on a quest to move it to another set. Before using this you will have to use the \"Target Set\" command to select a target set."),
        SWAP_SELECT("Target Set", "Mark a set as the target for quest movement. The \"Change Set\" command can then be used to move quests to this set"),
        TIER("Set group tier", "Selected a group and then click on a tier to set the group's tier."),
        BAG("Reward Bags", "Open up the reward bag menu. Here you will be able to modify the group tiers and add groups of items for the reward bags."),
        LOCATION("Edit Location", "Edit the target location for location tasks."),
        REPEATABLE("Set Repeatability", "Change if a quest should be repeatable or not, and if so, the properties of the repeatability."),
        TRIGGER("Trigger Quests", "Specify any properties for trigger quests."),
        MOB("Edit Monster", "Edit the monster target for killing tasks."),
        QUEST_SELECTION("Select Quest", "Mark a quest as the selected quest. When a quest is selected you can bind it to a Quest Tracker System or a Quest Gate System by right clicking it with a book."),
        QUEST_OPTION("Quest Option", "Click on a quest to select it and then click on the quests you want to link it to. If an option linked quest is completed all quests it's linked to becomes invisible and uncompletable. \n\nHold shift and click on the selected quest to remove all its links."),
        CHANGE_TASK("Change Task", "Change the task type of item tasks."),
        REQUIRED_PARENTS("Required parents", "Change how many of the parent quests that have to be completed before this one unlocks."),
        REPUTATION("Reputation", "Open the reputation menu where you can create reputations and their tiers."),
        REPUTATION_VALUE("Change value", "Change the value of the different reputation tiers"),
        REPUTATION_TASK("Edit reputation target", "Change the configurations for a reputation target for the selected reputation task"),
        REPUTATION_REWARD("Edit reputation reward", "Click on the reputation reward icon to bring up the reputation reward menu.");

        private String name;
        private String description;

        EditMode(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }
    private static final int BUTTON_SIZE = 16;
    private static final int BUTTON_ICON_SIZE = 12;
    private static final int BUTTON_ICON_SRC_X = 0;
    private static final int BUTTON_ICON_SRC_Y = 0;
    private static final int EDIT_BUTTONS_PER_ROW = 2;
    private static final int EDIT_BUTTONS_SRC_PER_ROW = 8;

    private EditButton[] getButtons() {
        return isMainPageOpen ? mainButtons : isMenuPageOpen ? menuButtons : isReputationPage ? reputationButtons : isBagPage ? selectedGroup != null ? groupButtons : bagButtons : selectedSet == null || !isSetOpened ? overviewButtons : selectedQuest == null ? setButtons : questButtons;
    }

    private EditButton[] groupButtons = createButtons(EditMode.NORMAL, EditMode.ITEM, EditMode.DELETE);
    private EditButton[] bagButtons = createButtons(EditMode.NORMAL, EditMode.CREATE, EditMode.RENAME, EditMode.TIER, EditMode.DELETE);
    private EditButton[] reputationButtons = createButtons(EditMode.NORMAL, EditMode.CREATE, EditMode.RENAME, EditMode.REPUTATION_VALUE, EditMode.DELETE);
    private EditButton[] mainButtons = createButtons(EditMode.NORMAL, EditMode.RENAME);
    private EditButton[] menuButtons = createButtons(EditMode.NORMAL, EditMode.BAG, EditMode.REPUTATION);
    private EditButton[] overviewButtons = createButtons(EditMode.NORMAL, EditMode.CREATE, EditMode.RENAME, EditMode.SWAP_SELECT, EditMode.DELETE);
    private EditButton[] setButtons = createButtons(EditMode.NORMAL, EditMode.MOVE, EditMode.CREATE, EditMode.REQUIREMENT, EditMode.SIZE, EditMode.ITEM, EditMode.REPEATABLE, EditMode.TRIGGER, EditMode.REQUIRED_PARENTS, EditMode.QUEST_SELECTION, EditMode.QUEST_OPTION, EditMode.SWAP, EditMode.DELETE);
    private EditButton[] questButtons = createButtons(EditMode.NORMAL, EditMode.RENAME, EditMode.TASK, EditMode.CHANGE_TASK, EditMode.ITEM, EditMode.LOCATION, EditMode.MOB, EditMode.REPUTATION_TASK, EditMode.REPUTATION_REWARD, EditMode.DELETE);

    private EditButton[] createButtons(EditMode... modes) {
        EditButton[] ret = new EditButton[modes.length];
        for (int i = 0; i < modes.length; i++) {
            EditMode mode = modes[i];
            ret[i] = new EditButton(mode, i);
        }
        return ret;
    }
    public class EditButton {
        private int x;
        private int y;
        private  EditMode mode;
        private List<String> text;

        public EditButton(EditMode mode, int id) {
            this.mode = mode;

            int x = id % EDIT_BUTTONS_PER_ROW;
            int y = id / EDIT_BUTTONS_PER_ROW;

            this.x = -38 + x * 20;
            this.y = 5 + y * 20;
        }



        private void draw(int mX, int mY) {
            int srcY = currentMode == mode ? 2 : inBounds(x, y, BUTTON_SIZE, BUTTON_SIZE, mX, mY) ? 1 : 0;
            drawRect(x, y, 256 - BUTTON_SIZE, srcY * BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE);
            drawRect(x + 2, y + 2, BUTTON_ICON_SRC_X + (mode.ordinal() % EDIT_BUTTONS_SRC_PER_ROW) * BUTTON_ICON_SIZE, BUTTON_ICON_SRC_Y + (mode.ordinal() / EDIT_BUTTONS_SRC_PER_ROW) * BUTTON_ICON_SIZE, BUTTON_ICON_SIZE, BUTTON_ICON_SIZE);
        }

        private void drawInfo(int mX, int mY) {
            if (inBounds(x, y, BUTTON_SIZE, BUTTON_SIZE, mX, mY)) {
                if (text == null) {
                    text = getLinesFromText(mode.getName() + "\n\n" + mode.getDescription(), 1F, 150);
                    for (int i = 1; i < text.size(); i++) {
                        text.set(i, GuiColor.GRAY + text.get(i));
                    }
                }

                drawMouseOver(text, mX + left, mY + top);
            }
        }

        private boolean onClick(int mX, int mY) {
            if (inBounds(x, y, BUTTON_SIZE, BUTTON_SIZE, mX, mY))    {
                currentMode = mode;
                modifyingQuest = null;
                return true;
            }

            return false;
        }
    }


    private boolean shouldDisplayControlArrow(boolean isMenuArrow) {
        return !isMainPageOpen && ((!(isMenuArrow && isMenuPageOpen) && editMenu == null) || (editMenu != null && !editMenu.hasButtons()));
    }

    private boolean inArrowBounds(boolean isMenuArrow, int mX, int mY) {
        if (isMenuArrow) {
            return inBounds(MENU_ARROW_X, MENU_ARROW_Y, MENU_ARROW_WIDTH, MENU_ARROW_HEIGHT, mX, mY);
        }else{
            return inBounds(BACK_ARROW_X, BACK_ARROW_Y, BACK_ARROW_WIDTH, BACK_ARROW_HEIGHT, mX, mY);
        }
    }

    private boolean shouldDisplayAndIsInArrowBounds(boolean isMenuArrow, int mX, int mY) {
        return shouldDisplayControlArrow(isMenuArrow) && inArrowBounds(isMenuArrow, mX, mY);
    }
}
