package hardcorequesting.common.team;

import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.network.NetworkManager;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.quests.data.QuestData;
import hardcorequesting.common.reputation.Reputation;
import hardcorequesting.common.reputation.ReputationManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public enum TeamAction {
    CREATE {
        @Override
        public void process(Team team, Player player, String teamName) {
            if (team.isSingle()) {
                TeamManager manager = TeamManager.getInstance();
                if (teamName.length() == 0) {
                    return;
                }
                
                for (Team t : manager.getNamedTeams()) {
                    if (t.getName().equals(teamName)) {
                        TeamError.USED_NAME.sendToClient(player);
                        return;
                    }
                }
                
                team.setId(UUID.randomUUID());
                manager.addTeam(team);
                team.setName(teamName);
                team.refreshTeamData(TeamUpdateSize.ONLY_MEMBERS);
                
                Team.declineAll(player.getUUID());
                TeamLiteStat.refreshTeam(team);
                NetworkManager.sendToAllPlayers(TeamUpdateType.CREATE_TEAM.build(team));
                if (player instanceof ServerPlayer) {
                    NetworkManager.sendToPlayer(TeamUpdateType.JOIN_TEAM.build(team, player.getUUID().toString()), (ServerPlayer) player);
                }
            }
        }
    },
    INVITE {
        @Override
        public void process(Team team, Player player, String playerName) {
            ServerPlayer invitedPlayer = HardcoreQuestingCore.getServer().getPlayerList().getPlayerByName(playerName);
            if (!team.isSingle() && team.isOwner(player) && invitedPlayer != null) {
                QuestingDataManager manager = QuestingDataManager.getInstance();
                PlayerEntry invitedPlayerEntry = new PlayerEntry(invitedPlayer.getUUID(), false, false);
                
                if (!manager.hasData(invitedPlayer.getUUID())) {
                    TeamError.INVALID_PLAYER.sendToClient(player);
                    return;
                }
                
                if (!manager.getQuestingData(invitedPlayer.getUUID()).getTeam().isSingle()) {
                    TeamError.IN_PARTY.sendToClient(player);
                    return;
                }
                
                if (!team.getPlayers().contains(invitedPlayerEntry)) {
                    team.getPlayers().add(invitedPlayerEntry);
                    team.refreshTeamData(TeamUpdateSize.ONLY_MEMBERS);
                    manager.getQuestingData(invitedPlayer.getUUID()).getTeam().refreshTeamData(TeamUpdateSize.ONLY_MEMBERS);
                    manager.getQuestingData(invitedPlayer.getUUID()).getTeam().getInvites().add(team);
                    NetworkManager.sendToPlayer(TeamUpdateType.INVITE.build(team), invitedPlayer);
                }
            }
        }
    },
    ACCEPT {
        @Override
        public void process(Team team, Player player, String data) {
            if (team.isSingle()) {
                UUID acceptId = UUID.fromString(data);
                Team inviteTeam = TeamManager.getInstance().getByTeamId(acceptId);
                if (inviteTeam != null) {
                    int id = 0;
                    for (PlayerEntry entry : inviteTeam.getPlayers()) {
                        if (entry.isInTeam()) {
                            id++;
                        } else if (entry.getUUID().equals(player.getUUID())) {
                            entry.setBookOpen(true);
                            entry.setInTeam(true);
                            QuestingDataManager.getInstance().getQuestingData(player).setTeam(inviteTeam);
                            
                            for (UUID questId : inviteTeam.getQuestData().keySet()) {
                                QuestData joinData = team.getQuestData().get(questId);
                                QuestData questData = inviteTeam.getQuestData().get(questId);
                                if (questData != null) {
                                    boolean[] old = questData.reward;
                                    questData.reward = new boolean[old.length + 1];
                                    for (int j = 0; j < questData.reward.length; j++) {
                                        if (j == id) {
                                            questData.reward[j] = joinData.reward[0]; //you keep your reward
                                        } else if (j < id) {
                                            questData.reward[j] = old[j];
                                        } else {
                                            questData.reward[j] = old[j - 1];
                                        }
                                        
                                    }
                                }
                            }
                            
                            for (UUID questId : inviteTeam.getQuestData().keySet()) {
                                QuestData joinData = team.getQuestData().get(questId);
                                QuestData questData = inviteTeam.getQuestData().get(questId);
                                if (questData != null && Quest.getQuest(questId) != null)
                                    Quest.getQuest(questId).mergeProgress(player.getUUID(), questData, joinData);
                            }
                            
                            for (Reputation reputation : ReputationManager.getInstance().getReputations().values()) {
                                if (reputation != null) {
                                    int joinValue = team.getReputation(reputation);
                                    int teamValue = inviteTeam.getReputation(reputation);
                                    int targetValue;
                                    if (Math.abs(joinValue) > Math.abs(teamValue)) {
                                        targetValue = joinValue;
                                    } else {
                                        targetValue = teamValue;
                                    }
                                    inviteTeam.setReputation(reputation, targetValue);
                                }
                            }
                            
                            inviteTeam.refreshData();
                            Team.declineAll(player.getUUID());
                            TeamLiteStat.refreshTeam(inviteTeam);
                            NetworkManager.sendToPlayer(TeamUpdateType.JOIN_TEAM.build(inviteTeam, player.getUUID()), entry.getPlayerMP());
                            break;
                        }
                    }
                }
            }
        }
    },
    DECLINE {
        @Override
        public void process(Team team, Player player, String data) {
            if (team.isSingle()) {
                UUID declineId = UUID.fromString(data);
                TeamManager manager = TeamManager.getInstance();
                Team inviteTeam = manager.getByTeamId(declineId);
                if (inviteTeam != null) {
                    inviteTeam.getPlayers().remove(new PlayerEntry(player.getUUID(), false, false));
                    inviteTeam.refreshTeamData(TeamUpdateSize.ONLY_OWNER);
                    team.refreshTeamData(TeamUpdateSize.ONLY_MEMBERS);
                }
            }
        }
    },
    KICK {
        @Override
        public void process(Team team, Player player, String toRemovePlayerUuid) {
            Player playerToRemove = HardcoreQuestingCore.getServer().getPlayerList().getPlayer(UUID.fromString(toRemovePlayerUuid));
            if (!team.isSingle() && team.isOwner(player) && playerToRemove != null) {
                PlayerEntry entryToRemove = team.getEntry(playerToRemove.getUUID());
                if (!entryToRemove.isOwner()) {
                    if (entryToRemove.isInTeam()) {
                        team.removePlayer(playerToRemove);
                        team.refreshTeamData(TeamUpdateSize.ALL);
                        TeamLiteStat.refreshTeam(team);
                    } else {
                        team.getPlayers().remove(entryToRemove);
                        team.refreshTeamData(TeamUpdateSize.ONLY_OWNER);
                    }
                    
                    QuestingDataManager.getInstance().getQuestingData(playerToRemove).getTeam().refreshTeamData(TeamUpdateSize.ONLY_MEMBERS);
                }
            }
        }
    },
    LEAVE {
        @Override
        public void process(Team team, Player player, String data) {
            if (!team.isSingle() && !team.isOwner(player)) {
                team.removePlayer(player);
                team.refreshTeamData(TeamUpdateSize.ALL);
                getTeam(player).refreshTeamData(TeamUpdateSize.ONLY_MEMBERS);
                TeamLiteStat.refreshTeam(team);
            }
        }
    },
    DISBAND {
        @Override
        public void process(Team team, Player player, String data) {
            if (!team.isSingle() && team.isOwner(player)) {
                team.deleteTeam();
                TeamLiteStat.refreshTeam(team);
            }
        }
    },
    NEXT_LIFE_SETTING {
        @Override
        public void process(Team team, Player player, String data) {
            if (!team.isSingle() && team.isOwner(player)) {
                team.setLifeSetting(LifeSetting.values()[(team.getLifeSetting().ordinal() + 1) % LifeSetting.values().length]);
                team.refreshTeamData(TeamUpdateSize.ALL);
            }
        }
    },
    NEXT_REWARD_SETTING {
        @Override
        public void process(Team team, Player player, String data) {
            if (!team.isSingle() && team.isOwner(player)) {
                team.setRewardSetting(RewardSetting.values()[(team.getRewardSetting().ordinal() + 1) % RewardSetting.values().length]);
                if (team.getRewardSetting() == RewardSetting.ALL)
                    team.setRewardSetting(RewardSetting.getDefault());
                team.refreshTeamData(TeamUpdateSize.ALL);
            }
        }
    };
    
    private static Team getTeam(Player player) {
        return QuestingDataManager.getInstance().getQuestingData(player).getTeam();
    }
    
    public void process(Player player, String data) {
        process(getTeam(player), player, data);
    }
    
    public abstract void process(Team team, Player player, String data);
}
