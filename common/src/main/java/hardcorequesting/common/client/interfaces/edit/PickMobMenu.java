package hardcorequesting.common.client.interfaces.edit;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.*;
import hardcorequesting.common.quests.task.icon.TameMobsTask;
import hardcorequesting.common.util.Translator;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class PickMobMenu extends GuiEditMenuExtended {
    //TODO add support for entity tags to replace functionality of this special case
    public static final List<Entry> EXTRA_TAME_ENTRIES = ImmutableList.of(new Entry(TameMobsTask.ABSTRACT_HORSE, Translator.plain("Any Horse-like Entity")));
    
    private static final int START_X = 20;
    private static final int START_Y = 20;
    private static final int OFFSET_Y = 8;
    private static final int VISIBLE_MOBS = 24;
    
    private final Consumer<Result> resultConsumer;
    private final String textKey;
    private Entry mob;
    private int amount;
    
    private final ScrollBar scrollBar;
    private final List<Entry> rawMobs;
    private final List<Entry> mobs;
    
    public static void display(GuiQuestBook gui, Player player, ResourceLocation initMobId, int initAmount, String textKey, Consumer<Result> resultConsumer) {
        gui.setEditMenu(new PickMobMenu(gui, player, initMobId, initAmount, textKey, Collections.emptyList(), resultConsumer));
    }
    
    public static void display(GuiQuestBook gui, Player player, ResourceLocation initMobId, int initAmount, String textKey, List<Entry> extraEntries, Consumer<Result> resultConsumer) {
        gui.setEditMenu(new PickMobMenu(gui, player, initMobId, initAmount, textKey, extraEntries, resultConsumer));
    }
    
    private PickMobMenu(GuiQuestBook gui, Player player, ResourceLocation initMobId, int initAmount, String textKey, List<Entry> extraEntries, Consumer<Result> resultConsumer) {
        super(gui, player, false, 180, 70, 180, 150);
        
        this.resultConsumer = resultConsumer;
        this.textKey = textKey;
        this.amount = initAmount;
        
        scrollBar = new ScrollBar(160, 18, 186, 171, 69, START_X) {
            @Override
            public boolean isVisible(GuiBase gui) {
                return mobs.size() > VISIBLE_MOBS;
            }
        };
        
        textBoxes.add(new TextBoxNumber(gui, 0, "hqm." + textKey + ".reqKills") {
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
                rawMobs.add(new Entry(type));
            }
        }
        
        rawMobs.addAll(extraEntries);
        
        for (Entry entry : rawMobs) {
            if (entry.id.equals(initMobId))
                this.mob = entry;
        }
        
        rawMobs.sort(Comparator.comparing(entry -> entry.id));
        updateMobs("");
    }
    
    private void updateMobs(String search) {
        search = search.toLowerCase();
        if (mobs != null) {
            mobs.clear();
            for (Entry rawMob : rawMobs) {
                if (rawMob.description.toString().toLowerCase().contains(search)
                        || rawMob.id.toString().toLowerCase().contains(search)) {
                    mobs.add(rawMob);
                }
            }
        }
    }
    
    @Override
    public void draw(PoseStack matrices, GuiBase gui, int mX, int mY) {
        super.draw(matrices, gui, mX, mY);
        
        ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        scrollBar.draw(matrices, gui);
        
        int start = scrollBar.isVisible(gui) ? Math.round((mobs.size() - VISIBLE_MOBS) * scrollBar.getScroll()) : 0;
        int end = Math.min(mobs.size(), start + VISIBLE_MOBS);
        for (int i = start; i < end; i++) {
            boolean selected = mobs.get(i).equals(mob);
            boolean inBounds = gui.inBounds(START_X, START_Y + (i - start) * OFFSET_Y, 130, 6, mX, mY);
            
            gui.drawString(matrices, mobs.get(i).description, START_X, START_Y + OFFSET_Y * (i - start), 0.7F, selected ? inBounds ? 0xC0C0C0 : 0xA0A0A0 : inBounds ? 0x707070 : 0x404040);
        }
        
        gui.drawString(matrices, Translator.translatable("hqm." + textKey + ".search"), 180, 20, 0x404040);
        gui.drawString(matrices, Translator.translatable("hqm." + textKey + "." + (mob == null ? "nothing" : "currently") + "Selected"), 180, 40, 0x404040);
        if (mob != null) {
            gui.drawString(matrices, mob.description, 180, 50, 0.7F, 0x404040);
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
                if (mobs.get(i).equals(mob)) {
                    mob = null;
                } else {
                    mob = mobs.get(i);
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
    protected boolean isArrowVisible() {
        return false;   //There is currently no precision for mobs. Change this if precision is ever added back
    }
    
    @Override
    protected void onArrowClick(boolean left) {
//        mob.setExact(!mob.isExact());
    }
    
    @Override
    protected String getArrowText() {
        return I18n.get("hqm." + textKey + "." + "type" + "Match.title");
    }
    
    @Override
    protected String getArrowDescription() {
        return I18n.get("hqm." + textKey + "." + "type" + "Match.desc");
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
        resultConsumer.accept(new Result(mob.id, Math.max(1, amount)));
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
    
    public static class Entry {
        private final ResourceLocation id;
        private final FormattedText description;
    
        private Entry(EntityType<?> type) {
            this.id = Registry.ENTITY_TYPE.getKey(type);
            this.description = type.getDescription();
        }
        
        public Entry(ResourceLocation id, FormattedText description) {
            this.id = id;
            this.description = description;
        }
    }
}
