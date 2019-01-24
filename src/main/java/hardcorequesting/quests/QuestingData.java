package hardcorequesting.quests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import hardcorequesting.HardcoreQuesting;
import net.minecraft.client.Minecraft;

import com.mojang.authlib.GameProfile;

import hardcorequesting.bag.Group;
import hardcorequesting.bag.GroupData;
import hardcorequesting.client.sounds.SoundHandler;
import hardcorequesting.client.sounds.Sounds;
import hardcorequesting.config.ModConfig;
import hardcorequesting.death.DeathStats;
import hardcorequesting.event.PlayerTracker;
import hardcorequesting.io.SaveHandler;
import hardcorequesting.items.ModItems;
import hardcorequesting.network.NetworkManager;
import hardcorequesting.network.message.LivesUpdate;
import hardcorequesting.team.PlayerEntry;
import hardcorequesting.team.Team;
import hardcorequesting.team.TeamStats;
import hardcorequesting.team.TeamUpdateSize;
import hardcorequesting.util.Translator;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListBansEntry;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class QuestingData {

    // private static boolean debugActive = false;
    public static int defaultLives;
    public static boolean autoHardcoreActivate;
    public static boolean autoQuestActivate;
    private static boolean hardcoreActive;
    private static boolean questActive;
    private static HashMap<UUID, QuestingData> data = new HashMap<>();
    private static List<Team> teams = new ArrayList<>();
    public UUID selectedQuestId = null;
    public int selectedTask = -1;
    public boolean playedLore;
    public boolean receivedBook;
    private Team team;
    private int lives;
    private UUID playerId;
    private String name;
    private Map<UUID, GroupData> groupData;
    private DeathStats deathStat;

    private QuestingData(UUID playerId) {
        this.lives = defaultLives;
        this.playerId = playerId;
        this.team = new Team(playerId);
        createGroupData();
        deathStat = new DeathStats(playerId);
        data.put(playerId, this);
    }

    public QuestingData(UUID playerId, int lives, int teamId, Map<UUID, GroupData> groupData, DeathStats deathStat) {
        this.playerId = playerId;
        this.lives = lives;
        if (teamId > -1 && teamId < QuestingData.getTeams().size())
            this.team = QuestingData.getTeams().get(teamId);
        if (team == null) team = new Team(playerId);
        createGroupData();
        this.groupData.putAll(groupData);
        if (deathStat == null) this.deathStat = new DeathStats(playerId);
        else this.deathStat = deathStat;
        data.put(playerId, this);
    }

    public static HashMap<UUID, QuestingData> getData() {
        return data;
    }

    //keep all the red line code in one spot
    public static boolean isSinglePlayer() {
        return FMLCommonHandler.instance().getMinecraftServerInstance().isSinglePlayer();
    }

    public static boolean isHardcoreActive() {
        return hardcoreActive;
    }

    public static boolean isQuestActive() {
        return questActive;
    }

    public static void activateHardcore() {
        WorldInfo worldInfo = null;
        try{
            if(HardcoreQuesting.loadingSide.isServer()){
                worldInfo = FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getWorldInfo();
            } else {
                worldInfo = Minecraft.getMinecraft().world.getWorldInfo();
            }
        } catch(Exception e){
            e.printStackTrace(); // todo exception handling
        }
        if(worldInfo != null){
            if(!hardcoreActive && !worldInfo.isHardcoreModeEnabled()){
                hardcoreActive = true;
            }
        }
    }

    public static void disableHardcore() {
        hardcoreActive = false;
    }

    public static void activateQuest(boolean giveBooks) {
        if (!questActive) {
            questActive = true;
            if (giveBooks) {
                for (GameProfile profile : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getOnlinePlayerProfiles()) {
                    if (profile != null) {
                        EntityPlayer player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(profile.getId());
                        if (player != null) {
                            spawnBook(player);
                        }
                    }
                }
            }
        }
    }

    public static void deactivate() {
        if (hardcoreActive || questActive /*|| debugActive*/) {
            hardcoreActive = false;
            questActive = false;
            //debugActive = false;
            data = new HashMap<>();
            teams = new ArrayList<>();
        }
    }

    public static void saveState() {
        try {
            SaveHandler.saveQuestingState(SaveHandler.getLocalFile("state"));
        } catch (IOException e) {
            HardcoreQuesting.LOG.error("Failed to save questing state!", e);
        }
    }

    public static void loadState(boolean remote) {
        try {
            SaveHandler.loadQuestingState(SaveHandler.getFile("state", remote));
        } catch (IOException e) {
            HardcoreQuesting.LOG.error("Failed to load questing state!", e);
        }
    }

    public static void saveQuestingData() {
        try {
            SaveHandler.saveQuestingData(SaveHandler.getLocalFile("data"));
        } catch (IOException e) {
            HardcoreQuesting.LOG.error("Failed to save questing data!", e);
        }
    }

    public static String saveQuestingData(EntityPlayer entity) {
        return SaveHandler.saveQuestingData(QuestingData.getQuestingData(entity));
    }

    public static void loadQuestingData(boolean remote) {
        try {
            data.clear();
            SaveHandler.loadQuestingData(SaveHandler.getFile("data", remote)).forEach(qData -> data.put(qData.getPlayerId(), qData));
        } catch (IOException e) {
            HardcoreQuesting.LOG.error("Failed to load questing data!", e);
        }
    }

    public static List<Team> getTeams() {
        return teams;
    }

    public static List<Team> getAllTeams() {
        List<Team> all = new ArrayList<>();
        all.addAll(getTeams());
        data.values().stream().filter(questingData -> questingData.getTeam().isSingle()).forEach(player -> all.add(player.getTeam()));
        return all;
    }

    public static QuestingData getQuestingData(EntityPlayer player) {
        return getQuestingData(player.getPersistentID());
    }

    public static QuestingData getQuestingData(UUID uuid) {
        if (!data.containsKey(uuid))
            data.put(uuid, new QuestingData(uuid));

        return data.get(uuid);
    }

    public static void disableVanillaHardcore(ICommandSender sender) {
        if (sender.getServer().getEntityWorld().getWorldInfo().isHardcoreModeEnabled()) {
            sender.sendMessage(new TextComponentTranslation("hqm.message.vanillaHardcore"));
            try {
                ReflectionHelper.setPrivateValue(WorldInfo.class, sender.getEntityWorld().getWorldInfo(), false, 20);
            } catch (Throwable ex) {
                ex.printStackTrace();
            }

            if (!sender.getServer().getEntityWorld().getWorldInfo().isHardcoreModeEnabled()) {
                sender.sendMessage(new TextComponentTranslation("hqm.message.vanillaHardcoreOverride"));
            }
        }
    }

    public static void spawnBook(EntityPlayer player) {
        if (!Quest.canQuestsBeEdited() && !player.world.isRemote && ModConfig.spawnBook && !QuestingData.getQuestingData(player).receivedBook && QuestingData.isQuestActive()) {
            QuestingData.getQuestingData(player).receivedBook = true;
            NBTTagCompound hqmTag = new NBTTagCompound();
            if (player.getEntityData().hasKey(PlayerTracker.HQ_TAG))
                hqmTag = player.getEntityData().getCompoundTag(PlayerTracker.HQ_TAG);
            hqmTag.setBoolean(PlayerTracker.RECEIVED_BOOK, true);
            player.getEntityData().setTag(PlayerTracker.HQ_TAG, hqmTag);
            ItemStack stack = new ItemStack(ModItems.book);
            if (!player.inventory.addItemStackToInventory(stack)) {
                spawnItemAtPlayer(player, stack);
            }
        }
    }

    private static void spawnItemAtPlayer(EntityPlayer player, ItemStack stack) {
        EntityItem item = new EntityItem(player.world, player.posX + 0.5D, player.posY + 0.5D, player.posZ + 0.5D, stack);
        player.world.spawnEntity(item);
        if (!(player instanceof FakePlayer))
            item.onCollideWithPlayer(player);
    }

    public static void addTeam(Team team) {
        team.setId(teams.size());
        teams.add(team);
    }

    public static EntityPlayer getPlayerFromUsername(String playerName) {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(playerName);
    }

    public static EntityPlayer getPlayer(String uuid) {
        if(uuid.split("-").length == 5){
            return getPlayer(UUID.fromString(uuid));
        }
        return getPlayerFromUsername(uuid);
    }

    public static EntityPlayer getPlayer(UUID uuid) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        return server == null ? null : server.getPlayerList().getPlayerByUUID(uuid);
    }

    public static void remove(EntityPlayer player) {
        data.remove(player.getPersistentID());
    }

    public static boolean hasData(EntityPlayer player) {
        return data.containsKey(player.getGameProfile().getId());
    }

    public static boolean hasData(UUID uuid) {
        return data.containsKey(uuid);
    }

    public DeathStats getDeathStat() {
        return deathStat;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getName() throws IllegalArgumentException{
        if (this.name == null) {
            // possible fix for #238
            EntityPlayer player = QuestingData.getPlayer(this.playerId);
            if (player != null) {
                this.name = player.getDisplayNameString();
            } else {
                this.name = "";
            }
        }
        return this.name;
    }

    private void createGroupData() {
        groupData = new HashMap<>();
        Group.getGroups().keySet().forEach(this::createGroupData);
    }

    private void createGroupData(UUID groupId) {
        groupData.put(groupId, new GroupData());
    }

    public int getLives() {
        boolean shareLives = getTeam().isSharingLives();
        return shareLives ? getTeam().getSharedLives() : getRawLives();
    }

    public int getLivesToStayAlive() {
        boolean shareLives = getTeam().isSharingLives();
        return shareLives ? getTeam().getPlayerCount() : 1;
    }

    public int getRawLives() {
        return lives;
    }

    public void setRawLives(int lives) {
        this.lives = lives;
    }

    public QuestData getQuestData(UUID questId) {
        return getTeam().getQuestData(questId);
    }

    public void setQuestData(UUID questId, QuestData data) {
        getTeam().setQuestData(questId, data);
    }

    public GroupData getGroupData(UUID groupId) {
        if (!groupData.containsKey(groupId))
            createGroupData(groupId);
        return groupData.get(groupId);
    }

    public int addLives(EntityPlayer player, int amount) {
        int max = ModConfig.MAXLIVES;
        int i = getRawLives() + amount;

        if (i <= max) {
            this.lives = i;
        } else {
            this.lives = max;
        }

        getTeam().refreshTeamLives();
        return this.lives;
    }

    public void removeLifeAndSendMessage(@Nonnull EntityPlayer player) {
        boolean isDead = !removeLives(player, 1);
        if (!isDead) {
            player.sendMessage(new TextComponentString(Translator.translate(getLives() != 1, "hqm.message.lostLife", getLives())));
        }
        if (getTeam().isSharingLives()) {
            for (PlayerEntry entry : getTeam().getPlayers()) {
                if (entry.isInTeam() && !entry.getUUID().equals(player.getPersistentID())) {
                    EntityPlayer other = getPlayer(entry.getUUID());
                    if (other != null) {
                        other.sendMessage(new TextComponentString(
                                Translator.translate(getLives() != 1, "hqm.message.lostTeamLife", player.getDisplayNameString(), (isDead ? " " + Translator.translate("hqm.message.andBanned") : ""), getLives())));
                    }
                }
            }
        }

    }

    public boolean removeLives(@Nonnull EntityPlayer player, int amount) {
        boolean shareLives = getTeam().isSharingLives();

        if (shareLives) {
            int dif = Math.min(this.lives - 1, amount);
            amount -= dif;
            this.lives -= dif;

            while (amount > 0) {
                int players = 0;
                for (PlayerEntry entry : QuestingData.getQuestingData(player).getTeam().getPlayers()) {
                    if (!entry.getUUID().equals(player.getPersistentID()) && QuestingData.getQuestingData(entry.getUUID()).getRawLives() > 1) {
                        players++;
                    }
                }
                if (players == 0) {
                    break;
                } else {
                    int id = (int) (Math.random() * players);
                    for (PlayerEntry entry : QuestingData.getQuestingData(player).getTeam().getPlayers()) {
                        if (!entry.getUUID().equals(player.getPersistentID()) && QuestingData.getQuestingData(entry.getUUID()).getRawLives() > 1) {
                            if (id == 0) {
                                QuestingData.getQuestingData(entry.getUUID()).lives--;
                                amount--;
                                break;
                            }
                            id--;
                        }
                    }
                }
            }

            this.lives -= amount;
        } else {
            this.lives = getRawLives() - amount;
        }

        if (player instanceof EntityPlayerMP) {
            NetworkManager.sendToPlayer(new LivesUpdate(this.playerId, this.lives), (EntityPlayerMP) player);
        }
        getTeam().refreshTeamLives();
        try {
            if (getLives() < getLivesToStayAlive()) {
                outOfLives(player);
                return false;
            }

            return true;
        } finally {
            TeamStats.refreshTeam(team);
        }
    }

    public void die(@Nonnull EntityPlayer player) {
        if(QuestingData.isHardcoreActive()){
            removeLifeAndSendMessage(player);
        }
    }

    /**
     * Deletes the world or bans the player from the server. This is handled on the server side
     *
     * @param player The player that should be banned
     */
    //todo reinspect this whole behaviour
    private void outOfLives(EntityPlayer player) {
        QuestingData data = QuestingData.getQuestingData(player);
        Team team = data.getTeam();
        if (!team.isSingle() && !teams.isEmpty()) {
            team.removePlayer(player.getPersistentID());
            if (team.getPlayerCount() == 0) {
                team.deleteTeam();
            } else {
                team.refreshTeamData(TeamUpdateSize.ALL);
            }
        }

        player.inventory.clear(); //had some problem with tconstruct, clear all items to prevent it

        MinecraftServer mcServer = player.getServer();

        if (mcServer.isSinglePlayer() && player.getName().equals(mcServer.getServerOwner())) {
            ((EntityPlayerMP) player).connection.disconnect(Translator.translateComponent("hqm.message.gameOver"));

            /*ReflectionHelper.setPrivateValue(MinecraftServer.class, mcServer, true, 41);
            mcServer.getActiveAnvilConverter().flushCache();

            mcServer.getActiveAnvilConverter().deleteWorldDirectory(mcServer.worldServers[0].getSaveHandler().getWorldDirectoryName());
            mcServer.initiateShutdown();*/
            // @todo: is this correct? lets fucking not delete the world
            //mcServer.getActiveAnvilConverter().flushCache();
            //mcServer.getActiveAnvilConverter().deleteWorldDirectory(mcServer.worldServers[0].getSaveHandler().getWorldDirectory().getName());
            //mcServer.initiateShutdown();
//            mcServer.deleteWorldAndStopServer();

        } else {
            String setBanReason = "Out of lives in Hardcore Questing mode";
            String setBannedBy = "HQM";

            UserListBansEntry userlistbansentry = new UserListBansEntry(player.getGameProfile(), (Date) null, setBannedBy, (Date) null, setBanReason);
            mcServer.getPlayerList().getBannedPlayers().addEntry(userlistbansentry);

            //mcServer.getConfigurationManager().getBannedPlayers().put(banentry);
            ((EntityPlayerMP) player).connection.disconnect(Translator.translateComponent("hqm.message.gameOver"));
            SoundHandler.playToAll(Sounds.DEATH);
        }


    }

    @Nonnull
    public Team getTeam() {
        if(!this.team.isSingle()){
            List<Team> teams = getTeams();
            if(teams.size() > this.team.getId()){
                this.team = teams.get(this.team.getId());
            }
        }
        return this.team;
    }

    public void setTeam(@Nullable Team team) {
        if(team == null){
            team = new Team(this.playerId);
        }
        this.team = team;
    }

    public Map<UUID, GroupData> getGroupData() {
        return groupData;
    }
}
