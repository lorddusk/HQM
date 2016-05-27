package hardcorequesting.team;

import hardcorequesting.network.NetworkManager;
import hardcorequesting.network.message.TeamErrorMessage;
import hardcorequesting.util.Translator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public enum TeamError {
    INVALID_PLAYER("hqm.team.invalidPlayer.title", "hqm.team.invalidPlayer.desc"),
    IN_PARTY("hqm.team.playerInParty.title", "hqm.team.playerInParty.desc"),
    USED_NAME("hqm.team.usedTeamName.title", "hqm.team.usedTeamName.desc");

    private String header;
    private String message;

    TeamError(String header, String message) {
        this.message = message;
        this.header = header;
    }

    public String getMessage() {
        return Translator.translate(message);
    }

    public String getHeader() {
        return Translator.translate(header);
    }

    //slightly ugly but there's no real way of getting hold of the interface, this works perfectly fine
    public static TeamError latestError;

    public void sendToClient(EntityPlayer player) {
        if (player instanceof EntityPlayerMP)
            NetworkManager.sendToPlayer(new TeamErrorMessage(this), (EntityPlayerMP) player);
    }
}
