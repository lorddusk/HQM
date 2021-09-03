package hardcorequesting.common.client.interfaces.edit;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.ResourceHelper;
import hardcorequesting.common.client.interfaces.widget.ScrollBar;
import hardcorequesting.common.death.DeathStat;
import hardcorequesting.common.death.DeathStatsManager;
import hardcorequesting.common.death.DeathType;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public class GuiEditMenuDeath extends GuiEditMenu {
    
    private static final int PLAYER_INFO_X = 180;
    private static final int PLAYER_INFO_Y = 20;
    private static final int PLAYER_TOTAL_DEATHS_Y = 12;
    private static final int BACKGROUND_SIZE = 22;
    private static final int ICON_OFFSET = 1;
    private static final int BACKGROUND_SRC_X = 234;
    private static final int BACKGROUND_SRC_Y = 55;
    private static final int ICON_SRC_X = 179;
    private static final int ICON_SRC_Y = 156;
    private static final int ICON_SIZE = 20;
    private static final int TYPE_LOCATION_X = 180;
    private static final int TYPE_LOCATION_Y = 50;
    private static final int TYPE_SPACING_X = 47;
    private static final int TYPE_SPACING_Y = 30;
    private static final int TEXT_OFFSET_X = 28;
    private static final int TEXT_OFFSET_Y = 7;
    private static final int PLAYERS_X = 20;
    private static final int PLAYERS_Y = 20;
    private static final int PLAYERS_SPACING = 20;
    private static final int DEATHS_RIGHT = 140;
    private static final String BEST_LABEL = "hqm.deathMenu.showWorst";
    private static final String TOTAL_LABEL = "hqm.deathMenu.showTotal";
    private static final int BEST_X = 185;
    private static final int TOTAL_X = 255;
    private static final int LABEL_Y = 210;
    private static final int VISIBLE_PLAYERS = 10;
    private static final float[] DIGIT_TEXT_SIZE = {1F, 1F, 0.8F, 0.6F, 0.4F};
    private UUID playerId;
    private boolean showTotal;
    private boolean showBest;
    private ScrollBar scrollBar;
    
    public GuiEditMenuDeath(GuiQuestBook guiQuestBook, Player player) {
        super(guiQuestBook, player);
        
        playerId = player.getUUID();
        
        scrollBar = new ScrollBar(160, 18, 186, 171, 69, PLAYERS_X) {
            @Override
            public boolean isVisible(GuiBase gui) {
                return DeathStatsManager.getInstance().getDeathStats().length > VISIBLE_PLAYERS;
            }
        };
    }
    
    @Override
    public void draw(PoseStack matrices, GuiBase gui, int mX, int mY) {
        super.draw(matrices, gui, mX, mY);
        
        scrollBar.draw(matrices, gui);
        
        DeathStat[] deathStats = DeathStatsManager.getInstance().getDeathStats();
        int start = scrollBar.isVisible(gui) ? Math.round((deathStats.length - VISIBLE_PLAYERS) * scrollBar.getScroll()) : 0;
        int end = Math.min(deathStats.length, start + VISIBLE_PLAYERS);
        for (int i = start; i < end; i++) {
            DeathStat stats = deathStats[i];
            
            boolean selected = stats.getUuid().equals(playerId);
            boolean inBounds = gui.inBounds(PLAYERS_X, PLAYERS_Y + (i - start) * PLAYERS_SPACING, 130, 9, mX, mY);
            gui.drawString(matrices, Translator.plain((i + 1) + ". " + stats.getName()), PLAYERS_X, PLAYERS_Y + (i - start) * PLAYERS_SPACING, getColor(selected, inBounds));
            String deaths = String.valueOf(stats.getTotalDeaths());
            gui.drawString(matrices, Translator.plain(deaths), DEATHS_RIGHT - gui.getStringWidth(deaths), PLAYERS_Y + (i - start) * PLAYERS_SPACING, 0x404040);
        }
        
        gui.drawString(matrices, Translator.translatable(BEST_LABEL), BEST_X, LABEL_Y, getColor(showBest, gui.inBounds(BEST_X, LABEL_Y, gui.getStringWidth(BEST_LABEL), 9, mX, mY)));
        gui.drawString(matrices, Translator.translatable(TOTAL_LABEL), TOTAL_X, LABEL_Y, getColor(showTotal, gui.inBounds(TOTAL_X, LABEL_Y, gui.getStringWidth(TOTAL_LABEL), 9, mX, mY)));
        
        DeathStat stats = getDeathStat();
        
        if (stats != null) {
            
            ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
            
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            for (int i = 0; i < DeathType.values().length; i++) {
                int x = i % 3;
                int y = i / 3;
                
                gui.drawRect(matrices, TYPE_LOCATION_X + TYPE_SPACING_X * x, TYPE_LOCATION_Y + TYPE_SPACING_Y * y, BACKGROUND_SRC_X, BACKGROUND_SRC_Y, BACKGROUND_SIZE, BACKGROUND_SIZE);
                gui.drawRect(matrices, TYPE_LOCATION_X + TYPE_SPACING_X * x + ICON_OFFSET, TYPE_LOCATION_Y + TYPE_SPACING_Y * y + ICON_OFFSET, ICON_SRC_X + ICON_SIZE * x, ICON_SRC_Y + ICON_SIZE * y, ICON_SIZE, ICON_SIZE);
            }
            
            gui.drawString(matrices, Translator.plain(stats.getName()), PLAYER_INFO_X, PLAYER_INFO_Y, 0x404040);
            gui.drawString(matrices, Translator.translatable("hqm.deathMenu.total", stats.getTotalDeaths()), PLAYER_INFO_X, PLAYER_INFO_Y + PLAYER_TOTAL_DEATHS_Y, 0.7F, 0x404040);
            
            for (DeathType type : DeathType.values()) {
                int i = type.ordinal();
                int x = i % 3;
                int y = i / 3;
                
                FormattedText text = Translator.plain(stats.getDeaths(type) + "");
                String str = Translator.rawString(text);
                if (str.length() > 5)
                    text = Translator.translatable("hqm.deathMenu.lots");
                str = Translator.rawString(text);
                float f = DIGIT_TEXT_SIZE[str.length() - 1];
                int offset = f == 1 ? 0 : Math.round(9 * (1 - f) - 1);
                gui.drawString(matrices, text, TYPE_LOCATION_X + TYPE_SPACING_X * x + TEXT_OFFSET_X, TYPE_LOCATION_Y + TYPE_SPACING_Y * y + TEXT_OFFSET_Y + offset, f, 0x404040);
            }
            
            
        }
        
    }
    
    @Override
    public void renderTooltip(PoseStack matrices, GuiBase gui, int mX, int mY) {
        super.renderTooltip(matrices, gui, mX, mY);
        
        DeathStat stats = getDeathStat();
        if (stats != null) {
            for (DeathType type : DeathType.values()) {
                int i = type.ordinal();
                int x = i % 3;
                int y = i / 3;
                
                
                if (gui.inBounds(TYPE_LOCATION_X + TYPE_SPACING_X * x, TYPE_LOCATION_Y + TYPE_SPACING_Y * y, BACKGROUND_SIZE, BACKGROUND_SIZE, mX, mY)) {
                    gui.renderTooltip(matrices, Translator.plain(stats.getDescription(type)), mX + gui.getLeft(), mY + gui.getTop());
                    break;
                }
            }
        }
    }
    
    @Override
    public void onClick(GuiBase gui, int mX, int mY, int b) {
        super.onClick(gui, mX, mY, b);
        
        scrollBar.onClick(gui, mX, mY);
        
        if (gui.inBounds(BEST_X, LABEL_Y, gui.getStringWidth(Translator.translatable(BEST_LABEL)), 9, mX, mY)) {
            showBest = !showBest;
            showTotal = false;
            playerId = null;
        } else if (gui.inBounds(TOTAL_X, LABEL_Y, gui.getStringWidth(Translator.translatable(TOTAL_LABEL)), 9, mX, mY)) {
            showBest = false;
            showTotal = !showTotal;
            playerId = null;
        } else {
            showBest = showTotal = false;
            DeathStat[] deathStats = DeathStatsManager.getInstance().getDeathStats();
            int start = scrollBar.isVisible(gui) ? Math.round((deathStats.length - VISIBLE_PLAYERS) * scrollBar.getScroll()) : 0;
            int end = Math.min(deathStats.length, start + VISIBLE_PLAYERS);
            for (int i = start; i < end; i++) {
                DeathStat stats = deathStats[i];
                
                if (gui.inBounds(PLAYERS_X, PLAYERS_Y + (i - start) * PLAYERS_SPACING, 130, 9, mX, mY)) {
                    if (stats.getUuid().equals(playerId)) {
                        playerId = null;
                    } else {
                        playerId = stats.getUuid();
                    }
                }
            }
        }
    }
    
    @Override
    public void onDrag(GuiBase gui, int mX, int mY) {
        scrollBar.onDrag(gui, mX, mY);
    }
    
    @Override
    public void onRelease(GuiBase gui, int mX, int mY) {
        scrollBar.onRelease(gui, mX, mY);
    }
    
    @Override
    public void onScroll(GuiBase gui, double mX, double mY, double scroll) {
        scrollBar.onScroll(gui, mX, mY, scroll);
    }
    
    @Override
    public void save(GuiBase gui) {
        
    }
    
    private DeathStat getDeathStat() {
        DeathStatsManager manager = DeathStatsManager.getInstance();
        if (showBest) {
            return manager.getBest();
        } else if (showTotal) {
            return manager.getTotal();
        } else if (playerId != null) {
            return manager.getDeathStat(playerId);
        } else {
            return null;
        }
    }
    
    private int getColor(boolean selected, boolean inBounds) {
        return selected ? inBounds ? 0xC0C0C0 : 0xA0A0A0 : inBounds ? 0x707070 : 0x404040;
    }
    
    
}
