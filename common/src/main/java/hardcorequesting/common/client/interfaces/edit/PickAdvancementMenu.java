package hardcorequesting.common.client.interfaces.edit;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.ResourceHelper;
import hardcorequesting.common.client.interfaces.widget.ArrowSelectionHelper;
import hardcorequesting.common.client.interfaces.widget.ExtendedScrollBar;
import hardcorequesting.common.client.interfaces.widget.ScrollBar;
import hardcorequesting.common.client.interfaces.widget.TextBoxGroup;
import hardcorequesting.common.util.Translator;
import net.minecraft.advancements.Advancement;
import net.minecraft.network.chat.FormattedText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class PickAdvancementMenu extends GuiEditMenu {
    
    private static final int START_X = 20;
    private static final int START_Y = 20;
    private static final int OFFSET_Y = 8;
    private static final int VISIBLE_MOBS = 24;
    
    private final Consumer<String> resultConsumer;
    private String advancement;
    
    private final ExtendedScrollBar<String> scrollBar;
    
    private final List<String> rawAdvancemenNames;
    private final List<String> advancementNames;
    private final ArrowSelectionHelper selectionHelper;
    
    public static void display(GuiQuestBook gui, UUID playerId, String advancement, Consumer<String> resultConsumer) {
        gui.setEditMenu(new PickAdvancementMenu(gui, playerId, advancement, resultConsumer));
    }
    
    private PickAdvancementMenu(GuiQuestBook gui, UUID playerId, String advancement, Consumer<String> resultConsumer) {
        super(gui, playerId, false);
    
        this.resultConsumer = resultConsumer;
        this.advancement = advancement;
        
        addScrollBar(scrollBar = new ExtendedScrollBar<>(gui, ScrollBar.Size.LONG, 160, 18, START_X,
                VISIBLE_MOBS, () -> PickAdvancementMenu.this.advancementNames));
        
        addTextBox(new TextBoxGroup.TextBox(gui, "", 250, 18, false) {
            @Override
            public void textChanged() {
                super.textChanged();
                updateAdvancements(getText());
            }
        });
        
        selectionHelper = new ArrowSelectionHelper(gui, 180, 70) {
            @Override
            protected void onArrowClick(boolean left) {
            }
    
            @Override
            protected FormattedText getArrowText() {
                return Translator.plain("Exact Advancement");
            }
    
            @Override
            protected FormattedText getArrowDescription() {
                return Translator.plain("Completing the exact advancement is required.");
            }
        };
        
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
    public void draw(PoseStack matrices, int mX, int mY) {
        super.draw(matrices, mX, mY);
        
        ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    
        selectionHelper.render(matrices, mX, mY);
        
        int nameY = START_Y;
        for (String name : scrollBar.getVisibleEntries()) {
            boolean selected = name.equals(advancement);
            boolean inBounds = gui.inBounds(START_X, nameY, 130, 6, mX, mY);
            
            gui.drawString(matrices, Translator.plain(name), START_X, nameY, 0.7F, selected ? inBounds ? 0xC0C0C0 : 0xA0A0A0 : inBounds ? 0x707070 : 0x404040);
            nameY += OFFSET_Y;
        }
        
        gui.drawString(matrices, Translator.plain("Search"), 180, 20, 0x404040);
        gui.drawString(matrices, Translator.plain(((advancement == null) ? "Nothing" : "Currently") + "Selected"), 180, 40, 0x404040);
        if (advancement != null) {
            gui.drawString(matrices, Translator.plain(advancement), 180, 50, 0.7F, 0x404040);
        }
    }
    
    @Override
    public void onClick(int mX, int mY, int b) {
        super.onClick(mX, mY, b);
        
        selectionHelper.onClick(mX, mY);
        
        int nameY = START_Y;
        for (String name : scrollBar.getVisibleEntries()) {
            if (gui.inBounds(START_X, nameY, 130, 6, mX, mY)) {
                
                if (name.equals(advancement)) {
                    advancement = null;
                } else {
                    advancement = name;
                }
                break;
            }
            nameY += OFFSET_Y;
        }
    }
    
    @Override
    public void onRelease(int mX, int mY, int button) {
        super.onRelease(mX, mY, button);
        
        selectionHelper.onRelease();
    }
    
    @Override
    public void save() {
        resultConsumer.accept(advancement);
    }
}

