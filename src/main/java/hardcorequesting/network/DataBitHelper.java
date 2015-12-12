package hardcorequesting.network;


import hardcorequesting.FileVersion;
import hardcorequesting.QuestingData;

public enum DataBitHelper {
    BYTE(8),
    SHORT(16),
    INT(32),
    BOOLEAN(1),
    EMPTY(0),
    NBT_LENGTH(15),
    PACKET_ID(5),
    NAME_LENGTH(5),

    PLAYERS(16) {
        @Override
        public int getBitCount(FileVersion version) {
            return version.lacks(FileVersion.REPEATABLE_QUESTS) ? 10 : super.getBitCount(version);
        }
    },
    QUESTS(10) {
        @Override
        public int getBitCount(FileVersion version) {
            return version.lacks(FileVersion.SETS) ? 7 : super.getBitCount(version);
        }
    },
    TASKS(4),
    REWARDS(3),
    QUEST_SETS(5),
    ITEM_PROGRESS(30),
    QUEST_NAME_LENGTH(5),
    QUEST_DESCRIPTION_LENGTH(16),
    QUEST_POS_X(9),
    QUEST_POS_Y(8),
    TASK_TYPE(4) {
        @Override
        public int getBitCount(FileVersion version) {
            return version.lacks(FileVersion.REPUTATION_KILL) ? 3 : super.getBitCount(version);
        }
    },
    TASK_ITEM_COUNT(6, 35),
    TASK_REQUIREMENT(32),
    QUEST_REWARD(3),
    ITEM_PRECISION(30) {
        public int getBitCount(FileVersion version) {
            return version.lacks(FileVersion.CUSTOM_PRECISION_TYPES) ? 2 : super.getBitCount(version);
        }
    },
    GROUP_ITEMS(6),
    GROUP_COUNT(10),
    TIER_COUNT(7),
    WEIGHT(19),
    COLOR(4),
    PASS_CODE(7),
    LIMIT(10),
    TEAMS(10),
    TEAM_ACTION_ID(4),
    LIVES(8),
    TEAM_LIVES(0) {
        @Override
        public int getBitCount(FileVersion version) {
            return DataBitHelper.PLAYERS.getBitCount(version) + DataBitHelper.LIVES.getBitCount(version);
        }
    },
    TEAM_ERROR(2),
    TEAM_REWARD_SETTING(2),
    TEAM_LIVES_SETTING(1),
    OP_ACTION(3),
    BAG_TIER(3, 4),
    DEATHS(12),
    TEAM_PROGRESS(7),
    TASK_LOCATION_COUNT(3, 4),
    WORLD_COORDINATE(32),
    LOCATION_VISIBILITY(2),
    TICKS(10),
    HOURS(32),
    REPEAT_TYPE(2),
    TRIGGER_TYPE(2),
    TASK_MOB_COUNT(3, 4),
    KILL_COUNT(16),
    MOB_ID_LENGTH(10),
    TRACKER_TYPE(2),
    PORTAL_TYPE(2),
    REPUTATION(8),
    REPUTATION_VALUE(32),
    REPUTATION_REWARD(3),
    REPUTATION_SETTING(3, 4),
    REPUTATION_MARKER(5, 30) //would normally be 31 but since there is the neutral marker too, there can only be 30 normal ones
    ;

    private int bitCount;

    DataBitHelper(int bitCount, int maximum) {
        this(bitCount);
        int calculatedMax = getMaximum();
        if (maximum < calculatedMax) {
            cachedMaximum = maximum;
        }
    }

    DataBitHelper(int bitCount) {
        this.bitCount = bitCount;
    }

    public int getBitCount(FileVersion version) {
        return bitCount;
    }

    public final int getBitCount() {
        return getBitCount(QuestingData.FILE_VERSION);
    }


    private boolean hasMaximum;
    private int cachedMaximum;

    public int getMaximum() {
        if (!hasMaximum) {
            hasMaximum = true;
            cachedMaximum = bitCount == INT.bitCount ? Integer.MAX_VALUE : (1 << bitCount) - 1;
        }

        return cachedMaximum;
    }
}
