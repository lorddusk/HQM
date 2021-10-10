package hardcorequesting.common.io;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class LocalDataManager implements DataReader {
    
    private final Map<String, FileProvider> tempPaths = Maps.newHashMap();
    
    @Override
    public Optional<String> read(String name) {
        return tempPaths.getOrDefault(name, FileProvider.EMPTY).get();
    }
    
    @Override
    public Optional<String> readData(String name) {
        return read(name);
    }
    
    public void provideTemp(String path, String str) {
        tempPaths.put(path, new FileProvider.StringProvider(str));
    }
    
    @Override
    public String toString() {
        return String.format("Network data with %d temp paths (%s)", tempPaths.size(), tempPaths.keySet().stream().sorted().collect(Collectors.joining(", ")));
    }
}
