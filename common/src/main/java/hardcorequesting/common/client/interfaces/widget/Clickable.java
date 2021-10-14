package hardcorequesting.common.client.interfaces.widget;

public interface Clickable {
    boolean onClick(int mX, int mY);
    
    default boolean onDrag(int mX, int mY) {
        return false;
    }
    
    default boolean onRelease(int mX, int mY) {
        return false;
    }
}