package hardcorequesting.common.io;

import com.google.common.collect.Maps;
import hardcorequesting.common.quests.SimpleSerializable;

import java.util.Map;
import java.util.stream.Collectors;

public class LocalDataManager implements DataManager {
    
    private final Map<String, FileProvider> tempPaths = Maps.newHashMap();
    
    @Override
    public FileProvider resolve(String name) {
        return tempPaths.getOrDefault(name, FileProvider.EMPTY);
    }
    
    @Override
    public FileProvider resolveData(String name) {
        return resolve(name);
    }
    
    public void provideTemp(SimpleSerializable serializable, String str) {
        provideTemp(serializable.filePath(), str);
    }
    
    public void provideTemp(String path, String str) {
        tempPaths.put(path, new FileProvider.StringProvider(str));
    }
    
    @Override
    public String toString() {
        return String.format("Network data with %d temp paths. (%s)", tempPaths.size(), tempPaths.keySet().stream().sorted().collect(Collectors.joining(", ")));
    }
}
