package hardcorequesting.tileentity;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.team.PlayerEntry;
import hardcorequesting.team.Team;
import hardcorequesting.util.Translator;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public enum TrackerType {
    TEAM("team") {
        @Override
        public int getMeta(TrackerBlockEntity tracker, Quest quest, int radius) {
            int meta = 0;
            for (Team team : QuestingData.getAllTeams()) {
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
            for (Team team : QuestingData.getAllTeams()) {
                if (team.getQuestData(quest.getQuestId()).completed) {
                    
                    for (PlayerEntry entry : team.getPlayers()) {
                        if (entry.isInTeam()) {
                            boolean valid = radius == 0;
                            if (!valid) {
                                PlayerEntity player = QuestingData.getPlayer(entry.getUUID());
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
            for (Team team : QuestingData.getAllTeams()) {
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
            PlayerEntity closestPlayer = null;
            for (ServerPlayerEntity player : HardcoreQuesting.getServer().getPlayerManager().getPlayerList()) {
                double distance = player.squaredDistanceTo(tracker.getPos().getX() + 0.5, tracker.getPos().getY() + 0.5, tracker.getPos().getZ() + 0.5);
                if (closestPlayer == null || distance < closest) {
                    closest = distance;
                    closestPlayer = player;
                }
            }
            
            if (closestPlayer != null) {
                return (int) (quest.getProgress(QuestingData.getQuestingData(closestPlayer).getTeam()) * 15);
            } else {
                return 0;
            }
        }
    };
    
    
    private String id;
    
    TrackerType(String name) {
        this.id = name;
    }
    
    private static boolean isPlayerWithinRadius(TrackerBlockEntity tracker, PlayerEntity player, int radius) {
        return player.squaredDistanceTo(tracker.getPos().getX() + 0.5, tracker.getPos().getY() + 0.5, tracker.getPos().getZ() + 0.5) < radius * radius;
    }
    
    private static boolean isValid(boolean valid, Team team, TrackerBlockEntity tracker, int radius) {
        if (!valid) {
            for (PlayerEntry entry : team.getPlayers()) {
                if (entry.isInTeam()) {
                    PlayerEntity player = QuestingData.getPlayer(entry.getUUID());
                    if (player != null && isPlayerWithinRadius(tracker, player, radius))
                        return true;
                }
            }
        }
        return valid;
    }
    
    public String getName() {
        return I18n.translate("hqm.tracker." + id + ".title");
    }
    
    public String getDescription() {
        return I18n.translate("hqm.tracker." + id + ".desc");
    }
    
    public abstract int getMeta(TrackerBlockEntity tracker, Quest quest, int radius);
}
