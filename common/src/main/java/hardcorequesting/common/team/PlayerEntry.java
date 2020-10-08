package hardcorequesting.common.team;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.io.adapter.Adapter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class PlayerEntry {
    private static final String ENTRY_UUID = "uuid";
    private static final String ENTRY_OWNER = "owner";
    private static final String ENTRY_BOOK = "bookOpen";
    private static final String ENTRY_IN_TEAM = "inTeam";
    private UUID uuid;
    private boolean inTeam;
    private boolean owner;
    private boolean bookOpen;
    private String playername;
    
    private PlayerEntry() {
        this.playername = null;
    }
    
    public PlayerEntry(UUID uuid, boolean inTeam, boolean owner) {
        this();
        this.uuid = uuid;
        this.inTeam = inTeam;
        this.owner = owner;
        this.bookOpen = false;
    }
    
    public static PlayerEntry read(JsonElement element) {
        JsonObject object = element.getAsJsonObject();
        PlayerEntry playerEntry = new PlayerEntry();
        playerEntry.uuid = UUID.fromString(GsonHelper.getAsString(object, ENTRY_UUID));
        playerEntry.owner = GsonHelper.getAsBoolean(object, ENTRY_OWNER);
        playerEntry.bookOpen = GsonHelper.getAsBoolean(object, ENTRY_BOOK);
        playerEntry.inTeam = GsonHelper.getAsBoolean(object, ENTRY_IN_TEAM);
        return playerEntry;
    }
    
    public UUID getUUID() {
        return uuid;
    }
    
    @Nullable
    @Environment(EnvType.CLIENT)
    public String getDisplayName() {
        if (this.playername == null) {
            Player entry = Minecraft.getInstance().level.getPlayerByUUID(this.getUUID());
            if (entry != null) {
                this.playername = entry.getScoreboardName();
            }
        }
        return playername;
    }
    
    public boolean isInTeam() {
        return inTeam;
    }
    
    public void setInTeam(boolean inTeam) {
        this.inTeam = inTeam;
    }
    
    public boolean isOwner() {
        return owner;
    }
    
    @Override
    public int hashCode() {
        return uuid != null ? uuid.hashCode() : 0;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        PlayerEntry entry = (PlayerEntry) o;
        
        return Objects.equals(uuid, entry.uuid);
    }
    
    public boolean isBookOpen() {
        return bookOpen;
    }
    
    public void setBookOpen(boolean bookOpen) {
        this.bookOpen = bookOpen;
    }
    
    public JsonElement toJson() {
        return Adapter.object()
                .add(ENTRY_UUID, uuid.toString())
                .add(ENTRY_OWNER, owner)
                .add(ENTRY_BOOK, bookOpen)
                .add(ENTRY_IN_TEAM, inTeam)
                .build();
    }
    
    public ServerPlayer getPlayerMP() {
        if (!HardcoreQuestingCore.platform.isClient()) {
            return HardcoreQuestingCore.getServer().getPlayerList().getPlayer(this.getUUID());
        } else {
            if (Minecraft.getInstance().hasSingleplayerServer()) {
                return Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayer(this.getUUID());
            }
        }
        return null;
    }
}
