package hardcorequesting.common.death;

import hardcorequesting.common.client.interfaces.GuiColor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;

import java.util.*;

public class DeathStat {
    private static final Map<DeathType, Comparator<DeathStat>> deathTypeComparator = new EnumMap<>(DeathType.class);
    
    static {
        for (DeathType type : DeathType.values()) {
            deathTypeComparator.put(type, Comparator.comparingInt(stat -> stat.getDeaths(type)));
        }
    }
    
    protected final Map<DeathType, Integer> deaths = new EnumMap<>(DeathType.class);
    {
        for (DeathType type : DeathType.values()) {
            deaths.put(type, 0);
        }
    }
    private final UUID uuid;
    private String cachedName;
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
            if (player != null) {
                return cachedName = player.getScoreboardName();
            }
        }
    
        if (cachedName != null) {
            return cachedName;
        }
        
        return "<invalid>";
    }
    
    public String getCachedName() {
        return cachedName;
    }
    
    public void setCachedName(String cachedName) {
        this.cachedName = cachedName;
    }
    
    public String getDescription(DeathType type) {
        return type.getName() + ": " + deaths.get(type);
    }
    
    public void increaseDeath(DeathType type) {
        increaseDeath(type, 1, true);
    }
    
    public void increaseDeath(DeathType type, int count, boolean resync) {
        deaths.merge(type, count, Integer::sum);
        totalDeaths = -1;
        if (resync) DeathStatsManager.getInstance().resync();
    }
    
    public int getTotalDeaths() {
        if (totalDeaths == -1) {
            totalDeaths = 0;
            for (int death : deaths.values())
                totalDeaths += death;
        }
        
        return totalDeaths;
    }
    
    public int getDeaths(DeathType type) {
        return deaths.get(type);
    }
    
    public static class DeathStatBest extends DeathStat {
        
        private static final String[] colourPrefixes = {GuiColor.YELLOW.toString(), GuiColor.LIGHT_GRAY.toString(), GuiColor.ORANGE.toString()};
        private static final String[] placePrefixes = {"first", "second", "third"};
        private final Map<DeathType, String> messages = new EnumMap<>(DeathType.class);
        
        public DeathStatBest(List<DeathStat> clientDeathList) {
            super(null);
            for (DeathType type : DeathType.values()) {
                clientDeathList.sort(deathTypeComparator.get(type));
                if (clientDeathList.isEmpty()) {
                    deaths.put(type, 0);
                    messages.put(type, GuiColor.RED + I18n.get("hqm.deathStat.noOneDied"));
                } else {
                    deaths.put(type, clientDeathList.get(0).getDeaths(type));
                    StringBuilder builder = new StringBuilder();
                    int currentValue = 0;
                    int standing = 0;
                    for (int j = 0; j < clientDeathList.size(); j++) {
                        int value = clientDeathList.get(j).getDeaths(type);
                        if (value < currentValue) {
                            standing = j;
                            if (value == 0 || standing >= 3) {
                                break;
                            }
                        }
                        currentValue = value;
                        if (j != 0) {
                            builder.append("\n");
                        }
                        builder.append(colourPrefixes[standing]).append(I18n.get("hqm.deathStat." + placePrefixes[standing]));
                        builder.append(GuiColor.WHITE + " ").append(clientDeathList.get(j).getName()).append(": ").append(clientDeathList.get(j).getDeaths(type));
                    }
                    messages.put(type, builder.toString());
                }
            }
        }
        
        @Override
        public String getName() {
            return I18n.get("hqm.deathStat.worstPlayers");
        }
        
        @Override
        public String getDescription(DeathType type) {
            return type.getName() + "\n\n" + messages.get(type);
        }
    }
    
    public static class DeathStatTotal extends DeathStat {
        
        public Map<DeathType, Integer> counts = new EnumMap<>(DeathType.class);
        
        public DeathStatTotal(List<DeathStat> clientDeathList) {
            super(null);
            for (DeathType type : DeathType.values()) {
                int counter = 0;
                for (DeathStat deathStat : clientDeathList) {
                    deaths.merge(type, deathStat.getDeaths(type), Integer::sum);
                    if (deathStat.getDeaths(type) > 0) {
                        counter++;
                    }
                }
                counts.put(type, counter);
            }
        }
        
        @Override
        public String getDescription(DeathType type) {
            int count = counts.get(type);
            return super.getDescription(type) + "\n\n" +
                   (count == 0 ?
                           GuiColor.RED + I18n.get("hqm.deathStat.noOneDied") :
                           GuiColor.GREEN.toString() + count + " " + I18n.get("hqm.deathStat.player" + (count == 1 ? "" : "s")) + " " + I18n.get("hqm.deathStat.diedThisWay"));
        }
        
        @Override
        public String getName() {
            return I18n.get("hqm.deathStat.everyone");
        }
    }
}
