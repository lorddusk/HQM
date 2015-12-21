package hardcorequesting;


public enum FileVersion {
    INITIAL,
    QUESTS,
    SETS,
    LORE,
    UNCOMPLETED_DISABLED,
    LORE_AUDIO,
    BAGS,
    LOCK,
    BAG_LIMITS,
    TEAMS,
    TEAM_SETTINGS,
    DEATHS,
    REMOVED_QUESTS,
    REPEATABLE_QUESTS,
    TRIGGER_QUESTS,
    OPTION_LINKS,
    NO_ITEM_IDS,
    NO_ITEM_IDS_FIX,
    PARENT_COUNT,
    REPUTATION,
    REPUTATION_KILL,
    REPUTATION_BARS,
    CUSTOM_PRECISION_TYPES,
    COMMAND_REWARDS;

    public boolean contains(FileVersion other) {
        return ordinal() >= other.ordinal();
    }

    public boolean lacks(FileVersion other) {
        return ordinal() < other.ordinal();
    }
}
