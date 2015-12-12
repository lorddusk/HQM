package hardcorequesting.client.interfaces;

import hardcorequesting.quests.ItemPrecision;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class GuiEditMenuItemPortal extends GuiEditMenuItem {
    private GuiEditMenuPortal parent;

    public GuiEditMenuItemPortal(GuiBase gui, GuiEditMenuPortal parent, EntityPlayer player, ItemStack item) {
        super(gui, player, item == null ? null : item.copy(), 0, Type.PORTAL, 1, ItemPrecision.PRECISE);

        this.parent = parent;
    }

    @Override
    protected void save(GuiBase gui) {
        if (selected instanceof ElementItem && selected.getItem() != null) {
            parent.setItem((ItemStack) selected.getItem());
        }
    }

    @Override
    protected void close(GuiBase gui) {
        gui.setEditMenu(parent);
    }
}
