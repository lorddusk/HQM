package hardcorequesting.config;

import com.google.common.collect.Lists;
import hardcorequesting.HardcoreQuesting;
import hardcorequesting.client.KeyboardHandler;
import hardcorequesting.quests.Quest;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ConfigHandler {

    private static final String EDITOR_KEY = "UseEditor";
    private static final boolean EDITOR_DEFAULT = false;
    private static final String EDITOR_COMMENT = "Only use this as a map maker who wants to create quests. Leaving this off allows you the play the existing quests.";
    private static final String SAVE_DEFAULT_KEY = "SaveDefault";
    private static final boolean SAVE_DEFAULT_DEFAULT = true;
    private static final String SAVE_DEFAULT_COMMENT = "This will save quests in an general map used upon world creation";
    private static final String USE_DEFAULT_KEY = "UseDefault";
    private static final boolean USE_DEFAULT_DEFAULT = true;
    private static final String USE_DEFAULT_COMMENT = "Upon world load quests will be reloaded from the default";
    private static final String KEYMAP_KEY = "KeyMap";
    private static final String KEYMAP_COMMENT = "Hotkeys used in the book, one entry per line(Format: [key]:[mode]";
    public static Configuration syncConfig;
    private static String[] KEYMAP_DEFAULT = null;

    private static List<String> readMeText = Lists.newArrayList("How to copy quests to a server:",
            "In HQM for 1.11.2 the file copying to a server works again. Copy the hqm/quests to the server side config hqm/quests.",
            "There is a config option to automatically sync the server quests to the client.");

    public static void initModConfig(String configPath) {
        ModConfig.init(new File(configPath + "hqmconfig.cfg"));
        MinecraftForge.EVENT_BUS.register(new ModConfig());
    }

    public static void initEditConfig(String configPath) {
        if(new File(configPath, "default").exists()){
            FMLLog.warning("[HQM] Detected old HQM quest files! These aren't fully compatible with the newer versions. To disable this message, delete the 'default' folder.");
        }
        try {
            FileUtils.writeLines(new File(configPath, "ReadMe.txt"), readMeText);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (syncConfig == null) {
            syncConfig = new Configuration(new File(configPath + "editmode.cfg"));
            loadSyncConfig();
        }
    }

    public static void loadSyncConfig() {
        Quest.isEditing = syncConfig.get(Configuration.CATEGORY_GENERAL, EDITOR_KEY, EDITOR_DEFAULT, EDITOR_COMMENT).getBoolean(EDITOR_DEFAULT);
        Quest.saveDefault = syncConfig.get(Configuration.CATEGORY_GENERAL, SAVE_DEFAULT_KEY, SAVE_DEFAULT_DEFAULT, SAVE_DEFAULT_COMMENT).getBoolean(SAVE_DEFAULT_DEFAULT);
        Quest.useDefault = syncConfig.get(Configuration.CATEGORY_GENERAL, USE_DEFAULT_KEY, USE_DEFAULT_DEFAULT, USE_DEFAULT_COMMENT).getBoolean(USE_DEFAULT_DEFAULT);
        if (HardcoreQuesting.proxy.isClient()) {
            if (KEYMAP_DEFAULT == null) {
                KEYMAP_DEFAULT = KeyboardHandler.getDefault();
            }
            KeyboardHandler.fromConfig(syncConfig.get(Configuration.CATEGORY_GENERAL, KEYMAP_KEY, KEYMAP_DEFAULT, KEYMAP_COMMENT).getStringList());
        }
        if (syncConfig.hasChanged()) {
            syncConfig.save();
        }
    }
}
