package hardcorequesting.common.reputation;


import hardcorequesting.common.util.WrappedText;

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
    
    public WrappedText getName() {
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
    
    public String getTitle() {
        return name + ": " + value;
    }
    
    public boolean isNeutral() {
        return neutral;
    }
    
    public String getLabel() {
        return name + " (" + value + ")";
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
}
