package hardcorequesting.team;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.server.FMLServerHandler;

import java.io.IOException;

public class PlayerEntry {

    private static final String UUID = "uuid";
    private static final String OWNER = "owner";
    private static final String BOOK = "bookOpen";
    private static final String IN_TEAM = "inTeam";
    private String uuid;
    private boolean inTeam;
    private boolean owner;
    private boolean bookOpen;

    private PlayerEntry() {

    }

    public PlayerEntry(String uuid, boolean inTeam, boolean owner) {
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
                case UUID:
                    playerEntry.uuid = in.nextString();
                    break;
                case OWNER:
                    playerEntry.owner = in.nextBoolean();
                    break;
                case BOOK:
                    playerEntry.bookOpen = in.nextBoolean();
                    break;
                case IN_TEAM:
                    playerEntry.inTeam = in.nextBoolean();
                    break;
                default:
                    break;
            }
        }
        in.endObject();
        return playerEntry;
    }

    public String getUUID() {
        return uuid;
    }

    @SideOnly(Side.CLIENT)
    public String getDisplayName() {
        return Minecraft.getMinecraft().theWorld.getPlayerEntityByUUID(java.util.UUID.fromString(uuid)).getDisplayNameString();
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

        return uuid != null ? uuid.equals(entry.uuid) : entry.uuid == null;
    }

    public boolean shouldRefreshData() {
        return bookOpen;
    }

    public boolean isBookOpen() {
        return bookOpen;
    }

    public void setBookOpen(boolean bookOpen) {
        this.bookOpen = bookOpen;
    }

    public void write(JsonWriter out) throws IOException {
        out.beginObject();
        out.name(UUID).value(uuid);
        out.name(OWNER).value(owner);
        out.name(BOOK).value(bookOpen);
        out.name(IN_TEAM).value(inTeam);
        out.endObject();
    }

    @SideOnly(Side.SERVER)
    public EntityPlayerMP getPlayerMP() {
        return FMLServerHandler.instance().getServer().getPlayerList().getPlayerByUUID(java.util.UUID.fromString(this.uuid));
    }
}
