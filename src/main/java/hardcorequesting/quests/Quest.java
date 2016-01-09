package hardcorequesting.quests;

import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import hardcorequesting.*;
import hardcorequesting.bag.Group;
import hardcorequesting.bag.GroupTier;
import hardcorequesting.client.EditMode;
import hardcorequesting.client.interfaces.*;
import hardcorequesting.client.sounds.SoundHandler;
import hardcorequesting.client.sounds.Sounds;
import hardcorequesting.items.ModItems;
import hardcorequesting.network.*;
import hardcorequesting.reputation.Reputation;
import hardcorequesting.reputation.ReputationBar;
import hardcorequesting.reward.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.List;

public class Quest {

    private static Map<Short, Quest> quests;

    public static boolean isEditing = false;
    public static int selectedQuestId;

    public static QuestTicker clientTicker;
    public static QuestTicker serverTicker;


    public static Collection<Quest> getQuests() {
        return QuestLine.getActiveQuestLine().quests.values();
    }

    public static List<QuestSet> getQuestSets() {
        return QuestLine.getActiveQuestLine().questSets;
    }

    public static int size() {
        return QuestLine.getActiveQuestLine().questCount;
    }


    //static {
    //    quests = new HashMap<Short, Quest>();
    //    new Quest(0, "The burnt village","Testing 1.2.3", 50, 50, false);
    //}


    private short id;
    private String name;
    private String description;
    private List<Quest> requirement;
    private List<Integer> requirementIds;
    private List<Quest> reversedRequirement;
    private List<Quest> optionLinks;
    private List<Integer> optionLinkIds;
    private List<Quest> reversedOptionLinks;
    private List<QuestTask> tasks;
    private List<String> cachedDescription;
    private List<ReputationReward> reputationRewards;
    int nextTaskId;
    private QuestTask selectedTask;
    private ItemStackRewardList rewards;
    private ItemStackRewardList rewardChoices;
    private CommandRewardList commandRewardList;
    private RepeatInfo repeatInfo = new RepeatInfo(RepeatType.NONE, 0, 0);
    private TriggerType triggerType = TriggerType.NONE;
    private int triggerTasks = 1;
    private boolean useModifiedParentRequirement;
    private int parentRequirementCount;
    private int x;
    private int y;
    private boolean isBig;
    private ItemStack icon;
    private QuestSet set;
    private int selectedReward = -1;
    private final List<LargeButton> buttons = new ArrayList<LargeButton>();

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
                DataWriter dw = PacketHandler.getWriter(PacketId.CLAIM_REWARD);
                dw.writeData(getId(), DataBitHelper.QUESTS);
                if (!rewardChoices.isEmpty()) {
                    dw.writeData(selectedReward, DataBitHelper.REWARDS);
                }
                PacketHandler.sendToServer(dw);
            }
        });

        buttons.add(new LargeButton("hqm.quest.manualSubmit", 185, 200) {
            @Override
            public boolean isEnabled(GuiBase gui, EntityPlayer player) {
                return ((QuestTaskItemsConsume) selectedTask).allowManual();
            }

            @Override
            public boolean isVisible(GuiBase gui, EntityPlayer player) {
                return selectedTask != null && selectedTask instanceof QuestTaskItemsConsume && !selectedTask.isCompleted(player);
            }

            @Override
            public void onClick(GuiBase gui, EntityPlayer player) {
                //if(!QuestingData.isDebugActive())
                //{
                PacketHandler.sendToServer(selectedTask.getWriterForTask(PacketId.TASK_REQUEST));
                //}
            }
        });

        buttons.add(new LargeButton("hqm.quest.manualDetect", 185, 200) {
            @Override
            public boolean isEnabled(GuiBase gui, EntityPlayer player) {
                return true;
            }

            @Override
            public boolean isVisible(GuiBase gui, EntityPlayer player) {
                return selectedTask != null && selectedTask instanceof QuestTaskItemsDetect && !selectedTask.isCompleted(player);
            }

            @Override
            public void onClick(GuiBase gui, EntityPlayer player) {
                PacketHandler.sendToServer(selectedTask.getWriterForTask(PacketId.TASK_REQUEST));
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
                return selectedTask != null && selectedTask instanceof QuestTaskDeath && Quest.isEditing;
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
                return selectedTask != null && selectedTask instanceof QuestTaskReputationKill && Quest.isEditing;
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
                return QuestingData.getQuestingData(player).selectedQuest != getId() || QuestingData.getQuestingData(player).selectedTask != selectedTask.getId();
            }

            @Override
            public boolean isVisible(GuiBase gui, EntityPlayer player) {
                return selectedTask != null && selectedTask instanceof QuestTaskItemsConsume && !selectedTask.isCompleted(player);
            }

            @Override
            public void onClick(GuiBase gui, EntityPlayer player) {
                //update locally too, then we don't have to refresh all the data(i.e. the server won't notify us about the change we already know about)
                QuestingData.getQuestingData(player).selectedQuest = getId();
                QuestingData.getQuestingData(player).selectedTask = selectedTask.getId();

                PacketHandler.sendToServer(selectedTask.getWriterForTask(PacketId.SELECT_TASK));
            }
        });

        int itemIds = 0;
        for (final TaskType taskType : TaskType.values()) {
            buttons.add(new LargeButton(taskType.getLangKeyName(), taskType.getLangKeyDescription(), 185 + (taskType.ordinal() % 2) * 65, 50 + (taskType.ordinal() / 2) * 35) {
                @Override
                public boolean isEnabled(GuiBase gui, EntityPlayer player) {
                    return tasks.size() < DataBitHelper.TASKS.getMaximum();
                }

                @Override
                public boolean isVisible(GuiBase gui, EntityPlayer player) {
                    return isEditing && selectedTask == null && ((GuiQuestBook) gui).getCurrentMode() == EditMode.TASK;
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
                        return isEditing && selectedTask != null && ((GuiQuestBook) gui).getCurrentMode() == EditMode.CHANGE_TASK;
                    }

                    @Override
                    public void onClick(GuiBase gui, EntityPlayer player) {
                        TaskType oldTaskType = TaskType.getType(selectedTask.getClass());
                        if (oldTaskType == null) return;

                        nextTaskId--;
                        Class<? extends QuestTask> clazz = taskType.clazz;
                        try {
                            Constructor<? extends QuestTask> constructor = clazz.getConstructor(Quest.class, String.class, String.class);
                            QuestTask task = constructor.newInstance(self, taskType.getLangKeyName(), taskType.getLangKeyDescription());

                            for (QuestTask questTask : selectedTask.getRequirements()) {
                                task.addRequirement(questTask);
                            }
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

    private Quest self = this;
    private final ScrollBar descriptionScroll;
    private final ScrollBar taskDescriptionScroll;
    private final ScrollBar taskScroll;
    private final List<ScrollBar> scrollBars = new ArrayList<ScrollBar>();
    private static final int VISIBLE_DESCRIPTION_LINES = 7;
    private static final int VISIBLE_TASKS = 3;

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


    public Quest(int id, String name, String description, int x, int y, boolean isBig) {
        this.id = (short) id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.isBig = isBig;
        this.description = description;

        requirement = new ArrayList<Quest>();
        reversedRequirement = new ArrayList<Quest>();
        optionLinks = new ArrayList<Quest>();
        reversedOptionLinks = new ArrayList<Quest>();
        tasks = new ArrayList<QuestTask>();
        rewards = new ItemStackRewardList();
        rewardChoices = new ItemStackRewardList();
        commandRewardList = new CommandRewardList();

        QuestLine.getActiveQuestLine().quests.put(this.id, this);

        if (this.id >= QuestLine.getActiveQuestLine().questCount) {
            QuestLine.getActiveQuestLine().questCount = this.id + 1;
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

    public String[] getCommandRewardsAsStrings()
    {
        return this.commandRewardList.asStrings();
    }

    public void setCommandRewards(String[] commands)
    {
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

    public void addRequirement(int id) {
        if (lookForId(id, false) || lookForId(id, true)) {
            return;
        }

        Quest quest = QuestLine.getActiveQuestLine().quests.get((short) id);
        if (quest != null) {
            requirement.add(quest);
            quest.reversedRequirement.add(this);
            SaveHelper.add(SaveHelper.EditType.REQUIREMENT_CHANGE);
        }
    }

    private boolean lookForId(int id, boolean reversed) {
        List<Quest> quests = reversed ? reversedRequirement : requirement;
        for (Quest quest : quests) {
            if (quest.id == id || quest.lookForId(id, reversed)) {
                return true;
            }
        }

        return false;
    }

    public void clearRequirements() {
        SaveHelper.add(SaveHelper.EditType.REQUIREMENT_REMOVE, requirement.size());
        for (Quest quest : requirement) {
            quest.reversedRequirement.remove(this);
        }
        requirement.clear();
    }

    public void addOptionLink(int id) {
        for (Quest quest : optionLinks) {
            if (quest.id == id) {
                return;
            }
        }
        for (Quest quest : reversedOptionLinks) {
            if (quest.id == id) {
                return;
            }
        }

        Quest quest = QuestLine.getActiveQuestLine().quests.get((short) id);
        if (quest != null) {
            SaveHelper.add(SaveHelper.EditType.OPTION_CHANGE);
            optionLinks.add(quest);
            quest.reversedOptionLinks.add(this);
        }
    }

    public void clearOptionLinks() {
        SaveHelper.add(SaveHelper.EditType.OPTION_REMOVE, optionLinks.size());
        for (Quest quest : reversedOptionLinks) {
            quest.optionLinks.remove(this);
        }

        for (Quest quest : optionLinks) {
            quest.reversedOptionLinks.remove(this);
        }
        reversedRequirement.clear();
        optionLinks.clear();
    }

    public QuestData getQuestData(EntityPlayer player) {
        return QuestingData.getQuestingData(player).getQuestData(id);
    }

    public QuestData getQuestData(String playerName) {
        return QuestingData.getQuestingData(playerName).getQuestData(id);
    }

    public short getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isVisible(EntityPlayer player) {
        return isVisible(QuestingData.getUserName(player));
    }

    boolean isVisible(EntityPlayer player, Map<Quest, Boolean> isVisibleCache, Map<Quest, Boolean> isLinkFreeCache) {
        return isVisible(QuestingData.getUserName(player), isVisibleCache, isLinkFreeCache);
    }

    public boolean isVisible(String playerName) {
        return isVisible(playerName, new HashMap<Quest, Boolean>(), new HashMap<Quest, Boolean>());
    }

    boolean isVisible(String playerName, Map<Quest, Boolean> isVisibleCache, Map<Quest, Boolean> isLinkFreeCache) {
        Boolean cachedResult = isVisibleCache.get(this);
        if (cachedResult != null) {
            return cachedResult.booleanValue();
        }

        boolean result = triggerType.isQuestVisible(this, playerName) && isLinkFree(playerName, isLinkFreeCache) && visibleParentEvaluator.isValid(playerName, isVisibleCache, isLinkFreeCache);

        isVisibleCache.put(this, result);

        return result;
    }

    public boolean isEnabled(EntityPlayer player) {
        return isEnabled(QuestingData.getUserName(player));
    }

    boolean isEnabled(EntityPlayer player, Map<Quest, Boolean> isVisibleCache, Map<Quest, Boolean> isLinkFreeCache) {
        return isEnabled(QuestingData.getUserName(player), true, isVisibleCache, isLinkFreeCache);
    }

    public boolean isEnabled(String playerName) {
        return isEnabled(playerName, true);
    }

    public boolean isEnabled(String playerName, boolean requiresVisible) {
        return isEnabled(playerName, requiresVisible, new HashMap<Quest, Boolean>(), new HashMap<Quest, Boolean>());
    }

    boolean isEnabled(String playerName, boolean requiresVisible, Map<Quest, Boolean> isVisibleCache, Map<Quest, Boolean> isLinkFreeCache) {
        return !(set == null || !isLinkFree(playerName, isLinkFreeCache) || (requiresVisible && !triggerType.doesWorkAsInvisible() && !isVisible(playerName, isVisibleCache, isLinkFreeCache))) && enabledParentEvaluator.isValid(playerName, isVisibleCache, isLinkFreeCache);
    }

    public boolean isLinkFree(EntityPlayer player) {
        return isLinkFree(QuestingData.getUserName(player), new HashMap<Quest, Boolean>());
    }

    boolean isLinkFree(EntityPlayer player, Map<Quest, Boolean> cache) {
        return isLinkFree(QuestingData.getUserName(player), cache);
    }

    public boolean isLinkFree(String playerName) {
        return isLinkFree(playerName, new HashMap<Quest, Boolean>());
    }

    boolean isLinkFree(String playerName, Map<Quest, Boolean> cache) {
        Boolean cachedResult = cache.get(this);
        if (cachedResult != null) {
            return cachedResult.booleanValue();
        }

        boolean result = true;
        for (Quest optionLink : optionLinks) {
            if (optionLink.isCompleted(playerName)) {
                result = false;
                break;
            }
        }

        if (result) {
            for (Quest optionLink : reversedOptionLinks) {
                if (optionLink.isCompleted(playerName)) {
                    result = false;
                    break;
                }
            }
        }

        if (result) {
            result = linkParentEvaluator.isValid(playerName, null, cache);
        }

        cache.put(this, result);

        return result;
    }

    private ParentEvaluator enabledParentEvaluator = new ParentEvaluator() {
        @Override
        protected boolean isValid(String playerName, Quest parent, Map<Quest, Boolean> isVisibleCache, Map<Quest, Boolean> isLinkFreeCache) {
            return parent.isCompleted(playerName);
        }
    };

    private ParentEvaluator linkParentEvaluator = new ParentEvaluator() {
        @Override
        protected boolean isValid(String playerName, Quest parent, Map<Quest, Boolean> isVisibleCache, Map<Quest, Boolean> isLinkFreeCache) {
            return parent.isLinkFree(playerName, isLinkFreeCache);
        }
    };

    private ParentEvaluator visibleParentEvaluator = new ParentEvaluator() {
        @Override
        protected boolean isValid(String playerName, Quest parent, Map<Quest, Boolean> isVisibleCache, Map<Quest, Boolean> isLinkFreeCache) {
            return parent.isVisible(playerName, isVisibleCache, isLinkFreeCache) || parent.isCompleted(playerName);
        }
    };

    public void setReputationRewards(List<ReputationReward> reputationRewards) {
        this.reputationRewards = reputationRewards;
    }

    public List<ReputationReward> getReputationRewards() {
        return reputationRewards;
    }

    private abstract class ParentEvaluator {
        protected abstract boolean isValid(String playerName, Quest parent, Map<Quest, Boolean> isVisibleCache, Map<Quest, Boolean> isLinkFreeCache);

        private boolean isValid(String playerName, Map<Quest, Boolean> isVisibleCache, Map<Quest, Boolean> isLinkFreeCache) {
            int parents = getRequirement().size();
            int requiredAmount = useModifiedParentRequirement ? parentRequirementCount : parents;
            if (requiredAmount > parents) {
                return false;
            }

            int allowedUncompleted = parents - requiredAmount;
            int uncompleted = 0;
            for (Quest quest : getRequirement()) {
                if (!isValid(playerName, quest, isVisibleCache, isLinkFreeCache)) {
                    uncompleted++;
                    if (uncompleted > allowedUncompleted) {
                        return false;
                    }
                }
            }

            return true;
        }

    }

    public boolean isAvailable(EntityPlayer player) {
        return isAvailable(QuestingData.getUserName(player));
    }

    public boolean isCompleted(EntityPlayer player) {
        return isCompleted(QuestingData.getUserName(player));
    }

    public boolean isAvailable(String playerName) {
        QuestData data = getQuestData(playerName);
        return data != null && data.available;
    }

    public boolean isCompleted(String playerName) {
        QuestData data = getQuestData(playerName);
        return data != null && data.completed;
    }

    public List<Quest> getRequirement() {
        return requirement;
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

    public int getGuiCenterY() {
        return getGuiY() + getGuiH() / 2;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public boolean useBigIcon() {
        return isBig;
    }

    public void setTriggerType(TriggerType triggerType) {
        this.triggerType = triggerType;
    }

    public void setTriggerTasks(int triggerTasks) {
        this.triggerTasks = triggerTasks;
    }

    public TriggerType getTriggerType() {
        return triggerType;
    }

    public int getTriggerTasks() {
        return triggerTasks;
    }

    @SideOnly(Side.CLIENT)
    public int getColorFilter(EntityPlayer player, int tick) {
        if (isEditing && !isVisible(player)) {
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


    public static Quest getQuest(int id) {
        return QuestLine.getActiveQuestLine().quests.get((short) id);
    }

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
    //endregion

    @SideOnly(Side.CLIENT)
    private List<String> getCachedDescription(GuiBase gui) {
        if (cachedDescription == null) {
            cachedDescription = gui.getLinesFromText(description, 0.7F, 130);
        }
        return cachedDescription;
    }

    @SideOnly(Side.CLIENT)
    public void drawMenu(GuiQuestBook gui, EntityPlayer player, int mX, int mY) {
        if (!isEditing && selectedTask != null && !selectedTask.isVisible(player)) {
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
            if (isVisible || Quest.isEditing) {
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
        if (!rewards.isEmpty() || isEditing) {
            gui.drawString(Translator.translate("hqm.quest.rewards"), START_X, REWARD_STR_Y, 0x404040);
            drawRewards(gui, rewards.toArray(), REWARD_Y, -1, mX, mY, MAX_SELECT_REWARD_SLOTS);
            if (!rewardChoices.isEmpty() || isEditing) {
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
        GL11.glColor3f(1F, 1F, 1F);
        ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
        for (ScrollBar scrollBar : scrollBars) {
            scrollBar.draw(gui);
        }


        boolean claimed = getQuestData(player).claimed;
        int y = rewards == null || rewards.size() <= MAX_REWARD_SLOTS - (isEditing ? 2 : 1) ? REPUTATION_Y_LOWER : REPUTATION_Y;
        boolean hover = gui.inBounds(REPUTATION_X, y, REPUTATION_SIZE, REPUTATION_SIZE, mX, mY);


        if (reputationRewards != null || isEditing) {
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
            if (isEditing && gui.getCurrentMode() == EditMode.CHANGE_TASK) {
                if (selectedTask instanceof QuestTaskItems) {
                    gui.drawString(gui.getLinesFromText(Translator.translate("hqm.quest.itemTaskChangeTo"), 0.7F, 130), 180, 20, 0.7F, 0x404040);
                } else {
                    gui.drawString(gui.getLinesFromText(Translator.translate("hqm.quest.itemTaskTypeOnly"), 0.7F, 130), 180, 20, 0.7F, 0x404040);
                }
            } else {
                List<String> description = selectedTask.getCachedLongDescription(gui);
                int taskStartLine = taskDescriptionScroll.isVisible(gui) ? Math.round((description.size() - VISIBLE_DESCRIPTION_LINES) * taskDescriptionScroll.getScroll()) : 0;
                gui.drawString(description, taskStartLine, VISIBLE_DESCRIPTION_LINES, TASK_DESCRIPTION_X, TASK_DESCRIPTION_Y, 0.7F, 0x404040);

                selectedTask.draw(gui, player, mX, mY);
            }
        } else if (isEditing && gui.getCurrentMode() == EditMode.TASK) {
            gui.drawString(gui.getLinesFromText(Translator.translate("hqm.quest.createTasks"), 0.7F, 130), 180, 20, 0.7F, 0x404040);
        } else if (isEditing && gui.getCurrentMode() == EditMode.CHANGE_TASK) {
            gui.drawString(gui.getLinesFromText(Translator.translate("hqm.quest.itemTaskTypeChange"), 0.7F, 130), 180, 20, 0.7F, 0x404040);
        }

        if (!rewards.isEmpty() || isEditing) {
            drawRewardMouseOver(gui, rewards.toArray(), REWARD_Y, -1, mX, mY);
            if (!rewardChoices.isEmpty() || isEditing) {
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
        if (isEditing) {
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
            gui.drawItem(rewards[i], START_X + i * REWARD_OFFSET, y, mX, mY, selected == i);
        }
    }

    @SuppressWarnings("unchecked")
    @SideOnly(Side.CLIENT)
    private void drawRewardMouseOver(GuiQuestBook gui, ItemStack[] rewards, int y, int selected, int mX, int mY) {
        if (rewards != null) {
            for (int i = 0; i < rewards.length; i++) {
                if (gui.inBounds(START_X + i * REWARD_OFFSET, y, ITEM_SIZE, ITEM_SIZE, mX, mY)) {
                    if (rewards[i] != null) {
                        GuiQuestBook.setSelected(rewards[i]);
                        List<String> str = new ArrayList<String>();
                        try {
                            if (isEditing && !GuiQuestBook.isCtrlKeyDown()) {
                                str = rewards[i].getTooltip(Minecraft.getMinecraft().thePlayer, Minecraft.getMinecraft().gameSettings.advancedItemTooltips);
                                str.add("");
                                str.add(GuiColor.GRAY + Translator.translate("hqm.quest.crtlNonEditor"));
                            } else {
                                str.add(rewards[i].getDisplayName());
                            }
                        } catch (Throwable ignored) {
                            break;
                        }

                        if (selected == i) {
                            str.add(GuiColor.GREEN + Translator.translate("hqm.quest.selected"));
                        }
                        gui.drawMouseOver(str, gui.getLeft() + mX, gui.getTop() + mY);
                    }
                    break;
                }
            }
        }
    }

    private ItemStack[] getEditFriendlyRewards(ItemStack[] rewards, int max) {
        if (rewards == null) {
            return new ItemStack[1];
        } else if (isEditing && rewards.length < max) {
            return Arrays.copyOf(rewards, rewards.length + 1);
        } else {
            return rewards;
        }
    }

    private static final int MAX_REWARD_SLOTS = 7;
    private static final int MAX_SELECT_REWARD_SLOTS = 4;

    @SideOnly(Side.CLIENT)
    private void handleRewardClick(GuiQuestBook gui, EntityPlayer player, ItemStack[] rawRewards, int y, boolean canSelect, int mX, int mY) {
        ItemStack[] rewards = getEditFriendlyRewards(rawRewards, canSelect ? MAX_SELECT_REWARD_SLOTS : MAX_REWARD_SLOTS);

        for (int i = 0; i < rewards.length; i++) {
            if (gui.inBounds(START_X + i * REWARD_OFFSET, y, ITEM_SIZE, ITEM_SIZE, mX, mY)) {
                if (canSelect && (!isEditing || gui.getCurrentMode() == EditMode.NORMAL)) {
                    if (selectedReward == i) {
                        selectedReward = -1;
                    } else if (rewards[i] != null) {
                        selectedReward = i;
                    }
                } else if (isEditing && gui.getCurrentMode() == EditMode.ITEM) {
                    gui.setEditMenu(new GuiEditMenuItem(gui, player, rewards[i], i, canSelect ? GuiEditMenuItem.Type.PICK_REWARD : GuiEditMenuItem.Type.REWARD, rewards[i] == null ? 1 : rewards[i].stackSize, ItemPrecision.PRECISE));
                } else if (isEditing && gui.getCurrentMode() == EditMode.DELETE && rewards[i] != null) {
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
                if (task.isVisible(player) || isEditing) {

                    if (gui.inBounds(START_X, getTaskY(gui, id), gui.getStringWidth(task.getDescription()), TEXT_HEIGHT, mX, mY)) {
                        if (isEditing && gui.getCurrentMode() != EditMode.NORMAL) {
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

            if (!rewards.isEmpty() || isEditing) {
                handleRewardClick(gui, player, rewards.toArray(), REWARD_Y, false, mX, mY);
                if (!rewardChoices.isEmpty() || isEditing) {
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

            if (isEditing && selectedTask != null && gui.getCurrentMode() == EditMode.TASK) {
                selectedTask = null;
            }

            if (isEditing && gui.getCurrentMode() == EditMode.REPUTATION_REWARD) {
                int y = rewards == null || rewards.size() <= MAX_REWARD_SLOTS - (isEditing ? 2 : 1) ? REPUTATION_Y_LOWER : REPUTATION_Y;
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
                Constructor constructor = tasks.get(i).getDataType().getConstructor(QuestTask.class);
                Object obj = constructor.newInstance(tasks.get(i));
                data.tasks[i] = (QuestDataTask) obj;
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }

        return true;
    }


    public void save(DataWriter dw, QuestData questData, boolean light) {
        dw.writeBoolean(questData.completed);
        dw.writeBoolean(questData.claimed);
        dw.writeBoolean(questData.available);
        dw.writeData(questData.time, DataBitHelper.HOURS);
        if (questData.completed) {
            for (boolean b : questData.reward) {
                dw.writeBoolean(b);
            }
        }

        if (light) {
            for (int i = 0; i < tasks.size(); i++) {
                QuestTask task = tasks.get(i);
                questData.tasks[i] = task.validateData(questData.tasks[i]);
                task.write(dw, questData.tasks[i], true);
            }
        } else {
            dw.writeData(tasks.size(), DataBitHelper.TASKS);
            for (int i = 0; i < tasks.size(); i++) {
                dw.writeData(TaskType.getType(tasks.get(i).getClass()).ordinal(), DataBitHelper.TASK_TYPE);
                QuestTask task = tasks.get(i);
                questData.tasks[i] = task.validateData(questData.tasks[i]);
                task.write(dw, questData.tasks[i], false);
            }
        }
    }


    public void preRead(int players, QuestData data) {
        data.reward = new boolean[players];
    }

    public void read(DataReader dr, QuestData questData, FileVersion version, boolean light) {
        questData.completed = dr.readBoolean();
        questData.claimed = version.contains(FileVersion.REPUTATION) && dr.readBoolean();
        if (version.contains(FileVersion.REPEATABLE_QUESTS)) {
            questData.available = dr.readBoolean();
            questData.time = dr.readData(DataBitHelper.HOURS);
            if (questData.completed && repeatInfo.getType() == RepeatType.NONE) {
                questData.available = false;
            }
        } else if (repeatInfo.getType() != RepeatType.INSTANT) {
            questData.available = !questData.completed;
        }
        if (questData.completed) {
            for (int i = 0; i < questData.reward.length; i++) {
                if (version.contains(FileVersion.REPEATABLE_QUESTS)) {
                    questData.reward[i] = dr.readBoolean();
                } else {
                    questData.reward[i] = !dr.readBoolean() && questData.completed;
                }
            }
        }


        if (light) {
            for (int i = 0; i < tasks.size(); i++) {
                QuestTask task = tasks.get(i);
                questData.tasks[i] = task.validateData(questData.tasks[i]);
                task.read(dr, questData.tasks[i], version, true);
            }
        } else {
            int count = dr.readData(DataBitHelper.TASKS);
            for (int i = 0; i < count; i++) {
                int type = dr.readData(DataBitHelper.TASK_TYPE);
                if (i >= tasks.size()) {
                    try {
                        Class<? extends QuestTask> clazz = TaskType.values()[type].clazz;
                        Constructor<? extends QuestTask> constructor = clazz.getConstructor(Quest.class, String.class, String.class);
                        Object obj = constructor.newInstance(this, "Fake", "Fake");
                        QuestTask task = (QuestTask) obj;
                        nextTaskId--; //we created a fake task, it shouldn't occupy an id. It doesn't really matter since we're not editing, but what ever.

                        Constructor<? extends QuestDataTask> constructor2 = task.getDataType().getConstructor(new Class[]{QuestTask.class});
                        Object obj2 = constructor2.newInstance(task);

                        task.read(dr, (QuestDataTask) obj2, version, false); //read data to clear the data reader from it
                        task.onDelete();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    QuestTask task = tasks.get(i);
                    questData.tasks[i] = task.validateData(questData.tasks[i]);
                    task.read(dr, questData.tasks[i], version, false);
                }
            }
        }
    }

    public void postRead(QuestingData questingData, QuestData questData, FileVersion version) {
        String name = questingData.getName();
        if (version.lacks(FileVersion.UNCOMPLETED_DISABLED)) {
            if (isCompleted(name) && !isEnabled(name)) {
                questData.completed = false;
            }
        } else {
            boolean update = false;
            for (QuestTask task : tasks) {
                if (task.isCompleted(name)) {
                    task.autoComplete(name);
                } else if (task.getCompletedRatio(name) >= 1F) {
                    task.autoComplete(name);
                    task.completeTask(name);
                    update = true;
                }
            }

            if (update) {
                QuestTask.completeQuest(this, name);
            }
        }
    }


    public List<QuestTask> getTasks() {
        return tasks;
    }


    public void sendUpdatedDataToTeam(EntityPlayer player) {
        sendUpdatedDataToTeam(QuestingData.getQuestingData(player).getTeam());
    }

    public void sendUpdatedDataToTeam(String playerName) {
        sendUpdatedDataToTeam(QuestingData.getQuestingData(playerName).getTeam());
    }

    public void sendUpdatedDataToTeam(Team team) {
        for (Team.PlayerEntry entry : team.getPlayers()) {
            if (entry.shouldRefreshData()) {
                sendUpdatedData(entry.getName());
            }
        }
    }

    private void sendUpdatedData(String playerName) {
        DataWriter dw = PacketHandler.getWriter(PacketId.QUEST_DATA);
        dw.writeData(id, DataBitHelper.QUESTS);
        dw.writeData(QuestingData.getQuestingData(playerName).getTeam().getPlayerCount(), DataBitHelper.PLAYERS);
        save(dw, getQuestData(playerName), true);
        PacketHandler.sendToPlayer(playerName, dw);
    }

    public void claimReward(EntityPlayer player, DataReader dr) {
        if (hasReward(player)) {
            boolean sentInfo = false;
            if (getQuestData(player).getReward(player) && (!rewards.isEmpty() || !rewardChoices.isEmpty())) {
                List<ItemStack> items = new ArrayList<ItemStack>();
                if (!rewards.isEmpty()) {
                    for (ItemStack itemStack : rewards.toArray()) {
                        items.add(itemStack.copy());
                    }
                }
                if (!rewardChoices.isEmpty()) {
                    int id = dr.readData(DataBitHelper.REWARDS);
                    if (id >= 0 && id < rewardChoices.size()) {
                        items.add(rewardChoices.getReward(id).copy());
                    } else {
                        return;
                    }
                }

                List<ItemStack> itemsToAdd = new ArrayList<ItemStack>();
                for (ItemStack item : items) {
                    boolean added = false;
                    for (ItemStack itemStack : itemsToAdd) {
                        if (item.isItemEqual(itemStack) && ItemStack.areItemStackTagsEqual(item, itemStack)) {
                            itemStack.stackSize += item.stackSize;
                            added = true;
                            break;
                        }
                    }

                    if (!added) {
                        itemsToAdd.add(item.copy());
                    }
                }

                List<ItemStack> itemsToCheck = new ArrayList<ItemStack>();
                for (ItemStack itemStack : itemsToAdd) {
                    itemsToCheck.add(itemStack.copy());
                }
                for (int i = 0; i < player.inventory.mainInventory.length; i++) {
                    for (ItemStack itemStack : itemsToCheck) {
                        if (itemStack.stackSize > 0) {
                            if (player.inventory.mainInventory[i] == null) {
                                itemStack.stackSize -= itemStack.getMaxStackSize();
                                break;
                            } else if (player.inventory.mainInventory[i].isItemEqual(itemStack) && ItemStack.areItemStackTagsEqual(itemStack, player.inventory.mainInventory[i])) {
                                itemStack.stackSize -= itemStack.getMaxStackSize() - player.inventory.mainInventory[i].stackSize;
                                break;
                            }
                        }
                    }

                }


                boolean valid = true;
                for (ItemStack itemStack : itemsToCheck) {
                    if (itemStack.stackSize > 0) {
                        valid = false;
                        break;
                    }
                }

                if (valid) {
                    addItems(player, itemsToAdd);

                    player.inventory.markDirty();
                    QuestData data = getQuestData(player);
                    Team team = QuestingData.getQuestingData(player).getTeam();
                    if (!team.isSingle() && team.getRewardSetting() == Team.RewardSetting.ANY) {
                        for (int i = 0; i < data.reward.length; i++) {
                            data.reward[i] = false;
                        }
                        sendUpdatedDataToTeam(player);
                    } else {
                        data.claimReward(player);
                        sendUpdatedData(QuestingData.getUserName(player));
                    }
                    sentInfo = true;
                } else {
                    return;
                }
            }


            if (reputationRewards != null && getQuestData(player).canClaim()) {
                getQuestData(player).claimed = true;
                QuestingData.getQuestingData(player).getTeam().receiveAndSyncReputation(this, reputationRewards);
                EventHandler.instance().onEvent(new EventHandler.ReputationEvent(player));
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

    public static void addItems(EntityPlayer player, List<ItemStack> itemsToAdd) {
        for (int i = 0; i < player.inventory.mainInventory.length; i++) {
            Iterator<ItemStack> iterator = itemsToAdd.iterator();
            while (iterator.hasNext()) {
                ItemStack itemStack = iterator.next();

                if (player.inventory.mainInventory[i] == null) {
                    int amount = Math.min(itemStack.getMaxStackSize(), itemStack.stackSize);
                    ItemStack copy = itemStack.copy();
                    copy.stackSize = amount;
                    player.inventory.mainInventory[i] = copy;
                    itemStack.stackSize -= amount;
                    if (itemStack.stackSize <= 0) {
                        iterator.remove();
                    }
                    break;
                } else if (player.inventory.mainInventory[i].isItemEqual(itemStack) && ItemStack.areItemStackTagsEqual(itemStack, player.inventory.mainInventory[i])) {
                    int amount = Math.min(itemStack.getMaxStackSize() - player.inventory.mainInventory[i].stackSize, itemStack.stackSize);
                    player.inventory.mainInventory[i].stackSize += amount;
                    itemStack.stackSize -= amount;
                    if (itemStack.stackSize <= 0) {
                        iterator.remove();
                    }
                    break;
                }
            }
        }
    }

    public void setGuiCenterX(int x) {
        this.x = x - getGuiW() / 2;
    }

    public void setGuiCenterY(int y) {
        this.y = y - getGuiH() / 2;
    }

    public void setBigIcon(boolean b) {
        isBig = b;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
        cachedDescription = null;
    }

    public void setItem(GuiEditMenuItem.Element element, int id, GuiEditMenuItem.Type type, ItemPrecision precision, EntityPlayer player) {
        if (type == GuiEditMenuItem.Type.REWARD || type == GuiEditMenuItem.Type.PICK_REWARD) {
            if (element instanceof GuiEditMenuItem.ElementItem) {
                ItemStack itemStack = ((GuiEditMenuItem.ElementItem) element).getItem();
                if (itemStack != null) {
                    itemStack.stackSize = Math.min(127, element.getAmount());
                    setReward(itemStack, id, type == GuiEditMenuItem.Type.REWARD);
                }
            }
        } else if (selectedTask != null && selectedTask instanceof QuestTaskItems) {
            ((QuestTaskItems) selectedTask).setItem(element, id, precision);
        } else if (selectedTask != null && selectedTask instanceof QuestTaskLocation && type == GuiEditMenuItem.Type.LOCATION) {
            ((QuestTaskLocation) selectedTask).setIcon(id, (ItemStack) element.getItem(), player);
        } else if (selectedTask != null && selectedTask instanceof QuestTaskMob && type == GuiEditMenuItem.Type.MOB) {
            ((QuestTaskMob) selectedTask).setIcon(id, (ItemStack) element.getItem(), player);
        }
    }

    private void setReward(ItemStack itemStack, int id, boolean isStandardReward) {
        ItemStackRewardList rewardList = isStandardReward ? this.rewards : this.rewardChoices;

        if (id < rewardList.size()) {
            rewardList.set(id, itemStack);
            SaveHelper.add(SaveHelper.EditType.REWARD_CHANGE);
        } else {
            SaveHelper.add(SaveHelper.EditType.REWARD_CREATE);
            rewardList.add(itemStack);
        }
    }

    public enum TaskType {
        CONSUME(QuestTaskItemsConsume.class, "consume"),
        CRAFT(QuestTaskItemsCrafting.class, "craft"),
        LOCATION(QuestTaskLocation.class, "location"),
        CONSUME_QDS(QuestTaskItemsConsumeQDS.class, "consumeQDS"),
        DETECT(QuestTaskItemsDetect.class, "detect"),
        KILL(QuestTaskMob.class, "kill"),
        DEATH(QuestTaskDeath.class, "death"),
        REPUTATION(QuestTaskReputationTarget.class, "reputation"),
        REPUTATION_KILL(QuestTaskReputationKill.class, "reputationKill");

        private final Class<? extends QuestTask> clazz;
        private final String id;

        TaskType(Class<? extends QuestTask> clazz, String id) {
            this.clazz = clazz;
            this.id = id;
        }

        public QuestTask addTask(Quest quest) {
            QuestTask prev = quest.getTasks().size() > 0 ? quest.getTasks().get(quest.getTasks().size() - 1) : null;
            try {
                Constructor ex = clazz.getConstructor(Quest.class, String.class, String.class);
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

        public static TaskType getType(Class<? extends QuestTask> clazz) {
            for (TaskType type : values()) {
                if (type.clazz == clazz) return type;
            }
            return CONSUME;
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

    public void setIcon(ItemStack icon) {
        this.icon = icon;
        if (icon != null) {
            icon.stackSize = 1;
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

        if (selectedTask == null && tasks.size() > 0) {
            selectedTask = tasks.get(0);
        }
    }

    public boolean hasSet(QuestSet selectedSet) {
        return set.equals(selectedSet);
    }

    public void mergeProgress(String playerName, QuestData own, QuestData other) {
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
            task.mergeProgress(playerName, own.tasks[i], task.validateData(other.tasks[i]));
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
            task.autoComplete(QuestingData.getUserName(player));
            task.getData(player).completed = true;
        }
        QuestTask.completeQuest(this, QuestingData.getUserName(player));
    }

    public void reset(String playerName) {
        reset(getQuestData(playerName));
    }

    public void reset(QuestData data) {
        data.available = true;
        addTaskData(data);
    }

    public void resetAll() {
        for (Team team : QuestingData.getAllTeams()) {
            QuestData data = team.getQuestData(id);
            if (data != null && !data.available) {
                reset(data);
                sendUpdatedDataToTeam(team);
            }
        }
    }

    public void resetOnTime(int time) {
        for (Team team : QuestingData.getAllTeams()) {
            QuestData data = team.getQuestData(id);
            if (data != null && !data.available && data.time <= time) {
                reset(data);
                sendUpdatedDataToTeam(team);
            }
        }
    }

    public float getProgress(Team team) {
        String name = team.getPlayers().get(0).getName();
        float data = 0;
        for (QuestTask task : tasks) {
            data += task.getCompletedRatio(name);
        }

        return data / tasks.size();
    }

    public List<Quest> getOptionLinks() {
        return optionLinks;
    }

    public List<Quest> getReversedOptionLinks() {
        return reversedOptionLinks;
    }

    public void setUseModifiedParentRequirement(boolean useModifiedParentRequirement) {
        this.useModifiedParentRequirement = useModifiedParentRequirement;
    }

    public boolean getUseModifiedParentRequirement() {
        return useModifiedParentRequirement;
    }

    public void setParentRequirementCount(int parentRequirementCount) {
        this.parentRequirementCount = parentRequirementCount;
    }

    public int getParentRequirementCount() {
        return parentRequirementCount;
    }

    public static FileHelper FILE_HELPER;

    public static void saveAll(DataWriter dw) {
        dw.writeString(QuestLine.getActiveQuestLine().mainDescription, DataBitHelper.QUEST_DESCRIPTION_LENGTH);

        dw.writeData(QuestLine.getActiveQuestLine().questSets.size(), DataBitHelper.QUEST_SETS);
        for (QuestSet questSet : QuestLine.getActiveQuestLine().questSets) {
            dw.writeString(questSet.getName(), DataBitHelper.QUEST_NAME_LENGTH);
            dw.writeString(questSet.getDescription(), DataBitHelper.QUEST_DESCRIPTION_LENGTH);
            dw.writeData(questSet.getReputationBars().size(), DataBitHelper.BYTE);
            for (ReputationBar reputationBar : questSet.getReputationBars())
                dw.writeData(reputationBar.save(), DataBitHelper.INT);
        }

        Reputation.save(dw);

        dw.writeData(size(), DataBitHelper.QUESTS);
        for (int id = 0; id < size(); id++) {
            Quest quest = getQuest(id);
            dw.writeBoolean(quest != null);
            if (quest != null) {
                dw.writeString(quest.getName(), DataBitHelper.QUEST_NAME_LENGTH);
                dw.writeString(quest.getDescription(), DataBitHelper.QUEST_DESCRIPTION_LENGTH);
                dw.writeData(quest.getGuiX(), DataBitHelper.QUEST_POS_X);
                dw.writeData(quest.getGuiY(), DataBitHelper.QUEST_POS_Y);
                dw.writeBoolean(quest.useBigIcon());

                dw.writeData(quest.set.getId(), DataBitHelper.QUEST_SETS);

                if (quest.getIcon() != null && quest.getIcon().getItem() != null) {
                    dw.writeBoolean(true);
                    dw.writeItemStack(quest.getIcon(), false);
                } else {
                    dw.writeBoolean(false);
                }

                dw.writeBoolean(!quest.requirement.isEmpty());
                if (!quest.requirement.isEmpty()) {
                    dw.writeData(quest.requirement.size(), DataBitHelper.QUESTS);
                    for (Quest required : quest.requirement) {
                        dw.writeData(required.getId(), DataBitHelper.QUESTS);
                    }
                }

                dw.writeBoolean(!quest.optionLinks.isEmpty());
                if (!quest.optionLinks.isEmpty()) {
                    dw.writeData(quest.optionLinks.size(), DataBitHelper.QUESTS);
                    for (Quest optionLink : quest.optionLinks) {
                        dw.writeData(optionLink.getId(), DataBitHelper.QUESTS);
                    }
                }

                quest.getRepeatInfo().save(dw);
                dw.writeData(quest.triggerType.ordinal(), DataBitHelper.TRIGGER_TYPE);
                if (quest.triggerType.isUseTaskCount()) {
                    dw.writeData(quest.triggerTasks, DataBitHelper.TASKS);
                }

                if (quest.useModifiedParentRequirement) {
                    dw.writeBoolean(true);
                    dw.writeData(quest.parentRequirementCount, DataBitHelper.QUESTS);
                } else {
                    dw.writeBoolean(false);
                }


                dw.writeData(quest.tasks.size(), DataBitHelper.TASKS);
                for (int i = 0; i < quest.tasks.size(); i++) {
                    QuestTask task = quest.tasks.get(i);
                    int type = TaskType.getType(task.getClass()).ordinal();
                    dw.writeData(type, DataBitHelper.TASK_TYPE);
                    dw.writeString(task.getDescription(), DataBitHelper.QUEST_NAME_LENGTH);
                    dw.writeString(task.getLongDescription(), DataBitHelper.QUEST_DESCRIPTION_LENGTH);
                    task.save(dw);
                }

                writeRewardData(dw, quest.rewards.toArray(), quest);
                writeRewardData(dw, quest.rewardChoices.toArray(), quest);
                writeRewardData(dw, quest.commandRewardList);

                int count = quest.reputationRewards != null ? quest.reputationRewards.size() : 0;
                dw.writeData(count, DataBitHelper.REPUTATION_REWARD);
                if (quest.reputationRewards != null) {
                    for (ReputationReward reputationReward : quest.reputationRewards) {
                        dw.writeData(reputationReward.getReward().getId(), DataBitHelper.REPUTATION);
                        dw.writeData(reputationReward.getValue(), DataBitHelper.REPUTATION_VALUE);
                    }
                }
            }
        }


        GroupTier.saveAll(dw);
        Group.saveAll(dw);
    }

    private static void writeRewardData(DataWriter dw, ItemStack[] reward, Quest quest) {
        dw.writeBoolean(reward != null);
        if (reward != null) {
            int count = 0;
            for (ItemStack itemStack : reward) {
                if (itemStack != null) {
                    count++;
                }
            }
            dw.writeData(count, DataBitHelper.REWARDS);
            for (ItemStack itemStack : reward) {
                if (itemStack != null && itemStack.getItem() != null) {
                    dw.writeItemStack(itemStack, true);
                } else {
                    FMLLog.log("HQM", Level.ERROR, "The quest %s has an invalid item reference in it's rewards - substituting with HQM invalid item", quest.getName());
                    dw.writeItemStack(new ItemStack(ModItems.invalidItem, 1), false);
                }
            }
        }
    }

    private static void writeRewardData(DataWriter dw, CommandRewardList commands) {
        dw.writeBoolean(!commands.isEmpty());
        if (!commands.isEmpty())
        {
            dw.writeData(commands.size(), DataBitHelper.REWARDS);
            for (String command : commands.asStrings())
                dw.writeString(command, DataBitHelper.QUEST_DESCRIPTION_LENGTH);
        }
    }

    public static void loadAll(DataReader dr, FileVersion version) {
        if (isEditing) FMLLog.log("HQM-EDIT", Level.INFO, "Loading quests");
        if (dr != null) {
            EventHandler.instance().clear();
            try {
                if (version.lacks(FileVersion.LORE)) {
                    QuestLine.getActiveQuestLine().mainDescription = "No description";
                } else {
                    QuestLine.getActiveQuestLine().mainDescription = dr.readString(DataBitHelper.QUEST_DESCRIPTION_LENGTH);
                }

                if (version.lacks(FileVersion.SETS)) {
                    QuestLine.getActiveQuestLine().questSets.add(new QuestSet("Automatically generated", "This set was automatically generated. All your quests were put in this one."));
                } else {
                    int setCount = dr.readData(DataBitHelper.QUEST_SETS);
                    for (int i = 0; i < setCount; i++) {
                        String name = dr.readString(DataBitHelper.QUEST_NAME_LENGTH);
                        String description = dr.readString(DataBitHelper.QUEST_DESCRIPTION_LENGTH);
                        QuestSet questSet = new QuestSet(name, description);
                        if (version.contains(FileVersion.REPUTATION_BARS)) {
                            int barCount = dr.readData(DataBitHelper.BYTE);
                            for (int ii = 0; ii < barCount; ii++) {
                                int data = dr.readData(DataBitHelper.INT);
                                questSet.addRepBar(new ReputationBar(version, data));
                            }
                        }
                        QuestLine.getActiveQuestLine().questSets.add(questSet);
                    }
                }

                Reputation.load(dr, version);


                int count = dr.readData(DataBitHelper.QUESTS);
                if (isEditing) FMLLog.log("HQM-EDIT", Level.INFO, "%d quests found", count);
                for (int id = 0; id < count; id++) {
                    if (dr.readBoolean()) {
                        String name = dr.readString(DataBitHelper.QUEST_NAME_LENGTH);
                        String description = dr.readString(DataBitHelper.QUEST_DESCRIPTION_LENGTH);
                        int x = dr.readData(DataBitHelper.QUEST_POS_X);
                        int y = dr.readData(DataBitHelper.QUEST_POS_Y);
                        boolean big = dr.readBoolean();

                        Quest quest = new Quest(id, name, description, x, y, big);
                        if (isEditing) FMLLog.log("HQM-EDIT", Level.INFO, "Loading quest %s", name);

                        if (version.lacks(FileVersion.SETS)) {
                            quest.setQuestSet(QuestLine.getActiveQuestLine().questSets.get(0));
                        } else {
                            int setId = dr.readData(DataBitHelper.QUEST_SETS);
                            quest.setQuestSet(QuestLine.getActiveQuestLine().questSets.get(setId));
                        }

                        if (version.contains(FileVersion.SETS) && dr.readBoolean()) {
                            ItemStack readItemStack = dr.readItemStack(false);
                            if (readItemStack.getItem() == null) {
                                FMLLog.log("HQM", Level.ERROR, "Attempted to read invalid icon for quest");
                                readItemStack = null;
                            }
                            quest.setIcon(readItemStack);
                        } else {
                            quest.setIcon(null);
                        }


                        if (dr.readBoolean()) {
                            quest.requirementIds = new ArrayList<Integer>();
                            int requirementCount = dr.readData(DataBitHelper.QUESTS);
                            for (int i = 0; i < requirementCount; i++) {
                                quest.requirementIds.add(dr.readData(DataBitHelper.QUESTS));
                            }
                        }

                        if (version.contains(FileVersion.OPTION_LINKS) && dr.readBoolean()) {
                            quest.optionLinkIds = new ArrayList<Integer>();
                            int optionLinkCount = dr.readData(DataBitHelper.QUESTS);
                            for (int i = 0; i < optionLinkCount; i++) {
                                quest.optionLinkIds.add(dr.readData(DataBitHelper.QUESTS));
                            }
                        }

                        quest.getRepeatInfo().load(dr, version);
                        if (version.contains(FileVersion.TRIGGER_QUESTS)) {
                            quest.triggerType = TriggerType.values()[dr.readData(DataBitHelper.TRIGGER_TYPE)];
                            if (quest.triggerType.isUseTaskCount()) {
                                quest.triggerTasks = dr.readData(DataBitHelper.TASKS);
                            }
                        }

                        if (version.contains(FileVersion.PARENT_COUNT) && dr.readBoolean()) {
                            quest.useModifiedParentRequirement = true;
                            quest.parentRequirementCount = dr.readData(DataBitHelper.QUESTS);
                        } else {
                            quest.useModifiedParentRequirement = false;
                        }

                        int tasks = dr.readData(DataBitHelper.TASKS);
                        for (int i = 0; i < tasks; i++) {
                            int type = dr.readData(DataBitHelper.TASK_TYPE);
                            String taskName = dr.readString(DataBitHelper.QUEST_NAME_LENGTH);
                            String taskDescription = dr.readString(DataBitHelper.QUEST_DESCRIPTION_LENGTH);

                            try {
                                Class<? extends QuestTask> clazz = TaskType.values()[type].clazz;
                                Constructor<? extends QuestTask> constructor = clazz.getConstructor(Quest.class, String.class, String.class);
                                Object obj = constructor.newInstance(quest, taskName, taskDescription);
                                QuestTask task = (QuestTask) obj;
                                task.load(dr, version);
                                if (quest.tasks.size() > 0) {
                                    task.addRequirement(quest.tasks.get(quest.tasks.size() - 1));
                                }
                                quest.tasks.add(task);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }

                        if (isEditing) FMLLog.log("HQM-EDIT", Level.INFO, "Loading quest rewards", name);
                        quest.rewards.set(readRewardData(dr));
                        if (isEditing) FMLLog.log("HQM-EDIT", Level.INFO, "Loading quest reward choices", name);
                        quest.rewardChoices.set(readRewardData(dr));

                        if (version.contains(FileVersion.COMMAND_REWARDS)) {
                            if (isEditing) FMLLog.log("HQM-EDIT", Level.INFO, "Loading quest reward choices", name);
                            quest.commandRewardList.addAll(readCommandRewardData(dr));
                        }

                        if (version.contains(FileVersion.REPUTATION)) {
                            int reputationCount = dr.readData(DataBitHelper.REPUTATION_REWARD);
                            if (reputationCount == 0) {
                                quest.reputationRewards = null;
                            } else {
                                quest.reputationRewards = new ArrayList<ReputationReward>();
                                for (int i = 0; i < reputationCount; i++) {
                                    quest.reputationRewards.add(new ReputationReward(Reputation.getReputation(dr.readData(DataBitHelper.REPUTATION)), dr.readData(DataBitHelper.REPUTATION_VALUE)));
                                }
                            }
                        } else {
                            quest.reputationRewards = null;
                        }
                    }
                }


                for (Quest quest : QuestLine.getActiveQuestLine().quests.values()) {
                    if (quest.requirementIds != null) {
                        for (Integer requirementId : quest.requirementIds) {
                            quest.addRequirement(requirementId);
                        }
                    }

                    if (quest.optionLinkIds != null) {
                        for (Integer optionLinkId : quest.optionLinkIds) {
                            quest.addOptionLink(optionLinkId);
                        }
                    }
                }


                if (isEditing) FMLLog.log("HQM-EDIT", Level.INFO, "Loading bags");
                if (version.contains(FileVersion.BAGS)) {
                    GroupTier.readAll(dr, version);
                    Group.readAll(dr, version);
                }
                if (isEditing) FMLLog.log("HQM-EDIT", Level.INFO, "Loading complete");
            } catch (Exception ex) {
                ex.printStackTrace();
                FMLLog.log("HQM", Level.ERROR, ex, "Error occurred during quest loading");
            }

            /*PrintWriter writer = null;
            try {
                writer = new PrintWriter("english.txt", "UTF-8");

                writer.println("<Lore>" + getRawMainDescription());
                writer.println();


                List<GroupTier> tiers = GroupTier.getTiers();
                for (int i = 0; i < tiers.size(); i++) {
                    GroupTier tier = tiers.get(i);
                    writer.println("<RewardTier#" + i + ">" + tier.getName());
                }
                writer.println();

                for (Quest quest : getQuests()) {
                    if (quest != null) {
                        writer.println("<Quest#" + quest.getId() +">");
                        writer.println("<Name>" + quest.getName());
                        writer.println("<Description>" + quest.getDescription());
                        for (QuestTask task : quest.getTasks()) {
                            writer.println("<Task#" + task.getId() + ">");
                            writer.println("<Name>" + task.getDescription());
                            writer.println("<Description>" + task.getLongDescription());
                        }
                        writer.println();
                    }
                }
                writer.close();
            }catch (IOException ex) {
                ex.printStackTrace();
            }finally {
                if (writer != null) {
                    writer.close();
                }
            }*/


        }

        SaveHelper.onLoad();
    }

    private static ItemStack[] readRewardData(DataReader dr) {
        if (dr.readBoolean()) {
            int count = dr.readData(DataBitHelper.REWARDS);
            ItemStack[] reward = new ItemStack[count];
            for (int i = 0; i < reward.length; i++) {
                ItemStack rewardItemStack = dr.readAndFixItemStack(true);
                if (rewardItemStack != null && rewardItemStack.getItem() != null) {
                    reward[i] = rewardItemStack;
                } else {
                    FMLLog.log("HQM", Level.ERROR, "Invalid reward item. Substituting with HQM invalid item.");
                    reward[i] = new ItemStack(ModItems.invalidItem, 1);
                }
            }
            return reward;
        } else {
            return null;
        }
    }

    private static List<CommandReward> readCommandRewardData(DataReader dr) {
        List<CommandReward> rewards = new ArrayList<>();
        if(dr.readBoolean()) {
            int count  = dr.readData(DataBitHelper.REWARDS);
            for (int i = 0; i < count; i++) {
                String commandString = dr.readString(DataBitHelper.QUEST_DESCRIPTION_LENGTH);
                rewards.add(new CommandReward(new CommandReward.Command(commandString)));
            }
        }
        return rewards;
    }


    public static void init(final String path) {
        FILE_HELPER = new FileHelper() {
            private boolean isUnLocked = true;
            private File file = new File(path + "quests.hqm");
            private File lock = new File(path + "lock.txt");

            @Override
            public boolean loadData(File file) {
                return super.loadData(this.file);
            }

            @Override
            public SaveResult saveData(File file) {
                return super.saveData(this.file);
            }

            @Override
            public void write(DataWriter dw) {
                String lockCode = readCode();
                dw.writeString(lockCode, DataBitHelper.PASS_CODE);
                saveAll(dw);
            }

            @Override
            public void read(DataReader dr, FileVersion version) {
                isUnLocked = false;
                String code = version.lacks(FileVersion.LOCK) ? null : dr.readString(DataBitHelper.PASS_CODE);

                if (code == null || code.equals(readCode())) {
                    isUnLocked = true;
                }

                if (!isEditing || isUnLocked) {
                    loadAll(dr, version);
                }
            }

            private String readCode() {
                if (lock.exists()) {
                    BufferedReader br = null;
                    try {
                        br = new BufferedReader(new FileReader(lock));
                        String str = br.readLine();
                        return str.substring(0, Math.min(DataBitHelper.PASS_CODE.getMaximum(), str.length()));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        return null;
                    } finally {
                        if (br != null) {
                            try {
                                br.close();
                            } catch (IOException ignored) {
                            }
                        }
                    }
                }
                return null;
            }

        };

        QuestLine.getActiveQuestLine().mainPath = path;

        QuestLine.getActiveQuestLine().quests = new HashMap<Short, Quest>();
        QuestLine.getActiveQuestLine().questSets = new ArrayList<QuestSet>();
        FILE_HELPER.loadData(null);

    }

    //Todo at the moment this can create empty quests, not a problem but they will take up ids
    public static void removeQuest(Quest quest) {
        for (Quest requirement : quest.requirement) {
            requirement.reversedRequirement.remove(quest);
        }
        for (Quest optionLink : quest.optionLinks) {
            optionLink.reversedOptionLinks.remove(quest);
        }

        for (QuestTask task : quest.tasks) {
            task.onDelete();
        }

        quest.setQuestSet(null);
        QuestLine.getActiveQuestLine().quests.remove((Short) quest.getId());
        /*if (quest.getId() == size() - 1) {
            QuestLine.getActiveQuestLine().questCount--;
        }*/

        for (Quest other : QuestLine.getActiveQuestLine().quests.values()) {
            Iterator<Quest> iterator = other.requirement.iterator();
            while (iterator.hasNext()) {
                Quest element = iterator.next();
                if (element.getId() == quest.getId()) {
                    iterator.remove();
                }
            }

            iterator = other.optionLinks.iterator();
            while (iterator.hasNext()) {
                Quest element = iterator.next();
                if (element.getId() == quest.getId()) {
                    iterator.remove();
                }
            }
        }
    }

    public QuestSet getQuestSet() {
        return set;
    }

    public void setQuestSet(QuestSet set) {
        if (this.set != null) {
            this.set.removeQuest(this);
        }
        this.set = set;
        if (this.set != null) {
            this.set.addQuest(this);
        }
    }

    public List<Quest> getReversedRequirement() {
        return reversedRequirement;
    }

}
