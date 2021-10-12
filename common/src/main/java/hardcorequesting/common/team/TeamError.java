package hardcorequesting.common.team;

import hardcorequesting.common.network.NetworkManager;
import hardcorequesting.common.network.message.TeamErrorMessage;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public enum TeamError {
    INVALID_PLAYER("hqm.team.invalidPlayer.title", "hqm.team.invalidPlayer.desc"),
    IN_PARTY("hqm.team.playerInParty.title", "hqm.team.playerInParty.desc"),
    USED_NAME("hqm.team.usedTeamName.title", "hqm.team.usedTeamName.desc");
    
    //slightly ugly but there's no real way of getting hold of the interface, this works perfectly fine
    public static TeamError latestError;
    private final String header;
    private final String message;
    
    TeamError(String header, String message) {
        this.message = message;
        this.header = header;
    }
    
    @Environment(EnvType.CLIENT)
    public FormattedText getMessage() {
        return Translator.translatable(message).withStyle(ChatFormatting.DARK_RED);
    }
    
    @Environment(EnvType.CLIENT)
    public FormattedText getHeader() {
        return Translator.translatable(header).withStyle(ChatFormatting.DARK_RED);
    }
    
    public void sendToClient(Player player) {
        if (player instanceof ServerPlayer)
            NetworkManager.sendToPlayer(new TeamErrorMessage(this), (ServerPlayer) player);
    }
}
