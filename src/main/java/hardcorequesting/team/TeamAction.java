package hardcorequesting.team;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.network.NetworkManager;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestData;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.reputation.Reputation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public enum TeamAction {
    CREATE {
        @Override
        public void process(Team team, PlayerEntity player, String teamName) {
            if (team.isSingle()) {
                if (teamName.length() == 0) {
                    return;
                }
                
                for (Team t : QuestingData.getTeams()) {
                    if (t.getName().equals(teamName)) {
                        TeamError.USED_NAME.sendToClient(player);
                        return;
                    }
                }
                
                QuestingData.addTeam(team);
                team.setName(teamName);
                team.refreshTeamData(TeamUpdateSize.ONLY_MEMBERS);
                
                Team.declineAll(player.getUuid());
                TeamStats.refreshTeam(team);
                NetworkManager.sendToAllPlayers(TeamUpdateType.CREATE_TEAM.build(team));
                if (player instanceof ServerPlayerEntity) {
                    NetworkManager.sendToPlayer(TeamUpdateType.JOIN_TEAM.build(team, player.getUuid().toString()), (ServerPlayerEntity) player);
                }
            }
        }
    },
    INVITE {
        @Override
        public void process(Team team, PlayerEntity player, String playerName) {
            PlayerEntity invitee = HardcoreQuesting.getServer().getPlayerManager().getPlayer(playerName);
            if (!team.isSingle() && team.isOwner(player) && invitee != null) {
                PlayerEntry entry = new PlayerEntry(invitee.getUuid(), false, false);
                
                if (!QuestingData.hasData(entry.getUUID())) {
                    TeamError.INVALID_PLAYER.sendToClient(player);
                    return;
                }
                
                if (!QuestingData.getQuestingData(entry.getUUID()).getTeam().isSingle()) {
                    TeamError.IN_PARTY.sendToClient(player);
                    return;
                }
                
                if (!team.getPlayers().contains(entry)) {
                    team.getPlayers().add(entry);
                    team.refreshTeamData(TeamUpdateSize.ONLY_MEMBERS);
                    QuestingData.getQuestingData(entry.getUUID()).getTeam().refreshTeamData(TeamUpdateSize.ONLY_MEMBERS);
                    QuestingData.getQuestingData(entry.getUUID()).getTeam().getInvites().add(team);
                    NetworkManager.sendToPlayer(TeamUpdateType.INVITE.build(team), entry.getPlayerMP());
                }
            }
        }
    },
    ACCEPT {
        @Override
        public void process(Team team, PlayerEntity player, String data) {
            if (team.isSingle()) {
                int acceptId = Integer.parseInt(data);
                if (acceptId >= 0 && acceptId < QuestingData.getTeams().size()) {
                    Team inviteTeam = QuestingData.getTeams().get(acceptId);
                    int id = 0;
                    for (PlayerEntry entry : inviteTeam.getPlayers()) {
                        if (entry.isInTeam()) {
                            id++;
                        } else if (entry.getUUID().equals(player.getUuid())) {
                            entry.setBookOpen(true);
                            entry.setInTeam(true);
                            QuestingData.getQuestingData(entry.getUUID()).setTeam(inviteTeam);
                            team.setId(inviteTeam.getId());
                            
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
                                    Quest.getQuest(questId).mergeProgress(player.getUuid(), questData, joinData);
                            }
                            
                            for (Reputation reputation : Reputation.getReputations().values()) {
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
                            inviteTeam.refreshTeamData(TeamUpdateSize.ALL);
                            Team.declineAll(player.getUuid());
                            TeamStats.refreshTeam(inviteTeam);
                            NetworkManager.sendToPlayer(TeamUpdateType.JOIN_TEAM.build(inviteTeam, entry.getUUID()), entry.getPlayerMP());
                            break;
                        }
                    }
                }
            }
        }
    },
    DECLINE {
        @Override
        public void process(Team team, PlayerEntity player, String data) {
            if (team.isSingle()) {
                int declineId = Integer.parseInt(data);
                if (declineId >= 0 && declineId < QuestingData.getTeams().size()) {
                    Team inviteTeam = QuestingData.getTeams().get(declineId);
                    inviteTeam.getPlayers().remove(new PlayerEntry(player.getUuid(), false, false));
                    inviteTeam.refreshTeamData(TeamUpdateSize.ONLY_OWNER);
                    team.refreshTeamData(TeamUpdateSize.ONLY_MEMBERS);
                }
            }
        }
    },
    KICK {
        @Override
        public void process(Team team, PlayerEntity player, String toRemovePlayerUuid) {
            PlayerEntity playerToRemove = HardcoreQuesting.getServer().getPlayerManager().getPlayer(UUID.fromString(toRemovePlayerUuid));
            if (!team.isSingle() && team.isOwner(player) && playerToRemove != null) {
                PlayerEntry entryToRemove = team.getEntry(playerToRemove.getUuid());
                if (!entryToRemove.isOwner()) {
                    if (entryToRemove.isInTeam()) {
                        team.removePlayer(playerToRemove);
                        team.refreshTeamData(TeamUpdateSize.ALL);
                        TeamStats.refreshTeam(team);
                    } else {
                        team.getPlayers().remove(entryToRemove);
                        team.refreshTeamData(TeamUpdateSize.ONLY_OWNER);
                    }
                    
                    QuestingData.getQuestingData(playerToRemove).getTeam().refreshTeamData(TeamUpdateSize.ONLY_MEMBERS);
                }
            }
        }
    },
    LEAVE {
        @Override
        public void process(Team team, PlayerEntity player, String data) {
            if (!team.isSingle() && !team.isOwner(player)) {
                team.removePlayer(player);
                team.refreshTeamData(TeamUpdateSize.ALL);
                getTeam(player).refreshTeamData(TeamUpdateSize.ONLY_MEMBERS);
                TeamStats.refreshTeam(team);
            }
        }
    },
    DISBAND {
        @Override
        public void process(Team team, PlayerEntity player, String data) {
            if (!team.isSingle() && team.isOwner(player)) {
                team.deleteTeam();
                TeamStats.refreshTeam(team);
            }
        }
    },
    NEXT_LIFE_SETTING {
        @Override
        public void process(Team team, PlayerEntity player, String data) {
            if (!team.isSingle() && team.isOwner(player)) {
                team.setLifeSetting(LifeSetting.values()[(team.getLifeSetting().ordinal() + 1) % LifeSetting.values().length]);
                team.refreshTeamData(TeamUpdateSize.ALL);
            }
        }
    },
    NEXT_REWARD_SETTING {
        @Override
        public void process(Team team, PlayerEntity player, String data) {
            if (!team.isSingle() && team.isOwner(player)) {
                team.setRewardSetting(RewardSetting.values()[(team.getRewardSetting().ordinal() + 1) % RewardSetting.values().length]);
                if (team.getRewardSetting() == RewardSetting.ALL)
                    team.setRewardSetting(RewardSetting.getDefault());
                team.refreshTeamData(TeamUpdateSize.ALL);
            }
        }
    };
    
    private static Team getTeam(PlayerEntity player) {
        return QuestingData.getQuestingData(player).getTeam();
    }
    
    public void process(PlayerEntity player, String data) {
        process(getTeam(player), player, data);
    }
    
    public abstract void process(Team team, PlayerEntity player, String data);
}
