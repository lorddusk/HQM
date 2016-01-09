package hardcorequesting.bag;


import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import hardcorequesting.FileVersion;
import hardcorequesting.SaveHelper;
import hardcorequesting.Translator;
import hardcorequesting.client.interfaces.*;
import hardcorequesting.network.DataBitHelper;
import hardcorequesting.network.DataReader;
import hardcorequesting.network.DataWriter;
import hardcorequesting.quests.QuestLine;

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
        this.weights = Arrays.copyOf(weights, weights.length);
    }

    public void setColor(GuiColor color) {
        this.color = color;
    }

    public static void saveAll(DataWriter dw) {
        dw.writeData(QuestLine.getActiveQuestLine().tiers.size(), DataBitHelper.TIER_COUNT);
        for (GroupTier tier : QuestLine.getActiveQuestLine().tiers) {
            dw.writeString(tier.getName(), DataBitHelper.QUEST_NAME_LENGTH);
            dw.writeData(tier.getColor().ordinal(), DataBitHelper.COLOR);
            for (int weight : tier.weights) {
                dw.writeData(weight, DataBitHelper.WEIGHT);
            }
        }
    }

    public static void readAll(DataReader dr, FileVersion version) {
        QuestLine.getActiveQuestLine().tiers.clear();
        int count = dr.readData(DataBitHelper.TIER_COUNT);
        for (int i = 0; i < count; i++) {
            String name = dr.readString(DataBitHelper.QUEST_NAME_LENGTH);
            GuiColor color = GuiColor.values()[dr.readData(DataBitHelper.COLOR)];
            int[] weights = new int[BagTier.values().length];
            for (int j = 0; j < weights.length; j++) {
                weights[j] = dr.readData(DataBitHelper.WEIGHT);
            }

            QuestLine.getActiveQuestLine().tiers.add(new GroupTier(name, color, weights));
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
                            for (Group group : Group.getGroups()) {
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
}
