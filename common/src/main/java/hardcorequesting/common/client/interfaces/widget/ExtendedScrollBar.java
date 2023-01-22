package hardcorequesting.common.client.interfaces.widget;

import hardcorequesting.common.client.interfaces.GuiBase;

import java.util.List;
import java.util.function.Supplier;

public class ExtendedScrollBar<T> extends ScrollBar {
    
    private final Supplier<List<T>> listSupplier;
    private final int visibleRows, columns;
    
    public ExtendedScrollBar(GuiBase gui, Size size, int x, int y, int left, int visibleEntries, Supplier<List<T>> listSupplier) {
        this(gui, size, x, y, left, visibleEntries, 1, listSupplier);
    }
    
    public ExtendedScrollBar(GuiBase gui, Size size, int x, int y, int left, int visibleRows, int columns, Supplier<List<T>> listSupplier) {
        super(gui, size, x, y, left);
        this.listSupplier = listSupplier;
        this.visibleRows = visibleRows;
        this.columns = columns;
    }
    
    @Override
    public boolean isVisible() {
        List<T> list = listSupplier.get();
        return list != null && list.size() > visibleRows*columns;
    }
    
    public List<T> getVisibleEntries() {
        List<T> list = listSupplier.get();
        return list == null ? null : this.getVisibleEntries(list, columns, visibleRows);
    }
}
