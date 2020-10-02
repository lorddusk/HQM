package hardcorequesting.common.tileentity;

import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestingData;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.team.PlayerEntry;
import hardcorequesting.common.team.Team;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public enum TrackerType {
    TEAM("team") {
        @Override
        public int getMeta(TrackerBlockEntity tracker, Quest quest, int radius) {
            int meta = 0;
            for (Team team : QuestingDataManager.getInstance().getTeams()) {
                if (team.getQuestData(quest.getQuestId()).completed) {
                    boolean valid = radius == 0;
                    valid = isValid(valid, team, tracker, radius);
                    if (valid) {
                        meta++;
                        if (meta == 15)
                            break;
                    }
                }
            }
            return meta;
        }
    },
    PLAYER("player") {
        @Override
        public int getMeta(TrackerBlockEntity tracker, Quest quest, int radius) {
            int meta = 0;
            for (Team team : QuestingDataManager.getInstance().getTeams()) {
                if (team.getQuestData(quest.getQuestId()).completed) {
                    
                    for (PlayerEntry entry : team.getPlayers()) {
                        if (entry.isInTeam()) {
                            boolean valid = radius == 0;
                            if (!valid) {
                                Player player = QuestingData.getPlayer(entry.getUUID());
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
    PROGRESS_MAX("progressMax") {
        @Override
        public int getMeta(TrackerBlockEntity tracker, Quest quest, int radius) {
            int meta = 0;
            for (Team team : QuestingDataManager.getInstance().getTeams()) {
                int newMeta = (int) (quest.getProgress(team) * 15);
                if (newMeta > meta) {
                    boolean valid = radius == 0;
                    valid = isValid(valid, team, tracker, radius);
                    if (valid)
                        meta = newMeta;
                }
            }
            return meta;
        }
    },
    PROGRESS_CLOSE("progressClose") {
        @Override
        public int getMeta(TrackerBlockEntity tracker, Quest quest, int radius) {
            double closest = 0;
            Player closestPlayer = null;
            for (ServerPlayer player : HardcoreQuestingCore.getServer().getPlayerList().getPlayers()) {
                double distance = player.distanceToSqr(tracker.getBlockPos().getX() + 0.5, tracker.getBlockPos().getY() + 0.5, tracker.getBlockPos().getZ() + 0.5);
                if (closestPlayer == null || distance < closest) {
                    closest = distance;
                    closestPlayer = player;
                }
            }
            
            if (closestPlayer != null) {
                return (int) (quest.getProgress(QuestingDataManager.getInstance().getQuestingData(closestPlayer).getTeam()) * 15);
            } else {
                return 0;
            }
        }
    };
    
    
    private String id;
    
    TrackerType(String name) {
        this.id = name;
    }
    
    private static boolean isPlayerWithinRadius(TrackerBlockEntity tracker, Player player, int radius) {
        return player.distanceToSqr(tracker.getBlockPos().getX() + 0.5, tracker.getBlockPos().getY() + 0.5, tracker.getBlockPos().getZ() + 0.5) < radius * radius;
    }
    
    private static boolean isValid(boolean valid, Team team, TrackerBlockEntity tracker, int radius) {
        if (!valid) {
            for (PlayerEntry entry : team.getPlayers()) {
                if (entry.isInTeam()) {
                    Player player = QuestingData.getPlayer(entry.getUUID());
                    if (player != null && isPlayerWithinRadius(tracker, player, radius))
                        return true;
                }
            }
        }
        return valid;
    }
    
    public String getName() {
        return I18n.get("hqm.tracker." + id + ".title");
    }
    
    public String getDescription() {
        return I18n.get("hqm.tracker." + id + ".desc");
    }
    
    public abstract int getMeta(TrackerBlockEntity tracker, Quest quest, int radius);
}
