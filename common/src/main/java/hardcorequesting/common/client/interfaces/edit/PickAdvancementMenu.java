package hardcorequesting.common.client.interfaces.edit;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.client.interfaces.*;
import hardcorequesting.common.util.Translator;
import net.minecraft.advancements.Advancement;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class PickAdvancementMenu extends GuiEditMenuExtended {
    
    private static final int START_X = 20;
    private static final int START_Y = 20;
    private static final int OFFSET_Y = 8;
    private static final int VISIBLE_MOBS = 24;
    
    private final Consumer<String> resultConsumer;
    private String advancement;
    
    private ScrollBar scrollBar;
    
    private List<String> rawAdvancemenNames;
    private List<String> advancementNames;
    
    public static void display(GuiQuestBook gui, Player player, String advancement, Consumer<String> resultConsumer) {
        gui.setEditMenu(new PickAdvancementMenu(gui, player, advancement, resultConsumer));
    }
    
    private PickAdvancementMenu(GuiQuestBook gui, Player player, String advancement, Consumer<String> resultConsumer) {
        super(gui, player, false, 180, 70, 180, 150);
        
        this.resultConsumer = resultConsumer;
        this.advancement = advancement;
        
        scrollBar = new ScrollBar(160, 18, 186, 171, 69, START_X) {
            @Override
            public boolean isVisible(GuiBase gui) {
                return advancementNames.size() > VISIBLE_MOBS;
            }
        };
        
        textBoxes.add(new TextBoxGroup.TextBox(gui, "", 250, 18, false) {
            @Override
            public void textChanged(GuiBase gui) {
                super.textChanged(gui);
                updateAdvancements(getText());
            }
        });
        
        rawAdvancemenNames = new ArrayList<>();
        advancementNames = new ArrayList<>();
        
        // Just using this to gain access to the advancement manager
        for (Advancement a : HardcoreQuestingCore.getServer().getAdvancements().getAllAdvancements()) {
            String adv = a.getId().toString();
            rawAdvancemenNames.add(adv);
            advancementNames.add(adv);
        }
        
        Collections.sort(advancementNames);
        updateAdvancements("");
    }
    
    private void updateAdvancements(String search) {
        if (advancementNames != null) {
            advancementNames.clear();
            for (String rawAdv : rawAdvancemenNames) {
                if (rawAdv.toLowerCase().contains(search.toLowerCase())) {
                    advancementNames.add(rawAdv);
                }
            }
            
            Collections.sort(advancementNames);
        }
    }
    
    @Override
    public void draw(PoseStack matrices, GuiBase gui, int mX, int mY) {
        super.draw(matrices, gui, mX, mY);
        
        ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        scrollBar.draw(gui);
        
        int start = scrollBar.isVisible(gui) ? Math.round((advancementNames.size() - VISIBLE_MOBS) * scrollBar.getScroll()) : 0;
        int end = Math.min(advancementNames.size(), start + VISIBLE_MOBS);
        for (int i = start; i < end; i++) {
            boolean selected = advancementNames.get(i).equals(advancement);
            boolean inBounds = gui.inBounds(START_X, START_Y + (i - start) * OFFSET_Y, 130, 6, mX, mY);
            
            gui.drawString(matrices, Translator.plain(advancementNames.get(i)), START_X, START_Y + OFFSET_Y * (i - start), 0.7F, selected ? inBounds ? 0xC0C0C0 : 0xA0A0A0 : inBounds ? 0x707070 : 0x404040);
        }
        
        gui.drawString(matrices, Translator.plain("Search"), 180, 20, 0x404040);
        gui.drawString(matrices, Translator.plain(((advancement == null) ? "Nothing" : "Currently") + "Selected"), 180, 40, 0x404040);
        if (advancement != null) {
            gui.drawString(matrices, Translator.plain(advancement), 180, 50, 0.7F, 0x404040);
        }
    }
    
    @Override
    public void onClick(GuiBase gui, int mX, int mY, int b) {
        super.onClick(gui, mX, mY, b);
        
        scrollBar.onClick(gui, mX, mY);
        
        int start = scrollBar.isVisible(gui) ? Math.round((advancementNames.size() - VISIBLE_MOBS) * scrollBar.getScroll()) : 0;
        int end = Math.min(advancementNames.size(), start + VISIBLE_MOBS);
        for (int i = start; i < end; i++) {
            if (gui.inBounds(START_X, START_Y + (i - start) * OFFSET_Y, 130, 6, mX, mY)) {
                
                if (advancementNames.get(i).equals(advancement)) {
                    advancement = null;
                } else {
                    advancement = advancementNames.get(i);
                }
                break;
            }
        }
    }
    
    @Override
    public void onRelease(GuiBase gui, int mX, int mY) {
        super.onRelease(gui, mX, mY);
        
        scrollBar.onRelease(gui, mX, mY);
    }
    
    @Override
    protected void onArrowClick(boolean left) {
    }
    
    @Override
    protected String getArrowText() {
        return "Exact Advancement";
    }
    
    @Override
    protected String getArrowDescription() {
        return "Completing the exact advancement is required.";
    }
    
    @Override
    public void onDrag(GuiBase gui, int mX, int mY) {
        super.onDrag(gui, mX, mY);
        scrollBar.onDrag(gui, mX, mY);
    }
    
    @Override
    public void onScroll(GuiBase gui, double mX, double mY, double scroll) {
        super.onScroll(gui, mX, mY, scroll);
        scrollBar.onScroll(gui, mX, mY, scroll);
    }
    
    @Override
    public void save(GuiBase gui) {
        resultConsumer.accept(advancement);
    }
}

