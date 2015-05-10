package hardcorequesting.bag;


import hardcorequesting.FileVersion;
import hardcorequesting.client.interfaces.GuiColor;
import hardcorequesting.network.DataBitHelper;
import hardcorequesting.network.DataReader;
import hardcorequesting.network.DataWriter;
import hardcorequesting.quests.QuestLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GroupTier {


    private String name;
    private GuiColor color;
    private int[] weights;
    public GroupTier(String name, GuiColor color, int ... weights) {
        this.name = name;
        this.color = color;
        this.weights = Arrays.copyOf(weights, weights.length);
    }

    public String getName() {
        return name == null || name.equals("") ? "Unknown" : name;
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
}
