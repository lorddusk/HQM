package hardcorequesting.common.event;

import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.config.HQMConfig;
import hardcorequesting.common.death.DeathStatsManager;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestLine;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.util.HQMUtil;
import hardcorequesting.common.util.Translator;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class PlayerTracker {
    
    public static PlayerTracker instance;
    public static final String HQ_TAG = "HardcoreQuesting";
    public static final String RECEIVED_BOOK = "questBook";
    
    public PlayerTracker() {
        instance = this;
    }
    
    public int getRemainingLives(Player sender) {
        return QuestingDataManager.getInstance().getQuestingData(sender).getLives();
    }
    
    public void onPlayerLogin(ServerPlayer player) {
        QuestLine.sendDataToClient(player);
        
        QuestingDataManager questingData = QuestingDataManager.getInstance();
        if (!questingData.hasData(player)) {
            DeathStatsManager.getInstance().resync();
        }
        
        if (questingData.isHardcoreActive())
            sendLoginMessage(player);
        else if (HQMConfig.getInstance().Message.NO_HARDCORE_MESSAGE)
            player.sendMessage(Translator.translatable("hqm.message.noHardcore"), Util.NIL_UUID);
        
        if (!HQMUtil.isSinglePlayerOnly()) {
            Quest.setEditMode(false);
        }
        
        CompoundTag tags = HardcoreQuestingCore.platform.getPlayerExtraTag(player);
        if (tags.contains(HQ_TAG)) {
            if (tags.getCompound(HQ_TAG).getBoolean(RECEIVED_BOOK)) {
                questingData.getQuestingData(player).receivedBook = true;
            }
            if (!questingData.isQuestActive()) {
                tags.remove(HQ_TAG);
            }
        }
        
        questingData.spawnBook(player);
    }
    
    
    private void sendLoginMessage(Player player) {
        player.sendMessage(Translator.translatable("hqm.message.hardcore").append(" ")
                .append(Translator.pluralTranslated(getRemainingLives(player) != 1, "hqm.message.livesLeft", getRemainingLives(player))), Util.NIL_UUID);
    }
}
