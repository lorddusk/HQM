package hardcorequesting.common.client.interfaces.graphic;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.BookPage;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiColor;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.ResourceHelper;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenuDeath;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenuTeam;
import hardcorequesting.common.client.interfaces.widget.ExtendedScrollBar;
import hardcorequesting.common.client.interfaces.widget.LargeButton;
import hardcorequesting.common.client.interfaces.widget.ScrollBar;
import hardcorequesting.common.config.HQMConfig;
import hardcorequesting.common.death.DeathStatsManager;
import hardcorequesting.common.items.ModItems;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.reputation.Reputation;
import hardcorequesting.common.reputation.ReputationManager;
import hardcorequesting.common.team.PlayerEntry;
import hardcorequesting.common.team.Team;
import hardcorequesting.common.util.OPBookHelper;
import hardcorequesting.common.util.Translator;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;

/**
 * A graphic element for displaying the primary menu in the quest book.
 * It shows some general info such as hearts, reputation bars, and can lead to various other menus.
 */
public class MenuPageGraphic extends EditableGraphic {
    private static final int VISIBLE_DISPLAY_REPUTATIONS = 4;
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
    private static final int REPUTATION_OFFSET_Y = 24;
    
    private final ExtendedScrollBar<Reputation> reputationDisplayScroll;
    {
        addButton(new LargeButton(gui, "Reset", 90, 190) {
            @Override
            public boolean isEnabled() {
                return Screen.hasControlDown() && Screen.hasShiftDown();
            }
        
            @Override
            public boolean isVisible() {
                return MenuPageGraphic.this.gui.isOpBook;
            }
        
            @Override
            public void onClick() {
                OPBookHelper.reset(MenuPageGraphic.this.gui.getPlayer().getUUID());
            }
        });
    }
    
    public MenuPageGraphic(GuiQuestBook gui) {
        super(gui, EditMode.NORMAL, EditMode.BAG, EditMode.REPUTATION);
        addScrollBar(reputationDisplayScroll = new ExtendedScrollBar<>(gui, ScrollBar.Size.NORMAL, 160, 125, INFO_LEFT_X,
                VISIBLE_DISPLAY_REPUTATIONS, () -> ReputationManager.getInstance().getReputationList()));
    }
    
    @Override
    public void draw(PoseStack matrices, int mX, int mY) {
        super.draw(matrices, mX, mY);
        
        Player player = gui.getPlayer();
        
        QuestingDataManager manager = QuestingDataManager.getInstance();
        gui.drawString(matrices, Translator.translatable("hqm.questBook.lives"), INFO_RIGHT_X, INFO_LIVES_Y, 0x404040);
        if (HQMConfig.getInstance().ENABLE_TEAMS)
            gui.drawString(matrices, Translator.translatable("hqm.questBook.party"), INFO_RIGHT_X, INFO_TEAM_Y, 0x404040);
        gui.drawString(matrices, Translator.translatable("hqm.questBook.quests"), INFO_LEFT_X, INFO_QUESTS_Y, 0x404040);
        gui.drawString(matrices, Translator.translatable("hqm.questBook.reputation"), INFO_LEFT_X, INFO_REPUTATION_Y, 0x404040);
        
        QuestSetsGraphic.drawQuestInfo(matrices, gui, null, INFO_LEFT_X, INFO_QUESTS_Y + (int) (GuiQuestBook.TEXT_HEIGHT * 1.5F));
        gui.drawString(matrices, Translator.translatable("hqm.questBook.showQuests"), INFO_LEFT_X, INFO_QUESTS_Y + QUEST_CLICK_TEXT_Y, 0.7F, 0x707070);
        
        if (manager.isHardcoreActive()) {
            boolean almostOut = manager.getQuestingData(player).getLives() == manager.getQuestingData(player).getLivesToStayAlive();
            if (almostOut) {
                gui.drawString(matrices, Translator.translatable("hqm.questBook.deadOut", GuiColor.RED), INFO_RIGHT_X + 50, INFO_LIVES_Y + 2, 0.7F, 0x404040);
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
                gui.drawString(matrices, Translator.plain(lives + " x"), INFO_RIGHT_X + 5, INFO_LIVES_Y + INFO_HEARTS_Y + 5, 0.7F, 0x404040);
            }
            
            for (int i = 0; i < count; i++) {
                gui.drawItemStack(new ItemStack(ModItems.heart.get(), 1), heartX + spacing * i, INFO_LIVES_Y + INFO_HEARTS_Y, almostOut);
            }
        } else {
            gui.drawString(matrices, gui.getLinesFromText(Translator.translatable("hqm.questBook.infiniteLives"), 0.5F, GuiQuestBook.PAGE_WIDTH - 30), INFO_RIGHT_X, INFO_LIVES_Y + 12, 0.5F, 0x707070);
        }
        
        
        int deaths = DeathStatsManager.getInstance().getDeathStat(player.getUUID()).getTotalDeaths();
        gui.drawString(matrices, Translator.pluralTranslated(deaths != 1, "hqm.questBook.deaths", deaths), INFO_RIGHT_X, INFO_DEATHS_Y + DEATH_TEXT_Y, 0.7F, 0x404040);
        gui.drawString(matrices, Translator.translatable("hqm.questBook.moreInfo"), INFO_RIGHT_X, INFO_DEATHS_Y + DEATH_CLICK_TEXT_Y, 0.7F, 0x707070);
        
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
            for (PlayerEntry teamPlayer : team.getPlayers()) {
                if (teamPlayer.isInTeam()) {
                    players++;
                }
            }
            str = Translator.pluralTranslated(players != 1, "hqm.questBook.inParty", players);
        }
        
        gui.drawString(matrices, str, INFO_RIGHT_X, INFO_TEAM_Y + TEAM_TEXT_Y, 0.7F, 0x404040);
        gui.drawString(matrices, Translator.translatable("hqm.questBook.openParty"), INFO_RIGHT_X, INFO_TEAM_Y + TEAM_CLICK_TEXT_Y, 0.7F, 0x707070);
        
        if (gui.isOpBook) {
            gui.drawString(matrices, Translator.translatable("hqm.questBook.resetParty"), 22, 182, 0.6F, 0x404040);
            gui.drawString(matrices, gui.getLinesFromText(Translator.translatable("hqm.questBook.shiftCtrlConfirm"), 0.6F, 70), 22, 192, 0.6F, GuiColor.RED.getHexColor());
        }
        
        drawReputations(matrices, gui, mX, mY, player.getUUID());
    }
    
    private void drawReputations(PoseStack matrices, GuiQuestBook gui, int mX, int mY, final UUID playerId) {
        String info = null;
        
        List<Reputation> reputations = ReputationManager.getInstance().getReputationList();
        
        reputations.sort((reputation1, reputation2) -> Integer.compare(Math.abs(reputation2.getValue(playerId)), Math.abs(reputation1.getValue(playerId))));
        
        int repY = INFO_REPUTATION_Y + INFO_REPUTATION_OFFSET_Y;
        
        for (Reputation reputation : reputationDisplayScroll.getVisibleEntries()) {
            gui.applyColor(0xFFFFFFFF);
            ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
            info = reputation.drawAndGetTooltip(matrices, gui, INFO_LEFT_X + INFO_REPUTATION_OFFSET_X, repY,
                    mX, mY, info, playerId, false, null, null, false, null, null, false);
            repY += REPUTATION_OFFSET_Y;
        }
        
        if (info != null) {
            gui.renderTooltip(matrices, Translator.plain(info), mX + gui.getLeft(), mY + gui.getTop());
        }
    }
    
    
    @Override
    public void onClick(int mX, int mY, int button) {
        super.onClick(mX, mY, button);
    
        if (HQMConfig.getInstance().ENABLE_TEAMS && gui.inBounds(INFO_RIGHT_X, INFO_TEAM_Y + TEAM_CLICK_TEXT_Y, GuiQuestBook.PAGE_WIDTH, (int) (GuiQuestBook.TEXT_HEIGHT * 0.7F), mX, mY)) {
            gui.setEditMenu(new GuiEditMenuTeam(gui, gui.getPlayer().getUUID()));
        } else if (gui.inBounds(INFO_RIGHT_X, INFO_DEATHS_Y + DEATH_CLICK_TEXT_Y, GuiQuestBook.PAGE_WIDTH, (int) (GuiQuestBook.TEXT_HEIGHT * 0.7F), mX, mY)) {
            gui.setEditMenu(new GuiEditMenuDeath(gui, gui.getPlayer().getUUID()));
        } else if (gui.inBounds(INFO_LEFT_X, INFO_QUESTS_Y + QUEST_CLICK_TEXT_Y, GuiQuestBook.PAGE_WIDTH, (int) (GuiQuestBook.TEXT_HEIGHT * 0.7F), mX, mY)) {
            gui.setPage(BookPage.SetsPage.INSTANCE);
        }
    }
    
    @Override
    protected void setEditMode(EditMode mode) {
        if (mode == EditMode.BAG) {
            gui.setPage(BookPage.BagsPage.INSTANCE);
        } else if (mode == EditMode.REPUTATION) {
            gui.setPage(BookPage.ReputationPage.INSTANCE);
        } else super.setEditMode(mode);
    }
}