package hardcorequesting.team;

import hardcorequesting.network.NetworkManager;
import hardcorequesting.network.message.TeamErrorMessage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

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
        return I18n.get(message);
    }
    
    @Environment(EnvType.CLIENT)
    public String getHeader() {
        return I18n.get(header);
    }
    
    public void sendToClient(Player player) {
        if (player instanceof ServerPlayer)
            NetworkManager.sendToPlayer(new TeamErrorMessage(this), (ServerPlayer) player);
    }
}
