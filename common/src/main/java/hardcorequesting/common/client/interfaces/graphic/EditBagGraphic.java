package hardcorequesting.common.client.interfaces.graphic;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.bag.BagTier;
import hardcorequesting.common.bag.Group;
import hardcorequesting.common.bag.GroupTier;
import hardcorequesting.common.bag.GroupTierManager;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenuTier;
import hardcorequesting.common.client.interfaces.edit.TextMenu;
import hardcorequesting.common.client.interfaces.widget.ScrollBar;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.FormattedText;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class EditBagGraphic {
    
    public static void draw(PoseStack matrices, GuiQuestBook gui, ScrollBar tierScroll, ScrollBar groupScroll, int x, int y) {
        List<GroupTier> tiers = GroupTierManager.getInstance().getTiers();
        int start = tierScroll.isVisible(gui) ? Math.round((tiers.size() - GuiQuestBook.VISIBLE_TIERS) * tierScroll.getScroll()) : 0;
        for (int i = start; i < Math.min(start + GuiQuestBook.VISIBLE_TIERS, tiers.size()); i++) {
            GroupTier groupTier = tiers.get(i);
            
            String str = groupTier.getName();
            int yPos = GuiQuestBook.TIERS_Y + GuiQuestBook.TIERS_SPACING * (i - start);
            boolean inBounds = gui.inBounds(GuiQuestBook.TIERS_X, yPos, gui.getStringWidth(str), GuiQuestBook.TEXT_HEIGHT, x, y);
            int color = groupTier.getColor().getHexColor();
            if (inBounds) {
                color &= 0xFFFFFF;
                color |= 0xBB << 24;
                RenderSystem.enableBlend();
            }
            gui.drawString(matrices, Translator.plain(str), GuiQuestBook.TIERS_X, yPos, color);
            if (inBounds) {
                RenderSystem.disableBlend();
            }
            
            for (int j = 0; j < BagTier.values().length; j++) {
                BagTier bagTier = BagTier.values()[j];
                gui.drawCenteredString(matrices, Translator.text(groupTier.getWeights()[j] + "", bagTier.getColor()),
                        GuiQuestBook.TIERS_X + GuiQuestBook.TIERS_SECOND_LINE_X + j * GuiQuestBook.WEIGHT_SPACING,
                        yPos + GuiQuestBook.TIERS_SECOND_LINE_Y, 0.7F,
                        GuiQuestBook.WEIGHT_SPACING, 0, 0x404040);
            }
        }
        
        List<Group> groups = new ArrayList<>(Group.getGroups().values());
        start = groupScroll.isVisible(gui) ? Math.round((groups.size() - GuiQuestBook.VISIBLE_GROUPS) * groupScroll.getScroll()) : 0;
        for (int i = start; i < Math.min(start + GuiQuestBook.VISIBLE_GROUPS, groups.size()); i++) {
            Group group = groups.get(i);
            
            FormattedText str = Translator.plain(group.getDisplayName());
            int yPos = GuiQuestBook.GROUPS_Y + GuiQuestBook.GROUPS_SPACING * (i - start);
            boolean inBounds = gui.inBounds(GuiQuestBook.GROUPS_X, yPos, gui.getStringWidth(str), GuiQuestBook.TEXT_HEIGHT, x, y);
            int color = group.getTier().getColor().getHexColor();
            boolean selected = group == gui.modifyingGroup;
            if (inBounds || selected) {
                color &= 0xFFFFFF;
                RenderSystem.enableBlend();
                
                if (selected) {
                    color |= 0x50 << 24;
                } else {
                    color |= 0xBB << 24;
                }
            }
            
            gui.drawString(matrices, str, GuiQuestBook.GROUPS_X, yPos, color);
            if (inBounds || selected) {
                RenderSystem.disableBlend();
            }
            
            gui.drawString(matrices, Translator.translatable("hqm.questBook.items", group.getItems().size()),
                    GuiQuestBook.GROUPS_X + GuiQuestBook.GROUPS_SECOND_LINE_X,
                    yPos + GuiQuestBook.GROUPS_SECOND_LINE_Y,
                    0.7F, 0x404040);
        }
    }
    
    public static void onClick(GuiQuestBook gui, ScrollBar tierScroll, ScrollBar groupScroll, int x, int y) {
        //Handle click on groups
        List<Group> groups = new ArrayList<>(Group.getGroups().values());
        int start = groupScroll.isVisible(gui) ? Math.round((groups.size() - GuiQuestBook.VISIBLE_GROUPS) * groupScroll.getScroll()) : 0;
        for (int i = start; i < Math.min(start + GuiQuestBook.VISIBLE_GROUPS, groups.size()); i++) {
            Group group = groups.get(i);
            
            int posY = GuiQuestBook.GROUPS_Y + GuiQuestBook.GROUPS_SPACING * (i - start);
            if (gui.inBounds(GuiQuestBook.GROUPS_X, posY, gui.getStringWidth(group.getDisplayName()), GuiQuestBook.TEXT_HEIGHT, x, y)) {
                switch (gui.getCurrentMode()) {
                    case TIER:
                        gui.modifyingGroup = (group == gui.modifyingGroup ? null : group);
                        break;
                    case NORMAL:
                        GuiQuestBook.selectedGroup = group;
                        gui.getTextBoxGroupAmount().setTextAndCursor(gui, String.valueOf(GuiQuestBook.getSelectedGroup().getLimit()));
                        break;
                    case RENAME:
                        TextMenu.display(gui, gui.getPlayer().getUUID(), group.getDisplayName(), true, group::setName);
                        break;
                    case DELETE:
                        Group.remove(group.getId());
                        SaveHelper.add(EditType.GROUP_REMOVE);
                        break;
                    default:
                        break;
                }
                break;
            }
        }
    
        //Handle click on tiers
        List<GroupTier> tiers = GroupTierManager.getInstance().getTiers();
        start = tierScroll.isVisible(gui) ? Math.round((tiers.size() - GuiQuestBook.VISIBLE_TIERS) * tierScroll.getScroll()) : 0;
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
                        gui.setEditMenu(new GuiEditMenuTier(gui, gui.getPlayer().getUUID(), groupTier));
                        break;
                    case RENAME:
                        TextMenu.display(gui, gui.getPlayer().getUUID(), groupTier.getName(), 110, groupTier::setName);
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
}