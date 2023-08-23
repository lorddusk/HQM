package hardcorequesting.common.client.interfaces.graphic;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.bag.*;
import hardcorequesting.common.client.BookPage;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.EditBagTierMenu;
import hardcorequesting.common.client.interfaces.edit.WrappedTextMenu;
import hardcorequesting.common.client.interfaces.widget.ExtendedScrollBar;
import hardcorequesting.common.client.interfaces.widget.LargeButton;
import hardcorequesting.common.client.interfaces.widget.ScrollBar;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.FormattedText;

import java.util.ArrayList;
import java.util.List;

/**
 * A graphic element for displaying the main page for editing reward bag info.
 * From here, a page for editing a specific group can be shown.
 */
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
    private final ExtendedScrollBar<LootGroup> groupScroll;
    private final ExtendedScrollBar<GroupTier> tierScroll;
    
    {
        addClickable(new LargeButton(gui, "hqm.questBook.createGroup", 100, 175) {
            @Override
            public boolean isVisible() {
                return EditBagsGraphic.this.gui.getCurrentMode() == EditMode.CREATE;
            }
        
            @Override
            public void onClick() {
                LootGroup.add(new LootGroup(null));
                SaveHelper.add(EditType.GROUP_CREATE);
            }
        });
    
        addClickable(new LargeButton(gui, "hqm.questBook.createTier", 100, 200) {
            @Override
            public boolean isVisible() {
                return EditBagsGraphic.this.gui.getCurrentMode() == EditMode.CREATE;
            }
        
            @Override
            public void onClick() {
                GroupTierManager.getInstance().getTiers().add(new GroupTier("New Tier", TierColor.BLACK, 0, 0, 0, 0, 0));
                SaveHelper.add(EditType.TIER_CREATE);
            }
        });
    
    }
    
    private LootGroup selectedGroup;
    
    public EditBagsGraphic(BookPage.BagsPage page, GuiQuestBook gui) {
        super(gui, EditMode.NORMAL, EditMode.CREATE, EditMode.RENAME, EditMode.TIER, EditMode.DELETE);
        this.page = page;
    
        addScrollBar(groupScroll = new ExtendedScrollBar<>(gui, ScrollBar.Size.LONG, 160, 18, GROUPS_X,
                VISIBLE_GROUPS, () -> new ArrayList<>(LootGroup.getGroups().values())));
    
        addScrollBar(tierScroll = new ExtendedScrollBar<>(gui, ScrollBar.Size.LONG, 312, 18, TIERS_X,
                VISIBLE_TIERS, () -> GroupTierManager.getInstance().getTiers()));
    }
    
    @Override
    public void draw(GuiGraphics graphics, int mX, int mY) {
        super.draw(graphics, mX, mY);
        
        int yPos = TIERS_Y;
        for (GroupTier groupTier : tierScroll.getVisibleEntries()) {
            
            FormattedText str = groupTier.getName();
            boolean inBounds = gui.inBounds(TIERS_X, yPos, gui.getStringWidth(str), GuiQuestBook.TEXT_HEIGHT, mX, mY);
            int color = groupTier.getColor().getHexColor();
            if (inBounds) {
                color &= 0xFFFFFF;
                color |= 0xBB << 24;
                RenderSystem.enableBlend();
            }
            gui.drawString(graphics, str, TIERS_X, yPos, color);
            if (inBounds) {
                RenderSystem.disableBlend();
            }
            
            for (int j = 0; j < BagTier.values().length; j++) {
                BagTier bagTier = BagTier.values()[j];
                gui.drawCenteredString(graphics, Translator.text(groupTier.getWeights()[j] + "").withStyle(bagTier.getColor()),
                        TIERS_X + TIERS_SECOND_LINE_X + j * WEIGHT_SPACING,
                        yPos + TIERS_SECOND_LINE_Y, 0.7F,
                        WEIGHT_SPACING, 0, 0x404040);
            }
            yPos += TIERS_SPACING;
        }
        
        yPos = GROUPS_Y;
        for (LootGroup group : groupScroll.getVisibleEntries()) {
            
            FormattedText str = group.getDisplayName();
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
            
            gui.drawString(graphics, str, GROUPS_X, yPos, color);
            if (inBounds || selected) {
                RenderSystem.disableBlend();
            }
            
            gui.drawString(graphics, Translator.translatable("hqm.questBook.items", group.getItems().size()),
                    GROUPS_X + GROUPS_SECOND_LINE_X,
                    yPos + GROUPS_SECOND_LINE_Y,
                    0.7F, 0x404040);
            yPos += GROUPS_SPACING;
        }
    }
    
    @Override
    public void onClick(int mX, int mY, int button) {
        super.onClick(mX, mY, button);
        
        //Handle click on groups
        int posY = GROUPS_Y;
        for (LootGroup group : groupScroll.getVisibleEntries()) {
            
            if (gui.inBounds(GROUPS_X, posY, gui.getStringWidth(group.getDisplayName()), GuiQuestBook.TEXT_HEIGHT, mX, mY)) {
                switch (gui.getCurrentMode()) {
                    case TIER:
                        selectedGroup = (group == selectedGroup ? null : group);
                        break;
                    case NORMAL:
                        gui.setPage(page.forGroup(group));
                        break;
                    case RENAME:
                        WrappedTextMenu.displayWithOptionalResult(gui, group.getRawName(), true, text -> group.setName(text.orElse(null)));
                        break;
                    case DELETE:
                        LootGroup.remove(group.getId());
                        SaveHelper.add(EditType.GROUP_REMOVE);
                        break;
                    default:
                        break;
                }
                break;
            }
            posY += GROUPS_SPACING;
        }
    
        //Handle click on tiers
        posY = TIERS_Y;
        for (GroupTier groupTier : tierScroll.getVisibleEntries()) {
            
            if (gui.inBounds(TIERS_X, posY, gui.getStringWidth(groupTier.getName()), GuiQuestBook.TEXT_HEIGHT, mX, mY)) {
                switch (gui.getCurrentMode()) {
                    case TIER -> {
                        if (selectedGroup != null) {
                            selectedGroup.setTier(groupTier);
                            SaveHelper.add(EditType.GROUP_CHANGE);
                        }
                    }
                    case NORMAL -> EditBagTierMenu.display(gui, groupTier);
                    case RENAME -> WrappedTextMenu.display(gui, groupTier.getRawName(), 110, groupTier::setName);
                    case DELETE -> {
                        if (replaceUseOfTier(groupTier)) {
                            // With any uses of the tier replaced, we can now safely remove the tier
                            GroupTierManager.getInstance().getTiers().remove(groupTier);
                            SaveHelper.add(EditType.TIER_REMOVE);
                        }
                    }
                    default -> {
                    }
                }
                break;
            }
            posY += TIERS_SPACING;
        }
    }
    
    private static boolean replaceUseOfTier(GroupTier groupTier) {
        if (LootGroup.getGroups().size() > 0) {
            List<GroupTier> tiers = GroupTierManager.getInstance().getTiers();
            if (tiers.size() <= 1) {
                return false;  // The last tier cannot be removed if there are groups left and no replacement tier
            } else {
                GroupTier replacementTier = tiers.get(0);
                if (replacementTier == groupTier)
                    replacementTier = tiers.get(1);
                for (LootGroup group : LootGroup.getGroups().values()) {
                    if (group.getTier() == groupTier) {
                        group.setTier(replacementTier);
                    }
                }
            }
        }
        return true;
    }
    
    @Override
    protected void setEditMode(EditMode mode) {
        if (mode != EditMode.TIER)
            selectedGroup = null;
        super.setEditMode(mode);
    }
}