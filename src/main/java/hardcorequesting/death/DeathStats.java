package hardcorequesting.death;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.client.interfaces.GuiColor;
import hardcorequesting.io.SaveHandler;
import hardcorequesting.network.NetworkManager;
import hardcorequesting.network.message.DeathStatsMessage;
import hardcorequesting.quests.QuestingData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class DeathStats {
    private static final DeathComparator deathComparator = new DeathComparator(-1);
    private static final DeathComparator[] deathTypeComparator = new DeathComparator[DeathType.values().length];
    private static Map<UUID, DeathStats> deathMap;
    private static DeathStats[] clientDeathList;
    private static DeathStats clientBest;
    private static DeathStats clientTotal;
    
    static {
        for (int i = 0; i < deathTypeComparator.length; i++) {
            deathTypeComparator[i] = new DeathComparator(i);
        }
    }
    
    protected int[] deaths = new int[DeathType.values().length];
    private UUID uuid;
    private int totalDeaths = -1;
    
    public DeathStats(UUID uuid) {
        this.uuid = uuid;
    }
    
    public static DeathStats getBest() {
        return clientBest;
    }
    
    public static DeathStats getTotal() {
        return clientTotal;
    }
    
    public static List<DeathStats> getDeathStatsList() {
        return QuestingData.getData().values().stream().map(QuestingData::getDeathStat).collect(Collectors.toList());
    }
    
    public static void loadAll(boolean isClient, boolean remote) {
        deathMap = new HashMap<>();
        try {
            for (DeathStats stats : SaveHandler.loadDeaths(SaveHandler.getFile("deaths", remote)))
                deathMap.put(stats.uuid, stats);
            if (isClient) updateClientDeathList();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void saveAll() {
        try {
            SaveHandler.saveDeaths(SaveHandler.getLocalFile("deaths"));
        } catch (IOException e) {
            HardcoreQuesting.LOG.log(Level.INFO, "Failed saving bags");
        }
    }
    
    public static void resync() {
        NetworkManager.sendToAllPlayers(new DeathStatsMessage(HardcoreQuesting.loadingSide == EnvType.CLIENT));
    }
    
    private static void updateClientDeathList() {
        clientDeathList = new DeathStats[deathMap.size()];
        int id = 0;
        for (DeathStats deathStats : deathMap.values()) {
            deathStats.totalDeaths = -1;
            clientDeathList[id++] = deathStats;
        }
        
        clientBest = new DeathStatsBest();
        clientTotal = new DeathStatsTotal();
        
        Arrays.sort(clientDeathList, deathComparator);
    }
    
    public static DeathStats getDeathStats(UUID uuid) {
        DeathStats stats = deathMap.get(uuid);
        return stats == null ? new DeathStats(uuid) : stats;
    }
    
    public static DeathStats[] getDeathStats() {
        return clientDeathList;
    }
    
    public UUID getUuid() {
        return uuid;
    }
    
    @Environment(EnvType.CLIENT)
    public String getName() throws IllegalArgumentException {
        if (Minecraft.getInstance().level != null) {
            Player player = Minecraft.getInstance().level.getPlayerByUUID(this.uuid);
            if (player == null) {
                return "<invalid>";
            }
            return player.getScoreboardName();
        }
        return this.uuid.toString();
    }
    
    public String getDescription(int id) {
        return DeathType.values()[id].getName() + ": " + deaths[id];
    }
    
    public void increaseDeath(int id) {
        deaths[id]++;
        totalDeaths = -1;
        resync();
    }
    
    public void increaseDeath(int id, int count, boolean resync) {
        deaths[id] += count;
        totalDeaths = -1;
        if (resync) resync();
    }
    
    public int getTotalDeaths() {
        if (totalDeaths == -1) {
            totalDeaths = 0;
            for (int death : deaths)
                totalDeaths += death;
        }
        
        return totalDeaths;
    }
    
    public int getDeaths(int id) {
        return deaths[id];
    }
    
    private static class DeathComparator implements Comparator<DeathStats> {
        
        private int id;
        
        private DeathComparator(int id) {
            this.id = id;
        }
        
        @Override
        public int compare(DeathStats o1, DeathStats o2) {
            if (id == -1) {
                return ((Integer) o2.getTotalDeaths()).compareTo(o1.getTotalDeaths());
            } else {
                return ((Integer) o2.getDeaths(id)).compareTo(o1.getDeaths(id));
            }
        }
    }
    
    private static class DeathStatsBest extends DeathStats {
        
        private static final String[] colourPrefixes = {GuiColor.YELLOW.toString(), GuiColor.LIGHT_GRAY.toString(), GuiColor.ORANGE.toString()};
        private static final String[] placePrefixes = {"first", "second", "third"};
        private String[] messages = new String[DeathType.values().length];
        
        private DeathStatsBest() {
            super(null);
            for (int i = 0; i < messages.length; i++) {
                Arrays.sort(clientDeathList, deathTypeComparator[i]);
                if (clientDeathList.length < 1) {
                    deaths[i] = 0;
                    messages[i] = GuiColor.RED + I18n.get("hqm.deathStat.noOneDied");
                } else {
                    deaths[i] = clientDeathList[0].getDeaths(i);
                    messages[i] = "";
                    int currentValue = 0;
                    int standing = 0;
                    for (int j = 0; j < clientDeathList.length; j++) {
                        int value = clientDeathList[j].getDeaths(i);
                        if (value < currentValue) {
                            standing = j;
                            if (value == 0 || standing >= 3) {
                                break;
                            }
                        }
                        currentValue = value;
                        if (j != 0) {
                            messages[i] += "\n";
                        }
                        messages[i] += colourPrefixes[standing] + I18n.get("hqm.deathStat." + placePrefixes[standing]);
                        messages[i] += GuiColor.WHITE + " " + clientDeathList[j].getName() + ": " + clientDeathList[j].getDeaths(i);
                    }
                    
                }
            }
        }
        
        @Override
        public String getName() {
            return I18n.get("hqm.deathStat.worstPlayers");
        }
        
        @Override
        public String getDescription(int id) {
            return DeathType.values()[id].getName() + "\n\n" + messages[id];
        }
    }
    
    private static class DeathStatsTotal extends DeathStats {
        
        private int[] count = new int[DeathType.values().length];
        
        private DeathStatsTotal() {
            super(null);
            for (int i = 0; i < count.length; i++) {
                for (DeathStats deathStats : clientDeathList) {
                    deaths[i] += deathStats.getDeaths(i);
                    if (deathStats.getDeaths(i) > 0) {
                        count[i]++;
                    }
                }
                
            }
        }
        
        @Override
        public String getDescription(int id) {
            return super.getDescription(id) + "\n\n" +
                   (count[id] == 0 ?
                           GuiColor.RED + I18n.get("hqm.deathStat.noOneDied") :
                           GuiColor.GREEN.toString() + count[id] + " " + I18n.get("hqm.deathStat.player" + (count[id] == 1 ? "" : "s")) + " " + I18n.get("hqm.deathStat.diedThisWay"));
        }
        
        @Override
        public String getName() {
            return I18n.get("hqm.deathStat.everyone");
        }
    }
}
