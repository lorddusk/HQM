package hardcorequesting.client.interfaces.edit;

import hardcorequesting.client.interfaces.GuiBase;
import hardcorequesting.quests.ItemPrecision;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class GuiEditMenuItemPortal extends GuiEditMenuItem {

    private GuiEditMenuPortal parent;

    public GuiEditMenuItemPortal(GuiBase gui, GuiEditMenuPortal parent, EntityPlayer player, ItemStack stack) {
        super(gui, player, !stack.isEmpty() ? stack.copy() : null, 0, Type.PORTAL, 1, ItemPrecision.PRECISE);

        this.parent = parent;
    }

    @Override
    public void save(GuiBase gui) {
        if (selected instanceof ElementItem && selected.getStack() != null) {
            parent.setItem((ItemStack) selected.getStack());
        }
    }

    @Override
    public void close(GuiBase gui) {
        gui.setEditMenu(parent);
    }
}
