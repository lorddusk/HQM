package hardcorequesting.common.client.interfaces.graphic;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.bag.LootGroup;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.PickItemMenu;
import hardcorequesting.common.client.interfaces.widget.NumberTextBox;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * A graphic element for displaying the page for editing a specific reward group.
 */
@Environment(EnvType.CLIENT)
public class EditGroupGraphic extends EditableGraphic {
    private static final int GROUP_ITEMS_X = 20;
    private static final int GROUP_ITEMS_Y = 40;
    private static final int GROUP_ITEMS_SPACING = 20;
    private static final int ITEMS_PER_LINE = 7;
    
    private final LootGroup group;
    
    public EditGroupGraphic(GuiQuestBook gui, LootGroup group) {
        super(gui, EditMode.NORMAL, EditMode.ITEM, EditMode.DELETE);
        this.group = group;
    
        addTextBox(new NumberTextBox(gui, 180, 30, FormattedText.EMPTY, false, 3, group::getLimit, group::setLimit));
    }
    
    @Override
    public void draw(GuiGraphics graphics, int mX, int mY) {
        super.draw(graphics, mX, mY);
        
        gui.drawString(graphics, group.getDisplayName(), EditBagsGraphic.GROUPS_X, EditBagsGraphic.GROUPS_Y, group.getTier().getColor().getHexColor());
        List<ItemStack> items = new ArrayList<>(group.getItems());
        items.add(ItemStack.EMPTY);
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            
            int xPos = (i % ITEMS_PER_LINE) * GROUP_ITEMS_SPACING + GROUP_ITEMS_X;
            int yPos = (i / ITEMS_PER_LINE) * GROUP_ITEMS_SPACING + GROUP_ITEMS_Y;
            
            gui.drawItemStack(graphics, stack, xPos, yPos, mX, mY, false);
        }
        
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            
            int xPos = (i % ITEMS_PER_LINE) * GROUP_ITEMS_SPACING + GROUP_ITEMS_X;
            int yPos = (i / ITEMS_PER_LINE) * GROUP_ITEMS_SPACING + GROUP_ITEMS_Y;
            
            if (gui.inBounds(xPos, yPos, GuiQuestBook.ITEM_SIZE, GuiQuestBook.ITEM_SIZE, mX, mY)) {
                if (!stack.isEmpty()) {
                    try {
                        graphics.renderTooltip(this.gui.getFont(), stack, mX + gui.getLeft(), mY + gui.getTop());
                    } catch (Exception ignored) {
                    }
                }
                break;
            }
        }
        
        gui.drawString(graphics, Translator.translatable("hqm.questBook.maxRetrieval"), 180, 20, 0x404040);
        gui.drawString(graphics, Translator.translatable("hqm.questBook.noRestriction"), 180, 48, 0.7F, 0x404040);
    }
    
    @Override
    public void onClick(int mX, int mY, int b) {
        super.onClick(mX, mY, b);
        
        List<ItemStack> items = new ArrayList<>(group.getItems());
        items.add(ItemStack.EMPTY);
        for (int i = 0; i < items.size(); i++) {
            int xPos = (i % ITEMS_PER_LINE) * GROUP_ITEMS_SPACING + GROUP_ITEMS_X;
            int yPos = (i / ITEMS_PER_LINE) * GROUP_ITEMS_SPACING + GROUP_ITEMS_Y;
            
            if (gui.inBounds(xPos, yPos, GuiQuestBook.ITEM_SIZE, GuiQuestBook.ITEM_SIZE, mX, mY)) {
                if (gui.getCurrentMode() == EditMode.ITEM) {
                    ItemStack stack = i < items.size() ? items.get(i) : ItemStack.EMPTY;
                    int amount;
                    if (!stack.isEmpty()) {
                        stack = stack.copy();
                        amount = stack.getCount();
                    } else {
                        amount = 1;
                    }
                    
                    final int id = i;
                    PickItemMenu.display(gui, stack, PickItemMenu.Type.ITEM, amount,
                            result -> group.setItem(id, result.getWithAmount()));
                    
                } else if (gui.getCurrentMode() == EditMode.DELETE) {
                    group.removeItem(i);
                    SaveHelper.add(EditType.GROUP_ITEM_REMOVE);
                }
                break;
            }
        }
    }
}
