package hardcorequesting.event;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.config.HQMConfig;
import hardcorequesting.death.DeathStats;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestLine;
import hardcorequesting.quests.QuestSet;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.util.HQMUtil;
import hardcorequesting.util.Translator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

public class PlayerTracker {
    
    public static PlayerTracker instance;
    public static final String HQ_TAG = "HardcoreQuesting";
    public static final String RECEIVED_BOOK = "questBook";
    
    public PlayerTracker() {
        instance = this;
    }
    
    public int getRemainingLives(PlayerEntity sender) {
        return QuestingData.getQuestingData(sender).getLives();
    }
    
    public void onPlayerLogin(ServerPlayerEntity player) {
        if (!QuestingData.hasData(player)) {
            DeathStats.resync();
        }
        
        QuestLine.sendServerSync(player);
        
        if (QuestingData.isHardcoreActive())
            sendLoginMessage(player);
        else if (HQMConfig.getInstance().Message.NO_HARDCORE_MESSAGE)
            player.sendMessage(Translator.translatable("hqm.message.noHardcore"), Util.NIL_UUID);
        
        if (!HQMUtil.isGameSingleplayer()) {
            Quest.setEditMode(false);
        }
        
        CompoundTag tags = HardcoreQuesting.PLAYER_EXTRA_DATA.get(player).tag;
        if (tags.contains(HQ_TAG)) {
            if (tags.getCompound(HQ_TAG).getBoolean(RECEIVED_BOOK)) {
                QuestingData.getQuestingData(player).receivedBook = true;
            }
            if (!QuestingData.isQuestActive()) {
                tags.remove(HQ_TAG);
            }
        }
        
        QuestSet.loginReset();
        
        QuestingData.spawnBook(player);
    }
    
    
    private void sendLoginMessage(PlayerEntity player) {
        player.sendMessage(Translator.translatable("hqm.message.hardcore").append(" ")
                .append(Translator.translatable(Formatting.RESET, getRemainingLives(player) != 1, "hqm.message.livesLeft", getRemainingLives(player))), Util.NIL_UUID);
    }
}
