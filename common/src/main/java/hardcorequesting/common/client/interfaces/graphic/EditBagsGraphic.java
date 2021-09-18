package hardcorequesting.common.client.interfaces.graphic;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.bag.BagTier;
import hardcorequesting.common.bag.Group;
import hardcorequesting.common.bag.GroupTier;
import hardcorequesting.common.bag.GroupTierManager;
import hardcorequesting.common.client.BookPage;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiColor;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenuTier;
import hardcorequesting.common.client.interfaces.edit.TextMenu;
import hardcorequesting.common.client.interfaces.widget.LargeButton;
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
public class EditBagsGraphic extends EditableGraphic {
    public static final int TIERS_X = 180;
    public static final int TIERS_Y = 20;
    public static final int TIERS_SPACING = 25;
    public static final int TIERS_SECOND_LINE_X = -5;
    public static final int TIERS_SECOND_LINE_Y = 12;
    public static final int WEIGHT_SPACING = 25;
    public static final int VISIBLE_TIERS = 8;
    public static final int GROUPS_X = 20;
    public static final int GROUPS_Y = 20;
    public static final int GROUPS_SPACING = 25;
    public static final int GROUPS_SECOND_LINE_X = 5;
    public static final int GROUPS_SECOND_LINE_Y = 12;
    public static final int VISIBLE_GROUPS = 8;
    
    private final BookPage.BagsPage page;
    private final ScrollBar groupScroll;
    private final ScrollBar tierScroll;
    
    {
        addButton(new LargeButton("hqm.questBook.createGroup", 100, 175) {
            @Override
            public boolean isEnabled() {
                return true;
            }
        
            @Override
            public boolean isVisible() {
                return EditBagsGraphic.this.gui.getCurrentMode() == EditMode.CREATE;
            }
        
            @Override
            public void onClick(GuiBase gui) {
                Group.add(new Group(null));
                SaveHelper.add(EditType.GROUP_CREATE);
            }
        });
    
        addButton(new LargeButton("hqm.questBook.createTier", 100, 200) {
            @Override
            public boolean isEnabled() {
                return true;
            }
        
            @Override
            public boolean isVisible() {
                return EditBagsGraphic.this.gui.getCurrentMode() == EditMode.CREATE;
            }
        
            @Override
            public void onClick(GuiBase gui) {
                GroupTierManager.getInstance().getTiers().add(new GroupTier("New Tier", GuiColor.BLACK, 0, 0, 0, 0, 0));
                SaveHelper.add(EditType.TIER_CREATE);
            }
        });
    
    }
    
    private Group selectedGroup;
    
    public EditBagsGraphic(BookPage.BagsPage page, GuiQuestBook gui) {
        super(gui, EditMode.NORMAL, EditMode.CREATE, EditMode.RENAME, EditMode.TIER, EditMode.DELETE);
        this.page = page;
    
        addScrollBar(groupScroll = new ScrollBar(gui, 160, 18, 186, 171, 69, GROUPS_X) {
            @Override
            public boolean isVisible() {
                return Group.getGroups().size() > VISIBLE_GROUPS;
            }
        });
    
        addScrollBar(tierScroll = new ScrollBar(gui, 312, 18, 186, 171, 69, TIERS_X) {
            @Override
            public boolean isVisible() {
                return GroupTierManager.getInstance().getTiers().size() > VISIBLE_TIERS;
            }
        });
    }
    
    @Override
    public void draw(PoseStack matrices, GuiQuestBook gui, int mX, int mY) {
        super.draw(matrices, gui, mX, mY);
        
        List<GroupTier> tiers = GroupTierManager.getInstance().getTiers();
        int start = tierScroll.isVisible() ? Math.round((tiers.size() - VISIBLE_TIERS) * tierScroll.getScroll()) : 0;
        for (int i = start; i < Math.min(start + VISIBLE_TIERS, tiers.size()); i++) {
            GroupTier groupTier = tiers.get(i);
            
            String str = groupTier.getName();
            int yPos = TIERS_Y + TIERS_SPACING * (i - start);
            boolean inBounds = gui.inBounds(TIERS_X, yPos, gui.getStringWidth(str), GuiQuestBook.TEXT_HEIGHT, mX, mY);
            int color = groupTier.getColor().getHexColor();
            if (inBounds) {
                color &= 0xFFFFFF;
                color |= 0xBB << 24;
                RenderSystem.enableBlend();
            }
            gui.drawString(matrices, Translator.plain(str), TIERS_X, yPos, color);
            if (inBounds) {
                RenderSystem.disableBlend();
            }
            
            for (int j = 0; j < BagTier.values().length; j++) {
                BagTier bagTier = BagTier.values()[j];
                gui.drawCenteredString(matrices, Translator.text(groupTier.getWeights()[j] + "", bagTier.getColor()),
                        TIERS_X + TIERS_SECOND_LINE_X + j * WEIGHT_SPACING,
                        yPos + TIERS_SECOND_LINE_Y, 0.7F,
                        WEIGHT_SPACING, 0, 0x404040);
            }
        }
        
        List<Group> groups = new ArrayList<>(Group.getGroups().values());
        start = groupScroll.isVisible() ? Math.round((groups.size() - VISIBLE_GROUPS) * groupScroll.getScroll()) : 0;
        for (int i = start; i < Math.min(start + VISIBLE_GROUPS, groups.size()); i++) {
            Group group = groups.get(i);
            
            FormattedText str = Translator.plain(group.getDisplayName());
            int yPos = GROUPS_Y + GROUPS_SPACING * (i - start);
            boolean inBounds = gui.inBounds(GROUPS_X, yPos, gui.getStringWidth(str), GuiQuestBook.TEXT_HEIGHT, mX, mY);
            int color = group.getTier().getColor().getHexColor();
            boolean selected = group == selectedGroup;
            if (inBounds || selected) {
                color &= 0xFFFFFF;
                RenderSystem.enableBlend();
                
                if (selected) {
                    color |= 0x50 << 24;
                } else {
                    color |= 0xBB << 24;
                }
            }
            
            gui.drawString(matrices, str, GROUPS_X, yPos, color);
            if (inBounds || selected) {
                RenderSystem.disableBlend();
            }
            
            gui.drawString(matrices, Translator.translatable("hqm.questBook.items", group.getItems().size()),
                    GROUPS_X + GROUPS_SECOND_LINE_X,
                    yPos + GROUPS_SECOND_LINE_Y,
                    0.7F, 0x404040);
        }
    }
    
    @Override
    public void onClick(GuiQuestBook gui, int mX, int mY, int button) {
        super.onClick(gui, mX, mY, button);
        
        //Handle click on groups
        List<Group> groups = new ArrayList<>(Group.getGroups().values());
        int start = groupScroll.isVisible() ? Math.round((groups.size() - VISIBLE_GROUPS) * groupScroll.getScroll()) : 0;
        for (int i = start; i < Math.min(start + VISIBLE_GROUPS, groups.size()); i++) {
            Group group = groups.get(i);
            
            int posY = GROUPS_Y + GROUPS_SPACING * (i - start);
            if (gui.inBounds(GROUPS_X, posY, gui.getStringWidth(group.getDisplayName()), GuiQuestBook.TEXT_HEIGHT, mX, mY)) {
                switch (gui.getCurrentMode()) {
                    case TIER:
                        selectedGroup = (group == selectedGroup ? null : group);
                        break;
                    case NORMAL:
                        gui.setPage(page.forGroup(group));
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
        start = tierScroll.isVisible() ? Math.round((tiers.size() - VISIBLE_TIERS) * tierScroll.getScroll()) : 0;
        for (int i = start; i < Math.min(start + VISIBLE_TIERS, tiers.size()); i++) {
            GroupTier groupTier = tiers.get(i);
        
            int posY = TIERS_Y + TIERS_SPACING * (i - start);
            if (gui.inBounds(TIERS_X, posY, gui.getStringWidth(groupTier.getName()), GuiQuestBook.TEXT_HEIGHT, mX, mY)) {
                switch (gui.getCurrentMode()) {
                    case TIER:
                        if (selectedGroup != null) {
                            selectedGroup.setTier(groupTier);
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
    
    @Override
    protected void setEditMode(EditMode mode) {
        if (mode != EditMode.TIER)
            selectedGroup = null;
        super.setEditMode(mode);
    }
}