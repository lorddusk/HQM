package hardcorequesting.common.reputation;


import hardcorequesting.common.util.WrappedText;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ReputationMarker implements Comparable<ReputationMarker> {
    
    private WrappedText name;
    private int value;
    private final boolean neutral;
    private int id;
    
    public ReputationMarker(WrappedText name, int value, boolean neutral) {
        this.name = name;
        this.value = value;
        this.neutral = neutral;
    }
    
    public MutableComponent getName() {
        return name.getText();
    }
    
    public WrappedText getRawName() {
        return name;
    }
    
    public void setName(WrappedText name) {
        this.name = name;
    }
    
    public int getValue() {
        return value;
    }
    
    public void setValue(int value) {
        this.value = value;
    }
    
    @Override
    public int compareTo(ReputationMarker o) {
        return Integer.compare(value, o.value);
    }
    
    public MutableComponent getTitle() {
        return this.getName().append(": " + value);
    }
    
    public boolean isNeutral() {
        return neutral;
    }
    
    public MutableComponent getLabel() {
        return this.getName().append(" (" + value + ")");
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
}
