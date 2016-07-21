package hardcorequesting.util;

public class Tuple<X, Y> {

    public final X first;
    public final Y second;

    public Tuple(X first, Y second) {
        this.first = first;
        this.second = second;
    }

    public X getFirst() {
        return this.first;
    }

    public Y getSecond() {
        return this.getSecond();
    }

}
