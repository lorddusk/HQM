package hardcorequesting.client.interfaces;

import hardcorequesting.TeamStats;
import hardcorequesting.Translator;
import net.minecraft.entity.player.EntityPlayer;

public class GuiEditMenuTeamList extends GuiEditMenu {
    private GuiEditMenuTeam parent;

    protected GuiEditMenuTeamList(GuiQuestBook gui, EntityPlayer player, GuiEditMenuTeam parent) {
        super(gui, player);
        this.parent = parent;
    }

    private static final int TEAM_X = 20;
    private static final int TEAM_X_2ND_PAGE = 180;
    private static final int TEAM_Y = 20;
    private static final int TEAM_OFFSET = 50;
    private static final int TEAM_LINE_OFFSET = 10;
    private static final int TEAM_LINE_INDENT = 5;
    private static final int TEAMS_PER_PAGE = 4;
    private static final int TEAMS_PER_PAIR = TEAMS_PER_PAGE * 2;

    private int pagePair = 0;

    @Override
    public void draw(GuiBase gui, int mX, int mY) {
        super.draw(gui, mX, mY);

        drawArrow(gui, mX, mY, true);
        drawArrow(gui, mX, mY, false);

        TeamStats[] teamStats = TeamStats.getTeamStats();
        int start = pagePair * TEAMS_PER_PAIR;
        int end = Math.min(start + TEAMS_PER_PAIR, teamStats.length);

        for (int i = start; i < end; i++) {
            TeamStats teamStat = teamStats[i];

            int x = (i - start) < TEAMS_PER_PAGE ? TEAM_X : TEAM_X_2ND_PAGE;
            int y = TEAM_Y + ((i - start) % TEAMS_PER_PAGE) * TEAM_OFFSET;
            gui.drawString(teamStat.getName(), x, y, 0x404040);
            gui.drawString(Translator.translate("hqm.teamList.done", teamStat.getProgress()), x + TEAM_LINE_INDENT, y + TEAM_LINE_OFFSET, 0.7F, 0x404040);
            gui.drawString(Translator.translate("hqm.teamList.players", teamStat.getPlayers()), x + TEAM_LINE_INDENT, y + TEAM_LINE_OFFSET * 2, 0.7F, 0x404040);
            gui.drawString(Translator.translate("hqm.teamList.lives", teamStat.getLives()), x + TEAM_LINE_INDENT, y + TEAM_LINE_OFFSET * 3, 0.7F, 0x404040);
        }

        gui.drawCenteredString(Translator.translate("hqm.teamList.page", ((pagePair * 2) + 1)), 0, 202, 0.7F, 170, 30, 0x707070);
        if (end - start > TEAMS_PER_PAGE) {
            gui.drawCenteredString(Translator.translate("hqm.teamList.page", ((pagePair * 2) + 2)), 170, 202, 0.7F, 170, 30, 0x707070);
        }
    }

    private static final int ARROW_X_LEFT = 30;
    private static final int ARROW_X_RIGHT = 288;
    private static final int ARROW_Y = 212;
    private static final int ARROW_SRC_X = 181;
    private static final int ARROW_SRC_Y = 69;
    private static final int ARROW_W = 20;
    private static final int ARROW_H = 9;

    private void drawArrow(GuiBase gui, int mX, int mY, boolean left) {
        int x = left ? ARROW_X_LEFT : ARROW_X_RIGHT;
        int srcY = 0;

        if (isArrowEnabled(left)) {
            srcY = gui.inBounds(x, ARROW_Y, ARROW_W, ARROW_H, mX, mY) ? 2 : 1;
        }
        gui.drawRect(x, ARROW_Y, ARROW_SRC_X, ARROW_SRC_Y + srcY * ARROW_W, ARROW_H, ARROW_W, left ? RenderRotation.ROTATE_90 : RenderRotation.ROTATE_270);
    }

    private boolean isArrowEnabled(boolean left) {
        return (left && pagePair > 0) || (!left && pagePair < Math.ceil((float) TeamStats.getTeamStats().length / TEAMS_PER_PAIR) - 1);
    }

    @Override
    public void onClick(GuiBase gui, int mX, int mY, int b) {
        super.onClick(gui, mX, mY, b);

        if (isArrowEnabled(true) && gui.inBounds(ARROW_X_LEFT, ARROW_Y, ARROW_W, ARROW_H, mX, mY)) {
            pagePair--;
        } else if (isArrowEnabled(false) && gui.inBounds(ARROW_X_RIGHT, ARROW_Y, ARROW_W, ARROW_H, mX, mY)) {
            pagePair++;
        }
    }

    @Override
    protected void save(GuiBase gui) {

    }

    @Override
    protected void close(GuiBase gui) {
        gui.setEditMenu(parent);
    }
}
