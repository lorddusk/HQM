package hardcorequesting.common.client.interfaces.edit;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.*;
import hardcorequesting.common.util.Translator;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class GuiEditMenuMob extends GuiEditMenuExtended {
    
    private static final int START_X = 20;
    private static final int START_Y = 20;
    private static final int OFFSET_Y = 8;
    private static final int VISIBLE_MOBS = 24;
    
    private final Consumer<Result> resultConsumer;
    private ResourceLocation mobId;
    private int amount;
    
    private ScrollBar scrollBar;
    private List<ResourceLocation> rawMobs;
    private List<ResourceLocation> mobs;
    
    public static void display(GuiQuestBook gui, Player player, ResourceLocation initMobId, int initAmount, Consumer<Result> resultConsumer) {
        gui.setEditMenu(new GuiEditMenuMob(gui, player, initMobId, initAmount, resultConsumer));
    }
    
    private GuiEditMenuMob(GuiQuestBook gui, Player player, ResourceLocation initMobId, int initAmount, Consumer<Result> resultConsumer) {
        super(gui, player, false, 180, 70, 180, 150);
        
        this.resultConsumer = resultConsumer;
        this.mobId = initMobId;
        this.amount = initAmount;
        
        scrollBar = new ScrollBar(160, 18, 186, 171, 69, START_X) {
            @Override
            public boolean isVisible(GuiBase gui) {
                return mobs.size() > VISIBLE_MOBS;
            }
        };
        
        textBoxes.add(new TextBoxNumber(gui, 0, "hqm.mobTask.reqKills") {
            @Override
            protected int getValue() {
                return amount;
            }
            
            @Override
            protected void setValue(int number) {
                amount = number;
            }
        });
        
        textBoxes.add(new TextBoxGroup.TextBox(gui, "", 250, 18, false) {
            @Override
            public void textChanged(GuiBase gui) {
                super.textChanged(gui);
                updateMobs(getText());
            }
        });
        
        rawMobs = new ArrayList<>();
        mobs = new ArrayList<>();
        
        for (EntityType<?> type : Registry.ENTITY_TYPE) {
            if (type.canSummon()) {
                rawMobs.add(Registry.ENTITY_TYPE.getKey(type));
            }
        }
        
        
        Collections.sort(rawMobs);
        updateMobs("");
    }
    
    private void updateMobs(String search) {
        if (mobs != null) {
            mobs.clear();
            for (ResourceLocation rawMob : rawMobs) {
                if (Registry.ENTITY_TYPE.get(rawMob).getDescription().toString().toLowerCase().contains(search.toLowerCase())) {
                    mobs.add(rawMob);
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
        
        int start = scrollBar.isVisible(gui) ? Math.round((mobs.size() - VISIBLE_MOBS) * scrollBar.getScroll()) : 0;
        int end = Math.min(mobs.size(), start + VISIBLE_MOBS);
        for (int i = start; i < end; i++) {
            boolean selected = mobs.get(i).equals(mobId);
            boolean inBounds = gui.inBounds(START_X, START_Y + (i - start) * OFFSET_Y, 130, 6, mX, mY);
            
            gui.drawString(matrices, Registry.ENTITY_TYPE.get(mobs.get(i)).getDescription(), START_X, START_Y + OFFSET_Y * (i - start), 0.7F, selected ? inBounds ? 0xC0C0C0 : 0xA0A0A0 : inBounds ? 0x707070 : 0x404040);
        }
        
        gui.drawString(matrices, Translator.translatable("hqm.mobTask.search"), 180, 20, 0x404040);
        gui.drawString(matrices, Translator.translatable("hqm.mobTask." + (mobId == null ? "nothing" : "currently") + "Selected"), 180, 40, 0x404040);
        if (mobId != null) {
            gui.drawString(matrices, Registry.ENTITY_TYPE.get(mobId).getDescription(), 180, 50, 0.7F, 0x404040);
        }
    }
    
    @Override
    public void onClick(GuiBase gui, int mX, int mY, int b) {
        super.onClick(gui, mX, mY, b);
        
        scrollBar.onClick(gui, mX, mY);
        
        int start = scrollBar.isVisible(gui) ? Math.round((mobs.size() - VISIBLE_MOBS) * scrollBar.getScroll()) : 0;
        int end = Math.min(mobs.size(), start + VISIBLE_MOBS);
        for (int i = start; i < end; i++) {
            if (gui.inBounds(START_X, START_Y + (i - start) * OFFSET_Y, 130, 6, mX, mY)) {
                if (mobs.get(i).equals(mobId)) {
                    mobId = null;
                } else {
                    mobId = mobs.get(i);
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
//        mob.setExact(!mob.isExact());
    }
    
    @Override
    protected String getArrowText() {
        return I18n.get("hqm.mobTask." + "type" + "Match.title");
    }
    
    @Override
    protected String getArrowDescription() {
        return I18n.get("hqm.mobTask." + "type" + "Match.desc");
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
        resultConsumer.accept(new Result(mobId, Math.max(1, amount)));
    }
    
    public static class Result {
        private final ResourceLocation mobId;
        private final int amount;
    
        private Result(ResourceLocation mobId, int amount) {
            this.mobId = mobId;
            this.amount = amount;
        }
    
        public ResourceLocation getMobId() {
            return mobId;
        }
    
        public int getAmount() {
            return amount;
        }
    }
}
