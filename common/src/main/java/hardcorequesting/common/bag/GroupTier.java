package hardcorequesting.common.bag;

import hardcorequesting.common.client.interfaces.GuiColor;
import hardcorequesting.common.quests.QuestLine;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.I18n;

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
    
    public static void initBaseTiers(QuestLine questLine) {
        List<GroupTier> tiers = questLine.groupTierManager.getTiers();
        tiers.add(new GroupTier("Crap", GuiColor.RED, 50, 50, 50, 5, 0));
        tiers.add(new GroupTier("Plain", GuiColor.GRAY, 50, 50, 50, 30, 10));
        tiers.add(new GroupTier("Common", GuiColor.GREEN, 20, 30, 40, 30, 20));
        tiers.add(new GroupTier("Uncommon", GuiColor.BLUE, 5, 10, 15, 20, 25));
        tiers.add(new GroupTier("Rare", GuiColor.ORANGE, 3, 6, 12, 18, 21));
        tiers.add(new GroupTier("Unique", GuiColor.PURPLE, 1, 2, 3, 4, 30));
    }
    
    @Environment(EnvType.CLIENT)
    public String getName() {
        return name == null || name.equals("") ? I18n.get("hqm.bag.unknown") : name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getRawName() {
        return name;
    }
    
    public GuiColor getColor() {
        return color;
    }
    
    public void setColor(GuiColor color) {
        this.color = color;
    }
    
    public int[] getWeights() {
        return weights;
    }
    
    public GroupTier copy() {
        return new GroupTier(name, getColor(), getWeights());
    }
    
    public void load(GroupTier tier) {
        this.name = tier.name;
        this.color = tier.color;
        this.weights = Arrays.copyOf(tier.weights, tier.weights.length);
    }
}
