package hardcorequesting;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.ReflectionHelper;
import hardcorequesting.bag.Group;
import hardcorequesting.bag.GroupData;
import hardcorequesting.client.sounds.SoundHandler;
import hardcorequesting.client.sounds.Sounds;
import hardcorequesting.config.ModConfig;
import hardcorequesting.items.ModItems;
import hardcorequesting.network.*;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestData;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.BanEntry;
import net.minecraft.server.management.UserListBans;
import net.minecraft.server.management.UserListBansEntry;
import net.minecraft.server.management.UserListEntry;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StringUtils;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.util.FakePlayer;

import java.io.File;
import java.util.*;

public class QuestingData {


    private Team team;
    private int lives;
    private String name;
    private List<GroupData> groupData;
    public int selectedQuest = -1;
    public int selectedTask = -1;
    public boolean playedLore;
    public boolean receivedBook;


    private DeathStats deathStat;

    static HashMap<String, QuestingData> getData() {
        return data;
    }

    public DeathStats getDeathStat() {
        return deathStat;
    }

    public String getName() {
        return name;
    }

    private QuestingData(String name) {
        this.lives = defaultLives;
        this.name = name;
        this.team = new Team(name);
        createGroupData();
        deathStat = new DeathStats(name);
        data.put(name, this);
    }


    private void createGroupData() {
        groupData = new ArrayList<GroupData>();
        for (int i = 0; i < Group.size(); i++) {
            createGroupData(i);
        }
    }

    private void createGroupData(int id) {
        //int start = id >= Group.getGroups().size() ? 0 : id;
        int start = id >= groupData.size() ? groupData.size() : id;
        for (int i = start; i <= id; i++) {
            if (Group.getGroup(i) != null) {
                groupData.add(i, new GroupData());
            } else {
                groupData.add(i, null);
            }
        }
    }

    public int getLives() {
        boolean shareLives = getTeam().isSharingLives();
        if (shareLives) {
            return getTeam().getSharedLives();
        } else {
            return getRawLives();
        }
    }

    public int getLivesToStayAlive() {
        boolean shareLives = getTeam().isSharingLives();
        if (shareLives) {
            return getTeam().getPlayerCount();
        } else {
            return 1;
        }
    }

    public int getRawLives() {
        return lives;
    }

    public QuestData getQuestData(int id) {
        return getTeam().getQuestData(id);
    }

    public GroupData getGroupData(int id) {
        if (id < 0 || id >= Group.size()) return null;

        if (id >= groupData.size()) {
            createGroupData(id);
        }
        return groupData.get(id);
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

    public void removeLifeAndSendMessage(EntityPlayer player) {
        boolean isDead = !removeLives(player, 1);
        if (!isDead) {
            player.addChatMessage(new ChatComponentText(Translator.translate(getLives() != 1, "hqm.message.lostLife", getLives())));
        }
        if (getTeam().isSharingLives()) {
            for (Team.PlayerEntry entry : getTeam().getPlayers()) {
                if (entry.isInTeam() && !entry.getName().equals(getUserName(player))) {
                    EntityPlayer other = getPlayer(entry.getName());
                    if (other != null) {
                        other.addChatMessage(new ChatComponentText(
                                Translator.translate(getLives() != 1, "hqm.message.lostTeamLife", getUserName(player), (isDead ? " " + Translator.translate("hqm.message.andBanned") : ""), getLives())));
                    }
                }
            }
        }

    }

    public boolean removeLives(EntityPlayer player, int amount) {
        boolean shareLives = getTeam().isSharingLives();

        if (shareLives) {
            int dif = Math.min(this.lives - 1, amount);
            amount -= dif;
            this.lives -= dif;

            while (amount > 0) {
                int players = 0;
                for (Team.PlayerEntry entry : QuestingData.getQuestingData(player).getTeam().getPlayers()) {
                    if (!entry.getName().equals(getUserName(player)) && QuestingData.getQuestingData(entry.getName()).getRawLives() > 1) {
                        players++;
                    }
                }
                if (players == 0) {
                    break;
                } else {
                    int id = (int) (Math.random() * players);
                    for (Team.PlayerEntry entry : QuestingData.getQuestingData(player).getTeam().getPlayers()) {
                        if (!entry.getName().equals(getUserName(player)) && QuestingData.getQuestingData(entry.getName()).getRawLives() > 1) {
                            if (id == 0) {
                                QuestingData.getQuestingData(entry.getName()).lives--;
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

    public void die(EntityPlayer player) {
        if (!QuestingData.isHardcoreActive()) return;

        removeLifeAndSendMessage(player);
    }


    /**
     * Deletes the world or bans the player from the server. This is handled on the server side
     *
     * @param playerEntity The player that should be banned
     */
    private void outOfLives(EntityPlayer playerEntity) {
        QuestingData data = QuestingData.getQuestingData(playerEntity);
        Team team = data.getTeam();
        if (!team.isSingle() && !teams.isEmpty()) {
            team.removePlayer(QuestingData.getUserName(playerEntity));
            if (team.getPlayerCount() == 0) {
                team.deleteTeam();
            } else {
                team.refreshTeamData(Team.UpdateType.ALL);
            }
        }

        playerEntity.inventory.clearInventory(null, -1); //had some problem with tconstruct, clear all items to prevent it

        MinecraftServer mcServer = MinecraftServer.getServer();

        if (mcServer.isSinglePlayer() && playerEntity.getCommandSenderName().equals(mcServer.getServerOwner())) {
            ((EntityPlayerMP) playerEntity).playerNetServerHandler.kickPlayerFromServer(Translator.translate("hqm.message.gameOver"));

            /*ReflectionHelper.setPrivateValue(MinecraftServer.class, mcServer, true, 41);
            mcServer.getActiveAnvilConverter().flushCache();

            mcServer.getActiveAnvilConverter().deleteWorldDirectory(mcServer.worldServers[0].getSaveHandler().getWorldDirectoryName());
            mcServer.initiateShutdown();*/
            mcServer.deleteWorldAndStopServer();

        } else {
            String setBanReason = "Out of lives in Hardcore Questing mode";
            String setBannedBy = "HQM";

            UserListBansEntry userlistbansentry = new UserListBansEntry(playerEntity.getGameProfile(), (Date) null, setBannedBy, (Date) null, setBanReason);
            mcServer.getConfigurationManager().func_152608_h().func_152687_a(userlistbansentry);

            //mcServer.getConfigurationManager().getBannedPlayers().put(banentry);
            ((EntityPlayerMP) playerEntity).playerNetServerHandler.kickPlayerFromServer(Translator.translate("hqm.message.gameOver"));
            SoundHandler.playToAll(Sounds.DEATH);
        }


    }

    //keep all the red line code in one spot
    public static boolean isSinglePlayer() {
        return MinecraftServer.getServer().isSinglePlayer();
    }


    private static boolean hardcoreActive;
    private static boolean questActive;
    // private static boolean debugActive = false;
    public static int defaultLives;

   /* public static boolean isDebugActive() {
        return debugActive;
    }*/

    public static boolean isHardcoreActive() {
        return hardcoreActive;
    }

    public static boolean isQuestActive() {
        return questActive;
    }

    /*public static void activateDebug() {
        if(!debugActive)
            debugActive = true;
    }

    public static void deactivateDebug() {
        if(debugActive)
            debugActive = false;
    }*/

    public static void activateHardcore() {
        if (!hardcoreActive && !MinecraftServer.getServer().getEntityWorld().getWorldInfo().isHardcoreModeEnabled()) {
            hardcoreActive = true;
        }
    }

    public static void disableHardcore() {
        hardcoreActive = false;
    }

    public static void activateQuest() {
        if (!questActive) {
            questActive = true;
            for (String name : FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getAllUsernames()) {
                if (name != null) {
                    EntityPlayer player = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(name);
                    if (player != null) {
                        spawnBook(player);
                    }
                }
            }

        }
    }


    public static void deactivate() {
        if (hardcoreActive || questActive /*|| debugActive*/) {
            PacketHandler.reset();
            hardcoreActive = false;
            questActive = false;
            //debugActive = false;
            data = new HashMap<String, QuestingData>();
            teams = new ArrayList<Team>();
        }
    }

    private static HashMap<String, QuestingData> data = new HashMap<String, QuestingData>();
    private static List<Team> teams = new ArrayList<Team>();
    public static boolean autoHardcoreActivate;
    public static boolean autoQuestActivate;


    public static List<Team> getTeams() {
        return teams;
    }

    public static List<Team> getAllTeams() {
        List<Team> all = new ArrayList<Team>();
        for (Team team : getTeams()) {
            all.add(team);
        }
        for (QuestingData player : data.values()) {
            if (player.getTeam().isSingle()) {
                all.add(player.getTeam());
            }
        }
        return all;
    }

    public static QuestingData getQuestingData(EntityPlayer player) {
        return getQuestingData(getUserName(player));
    }

    public static String getUserName(EntityPlayer player) {
        String name = player.getGameProfile().getName();
        String override = PacketHandler.getOverriddenName(name);

        return override != null ? override : name;
    }

    public static QuestingData getQuestingData(String name) {
        if (!data.containsKey(name)) {
            new QuestingData(name);
        }

        return data.get(name);
    }


    /**
     * =============== SAVING/LOADING CODE ===============
     */

	/* This is saved as a header of the file and read again when it's loaded. This is used when it loads again so 
     * we can load it differently depending on the version. The version should manually increase if we change the
	 * way things are saved. This includes if we add extra things that has to be saved. If we can't treat different
	 * versions differently then files from old version (i.e. from before the user updated the mod) will count as
	 * corrupted since they can't be read. The version is sent as a parameter to the load constructor of the class.
	 * At the moment it's not used since there's only one version.
	 * 
	 * There's NO reason to increase this number each time you develop something new. Corrupt files doesn't matter when
	 * developing since that will only be our testing files. For instance, say we've released a version with only the
	 * lives (no quest book) and call that file version 0. Then when we want to save the quest book we increase FILE_VERSION
	 * to 1 and start developing. When it's released it's released as file version 1 even though the quest book's saving
	 * might have changed a few times during the development. The important things is that version 0 is still the old thing
	 * without the quest book so it won't try to load the quest book otherwise.
	 * 
	 * Files from newer versions are obviously not going to be able to be read. So if the player is still running a version
	 * with the file version of 0 but copies a file from file version 1 it's not going to works. That's very obvious and
	 * totally expected.
	 * 
	 * If you have any questions about the file version I'll be happy to answer them /Vswe
	 */
    public static final FileVersion FILE_VERSION = FileVersion.values()[FileVersion.values().length - 1];

    private static final FileHelper FILE_HELPER = new FileHelper() {
        @Override
        public void write(DataWriter dw) {
            saveAllData(dw);
        }

        @Override
        public void read(DataReader dr, FileVersion version) {
            readAllData(dr, version);
        }
    };

    private static final String path = "HardcoreQuesting/players.dat";

    /**
     * Loads all the Questing Data
     *
     * @param worldPath The path of the World's save path
     */
    public static void load(File worldPath, WorldServer world) {
        File file = new File(worldPath, path);
        deactivate();
        if (!file.exists()) {
            if (world.getWorldInfo().getWorldTotalTime() == 0) {
                if (autoHardcoreActivate) {
                    activateHardcore();
                }
                if (autoQuestActivate) {
                    activateQuest();
                }
            }

            return;
        }

        data.clear();
        teams.clear();

        FILE_HELPER.loadData(file);

    }

    /**
     * Saves all the QuestingData
     *
     * @param worldPath The path of the World's save path
     */
    public static void save(File worldPath, WorldServer world) {
        if (!(isHardcoreActive() || isQuestActive()) || Quest.isEditing) {
            return;
        }

        File file = new File(worldPath, path);

        FILE_HELPER.saveData(file);
    }


    public static void saveAllData(DataWriter dw) {
        dw.writeBoolean(isHardcoreActive());
        dw.writeBoolean(isQuestActive());

        dw.writeData(teams.size(), DataBitHelper.TEAMS);
        for (Team team : teams) {
            team.saveData(dw, false);
        }

        dw.writeData(data.values().size(), DataBitHelper.PLAYERS);
        for (QuestingData d : data.values()) {
            dw.writeString(d.name, DataBitHelper.NAME_LENGTH);
            d.saveData(dw, false);
        }
    }

    public static void readAllData(DataReader dr, FileVersion version) {
        if (version.lacks(FileVersion.SETS)) {
            activateHardcore();
            activateQuest();
        }

        if (version.lacks(FileVersion.QUESTS)) {
            while (dr.doesUnderlyingStreamHasMoreThanAByteOfData()) {
                new QuestingData(version, dr);
            }
        } else {
            if (dr.readBoolean()) {
                activateHardcore();
            }
            if (dr.readBoolean()) {
                activateQuest();
            }
            if (version.contains(FileVersion.TEAMS)) {
                int teamAmount = dr.readData(DataBitHelper.TEAMS);
                for (int i = 0; i < teamAmount; i++) {
                    Team team = new Team(null);
                    addTeam(team);
                    team.loadData(version, dr, false);
                }
            }

            int amount = dr.readData(DataBitHelper.PLAYERS);
            for (int i = 0; i < amount; i++) {
                new QuestingData(version, dr);
            }
        }


    }

    /**
     * Creates a new QuestingData from the given DataInputStream with the given version
     *
     * @param version The FILE_VERSION that the data is from
     */
    private QuestingData(FileVersion version, DataReader dr) {
        this.name = dr.readString(version.lacks(FileVersion.QUESTS) ? DataBitHelper.BYTE : DataBitHelper.NAME_LENGTH);

        deathStat = new DeathStats(name);
        createGroupData();
        loadData(version, dr, version.lacks(FileVersion.SETS));

        if (!data.containsKey(name)) {
            data.put(name, this);
        }
        team.postRead(this, version);

        //When filling in data for a player on a team, we sometimes follow a codepath that causes us to attempt to
        //access its teammates.  If those teammates haven't been loaded yet, we'll generate dummy versions of the
        //players and put them into the questing data db (for some reason??) then
        //we won't load the player's data when it comes time to acutally load them from the file because the player is
        //already in the database (why???) and so that player will have their progress wiped.  If that player is
        //the team leader, then the team's progress gets nuked.

        //This can be avoided by checking the database after loading a player & removing dummy teammates.  It should
        //probably be avoided by dropping a nuke on the code responsible, but you gotta do what ya gotta do.
        for (Object teammateObj : team.getPlayers()) {
            Team.PlayerEntry teammate = (Team.PlayerEntry) teammateObj;

            if (!teammate.isInTeam())
                continue;

            String name = teammate.getName();
            if (QuestingData.data.containsKey(name)) {
                QuestingData teammatePlayer = (QuestingData) QuestingData.data.get(name);
                if (teammatePlayer.getTeam().getId() != team.getId())
                    QuestingData.data.remove(name);
            }
        }
    }


    private void saveData(DataWriter dw, boolean light) {
        if (isHardcoreActive()) {
            dw.writeData(this.lives, DataBitHelper.LIVES);
        }

        if (isQuestActive()) {
            Quest.serverTicker.save(dw);

            if (selectedQuest != -1 && selectedTask != -1) {
                dw.writeBoolean(true);
                dw.writeData(selectedQuest, DataBitHelper.QUESTS);
                dw.writeData(selectedTask, DataBitHelper.TASKS);
            } else {
                dw.writeBoolean(false);
            }

            dw.writeBoolean(playedLore);
            dw.writeBoolean(receivedBook);

            if (team.isSingle() || light) {
                dw.writeBoolean(true);
                team.saveData(dw, light);
            } else {
                dw.writeBoolean(false);
                dw.writeData(team.getId(), DataBitHelper.TEAMS);
            }


            if (!light) {
                int groupCount = 0;
                for (Group group : Group.getGroups()) {
                    if (group != null && group.getLimit() > 0) {
                        groupCount++;
                    }
                }
                dw.writeData(groupCount, DataBitHelper.GROUP_COUNT);
                for (int i = 0; i < groupData.size(); i++) {
                    if (Group.getGroup(i) != null && Group.getGroup(i).getLimit() > 0) {
                        dw.writeData(i, DataBitHelper.GROUP_COUNT);
                        dw.writeData(getGroupData(i).retrieved, DataBitHelper.LIMIT);
                    }
                }
            }
        }

        DeathStats.save(this, dw, light);
        if (light) {
            TeamStats.save(dw);
        }
    }

    private void loadData(FileVersion version, DataReader dr, boolean light) {
        if (isHardcoreActive()) {
            this.lives = dr.readData(DataBitHelper.LIVES);
        } else {
            this.lives = defaultLives;
        }

        if (isQuestActive() && version.contains(FileVersion.QUESTS)) {
            if (version.contains(FileVersion.REPEATABLE_QUESTS)) {
                if (light) {
                    Quest.clientTicker.load(dr);
                } else {
                    Quest.serverTicker.load(dr);
                }
            }

            if (dr.readBoolean()) {
                selectedQuest = dr.readData(DataBitHelper.QUESTS);
                selectedTask = dr.readData(DataBitHelper.TASKS);
            } else {
                selectedQuest = -1;
                selectedTask = -1;
            }

            if (version.contains(FileVersion.LORE_AUDIO)) {
                this.playedLore = dr.readBoolean();
                this.receivedBook = dr.readBoolean();
            }

            if (version.lacks(FileVersion.TEAMS) || dr.readBoolean()) {
                team = new Team(getName());
                team.loadData(version, dr, light);
            } else {
            	Team _team = dr.readData(DataBitHelper.TEAMS);
            	if (teams.contains(_team)) {
                	team = teams.get(team);
            	}
            }


            if (!light) {
                if (version.contains(FileVersion.BAG_LIMITS)) {
                    int groupCount = dr.readData(DataBitHelper.GROUP_COUNT);
                    for (int i = 0; i < groupCount; i++) {
                        int id = dr.readData(DataBitHelper.GROUP_COUNT);
                        if (getGroupData(id) != null) {
                            getGroupData(id).retrieved = dr.readData(DataBitHelper.LIMIT);
                        }
                    }
                }
            }
        }

        if (version.contains(FileVersion.DEATHS)) {
            DeathStats.load(this, dr, light);
        }

        if (light) {
            TeamStats.load(dr);
        }
    }

    private void sendDataToClient(DataWriter dw, String playerName) {
        dw.writeBoolean(isHardcoreActive());
        dw.writeBoolean(isQuestActive());

        saveData(dw, true);
        PacketHandler.sendToPlayer(playerName, dw);
    }

    public void sendDataToClientAndOpenInterface(EntityPlayer player, String name) {
        EventHandler.instance().onEvent(new EventHandler.BookOpeningEvent(QuestingData.getUserName(player), name != null, QuestingData.getUserName(player).equals(player.getGameProfile().getName())));

        PacketHandler.add(player, name);
        DataWriter dw = PacketHandler.getWriter(PacketId.OPEN_INTERFACE);
        dw.writeBoolean(name != null);
        if (name != null) {
            dw.writeString(QuestingData.getUserName(player), DataBitHelper.NAME_LENGTH);
        }
        sendDataToClient(dw, QuestingData.getUserName(player));
    }

    public void refreshClientData(String playerName) {
        sendDataToClient(PacketHandler.getWriter(PacketId.REFRESH_INTERFACE), playerName);
    }


    public void receiveDataFromServer(DataReader dr) {
        hardcoreActive = dr.readBoolean();
        questActive = dr.readBoolean();


        loadData(FILE_VERSION, dr, true);
    }


    public static void disableVanillaHardcore(ICommandSender sender) {
        if (MinecraftServer.getServer().getEntityWorld().getWorldInfo().isHardcoreModeEnabled()) {
            sender.addChatMessage(new ChatComponentTranslation("hqm.message.vanillaHardcore"));
            try {
                ReflectionHelper.setPrivateValue(WorldInfo.class, sender.getEntityWorld().getWorldInfo(), false, 20);
            } catch (Throwable ex) {
                ex.printStackTrace();
            }

            if (!MinecraftServer.getServer().getEntityWorld().getWorldInfo().isHardcoreModeEnabled()) {
                sender.addChatMessage(new ChatComponentTranslation("hqm.message.vanillaHardcoreOverride"));
            }
        }
    }

    public static void spawnBook(EntityPlayer player) {
        if (!Quest.isEditing && !player.worldObj.isRemote && ModConfig.spawnBook && !QuestingData.getQuestingData(player).receivedBook && QuestingData.isQuestActive()) {
            QuestingData.getQuestingData(player).receivedBook = true;
            ItemStack diary = new ItemStack(ModItems.book);
            if (!player.inventory.addItemStackToInventory(diary)) {
                spawnItemAtPlayer(player, diary);
            }
        }
    }

    private static void spawnItemAtPlayer(EntityPlayer player, ItemStack stack) {
        EntityItem item = new EntityItem(player.worldObj, player.posX + 0.5D, player.posY + 0.5D, player.posZ + 0.5D, stack);
        player.worldObj.spawnEntityInWorld(item);
        if (!(player instanceof FakePlayer))
            item.onCollideWithPlayer(player);
    }

    public Team getTeam() {
        if (!team.isSingle() && !getTeams().isEmpty()) {
            team = getTeams().get(team.getId());
        }
        return team;
    }

    public static void addTeam(Team team) {
        team.setId(teams.size());
        teams.add(team);
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public static EntityPlayer getPlayer(String playerName) {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152612_a(playerName);
    }

    public static boolean hasData(String playerName) {
        if (data.containsKey(playerName)) {
            return true;
        } else {
            EntityPlayer player = getPlayer(playerName);
            return player != null && player.getGameProfile().getName().equals(playerName);
        }
    }


    public static void remove(EntityPlayer player) {
        data.remove(getUserName(player));
    }


    public static boolean hasData(EntityPlayer player) {
        return data.containsKey(player.getGameProfile().getName());
    }


}
