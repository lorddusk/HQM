package hardcorequesting.common.io;

import java.nio.file.Path;
import java.util.Optional;

public interface FileProvider {
    
    FileProvider EMPTY = new FileProvider() {
        @Override
        public Optional<String> get() {
            return Optional.empty();
        }
    
        @Override
        public void set(String str) {
            throw new UnsupportedOperationException("Can not save to empty provider.");
        }
    };
    
    Optional<String> get();
    
    void set(String str);
    
    class PathProvider implements FileProvider {
        private final Path path;
        
        public PathProvider(Path path) {
            this.path = path;
        }
        
        @Override
        public Optional<String> get() {
            return SaveHandler.load(path);
        }
        
        @Override
        public void set(String str) {
            SaveHandler.save(path, str);
        }
    }
    
    class StringProvider implements FileProvider {
        private final String s;
        
        public StringProvider(String str) {
            s = str;
        }
        
        @Override
        public Optional<String> get() {
            return Optional.of(s);
        }
        
        @Override
        public void set(String str) {
            throw new UnsupportedOperationException("Should not save with cached string.");
        }
    }
}
