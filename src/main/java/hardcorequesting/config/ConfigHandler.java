package hardcorequesting.config;

import cpw.mods.fml.common.FMLCommonHandler;
import hardcorequesting.quests.Quest;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class ConfigHandler
{
    public static Configuration syncConfig;

	public static void initModConfig(String configPath)
    {
       ModConfig.init(new File(configPath + "hqmconfig.cfg"));
        FMLCommonHandler.instance().bus().register(new ModConfig());
    }

    public static void initEditConfig(String configPath)
    {
        if (syncConfig == null)
        {
            syncConfig = new Configuration(new File(configPath + "editmode.cfg"));
            loadSyncConfig();
        }
    }

    public static void loadSyncConfig()
    {
        Quest.isEditing = syncConfig.get(Configuration.CATEGORY_GENERAL, EDITOR_KEY, EDITOR_DEFAULT, EDITOR_COMMENT).getBoolean(EDITOR_DEFAULT);
        if(syncConfig.hasChanged())
            syncConfig.save();
    }

    private static final String EDITOR_KEY = "UseEditor";
    private static final boolean EDITOR_DEFAULT = false;
    private static final String EDITOR_COMMENT = "Only use this as a map maker who wants to create quests. Leaving this off allows you the play the existing quests.";
}
