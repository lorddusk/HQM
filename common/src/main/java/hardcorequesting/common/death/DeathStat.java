package hardcorequesting.common.death;

import hardcorequesting.common.client.interfaces.GuiColor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;

import java.util.Arrays;
import java.util.Comparator;
import java.util.UUID;

public class DeathStat {
    private static final DeathComparator[] deathTypeComparator = new DeathComparator[DeathType.values().length];
    
    static {
        for (int i = 0; i < deathTypeComparator.length; i++) {
            deathTypeComparator[i] = new DeathComparator(i);
        }
    }
    
    protected int[] deaths = new int[DeathType.values().length];
    private UUID uuid;
    int totalDeaths = -1;
    
    public DeathStat(UUID uuid) {
        this.uuid = uuid;
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
        DeathStatsManager.getInstance().resync();
    }
    
    public void increaseDeath(int id, int count, boolean resync) {
        deaths[id] += count;
        totalDeaths = -1;
        if (resync) DeathStatsManager.getInstance().resync();
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
    
    static class DeathComparator implements Comparator<DeathStat> {
        private int id;
        
        DeathComparator(int id) {
            this.id = id;
        }
        
        @Override
        public int compare(DeathStat o1, DeathStat o2) {
            if (id == -1) {
                return Integer.compare(o2.getTotalDeaths(), o1.getTotalDeaths());
            } else {
                return Integer.compare(o2.getDeaths(id), o1.getDeaths(id));
            }
        }
    }
    
    public static class DeathStatBest extends DeathStat {
        
        private static final String[] colourPrefixes = {GuiColor.YELLOW.toString(), GuiColor.LIGHT_GRAY.toString(), GuiColor.ORANGE.toString()};
        private static final String[] placePrefixes = {"first", "second", "third"};
        private String[] messages = new String[DeathType.values().length];
        
        public DeathStatBest(DeathStat[] clientDeathList) {
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
    
    public static class DeathStatTotal extends DeathStat {
        
        public int[] count = new int[DeathType.values().length];
        
        public DeathStatTotal(DeathStat[] clientDeathList) {
            super(null);
            for (int i = 0; i < count.length; i++) {
                for (DeathStat deathStat : clientDeathList) {
                    deaths[i] += deathStat.getDeaths(i);
                    if (deathStat.getDeaths(i) > 0) {
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
