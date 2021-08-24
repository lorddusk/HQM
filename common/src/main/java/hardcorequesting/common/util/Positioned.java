package hardcorequesting.common.util;

/**
 * Wrapper to pair screen coords info with an object.
 */
public class Positioned<T> {
    private final int x, y;
    private final T element;
    
    public Positioned(int x, int y, T element) {
        this.x = x;
        this.y = y;
        this.element = element;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public T getElement() {
        return element;
    }
}
