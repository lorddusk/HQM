package hardcorequesting.network;

public enum PacketId {
    OPEN_INTERFACE,
    QUEST_DATA,
    TASK_REQUEST,
    CLAIM_REWARD,
    SELECT_TASK,
    SOUND,
    LORE,
    TEAM,
    REFRESH_INTERFACE,
    CLOSE_INTERFACE,
    REFRESH_TEAM,
    OP_BOOK,
    QUEST_SYNC,
    BAG_INTERFACE,
    DEATH_STATS_UPDATE,
    TEAM_STATS_UPDATE,
    TRACKER_ACTIVATE,
    TRACKER_RESPONSE,
    BLOCK_SYNC;


    public byte getId() {
        return (byte) ordinal();
    }

    public static PacketId getFromId(byte id) {
        return values()[id];
    }
}
