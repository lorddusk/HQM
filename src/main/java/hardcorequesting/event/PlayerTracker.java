package hardcorequesting.event;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.capabilities.ModCapabilities;
import hardcorequesting.config.HQMConfig;
import hardcorequesting.death.DeathStatsManager;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestLine;
import hardcorequesting.quests.QuestSet;
import hardcorequesting.quests.QuestingDataManager;
import hardcorequesting.util.HQMUtil;
import hardcorequesting.util.Translator;
import net.fabricmc.api.EnvType;
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
        if (HardcoreQuesting.LOADING_SIDE == EnvType.SERVER)
            QuestLine.sendDataToClient(player);
        
        QuestingDataManager questingData = QuestingDataManager.getInstance();
        if (!questingData.hasData(player)) {
            DeathStatsManager.getInstance().resync();
        }
        
        if (questingData.isHardcoreActive())
            sendLoginMessage(player);
        else if (HQMConfig.getInstance().Message.NO_HARDCORE_MESSAGE)
            player.sendMessage(Translator.translatable("hqm.message.noHardcore"), Util.NIL_UUID);
        
        if (!HQMUtil.isGameSingleplayer()) {
            Quest.setEditMode(false);
        }
        
        CompoundTag tags = ModCapabilities.PLAYER_EXTRA_DATA.get(player).tag;
        if (tags.contains(HQ_TAG)) {
            if (tags.getCompound(HQ_TAG).getBoolean(RECEIVED_BOOK)) {
                questingData.getQuestingData(player).receivedBook = true;
            }
            if (!questingData.isQuestActive()) {
                tags.remove(HQ_TAG);
            }
        }
        
        QuestSet.loginReset();
        
        questingData.spawnBook(player);
    }
    
    
    private void sendLoginMessage(Player player) {
        player.sendMessage(Translator.translatable("hqm.message.hardcore").append(" ")
                .append(Translator.pluralTranslated(getRemainingLives(player) != 1, "hqm.message.livesLeft", getRemainingLives(player))), Util.NIL_UUID);
    }
}
