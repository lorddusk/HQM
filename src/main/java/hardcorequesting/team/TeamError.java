package hardcorequesting.team;

import hardcorequesting.network.NetworkManager;
import hardcorequesting.network.message.TeamErrorMessage;
import hardcorequesting.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public enum TeamError {
    INVALID_PLAYER("hqm.team.invalidPlayer.title", "hqm.team.invalidPlayer.desc"),
    IN_PARTY("hqm.team.playerInParty.title", "hqm.team.playerInParty.desc"),
    USED_NAME("hqm.team.usedTeamName.title", "hqm.team.usedTeamName.desc");
    
    //slightly ugly but there's no real way of getting hold of the interface, this works perfectly fine
    public static TeamError latestError;
    private String header;
    private String message;
    
    TeamError(String header, String message) {
        this.message = message;
        this.header = header;
    }
    
    @Environment(EnvType.CLIENT)
    public String getMessage() {
        return I18n.translate(message);
    }
    
    @Environment(EnvType.CLIENT)
    public String getHeader() {
        return I18n.translate(header);
    }
    
    public void sendToClient(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity)
            NetworkManager.sendToPlayer(new TeamErrorMessage(this), (ServerPlayerEntity) player);
    }
}
