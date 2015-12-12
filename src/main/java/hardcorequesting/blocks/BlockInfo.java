package hardcorequesting.blocks;


import hardcorequesting.config.ModConfig;

public abstract class BlockInfo {
    public static final String TEXTURE_LOCATION = "hqm";
    public static final String LOCALIZATION_START = "hqm:";
    public static final String ITEMBARREL_UNLOCALIZED_NAME = "item_barrel";
    public static final String ITEMBARREL_TE_KEY = "ItemBarrel";
    public static final String QUEST_TRACKER_UNLOCALIZED_NAME = "quest_tracker";
    public static final String QUEST_TRACKER_TE_KEY = "QuestTracker";
    public static final String QUEST_PORTAL_UNLOCALIZED_NAME = "quest_portal";
    public static final String QUEST_PORTAL_TE_KEY = "QuestPortal";

    public static final String ITEMBARREL_ICON = "hqmItemBarrel";
    public static final String ITEMBARREL_ICON_EMPTY = "hqmItemBarrelEmpty";
    public static final String QUEST_TRACKER_ICON = "hqmQuestTracker";
    public static final String QUEST_TRACKER_ICON_EMPTY = "hqmItemBarrelEmpty";
    public static final String QUEST_PORTAL_ICON = "hqmQuestPortal";
    public static final String QUEST_PORTAL_EMPTY_ICON = "hqmItemBarrelEmpty";
    public static final String QUEST_PORTAL_TRANSPARENT_ICON = "hqmQuestPortalTransparent";
    public static final String QUEST_PORTAL_TECH_ICON = "hqmQuestPortalTech";
    public static final String QUEST_PORTAL_TECH_EMPTY_ICON = "hqmQuestPortalTechTop";
    public static final String QUEST_PORTAL_MAGIC_ICON = "hqmQuestPortalMagic";

    private BlockInfo() {
    }
}
