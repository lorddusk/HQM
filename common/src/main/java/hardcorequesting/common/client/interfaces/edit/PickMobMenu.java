package hardcorequesting.common.client.interfaces.edit;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.ResourceHelper;
import hardcorequesting.common.client.interfaces.widget.*;
import hardcorequesting.common.quests.task.icon.TameMobsTask;
import hardcorequesting.common.util.Translator;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class PickMobMenu extends GuiEditMenu {
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
    
    private final ExtendedScrollBar<Entry> scrollBar;
    private final List<Entry> rawMobs;
    private final List<Entry> mobs;
    
    public static void display(GuiQuestBook gui, ResourceLocation initMobId, int initAmount, String textKey, Consumer<Result> resultConsumer) {
        gui.setEditMenu(new PickMobMenu(gui, initMobId, initAmount, textKey, Collections.emptyList(), resultConsumer));
    }
    
    public static void display(GuiQuestBook gui, ResourceLocation initMobId, int initAmount, String textKey, List<Entry> extraEntries, Consumer<Result> resultConsumer) {
        gui.setEditMenu(new PickMobMenu(gui, initMobId, initAmount, textKey, extraEntries, resultConsumer));
    }
    
    private PickMobMenu(GuiQuestBook gui, ResourceLocation initMobId, int initAmount, String textKey, List<Entry> extraEntries, Consumer<Result> resultConsumer) {
        super(gui, false);
    
        this.resultConsumer = resultConsumer;
        this.textKey = textKey;
        this.amount = initAmount;
        
        addScrollBar(scrollBar = new ExtendedScrollBar<>(gui, ScrollBar.Size.LONG, 160, 18, START_X,
                VISIBLE_MOBS, () -> PickMobMenu.this.mobs));
        
        addTextBox(new NumberTextBox(gui, 180, 150, Translator.translatable("hqm." + textKey + ".reqKills"), () -> amount, value -> amount = value));
        
        addTextBox(new TextBox(gui, "", 250, 18, false) {
            @Override
            public void textChanged() {
                super.textChanged();
                updateMobs(getText());
            }
        });
    
        addClickable(new ArrowSelectionHelper(gui, 180, 70) {
            @Override
            protected boolean isArrowVisible() {
                return false;   //There is currently no precision for mobs. Change this if precision is ever added back
            }
        
            @Override
            protected void onArrowClick(boolean left) {
//              mob.setExact(!mob.isExact());
            }
        
            @Override
            protected FormattedText getArrowText() {
                return Translator.translatable("hqm." + textKey + "." + "type" + "Match.title");
            }
        
            @Override
            protected FormattedText getArrowDescription() {
                return Translator.translatable("hqm." + textKey + "." + "type" + "Match.desc");
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
    public void draw(PoseStack matrices, int mX, int mY) {
        super.draw(matrices, mX, mY);
        
        ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        
        int mobY = START_Y;
        for (Entry entry : scrollBar.getVisibleEntries()) {
            boolean selected = entry.equals(mob);
            boolean inBounds = gui.inBounds(START_X, mobY, 130, 6, mX, mY);
            
            gui.drawString(matrices, entry.description, START_X, mobY, 0.7F, selected ? inBounds ? 0xC0C0C0 : 0xA0A0A0 : inBounds ? 0x707070 : 0x404040);
            mobY += OFFSET_Y;
        }
        
        gui.drawString(matrices, Translator.translatable("hqm." + textKey + ".search"), 180, 20, 0x404040);
        gui.drawString(matrices, Translator.translatable("hqm." + textKey + "." + (mob == null ? "nothing" : "currently") + "Selected"), 180, 40, 0x404040);
        if (mob != null) {
            gui.drawString(matrices, mob.description, 180, 50, 0.7F, 0x404040);
        }
    }
    
    @Override
    public void onClick(int mX, int mY, int b) {
        super.onClick(mX, mY, b);
        
        int mobY = START_Y;
        for (Entry entry : scrollBar.getVisibleEntries()) {
            if (gui.inBounds(START_X, mobY, 130, 6, mX, mY)) {
                if (entry.equals(mob)) {
                    mob = null;
                } else {
                    mob = entry;
                }
                break;
            }
            mobY += OFFSET_Y;
        }
    }
    
    @Override
    public void save() {
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
