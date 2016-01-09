package hardcorequesting.config;

import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import hardcorequesting.ModInformation;
import hardcorequesting.QuestingData;
import hardcorequesting.Team;
import hardcorequesting.items.ItemBag;
import hardcorequesting.quests.QuestLine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

public class ModConfig {
    private static final String CATEGORY_GENERAL = "General";

    private static final String LIVES_KEY = "Default lives";
    private static final int LIVES_DEFAULT = 3;
    private static final String LIVES_COMMENT = "How many lives players should start with.";

    private static final String AUTO_KEY = "Auto-start hardcore mode";
    private static final boolean AUTO_DEFAULT = false;
    private static final String AUTO_COMMENT = "If set to true, new worlds will automatically activate Hardcore mode";

    private static final String AUTO_QUEST_KEY = "Auto-start questing mode";
    private static final boolean AUTO_QUEST_DEFAULT = true;
    private static final String AUTO_QUEST_COMMENT = "If set to true, new worlds will automatically activate Questing mode";

    public static final String MAXLIVES_KEY = "MaxLives";
    public static final int MAXLIVES_DEFAULT = 20;
    private static final String MAXLIVES_COMMENT = "Use this to set the maximum lives obtainable (Max 255)";
    public static int MAXLIVES;

    private static final String SPAWNBOOK_KEY = "SpawnBook";
    private static final boolean SPAWNBOOK_DEFAULT = true;
    private static final String SPAWNBOOK_COMMENT = "Use this if you want the book to spawn on create world";
    public static boolean spawnBook;

    private static final String EDITOR_KEY = "UseEditor";
    private static final boolean EDITOR_DEFAULT = false;
    private static final String EDITOR_COMMENT = "Only use this as a map maker who wants to create quests. Leaving this off allows you the play the existing quests.";

    private static final String MULTI_REWARD_KEY = "MultiReward";
    private static final boolean MULTI_REWARD_DEFAULT = true;
    private static final String MULTI_REWARD_COMMENT = "Allow every single player in a party to claim the reward for a quest. Setting this to false will give the party one set of rewards to share.";

    private static final String SYNC_KEY = "ServerSync";
    private static final boolean SYNC_DEFAULT = false;
    private static final String SYNC_COMMENT = "If this is set to true, the server will send the quests to clients connecting to it.";

    private static final String REWARD_KEY = "RewardInterface";
    private static final boolean REWARD_DEFAULT = true;
    private static final String REWARD_COMMENT = "Display an interface with the contents of the reward bag when you open it.";

    private static final String FRESHNESS_KEY = "RotTimer";
    private static final boolean FRESHNESS_DEFAULT = false;
    private static final String FRESHNESS_COMMENT = "Turn on/off the rot timer.";
    public static boolean ROTTIMER;
    private static final String ROT_KEY = "RotTime";
    private static final int ROT_DEFAULT = 120;
    private static final String ROT_COMMENT = "Define in seconds how long the rot timer is.";
    public static int MAXROT;

    public static boolean NO_HARDCORE_MESSAGE;
    public static final boolean NO_HARDCORE_MESSAGE_DEFAULT = true;
    public static final String NO_HARDCORE_MESSAGE_COMMENT = "Enable or disable sending a status message if Hardcore Questing mode is off";
    public static final String NO_HARDCORE_MESSAGE_KEY = "NoHardcoreMessage";

    public static boolean ALWAYS_USE_TIER_NAME_FOR_REWARD_TITLES;
    private static final String ALWAYS_USE_TIER_NAME_FOR_REWARD_TITLES_KEY = "AlwaysUseTierNameForRewardTitles";
    private static final boolean ALWAYS_USE_TIER_NAME_FOR_REWARD_TITLES_DEFAULT = false;
    private static final String ALWAYS_USE_TIER_NAME_FOR_REWARD_TITLES_COMMENT = "Always display the tier name, instead of the individual bag's name, when opening a reward bag.";

    public static boolean LOSE_QUEST_BOOK_ON_DEATH;
    private static final String LOSE_QUEST_BOOK_ON_DEATH_KEY = "LoseQuestBookOnDeath";
    private static final boolean LOSE_QUEST_BOOK_ON_DEATH_DEFAULT = true;
    private static final String LOSE_QUEST_BOOK_ON_DEATH_COMMENT = "Loose the quest book when you die, if set to false it will stay in your inventory";

    public static int OVERLAY_XPOS;
    public static int OVERLAY_YPOS;
    public static int OVERLAY_XPOSDEFAULT = 2;
    public static int OVERLAY_YPOSDEFAULT = 2;

    public static Configuration config;

    public static void init(File file) {
        if (config == null) {
            config = new Configuration(file);
            loadConfig();
        }
    }

    @SubscribeEvent
    public void onConfigChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.modID.equalsIgnoreCase(ModInformation.ID))
            loadConfig();
    }

    private static void loadConfig() {
        int lives = config.get(CATEGORY_GENERAL, LIVES_KEY, LIVES_DEFAULT, LIVES_COMMENT).getInt(LIVES_DEFAULT);
        MAXLIVES = config.get(CATEGORY_GENERAL, MAXLIVES_KEY, MAXLIVES_DEFAULT, MAXLIVES_COMMENT).getInt(MAXLIVES_DEFAULT);

        if (MAXLIVES < 1) {
            MAXLIVES = 1;
        } else if (MAXLIVES > 255) {
            MAXLIVES = 255;
        }

        if (lives < 1) {
            lives = 1;
        } else if (lives > MAXLIVES) {
            lives = MAXLIVES;
        }

        if (MAXROT < 1) {
            MAXROT = 1;
        }

        QuestingData.defaultLives = lives;

        QuestingData.autoHardcoreActivate = config.get(CATEGORY_GENERAL, AUTO_KEY, AUTO_DEFAULT, AUTO_COMMENT).getBoolean(AUTO_DEFAULT);

        QuestingData.autoQuestActivate = config.get(CATEGORY_GENERAL, AUTO_QUEST_KEY, AUTO_QUEST_DEFAULT, AUTO_QUEST_COMMENT).getBoolean(AUTO_QUEST_DEFAULT);

        Team.RewardSetting.isAllModeEnabled = config.get(CATEGORY_GENERAL, MULTI_REWARD_KEY, MULTI_REWARD_DEFAULT, MULTI_REWARD_COMMENT).getBoolean(MULTI_REWARD_DEFAULT);

        ItemBag.displayGui = config.get(CATEGORY_GENERAL, REWARD_KEY, REWARD_DEFAULT, REWARD_COMMENT).getBoolean(REWARD_DEFAULT);

        spawnBook = config.get(CATEGORY_GENERAL, SPAWNBOOK_KEY, SPAWNBOOK_DEFAULT, SPAWNBOOK_COMMENT).getBoolean(SPAWNBOOK_DEFAULT);

        QuestLine.doServerSync = config.get(CATEGORY_GENERAL, SYNC_KEY, SYNC_DEFAULT, SYNC_COMMENT).getBoolean(SYNC_DEFAULT);

        ROTTIMER = config.get(CATEGORY_GENERAL, FRESHNESS_KEY, FRESHNESS_DEFAULT, FRESHNESS_COMMENT).getBoolean(FRESHNESS_DEFAULT);
        MAXROT = config.get(CATEGORY_GENERAL, ROT_KEY, ROT_DEFAULT, ROT_COMMENT).getInt(ROT_DEFAULT);

        NO_HARDCORE_MESSAGE = config.get(CATEGORY_GENERAL, NO_HARDCORE_MESSAGE_KEY, NO_HARDCORE_MESSAGE_DEFAULT, NO_HARDCORE_MESSAGE_COMMENT).getBoolean(NO_HARDCORE_MESSAGE_DEFAULT);

        ALWAYS_USE_TIER_NAME_FOR_REWARD_TITLES = config.get(CATEGORY_GENERAL, ALWAYS_USE_TIER_NAME_FOR_REWARD_TITLES_KEY, ALWAYS_USE_TIER_NAME_FOR_REWARD_TITLES_DEFAULT, ALWAYS_USE_TIER_NAME_FOR_REWARD_TITLES_COMMENT).getBoolean(ALWAYS_USE_TIER_NAME_FOR_REWARD_TITLES_DEFAULT);

        LOSE_QUEST_BOOK_ON_DEATH = config.get(CATEGORY_GENERAL, LOSE_QUEST_BOOK_ON_DEATH_KEY, LOSE_QUEST_BOOK_ON_DEATH_DEFAULT, LOSE_QUEST_BOOK_ON_DEATH_COMMENT).getBoolean(LOSE_QUEST_BOOK_ON_DEATH_DEFAULT);

        if (config.hasChanged())
            config.save();
    }

    @SuppressWarnings("unchecked")
    public static List<IConfigElement> getConfigElements() {
        List<IConfigElement> list = new ArrayList<IConfigElement>();
        list.addAll(new ConfigElement(config.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements());
        return list;
    }

}
