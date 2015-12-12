package hardcorequesting.reputation;


public class ReputationMarker implements Comparable<ReputationMarker> {
    private String name;
    private int value;
    private boolean neutral;
    private int id;

    public ReputationMarker(String name, int value, boolean neutral) {
        this.name = name;
        this.value = value;
        this.neutral = neutral;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
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
        return ((Integer) value).compareTo(o.value);
    }

    public String getTitle() {
        return name + ": " + value;
    }

    public boolean isNeutral() {
        return neutral;
    }

    public void setNeutral(boolean neutral) {
        this.neutral = neutral;
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
