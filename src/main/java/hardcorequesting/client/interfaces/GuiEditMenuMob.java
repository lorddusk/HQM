package hardcorequesting.client.interfaces;

import cpw.mods.fml.relauncher.ReflectionHelper;
import hardcorequesting.Translator;
import hardcorequesting.config.ModConfig;
import hardcorequesting.network.DataBitHelper;
import hardcorequesting.quests.QuestTaskMob;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class GuiEditMenuMob extends GuiEditMenuExtended {

    private static final int START_X = 20;
    private static final int START_Y = 20;
    private static final int OFFSET_Y = 8;
    private static final int VISIBLE_MOBS = 24;
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
            protected int getValue() {
                return mob.getCount();
            }

            @Override
            protected void setValue(int number) {
                mob.setCount(number);
            }
        });

        textBoxes.add(new TextBoxGroup.TextBox(gui, "", 250, 18, false) {
            @Override
            protected void textChanged(GuiBase gui) {
                super.textChanged(gui);
                updateMobs(getText());
            }
        });

        rawMobs = new ArrayList<String>();
        mobs = new ArrayList<String>();

        for (Object obj : EntityList.classToStringMapping.keySet()) {
            Class clazz = (Class) obj;
            if (EntityLivingBase.class.isAssignableFrom(clazz)) {
                rawMobs.add((String) EntityList.classToStringMapping.get(clazz));
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

    @Override
    public void draw(GuiBase gui, int mX, int mY) {
        super.draw(gui, mX, mY);

        ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
        GL11.glColor4f(1F, 1F, 1F, 1F);
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
    protected void save(GuiBase gui) {
        mob.setCount(Math.min(DataBitHelper.KILL_COUNT.getMaximum(), Math.max(1, mob.getCount())));

        if ((mob.getIcon() == null || mob.getIcon().getItem() == Items.spawn_egg) && mob.getMob() != null) {
            HashMap ids = ReflectionHelper.getPrivateValue(EntityList.class, null, 4);
            EntityList.EntityEggInfo info = (EntityList.EntityEggInfo) EntityList.entityEggs.get(ids.get(mob.getMob()));
            if (info != null) {
                mob.setIcon(new ItemStack(Items.spawn_egg, 1, info.spawnedID));
            }
        }

        task.setMob(id, mob, player);
    }
}
