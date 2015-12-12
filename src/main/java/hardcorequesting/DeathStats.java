package hardcorequesting;

import hardcorequesting.client.interfaces.GuiColor;
import hardcorequesting.network.*;
import net.minecraft.entity.player.EntityPlayer;

import java.util.*;

public class DeathStats {
    private static Map<String, DeathStats> clientDeaths;
    private static DeathStats[] clientDeathList;
    private static DeathStats clientBest;
    private static DeathStats clientTotal;

    public static DeathStats getBest() {
        return clientBest;
    }

    public static DeathStats getTotal() {
        return clientTotal;
    }

    private String name;
    protected int[] deaths = new int[DeathType.values().length];

    public DeathStats(String name) {
        this.name = name;
    }

    public String getName() {
        return Translator.translate(name);
    }

    public String getDescription(int id) {
        return DeathType.values()[id].getName() + ": " + deaths[id];
    }

    public void increaseDeath(int id) {
        if (deaths[id] < DataBitHelper.DEATHS.getMaximum()) {
            deaths[id]++;
            refreshSync();
        }
    }

    public static void handlePacket(EntityPlayer player, DataReader dr) {
        String name = dr.readString(DataBitHelper.NAME_LENGTH);
        DeathStats deathStats = clientDeaths.get(name);
        if (deathStats == null) {
            deathStats = new DeathStats(name);
            clientDeaths.put(name, deathStats);
        }
        deathStats.load(dr);
        updateClientDeathList();
    }

    private int totalDeaths = -1;

    public int getTotalDeaths() {
        if (totalDeaths == -1) {
            totalDeaths = 0;
            for (int death : deaths) {
                totalDeaths += death;
            }
        }

        return totalDeaths;
    }

    public int getDeaths(int id) {
        return deaths[id];
    }

    public void load(DataReader dr) {
        for (int i = 0; i < deaths.length; i++) {
            deaths[i] = dr.readData(DataBitHelper.DEATHS);
        }
    }

    public void save(DataWriter dw) {
        for (int death : deaths) {
            dw.writeData(death, DataBitHelper.DEATHS);
        }
    }


    public static void load(QuestingData questingData, DataReader dr, boolean light) {
        if (light) {
            clientDeaths = new HashMap<String, DeathStats>();
            int count = dr.readData(DataBitHelper.PLAYERS);
            for (int i = 0; i < count; i++) {
                String name = dr.readString(DataBitHelper.NAME_LENGTH);
                DeathStats stats = new DeathStats(name);
                stats.load(dr);
                clientDeaths.put(name, stats);
            }
            updateClientDeathList();
        } else {
            questingData.getDeathStat().load(dr);
        }
    }

    public static void save(QuestingData questingData, DataWriter dw, boolean light) {
        if (light) {
            dw.writeData(QuestingData.getData().size(), DataBitHelper.PLAYERS);
            for (QuestingData q : QuestingData.getData().values()) {
                dw.writeString(q.getName(), DataBitHelper.NAME_LENGTH);
                q.getDeathStat().save(dw);
            }
        } else {
            questingData.getDeathStat().save(dw);
        }
    }

    public void refreshSync() {
        DataWriter dw = PacketHandler.getWriter(PacketId.DEATH_STATS_UPDATE);
        dw.writeString(name, DataBitHelper.NAME_LENGTH);
        save(dw);
        PacketHandler.sendToAllPlayersWithOpenBook(dw);
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

    private static final DeathComparator deathComparator = new DeathComparator(-1);
    private static final DeathComparator[] deathTypeComparator = new DeathComparator[DeathType.values().length];

    static {
        for (int i = 0; i < deathTypeComparator.length; i++) {
            deathTypeComparator[i] = new DeathComparator(i);
        }
    }

    private static void updateClientDeathList() {
        clientDeathList = new DeathStats[clientDeaths.size()];
        int id = 0;
        for (DeathStats deathStats : clientDeaths.values()) {
            deathStats.totalDeaths = -1;
            clientDeathList[id++] = deathStats;
        }

        clientBest = new DeathStatsBest();
        clientTotal = new DeathStatsTotal();

        Arrays.sort(clientDeathList, deathComparator);
    }

    public static DeathStats getDeathStats(String name) {
        return clientDeaths.get(name);
    }

    public static DeathStats[] getDeathStats() {
        return clientDeathList;
    }

    private static class DeathStatsBest extends DeathStats {
        private static final String[] colourPrefixes = {GuiColor.YELLOW.toString(), GuiColor.LIGHT_GRAY.toString(), GuiColor.ORANGE.toString()};
        private static final String[] placePrefixes = {"first", "second", "third"};
        private String[] messages = new String[DeathType.values().length];

        private DeathStatsBest() {
            super("hqm.deathStat.worstPlayers");
            for (int i = 0; i < messages.length; i++) {
                Arrays.sort(clientDeathList, deathTypeComparator[i]);
                deaths[i] = clientDeathList[0].getDeaths(i);
                if (clientDeathList[0].getDeaths(i) == 0) {
                    messages[i] = GuiColor.RED + Translator.translate("hqm.deathStat.noOneDied");
                } else {
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
                        messages[i] += colourPrefixes[standing] + Translator.translate("hqm.deathStat." + placePrefixes[standing]);
                        messages[i] += GuiColor.WHITE + " " + clientDeathList[j].getName() + ": " + clientDeathList[j].getDeaths(i);
                    }

                }
            }
        }

        @Override
        public String getDescription(int id) {
            return DeathType.values()[id].getName() + "\n\n" + messages[id];
        }
    }

    private static class DeathStatsTotal extends DeathStats {
        private int[] count = new int[DeathType.values().length];

        private DeathStatsTotal() {
            super("hqm.deathStat.everyone");
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
                            GuiColor.RED + Translator.translate("hqm.deathStat.noOneDied") :
                            GuiColor.GREEN.toString() + count[id] + " " + Translator.translate("hqm.deathStat.player" + (count[id] == 1 ? "" : "s")) + " " + Translator.translate("hqm.deathStat.diedThisWay"));
        }
    }
}
