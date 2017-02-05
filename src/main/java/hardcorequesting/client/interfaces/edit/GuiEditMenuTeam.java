package hardcorequesting.client.interfaces.edit;

import hardcorequesting.client.interfaces.*;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.team.PlayerEntry;
import hardcorequesting.team.Team;
import hardcorequesting.team.TeamError;
import hardcorequesting.util.Translator;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.List;

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
    private List<ScrollBar> scrollBars;
    private LargeButton inviteButton;
    private Team inviteTeam;
    private TextBoxGroup textBoxes;
    private TextBoxGroup.TextBox teamName;
    private TextBoxGroup.TextBox inviteName;
    private PlayerEntry selectedEntry;

    public GuiEditMenuTeam(GuiQuestBook gui, EntityPlayer player) {
        super(gui, player);

        buttons.add(new LargeButton("hqm.party.create", 250, 20) {
            @Override
            public boolean isEnabled(GuiBase gui, EntityPlayer player) {
                return teamName.getText().length() > 0;
            }

            @Override
            public boolean isVisible(GuiBase gui, EntityPlayer player) {
                return inviteTeam == null && getTeam().isSingle();
            }

            @Override
            public void onClick(GuiBase gui, EntityPlayer player) {
                getTeam().create(teamName.getText());
            }
        });

        buttons.add(inviteButton = new LargeButton("hqm.party.invitePlayer", 250, 20) {
            @Override
            public boolean isEnabled(GuiBase gui, EntityPlayer player) {
                return inviteName.getText().length() > 0;
            }

            @Override
            public boolean isVisible(GuiBase gui, EntityPlayer player) {
                return !getTeam().isSingle() && getEntry(getTeam()).isOwner();
            }

            @Override
            public void onClick(GuiBase gui, EntityPlayer player) {
                getTeam().invite(inviteName.getText());
            }
        });

        buttons.add(new LargeButton("hqm.party.accept", 180, 20) {
            @Override
            public boolean isEnabled(GuiBase gui, EntityPlayer player) {
                return true;
            }

            @Override
            public boolean isVisible(GuiBase gui, EntityPlayer player) {
                return inviteTeam != null;
            }

            @Override
            public void onClick(GuiBase gui, EntityPlayer player) {
                inviteTeam.accept();
            }
        });

        buttons.add(new LargeButton("hqm.party.decline", 240, 20) {
            @Override
            public boolean isEnabled(GuiBase gui, EntityPlayer player) {
                return true;
            }

            @Override
            public boolean isVisible(GuiBase gui, EntityPlayer player) {
                return inviteTeam != null;
            }

            @Override
            public void onClick(GuiBase gui, EntityPlayer player) {
                inviteTeam.decline();
                inviteTeam = null;
            }
        });

        buttons.add(new LargeButton("hqm.party.decideLater", 180, 40) {
            @Override
            public boolean isEnabled(GuiBase gui, EntityPlayer player) {
                return true;
            }

            @Override
            public boolean isVisible(GuiBase gui, EntityPlayer player) {
                return inviteTeam != null;
            }

            @Override
            public void onClick(GuiBase gui, EntityPlayer player) {
                inviteTeam = null;
            }
        });

        buttons.add(new LargeButton(null, 250, 50) {
            @Override
            public boolean isEnabled(GuiBase gui, EntityPlayer player) {
                return !selectedEntry.isOwner();
            }

            @Override
            public boolean isVisible(GuiBase gui, EntityPlayer player) {
                return selectedEntry != null && getEntry(getTeam()).isOwner();
            }

            @Override
            public void onClick(GuiBase gui, EntityPlayer player) {
                getTeam().kick(selectedEntry.getUUID());
                selectedEntry = null;
            }

            @Override
            protected String getName() {
                return Translator.translate(selectedEntry.isInTeam() ? "hqm.party.kickPlayer" : "hqm.party.removeInvite");
            }
        });

        buttons.add(new LargeButton("hqm.party.leave", 250, 160) {
            @Override
            public boolean isEnabled(GuiBase gui, EntityPlayer player) {
                return GuiScreen.isShiftKeyDown();
            }

            @Override
            public boolean isVisible(GuiBase gui, EntityPlayer player) {
                return !getTeam().isSingle() && !getEntry(getTeam()).isOwner();
            }

            @Override
            public void onClick(GuiBase gui, EntityPlayer player) {
                getTeam().leave();
            }
        });

        buttons.add(new LargeButton("hqm.party.disband", 250, 160) {
            @Override
            public boolean isEnabled(GuiBase gui, EntityPlayer player) {
                return GuiScreen.isShiftKeyDown() && GuiScreen.isCtrlKeyDown();
            }

            @Override
            public boolean isVisible(GuiBase gui, EntityPlayer player) {
                return !getTeam().isSingle() && selectedEntry != null && selectedEntry.isOwner();
            }

            @Override
            public void onClick(GuiBase gui, EntityPlayer player) {
                getTeam().disband();
                selectedEntry = null;
            }
        });


        buttons.add(new LargeButton("hqm.party.list", 250, 190) {
            @Override
            public boolean isEnabled(GuiBase gui, EntityPlayer player) {
                return true;
            }

            @Override
            public boolean isVisible(GuiBase gui, EntityPlayer player) {
                return true;
            }

            @Override
            public void onClick(GuiBase gui, EntityPlayer player) {
                gui.setEditMenu(new GuiEditMenuTeamList((GuiQuestBook) gui, player, self));
            }
        });

        textBoxes = new TextBoxGroup();
        textBoxes.add(teamName = new TextBoxName(gui, "", 180, 26) {
            @Override
            protected boolean isVisible() {
                return getTeam().isSingle() && inviteTeam == null;
            }
        });
        teamName.setWidth((int) ((GuiQuestBook.PAGE_WIDTH - TITLE_X - 10) * 0.7F));

        textBoxes.add(inviteName = new TextBoxName(gui, "", 180, 26) {
            @Override
            protected boolean isVisible() {
                return !getTeam().isSingle() && getEntry(getTeam()).isOwner();
            }
        });

        scrollBars = new ArrayList<>();
        scrollBars.add(inviteScroll = new ScrollBar(155, 22, 186, 171, 69, PLAYER_X) {
            @Override
            public boolean isVisible(GuiBase gui) {
                return inviteTeam == null && getTeam().isSingle() && getTeam().getInvites() != null && getTeam().getInvites().size() > VISIBLE_INVITES;
            }
        });

        scrollBars.add(memberScroll = new ScrollBar(155, 22, 186, 171, 69, PLAYER_X) {
            @Override
            public boolean isVisible(GuiBase gui) {
                return (inviteTeam != null && inviteTeam.getPlayers().size() > VISIBLE_MEMBERS) || (!getTeam().isSingle() && getTeam().getPlayers().size() > VISIBLE_MEMBERS);
            }
        });
    }

    @Override
    public void draw(GuiBase gui, int mX, int mY) {


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

        super.draw(gui, mX, mY);

        textBoxes.draw(gui);

        ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        for (ScrollBar scrollBar : scrollBars) {
            scrollBar.draw(gui);
        }

        if (team.isSingle() && inviteTeam == null) {
            int inviteCount = team.getInvites() == null ? 0 : team.getInvites().size();
            if (inviteCount > 0) {
                gui.drawString(Translator.translate("hqm.party.invites"), TITLE_X, TITLE_Y, 0x404040);
                List<Team> invites = team.getInvites();
                int start = inviteScroll.isVisible(gui) ? Math.round((team.getInvites().size() - VISIBLE_INVITES) * inviteScroll.getScroll()) : 0;
                int end = Math.min(invites.size(), start + VISIBLE_INVITES);
                for (int i = start; i < end; i++) {
                    Team invite = invites.get(i);
                    gui.drawString(invite.getName(), PLAYER_X, PLAYER_Y + PLAYER_SPACING * (i - start), 0x404040);
                }
            } else {
                gui.drawString(Translator.translate("hqm.party.noInvites"), TITLE_X, TITLE_Y, 0x404040);
            }

            gui.drawString(Translator.translate("hqm.party.name"), 180, 20, 0.7F, 0x404040);
        } else {
            boolean isOwner = inviteTeam == null && entry.isOwner();
            String title = (inviteTeam == null ? team : inviteTeam).getName();
            gui.drawString(title, TITLE_X, TITLE_Y, 0x404040);
            List<PlayerEntry> players = (inviteTeam == null ? team : inviteTeam).getPlayers();
            int y = 0;
            int start = memberScroll.isVisible(gui) ? Math.round(((isOwner ? players.size() : (inviteTeam == null ? team : inviteTeam).getPlayerCount()) - VISIBLE_MEMBERS) * memberScroll.getScroll()) : 0;

            for (PlayerEntry player : players) {
                String str = player.getDisplayName();

                if (player.isOwner()) {
                    str += GuiColor.ORANGE + " [" + Translator.translate("hqm.party.owner") + "]";
                } else if (!player.isInTeam()) {
                    if (isOwner) {
                        str += GuiColor.LIGHT_GRAY + " [" + Translator.translate("hqm.party.invite") + "]";
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
                    gui.drawString(str, PLAYER_X, PLAYER_Y + PLAYER_SPACING * (y - start), 0.7F, color);
                }
                y++;
                if (y == start + VISIBLE_MEMBERS) {
                    break;
                }
            }

            if (inviteTeam == null) {
                if (entry.isOwner()) {
                    gui.drawString(Translator.translate("hqm.party.playerName"), 180, 20, 0.7F, 0x404040);

                    if (selectedEntry != null) {
                        gui.drawString(gui.getLinesFromText(Translator.translate("hqm.party.currentSelection", selectedEntry.getDisplayName()), 0.7F, 70), 177, 52, 0.7F, 0x404040);

                        if (selectedEntry.isOwner()) {
                            gui.drawString(gui.getLinesFromText(Translator.translate("hqm.party.shiftCtrlConfirm"), 0.6F, 70), 177, 162, 0.6F, GuiColor.RED.getHexColor());
                        }
                    }

                } else {
                    gui.drawString(gui.getLinesFromText(Translator.translate("hqm.party.shiftConfirm"), 0.7F, 70), 177, 162, 0.7F, GuiColor.RED.getHexColor());
                }
            }
            gui.drawString(gui.getLinesFromText(Translator.translate("hqm.party.stats"), 0.7F, 70), 177, 192, 0.7F, 0x404040);

            Team infoTeam = inviteTeam == null ? team : inviteTeam;

            int infoY = getInfoY();


            ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            gui.drawRect(INFO_BOX_X, infoY, INFO_BOX_SRC_X, INFO_BOX_SRC_Y, INFO_BOX_SIZE, INFO_BOX_SIZE);
            gui.drawRect(INFO_BOX_X, infoY + REWARD_SETTING_Y, INFO_BOX_SRC_X, INFO_BOX_SRC_Y, INFO_BOX_SIZE, INFO_BOX_SIZE);

            gui.drawString(Translator.translate("hqm.party.lifeSetting", infoTeam.getLifeSetting().getTitle()), INFO_BOX_X + INFO_BOX_TEXT_OFFSET_X, infoY + INFO_BOX_TEXT_OFFSET_Y, 0.7F, 0x404040);
            gui.drawString(Translator.translate("hqm.party.rewardSetting", infoTeam.getRewardSetting().getTitle()), INFO_BOX_X + INFO_BOX_TEXT_OFFSET_X, infoY + REWARD_SETTING_Y + INFO_BOX_TEXT_OFFSET_Y, 0.7F, 0x404040);

        }


    }

    @Override
    public void drawMouseOver(GuiBase gui, int mX, int mY) {
        super.drawMouseOver(gui, mX, mY);

        Team team = getTeam();
        PlayerEntry entry = getEntry(team);
        boolean isOwner = inviteTeam == null && entry.isOwner();

        if (!team.isSingle() || inviteTeam != null) {
            int infoY = getInfoY();
            Team infoTeam = inviteTeam == null ? team : inviteTeam;
            if (gui.inBounds(INFO_BOX_X, infoY, INFO_BOX_SIZE, INFO_BOX_SIZE, mX, mY)) {
                gui.drawMouseOver(gui.getLinesFromText(GuiColor.GREEN + infoTeam.getLifeSetting().getTitle() + "\n" + infoTeam.getLifeSetting().getDescription() + (isOwner ? "\n\n" + GuiColor.ORANGE + Translator.translate("hqm.party.change") : ""), 1F, 200), gui.getLeft() + mX, gui.getTop() + mY);
            } else if (gui.inBounds(INFO_BOX_X, infoY + REWARD_SETTING_Y, INFO_BOX_SIZE, INFO_BOX_SIZE, mX, mY)) {
                gui.drawMouseOver(gui.getLinesFromText(GuiColor.GREEN + infoTeam.getRewardSetting().getTitle() + "\n" + infoTeam.getRewardSetting().getDescription() + (isOwner ? "\n\n" + GuiColor.ORANGE + Translator.translate("hqm.party.change") : ""), 1F, 200), gui.getLeft() + mX, gui.getTop() + mY);
            }
        }

        if (TeamError.latestError != null) {
            if (inviteButton.inButtonBounds(gui, mX, mY)) {
                gui.drawMouseOver(gui.getLinesFromText(GuiColor.RED + TeamError.latestError.getHeader() + "\n" + TeamError.latestError.getMessage(), 1F, 150), mX + gui.getLeft(), mY + gui.getTop());
            } else {
                TeamError.latestError = null;
            }
        }
    }

    @Override
    public void onClick(GuiBase gui, int mX, int mY, int b) {
        super.onClick(gui, mX, mY, b);


        Team team = getTeam();
        if (team.isSingle() && inviteTeam == null) {
            List<Team> invites = team.getInvites();
            if (invites != null) {
                int start = inviteScroll.isVisible(gui) ? Math.round((team.getInvites().size() - VISIBLE_INVITES) * inviteScroll.getScroll()) : 0;
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
            int start = memberScroll.isVisible(gui) ? Math.round((team.getPlayers().size() - VISIBLE_MEMBERS) * memberScroll.getScroll()) : 0;
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


        textBoxes.onClick(gui, mX, mY);
        for (ScrollBar scrollBar : scrollBars) {
            scrollBar.onClick(gui, mX, mY);
        }
    }

    @Override
    public void onKeyTyped(GuiBase gui, char c, int k) {
        super.onKeyTyped(gui, c, k);
        textBoxes.onKeyStroke(gui, c, k);
    }

    @Override
    public void onDrag(GuiBase gui, int mX, int mY) {
        super.onDrag(gui, mX, mY);

        for (ScrollBar scrollBar : scrollBars) {
            scrollBar.onDrag(gui, mX, mY);
        }
    }

    @Override
    public void onRelease(GuiBase gui, int mX, int mY) {
        super.onRelease(gui, mX, mY);

        for (ScrollBar scrollBar : scrollBars) {
            scrollBar.onRelease(gui, mX, mY);
        }
    }

    @Override
    public void onScroll(GuiBase gui, int mX, int mY, int scroll) {
        super.onScroll(gui, mX, mY, scroll);

        for (ScrollBar scrollBar : scrollBars) {
            scrollBar.onScroll(gui, mX, mY, scroll);
        }
    }

    @Override
    public void save(GuiBase gui) {

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
        return QuestingData.getQuestingData(player).getTeam();
    }

    private PlayerEntry getEntry(Team team) {
        return team.getEntry(QuestingData.getUserUUID(player));
    }

    private class TextBoxName extends TextBoxGroup.TextBox {

        public TextBoxName(GuiQuestBook gui, String str, int x, int y) {
            super(gui, str, x, y, true);
            setMult(0.7F);
            offsetY = 5;
        }
    }


}
