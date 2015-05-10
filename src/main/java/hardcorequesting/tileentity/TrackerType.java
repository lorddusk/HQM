package hardcorequesting.tileentity;


import cpw.mods.fml.common.FMLCommonHandler;
import hardcorequesting.QuestingData;
import hardcorequesting.Team;
import hardcorequesting.quests.Quest;
import net.minecraft.entity.player.EntityPlayer;
import sun.net.www.content.text.plain;

public enum TrackerType {
    TEAM("Team Tracker", "Emits a redstone signal depending on how many teams that have completed the selected quest. Players without teams count as separate one person teams.") {
        @Override
        public int getMeta(TileEntityTracker tracker, Quest quest, int radius) {
            int meta = 0;
            for (Team team : QuestingData.getAllTeams()) {
                if (team.getQuestData(quest.getId()).completed) {
                    boolean valid = radius == 0;
                    if (!valid) {
                        for (Team.PlayerEntry entry : team.getPlayers()) {
                            if (entry.isInTeam()) {
                                EntityPlayer player = QuestingData.getPlayer(entry.getName());
                                if (player != null && isPlayerWithinRadius(tracker, player, radius)) {
                                    valid = true;
                                    break;
                                }
                            }
                        }
                    }

                    if (valid) {
                        meta++;
                        if (meta == 15) {
                            break;
                        }
                    }
                }
            }
            return meta;
        }
    },
    PLAYER("Player Tracker", "Emits a redstone signal depending on how many players that have completed this quest.") {
        @Override
        public int getMeta(TileEntityTracker tracker, Quest quest, int radius) {
            int meta = 0;
            for (Team team : QuestingData.getAllTeams()) {
                if (team.getQuestData(quest.getId()).completed) {

                    for (Team.PlayerEntry entry : team.getPlayers()) {
                        if (entry.isInTeam()) {
                            boolean valid = radius == 0;
                            if (!valid) {
                                EntityPlayer player = QuestingData.getPlayer(entry.getName());
                                if (player != null && isPlayerWithinRadius(tracker, player, radius)) {
                                    valid = true;
                                }
                            }

                            if (valid) {
                                meta++;
                                if (meta == 15) {
                                    break;
                                }
                            }
                        }
                    }

                }
            }
            return meta;
        }
    },
    PROGRESS_MAX("Progress Tracker (Max)", "Emits a redstone signal depending on the progress of this quest for the player/team that has the highest progress. This will only emit at full strength if someone has completed the quest.") {
        @Override
        public int getMeta(TileEntityTracker tracker, Quest quest, int radius) {
            int meta = 0;
            for (Team team : QuestingData.getAllTeams()) {
                int newMeta = (int)(quest.getProgress(team) * 15);
                if (newMeta > meta) {
                    boolean valid = radius == 0;
                    if (!valid) {
                        for (Team.PlayerEntry entry : team.getPlayers()) {
                            if (entry.isInTeam()) {
                                EntityPlayer player = QuestingData.getPlayer(entry.getName());
                                if (player != null && isPlayerWithinRadius(tracker, player, radius)) {
                                    valid = true;
                                    break;
                                }
                            }
                        }
                    }

                    if (valid) {
                        meta = newMeta;
                    }
                }
            }
            return meta;
        }
    },
    PROGRESS_CLOSE("Progress Tracker (Close)", "Emits a redstone signal depending on the progress of the nearest player. This will only emit at full strength if that player has completed the quest. This mode requires the players to be online, no matter the radius setting.") {
        @Override
        public int getMeta(TileEntityTracker tracker, Quest quest, int radius) {
            double closest = 0;
            EntityPlayer closestPlayer = null;
            for (String name : FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getAllUsernames()){
               EntityPlayer player = QuestingData.getPlayer(name);
                if (player != null) {
                    double distance = player.getDistanceSq(tracker.xCoord + 0.5, tracker.yCoord + 0.5, tracker.zCoord + 0.5);
                    if (closestPlayer == null || distance < closest) {
                        closest = distance;
                        closestPlayer = player;
                    }
                }
            }

            if (closestPlayer != null) {
                return (int)(quest.getProgress(QuestingData.getQuestingData(closestPlayer).getTeam()) * 15);
            }else{
                return 0;
            }
        }
    };


    private static boolean isPlayerWithinRadius(TileEntityTracker tracker, EntityPlayer player, int radius) {
        return player.getDistanceSq(tracker.xCoord + 0.5, tracker.yCoord + 0.5, tracker.zCoord + 0.5) < radius * radius;
    }

    private String name;
    private String description;

    TrackerType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public abstract int getMeta(TileEntityTracker tracker, Quest quest, int radius);
}
