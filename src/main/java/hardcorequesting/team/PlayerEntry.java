package hardcorequesting.team;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import hardcorequesting.HardcoreQuesting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
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
    
    public static PlayerEntry read(JsonReader in) throws IOException {
        PlayerEntry playerEntry = new PlayerEntry();
        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case ENTRY_UUID:
                    try {
                        playerEntry.uuid = UUID.fromString(in.nextString());
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace(); // todo error print
                    }
                    break;
                case ENTRY_OWNER:
                    playerEntry.owner = in.nextBoolean();
                    break;
                case ENTRY_BOOK:
                    playerEntry.bookOpen = in.nextBoolean();
                    break;
                case ENTRY_IN_TEAM:
                    playerEntry.inTeam = in.nextBoolean();
                    break;
                default:
                    break;
            }
        }
        in.endObject();
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
    
    public void write(JsonWriter out) throws IOException {
        out.beginObject();
        out.name(ENTRY_UUID).value(uuid.toString());
        out.name(ENTRY_OWNER).value(owner);
        out.name(ENTRY_BOOK).value(bookOpen);
        out.name(ENTRY_IN_TEAM).value(inTeam);
        out.endObject();
    }
    
    public ServerPlayer getPlayerMP() {
        if (HardcoreQuesting.LOADING_SIDE == EnvType.SERVER) {
            return HardcoreQuesting.getServer().getPlayerList().getPlayer(this.getUUID());
        } else {
            if (Minecraft.getInstance().hasSingleplayerServer()) {
                return Minecraft.getInstance().getSingleplayerServer().getPlayerList().getPlayer(this.getUUID());
            }
        }
        return null;
    }
}
