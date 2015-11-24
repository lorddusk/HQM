package hardcorequesting.config;

import cpw.mods.fml.common.FMLCommonHandler;
import hardcorequesting.client.KeyboardHandler;
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
        KeyboardHandler.fromConfig(syncConfig.get(Configuration.CATEGORY_GENERAL, KEYMAP_KEY, KEYMAP_DEFAULT, KEYMAP_COMMENT).getStringList());
        if(syncConfig.hasChanged())
            syncConfig.save();
    }

    private static final String EDITOR_KEY = "UseEditor";
    private static final boolean EDITOR_DEFAULT = false;
    private static final String EDITOR_COMMENT = "Only use this as a map maker who wants to create quests. Leaving this off allows you the play the existing quests.";

    private static final String KEYMAP_KEY = "KeyMap";
    private static final String[] KEYMAP_DEFAULT = KeyboardHandler.getDefault();
    private static final String KEYMAP_COMMENT = "Hotkeys used in the book, one entry per line(Format: [key]:[mode]";
}
