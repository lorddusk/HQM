package hardcorequesting.bag;

import hardcorequesting.client.interfaces.GuiColor;
import hardcorequesting.client.interfaces.GuiQuestBook;
import hardcorequesting.client.interfaces.ScrollBar;
import hardcorequesting.client.interfaces.edit.GuiEditMenuTextEditor;
import hardcorequesting.client.interfaces.edit.GuiEditMenuTier;
import hardcorequesting.io.SaveHandler;
import hardcorequesting.quests.QuestLine;
import hardcorequesting.util.SaveHelper;
import hardcorequesting.util.Translator;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class GroupTier {
    private String name;
    private GuiColor color;
    private int[] weights;

    public GroupTier(String name, GuiColor color, int... weights) {
        this.name = name;
        this.color = color;
        this.weights = Arrays.copyOf(weights, weights.length);
    }

    public String getName() {
        return name == null || name.equals("") ? Translator.translate("hqm.bag.unknown") : name;
    }

    public GuiColor getColor() {
        return color;
    }

    public int[] getWeights() {
        return weights;
    }

    public static List<GroupTier> getTiers() {
        return QuestLine.getActiveQuestLine().tiers;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GroupTier copy() {
        return new GroupTier(getName(), getColor(), getWeights());
    }

    public void load(GroupTier tier) {
        this.name = tier.name;
        this.color = tier.color;
        this.weights = Arrays.copyOf(tier.weights, tier.weights.length);
    }

    public void setColor(GuiColor color) {
        this.color = color;
    }

    public static void saveAll() {
        try {
            SaveHandler.saveBags(SaveHandler.getLocalFile("bags"));
        } catch (IOException e) {
            FMLLog.log("HQM", Level.INFO, "Failed to save bags");
        }
    }

    public static void loadAll() {
        try {
            Group.getGroups().clear();
            GroupTier.getTiers().clear();
            GroupTier.getTiers().addAll(SaveHandler.loadBags(SaveHandler.getLocalFile("bags")));
        } catch (IOException e) {
            FMLLog.log("HQM", Level.INFO, "Failed to save bags");
        }
    }

    @SideOnly(Side.CLIENT)
    public static void mouseClickedOverview(GuiQuestBook gui, ScrollBar tierScroll, int x, int y) {
        List<GroupTier> tiers = GroupTier.getTiers();
        int start = tierScroll.isVisible(gui) ? Math.round((tiers.size() - GuiQuestBook.VISIBLE_TIERS) * tierScroll.getScroll()) : 0;
        for (int i = start; i < Math.min(start + GuiQuestBook.VISIBLE_TIERS, tiers.size()); i++) {
            GroupTier groupTier = tiers.get(i);

            int posY = GuiQuestBook.TIERS_Y + GuiQuestBook.TIERS_SPACING * (i - start);
            if (gui.inBounds(GuiQuestBook.TIERS_X, posY, gui.getStringWidth(groupTier.getName()), GuiQuestBook.TEXT_HEIGHT, x, y)) {
                switch (gui.getCurrentMode()) {
                    case TIER:
                        if (gui.modifyingGroup != null) {
                            gui.modifyingGroup.setTier(groupTier);
                            SaveHelper.add(SaveHelper.EditType.GROUP_CHANGE);
                        }
                        break;
                    case NORMAL:
                        gui.setEditMenu(new GuiEditMenuTier(gui, gui.getPlayer(), groupTier));
                        break;
                    case RENAME:
                        gui.setEditMenu(new GuiEditMenuTextEditor(gui, gui.getPlayer(), groupTier));
                        break;
                    case DELETE:
                        if (tiers.size() > 1 || Group.getGroups().size() == 0) {
                            for (Group group : Group.getGroups().values()) {
                                if (group.getTier() == groupTier) {
                                    group.setTier(i == 0 ? tiers.get(1) : tiers.get(0));
                                }
                            }
                            tiers.remove(i);
                            SaveHelper.add(SaveHelper.EditType.TIER_REMOVE);
                        }
                        break;
                    default:
                        break;
                }
                break;
            }
        }
    }

    public static void initBaseTiers(QuestLine questLine) {
        questLine.tiers.add(new GroupTier("Crap", GuiColor.RED, 50, 50, 50, 5, 0));
        questLine.tiers.add(new GroupTier("Plain", GuiColor.GRAY, 50, 50, 50, 30, 10));
        questLine.tiers.add(new GroupTier("Common", GuiColor.GREEN, 20, 30, 40, 30, 20));
        questLine.tiers.add(new GroupTier("Uncommon", GuiColor.BLUE, 5, 10, 15, 20, 25));
        questLine.tiers.add(new GroupTier("Rare", GuiColor.ORANGE, 3, 6, 12, 18, 21));
        questLine.tiers.add(new GroupTier("Unique", GuiColor.PURPLE, 1, 2, 3, 4, 30));
    }
}
