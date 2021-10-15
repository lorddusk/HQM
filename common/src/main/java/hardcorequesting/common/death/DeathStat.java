package hardcorequesting.common.death;

import com.google.common.collect.ImmutableList;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
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
    public MutableComponent getName() throws IllegalArgumentException {
        if (Minecraft.getInstance().level != null) {
            Player player = Minecraft.getInstance().level.getPlayerByUUID(this.uuid);
            if (player != null) {
                cachedName = player.getScoreboardName();
            }
        }
    
        return Translator.text(Objects.requireNonNullElse(cachedName, "<invalid>"));
    }
    
    public String getCachedName() {
        return cachedName;
    }
    
    public void setCachedName(String cachedName) {
        this.cachedName = cachedName;
    }
    
    public List<FormattedText> getDescription(DeathType type) {
        return Collections.singletonList(type.getName().append(": " + deaths.get(type)));
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
        
        private static final ChatFormatting[] colourPrefixes = {ChatFormatting.YELLOW, ChatFormatting.GRAY, ChatFormatting.GOLD};
        private static final String[] placePrefixes = {"first", "second", "third"};
        private final Map<DeathType, List<FormattedText>> messages = new EnumMap<>(DeathType.class);
        
        public DeathStatBest(List<DeathStat> clientDeathList) {
            super(null);
            for (DeathType type : DeathType.values()) {
                clientDeathList.sort(deathTypeComparator.get(type));
                if (clientDeathList.isEmpty()) {
                    deaths.put(type, 0);
                    messages.put(type, Collections.singletonList(Translator.translatable("hqm.deathStat.noOneDied").withStyle(ChatFormatting.DARK_RED)));
                } else {
                    deaths.put(type, clientDeathList.get(0).getDeaths(type));
                    ImmutableList.Builder<FormattedText> builder = ImmutableList.builder();
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
    
                        MutableComponent player = clientDeathList.get(j).getName()
                                .append(": " + clientDeathList.get(j).getDeaths(type)).withStyle(ChatFormatting.WHITE);
                        builder.add(Translator.translatable("hqm.deathStat." + placePrefixes[standing]).withStyle(colourPrefixes[standing])
                                .append(" ").append(player));
                    }
                    messages.put(type, builder.build());
                }
            }
        }
        
        @Override
        public MutableComponent getName() {
            return Translator.translatable("hqm.deathStat.worstPlayers");
        }
        
        @Override
        public List<FormattedText> getDescription(DeathType type) {
            List<FormattedText> description = new ArrayList<>();
            description.add(type.getName());
            description.add(FormattedText.EMPTY);
            description.addAll(messages.get(type));
            return description;
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
        public List<FormattedText> getDescription(DeathType type) {
            List<FormattedText> description = new ArrayList<>(super.getDescription(type));
            description.add(FormattedText.EMPTY);
            int count = counts.get(type);
            if (count == 0)
                description.add(Translator.translatable("hqm.deathStat.noOneDied").withStyle(ChatFormatting.DARK_RED));
            else
                description.add(Translator.plural("hqm.player", count).append(" ")
                        .append(Translator.translatable("hqm.deathStat.diedThisWay")).withStyle(ChatFormatting.DARK_GREEN));
            return description;
        }
        
        @Override
        public MutableComponent getName() {
            return Translator.translatable("hqm.deathStat.everyone");
        }
    }
}
