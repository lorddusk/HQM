package hardcorequesting.client.interfaces.edit;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.client.interfaces.GuiBase;
import hardcorequesting.client.interfaces.GuiQuestBook;
import hardcorequesting.client.interfaces.RenderRotation;
import hardcorequesting.team.TeamLiteStat;
import hardcorequesting.util.Translator;
import net.minecraft.world.entity.player.Player;

public class GuiEditMenuTeamList extends GuiEditMenu {
    
    private static final int TEAM_X = 20;
    private static final int TEAM_X_2ND_PAGE = 180;
    private static final int TEAM_Y = 20;
    private static final int TEAM_OFFSET = 50;
    private static final int TEAM_LINE_OFFSET = 10;
    private static final int TEAM_LINE_INDENT = 5;
    private static final int TEAMS_PER_PAGE = 4;
    private static final int TEAMS_PER_PAIR = TEAMS_PER_PAGE * 2;
    private static final int ARROW_X_LEFT = 30;
    private static final int ARROW_X_RIGHT = 288;
    private static final int ARROW_Y = 212;
    private static final int ARROW_SRC_X = 181;
    private static final int ARROW_SRC_Y = 69;
    private static final int ARROW_W = 20;
    private static final int ARROW_H = 9;
    private GuiEditMenuTeam parent;
    private int pagePair = 0;
    
    protected GuiEditMenuTeamList(GuiQuestBook gui, Player player, GuiEditMenuTeam parent) {
        super(gui, player);
        this.parent = parent;
    }
    
    @Override
    public void draw(PoseStack matrices, GuiBase gui, int mX, int mY) {
        super.draw(matrices, gui, mX, mY);
        
        drawArrow(gui, mX, mY, true);
        drawArrow(gui, mX, mY, false);
        
        TeamLiteStat[] teamStats = TeamLiteStat.getTeamStats();
        int start = pagePair * TEAMS_PER_PAIR;
        int end = Math.min(start + TEAMS_PER_PAIR, teamStats.length);
        
        for (int i = start; i < end; i++) {
            TeamLiteStat teamStat = teamStats[i];
            
            int x = (i - start) < TEAMS_PER_PAGE ? TEAM_X : TEAM_X_2ND_PAGE;
            int y = TEAM_Y + ((i - start) % TEAMS_PER_PAGE) * TEAM_OFFSET;
            gui.drawString(matrices, Translator.plain(teamStat.getName()), x, y, 0x404040);
            gui.drawString(matrices, Translator.translatable("hqm.teamList.done", teamStat.getProgress()), x + TEAM_LINE_INDENT, y + TEAM_LINE_OFFSET, 0.7F, 0x404040);
            gui.drawString(matrices, Translator.translatable("hqm.teamList.players", teamStat.getPlayers()), x + TEAM_LINE_INDENT, y + TEAM_LINE_OFFSET * 2, 0.7F, 0x404040);
            gui.drawString(matrices, Translator.translatable("hqm.teamList.lives", teamStat.getLives()), x + TEAM_LINE_INDENT, y + TEAM_LINE_OFFSET * 3, 0.7F, 0x404040);
        }
        
        gui.drawCenteredString(matrices, Translator.translatable("hqm.teamList.page", ((pagePair * 2) + 1)), 0, 202, 0.7F, 170, 30, 0x707070);
        if (end - start > TEAMS_PER_PAGE) {
            gui.drawCenteredString(matrices, Translator.translatable("hqm.teamList.page", ((pagePair * 2) + 2)), 170, 202, 0.7F, 170, 30, 0x707070);
        }
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
    public void close(GuiBase gui) {
        gui.setEditMenu(parent);
    }
    
    @Override
    public void save(GuiBase gui) {
        
    }
    
    private void drawArrow(GuiBase gui, int mX, int mY, boolean left) {
        int x = left ? ARROW_X_LEFT : ARROW_X_RIGHT;
        int srcY = 0;
        
        if (isArrowEnabled(left)) {
            srcY = gui.inBounds(x, ARROW_Y, ARROW_W, ARROW_H, mX, mY) ? 2 : 1;
        }
        gui.drawRect(x, ARROW_Y, ARROW_SRC_X, ARROW_SRC_Y + srcY * ARROW_W, ARROW_H, ARROW_W, left ? RenderRotation.ROTATE_90 : RenderRotation.ROTATE_270);
    }
    
    private boolean isArrowEnabled(boolean left) {
        return (left && pagePair > 0) || (!left && pagePair < Math.ceil((float) TeamLiteStat.getTeamStats().length / TEAMS_PER_PAIR) - 1);
    }
}
