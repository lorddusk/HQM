package hardcorequesting.client.interfaces.edit;

import hardcorequesting.client.interfaces.*;
import hardcorequesting.quests.task.QuestTaskTame;
import hardcorequesting.util.Translator;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GuiEditMenuTame extends GuiEditMenuExtended {

    private static final int START_X = 20;
    private static final int START_Y = 20;
    private static final int OFFSET_Y = 8;
    private static final int VISIBLE_MOBS = 24;
    private QuestTaskTame task;
    private QuestTaskTame.Tame tame;
    private int id;
    private ScrollBar scrollBar;
    private List<String> rawTames;
    private List<String> tames;

    public GuiEditMenuTame(GuiQuestBook gui, QuestTaskTame task, final QuestTaskTame.Tame tame, int id, EntityPlayer player) {
        super(gui, player, false, 180, 70, 180, 150);
        this.task = task;
        this.tame = tame;
        this.id = id;

        scrollBar = new ScrollBar(160, 18, 186, 171, 69, START_X) {
            @Override
            public boolean isVisible(GuiBase gui) {
                return tames.size() > VISIBLE_MOBS;
            }
        };

        textBoxes.add(new TextBoxNumber(gui, 0, "hqm.tameTask.reqKills") {
            @Override
            protected int getValue() {
                return tame.getCount();
            }

            @Override
            protected void setValue(int number) {
                tame.setCount(number);
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

        for (Map.Entry<ResourceLocation, EntityEntry> entry : ForgeRegistries.ENTITIES.getEntries()) {
            Class<?> clazz = entry.getValue().getEntityClass();
            if (EntityLivingBase.class.isAssignableFrom(clazz)) {
                rawTames.add(entry.getKey().toString());
            }
        }

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
    public void draw(GuiBase gui, int mX, int mY) {
        super.draw(gui, mX, mY);

        ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        scrollBar.draw(gui);

        int start = scrollBar.isVisible(gui) ? Math.round((tames.size() - VISIBLE_MOBS) * scrollBar.getScroll()) : 0;
        int end = Math.min(tames.size(), start + VISIBLE_MOBS);
        for (int i = start; i < end; i++) {
            boolean selected = tames.get(i).equals(tame.getTame());
            boolean inBounds = gui.inBounds(START_X, START_Y + (i - start) * OFFSET_Y, 130, 6, mX, mY);

            gui.drawString(tames.get(i), START_X, START_Y + OFFSET_Y * (i - start), 0.7F, selected ? inBounds ? 0xC0C0C0 : 0xA0A0A0 : inBounds ? 0x707070 : 0x404040);
        }

        gui.drawString(Translator.translate("hqm.tameTask.search"), 180, 20, 0x404040);
        gui.drawString(Translator.translate("hqm.tameTask." + (tame.getTame() == null ? "nothing" : "currently") + "Selected"), 180, 40, 0x404040);
        if (tame.getTame() != null) {
            gui.drawString(tame.getTame(), 180, 50, 0.7F, 0x404040);
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

                if (tames.get(i).equals(tame.getTame())) {
                    tame.setTame(null);
                } else {
                    tame.setTame(tames.get(i));
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
        tame.setExact(!tame.isExact());
    }

    @Override
    protected String getArrowText() {
        return Translator.translate("hqm.tameTask." + (tame.isExact() ? "exact" : "type") + "Match.title");
    }

    @Override
    protected String getArrowDescription() {
        return Translator.translate("hqm.tameTask." + (tame.isExact() ? "exact" : "type") + "Match.desc");
    }

    @Override
    public void onDrag(GuiBase gui, int mX, int mY) {
        super.onDrag(gui, mX, mY);
        scrollBar.onDrag(gui, mX, mY);
    }

    @Override
    public void onScroll(GuiBase gui, int mX, int mY, int scroll) {
        super.onScroll(gui, mX, mY, scroll);
        scrollBar.onScroll(gui, mX, mY, scroll);
    }

    @Override
    public void save(GuiBase gui) {
        tame.setCount(Math.max(1, tame.getCount()));

        if ((tame.getIconStack() == null || tame.getIconStack().getItem() == Items.SPAWN_EGG) && tame.getTame() != null) {
            if (EntityList.ENTITY_EGGS.containsKey(new ResourceLocation(tame.getTame()))) {
                ItemStack stack = new ItemStack(net.minecraft.init.Items.SPAWN_EGG);
                ItemMonsterPlacer.applyEntityIdToItemStack(stack, new ResourceLocation(tame.getTame()));
            }
        }

        task.setTame(id, tame, player);
    }
}
