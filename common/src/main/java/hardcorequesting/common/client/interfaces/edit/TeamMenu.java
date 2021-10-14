package hardcorequesting.common.client.interfaces.edit;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.ResourceHelper;
import hardcorequesting.common.client.interfaces.widget.ExtendedScrollBar;
import hardcorequesting.common.client.interfaces.widget.LargeButton;
import hardcorequesting.common.client.interfaces.widget.ScrollBar;
import hardcorequesting.common.client.interfaces.widget.TextBoxGroup;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.team.PlayerEntry;
import hardcorequesting.common.team.Team;
import hardcorequesting.common.team.TeamError;
import hardcorequesting.common.util.Translator;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.FormattedText;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TeamMenu extends GuiEditMenu {
    
    private static final int TITLE_X = 20;
    private static final int TITLE_Y = 20;
    private static final int PLAYER_X = 25;
    private static final int PLAYER_Y = 32;
    private static final int PLAYER_SPACING = 11;
    private static final int VISIBLE_INVITES = 16;
    private static final int VISIBLE_MEMBERS = 16;
    private static final int INFO_BOX_X = 180;
    private static final int INFO_BOX_SIZE = 16;
    private static final int INFO_BOX_TEXT_OFFSET_X = 20;
    private static final int INFO_BOX_TEXT_OFFSET_Y = 6;
    private static final int INFO_BOX_SRC_X = 240;
    private static final int INFO_BOX_SRC_Y = 224;
    private static final int REWARD_SETTING_Y = 20;
    
    private final ScrollBar memberScroll;
    private final ExtendedScrollBar<Team> inviteScroll;
    private final LargeButton inviteButton;
    private final TextBoxGroup.TextBox teamName;
    private final TextBoxGroup.TextBox inviteName;
    
    private Team inviteTeam;
    private PlayerEntry selectedEntry;
    
    public TeamMenu(GuiQuestBook gui, UUID playerId) {
        super(gui, playerId);
        
        addClickable(new LargeButton(gui, "hqm.party.create", 250, 20) {
            @Override
            public boolean isEnabled() {
                return teamName.getText().length() > 0;
            }
            
            @Override
            public boolean isVisible() {
                return inviteTeam == null && getTeam().isSingle();
            }
            
            @Override
            public void onClick() {
                getTeam().create(teamName.getText());
            }
        });
        
        addClickable(inviteButton = new LargeButton(gui, "hqm.party.invitePlayer", 250, 20) {
            @Override
            public boolean isEnabled() {
                return inviteName.getText().length() > 0;
            }
            
            @Override
            public boolean isVisible() {
                return !getTeam().isSingle() && getEntry(getTeam()).isOwner();
            }
            
            @Override
            public void onClick() {
                getTeam().invite(inviteName.getText());
            }
        });
        
        addClickable(new LargeButton(gui, "hqm.party.accept", 180, 20) {
            @Override
            public boolean isVisible() {
                return inviteTeam != null;
            }
            
            @Override
            public void onClick() {
                inviteTeam.accept();
                inviteTeam = null;
            }
        });
        
        addClickable(new LargeButton(gui, "hqm.party.decline", 240, 20) {
            @Override
            public boolean isVisible() {
                return inviteTeam != null;
            }
            
            @Override
            public void onClick() {
                inviteTeam.decline();
                inviteTeam = null;
            }
        });
        
        addClickable(new LargeButton(gui, "hqm.party.decideLater", 180, 40) {
            @Override
            public boolean isVisible() {
                return inviteTeam != null;
            }
            
            @Override
            public void onClick() {
                inviteTeam = null;
            }
        });
        
        addClickable(new LargeButton(gui, null, 250, 50) {
            @Override
            public boolean isEnabled() {
                return !selectedEntry.isOwner();
            }
            
            @Override
            public boolean isVisible() {
                return selectedEntry != null && getEntry(getTeam()).isOwner();
            }
            
            @Override
            public void onClick() {
                getTeam().kick(selectedEntry.getUUID());
                selectedEntry = null;
            }
            
            @Override
            protected FormattedText getName() {
                return Translator.translatable(selectedEntry.isInTeam() ? "hqm.party.kickPlayer" : "hqm.party.removeInvite");
            }
        });
        
        addClickable(new LargeButton(gui, "hqm.party.leave", 250, 160) {
            @Override
            public boolean isEnabled() {
                return Screen.hasShiftDown();
            }
            
            @Override
            public boolean isVisible() {
                return !getTeam().isSingle() && !getEntry(getTeam()).isOwner();
            }
            
            @Override
            public void onClick() {
                getTeam().leave();
            }
        });
        
        addClickable(new LargeButton(gui, "hqm.party.disband", 250, 160) {
            @Override
            public boolean isEnabled() {
                return Screen.hasShiftDown() && Screen.hasControlDown();
            }
            
            @Override
            public boolean isVisible() {
                return !getTeam().isSingle() && selectedEntry != null && selectedEntry.isOwner();
            }
            
            @Override
            public void onClick() {
                getTeam().disband();
                selectedEntry = null;
            }
        });
        
        addClickable(new LargeButton(gui, "hqm.party.list", 250, 190) {
            @Override
            public void onClick() {
                gui.setEditMenu(new TeamListMenu(gui, playerId, TeamMenu.this));
            }
        });
        
        addTextBox(teamName = new TextBoxName(gui, "", 180, 26) {
            @Override
            protected boolean isVisible() {
                return getTeam().isSingle() && inviteTeam == null;
            }
        });
        teamName.setWidth((int) ((GuiQuestBook.PAGE_WIDTH - TITLE_X - 10) * 0.7F));
        
        addTextBox(inviteName = new TextBoxName(gui, "", 180, 26) {
            @Override
            protected boolean isVisible() {
                return !getTeam().isSingle() && getEntry(getTeam()).isOwner();
            }
        });
        
        addScrollBar(inviteScroll = new ExtendedScrollBar<>(gui, ScrollBar.Size.LONG, 155, 22, PLAYER_X, VISIBLE_INVITES, () -> getTeam().getInvites()) {
            @Override
            public boolean isVisible() {
                return inviteTeam == null && getTeam().isSingle() && super.isVisible();
            }
        });
        
        addScrollBar(memberScroll = new ScrollBar(gui, ScrollBar.Size.LONG, 155, 22, PLAYER_X) {
            @Override
            public boolean isVisible() {
                return (inviteTeam != null && inviteTeam.getPlayers().size() > VISIBLE_MEMBERS) || (!getTeam().isSingle() && getTeam().getPlayers().size() > VISIBLE_MEMBERS);
            }
        });
    }
    
    @Override
    public void draw(PoseStack matrices, int mX, int mY) {
        Team team = getTeam();
        PlayerEntry entry = getEntry(team);
        
        if (Team.reloadedInvites) {
            if (inviteTeam != null) {
                boolean stillThere = false;
                for (Team t : team.getInvites()) {
                    if (t.getName().equals(inviteTeam.getName())) {
                        inviteTeam = t;
                        stillThere = true;
                    }
                }
    
                if (!stillThere) {
                    inviteTeam = null;
                }
            }
            Team.reloadedInvites = false;
        }
        
        super.draw(matrices, mX, mY);
        
        ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
        
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        
        if (team.isSingle() && inviteTeam == null) {
            if (!team.getInvites().isEmpty()) {
                gui.drawString(matrices, Translator.translatable("hqm.party.invites"), TITLE_X, TITLE_Y, 0x404040);
                
                int inviteY = PLAYER_Y;
                for (Team invite : inviteScroll.getVisibleEntries()) {
                    gui.drawString(matrices, Translator.plain(invite.getName()), PLAYER_X, inviteY, 0x404040);
                    inviteY += PLAYER_SPACING;
                }
            } else {
                gui.drawString(matrices, Translator.translatable("hqm.party.noInvites"), TITLE_X, TITLE_Y, 0x404040);
            }
            
            gui.drawString(matrices, Translator.translatable("hqm.party.name"), 180, 20, 0.7F, 0x404040);
        } else {
            boolean isOwner = inviteTeam == null && entry.isOwner();
            Team shownTeam = inviteTeam == null ? team : inviteTeam;
            String title = shownTeam.getName();
            gui.drawString(matrices, Translator.plain(title), TITLE_X, TITLE_Y, 0x404040);
            List<PlayerEntry> players = isOwner ? shownTeam.getPlayers() : shownTeam.getTeamMembers();
            
            int memberY = PLAYER_Y;
            for (PlayerEntry player : memberScroll.getVisibleEntries(players, VISIBLE_MEMBERS)) {
                String str = player.getDisplayName();
                if (player.isOwner()) {
                    str += ChatFormatting.GOLD + " [" + I18n.get("hqm.party.owner") + "]";
                } else if (!player.isInTeam()) {
                    str += ChatFormatting.GRAY + " [" + I18n.get("hqm.party.invite") + "]";
                }
                
                int color = 0x404040;
                if (isOwner) {
                    if (player.equals(selectedEntry)) {
                        color = 0xD0D0D0;
                    } else if (gui.inBounds(PLAYER_X, memberY, (int) (gui.getStringWidth(player.getDisplayName()) * 0.7F), (int) (GuiBase.TEXT_HEIGHT * 0.7F), mX, mY)) {
                        color = 0x808080;
                    }
                }
                gui.drawString(matrices, Translator.plain(str), PLAYER_X, memberY, 0.7F, color);
                memberY += PLAYER_SPACING;
            }
            
            if (inviteTeam == null) {
                if (entry.isOwner()) {
                    gui.drawString(matrices, Translator.translatable("hqm.party.playerName"), 180, 20, 0.7F, 0x404040);
                    
                    if (selectedEntry != null) {
                        gui.drawString(matrices, gui.getLinesFromText(Translator.translatable("hqm.party.currentSelection", selectedEntry.getDisplayName()), 0.7F, 70), 177, 52, 0.7F, 0x404040);
                        
                        if (selectedEntry.isOwner()) {
                            gui.drawString(matrices, gui.getLinesFromText(Translator.translatable("hqm.party.shiftCtrlConfirm"), 0.6F, 70), 177, 162, 0.6F, 0xff5555);
                        }
                    }
                    
                } else {
                    gui.drawString(matrices, gui.getLinesFromText(Translator.translatable("hqm.party.shiftConfirm"), 0.7F, 70), 177, 162, 0.7F, 0xff5555);
                }
            }
            gui.drawString(matrices, gui.getLinesFromText(Translator.translatable("hqm.party.stats"), 0.7F, 70), 177, 192, 0.7F, 0x404040);
    
            int infoY = getInfoY();
            
            ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
            
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            gui.drawRect(matrices, INFO_BOX_X, infoY, INFO_BOX_SRC_X, INFO_BOX_SRC_Y, INFO_BOX_SIZE, INFO_BOX_SIZE);
            gui.drawRect(matrices, INFO_BOX_X, infoY + REWARD_SETTING_Y, INFO_BOX_SRC_X, INFO_BOX_SRC_Y, INFO_BOX_SIZE, INFO_BOX_SIZE);
            
            gui.drawString(matrices, Translator.translatable("hqm.party.lifeSetting", shownTeam.getLifeSetting().getTitle()), INFO_BOX_X + INFO_BOX_TEXT_OFFSET_X, infoY + INFO_BOX_TEXT_OFFSET_Y, 0.7F, 0x404040);
            gui.drawString(matrices, Translator.translatable("hqm.party.rewardSetting", shownTeam.getRewardSetting().getTitle()), INFO_BOX_X + INFO_BOX_TEXT_OFFSET_X, infoY + REWARD_SETTING_Y + INFO_BOX_TEXT_OFFSET_Y, 0.7F, 0x404040);
            
        }
        
        
    }
    
    @Override
    public void drawTooltip(PoseStack matrices, int mX, int mY) {
        super.drawTooltip(matrices, mX, mY);
        
        Team team = getTeam();
        PlayerEntry entry = getEntry(team);
        boolean isOwner = inviteTeam == null && entry.isOwner();
        
        if (!team.isSingle() || inviteTeam != null) {
            int infoY = getInfoY();
            Team infoTeam = inviteTeam == null ? team : inviteTeam;
            if (gui.inBounds(INFO_BOX_X, infoY, INFO_BOX_SIZE, INFO_BOX_SIZE, mX, mY)) {
                
                List<FormattedText> tooltip = new ArrayList<>();
                tooltip.add(infoTeam.getLifeSetting().getTitle());
                tooltip.addAll(gui.getLinesFromText(infoTeam.getLifeSetting().getDescription(), 1F, 200));
                if (isOwner) {
                    tooltip.add(FormattedText.EMPTY);
                    tooltip.add(Translator.translatable("hqm.party.change").withStyle(ChatFormatting.GOLD));
                }
                gui.renderTooltipL(matrices, tooltip, gui.getLeft() + mX, gui.getTop() + mY);
            } else if (gui.inBounds(INFO_BOX_X, infoY + REWARD_SETTING_Y, INFO_BOX_SIZE, INFO_BOX_SIZE, mX, mY)) {
                
                List<FormattedText> tooltip = new ArrayList<>();
                tooltip.add(infoTeam.getRewardSetting().getTitle());
                tooltip.addAll(gui.getLinesFromText(infoTeam.getRewardSetting().getDescription(), 1F, 200));
                if (isOwner) {
                    tooltip.add(FormattedText.EMPTY);
                    tooltip.add(Translator.translatable("hqm.party.change").withStyle(ChatFormatting.GOLD));
                }
                
                gui.renderTooltipL(matrices, tooltip, gui.getLeft() + mX, gui.getTop() + mY);
            }
        }
        
        if (TeamError.latestError != null) {
            if (inviteButton.inButtonBounds(mX, mY)) {
                List<FormattedText> tooltip = new ArrayList<>();
                tooltip.add(TeamError.latestError.getHeader());
                tooltip.addAll(gui.getLinesFromText(TeamError.latestError.getMessage(), 1F, 150));
                gui.renderTooltipL(matrices, tooltip, mX + gui.getLeft(), mY + gui.getTop());
            } else {
                TeamError.latestError = null;
            }
        }
    }
    
    @Override
    public void onClick(int mX, int mY, int b) {
        super.onClick(mX, mY, b);
        
        
        Team team = getTeam();
        if (team.isSingle() && inviteTeam == null) {
            int inviteY = PLAYER_Y;
            for (Team invite : inviteScroll.getVisibleEntries()) {
                if (gui.inBounds(PLAYER_X, inviteY, (int) (gui.getStringWidth(invite.getName()) * 0.7F), (int) (GuiBase.TEXT_HEIGHT * 0.7F), mX, mY)) {
                    inviteTeam = invite;
                    break;
                }
                inviteY += PLAYER_SPACING;
            }
        } else if (!team.isSingle() && getEntry(team).isOwner()) {
            int memberY = PLAYER_Y;
            for (PlayerEntry entry : memberScroll.getVisibleEntries(team.getPlayers(), VISIBLE_MEMBERS)) {
                if (gui.inBounds(PLAYER_X, memberY, (int) (gui.getStringWidth(entry.getDisplayName()) * 0.7F), (int) (GuiBase.TEXT_HEIGHT * 0.7F), mX, mY)) {
                    selectedEntry = selectedEntry == entry ? null : entry;
                    break;
                }
                memberY += PLAYER_SPACING;
            }
            
            int infoY = getInfoY();
            
            if (gui.inBounds(INFO_BOX_X, infoY, INFO_BOX_SIZE, INFO_BOX_SIZE, mX, mY)) {
                team.nextLifeSetting();
            } else if (gui.inBounds(INFO_BOX_X, infoY + REWARD_SETTING_Y, INFO_BOX_SIZE, INFO_BOX_SIZE, mX, mY)) {
                team.nextRewardSetting();
            }
        }
    }
    
    @Override
    public void save() {
        
    }
    
    private int getInfoY() {
        if (inviteTeam != null) {
            return 80;
        } else if (getEntry(getTeam()).isOwner()) {
            return selectedEntry != null ? 80 : 50;
        } else {
            return 20;
        }
    }
    
    private Team getTeam() {
        return QuestingDataManager.getInstance().getQuestingData(playerId).getTeam();
    }
    
    private PlayerEntry getEntry(Team team) {
        return team.getEntry(this.playerId);
    }
    
    private static class TextBoxName extends TextBoxGroup.TextBox {
        public TextBoxName(GuiQuestBook gui, String str, int x, int y) {
            super(gui, str, x, y, true);
            setMult(0.7F);
            offsetY = 5;
        }
    }
    
    
}
