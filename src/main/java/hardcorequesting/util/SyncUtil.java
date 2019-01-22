package hardcorequesting.util;

import java.util.ArrayList;
import java.util.List;

public class SyncUtil {
    public static List<String> splitData (String input, int splitSize) {
        List<String> output = new ArrayList<>();

        int len = input.length();

        for (int i = 0; i < len; i += splitSize) {
            output.add(input.substring(i, Math.min(len, i+splitSize)));
        }

        return output;
    }

    public static String joinData (List<String> input) {
        return joinData(input, "");
    }

    public static String joinData (List<String> input, String joiner) {
        return String.join(joiner, input);
    }
}
