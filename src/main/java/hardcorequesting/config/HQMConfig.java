package hardcorequesting.config;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.client.KeyboardHandler;
import hardcorequesting.items.ItemBag;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestLine;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.team.RewardSetting;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = HardcoreQuesting.ID)
@Config.RequiresMcRestart
@Mod.EventBusSubscriber(modid = HardcoreQuesting.ID)
public class HQMConfig {

    @Config.Comment("Settings related to hardcore mode")
    @Config.Name("Hardcore settings")
    public static Hardcore Hardcore = new Hardcore();
    @Config.Comment("Settings related to server start & modes")
    @Config.Name("Server start settings")
    public static Starting Starting = new Starting();
    @Config.Comment("Settings related to loot bags and loot tiers")
    @Config.Name("Loot settings")
    public static Loot Loot = new Loot();

    @Config.Name("Enable spawn with book")
    @Config.Comment("Enable this to cause the player to be granted a gift on spawning into the world")
    public static boolean SPAWN_BOOK = false;

    @Config.Name("Lose quest book upon death")
    @Config.Comment("Loose the quest book when you die, if set to false it will stay in your inventory")
    public static boolean LOSE_QUEST_BOOK = true;

    @Config.Name("All in party get rewards")
    @Config.Comment("Allow every single player in a party to claim the reward for a quest. Setting this to false will give the party one set of rewards to share.")
    public static boolean MULTI_REWARD = true;

    @Config.Name("NBT Subset Tags to Filter")
    @Config.Comment("Use this to specify NBT tags that should be ignored when comparing items with NBT subset")
    public static String[] NBT_SUBSET_FILTER = new String[]{"RepairCost"};

    @Config.Comment("Settings related to messages sent from the server")
    @Config.Name("Message settings")
    public static Message Message = new Message();
    @Config.Comment("Settings related to edit mode")
    @Config.Name("Editing settings")
    public static Editing Editing = new Editing();

    public static class Hardcore {
        @Config.Name("Default lives")
        @Config.Comment("How many lives players should start with.")
        @Config.RangeInt(min = 1, max = 256)
        public int DEFAULT_LIVES = 3;

        @Config.Name("Heart Rot Timer in Seconds")
        @Config.Comment("Define in seconds how long the rot timer is.")
        @Config.RangeInt(min = 1)
        public int HEART_ROT_TIME = 120;

        @Config.Name("Enable rot timer")
        @Config.Comment("Set to true to enable the heart rot timer")
        public boolean HEART_ROT_ENABLE = false;

        @Config.Name("Maximum lives obtainable")
        @Config.Comment("Use this to set the maximum lives obtainable")
        @Config.RangeInt(min = 0, max = 256)
        public int MAX_LIVES = 20;
    }

    public static class Starting {
        @Config.Name("Auto-start hardcore mode")
        @Config.Comment("If set to true, new worlds will automatically activate Hardcore mode")
        public boolean AUTO_HARDCORE = false;
        @Config.Name("Auto-start questing mode")
        @Config.Comment("If set to true, new worlds will automatically activate Questing mode")
        public boolean AUTO_QUESTING = true;
        @Config.Name("Enable server sync")
        @Config.Comment("Set to true to enable the sending of quests from the server to all clients that connect to it")
        public boolean SERVER_SYNC = false;
    }

    public static class Loot {
        @Config.Name("Always use tier name for reward titles")
        @Config.Comment("Always display the tier name, instead of the individual bag's name, when opening a reward bag.")
        public boolean ALWAYS_USE_TIER = false;
        @Config.Name("Display reward interface")
        @Config.Comment("Set to true to display an interface with the contents of the reward bag when you open it.")
        public boolean REWARD_INTERFACE = true;
    }

    public static class Message {
        @Config.Name("Enable hardcore mode status message")
        @Config.Comment("Set to true to enable sending a status message if Hardcore Questing mode is off")
        public boolean NO_HARDCORE_MESSAGE = false;

        @Config.Name("Enable OP command reminder")
        @Config.Comment("Set to false to prevent the 'use /hqm op instead' message when operators use '/hqm edit' instead.")
        public boolean OP_REMINDER = true;
    }

    public static class Editing {
        @Config.Name("Save quests in default folder")
        @Config.Comment("Set to true to save work-in-progress quests in the default folder of the configuration")
        public boolean SAVE_DEFAULT = true;

        @Config.Name("Enable edit mode by default")
        @Config.Comment("Set to true to automatically enable edit mode when entering worlds in single-player. Has no effect in multiplayer.")
        public boolean USE_EDITOR = false;

        @Config.Name("Hotkeys")
        @Config.Comment("Hotkeys used in the book, one entry per line(Format: [key]:[mode]")
        public String[] HOTKEYS = new String[]{"D:delete", "N:create", "M:move", "INSERT:create", "R:rename", "DELETE:delete", "SPACE:normal", "S:swap", "S:swap_select"};
    }

    @SuppressWarnings("deprecation")
    public static void loadConfig() {
        int maxlives = HQMConfig.Hardcore.MAX_LIVES;
        int lives = HQMConfig.Hardcore.DEFAULT_LIVES;

        if (lives > maxlives) lives = maxlives;

        QuestingData.defaultLives = lives;
        QuestingData.autoHardcoreActivate = HQMConfig.Starting.AUTO_HARDCORE;
        QuestingData.autoQuestActivate = HQMConfig.Starting.AUTO_QUESTING;
        RewardSetting.isAllModeEnabled = HQMConfig.MULTI_REWARD;
        ItemBag.displayGui = HQMConfig.Loot.REWARD_INTERFACE;
        QuestLine.doServerSync = HQMConfig.Starting.SERVER_SYNC;

        Quest.isEditing = HQMConfig.Editing.USE_EDITOR;
        Quest.saveDefault = HQMConfig.Editing.SAVE_DEFAULT;
        if (HardcoreQuesting.proxy.isClient()) {
            KeyboardHandler.fromConfig(HQMConfig.Editing.HOTKEYS);
        }
    }

    @SubscribeEvent
    public void onConfigChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equalsIgnoreCase(HardcoreQuesting.ID))
            loadConfig();
    }


    @Config.Ignore
    public static int OVERLAY_XPOS;
    @Config.Ignore
    public static int OVERLAY_YPOS;
    @Config.Ignore
    public static int OVERLAY_XPOSDEFAULT = 2;
    @Config.Ignore
    public static int OVERLAY_YPOSDEFAULT = 2;
}
