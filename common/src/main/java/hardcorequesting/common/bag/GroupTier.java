package hardcorequesting.common.bag;

import hardcorequesting.common.quests.QuestLine;
import hardcorequesting.common.util.Translator;
import hardcorequesting.common.util.WrappedText;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.List;

public class GroupTier {
    
    private WrappedText name;
    private TierColor color;
    private int[] weights;
    
    public GroupTier(String name, TierColor color, int... weights) {
        this(WrappedText.create(name), color, weights);
    }
    
    public GroupTier(WrappedText name, TierColor color, int... weights) {
        this.name = name;
        this.color = color;
        this.weights = Arrays.copyOf(weights, BagTier.values().length);
    }
    
    public static void initBaseTiers(QuestLine questLine) {
        List<GroupTier> tiers = questLine.groupTierManager.getTiers();
        tiers.add(new GroupTier("Crap", TierColor.RED, 50, 50, 50, 5, 0));
        tiers.add(new GroupTier("Plain", TierColor.GRAY, 50, 50, 50, 30, 10));
        tiers.add(new GroupTier("Common", TierColor.GREEN, 20, 30, 40, 30, 20));
        tiers.add(new GroupTier("Uncommon", TierColor.BLUE, 5, 10, 15, 20, 25));
        tiers.add(new GroupTier("Rare", TierColor.ORANGE, 3, 6, 12, 18, 21));
        tiers.add(new GroupTier("Unique", TierColor.PURPLE, 1, 2, 3, 4, 30));
    }
    
    @Environment(EnvType.CLIENT)
    public Component getName() {
        return name == null || name.getRawText().isEmpty() ? Translator.translatable("hqm.bag.unknown") : name.getText();
    }
    
    public void setName(WrappedText name) {
        this.name = name;
    }
    
    public WrappedText getRawName() {
        return name;
    }
    
    public TierColor getColor() {
        return color;
    }
    
    public void setColor(TierColor color) {
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
        this.weights = Arrays.copyOf(tier.weights, BagTier.values().length);
    }
}
