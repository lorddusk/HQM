package hardcorequesting.client.interfaces.edit;

import hardcorequesting.client.interfaces.*;
import hardcorequesting.quests.task.QuestTaskMob;
import hardcorequesting.util.Translator;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GuiEditMenuMob extends GuiEditMenuExtended {

    private QuestTaskMob task;
    private QuestTaskMob.Mob mob;
    private int id;
    private ScrollBar scrollBar;

    private List<String> rawMobs;
    private List<String> mobs;

    public GuiEditMenuMob(GuiQuestBook gui, QuestTaskMob task, final QuestTaskMob.Mob mob, int id, EntityPlayer player) {
        super(gui, player, false, 180, 70, 180, 150);
        this.task = task;
        this.mob = mob;
        this.id = id;

        scrollBar = new ScrollBar(160, 18, 186, 171, 69, START_X) {
            @Override
            public boolean isVisible(GuiBase gui) {
                return mobs.size() > VISIBLE_MOBS;
            }
        };

        textBoxes.add(new TextBoxNumber(gui, 0, "hqm.mobTask.reqKills") {
            @Override
            protected void setValue(int number) {
                mob.setCount(number);
            }

            @Override
            protected int getValue() {
                return mob.getCount();
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

        for (Map.Entry<ResourceLocation, EntityEntry> entry : ForgeRegistries.ENTITIES.getEntries()) {
            if (EntityLivingBase.class.isAssignableFrom(entry.getValue().getEntityClass())) {
                rawMobs.add(entry.getValue().getName());
            }
        }

        Collections.sort(rawMobs);
        updateMobs("");
    }

    private void updateMobs(String search) {
        if (mobs != null) {
            mobs.clear();
            for (String rawMob : rawMobs) {
                if (rawMob.toLowerCase().contains(search.toLowerCase())) {
                    mobs.add(rawMob);
                }
            }
        }
    }

    private static final int START_X = 20;
    private static final int START_Y = 20;
    private static final int OFFSET_Y = 8;
    private static final int VISIBLE_MOBS = 24;

    @Override
    public void draw(GuiBase gui, int mX, int mY) {
        super.draw(gui, mX, mY);

        ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        scrollBar.draw(gui);

        int start = scrollBar.isVisible(gui) ? Math.round((mobs.size() - VISIBLE_MOBS) * scrollBar.getScroll()) : 0;
        int end = Math.min(mobs.size(), start + VISIBLE_MOBS);
        for (int i = start; i < end; i++) {
            boolean selected = mobs.get(i).equals(mob.getMob());
            boolean inBounds = gui.inBounds(START_X, START_Y + (i - start) * OFFSET_Y, 130, 6, mX, mY);

            gui.drawString(mobs.get(i), START_X, START_Y + OFFSET_Y * (i - start), 0.7F, selected ? inBounds ? 0xC0C0C0 : 0xA0A0A0 : inBounds ? 0x707070 : 0x404040);
        }

        gui.drawString(Translator.translate("hqm.mobTask.search"), 180, 20, 0x404040);
        gui.drawString(Translator.translate("hqm.mobTask." + (mob.getMob() == null ? "nothing" : "currently") + "Selected"), 180, 40, 0x404040);
        if (mob.getMob() != null) {
            gui.drawString(mob.getMob(), 180, 50, 0.7F, 0x404040);
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

                if (mobs.get(i).equals(mob.getMob())) {
                    mob.setMob(null);
                } else {
                    mob.setMob(mobs.get(i));
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
        mob.setExact(!mob.isExact());
    }

    @Override
    protected String getArrowText() {
        return Translator.translate("hqm.mobTask." + (mob.isExact() ? "exact" : "type") + "Match.title");
    }

    @Override
    protected String getArrowDescription() {
        return Translator.translate("hqm.mobTask." + (mob.isExact() ? "exact" : "type") + "Match.desc");
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
        mob.setCount(Math.max(1, mob.getCount()));

        if ((mob.getIcon() == null || mob.getIcon().getItem() == Items.SPAWN_EGG) && mob.getMob() != null) {
            if (EntityList.ENTITY_EGGS.containsKey(new ResourceLocation(mob.getMob()))){
                ItemStack stack = new ItemStack(net.minecraft.init.Items.SPAWN_EGG);
                ItemMonsterPlacer.applyEntityIdToItemStack(stack, new ResourceLocation(mob.getMob()));
            }
        }

        task.setMob(id, mob, player);
    }
}
