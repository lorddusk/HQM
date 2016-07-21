package hardcorequesting.team;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cpw.mods.fml.server.FMLServerHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.ServerConfigurationManager;

import java.io.IOException;
import java.util.UUID;

public class PlayerEntry {
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

    public String getUUID() {
        return uuid;
    }

    @SideOnly(Side.CLIENT)
    public String getDisplayName() {
        return Minecraft.getMinecraft().theWorld.getPlayerEntityByUUID(java.util.UUID.fromString(uuid)).getCommandSenderName();
    }

    public boolean isInTeam() {
        return inTeam;
    }

    public boolean isOwner() {
        return owner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayerEntry entry = (PlayerEntry) o;

        return uuid != null ? uuid.equals(entry.uuid) : entry.uuid == null;
    }

    @Override
    public int hashCode() {
        return uuid != null ? uuid.hashCode() : 0;
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

    private static final String UUID = "uuid";
    private static final String OWNER = "owner";
    private static final String BOOK = "bookOpen";
    private static final String IN_TEAM = "inTeam";

    public void write(JsonWriter out) throws IOException {
        out.beginObject();
        out.name(UUID).value(uuid);
        out.name(OWNER).value(owner);
        out.name(BOOK).value(bookOpen);
        out.name(IN_TEAM).value(inTeam);
        out.endObject();
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

    public void setInTeam(boolean inTeam) {
        this.inTeam = inTeam;
    }

    @SideOnly(Side.SERVER)
    public EntityPlayerMP getPlayerMP() {
        ServerConfigurationManager configurationManager = FMLServerHandler.instance().getServer().getConfigurationManager();
        UUID uuidToFind = java.util.UUID.fromString(this.uuid);
        for (String userName : configurationManager.getAllUsernames()) {
            EntityPlayerMP playerByUsername = configurationManager.getPlayerByUsername(userName);
            if(playerByUsername.getUniqueID().equals(uuidToFind))
                return playerByUsername;
        }
        return null;
    }
}
