package hardcorequesting.common.util;

import hardcorequesting.common.HardcoreQuestingCore;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

public class Executor {
    private Executor() {}
    
    public static <T> T call(Supplier<Callable<T>> client, Supplier<Callable<T>> server) {
        try {
            return HardcoreQuestingCore.platform.isClient() ? client.get().call() : server.get().call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
