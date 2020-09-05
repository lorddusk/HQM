package hardcorequesting.event;

import hardcorequesting.capabilities.ModCapabilities;
import hardcorequesting.config.HQMConfig;
import hardcorequesting.death.DeathStats;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestLine;
import hardcorequesting.quests.QuestSet;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.util.HQMUtil;
import hardcorequesting.util.Translator;
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
        return QuestingData.getQuestingData(sender).getLives();
    }
    
    public void onPlayerLogin(ServerPlayer player) {
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
        
        CompoundTag tags = ModCapabilities.PLAYER_EXTRA_DATA.get(player).tag;
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
    
    
    private void sendLoginMessage(Player player) {
        player.sendMessage(Translator.translatable("hqm.message.hardcore").append(" ")
                .append(Translator.pluralTranslated(getRemainingLives(player) != 1, "hqm.message.livesLeft", getRemainingLives(player))), Util.NIL_UUID);
    }
}
