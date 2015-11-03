package hardcorequesting;

import net.minecraft.util.StatCollector;

public class Translator
{
    public static String translate(String id)
    {
        return StatCollector.translateToLocal(id);
    }

    public static String translate(String id, Object... args)
    {
        return StatCollector.translateToLocalFormatted(id, args);
    }
}
