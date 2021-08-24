package hardcorequesting.common.bag;

import hardcorequesting.common.client.interfaces.GuiColor;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.ScrollBar;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenuTier;
import hardcorequesting.common.client.interfaces.edit.TextMenu;
import hardcorequesting.common.quests.QuestLine;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;
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
    
    @Environment(EnvType.CLIENT)
    public static void mouseClickedOverview(GuiQuestBook gui, ScrollBar tierScroll, int x, int y) {
        List<GroupTier> tiers = GroupTierManager.getInstance().getTiers();
        int start = tierScroll.isVisible(gui) ? Math.round((tiers.size() - GuiQuestBook.VISIBLE_TIERS) * tierScroll.getScroll()) : 0;
        for (int i = start; i < Math.min(start + GuiQuestBook.VISIBLE_TIERS, tiers.size()); i++) {
            GroupTier groupTier = tiers.get(i);
            
            int posY = GuiQuestBook.TIERS_Y + GuiQuestBook.TIERS_SPACING * (i - start);
            if (gui.inBounds(GuiQuestBook.TIERS_X, posY, gui.getStringWidth(groupTier.getName()), GuiQuestBook.TEXT_HEIGHT, x, y)) {
                switch (gui.getCurrentMode()) {
                    case TIER:
                        if (gui.modifyingGroup != null) {
                            gui.modifyingGroup.setTier(groupTier);
                            SaveHelper.add(EditType.GROUP_CHANGE);
                        }
                        break;
                    case NORMAL:
                        gui.setEditMenu(new GuiEditMenuTier(gui, gui.getPlayer(), groupTier));
                        break;
                    case RENAME:
                        TextMenu.display(gui, gui.getPlayer(), groupTier.getName(), 110, groupTier::setName);
                        break;
                    case DELETE:
                        if (tiers.size() > 1 || Group.getGroups().size() == 0) {
                            for (Group group : Group.getGroups().values()) {
                                if (group.getTier() == groupTier) {
                                    group.setTier(i == 0 ? tiers.get(1) : tiers.get(0));
                                }
                            }
                            tiers.remove(i);
                            SaveHelper.add(EditType.TIER_REMOVE);
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
    
    private void setName(String name) {
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
