package hardcorequesting.common.client.interfaces.edit;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiColor;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.ResourceHelper;
import hardcorequesting.common.client.interfaces.widget.LargeButton;
import hardcorequesting.common.client.interfaces.widget.ScrollBar;
import hardcorequesting.common.client.interfaces.widget.TextBoxGroup;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.team.PlayerEntry;
import hardcorequesting.common.team.Team;
import hardcorequesting.common.team.TeamError;
import hardcorequesting.common.util.Translator;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.FormattedText;

import java.util.List;
import java.util.UUID;

public class GuiEditMenuTeam extends GuiEditMenu {
    
    private static final int TEXT_HEIGHT = 9;
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
    private GuiEditMenuTeam self = this;
    private ScrollBar memberScroll;
    private ScrollBar inviteScroll;
    private LargeButton inviteButton;
    private Team inviteTeam;
    private TextBoxGroup.TextBox teamName;
    private TextBoxGroup.TextBox inviteName;
    private PlayerEntry selectedEntry;
    
    public GuiEditMenuTeam(GuiQuestBook gui, UUID playerId) {
        super(gui, playerId);
        
        addButton(new LargeButton(gui, "hqm.party.create", 250, 20) {
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
        
        addButton(inviteButton = new LargeButton(gui, "hqm.party.invitePlayer", 250, 20) {
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
        
        addButton(new LargeButton(gui, "hqm.party.accept", 180, 20) {
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
        
        addButton(new LargeButton(gui, "hqm.party.decline", 240, 20) {
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
        
        addButton(new LargeButton(gui, "hqm.party.decideLater", 180, 40) {
            @Override
            public boolean isVisible() {
                return inviteTeam != null;
            }
            
            @Override
            public void onClick() {
                inviteTeam = null;
            }
        });
        
        addButton(new LargeButton(gui, null, 250, 50) {
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
        
        addButton(new LargeButton(gui, "hqm.party.leave", 250, 160) {
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
        
        addButton(new LargeButton(gui, "hqm.party.disband", 250, 160) {
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
        
        addButton(new LargeButton(gui, "hqm.party.list", 250, 190) {
            @Override
            public void onClick() {
                gui.setEditMenu(new GuiEditMenuTeamList(gui, playerId, self));
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
        
        addScrollBar(inviteScroll = new ScrollBar(gui, 155, 22, 186, 171, 69, PLAYER_X) {
            @Override
            public boolean isVisible() {
                return inviteTeam == null && getTeam().isSingle() && getTeam().getInvites() != null && getTeam().getInvites().size() > VISIBLE_INVITES;
            }
        });
        
        addScrollBar(memberScroll = new ScrollBar(gui, 155, 22, 186, 171, 69, PLAYER_X) {
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
                if (team.getInvites() == null) {
                    inviteTeam = null;
                } else {
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
            }
            Team.reloadedInvites = false;
        }
        
        super.draw(matrices, mX, mY);
        
        ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
        
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        
        if (team.isSingle() && inviteTeam == null) {
            int inviteCount = team.getInvites() == null ? 0 : team.getInvites().size();
            if (inviteCount > 0) {
                gui.drawString(matrices, Translator.translatable("hqm.party.invites"), TITLE_X, TITLE_Y, 0x404040);
                List<Team> invites = team.getInvites();
                int start = inviteScroll.isVisible() ? Math.round((team.getInvites().size() - VISIBLE_INVITES) * inviteScroll.getScroll()) : 0;
                int end = Math.min(invites.size(), start + VISIBLE_INVITES);
                for (int i = start; i < end; i++) {
                    Team invite = invites.get(i);
                    gui.drawString(matrices, Translator.plain(invite.getName()), PLAYER_X, PLAYER_Y + PLAYER_SPACING * (i - start), 0x404040);
                }
            } else {
                gui.drawString(matrices, Translator.translatable("hqm.party.noInvites"), TITLE_X, TITLE_Y, 0x404040);
            }
            
            gui.drawString(matrices, Translator.translatable("hqm.party.name"), 180, 20, 0.7F, 0x404040);
        } else {
            boolean isOwner = inviteTeam == null && entry.isOwner();
            String title = (inviteTeam == null ? team : inviteTeam).getName();
            gui.drawString(matrices, Translator.plain(title), TITLE_X, TITLE_Y, 0x404040);
            List<PlayerEntry> players = (inviteTeam == null ? team : inviteTeam).getPlayers();
            int y = 0;
            int start = memberScroll.isVisible() ? Math.round(((isOwner ? players.size() : (inviteTeam == null ? team : inviteTeam).getPlayerCount()) - VISIBLE_MEMBERS) * memberScroll.getScroll()) : 0;
            
            for (PlayerEntry player : players) {
                String str = player.getDisplayName();
                
                if (player.isOwner()) {
                    str += GuiColor.ORANGE + " [" + I18n.get("hqm.party.owner") + "]";
                } else if (!player.isInTeam()) {
                    if (isOwner) {
                        str += GuiColor.LIGHT_GRAY + " [" + I18n.get("hqm.party.invite") + "]";
                    } else {
                        continue;
                    }
                }
                
                if (y >= start) {
                    int color = 0x404040;
                    if (isOwner) {
                        if (player.equals(selectedEntry)) {
                            color = 0xD0D0D0;
                        } else if (gui.inBounds(PLAYER_X, PLAYER_Y + PLAYER_SPACING * (y - start), (int) (gui.getStringWidth(player.getDisplayName()) * 0.7F), (int) (TEXT_HEIGHT * 0.7F), mX, mY)) {
                            color = 0x808080;
                        }
                    }
                    gui.drawString(matrices, Translator.plain(str), PLAYER_X, PLAYER_Y + PLAYER_SPACING * (y - start), 0.7F, color);
                }
                y++;
                if (y == start + VISIBLE_MEMBERS) {
                    break;
                }
            }
            
            if (inviteTeam == null) {
                if (entry.isOwner()) {
                    gui.drawString(matrices, Translator.translatable("hqm.party.playerName"), 180, 20, 0.7F, 0x404040);
                    
                    if (selectedEntry != null) {
                        gui.drawString(matrices, gui.getLinesFromText(Translator.translatable("hqm.party.currentSelection", selectedEntry.getDisplayName()), 0.7F, 70), 177, 52, 0.7F, 0x404040);
                        
                        if (selectedEntry.isOwner()) {
                            gui.drawString(matrices, gui.getLinesFromText(Translator.translatable("hqm.party.shiftCtrlConfirm"), 0.6F, 70), 177, 162, 0.6F, GuiColor.RED.getHexColor());
                        }
                    }
                    
                } else {
                    gui.drawString(matrices, gui.getLinesFromText(Translator.translatable("hqm.party.shiftConfirm"), 0.7F, 70), 177, 162, 0.7F, GuiColor.RED.getHexColor());
                }
            }
            gui.drawString(matrices, gui.getLinesFromText(Translator.translatable("hqm.party.stats"), 0.7F, 70), 177, 192, 0.7F, 0x404040);
            
            Team infoTeam = inviteTeam == null ? team : inviteTeam;
            
            int infoY = getInfoY();
            
            
            ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
            
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            gui.drawRect(matrices, INFO_BOX_X, infoY, INFO_BOX_SRC_X, INFO_BOX_SRC_Y, INFO_BOX_SIZE, INFO_BOX_SIZE);
            gui.drawRect(matrices, INFO_BOX_X, infoY + REWARD_SETTING_Y, INFO_BOX_SRC_X, INFO_BOX_SRC_Y, INFO_BOX_SIZE, INFO_BOX_SIZE);
            
            gui.drawString(matrices, Translator.translatable("hqm.party.lifeSetting", infoTeam.getLifeSetting().getTitle()), INFO_BOX_X + INFO_BOX_TEXT_OFFSET_X, infoY + INFO_BOX_TEXT_OFFSET_Y, 0.7F, 0x404040);
            gui.drawString(matrices, Translator.translatable("hqm.party.rewardSetting", infoTeam.getRewardSetting().getTitle()), INFO_BOX_X + INFO_BOX_TEXT_OFFSET_X, infoY + REWARD_SETTING_Y + INFO_BOX_TEXT_OFFSET_Y, 0.7F, 0x404040);
            
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
                gui.renderTooltipL(matrices, gui.getLinesFromText(Translator.plain(GuiColor.GREEN + infoTeam.getLifeSetting().getTitle() + "\n" + infoTeam.getLifeSetting().getDescription() + (isOwner ? "\n\n" + GuiColor.ORANGE + I18n.get("hqm.party.change") : "")), 1F, 200), gui.getLeft() + mX, gui.getTop() + mY);
            } else if (gui.inBounds(INFO_BOX_X, infoY + REWARD_SETTING_Y, INFO_BOX_SIZE, INFO_BOX_SIZE, mX, mY)) {
                gui.renderTooltipL(matrices, gui.getLinesFromText(Translator.plain(GuiColor.GREEN + infoTeam.getRewardSetting().getTitle() + "\n" + infoTeam.getRewardSetting().getDescription() + (isOwner ? "\n\n" + GuiColor.ORANGE + I18n.get("hqm.party.change") : "")), 1F, 200), gui.getLeft() + mX, gui.getTop() + mY);
            }
        }
        
        if (TeamError.latestError != null) {
            if (inviteButton.inButtonBounds(mX, mY)) {
                gui.renderTooltipL(matrices, gui.getLinesFromText(Translator.plain(GuiColor.RED + TeamError.latestError.getHeader() + "\n" + TeamError.latestError.getMessage()), 1F, 150), mX + gui.getLeft(), mY + gui.getTop());
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
            List<Team> invites = team.getInvites();
            if (invites != null) {
                int start = inviteScroll.isVisible() ? Math.round((team.getInvites().size() - VISIBLE_INVITES) * inviteScroll.getScroll()) : 0;
                int end = Math.min(invites.size(), start + VISIBLE_INVITES);
                for (int i = start; i < end; i++) {
                    Team invite = invites.get(i);
                    if (gui.inBounds(PLAYER_X, PLAYER_Y + PLAYER_SPACING * i, (int) (gui.getStringWidth(invite.getName()) * 0.7F), (int) (TEXT_HEIGHT * 0.7F), mX, mY)) {
                        inviteTeam = invite;
                        break;
                    }
                }
            }
        } else if (!team.isSingle() && getEntry(team).isOwner()) {
            int start = memberScroll.isVisible() ? Math.round((team.getPlayers().size() - VISIBLE_MEMBERS) * memberScroll.getScroll()) : 0;
            int end = Math.min(team.getPlayers().size(), start + VISIBLE_MEMBERS);
            for (int i = start; i < end; i++) {
                PlayerEntry entry = team.getPlayers().get(i);
                if (gui.inBounds(PLAYER_X, PLAYER_Y + PLAYER_SPACING * (i - start), (int) (gui.getStringWidth(entry.getDisplayName()) * 0.7F), (int) (TEXT_HEIGHT * 0.7F), mX, mY)) {
                    selectedEntry = selectedEntry == entry ? null : entry;
                    break;
                }
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
