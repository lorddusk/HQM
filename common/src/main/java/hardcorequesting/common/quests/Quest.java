package hardcorequesting.common.quests;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.client.ClientChange;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.*;
import hardcorequesting.common.client.interfaces.edit.IntInputMenu;
import hardcorequesting.common.client.interfaces.edit.TextMenu;
import hardcorequesting.common.config.HQMConfig;
import hardcorequesting.common.event.EventTrigger;
import hardcorequesting.common.network.GeneralUsage;
import hardcorequesting.common.network.IMessage;
import hardcorequesting.common.network.NetworkManager;
import hardcorequesting.common.network.message.QuestDataUpdateMessage;
import hardcorequesting.common.quests.data.QuestData;
import hardcorequesting.common.quests.reward.QuestRewards;
import hardcorequesting.common.quests.task.DeathTask;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.quests.task.TaskType;
import hardcorequesting.common.quests.task.item.ConsumeItemTask;
import hardcorequesting.common.quests.task.reputation.KillReputationTask;
import hardcorequesting.common.team.PlayerEntry;
import hardcorequesting.common.team.Team;
import hardcorequesting.common.team.TeamManager;
import hardcorequesting.common.util.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class Quest {
    
    private static final int VISIBLE_DESCRIPTION_LINES = 7;
    private static final int VISIBLE_TASKS = 3;
    //region pixelinfo
    public static final int START_X = 20;
    private static final int TEXT_HEIGHT = 9;
    private static final int TASK_LABEL_START_Y = 100;
    private static final int TASK_MARGIN = 2;
    private static final int TITLE_START_Y = 15;
    private static final int DESCRIPTION_START_Y = 30;
    private static final int TASK_DESCRIPTION_X = 180;
    private static final int TASK_DESCRIPTION_Y = 20;
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
    
    private final QuestRewards rewards = new QuestRewards(this);
    public int nextTaskId;
    private UUID questId;
    private String name;
    private String description;
    private List<UUID> requirement;
    private List<UUID> reversedRequirement;
    private List<UUID> optionLinks;
    private List<UUID> reversedOptionLinks;
    private List<QuestTask<?>> tasks;
    private List<FormattedText> cachedDescription;
    private QuestTask<?> selectedTask;
    private RepeatInfo repeatInfo = new RepeatInfo(RepeatType.NONE, 0, 0);
    private TriggerType triggerType = TriggerType.NONE;
    private int triggerTasks = 1;
    private int parentRequirementCount = -1;
    private int x;
    private int y;
    private boolean isBig;
    private ItemStack iconStack = ItemStack.EMPTY;
    private QuestSet set;
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
                return selectedTask != null && selectedTask instanceof DeathTask && Quest.canQuestsBeEdited();
            }
            
            @Override
            @Environment(EnvType.CLIENT)
            public void onClick(GuiBase gui, Player player) {
                DeathTask task = (DeathTask) selectedTask;
                IntInputMenu.display(gui, player, "hqm.deathTask.reqDeathCount", task.getDeathsRequired(), task::setDeaths);
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
                return selectedTask != null && selectedTask instanceof KillReputationTask && Quest.canQuestsBeEdited();
            }
            
            @Override
            @Environment(EnvType.CLIENT)
            public void onClick(GuiBase gui, Player player) {
                KillReputationTask task = (KillReputationTask) selectedTask;
                IntInputMenu.display(gui, player, "hqm.mobTask.reqKills", task.getKillsRequirement(), task::setKills);
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
                return selectedTask instanceof ConsumeItemTask && !selectedTask.isCompleted(player);
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
    
    public QuestRewards getRewards() {
        return rewards;
    }
    
    public boolean hasReward(Player player) {
        return getRewards().hasReward(getQuestData(player), player);
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
            SaveHelper.add(EditType.REQUIREMENT_CHANGE);
        }
    }
    
    private boolean lookForId(UUID questId, boolean reversed) {
        return lookForId(questId, new HashSet<>(), reversed);
    }
    
    private boolean lookForId(UUID questId, Set<UUID> visited, boolean reversed) {
        if (visited.add(this.getQuestId())) {
            List<UUID> currentRequirements = reversed ? reversedRequirement : requirement;
            for (UUID id : currentRequirements)
                if (id.equals(questId) || QuestSetsManager.getInstance().quests.get(id).lookForId(questId, visited, reversed))
                    return true;
        }
        return false;
    }
    
    public void clearRequirements() {
        SaveHelper.add(EditType.REQUIREMENT_REMOVE, requirement.size());
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
            SaveHelper.add(EditType.OPTION_CHANGE);
            optionLinks.add(quest.getQuestId());
            quest.reversedOptionLinks.add(getQuestId());
        }
    }
    
    public void clearOptionLinks() {
        SaveHelper.add(EditType.OPTION_REMOVE, optionLinks.size());
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
        boolean hasReward = hasReward(player);
        if (canQuestsBeEdited() && !isVisible(player)) {
            return HQMConfig.QUEST_INVISIBLE;
        } else if (!isEnabled(player)) {
            return HQMConfig.QUEST_DISABLED;
        } else if (!isAvailable(player) && !hasReward) {
            return getRepeatInfo().getType() == RepeatType.NONE ? HQMConfig.QUEST_COMPLETE : HQMConfig.QUEST_COMPLETE_REPEATABLE;
        } else {
            if (HQMConfig.getInstance().Interface.Quests.SINGLE_COLOUR) return HQMConfig.QUEST_AVAILABLE;
            
            int componentBase = 0xBB;
            int componentVariable = (int) (Math.abs(Math.sin(tick / 5F)) * 0x44);
            
            int component = componentBase + componentVariable;
            
            
            int red = hasReward ? componentBase : component;
            int green = hasReward ? component * 3 / 4 : component;
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
        QuestData data = getQuestData(player);
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
            QuestTask<?> task = tasks.get(i);
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
        
        for (LargeButton button : buttons) {
            button.draw(matrices, gui, player, mX, mY);
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
        for (ScrollBar scrollBar : scrollBars) {
            scrollBar.draw(gui);
        }
        
        rewards.draw(matrices, gui, player, mX, mY, data);
        
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
            
            selectedTask.getGraphic().draw(matrices, gui, player, mX, mY);
            //}
        } else if (canQuestsBeEdited() && gui.getCurrentMode() == EditMode.TASK) {
            gui.drawString(matrices, gui.getLinesFromText(Translator.translatable("hqm.quest.createTasks"), 0.7F, 130), 180, 20, 0.7F, 0x404040);
        /*} else if (canQuestsBeEdited() && gui.getCurrentMode() == EditMode.CHANGE_TASK) {
            gui.drawString(gui.getLinesFromText(Translator.translate("hqm.quest.itemTaskTypeChange"), 0.7F, 130), 180, 20, 0.7F, 0x404040);*/
        }
    
        for (LargeButton button : buttons) {
            button.renderTooltip(matrices, gui, player, mX, mY);
        }
    
        rewards.drawTooltips(matrices, gui, player, mX, mY, data);
    }
    
    @Environment(EnvType.CLIENT)
    private int getVisibleTasks(GuiBase gui) {
        if (canQuestsBeEdited()) {
            return tasks.size();
        }
        
        int count = 0;
        for (QuestTask<?> task : tasks) {
            if (task.isVisible(((GuiQuestBook) gui).getPlayer())) {
                count++;
            }
        }
        return count;
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
                QuestTask<?> task = tasks.get(i);
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
                                TextMenu.display(gui, player, task.getDescription(), true,
                                        task::setDescription);
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
                                for (QuestTask<?> questTask : tasks) {
                                    questTask.updateId();
                                }
    
                                getQuestData(player).clearTaskData(this);
                                SaveHelper.add(EditType.TASK_REMOVE);
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
    
            rewards.onClick(gui, player, mX, mY);
    
            if (selectedTask != null) {
                selectedTask.getGraphic().onClick(gui, player, mX, mY, b);
            }
            
            
            for (LargeButton button : buttons) {
                if (button.inButtonBounds(gui, mX, mY) && button.isVisible(gui, player) && button.isEnabled(gui, player)) {
                    button.onClick(gui, player);
                    break;
                }
            }
            
            if (gui.getCurrentMode() == EditMode.RENAME) {
                if (gui.inBounds(START_X, TITLE_START_Y, 140, TEXT_HEIGHT, mX, mY)) {
                    TextMenu.display(gui, player, getName(), true, this::setName);
                } else if (gui.inBounds(START_X, DESCRIPTION_START_Y, 130, (int) (VISIBLE_DESCRIPTION_LINES * TEXT_HEIGHT * 0.7), mX, mY)) {
                    TextMenu.display(gui, player, getDescription(), false, this::setDescription);
                } else if (selectedTask != null && gui.inBounds(TASK_DESCRIPTION_X, TASK_DESCRIPTION_Y, 130, (int) (VISIBLE_DESCRIPTION_LINES * TEXT_HEIGHT * 0.7), mX, mY)) {
                    TextMenu.display(gui, player, selectedTask.getLongDescription(), false,
                            selectedTask::setLongDescription);
                }
            }
            
            if (canQuestsBeEdited() && selectedTask != null && gui.getCurrentMode() == EditMode.TASK) {
                selectedTask = null;
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
        data.verifyTasksSize(this);
        return data;
    }
    
    public List<QuestTask<?>> getTasks() {
        return tasks;
    }
    
    public void sendUpdatedDataToTeam(Player player) {
        sendUpdatedDataToTeam(QuestingDataManager.getInstance().getQuestingData(player).getTeam());
    }
    
    public void sendUpdatedDataToTeam(UUID playerId) {
        sendUpdatedDataToTeam(QuestingDataManager.getInstance().getQuestingData(playerId).getTeam());
    }
    
    public void sendUpdatedDataToTeam(Team team) {
        MinecraftServer server = HardcoreQuestingCore.getServer();
        if (server == null || !server.isSameThread())
            throw new IllegalStateException("Tried sending data to players from the client-side. Something is being called client-side when it shouldn't be!");
        
        for (PlayerEntry entry : team.getPlayers()) {
            sendUpdatedData(entry.getPlayerMP());
        }
    }
    
    public void sendUpdatedData(ServerPlayer player) {
        if (player == null) return; // Don't send to nobody you silly goose
        IMessage update = new QuestDataUpdateMessage(
                getQuestId(),
                QuestingDataManager.getInstance().getQuestingData(player).getTeam().getPlayerCount(),
                QuestingDataManager.getInstance().getQuestingData(player).getQuestData(getQuestId())
        );
        NetworkManager.sendToPlayer(update, player);
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
            if (other.available && !own.teamRewardClaimed) {
                own.available = true;
            }
        }
    
    
        for (QuestTask<?> task : tasks) {
            task.mergeProgress(playerId, own, other);
        }
    }
    
    public void copyProgress(QuestData own, QuestData other) {
        own.completed = other.completed;
        own.available = other.available;
    
        for (QuestTask<?> task : tasks) {
            task.copyProgress(own, other);
        }
    }
    
    public void completeQuest(Player player) {
        for (QuestTask<?> task : tasks) {
            task.completeTask(player.getUUID());
        }
        QuestTask.completeQuest(this, player.getUUID());
    }
    
    public void reset(UUID playerId) {
        reset(getQuestData(playerId));
    }
    
    public void reset(QuestData data) {
        data.available = true;
        data.clearTaskData(this);
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
        float data = 0;
        for (QuestTask<?> task : this.tasks) {
            data += task.getCompletedRatio(team);
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
