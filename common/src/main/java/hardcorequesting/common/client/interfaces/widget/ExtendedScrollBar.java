package hardcorequesting.common.client.interfaces.widget;

import hardcorequesting.common.client.interfaces.GuiBase;

import java.util.List;
import java.util.function.Supplier;

public class ExtendedScrollBar<T> extends ScrollBar {
    
    private final Supplier<List<T>> listSupplier;
    private final int visibleEntries;
    
    public ExtendedScrollBar(GuiBase gui, Size size, int x, int y, int left, int visibleEntries, Supplier<List<T>> listSupplier) {
        super(gui, size, x, y, left);
        this.listSupplier = listSupplier;
        this.visibleEntries = visibleEntries;
    }
    
    @Override
    public boolean isVisible() {
        List<T> list = listSupplier.get();
        return list != null && list.size() > visibleEntries;
    }
    
    public List<T> getVisibleEntries() {
        List<T> list = listSupplier.get();
        return list == null ? null : this.getVisibleEntries(list, visibleEntries);
    }
}
