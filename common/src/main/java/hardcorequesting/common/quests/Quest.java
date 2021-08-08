package hardcorequesting.common.quests;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.ClientChange;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.*;
import hardcorequesting.common.client.interfaces.edit.*;
import hardcorequesting.common.client.sounds.SoundHandler;
import hardcorequesting.common.client.sounds.Sounds;
import hardcorequesting.common.config.HQMConfig;
import hardcorequesting.common.event.EventTrigger;
import hardcorequesting.common.network.GeneralUsage;
import hardcorequesting.common.network.IMessage;
import hardcorequesting.common.network.NetworkManager;
import hardcorequesting.common.network.message.QuestDataUpdateMessage;
import hardcorequesting.common.quests.data.QuestDataTask;
import hardcorequesting.common.quests.reward.CommandRewardList;
import hardcorequesting.common.quests.reward.ItemStackRewardList;
import hardcorequesting.common.quests.reward.ReputationReward;
import hardcorequesting.common.quests.task.*;
import hardcorequesting.common.team.PlayerEntry;
import hardcorequesting.common.team.RewardSetting;
import hardcorequesting.common.team.Team;
import hardcorequesting.common.team.TeamManager;
import hardcorequesting.common.util.HQMUtil;
import hardcorequesting.common.util.OPBookHelper;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.awt.*;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class Quest {
    
    private static final int VISIBLE_DESCRIPTION_LINES = 7;
    private static final int VISIBLE_TASKS = 3;
    //region pixelinfo
    private static final int START_X = 20;
    private static final int TEXT_HEIGHT = 9;
    private static final int TASK_LABEL_START_Y = 100;
    private static final int TASK_MARGIN = 2;
    private static final int TITLE_START_Y = 15;
    private static final int DESCRIPTION_START_Y = 30;
    private static final int TASK_DESCRIPTION_X = 180;
    private static final int TASK_DESCRIPTION_Y = 20;
    private static final int REWARD_STR_Y = 140;
    private static final int REWARD_Y = 150;
    private static final int REWARD_Y_OFFSET = 40;
    private static final int REWARD_OFFSET = 20;
    private static final int ITEM_SIZE = 18;
    private static final int REPUTATION_X = 142;
    private static final int REPUTATION_Y = 133;
    private static final int REPUTATION_Y_LOWER = 150;
    private static final int REPUTATION_SIZE = 16;
    private static final int REPUTATION_SRC_X = 30;
    private static final int REPUTATION_SRC_Y = 82;
    private static final int MAX_REWARD_SLOTS = 7;
    private static final int MAX_SELECT_REWARD_SLOTS = 4;
    @Deprecated
    public static boolean isEditing = false;
    public static UUID speciallySelectedQuestId = null;
    public static QuestTicker clientTicker;
    public static QuestTicker serverTicker;
    private final List<LargeButton> buttons = new ArrayList<>();
    private final ScrollBar descriptionScroll;
    private final ScrollBar taskDescriptionScroll;
    private final ScrollBar taskScroll;
    private final List<ScrollBar> scrollBars = new ArrayList<>();
    public int nextTaskId;
    private UUID questId;
    private String name;
    private String description;
    private List<UUID> requirement;
    private List<UUID> reversedRequirement;
    private List<UUID> optionLinks;
    private List<UUID> reversedOptionLinks;
    private List<QuestTask> tasks;
    private List<FormattedText> cachedDescription;
    private List<ReputationReward> reputationRewards;
    private QuestTask selectedTask;
    private ItemStackRewardList rewards;
    private ItemStackRewardList rewardChoices;
    private CommandRewardList commandRewardList;
    private RepeatInfo repeatInfo = new RepeatInfo(RepeatType.NONE, 0, 0);
    private TriggerType triggerType = TriggerType.NONE;
    private int triggerTasks = 1;
    private int parentRequirementCount = -1;
    private int x;
    private int y;
    private boolean isBig;
    private ItemStack iconStack = ItemStack.EMPTY;
    private QuestSet set;
    private int selectedReward = -1;
    private int lastClicked;
    private ParentEvaluator enabledParentEvaluator = new ParentEvaluator() {
        @Override
        protected boolean isValid(UUID playerId, Quest parent, Map<Quest, Boolean> isVisibleCache, Map<Quest, Boolean> isLinkFreeCache) {
            return parent.isCompleted(playerId);
        }
    };
    private ParentEvaluator linkParentEvaluator = new ParentEvaluator() {
        @Override
        protected boolean isValid(UUID playerId, Quest parent, Map<Quest, Boolean> isVisibleCache, Map<Quest, Boolean> isLinkFreeCache) {
            return parent.isLinkFree(playerId, isLinkFreeCache);
        }
    };
    private ParentEvaluator visibleParentEvaluator = new ParentEvaluator() {
        @Override
        protected boolean isValid(UUID playerId, Quest parent, Map<Quest, Boolean> isVisibleCache, Map<Quest, Boolean> isLinkFreeCache) {
            return parent.isVisible(playerId, isVisibleCache, isLinkFreeCache) || parent.isCompleted(playerId);
        }
    };
    
    {
        buttons.add(new LargeButton("hqm.quest.claim", 100, 190) {
            @Override
            public boolean isEnabled(GuiBase gui, Player player) {
                return canPlayerClaimReward(player);
            }
            
            @Override
            public boolean isVisible(GuiBase gui, Player player) {
                return hasReward(player);
            }
            
            @Override
            public void onClick(GuiBase gui, Player player) {
                NetworkManager.sendToServer(ClientChange.CLAIM_QUEST.build(new Tuple<>(getQuestId(), rewardChoices.isEmpty() ? -1 : selectedReward)));
            }
        });
        
        buttons.add(new LargeButton("hqm.quest.manualSubmit", 185, 200) {
            @Override
            public boolean isEnabled(GuiBase gui, Player player) {
                return selectedTask.allowManual();
            }
            
            @Override
            public boolean isVisible(GuiBase gui, Player player) {
                return selectedTask != null && selectedTask.allowManual() && !selectedTask.isCompleted(player);
            }
            
            @Override
            public void onClick(GuiBase gui, Player player) {
                NetworkManager.sendToServer(ClientChange.UPDATE_TASK.build(selectedTask));
            }
        });
        
        buttons.add(new LargeButton("hqm.quest.manualDetect", 185, 200) {
            @Override
            public boolean isEnabled(GuiBase gui, Player player) {
                return selectedTask.allowDetect();
            }
            
            @Override
            public boolean isVisible(GuiBase gui, Player player) {
                return selectedTask != null && selectedTask.allowDetect() && !selectedTask.isCompleted(player);
            }
            
            @Override
            public void onClick(GuiBase gui, Player player) {
                NetworkManager.sendToServer(ClientChange.UPDATE_TASK.build(selectedTask));
            }
        });
        
        buttons.add(new LargeButton("hqm.quest.requirement", 185, 200) {
            @Override
            @Environment(EnvType.CLIENT)
            public boolean isEnabled(GuiBase gui, Player player) {
                return true;
            }
            
            @Override
            @Environment(EnvType.CLIENT)
            public boolean isVisible(GuiBase gui, Player player) {
                return selectedTask != null && selectedTask instanceof QuestTaskDeath && Quest.canQuestsBeEdited();
            }
            
            @Override
            @Environment(EnvType.CLIENT)
            public void onClick(GuiBase gui, Player player) {
                gui.setEditMenu(new GuiEditMenuDeathTask(gui, player, (QuestTaskDeath) selectedTask));
            }
        });
        
        buttons.add(new LargeButton("hqm.quest.requirement", 250, 95) {
            @Override
            @Environment(EnvType.CLIENT)
            public boolean isEnabled(GuiBase gui, Player player) {
                return true;
            }
            
            @Override
            @Environment(EnvType.CLIENT)
            public boolean isVisible(GuiBase gui, Player player) {
                return selectedTask != null && selectedTask instanceof QuestTaskReputationKill && Quest.canQuestsBeEdited();
            }
            
            @Override
            @Environment(EnvType.CLIENT)
            public void onClick(GuiBase gui, Player player) {
                gui.setEditMenu(new GuiEditMenuReputationKillTask(gui, player, (QuestTaskReputationKill) selectedTask));
            }
        });
        
        
        buttons.add(new LargeButton("hqm.quest.selectTask", 250, 200) {
            @Override
            public boolean isEnabled(GuiBase gui, Player player) {
                QuestingData data = QuestingDataManager.getInstance().getQuestingData(player);
                if (data != null && data.selectedQuestId != null && data.selectedQuestId.equals(getQuestId())) {
                    return data.selectedTask != selectedTask.getId();
                }
                return false;
            }
            
            @Override
            public boolean isVisible(GuiBase gui, Player player) {
                return selectedTask instanceof QuestTaskItemsConsume && !selectedTask.isCompleted(player);
            }
            
            @Environment(EnvType.CLIENT)
            @Override
            public void onClick(GuiBase gui, Player player) {
                //update locally too, then we don't have to refresh all the data(i.e. the server won't notify us about the change we already know about)
                QuestingDataManager.getInstance().getQuestingData(player).selectedQuestId = getQuestId();
                QuestingDataManager.getInstance().getQuestingData(player).selectedTask = selectedTask.getId();
                
                player.displayClientMessage(new TranslatableComponent("tile.hqm:item_barrel.selectedTask", selectedTask.getDescription()).withStyle(ChatFormatting.GREEN), false);
                
                //NetworkManager.sendToServer(ClientChange.SELECT_QUEST.build(selectedTask));
                GeneralUsage.sendBookSelectTaskUpdate(Quest.this.selectedTask);
            }
        });
        
        int itemIds = 0;
        for (final TaskType taskType : TaskType.values()) {
            buttons.add(new LargeButton(taskType.getLangKeyName(), taskType.getLangKeyDescription(), 185 + (taskType.ordinal() % 2) * 65, 50 + (taskType.ordinal() / 2) * 20) {
                @Override
                public boolean isEnabled(GuiBase gui, Player player) {
                    return true;
                }
                
                @Environment(EnvType.CLIENT)
                @Override
                public boolean isVisible(GuiBase gui, Player player) {
                    return canQuestsBeEdited() && selectedTask == null && ((GuiQuestBook) gui).getCurrentMode() == EditMode.TASK;
                }
                
                @Override
                public void onClick(GuiBase gui, Player player) {
                    taskType.addTask(Quest.this);
                }
            });
            
            if (QuestTaskItems.class.isAssignableFrom(taskType.clazz)) {
                buttons.add(new LargeButton(taskType.getLangKeyName(), taskType.getLangKeyDescription(), 185 + (itemIds % 2) * 65, 50 + (itemIds / 2) * 35) {
                    @Override
                    public boolean isEnabled(GuiBase gui, Player player) {
                        return selectedTask instanceof QuestTaskItems;
                    }
                    
                    @Override
                    public boolean isVisible(GuiBase gui, Player player) {
                        return false; // canQuestsBeEdited() && selectedTask != null && ((GuiQuestBook) gui).getCurrentMode() == EditMode.CHANGE_TASK;
                    }
                    
                    @Override
                    public void onClick(GuiBase gui, Player player) {
                        TaskType oldTaskType = TaskType.getType(selectedTask.getClass());
                        if (oldTaskType == null) return;
                        
                        nextTaskId--;
                        Class<? extends QuestTask> clazz = taskType.clazz;
                        try {
                            Constructor<? extends QuestTask> constructor = clazz.getConstructor(Quest.class, String.class, String.class);
                            QuestTask task = constructor.newInstance(Quest.this, taskType.getLangKeyName(), taskType.getLangKeyDescription());
                            
                            selectedTask.getRequirements().forEach(task::addRequirement);
                            for (QuestTask questTask : tasks) {
                                List<QuestTask> requirements = questTask.getRequirements();
                                for (int j = 0; j < requirements.size(); j++) {
                                    if (requirements.get(j).equals(selectedTask)) {
                                        requirements.set(j, task);
                                    }
                                }
                            }
                            for (int j = 0; j < tasks.size(); j++) {
                                if (tasks.get(j).equals(selectedTask)) {
                                    tasks.set(j, task);
                                    break;
                                }
                            }
                            
                            if (!selectedTask.getLangKeyDescription().equals(oldTaskType.getLangKeyName())) {
                                task.setDescription(selectedTask.getLangKeyDescription());
                            }
                            if (!selectedTask.getLangKeyLongDescription().equals(oldTaskType.getLangKeyDescription())) {
                                task.setLongDescription(selectedTask.getLangKeyLongDescription());
                            }
                            ((QuestTaskItems) task).setItems(((QuestTaskItems) selectedTask).getItems());
                            task.setId(selectedTask.getId());
                            selectedTask = task;
                            SaveHelper.add(SaveHelper.EditType.TASK_CHANGE_TYPE);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                itemIds++;
            }
        }
    }
    
    {
        scrollBars.add(descriptionScroll = new ScrollBar(155, 28, 64, 249, 102, START_X) {
            @Environment(EnvType.CLIENT)
            @Override
            public boolean isVisible(GuiBase gui) {
                return getCachedDescription(gui).size() > VISIBLE_DESCRIPTION_LINES;
            }
        });
        scrollBars.add(taskDescriptionScroll = new ScrollBar(312, 18, 64, 249, 102, TASK_DESCRIPTION_X) {
            @Environment(EnvType.CLIENT)
            @Override
            public boolean isVisible(GuiBase gui) {
                return selectedTask != null && selectedTask.getCachedLongDescription(gui).size() > VISIBLE_DESCRIPTION_LINES;
            }
        });
        
        scrollBars.add(taskScroll = new ScrollBar(155, 100, 29, 242, 102, START_X) {
            @Environment(EnvType.CLIENT)
            @Override
            public boolean isVisible(GuiBase gui) {
                return tasks.size() > VISIBLE_TASKS && getVisibleTasks(gui) > VISIBLE_TASKS;
            }
        });
    }
    
    public Quest(String name, String description, int x, int y, boolean isBig) {
        do {
            this.questId = UUID.randomUUID();
        } while (getQuests().containsKey(this.questId));
        this.name = name;
        this.x = x;
        this.y = y;
        this.isBig = isBig;
        this.description = description;
        
        requirement = new ArrayList<>();
        reversedRequirement = new ArrayList<>();
        optionLinks = new ArrayList<>();
        reversedOptionLinks = new ArrayList<>();
        tasks = new ArrayList<>();
        rewards = new ItemStackRewardList();
        rewardChoices = new ItemStackRewardList();
        commandRewardList = new CommandRewardList();
        
        QuestSetsManager.getInstance().quests.put(getQuestId(), this);
    }
    
    public static Map<UUID, Quest> getQuests() {
        return QuestSetsManager.getInstance().quests;
    }
    
    public static List<QuestSet> getQuestSets() {
        return QuestLine.getActiveQuestLine().questSetsManager.questSets;
    }
    
    @Environment(EnvType.CLIENT)
    public static List<FormattedText> getMainDescription(GuiBase gui) {
        return QuestLine.getActiveQuestLine().getMainDescription(gui);
    }
    
    public static String getRawMainDescription() {
        return QuestLine.getActiveQuestLine().mainDescription;
    }
    
    public static Quest getQuest(UUID questId) {
        if (questId == null) return null;
        
        return QuestSetsManager.getInstance().quests.get(questId);
    }
    
    public static void addItems(Player player, List<ItemStack> itemsToAdd) {
        for (int i = 0; i < player.inventory.items.size(); i++) {
            Iterator<ItemStack> iterator = itemsToAdd.iterator();
            while (iterator.hasNext()) {
                ItemStack nextStack = iterator.next();
                ItemStack stack = player.inventory.items.get(i);
                
                if (stack.isEmpty()) {
                    int amount = Math.min(nextStack.getMaxStackSize(), nextStack.getCount());
                    ItemStack copyStack = nextStack.copy();
                    copyStack.setCount(amount);
                    player.inventory.items.set(i, copyStack);
                    nextStack.shrink(amount);
                    if (nextStack.getCount() <= 0) {
                        iterator.remove();
                    }
                    break;
                } else if (stack.sameItemStackIgnoreDurability(nextStack) && ItemStack.tagMatches(nextStack, stack)) {
                    int amount = Math.min(nextStack.getMaxStackSize() - stack.getCount(), nextStack.getCount());
                    stack.grow(amount);
                    nextStack.shrink(amount);
                    if (nextStack.getCount() <= 0) {
                        iterator.remove();
                    }
                    break;
                }
            }
        }
    }
    
    public static void removeQuest(Quest quest) {
        for (UUID requirementId : quest.requirement) {
            Quest.getQuest(requirementId).reversedRequirement.remove(quest.getQuestId());
        }
        for (UUID optionLinkId : quest.optionLinks) {
            Quest.getQuest(optionLinkId).reversedOptionLinks.remove(quest.getQuestId());
        }
        
        quest.tasks.forEach(QuestTask::onDelete);
        
        quest.setQuestSet(null);
        QuestSetsManager.getInstance().quests.remove(quest.getQuestId());
        
        for (Quest other : QuestSetsManager.getInstance().quests.values()) {
            Iterator<UUID> iterator = other.requirement.iterator();
            while (iterator.hasNext()) {
                UUID element = iterator.next();
                if (element.equals(quest.getQuestId())) {
                    iterator.remove();
                }
            }
            
            iterator = other.optionLinks.iterator();
            while (iterator.hasNext()) {
                UUID element = iterator.next();
                if (element.equals(quest.getQuestId())) {
                    iterator.remove();
                }
            }
        }
    }
    
    public void setX(int x) {
        this.x = x;
    }
    
    public void setY(int y) {
        this.y = y;
    }
    
    public NonNullList<ItemStack> getReward() {
        return rewards.toList();
    }
    
    public void setReward(NonNullList<ItemStack> reward) {
        this.rewards.set(reward);
    }
    
    public NonNullList<ItemStack> getRewardChoice() {
        return rewardChoices.toList();
    }
    
    public void setRewardChoice(NonNullList<ItemStack> rewardChoice) {
        this.rewardChoices.set(rewardChoice);
    }
    
    public String[] getCommandRewardsAsStrings() {
        return this.commandRewardList.asStrings();
    }
    
    public void setCommandRewards(String[] commands) {
        this.commandRewardList.set(commands);
    }
    
    public void addCommand(String command) {
        this.commandRewardList.add(command);
    }
    
    public void editCommand(int id, String command) {
        this.commandRewardList.set(id, command);
    }
    
    public void removeCommand(int id) {
        this.commandRewardList.remove(id);
    }
    
    public RepeatInfo getRepeatInfo() {
        return repeatInfo;
    }
    
    public void setRepeatInfo(RepeatInfo repeatInfo) {
        this.repeatInfo = repeatInfo;
    }
    
    public void addRequirement(UUID requirementQuestId) {
        if (requirementQuestId.equals(this.questId)) return;
        
        if (lookForId(requirementQuestId, false) || lookForId(requirementQuestId, true)) return;
        
        Quest quest = QuestSetsManager.getInstance().quests.get(requirementQuestId);
        if (quest != null) {
            requirement.add(quest.getQuestId());
            quest.reversedRequirement.add(this.getQuestId());
            SaveHelper.add(SaveHelper.EditType.REQUIREMENT_CHANGE);
        }
    }
    
    private boolean lookForId(UUID questId, boolean reversed) {
        List<UUID> currentRequirements = reversed ? reversedRequirement : requirement;
        for (UUID id : currentRequirements)
            if (id.equals(questId) || QuestSetsManager.getInstance().quests.get(id).lookForId(questId, reversed))
                return true;
        return false;
    }
    
    public void clearRequirements() {
        SaveHelper.add(SaveHelper.EditType.REQUIREMENT_REMOVE, requirement.size());
        for (UUID questId : requirement)
            QuestSetsManager.getInstance().quests.get(questId).reversedRequirement.remove(getQuestId());
        requirement.clear();
    }
    
    public void addOptionLink(UUID optionLinkId) {
        if (optionLinkId.equals(this.questId)) return;
        
        for (UUID currentOptionId : optionLinks) {
            if (currentOptionId.equals(optionLinkId)) {
                return;
            }
        }
        for (UUID currentReverseOptionsId : reversedOptionLinks) {
            if (currentReverseOptionsId.equals(optionLinkId)) {
                return;
            }
        }
        
        Quest quest = QuestSetsManager.getInstance().quests.get(optionLinkId);
        if (quest != null) {
            SaveHelper.add(SaveHelper.EditType.OPTION_CHANGE);
            optionLinks.add(quest.getQuestId());
            quest.reversedOptionLinks.add(getQuestId());
        }
    }
    
    public void clearOptionLinks() {
        SaveHelper.add(SaveHelper.EditType.OPTION_REMOVE, optionLinks.size());
        for (UUID questId : reversedOptionLinks) {
            QuestSetsManager.getInstance().quests.get(questId).optionLinks.remove(getQuestId());
        }
        
        for (UUID questId : optionLinks) {
            QuestSetsManager.getInstance().quests.get(questId).reversedOptionLinks.remove(getQuestId());
        }
        reversedRequirement.clear();
        optionLinks.clear();
    }
    
    public QuestData getQuestData(Player player) {
        return QuestingDataManager.getInstance().getQuestingData(player).getQuestData(getQuestId());
    }
    
    public QuestData getQuestData(UUID uuid) {
        return QuestingDataManager.getInstance().getQuestingData(uuid).getQuestData(getQuestId());
    }
    
    public void setQuestData(Player player, QuestData data) {
        QuestingDataManager.getInstance().getQuestingData(player).setQuestData(getQuestId(), data);
    }
    
    public UUID getQuestId() {
        return questId;
    }
    
    public void setId(UUID questId) {
        if (getQuestSet() != null)
            getQuestSet().removeQuest(this);
        getQuests().remove(getQuestId());
        this.questId = questId;
        getQuests().put(getQuestId(), this);
        if (getQuestSet() != null)
            getQuestSet().addQuest(this);
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public boolean isVisible(Player player) {
        return isVisible(player.getUUID());
    }
    
    boolean isVisible(Player player, Map<Quest, Boolean> isVisibleCache, Map<Quest, Boolean> isLinkFreeCache) {
        return isVisible(player.getUUID(), isVisibleCache, isLinkFreeCache);
    }
    
    public boolean isVisible(UUID uuid) {
        return isVisible(uuid, new HashMap<>(), new HashMap<>());
    }
    
    boolean isVisible(UUID playerId, Map<Quest, Boolean> isVisibleCache, Map<Quest, Boolean> isLinkFreeCache) {
        Boolean cachedResult = isVisibleCache.get(this);
        if (cachedResult != null) return cachedResult;
        
        boolean result = this.triggerType.isQuestVisible(this, playerId)
                         && isLinkFree(playerId, isLinkFreeCache)
                         && this.visibleParentEvaluator.isValid(playerId, isVisibleCache, isLinkFreeCache);
        isVisibleCache.put(this, result);
        return result;
    }
    
    public boolean isEnabled(Player player) {
        return isEnabled(player.getUUID());
    }
    
    boolean isEnabled(Player player, Map<Quest, Boolean> isVisibleCache, Map<Quest, Boolean> isLinkFreeCache) {
        return isEnabled(player.getUUID(), true, isVisibleCache, isLinkFreeCache);
    }
    
    public boolean isEnabled(UUID playerId) {
        return isEnabled(playerId, true);
    }
    
    public boolean isEnabled(UUID playerId, boolean requiresVisible) {
        return isEnabled(playerId, requiresVisible, new HashMap<>(), new HashMap<>());
    }
    
    boolean isEnabled(UUID playerId, boolean requiresVisible, Map<Quest, Boolean> isVisibleCache, Map<Quest, Boolean> isLinkFreeCache) {
        return !(set == null || !isLinkFree(playerId, isLinkFreeCache) || (requiresVisible && !triggerType.doesWorkAsInvisible() && !isVisible(playerId, isVisibleCache, isLinkFreeCache))) && enabledParentEvaluator.isValid(playerId, isVisibleCache, isLinkFreeCache);
    }
    
    public boolean isLinkFree(Player player) {
        return isLinkFree(player.getUUID(), new HashMap<>());
    }
    
    boolean isLinkFree(Player player, Map<Quest, Boolean> cache) {
        return isLinkFree(player.getUUID(), cache);
    }
    
    public boolean isLinkFree(UUID uuid) {
        return isLinkFree(uuid, new HashMap<>());
    }
    
    boolean isLinkFree(UUID playerId, Map<Quest, Boolean> cache) {
        Boolean cachedResult = cache.get(this);
        if (cachedResult != null) return cachedResult;
        
        boolean result = true;
        for (UUID optionLinkId : optionLinks) {
            if (QuestSetsManager.getInstance().quests.get(optionLinkId).isCompleted(playerId)) {
                result = false;
                break;
            }
        }
        
        if (result) {
            for (UUID optionLinkId : reversedOptionLinks) {
                if (QuestSetsManager.getInstance().quests.get(optionLinkId).isCompleted(playerId)) {
                    result = false;
                    break;
                }
            }
        }
        
        if (result) {
            result = linkParentEvaluator.isValid(playerId, null, cache);
        }
        
        cache.put(this, result);
        
        return result;
    }
    
    public List<ReputationReward> getReputationRewards() {
        return reputationRewards;
    }
    
    public void setReputationRewards(List<ReputationReward> reputationRewards) {
        this.reputationRewards = reputationRewards;
    }
    
    public boolean isAvailable(Player player) {
        return isAvailable(player.getUUID());
    }
    
    public boolean isCompleted(Player player) {
        return isCompleted(player.getUUID());
    }
    
    public boolean isAvailable(UUID playerId) {
        QuestData data = getQuestData(playerId);
        return data != null && data.available;
    }
    
    public boolean isCompleted(UUID uuid) {
        QuestData data = getQuestData(uuid);
        return data != null && data.completed;
    }
    
    public List<Quest> getRequirements() {
        return this.requirement.stream().map(QuestSetsManager.getInstance().quests::get).collect(Collectors.toList());
    }
    
    //interface stuff
    public int getGuiX() {
        return x;
    }
    
    public int getGuiY() {
        return y;
    }
    
    @Environment(EnvType.CLIENT)
    public int getGuiU() {
        return isBig ? GuiQuestBook.PAGE_WIDTH + 25 : GuiQuestBook.PAGE_WIDTH;
    }
    
    @Environment(EnvType.CLIENT)
    public int getGuiV(Player player, int x, int y) {
        return isEnabled(player) && isMouseInObject(x, y) ? getGuiH() : 0;
    }
    
    public int getGuiW() {
        return isBig ? 31 : 25;
    }
    
    public int getGuiH() {
        return isBig ? 37 : 30;
    }
    
    public int getGuiCenterX() {
        return getGuiX() + getGuiW() / 2;
    }
    
    public void setGuiCenterX(int x) {
        this.x = x - getGuiW() / 2;
    }
    
    public int getGuiCenterY() {
        return getGuiY() + getGuiH() / 2;
    }
    
    public void setGuiCenterY(int y) {
        this.y = y - getGuiH() / 2;
    }
    
    public ItemStack getIconStack() {
        return iconStack;
    }
    
    public void setIconStack(ItemStack iconStack) {
        this.iconStack = iconStack;
        if (iconStack != null) {
            iconStack.setCount(1);
        }
    }
    //endregion
    
    public boolean useBigIcon() {
        return isBig;
    }
    
    public TriggerType getTriggerType() {
        return triggerType;
    }
    
    public void setTriggerType(TriggerType triggerType) {
        this.triggerType = triggerType;
    }
    
    public int getTriggerTasks() {
        return triggerTasks;
    }
    
    public void setTriggerTasks(int triggerTasks) {
        this.triggerTasks = triggerTasks;
    }
    
    @Environment(EnvType.CLIENT)
    public int getColorFilter(Player player, int tick) {
        if (canQuestsBeEdited() && !isVisible(player)) {
            return HQMConfig.QUEST_INVISIBLE;
        } else if (!isEnabled(player)) {
            return HQMConfig.QUEST_DISABLED;
        } else if (!isAvailable(player) && !hasReward(player)) {
            return getRepeatInfo().getType() == RepeatType.NONE ? HQMConfig.QUEST_COMPLETE : HQMConfig.QUEST_COMPLETE_REPEATABLE;
        } else {
            if (HQMConfig.getInstance().Interface.Quests.SINGLE_COLOUR) return HQMConfig.QUEST_AVAILABLE;
            
            int componentBase = 0xBB;
            int componentVariable = (int) (Math.abs(Math.sin(tick / 5F)) * 0x44);
            
            int component = componentBase + componentVariable;
            
            
            int red = hasReward(player) ? componentBase : component;
            int green = hasReward(player) ? component * 3 / 4 : component;
            int blue = component;
            
            return (0xFF << 24) |
                   (red << 16) |
                   (green << 8) |
                   (blue);
            
        }
    }
    
    @Environment(EnvType.CLIENT)
    public boolean isMouseInObject(int x, int y) {
        //quick check
        if (getGuiX() > x || x > getGuiX() + getGuiW() || getGuiY() > y || y > getGuiY() + getGuiH()) return false;
        
        
        //precise check
        Polygon poly = new Polygon();
        
        if (isBig) {
            poly.addPoint(getGuiX() + 1, getGuiY() + 10);
            poly.addPoint(getGuiX() + 15, getGuiY() + 1);
            poly.addPoint(getGuiX() + 30, getGuiY() + 10);
            poly.addPoint(getGuiX() + 30, getGuiY() + 27);
            poly.addPoint(getGuiX() + 15, getGuiY() + 36);
            poly.addPoint(getGuiX() + 1, getGuiY() + 27);
        } else {
            poly.addPoint(getGuiX() + 1, getGuiY() + 8);
            poly.addPoint(getGuiX() + 12, getGuiY() + 2);
            poly.addPoint(getGuiX() + 23, getGuiY() + 8);
            poly.addPoint(getGuiX() + 23, getGuiY() + 8);
            poly.addPoint(getGuiX() + 23, getGuiY() + 21);
            poly.addPoint(getGuiX() + 12, getGuiY() + 27);
            poly.addPoint(getGuiX() + 1, getGuiY() + 21);
        }
        
        return poly.contains(x, y);
    }
    
    @Environment(EnvType.CLIENT)
    private List<FormattedText> getCachedDescription(GuiBase gui) {
        if (cachedDescription == null) {
            cachedDescription = gui.getLinesFromText(Translator.plain(description), 0.7F, 130);
        }
        return cachedDescription;
    }
    
    @Environment(EnvType.CLIENT)
    public void drawMenu(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY) {
        if (!canQuestsBeEdited() && selectedTask != null && !selectedTask.isVisible(player)) {
            if (tasks.size() > 0) {
                selectedTask = tasks.get(0);
            } else {
                selectedTask = null;
            }
        }
        
        gui.drawString(matrices, Translator.plain(name), START_X, TITLE_START_Y, 0x404040);
        
        int startLine = descriptionScroll.isVisible(gui) ? Math.round((getCachedDescription(gui).size() - VISIBLE_DESCRIPTION_LINES) * descriptionScroll.getScroll()) : 0;
        gui.drawString(matrices, getCachedDescription(gui), startLine, VISIBLE_DESCRIPTION_LINES, START_X, DESCRIPTION_START_Y, 0.7F, 0x404040);
        
        int id = 0;
        int start = taskScroll.isVisible(gui) ? Math.round((getVisibleTasks(gui) - VISIBLE_TASKS) * taskScroll.getScroll()) : 0;
        int end = Math.min(start + VISIBLE_TASKS, tasks.size());
        for (int i = start; i < end; i++) {
            QuestTask task = tasks.get(i);
            boolean isVisible = task.isVisible(player);
            if (isVisible || Quest.canQuestsBeEdited()) {
                boolean completed = task.isCompleted(player);
                int yPos = getTaskY(gui, id);
                boolean inBounds = gui.inBounds(START_X, yPos, gui.getStringWidth(task.getDescription()), TEXT_HEIGHT, mX, mY);
                boolean isSelected = task == selectedTask;
                gui.drawString(matrices, Translator.plain(task.getDescription()), START_X, yPos, completed ? isSelected ? inBounds ? 0x40BB40 : 0x40A040 : inBounds ? 0x10A010 : 0x107010 : isSelected ? inBounds ? 0xAAAAAA : 0x888888 : inBounds ? 0x666666 : isVisible ? 0x404040 : 0xDDDDDD);
                
                id++;
            }
        }
        
        if (selectedReward != -1 && !hasReward(player)) {
            selectedReward = -1;
        }
        if (!rewards.isEmpty() || canQuestsBeEdited()) {
            gui.drawString(matrices, Translator.translatable("hqm.quest.rewards"), START_X, REWARD_STR_Y, 0x404040);
            drawRewards(gui, rewards.toList(), REWARD_Y, -1, mX, mY, MAX_SELECT_REWARD_SLOTS);
            if (!rewardChoices.isEmpty() || canQuestsBeEdited()) {
                gui.drawString(matrices, Translator.translatable("hqm.quest.pickOne"), START_X, REWARD_STR_Y + REWARD_Y_OFFSET, 0x404040);
                drawRewards(gui, rewardChoices.toList(), REWARD_Y + REWARD_Y_OFFSET, selectedReward, mX, mY, MAX_REWARD_SLOTS);
            }
        } else if (!rewardChoices.isEmpty()) {
            gui.drawString(matrices, Translator.translatable("hqm.quest.pickOneReward"), START_X, REWARD_STR_Y, 0x404040);
            drawRewards(gui, rewardChoices.toList(), REWARD_Y, selectedReward, mX, mY, MAX_REWARD_SLOTS);
        }
        
        for (LargeButton button : buttons) {
            button.draw(matrices, gui, player, mX, mY);
        }
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
        for (ScrollBar scrollBar : scrollBars) {
            scrollBar.draw(gui);
        }
        
        
        boolean claimed = getQuestData(player).claimed;
        int y = rewards == null || rewards.size() <= MAX_REWARD_SLOTS - (canQuestsBeEdited() ? 2 : 1) ? REPUTATION_Y_LOWER : REPUTATION_Y;
        boolean hover = gui.inBounds(REPUTATION_X, y, REPUTATION_SIZE, REPUTATION_SIZE, mX, mY);
        
        
        if (reputationRewards != null || canQuestsBeEdited()) {
            if (reputationRewards == null) {
                claimed = true;
            }
            
            int backgroundIndex = claimed ? 2 : hover ? 1 : 0;
            int foregroundIndex;
            if (claimed) {
                foregroundIndex = 3;
            } else {
                boolean positive = false;
                boolean negative = false;
                for (ReputationReward reputationReward : reputationRewards) {
                    if (reputationReward.getValue() < 0) {
                        negative = true;
                    } else if (reputationReward.getValue() > 0) {
                        positive = true;
                    }
                }
                
                if (negative == positive) {
                    foregroundIndex = 2;
                } else {
                    foregroundIndex = positive ? 0 : 1;
                }
            }
            
            
            foregroundIndex += 3;
            gui.drawRect(REPUTATION_X, y, REPUTATION_SRC_X + backgroundIndex * REPUTATION_SIZE, REPUTATION_SRC_Y, REPUTATION_SIZE, REPUTATION_SIZE);
            gui.drawRect(REPUTATION_X, y, REPUTATION_SRC_X + foregroundIndex * REPUTATION_SIZE, REPUTATION_SRC_Y, REPUTATION_SIZE, REPUTATION_SIZE);
        }
        
        if (selectedTask != null) {
            /*if (canQuestsBeEdited() && gui.getCurrentMode() == EditMode.CHANGE_TASK) {
                if (selectedTask instanceof QuestTaskItems) {
                    gui.drawString(gui.getLinesFromText(Translator.translate("hqm.quest.itemTaskChangeTo"), 0.7F, 130), 180, 20, 0.7F, 0x404040);
                } else {
                    gui.drawString(gui.getLinesFromText(Translator.translate("hqm.quest.itemTaskTypeOnly"), 0.7F, 130), 180, 20, 0.7F, 0x404040);
                }
            } else {*/
            List<FormattedText> description = selectedTask.getCachedLongDescription(gui);
            int taskStartLine = taskDescriptionScroll.isVisible(gui) ? Math.round((description.size() - VISIBLE_DESCRIPTION_LINES) * taskDescriptionScroll.getScroll()) : 0;
            gui.drawString(matrices, description, taskStartLine, VISIBLE_DESCRIPTION_LINES, TASK_DESCRIPTION_X, TASK_DESCRIPTION_Y, 0.7F, 0x404040);
            
            selectedTask.draw(matrices, gui, player, mX, mY);
            //}
        } else if (canQuestsBeEdited() && gui.getCurrentMode() == EditMode.TASK) {
            gui.drawString(matrices, gui.getLinesFromText(Translator.translatable("hqm.quest.createTasks"), 0.7F, 130), 180, 20, 0.7F, 0x404040);
        /*} else if (canQuestsBeEdited() && gui.getCurrentMode() == EditMode.CHANGE_TASK) {
            gui.drawString(gui.getLinesFromText(Translator.translate("hqm.quest.itemTaskTypeChange"), 0.7F, 130), 180, 20, 0.7F, 0x404040);*/
        }
        
        if (!rewards.isEmpty() || canQuestsBeEdited()) {
            drawRewardMouseOver(matrices, gui, rewards.toList(), REWARD_Y, -1, mX, mY);
            if (!rewardChoices.isEmpty() || canQuestsBeEdited()) {
                drawRewardMouseOver(matrices, gui, rewardChoices.toList(), REWARD_Y + REWARD_Y_OFFSET, selectedReward, mX, mY);
            }
        } else if (!rewardChoices.isEmpty()) {
            drawRewardMouseOver(matrices, gui, rewardChoices.toList(), REWARD_Y, selectedReward, mX, mY);
        }
        for (LargeButton button : buttons) {
            button.renderTooltip(matrices, gui, player, mX, mY);
        }
        
        if (reputationRewards != null && hover) {
            List<FormattedText> str = new ArrayList<>();
            for (ReputationReward reputationReward : reputationRewards) {
                if (reputationReward.getValue() != 0 && reputationReward.getReward() != null && reputationReward.getReward().isValid()) {
                    str.add(Translator.plain(reputationReward.getLabel()));
                }
                
            }
            
            List<FormattedText> commentLines = gui.getLinesFromText(Translator.translatable("hqm.quest.partyRepReward" + (claimed ? "Claimed" : "")), 1, 200);
            if (commentLines != null) {
                str.add(FormattedText.EMPTY);
                for (FormattedText commentLine : commentLines) {
                    str.add(Translator.text(Translator.rawString(commentLine), GuiColor.GRAY));
                }
            }
            gui.renderTooltipL(matrices, str, mX + gui.getLeft(), mY + gui.getTop());
        }
    }
    
    @Environment(EnvType.CLIENT)
    private int getVisibleTasks(GuiBase gui) {
        if (canQuestsBeEdited()) {
            return tasks.size();
        }
        
        int count = 0;
        for (QuestTask task : tasks) {
            if (task.isVisible(((GuiQuestBook) gui).getPlayer())) {
                count++;
            }
        }
        return count;
    }
    
    private boolean canPlayerClaimReward(Player player) {
        return hasReward(player) && (rewardChoices.isEmpty() || selectedReward != -1) && isEnabled(player);
    }
    
    public boolean hasReward(Player player) {
        return (getQuestData(player).getReward(player) && (!rewards.isEmpty() || !rewardChoices.isEmpty())) || (getQuestData(player).canClaim() && (reputationRewards != null || !commandRewardList.isEmpty()));
    }
    
    @Environment(EnvType.CLIENT)
    private void drawRewards(GuiQuestBook gui, NonNullList<ItemStack> rewards, int y, int selected, int mX, int mY, int max) {
        rewards = getEditFriendlyRewards(rewards, max);
        
        
        for (int i = 0; i < rewards.size(); i++) {
            gui.drawItemStack(rewards.get(i), START_X + i * REWARD_OFFSET, y, mX, mY, selected == i);
        }
    }
    
    @Environment(EnvType.CLIENT)
    private void drawRewardMouseOver(PoseStack matrices, GuiQuestBook gui, NonNullList<ItemStack> rewards, int y, int selected, int mX, int mY) {
        if (rewards != null) {
            for (int i = 0; i < rewards.size(); i++) {
                if (gui.inBounds(START_X + i * REWARD_OFFSET, y, ITEM_SIZE, ITEM_SIZE, mX, mY)) {
                    if (!rewards.get(i).isEmpty()) {
                        GuiQuestBook.setSelectedStack(rewards.get(i));
                        List<Component> str = rewards.get(i).getTooltipLines(Minecraft.getInstance().player, Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
                        List<FormattedText> list2 = Lists.newArrayList(str);
                        if (selected == i) {
                            list2.add(FormattedText.EMPTY);
                            list2.add(Translator.translatable("hqm.quest.selected", GuiColor.GREEN));
                        }
                        gui.renderTooltipL(matrices, list2, gui.getLeft() + mX, gui.getTop() + mY);
                    }
                    break;
                }
            }
        }
    }
    
    @Environment(EnvType.CLIENT)
    private NonNullList<ItemStack> getEditFriendlyRewards(NonNullList<ItemStack> rewards, int max) {
        if (rewards.isEmpty()) {
            return NonNullList.withSize(1, ItemStack.EMPTY);
        } else if (canQuestsBeEdited() && rewards.size() < max) {
            NonNullList<ItemStack> rewardsWithEmpty = NonNullList.create();
            rewardsWithEmpty.addAll(rewards);
            rewardsWithEmpty.add(ItemStack.EMPTY);
            return rewardsWithEmpty;
        } else {
            return rewards;
        }
    }
    
    @Environment(EnvType.CLIENT)
    private void handleRewardClick(GuiQuestBook gui, Player player, NonNullList<ItemStack> rawRewards, int y, boolean canSelect, int mX, int mY) {
        NonNullList<ItemStack> rewards = getEditFriendlyRewards(rawRewards, canSelect ? MAX_SELECT_REWARD_SLOTS : MAX_REWARD_SLOTS);
        
        boolean doubleClick = false;
        
        for (int i = 0; i < rewards.size(); i++) {
            if (gui.inBounds(START_X + i * REWARD_OFFSET, y, ITEM_SIZE, ITEM_SIZE, mX, mY)) {
                if (gui.getCurrentMode() == EditMode.NORMAL) {
                    int lastDiff = player.tickCount - lastClicked;
                    if (lastDiff < 0) {
                        lastClicked = player.tickCount;
                    } else if (lastDiff < 6) {
                        doubleClick = true;
                    } else {
                        lastClicked = player.tickCount;
                    }
                }
                if (canSelect && (!canQuestsBeEdited() || (gui.getCurrentMode() == EditMode.NORMAL && !doubleClick))) {
                    if (selectedReward == i) {
                        selectedReward = -1;
                    } else if (!rewards.get(i).isEmpty()) {
                        selectedReward = i;
                    }
                } else if (canQuestsBeEdited()) {
                    if (gui.getCurrentMode() == EditMode.DELETE) {
                        if (i < rawRewards.size()) {
                            rawRewards.remove(i);
                            if (canSelect && selectedReward != -1) {
                                if (selectedReward == i) {
                                    selectedReward = -1;
                                } else if (selectedReward > i) {
                                    selectedReward--;
                                }
                            }
                            
                            if (canSelect) {
                                this.rewardChoices.set(rawRewards);
                            } else {
                                this.rewards.set(rawRewards);
                            }
                            SaveHelper.add(SaveHelper.EditType.REWARD_REMOVE);
                        }
                    } else if (gui.getCurrentMode() == EditMode.ITEM || doubleClick) {
                        final int id = i;
                        PickItemMenu.display(gui, player, rewards.get(i), PickItemMenu.Type.ITEM, rewards.get(i).isEmpty() ? 1 : rewards.get(i).getCount(),
                                result -> {
                                    ItemStack stack = result.getStack().copy();
                                    stack.setCount(Math.min(127, result.getAmount()));
                                    this.setReward(stack, id, !canSelect);
                                });
                    }
                }
                
                break;
            }
        }
    }
    
    @Environment(EnvType.CLIENT)
    private int getTaskY(GuiQuestBook gui, int id) {
        return TASK_LABEL_START_Y + id * (TEXT_HEIGHT + TASK_MARGIN);
    }
    
    @Environment(EnvType.CLIENT)
    public void onClick(GuiQuestBook gui, Player player, int mX, int mY, int b) {
        if (b == 1) {
            gui.loadMap();
        } else {
            int id = 0;
            int start = taskScroll.isVisible(gui) ? Math.round((getVisibleTasks(gui) - VISIBLE_TASKS) * taskScroll.getScroll()) : 0;
            int end = Math.min(start + VISIBLE_TASKS, tasks.size());
            for (int i = start; i < end; i++) {
                QuestTask task = tasks.get(i);
                if (task.isVisible(player) || canQuestsBeEdited()) {
                    if (gui.inBounds(START_X, getTaskY(gui, id), gui.getStringWidth(task.getDescription()), TEXT_HEIGHT, mX, mY)) {
                        if (gui.isOpBook && Screen.hasShiftDown()) {
                            OPBookHelper.reverseTaskCompletion(task, player);
                            return;
                        }
                        if (canQuestsBeEdited() && gui.getCurrentMode() == EditMode.TASK) {
                            gui.setCurrentMode(EditMode.NORMAL);
                        }
                        if (canQuestsBeEdited() && (gui.getCurrentMode() == EditMode.RENAME || gui.getCurrentMode() == EditMode.DELETE)) {
                            if (gui.getCurrentMode() == EditMode.RENAME) {
                                gui.setEditMenu(new GuiEditMenuTextEditor(gui, player, task, true));
                            } else if (gui.getCurrentMode() == EditMode.DELETE) {
                                if (i + 1 < tasks.size()) {
                                    tasks.get(i + 1).clearRequirements();
                                    
                                    if (i > 0) {
                                        tasks.get(i + 1).addRequirement(tasks.get(i - 1));
                                    }
                                }
                                if (selectedTask == task) {
                                    selectedTask = null;
                                }
                                
                                task.onDelete();
                                
                                tasks.remove(i);
                                nextTaskId = 0;
                                for (QuestTask questTask : tasks) {
                                    questTask.updateId();
                                }
                                
                                addTaskData(getQuestData(player));
                                SaveHelper.add(SaveHelper.EditType.TASK_REMOVE);
                            }
                        } else if (task == selectedTask) {
                            selectedTask = null;
                        } else {
                            selectedTask = task;
                            taskDescriptionScroll.resetScroll();
                        }
                        break;
                    }
                    
                    id++;
                }
            }
            
            for (ScrollBar scrollBar : scrollBars) {
                scrollBar.onClick(gui, mX, mY);
            }
            
            if (!rewards.isEmpty() || canQuestsBeEdited()) {
                handleRewardClick(gui, player, rewards.toList(), REWARD_Y, false, mX, mY);
                if (!rewardChoices.isEmpty() || canQuestsBeEdited()) {
                    handleRewardClick(gui, player, rewardChoices.toList(), REWARD_Y + REWARD_Y_OFFSET, true, mX, mY);
                }
            } else if (!rewardChoices.isEmpty()) {
                handleRewardClick(gui, player, rewardChoices.toList(), REWARD_Y, true, mX, mY);
            }
            
            if (selectedTask != null) {
                selectedTask.onClick(gui, player, mX, mY, b);
            }
            
            
            for (LargeButton button : buttons) {
                if (button.inButtonBounds(gui, mX, mY) && button.isVisible(gui, player) && button.isEnabled(gui, player)) {
                    button.onClick(gui, player);
                    break;
                }
            }
            
            if (gui.getCurrentMode() == EditMode.RENAME) {
                if (gui.inBounds(START_X, TITLE_START_Y, 140, TEXT_HEIGHT, mX, mY)) {
                    gui.setEditMenu(new GuiEditMenuTextEditor(gui, player, this, true));
                } else if (gui.inBounds(START_X, DESCRIPTION_START_Y, 130, (int) (VISIBLE_DESCRIPTION_LINES * TEXT_HEIGHT * 0.7), mX, mY)) {
                    gui.setEditMenu(new GuiEditMenuTextEditor(gui, player, this, false));
                } else if (selectedTask != null && gui.inBounds(TASK_DESCRIPTION_X, TASK_DESCRIPTION_Y, 130, (int) (VISIBLE_DESCRIPTION_LINES * TEXT_HEIGHT * 0.7), mX, mY)) {
                    gui.setEditMenu(new GuiEditMenuTextEditor(gui, player, selectedTask, false));
                }
            }
            
            if (canQuestsBeEdited() && selectedTask != null && gui.getCurrentMode() == EditMode.TASK) {
                selectedTask = null;
            }
            
            if (canQuestsBeEdited() && gui.getCurrentMode() == EditMode.REPUTATION_REWARD) {
                int y = rewards == null || rewards.size() <= MAX_REWARD_SLOTS - (canQuestsBeEdited() ? 2 : 1) ? REPUTATION_Y_LOWER : REPUTATION_Y;
                if (gui.inBounds(REPUTATION_X, y, REPUTATION_SIZE, REPUTATION_SIZE, mX, mY)) {
                    gui.setEditMenu(new GuiEditMenuReputationReward(gui, player, reputationRewards));
                }
            }
        }
    }
    
    @Environment(EnvType.CLIENT)
    public void onDrag(GuiQuestBook gui, Player player, int mX, int mY, int b) {
        for (ScrollBar scrollBar : scrollBars) {
            scrollBar.onDrag(gui, mX, mY);
        }
    }
    
    @Environment(EnvType.CLIENT)
    public void onRelease(GuiQuestBook gui, Player player, int mX, int mY, int b) {
        for (ScrollBar scrollBar : scrollBars) {
            scrollBar.onRelease(gui, mX, mY);
        }
    }
    
    public QuestData createData(int players) {
        QuestData data = new QuestData(players);
        if (!addTaskData(data)) {
            return null;
        }
        return data;
    }
    
    public boolean addTaskData(QuestData data) {
        data.tasks = new QuestDataTask[tasks.size()];
        for (int i = 0; i < tasks.size(); i++) {
            try {
                Constructor<? extends QuestDataTask> constructor = tasks.get(i).getDataType().getConstructor(QuestTask.class);
                QuestDataTask obj = constructor.newInstance(tasks.get(i));
                data.tasks[i] = obj;
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }
        
        return true;
    }
    
    public void initRewards(int players, QuestData data) {
        data.reward = new boolean[players];
    }
    
    public List<QuestTask> getTasks() {
        return tasks;
    }
    
    public void sendUpdatedDataToTeam(Player player) {
        sendUpdatedDataToTeam(QuestingDataManager.getInstance().getQuestingData(player).getTeam());
    }
    
    public void sendUpdatedDataToTeam(UUID playerId) {
        sendUpdatedDataToTeam(QuestingDataManager.getInstance().getQuestingData(playerId).getTeam());
    }
    
    public void sendUpdatedDataToTeam(Team team) {
        for (PlayerEntry entry : team.getPlayers()) {
            sendUpdatedData(entry.getPlayerMP());
        }
    }
    
    private void sendUpdatedData(ServerPlayer player) {
        if (player == null) return; // Don't send to nobody you silly goose
        IMessage update = new QuestDataUpdateMessage(
                getQuestId(),
                QuestingDataManager.getInstance().getQuestingData(player).getTeam().getPlayerCount(),
                QuestingDataManager.getInstance().getQuestingData(player).getQuestData(getQuestId())
        );
        NetworkManager.sendToPlayer(update, player);
    }
    
    public void claimReward(Player player, int selectedReward) {
        if (hasReward(player)) {
            boolean sentInfo = false;
            if (getQuestData(player).getReward(player) && (!rewards.isEmpty() || !rewardChoices.isEmpty())) {
                List<ItemStack> items = new ArrayList<>();
                if (!rewards.isEmpty()) {
                    for (ItemStack stack : rewards.toList()) {
                        items.add(stack.copy());
                    }
                }
                if (!rewardChoices.isEmpty()) {
                    if (selectedReward >= 0 && selectedReward < rewardChoices.size()) {
                        items.add(rewardChoices.getReward(selectedReward).copy());
                    } else {
                        return;
                    }
                }
                
                List<ItemStack> itemsToAdd = new ArrayList<>();
                for (ItemStack stack : items) {
                    boolean added = false;
                    for (ItemStack stack1 : itemsToAdd) {
                        if (stack.sameItemStackIgnoreDurability(stack1) && ItemStack.tagMatches(stack, stack1)) {
                            stack1.grow(stack.getCount());
                            added = true;
                            break;
                        }
                    }
                    
                    if (!added) {
                        itemsToAdd.add(stack.copy());
                    }
                }
                
                List<ItemStack> itemsToCheck = new ArrayList<>();
                for (ItemStack stack : itemsToAdd) {
                    itemsToCheck.add(stack.copy());
                }
                for (int i = 0; i < player.inventory.items.size(); i++) {
                    for (ItemStack stack1 : itemsToCheck) {
                        if (stack1.getCount() > 0) {
                            ItemStack stack = player.inventory.items.get(i);
                            if (stack == ItemStack.EMPTY) {
                                stack1.shrink(stack1.getMaxStackSize());
                                break;
                            } else if (stack.sameItemStackIgnoreDurability(stack1) && ItemStack.tagMatches(stack1, stack)) {
                                stack1.shrink(stack1.getMaxStackSize() - stack.getCount());
                                break;
                            }
                        }
                    }
                    
                }
                
                
                boolean valid = true;
                for (ItemStack stack : itemsToCheck) {
                    if (stack.getCount() > 0) {
                        valid = false;
                        break;
                    }
                }
                
                if (valid) {
                    addItems(player, itemsToAdd);
                    player.inventory.setChanged();
                    QuestData data = getQuestData(player);
                    Team team = QuestingDataManager.getInstance().getQuestingData(player).getTeam();
                    if (!team.isSingle() && team.getRewardSetting() == RewardSetting.ANY) {
                        Arrays.fill(data.reward, false);
                        sendUpdatedDataToTeam(player);
                    } else {
                        data.claimReward(player);
                        if (player instanceof ServerPlayer)
                            sendUpdatedData((ServerPlayer) player);
                    }
                    sentInfo = true;
                } else {
                    return;
                }
            }
            
            
            if (reputationRewards != null && getQuestData(player).canClaim()) {
                getQuestData(player).claimed = true;
                QuestingDataManager.getInstance().getQuestingData(player).getTeam().receiveAndSyncReputation(this, reputationRewards);
                EventTrigger.instance().onReputationChange(new EventTrigger.ReputationEvent(player));
                sentInfo = true;
            }
            
            if (commandRewardList != null && getQuestData(player).canClaim()) {
                getQuestData(player).claimed = true;
                commandRewardList.executeAll(player);
                sentInfo = true;
            }
            
            if (sentInfo) {
                SoundHandler.play(Sounds.COMPLETE, player);
            }
            
        }
    }
    
    public void setBigIcon(boolean b) {
        isBig = b;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
        cachedDescription = null;
    }
    
    private void setReward(ItemStack stack, int id, boolean isStandardReward) {
        ItemStackRewardList rewardList = isStandardReward ? this.rewards : this.rewardChoices;
        
        if (id < rewardList.size()) {
            rewardList.set(id, stack);
            SaveHelper.add(SaveHelper.EditType.REWARD_CHANGE);
        } else {
            SaveHelper.add(SaveHelper.EditType.REWARD_CREATE);
            rewardList.add(stack);
        }
    }
    
    public boolean hasSameSetAs(Quest child) {
        return child.set.equals(set);
    }
    
    @Environment(EnvType.CLIENT)
    public void onScroll(GuiQuestBook gui, double x, double y, double scroll) {
        for (ScrollBar scrollBar : scrollBars) {
            scrollBar.onScroll(gui, x, y, scroll);
        }
    }
    
    @Environment(EnvType.CLIENT)
    public void onOpen(GuiQuestBook gui, Player player) {
        if (selectedTask == null) {
            for (QuestTask task : tasks) {
                if (!task.isCompleted(player)) {
                    selectedTask = task;
                    break;
                }
            }
        }
        
        if (selectedTask == null && tasks.size() > 0)
            selectedTask = tasks.get(0);
        
        QuestingDataManager.getInstance().getQuestingData(player).selectedQuestId = getQuestId();
        QuestingDataManager.getInstance().getQuestingData(player).selectedTask = selectedTask == null ? -1 : selectedTask.getId();
        if (selectedTask != null) {
            //NetworkManager.sendToServer(ClientChange.SELECT_QUEST.build(selectedTask));
            GeneralUsage.sendBookSelectTaskUpdate(Quest.this.selectedTask);
        }
        
        EventTrigger.instance().onQuestSelected(new EventTrigger.QuestSelectedEvent(player, this.getQuestId()));
    }
    
    public boolean hasSet(QuestSet selectedSet) {
        return set != null && set.equals(selectedSet);
    }
    
    public void mergeProgress(UUID playerId, QuestData own, QuestData other) {
        if (other.completed) {
            own.completed = true;
            
            //If a quest is marked both claimed & available, then repeatable quests will never reset.
            if (other.available && !own.claimed) {
                own.available = true;
            }
        }
        
        
        for (int i = 0; i < own.tasks.length; i++) {
            QuestTask task = tasks.get(i);
            own.tasks[i] = task.validateData(own.tasks[i]);
            task.mergeProgress(playerId, own.tasks[i], task.validateData(other.tasks[i]));
        }
    }
    
    public void copyProgress(QuestData own, QuestData other) {
        own.completed = other.completed;
        own.available = other.available;
        
        for (int i = 0; i < own.tasks.length; i++) {
            QuestTask task = tasks.get(i);
            own.tasks[i] = task.validateData(own.tasks[i]);
            task.copyProgress(own.tasks[i], task.validateData(other.tasks[i]));
        }
    }
    
    public void completeQuest(Player player) {
        for (QuestTask task : tasks) {
            task.autoComplete(player.getUUID());
            task.getData(player).completed = true;
        }
        QuestTask.completeQuest(this, player.getUUID());
    }
    
    public void reset(UUID playerId) {
        reset(getQuestData(playerId));
    }
    
    public void reset(QuestData data) {
        data.available = true;
        addTaskData(data);
    }
    
    public void resetAll() {
        for (Team team : TeamManager.getInstance().getTeams()) {
            QuestData data = team.getQuestData(getQuestId());
            if (data != null && !data.available) {
                reset(data);
                sendUpdatedDataToTeam(team);
            }
        }
    }
    
    public void resetOnTime(long time) {
        for (Team team : TeamManager.getInstance().getTeams()) {
            QuestData data = team.getQuestData(getQuestId());
            if (data != null && !data.available && data.time <= time) {
                reset(data);
                sendUpdatedDataToTeam(team);
            }
        }
    }
    
    public float getProgress(Team team) {
        UUID uuid = team.getPlayers().get(0).getUUID();
        float data = 0;
        for (QuestTask task : this.tasks) {
            data += task.getCompletedRatio(uuid);
        }
        
        return data / this.tasks.size();
    }
    
    public List<Quest> getOptionLinks() {
        return QuestSetsManager.getInstance().quests.values().stream().filter(quest -> optionLinks.contains(quest.getQuestId())).collect(Collectors.toList());
    }
    
    public List<Quest> getReversedOptionLinks() {
        return QuestSetsManager.getInstance().quests.values().stream().filter(quest -> reversedOptionLinks.contains(quest.getQuestId())).collect(Collectors.toList());
    }
    
    public boolean getUseModifiedParentRequirement() {
        return parentRequirementCount != -1;
    }
    
    public int getParentRequirementCount() {
        return parentRequirementCount == -1 ? this.getRequirements().size() : this.parentRequirementCount;
    }
    
    public int _getParentRequirementCount() {
        return parentRequirementCount;
    }
    
    public void setParentRequirementCount(int parentRequirementCount) {
        this.parentRequirementCount = parentRequirementCount;
    }
    
    public QuestSet getQuestSet() {
        return set;
    }
    
    public void setQuestSet(QuestSet set) {
        if (this.set != null)
            this.set.removeQuest(this);
        this.set = set;
        if (this.set != null)
            this.set.addQuest(this);
    }
    
    public List<Quest> getReversedRequirement() {
        return QuestSetsManager.getInstance().quests.values().stream().filter(quest -> reversedRequirement.contains(quest.getQuestId())).collect(Collectors.toList());
    }
    
    public static boolean canQuestsBeEdited() {
        if (isEditing && !HQMUtil.isGameSingleplayer()) {
            setEditMode(false);
        }
        return isEditing;
    }
    
    public static void setEditMode(boolean enabled) {
        isEditing = enabled;
    }
    
    public enum TaskType {
        CONSUME(QuestTaskItemsConsume.class, "consume"),
        CRAFT(QuestTaskItemsCrafting.class, "craft"),
        LOCATION(QuestTaskLocation.class, "location"),
        CONSUME_QDS(QuestTaskItemsConsumeQDS.class, "consumeQDS"),
        DETECT(QuestTaskItemsDetect.class, "detect"),
        KILL(QuestTaskMob.class, "kill"),
        TAME(QuestTaskTame.class, "tame"),
        DEATH(QuestTaskDeath.class, "death"),
        REPUTATION(QuestTaskReputationTarget.class, "reputation"),
        REPUTATION_KILL(QuestTaskReputationKill.class, "reputationKill"),
        ADVANCEMENT(QuestTaskAdvancement.class, "advancement"),
        COMPLETION(QuestTaskCompleted.class, "completion"),
        BLOCK_BREAK(QuestTaskBlockBreak.class, "break"),
        BLOCK_PLACE(QuestTaskBlockPlace.class, "place");
        
        private final Class<? extends QuestTask> clazz;
        private final String id;
        
        TaskType(Class<? extends QuestTask> clazz, String id) {
            this.clazz = clazz;
            this.id = id;
        }
        
        public static TaskType getType(Class<? extends QuestTask> clazz) {
            for (TaskType type : values()) {
                if (type.clazz == clazz) return type;
            }
            return CONSUME;
        }
        
        public QuestTask addTask(Quest quest) {
            QuestTask prev = quest.getTasks().size() > 0 ? quest.getTasks().get(quest.getTasks().size() - 1) : null;
            try {
                Constructor<? extends QuestTask> ex = clazz.getConstructor(Quest.class, String.class, String.class);
                QuestTask task = ex.newInstance(quest, getName(), getDescription());
                if (prev != null) {
                    task.addRequirement(prev);
                }
                quest.getTasks().add(task);
                SaveHelper.add(SaveHelper.EditType.TASK_CREATE);
                return task;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        
        public String getLangKeyDescription() {
            return "hqm.taskType." + id + ".desc";
        }
        
        public String getLangKeyName() {
            return "hqm.taskType." + id + ".title";
        }
        
        public String getDescription() {
            return Translator.get(getLangKeyDescription());
        }
        
        public String getName() {
            return Translator.get(getLangKeyName());
        }
    }
    
    private abstract class ParentEvaluator {
        
        protected abstract boolean isValid(UUID uuid, Quest parent, Map<Quest, Boolean> isVisibleCache, Map<Quest, Boolean> isLinkFreeCache);
        
        private boolean isValid(UUID uuid, Map<Quest, Boolean> isVisibleCache, Map<Quest, Boolean> isLinkFreeCache) {
            int parents = getRequirements().size();
            int requiredAmount = getParentRequirementCount();
            if (requiredAmount > parents) {
                return false;
            }
            
            int allowedUncompleted = parents - requiredAmount;
            int uncompleted = 0;
            for (Quest quest : getRequirements()) {
                if (!isValid(uuid, quest, isVisibleCache, isLinkFreeCache)) {
                    uncompleted++;
                    if (uncompleted > allowedUncompleted) {
                        return false;
                    }
                }
            }
            return true;
        }
        
    }
    
}
