package hardcorequesting.team;

import hardcorequesting.network.NetworkManager;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestData;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.reputation.Reputation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;

public enum TeamAction {
    CREATE {
        @Override
        public void process(Team team, EntityPlayer player, String teamName) {
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

                Team.declineAll(QuestingData.getUserUUID(player));
                TeamStats.refreshTeam(team);
                NetworkManager.sendToAllPlayers(TeamUpdateType.CREATE_TEAM.build(team));
                if (player instanceof EntityPlayerMP) {
                    NetworkManager.sendToPlayer(TeamUpdateType.JOIN_TEAM.build(team, player.getUniqueID().toString()), (EntityPlayerMP) player);
                }
            }
        }
    },
    INVITE {
        @Override
        public void process(Team team, EntityPlayer player, String playerName) {
            EntityPlayer invitee = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(playerName);
            if (!team.isSingle() && team.isOwner(player) && invitee != null) {
                PlayerEntry entry = new PlayerEntry(invitee.getUniqueID().toString(), false, false);

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
                }
            }
        }
    },
    ACCEPT {
        @Override
        public void process(Team team, EntityPlayer player, String data) {
            if (team.isSingle()) {
                int acceptId = Integer.parseInt(data);
                if (acceptId >= 0 && acceptId < QuestingData.getTeams().size()) {
                    Team inviteTeam = QuestingData.getTeams().get(acceptId);
                    int id = 0;
                    for (PlayerEntry entry : inviteTeam.getPlayers()) {
                        if (entry.isInTeam()) {
                            id++;
                        } else if (entry.getUUID().equals(QuestingData.getUserUUID(player))) {
                            entry.setBookOpen(team.getPlayers().get(0).isBookOpen());
                            entry.setInTeam(true);
                            QuestingData.getQuestingData(entry.getUUID()).setTeam(inviteTeam);
                            team.setId(inviteTeam.getId());

                            for (String quest : inviteTeam.getQuestData().keySet()) {
                                QuestData joinData = team.getQuestData().get(quest);
                                QuestData questData = inviteTeam.getQuestData().get(quest);
                                if (questData != null) {
                                    boolean old[] = questData.reward;
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

                            for (String quest : inviteTeam.getQuestData().keySet()) {
                                QuestData joinData = team.getQuestData().get(quest);
                                QuestData questData = inviteTeam.getQuestData().get(quest);
                                if (questData != null && Quest.getQuest(quest) != null)
                                    Quest.getQuest(quest).mergeProgress(QuestingData.getUserUUID(player), questData, joinData);
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
                                    team.setReputation(reputation, targetValue);
                                }
                            }

                            inviteTeam.refreshData();
                            inviteTeam.refreshTeamData(TeamUpdateSize.ALL);
                            Team.declineAll(QuestingData.getUserUUID(player));
                            TeamStats.refreshTeam(team);
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
        public void process(Team team, EntityPlayer player, String data) {
            if (team.isSingle()) {
                int declineId = Integer.parseInt(data);
                if (declineId >= 0 && declineId < QuestingData.getTeams().size()) {
                    Team inviteTeam = QuestingData.getTeams().get(declineId);
                    inviteTeam.getPlayers().remove(new PlayerEntry(QuestingData.getUserUUID(player), false, false));
                    inviteTeam.refreshTeamData(TeamUpdateSize.ONLY_OWNER);
                    team.refreshTeamData(TeamUpdateSize.ONLY_MEMBERS);
                }
            }
        }
    },
    KICK {
        @Override
        public void process(Team team, EntityPlayer player, String playerNameToRemove) {
            EntityPlayer playerToRemove = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(playerNameToRemove);
            if (!team.isSingle() && team.isOwner(player) && playerToRemove != null) {
                PlayerEntry entryToRemove = team.getEntry(playerToRemove.getUniqueID().toString());
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
        public void process(Team team, EntityPlayer player, String data) {
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
        public void process(Team team, EntityPlayer player, String data) {
            if (!team.isSingle() && team.isOwner(player)) {
                team.deleteTeam();
                TeamStats.refreshTeam(team);
            }
        }
    },
    NEXT_LIFE_SETTING {
        @Override
        public void process(Team team, EntityPlayer player, String data) {
            if (!team.isSingle() && team.isOwner(player)) {
                team.setLifeSetting(LifeSetting.values()[(team.getLifeSetting().ordinal() + 1) % LifeSetting.values().length]);
                team.refreshTeamData(TeamUpdateSize.ALL);
            }
        }
    },
    NEXT_REWARD_SETTING {
        @Override
        public void process(Team team, EntityPlayer player, String data) {
            if (!team.isSingle() && team.isOwner(player)) {
                team.setRewardSetting(RewardSetting.values()[(team.getRewardSetting().ordinal() + 1) % RewardSetting.values().length]);
                if (team.getRewardSetting() == RewardSetting.ALL)
                    team.setRewardSetting(RewardSetting.getDefault());
                team.refreshTeamData(TeamUpdateSize.ALL);
            }
        }
    };

    private static Team getTeam(EntityPlayer player) {
        return QuestingData.getQuestingData(player).getTeam();
    }

    public void process(EntityPlayer player, String data) {
        process(getTeam(player), player, data);
    }

    public abstract void process(Team team, EntityPlayer player, String data);
}
