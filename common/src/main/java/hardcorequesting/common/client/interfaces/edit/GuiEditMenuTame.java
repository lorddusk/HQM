package hardcorequesting.common.client.interfaces.edit;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.*;
import hardcorequesting.common.util.Translator;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class GuiEditMenuTame extends GuiEditMenuExtended {
    
    private static final int START_X = 20;
    private static final int START_Y = 20;
    private static final int OFFSET_Y = 8;
    private static final int VISIBLE_MOBS = 24;
    
    private final Consumer<Result> resultConsumer;
    private String entityId;
    private int amount;
    private ScrollBar scrollBar;
    private List<String> rawTames;
    private List<String> tames;
    
    public static void display(GuiQuestBook gui, Player player, String entityId, int amount, Consumer<Result> resultConsumer) {
        gui.setEditMenu(new GuiEditMenuTame(gui, player, entityId, amount, resultConsumer));
    }
    
    private GuiEditMenuTame(GuiQuestBook gui, Player player, String entityId, int amount, Consumer<Result> resultConsumer) {
        super(gui, player, false, 180, 70, 180, 150);
        
        this.resultConsumer = resultConsumer;
        this.entityId = entityId;
        this.amount = amount;
        
        scrollBar = new ScrollBar(160, 18, 186, 171, 69, START_X) {
            @Override
            public boolean isVisible(GuiBase gui) {
                return tames.size() > VISIBLE_MOBS;
            }
        };
        
        textBoxes.add(new TextBoxNumber(gui, 0, "hqm.tameTask.reqKills") {
            @Override
            protected int getValue() {
                return GuiEditMenuTame.this.amount;
            }
            
            @Override
            protected void setValue(int number) {
                GuiEditMenuTame.this.amount = number;
            }
        });
        
        textBoxes.add(new TextBoxGroup.TextBox(gui, "", 250, 18, false) {
            @Override
            public void textChanged(GuiBase gui) {
                super.textChanged(gui);
                updateTames(getText());
            }
        });
        
        rawTames = new ArrayList<>();
        tames = new ArrayList<>();
        
        for (EntityType<?> entityType : Registry.ENTITY_TYPE) {
            if (entityType.canSummon())
                rawTames.add(Registry.ENTITY_TYPE.getKey(entityType).toString());
        }
        
        rawTames.add("minecraft:abstracthorse");
        
        Collections.sort(rawTames);
        updateTames("");
    }
    
    private void updateTames(String search) {
        if (tames != null) {
            tames.clear();
            for (String rawTame : rawTames) {
                if (rawTame.toLowerCase().contains(search.toLowerCase())) {
                    tames.add(rawTame);
                }
            }
        }
    }
    
    @Override
    public void draw(PoseStack matrices, GuiBase gui, int mX, int mY) {
        super.draw(matrices, gui, mX, mY);
        
        ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        scrollBar.draw(gui);
        
        int start = scrollBar.isVisible(gui) ? Math.round((tames.size() - VISIBLE_MOBS) * scrollBar.getScroll()) : 0;
        int end = Math.min(tames.size(), start + VISIBLE_MOBS);
        for (int i = start; i < end; i++) {
            boolean selected = tames.get(i).equals(entityId);
            boolean inBounds = gui.inBounds(START_X, START_Y + (i - start) * OFFSET_Y, 130, 6, mX, mY);
            
            gui.drawString(matrices, Translator.plain(tames.get(i)), START_X, START_Y + OFFSET_Y * (i - start), 0.7F, selected ? inBounds ? 0xC0C0C0 : 0xA0A0A0 : inBounds ? 0x707070 : 0x404040);
        }
        
        gui.drawString(matrices, Translator.translatable("hqm.tameTask.search"), 180, 20, 0x404040);
        gui.drawString(matrices, Translator.translatable("hqm.tameTask." + (entityId == null ? "nothing" : "currently") + "Selected"), 180, 40, 0x404040);
        if (entityId != null) {
            gui.drawString(matrices, Translator.plain(entityId), 180, 50, 0.7F, 0x404040);
        }
    }
    
    @Override
    public void onClick(GuiBase gui, int mX, int mY, int b) {
        super.onClick(gui, mX, mY, b);
        
        scrollBar.onClick(gui, mX, mY);
        
        int start = scrollBar.isVisible(gui) ? Math.round((tames.size() - VISIBLE_MOBS) * scrollBar.getScroll()) : 0;
        int end = Math.min(tames.size(), start + VISIBLE_MOBS);
        for (int i = start; i < end; i++) {
            if (gui.inBounds(START_X, START_Y + (i - start) * OFFSET_Y, 130, 6, mX, mY)) {
                
                if (tames.get(i).equals(entityId)) {
                    entityId = null;
                } else {
                    entityId = tames.get(i);
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
//        tame.setExact(!tame.isExact());
    }
    
    @Override
    protected String getArrowText() {
        return I18n.get("hqm.tameTask." + "type" + "Match.title");
    }
    
    @Override
    protected String getArrowDescription() {
        return I18n.get("hqm.tameTask." + "exact" + "Match.desc");
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
        resultConsumer.accept(new Result(entityId, Math.max(1, amount)));
    }
    
    public static class Result {
        private final String entityId;
        private final int amount;
    
        private Result(String entityId, int amount) {
            this.entityId = entityId;
            this.amount = amount;
        }
    
        public String getEntityId() {
            return entityId;
        }
    
        public int getAmount() {
            return amount;
        }
    }
}
