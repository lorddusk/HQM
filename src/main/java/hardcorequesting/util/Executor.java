package hardcorequesting.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

public class Executor {
    private Executor() {}
    
    public static <T> T call(Supplier<Callable<T>> client, Supplier<Callable<T>> server) {
        try {
            return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? client.get().call() : server.get().call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
