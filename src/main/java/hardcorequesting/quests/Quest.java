package hardcorequesting.quests;

import java.awt.Polygon;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import hardcorequesting.client.ClientChange;
import hardcorequesting.client.EditMode;
import hardcorequesting.client.interfaces.GuiBase;
import hardcorequesting.client.interfaces.GuiColor;
import hardcorequesting.client.interfaces.GuiQuestBook;
import hardcorequesting.client.interfaces.LargeButton;
import hardcorequesting.client.interfaces.ResourceHelper;
import hardcorequesting.client.interfaces.ScrollBar;
import hardcorequesting.client.interfaces.edit.GuiEditMenuDeathTask;
import hardcorequesting.client.interfaces.edit.GuiEditMenuItem;
import hardcorequesting.client.interfaces.edit.GuiEditMenuReputationKillTask;
import hardcorequesting.client.interfaces.edit.GuiEditMenuReputationReward;
import hardcorequesting.client.interfaces.edit.GuiEditMenuTextEditor;
import hardcorequesting.client.sounds.SoundHandler;
import hardcorequesting.client.sounds.Sounds;
import hardcorequesting.event.EventTrigger;
import hardcorequesting.network.GeneralUsage;
import hardcorequesting.network.NetworkManager;
import hardcorequesting.network.message.QuestDataUpdateMessage;
import hardcorequesting.quests.data.QuestDataTask;
import hardcorequesting.quests.reward.CommandRewardList;
import hardcorequesting.quests.reward.ItemStackRewardList;
import hardcorequesting.quests.reward.ReputationReward;
import hardcorequesting.quests.task.*;
import hardcorequesting.team.PlayerEntry;
import hardcorequesting.team.RewardSetting;
import hardcorequesting.team.Team;
import hardcorequesting.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
    public static boolean saveDefault = true;
    public static boolean useDefault = true;
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
    private List<String> cachedDescription;
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
            public boolean isEnabled(GuiBase gui, EntityPlayer player) {
                return canPlayerClaimReward(player);
            }

            @Override
            public boolean isVisible(GuiBase gui, EntityPlayer player) {
                return hasReward(player);
            }

            @Override
            public void onClick(GuiBase gui, EntityPlayer player) {
                NetworkManager.sendToServer(ClientChange.CLAIM_QUEST.build(new Tuple<>(getQuestId(), rewardChoices.isEmpty() ? -1 : selectedReward)));
            }
        });

        buttons.add(new LargeButton("hqm.quest.manualSubmit", 185, 200) {
            @Override
            public boolean isEnabled(GuiBase gui, EntityPlayer player) {
                return selectedTask.allowManual();
            }

            @Override
            public boolean isVisible(GuiBase gui, EntityPlayer player) {
                return selectedTask != null && selectedTask.allowManual() && !selectedTask.isCompleted(player);
            }

            @Override
            public void onClick(GuiBase gui, EntityPlayer player) {
                NetworkManager.sendToServer(ClientChange.UPDATE_TASK.build(selectedTask));
            }
        });

        buttons.add(new LargeButton("hqm.quest.manualDetect", 185, 200) {
            @Override
            public boolean isEnabled(GuiBase gui, EntityPlayer player) {
                return selectedTask.allowDetect();
            }

            @Override
            public boolean isVisible(GuiBase gui, EntityPlayer player) {
                return selectedTask != null && selectedTask.allowDetect() && !selectedTask.isCompleted(player);
            }

            @Override
            public void onClick(GuiBase gui, EntityPlayer player) {
                NetworkManager.sendToServer(ClientChange.UPDATE_TASK.build(selectedTask));
            }
        });

        buttons.add(new LargeButton("hqm.quest.requirement", 185, 200) {
            @Override
            @SideOnly(Side.CLIENT)
            public boolean isEnabled(GuiBase gui, EntityPlayer player) {
                return true;
            }

            @Override
            @SideOnly(Side.CLIENT)
            public boolean isVisible(GuiBase gui, EntityPlayer player) {
                return selectedTask != null && selectedTask instanceof QuestTaskDeath && Quest.canQuestsBeEdited();
            }

            @Override
            @SideOnly(Side.CLIENT)
            public void onClick(GuiBase gui, EntityPlayer player) {
                gui.setEditMenu(new GuiEditMenuDeathTask(gui, player, (QuestTaskDeath) selectedTask));
            }
        });

        buttons.add(new LargeButton("hqm.quest.requirement", 250, 95) {
            @Override
            @SideOnly(Side.CLIENT)
            public boolean isEnabled(GuiBase gui, EntityPlayer player) {
                return true;
            }

            @Override
            @SideOnly(Side.CLIENT)
            public boolean isVisible(GuiBase gui, EntityPlayer player) {
                return selectedTask != null && selectedTask instanceof QuestTaskReputationKill && Quest.canQuestsBeEdited();
            }

            @Override
            @SideOnly(Side.CLIENT)
            public void onClick(GuiBase gui, EntityPlayer player) {
                gui.setEditMenu(new GuiEditMenuReputationKillTask(gui, player, (QuestTaskReputationKill) selectedTask));
            }
        });


        buttons.add(new LargeButton("hqm.quest.selectTask", 250, 200) {
            @Override
            public boolean isEnabled(GuiBase gui, EntityPlayer player) {
                QuestingData data = QuestingData.getQuestingData(player);
                if(data != null && data.selectedQuestId != null && data.selectedQuestId.equals(getQuestId())){
                    return data.selectedTask == selectedTask.getId();
                }
                return false;
            }

            @Override
            public boolean isVisible(GuiBase gui, EntityPlayer player) {
                return selectedTask instanceof QuestTaskItemsConsume && !selectedTask.isCompleted(player);
            }

            @Override
            public void onClick(GuiBase gui, EntityPlayer player) {
                //update locally too, then we don't have to refresh all the data(i.e. the server won't notify us about the change we already know about)
                QuestingData.getQuestingData(player).selectedQuestId = getQuestId();
                QuestingData.getQuestingData(player).selectedTask = selectedTask.getId();

                player.sendMessage(new TextComponentTranslation("tile.hqm:item_barrele.selectedTask", selectedTask.getDescription()).setStyle(new Style().setColor(TextFormatting.GREEN)));

                //NetworkManager.sendToServer(ClientChange.SELECT_QUEST.build(selectedTask));
                GeneralUsage.sendBookSelectTaskUpdate(Quest.this.selectedTask);
            }
        });

        int itemIds = 0;
        for (final TaskType taskType : TaskType.values()) {
            buttons.add(new LargeButton(taskType.getLangKeyName(), taskType.getLangKeyDescription(), 185 + (taskType.ordinal() % 2) * 65, 50 + (taskType.ordinal() / 2) * 20) {
                @Override
                public boolean isEnabled(GuiBase gui, EntityPlayer player) {
                    return true;
                }

                @Override
                public boolean isVisible(GuiBase gui, EntityPlayer player) {
                    return canQuestsBeEdited() && selectedTask == null && ((GuiQuestBook) gui).getCurrentMode() == EditMode.TASK;
                }

                @Override
                public void onClick(GuiBase gui, EntityPlayer player) {
                    taskType.addTask(Quest.this);
                }
            });

            if (QuestTaskItems.class.isAssignableFrom(taskType.clazz)) {
                buttons.add(new LargeButton(taskType.getLangKeyName(), taskType.getLangKeyDescription(), 185 + (itemIds % 2) * 65, 50 + (itemIds / 2) * 35) {
                    @Override
                    public boolean isEnabled(GuiBase gui, EntityPlayer player) {
                        return selectedTask instanceof QuestTaskItems;
                    }

                    @Override
                    public boolean isVisible(GuiBase gui, EntityPlayer player) {
                        return false; // canQuestsBeEdited() && selectedTask != null && ((GuiQuestBook) gui).getCurrentMode() == EditMode.CHANGE_TASK;
                    }

                    @Override
                    public void onClick(GuiBase gui, EntityPlayer player) {
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
            @Override
            public boolean isVisible(GuiBase gui) {
                return getCachedDescription(gui).size() > VISIBLE_DESCRIPTION_LINES;
            }
        });
        scrollBars.add(taskDescriptionScroll = new ScrollBar(312, 18, 64, 249, 102, TASK_DESCRIPTION_X) {
            @Override
            public boolean isVisible(GuiBase gui) {
                return selectedTask != null && selectedTask.getCachedLongDescription(gui).size() > VISIBLE_DESCRIPTION_LINES;
            }
        });

        scrollBars.add(taskScroll = new ScrollBar(155, 100, 29, 242, 102, START_X) {
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

        QuestLine.getActiveQuestLine().quests.put(getQuestId(), this);
    }

    public static Map<UUID, Quest> getQuests() {
        return QuestLine.getActiveQuestLine().quests;
    }

    public static List<QuestSet> getQuestSets() {
        return QuestLine.getActiveQuestLine().questSets;
    }

    @SideOnly(Side.CLIENT)
    public static List<String> getMainDescription(GuiBase gui) {
        if (QuestLine.getActiveQuestLine().cachedMainDescription == null) {
            QuestLine.getActiveQuestLine().cachedMainDescription = gui.getLinesFromText(QuestLine.getActiveQuestLine().mainDescription, 0.7F, 130);
        }

        return QuestLine.getActiveQuestLine().cachedMainDescription;
    }

    public static String getRawMainDescription() {
        return QuestLine.getActiveQuestLine().mainDescription;
    }

    public static void setMainDescription(String mainDescription) {
        QuestLine.getActiveQuestLine().mainDescription = mainDescription;
        QuestLine.getActiveQuestLine().cachedMainDescription = null;
    }

    public static Quest getQuest(UUID questId) {
        if (questId == null) return null;

        return QuestLine.getActiveQuestLine().quests.get(questId);
    }

    public static void addItems(EntityPlayer player, List<ItemStack> itemsToAdd) {
        for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
            Iterator<ItemStack> iterator = itemsToAdd.iterator();
            while (iterator.hasNext()) {
                ItemStack nextStack = iterator.next();
                ItemStack stack = player.inventory.mainInventory.get(i);

                if (stack.isEmpty()) {
                    int amount = Math.min(nextStack.getMaxStackSize(), nextStack.getCount());
                    ItemStack copyStack = nextStack.copy();
                    copyStack.setCount(amount);
                    player.inventory.mainInventory.set(i, copyStack);
                    nextStack.shrink(amount);
                    if (nextStack.getCount() <= 0) {
                        iterator.remove();
                    }
                    break;
                } else if (stack.isItemEqual(nextStack) && ItemStack.areItemStackTagsEqual(nextStack, stack)) {
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
        QuestLine.getActiveQuestLine().quests.remove(quest.getQuestId());

        for (Quest other : QuestLine.getActiveQuestLine().quests.values()) {
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

    public ItemStack[] getReward() {
        return rewards.toArray();
    }

    public void setReward(ItemStack[] reward) {
        this.rewards.set(reward);
    }

    public ItemStack[] getRewardChoice() {
        return rewardChoices.toArray();
    }

    public void setRewardChoice(ItemStack[] rewardChoice) {
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
        if (lookForId(requirementQuestId, false) || lookForId(requirementQuestId, true)) return;

        Quest quest = QuestLine.getActiveQuestLine().quests.get(requirementQuestId);
        if (quest != null) {
            requirement.add(quest.getQuestId());
            quest.reversedRequirement.add(this.getQuestId());
            SaveHelper.add(SaveHelper.EditType.REQUIREMENT_CHANGE);
        }
    }

    private boolean lookForId(UUID questId, boolean reversed) {
        List<UUID> currentRequirements = reversed ? reversedRequirement : requirement;
        for (UUID id : currentRequirements)
            if (id.equals(questId) || QuestLine.getActiveQuestLine().quests.get(id).lookForId(questId, reversed))
                return true;
        return false;
    }

    public void clearRequirements() {
        SaveHelper.add(SaveHelper.EditType.REQUIREMENT_REMOVE, requirement.size());
        for (UUID questId : requirement)
            QuestLine.getActiveQuestLine().quests.get(questId).reversedRequirement.remove(getQuestId());
        requirement.clear();
    }

    public void addOptionLink(UUID optionLinkId) {
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

        Quest quest = QuestLine.getActiveQuestLine().quests.get(optionLinkId);
        if (quest != null) {
            SaveHelper.add(SaveHelper.EditType.OPTION_CHANGE);
            optionLinks.add(quest.getQuestId());
            quest.reversedOptionLinks.add(getQuestId());
        }
    }

    public void clearOptionLinks() {
        SaveHelper.add(SaveHelper.EditType.OPTION_REMOVE, optionLinks.size());
        for (UUID questId : reversedOptionLinks) {
            QuestLine.getActiveQuestLine().quests.get(questId).optionLinks.remove(getQuestId());
        }

        for (UUID questId : optionLinks) {
            QuestLine.getActiveQuestLine().quests.get(questId).reversedOptionLinks.remove(getQuestId());
        }
        reversedRequirement.clear();
        optionLinks.clear();
    }

    public QuestData getQuestData(EntityPlayer player) {
        return QuestingData.getQuestingData(player).getQuestData(getQuestId());
    }

    public QuestData getQuestData(UUID uuid) {
        return QuestingData.getQuestingData(uuid).getQuestData(getQuestId());
    }

    public void setQuestData(EntityPlayer player, QuestData data) {
        QuestingData.getQuestingData(player).setQuestData(getQuestId(), data);
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

    public boolean isVisible(EntityPlayer player) {
        return isVisible(player.getPersistentID());
    }

    boolean isVisible(EntityPlayer player, Map<Quest, Boolean> isVisibleCache, Map<Quest, Boolean> isLinkFreeCache) {
        return isVisible(player.getPersistentID(), isVisibleCache, isLinkFreeCache);
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

    public boolean isEnabled(EntityPlayer player) {
        return isEnabled(player.getPersistentID());
    }

    boolean isEnabled(EntityPlayer player, Map<Quest, Boolean> isVisibleCache, Map<Quest, Boolean> isLinkFreeCache) {
        return isEnabled(player.getPersistentID(), true, isVisibleCache, isLinkFreeCache);
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

    public boolean isLinkFree(EntityPlayer player) {
        return isLinkFree(player.getPersistentID(), new HashMap<>());
    }

    boolean isLinkFree(EntityPlayer player, Map<Quest, Boolean> cache) {
        return isLinkFree(player.getPersistentID(), cache);
    }

    public boolean isLinkFree(UUID uuid) {
        return isLinkFree(uuid, new HashMap<>());
    }

    boolean isLinkFree(UUID playerId, Map<Quest, Boolean> cache) {
        Boolean cachedResult = cache.get(this);
        if (cachedResult != null) return cachedResult;

        boolean result = true;
        for (UUID optionLinkId : optionLinks) {
            if (QuestLine.getActiveQuestLine().quests.get(optionLinkId).isCompleted(playerId)) {
                result = false;
                break;
            }
        }

        if (result) {
            for (UUID optionLinkId : reversedOptionLinks) {
                if (QuestLine.getActiveQuestLine().quests.get(optionLinkId).isCompleted(playerId)) {
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

    public boolean isAvailable(EntityPlayer player) {
        return isAvailable(player.getPersistentID());
    }

    public boolean isCompleted(EntityPlayer player) {
        return isCompleted(player.getPersistentID());
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
        return this.requirement.stream().map(QuestLine.getActiveQuestLine().quests::get).collect(Collectors.toList());
    }

    //interface stuff
    public int getGuiX() {
        return x;
    }

    public int getGuiY() {
        return y;
    }

    public int getGuiU() {
        return isBig ? GuiQuestBook.PAGE_WIDTH + 25 : GuiQuestBook.PAGE_WIDTH;
    }

    public int getGuiV(EntityPlayer player, int x, int y) {
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

    @SideOnly(Side.CLIENT)
    public int getColorFilter(EntityPlayer player, int tick) {
        if (canQuestsBeEdited() && !isVisible(player)) {
            return 0x55FFFFFF;
        } else if (!isEnabled(player)) {
            return 0xFF888888;
        } else if (!isAvailable(player) && !hasReward(player)) {
            return getRepeatInfo().getType() == RepeatType.NONE ? 0xFFFFFFFF : 0xFFFFFFCC;
        } else {
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

    @SideOnly(Side.CLIENT)
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

    @SideOnly(Side.CLIENT)
    private List<String> getCachedDescription(GuiBase gui) {
        if (cachedDescription == null) {
            cachedDescription = gui.getLinesFromText(description, 0.7F, 130);
        }
        return cachedDescription;
    }

    @SideOnly(Side.CLIENT)
    public void drawMenu(GuiQuestBook gui, EntityPlayer player, int mX, int mY) {
        if (!canQuestsBeEdited() && selectedTask != null && !selectedTask.isVisible(player)) {
            if (tasks.size() > 0) {
                selectedTask = tasks.get(0);
            } else {
                selectedTask = null;
            }
        }

        gui.drawString(name, START_X, TITLE_START_Y, 0x404040);

        int startLine = descriptionScroll.isVisible(gui) ? Math.round((getCachedDescription(gui).size() - VISIBLE_DESCRIPTION_LINES) * descriptionScroll.getScroll()) : 0;
        gui.drawString(getCachedDescription(gui), startLine, VISIBLE_DESCRIPTION_LINES, START_X, DESCRIPTION_START_Y, 0.7F, 0x404040);

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
                gui.drawString(task.getDescription(), START_X, yPos, completed ? isSelected ? inBounds ? 0x40BB40 : 0x40A040 : inBounds ? 0x10A010 : 0x107010 : isSelected ? inBounds ? 0xAAAAAA : 0x888888 : inBounds ? 0x666666 : isVisible ? 0x404040 : 0xDDDDDD);

                id++;
            }
        }

        if (selectedReward != -1 && !hasReward(player)) {
            selectedReward = -1;
        }
        if (!rewards.isEmpty() || canQuestsBeEdited()) {
            gui.drawString(Translator.translate("hqm.quest.rewards"), START_X, REWARD_STR_Y, 0x404040);
            drawRewards(gui, rewards.toArray(), REWARD_Y, -1, mX, mY, MAX_SELECT_REWARD_SLOTS);
            if (!rewardChoices.isEmpty() || canQuestsBeEdited()) {
                gui.drawString(Translator.translate("hqm.quest.pickOne"), START_X, REWARD_STR_Y + REWARD_Y_OFFSET, 0x404040);
                drawRewards(gui, rewardChoices.toArray(), REWARD_Y + REWARD_Y_OFFSET, selectedReward, mX, mY, MAX_REWARD_SLOTS);
            }
        } else if (!rewardChoices.isEmpty()) {
            gui.drawString(Translator.translate("hqm.quest.pickOneReward"), START_X, REWARD_STR_Y, 0x404040);
            drawRewards(gui, rewardChoices.toArray(), REWARD_Y, selectedReward, mX, mY, MAX_REWARD_SLOTS);
        }

        for (LargeButton button : buttons) {
            button.draw(gui, player, mX, mY);
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
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
                List<String> description = selectedTask.getCachedLongDescription(gui);
                int taskStartLine = taskDescriptionScroll.isVisible(gui) ? Math.round((description.size() - VISIBLE_DESCRIPTION_LINES) * taskDescriptionScroll.getScroll()) : 0;
                gui.drawString(description, taskStartLine, VISIBLE_DESCRIPTION_LINES, TASK_DESCRIPTION_X, TASK_DESCRIPTION_Y, 0.7F, 0x404040);

                selectedTask.draw(gui, player, mX, mY);
            //}
        } else if (canQuestsBeEdited() && gui.getCurrentMode() == EditMode.TASK) {
            gui.drawString(gui.getLinesFromText(Translator.translate("hqm.quest.createTasks"), 0.7F, 130), 180, 20, 0.7F, 0x404040);
        /*} else if (canQuestsBeEdited() && gui.getCurrentMode() == EditMode.CHANGE_TASK) {
            gui.drawString(gui.getLinesFromText(Translator.translate("hqm.quest.itemTaskTypeChange"), 0.7F, 130), 180, 20, 0.7F, 0x404040);*/
        }

        if (!rewards.isEmpty() || canQuestsBeEdited()) {
            drawRewardMouseOver(gui, rewards.toArray(), REWARD_Y, -1, mX, mY);
            if (!rewardChoices.isEmpty() || canQuestsBeEdited()) {
                drawRewardMouseOver(gui, rewardChoices.toArray(), REWARD_Y + REWARD_Y_OFFSET, selectedReward, mX, mY);
            }
        } else if (!rewardChoices.isEmpty()) {
            drawRewardMouseOver(gui, rewardChoices.toArray(), REWARD_Y, selectedReward, mX, mY);
        }
        for (LargeButton button : buttons) {
            button.drawMouseOver(gui, player, mX, mY);
        }

        if (reputationRewards != null && hover) {
            List<String> str = new ArrayList<String>();
            for (ReputationReward reputationReward : reputationRewards) {
                if (reputationReward.getValue() != 0 && reputationReward.getReward() != null && reputationReward.getReward().isValid()) {
                    str.add(reputationReward.getLabel());
                }

            }

            List<String> commentLines = gui.getLinesFromText(Translator.translate("hqm.quest.partyRepReward" + (claimed ? "Claimed" : "")), 1, 200);
            if (commentLines != null) {
                str.add("");
                for (String commentLine : commentLines) {
                    str.add(GuiColor.GRAY + commentLine);
                }
            }
            gui.drawMouseOver(str, mX + gui.getLeft(), mY + gui.getTop());
        }
    }

    @SideOnly(Side.CLIENT)
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

    private boolean canPlayerClaimReward(EntityPlayer player) {
        return hasReward(player) && (rewardChoices.isEmpty() || selectedReward != -1) && isEnabled(player);
    }

    public boolean hasReward(EntityPlayer player) {
        return (getQuestData(player).getReward(player) && (!rewards.isEmpty() || !rewardChoices.isEmpty())) || (getQuestData(player).canClaim() && (reputationRewards != null || !commandRewardList.isEmpty()));
    }

    @SideOnly(Side.CLIENT)
    private void drawRewards(GuiQuestBook gui, ItemStack[] rewards, int y, int selected, int mX, int mY, int max) {
        rewards = getEditFriendlyRewards(rewards, max);


        for (int i = 0; i < rewards.length; i++) {
            gui.drawItemStack(rewards[i], START_X + i * REWARD_OFFSET, y, mX, mY, selected == i);
        }
    }

    @SideOnly(Side.CLIENT)
    private void drawRewardMouseOver(GuiQuestBook gui, ItemStack[] rewards, int y, int selected, int mX, int mY) {
        if (rewards != null) {
            for (int i = 0; i < rewards.length; i++) {
                if (gui.inBounds(START_X + i * REWARD_OFFSET, y, ITEM_SIZE, ITEM_SIZE, mX, mY)) {
                    if (rewards[i] != null) {
                        GuiQuestBook.setSelectedStack(rewards[i]);
                        List<String> str = rewards[i].getTooltip(Minecraft.getMinecraft().player, gui.mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);

                        if (selected == i) {
                            str.add("");
                            str.add(GuiColor.GREEN + Translator.translate("hqm.quest.selected"));
                        }
                        gui.drawMouseOver(str, gui.getLeft() + mX, gui.getTop() + mY);
                    }
                    break;
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private ItemStack[] getEditFriendlyRewards(ItemStack[] rewards, int max) {
        if (rewards == null) {
            return new ItemStack[]{ItemStack.EMPTY};
        } else if (canQuestsBeEdited() && rewards.length < max) {
            ItemStack[] ret = Arrays.copyOf(rewards, rewards.length + 1);
            ret[ret.length - 1] = ItemStack.EMPTY;
            return ret;
        } else {
            return rewards;
        }
    }

    @SideOnly(Side.CLIENT)
    private void handleRewardClick(GuiQuestBook gui, EntityPlayer player, ItemStack[] rawRewards, int y, boolean canSelect, int mX, int mY) {
        ItemStack[] rewards = getEditFriendlyRewards(rawRewards, canSelect ? MAX_SELECT_REWARD_SLOTS : MAX_REWARD_SLOTS);

        for (int i = 0; i < rewards.length; i++) {
            if (gui.inBounds(START_X + i * REWARD_OFFSET, y, ITEM_SIZE, ITEM_SIZE, mX, mY)) {
                if (canSelect && (!canQuestsBeEdited() || gui.getCurrentMode() == EditMode.NORMAL)) {
                    if (selectedReward == i) {
                        selectedReward = -1;
                    } else if (rewards[i] != null) {
                        selectedReward = i;
                    }
                } else if (canQuestsBeEdited() && gui.getCurrentMode() == EditMode.ITEM) {
                    gui.setEditMenu(new GuiEditMenuItem(gui, player, rewards[i], i, canSelect ? GuiEditMenuItem.Type.PICK_REWARD : GuiEditMenuItem.Type.REWARD, rewards[i] == null ? 1 : rewards[i].getCount(), ItemPrecision.PRECISE));
                } else if (canQuestsBeEdited() && gui.getCurrentMode() == EditMode.DELETE && rewards[i] != null) {
                    ItemStack[] newRewards;
                    if (rawRewards.length == 1) {
                        newRewards = null;
                        if (canSelect) {
                            selectedReward = -1;
                        }
                    } else {
                        newRewards = new ItemStack[rawRewards.length - 1];
                        int id = 0;
                        for (int j = 0; j < rawRewards.length; j++) {
                            if (j != i) {
                                newRewards[id] = rawRewards[j];
                                id++;
                            }
                        }
                        if (canSelect && selectedReward != -1) {
                            if (selectedReward == i) {
                                selectedReward = -1;
                            } else if (selectedReward > i) {
                                selectedReward--;
                            }
                        }
                    }
                    if (canSelect) {
                        this.rewardChoices.set(newRewards);
                    } else {
                        this.rewards.set(newRewards);
                    }
                    SaveHelper.add(SaveHelper.EditType.REWARD_REMOVE);
                }

                break;
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private int getTaskY(GuiQuestBook gui, int id) {
        return TASK_LABEL_START_Y + id * (TEXT_HEIGHT + TASK_MARGIN);
    }

    @SideOnly(Side.CLIENT)
    public void onClick(GuiQuestBook gui, EntityPlayer player, int mX, int mY, int b) {
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
                        if (gui.isOpBook && GuiScreen.isShiftKeyDown()) {
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
                handleRewardClick(gui, player, rewards.toArray(), REWARD_Y, false, mX, mY);
                if (!rewardChoices.isEmpty() || canQuestsBeEdited()) {
                    handleRewardClick(gui, player, rewardChoices.toArray(), REWARD_Y + REWARD_Y_OFFSET, true, mX, mY);
                }
            } else if (!rewardChoices.isEmpty()) {
                handleRewardClick(gui, player, rewardChoices.toArray(), REWARD_Y, true, mX, mY);
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

    @SideOnly(Side.CLIENT)
    public void onDrag(GuiQuestBook gui, EntityPlayer player, int mX, int mY, int b) {
        for (ScrollBar scrollBar : scrollBars) {
            scrollBar.onDrag(gui, mX, mY);
        }
    }

    @SideOnly(Side.CLIENT)
    public void onRelease(GuiQuestBook gui, EntityPlayer player, int mX, int mY, int b) {
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
                Object obj = constructor.newInstance(tasks.get(i));
                data.tasks[i] = (QuestDataTask) obj;
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

    public void sendUpdatedDataToTeam(EntityPlayer player) {
        sendUpdatedDataToTeam(QuestingData.getQuestingData(player).getTeam());
    }

    public void sendUpdatedDataToTeam(UUID playerId) {
        sendUpdatedDataToTeam(QuestingData.getQuestingData(playerId).getTeam());
    }

    public void sendUpdatedDataToTeam(Team team) {
        for (PlayerEntry entry : team.getPlayers()) {
            sendUpdatedData(entry.getPlayerMP());
        }
    }

    private void sendUpdatedData(EntityPlayerMP player) {
        if (player == null) return; // Don't send to nobody you silly goose
        IMessage update = new QuestDataUpdateMessage(
                getQuestId(),
                QuestingData.getQuestingData(player).getTeam().getPlayerCount(),
                QuestingData.getQuestingData(player).getQuestData(getQuestId())
        );
        NetworkManager.sendToPlayer(update, player);
    }

    public void claimReward(EntityPlayer player, int selectedReward) {
        if (hasReward(player)) {
            boolean sentInfo = false;
            if (getQuestData(player).getReward(player) && (!rewards.isEmpty() || !rewardChoices.isEmpty())) {
                List<ItemStack> items = new ArrayList<>();
                if (!rewards.isEmpty()) {
                    for (ItemStack stack : rewards.toArray()) {
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
                        if (stack.isItemEqual(stack1) && ItemStack.areItemStackTagsEqual(stack, stack1)) {
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
                for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
                    for (ItemStack stack1 : itemsToCheck) {
                        if (stack1.getCount() > 0) {
                            ItemStack stack = player.inventory.mainInventory.get(i);
                            if (stack == ItemStack.EMPTY) {
                                stack1.shrink(stack1.getMaxStackSize());
                                break;
                            } else if (stack.isItemEqual(stack1) && ItemStack.areItemStackTagsEqual(stack1, stack)) {
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
                    player.inventory.markDirty();
                    QuestData data = getQuestData(player);
                    Team team = QuestingData.getQuestingData(player).getTeam();
                    if (!team.isSingle() && team.getRewardSetting() == RewardSetting.ANY) {
                        for (int i = 0; i < data.reward.length; i++) {
                            data.reward[i] = false;
                        }
                        sendUpdatedDataToTeam(player);
                    } else {
                        data.claimReward(player);
                        if (player instanceof EntityPlayerMP)
                            sendUpdatedData((EntityPlayerMP) player);
                    }
                    sentInfo = true;
                } else {
                    return;
                }
            }


            if (reputationRewards != null && getQuestData(player).canClaim()) {
                getQuestData(player).claimed = true;
                QuestingData.getQuestingData(player).getTeam().receiveAndSyncReputation(this, reputationRewards);
                EventTrigger.instance().onEvent(new EventTrigger.ReputationEvent(player));
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

    @SuppressWarnings("rawtypes")
    public void setItem(GuiEditMenuItem.Element element, int id, GuiEditMenuItem.Type type, ItemPrecision precision, EntityPlayer player) {
        if (type == GuiEditMenuItem.Type.REWARD || type == GuiEditMenuItem.Type.PICK_REWARD) {
            if (element instanceof GuiEditMenuItem.ElementItem) {
                ItemStack stack = ((GuiEditMenuItem.ElementItem) element).getStack();
                if (!stack.isEmpty()) {
                    stack.setCount(Math.min(127, element.getAmount()));
                    setReward(stack, id, type == GuiEditMenuItem.Type.REWARD);
                }
            }
        } else if (selectedTask instanceof QuestTaskItems) {
            ((QuestTaskItems) selectedTask).setItem(element, id, precision);
        } else if (selectedTask instanceof QuestTaskLocation && type == GuiEditMenuItem.Type.LOCATION) {
            ((QuestTaskLocation) selectedTask).setIcon(id, (ItemStack) element.getStack(), player);
        } else if (selectedTask instanceof QuestTaskTame && type == GuiEditMenuItem.Type.TAME) {
            ((QuestTaskTame) selectedTask).setIcon(id, (ItemStack) element.getStack(), player);
        } else if (selectedTask instanceof QuestTaskAdvancement && type == GuiEditMenuItem.Type.ADVANCEMENT) {
            ((QuestTaskAdvancement) selectedTask).setIcon(id, (ItemStack) element.getStack(), player);
        } else if (selectedTask instanceof QuestTaskMob && type == GuiEditMenuItem.Type.MOB) {
            ((QuestTaskMob) selectedTask).setIcon(id, (ItemStack) element.getStack(), player);
        }
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

    @SideOnly(Side.CLIENT)
    public void onScroll(GuiQuestBook gui, int x, int y, int scroll) {
        for (ScrollBar scrollBar : scrollBars) {
            scrollBar.onScroll(gui, x, y, scroll);
        }
    }

    @SideOnly(Side.CLIENT)
    public void onOpen(GuiQuestBook gui, EntityPlayer player) {
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

        QuestingData.getQuestingData(player).selectedQuestId = getQuestId();
        QuestingData.getQuestingData(player).selectedTask = selectedTask == null ? -1 : selectedTask.getId();
        if (selectedTask != null){
            //NetworkManager.sendToServer(ClientChange.SELECT_QUEST.build(selectedTask));
            GeneralUsage.sendBookSelectTaskUpdate(Quest.this.selectedTask);
        }

        EventTrigger.instance().onEvent(new EventTrigger.QuestSelectedEvent(player, this.getQuestId()));
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

    public void completeQuest(EntityPlayer player) {
        for (QuestTask task : tasks) {
            task.autoComplete(player.getPersistentID());
            task.getData(player).completed = true;
        }
        QuestTask.completeQuest(this, player.getPersistentID());
    }

    public void reset(UUID playerId) {
        reset(getQuestData(playerId));
    }

    public void reset(QuestData data) {
        data.available = true;
        addTaskData(data);
    }

    public void resetAll() {
        for (Team team : QuestingData.getAllTeams()) {
            QuestData data = team.getQuestData(getQuestId());
            if (data != null && !data.available) {
                reset(data);
                sendUpdatedDataToTeam(team);
            }
        }
    }

    public void resetOnTime(int time) {
        for (Team team : QuestingData.getAllTeams()) {
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
        return QuestLine.getActiveQuestLine().quests.values().stream().filter(quest -> optionLinks.contains(quest.getQuestId())).collect(Collectors.toList());
    }

    public List<Quest> getReversedOptionLinks() {
        return QuestLine.getActiveQuestLine().quests.values().stream().filter(quest -> reversedOptionLinks.contains(quest.getQuestId())).collect(Collectors.toList());
    }

    public boolean getUseModifiedParentRequirement() {
        return (parentRequirementCount != -1) && this.parentRequirementCount < this.getRequirements().size();
    }

    public int getParentRequirementCount() {
        return (parentRequirementCount == -1) ? this.getRequirements().size() : this.parentRequirementCount;
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
        return QuestLine.getActiveQuestLine().quests.values().stream().filter(quest -> reversedRequirement.contains(quest.getQuestId())).collect(Collectors.toList());
    }

    public static boolean canQuestsBeEdited(){
        if(isEditing && !HQMUtil.isGameSingleplayer()){
            setEditMode(false);
        }
        return isEditing;
    }
    
    public static void setEditMode(boolean enabled){
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
                QuestTask task = (QuestTask) ex.newInstance(quest, getName(), getDescription());
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
            return Translator.translate(getLangKeyDescription());
        }

        public String getName() {
            return Translator.translate(getLangKeyName());
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
