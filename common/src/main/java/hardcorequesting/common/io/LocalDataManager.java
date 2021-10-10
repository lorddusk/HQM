package hardcorequesting.common.io;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class LocalDataManager implements DataReader {
    
    private final Map<String, String> tempPaths = Maps.newHashMap();
    
    @Override
    public Optional<String> read(String name) {
        return Optional.ofNullable(tempPaths.get(name));
    }
    
    public void provide(String path, String str) {
        tempPaths.put(path, str);
    }
    
    @Override
    public String toString() {
        return String.format("Network data with %d temp paths (%s)", tempPaths.size(), tempPaths.keySet().stream().sorted().collect(Collectors.joining(", ")));
    }
}
