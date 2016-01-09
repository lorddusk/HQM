package hardcorequesting.tileentity;

import net.minecraftforge.fml.common.FMLCommonHandler;
import hardcorequesting.QuestingData;
import hardcorequesting.Team;
import hardcorequesting.Translator;
import hardcorequesting.quests.Quest;
import net.minecraft.entity.player.EntityPlayer;

public enum TrackerType {
    TEAM("team") {
        @Override
        public int getMeta(TileEntityTracker tracker, Quest quest, int radius) {
            int meta = 0;
            for (Team team : QuestingData.getAllTeams()) {
                if (team.getQuestData(quest.getId()).completed) {
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
    PROGRESS_MAX("progressMax") {
        @Override
        public int getMeta(TileEntityTracker tracker, Quest quest, int radius) {
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
        public int getMeta(TileEntityTracker tracker, Quest quest, int radius) {
            double closest = 0;
            EntityPlayer closestPlayer = null;
            for (String name : FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getAllUsernames()) {
                EntityPlayer player = QuestingData.getPlayer(name);
                if (player != null) {
                    double distance = player.getDistanceSq(tracker.getPos().getX() + 0.5, tracker.getPos().getY() + 0.5, tracker.getPos().getZ() + 0.5);
                    if (closestPlayer == null || distance < closest) {
                        closest = distance;
                        closestPlayer = player;
                    }
                }
            }

            if (closestPlayer != null) {
                return (int) (quest.getProgress(QuestingData.getQuestingData(closestPlayer).getTeam()) * 15);
            } else {
                return 0;
            }
        }
    };


    private static boolean isPlayerWithinRadius(TileEntityTracker tracker, EntityPlayer player, int radius) {
        return player.getDistanceSq(tracker.getPos().getX() + 0.5, tracker.getPos().getY() + 0.5, tracker.getPos().getZ() + 0.5) < radius * radius;
    }

    private static boolean isValid(boolean valid, Team team, TileEntityTracker tracker, int radius) {
        if (!valid) {
            for (Team.PlayerEntry entry : team.getPlayers()) {
                if (entry.isInTeam()) {
                    EntityPlayer player = QuestingData.getPlayer(entry.getName());
                    if (player != null && isPlayerWithinRadius(tracker, player, radius))
                        return true;
                }
            }
        }
        return valid;
    }

    private String id;

    TrackerType(String name) {
        this.id = name;
    }

    public String getName() {
        return Translator.translate("hqm.tracker." + id + ".title");
    }

    public String getDescription() {
        return Translator.translate("hqm.tracker." + id + ".desc");
    }

    public abstract int getMeta(TileEntityTracker tracker, Quest quest, int radius);
}
