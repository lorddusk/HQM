package hardcorequesting.common.death;

import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.io.SaveHandler;
import hardcorequesting.common.network.NetworkManager;
import hardcorequesting.common.network.message.DeathStatsMessage;
import hardcorequesting.common.quests.QuestLine;
import hardcorequesting.common.quests.SimpleSerializable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import java.util.*;

public class DeathStatsManager extends SimpleSerializable {
    private static final Comparator<DeathStat> DEATH_COMPARATOR = Comparator.comparingInt(DeathStat::getTotalDeaths).reversed();
    public static final String FILE_PATH = "deaths.json";
    private final Map<UUID, DeathStat> deathMap = new HashMap<>();
    private List<DeathStat> clientDeathList = Collections.emptyList();
    private DeathStat clientBest;
    private DeathStat clientTotal;
    
    public DeathStatsManager() {
        super();
    }
    
    public static DeathStatsManager getInstance() {
        return QuestLine.getActiveQuestLine().deathStatsManager;
    }
    
    public DeathStat getDeathStat(Player player) {
        return getDeathStat(player.getUUID());
    }
    
    public DeathStat getDeathStat(UUID uuid) {
        return deathMap.computeIfAbsent(uuid, DeathStat::new);
    }
    
    public List<DeathStat> getDeathStats() {
        return clientDeathList;
    }
    
    private void updateClientDeathList() {
        clientDeathList = deathMap.values().stream().sorted(DEATH_COMPARATOR).toList();
        
        clientBest = new DeathStat.DeathStatBest(clientDeathList);
        clientTotal = new DeathStat.DeathStatTotal(clientDeathList);
    }
    
    public DeathStat getBest() {
        return clientBest;
    }
    
    public DeathStat getTotal() {
        return clientTotal;
    }
    
    public void resync() {
        NetworkManager.sendToAllPlayers(new DeathStatsMessage(HardcoreQuestingCore.platform.isClient()));
    }
    
    public List<DeathStat> getDeathStatsList() {
        return Lists.newArrayList(deathMap.values());
    }
    
    @Override
    public String saveToString() {
        return SaveHandler.save(getDeathStatsList(), new TypeToken<List<DeathStat>>() {}.getType());
    }
    
    public void writeSimplified(FriendlyByteBuf buf) {
        buf.writeInt(deathMap.size());
        for (Map.Entry<UUID, DeathStat> entry : deathMap.entrySet()) {
            buf.writeUUID(entry.getKey());
            Map<DeathType, Integer> deaths = entry.getValue().deaths;
            int count = 0;
            for (int death : deaths.values()) {
                if (death != 0) {
                    count++;
                }
            }
            buf.writeByte(count);
            for (DeathType type : DeathType.values()) {
                int deathCount = deaths.get(type);
                if (deathCount != 0) {
                    buf.writeByte(type.ordinal());
                    buf.writeShort((short) deathCount);
                }
            }
        }
    }
    
    public Map<UUID, DeathStat> readSimplified(FriendlyByteBuf buf) {
        Map<UUID, DeathStat> deathMap = new HashMap<>();
        int length = buf.readInt();
        for (int i = 0; i < length; i++) {
            UUID uuid = buf.readUUID();
            DeathStat stat = new DeathStat(uuid);
            int count = buf.readByte();
            for (int j = 0; j < count; j++) {
                DeathType type = DeathType.getClamped(buf.readByte());
                stat.increaseDeath(type, buf.readShort(), false);
            }
            deathMap.put(uuid, stat);
        }
        return deathMap;
    }
    
    @Override
    public String filePath() {
        return FILE_PATH;
    }
    
    @Override
    public boolean isData() {
        return true;
    }
    
    @Override
    public void clear() {
        deathMap.clear();
    }
    
    @Override
    public void loadFromString(String string) {
        SaveHandler.<List<DeathStat>>load(string, new TypeToken<List<DeathStat>>() {}.getType()).ifPresent(list ->
                list.forEach(stat -> deathMap.put(stat.getUuid(), stat)));
        if (HardcoreQuestingCore.platform.isClient())
            updateClientDeathList();
    }
}
