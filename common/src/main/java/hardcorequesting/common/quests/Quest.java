package hardcorequesting.common.quests;

import com.mojang.datafixers.util.Either;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.config.HQMConfig;
import hardcorequesting.common.network.IMessage;
import hardcorequesting.common.network.NetworkManager;
import hardcorequesting.common.network.message.QuestDataUpdateMessage;
import hardcorequesting.common.platform.FluidStack;
import hardcorequesting.common.quests.data.QuestData;
import hardcorequesting.common.quests.reward.QuestRewards;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.team.PlayerEntry;
import hardcorequesting.common.team.Team;
import hardcorequesting.common.team.TeamManager;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.HQMUtil;
import hardcorequesting.common.util.SaveHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class Quest {
    
    @Deprecated
    public static boolean isEditing = false;
    public static UUID speciallySelectedQuestId = null;
    public static QuestTicker clientTicker;
    public static QuestTicker serverTicker;
    
    private final QuestRewards rewards = new QuestRewards(this);
    private UUID questId;
    private String name;
    private String description;
    private List<UUID> requirement;
    private List<UUID> reversedRequirement;
    private List<UUID> optionLinks;
    private List<UUID> reversedOptionLinks;
    private List<QuestTask<?>> tasks;
    private RepeatInfo repeatInfo = new RepeatInfo(RepeatType.NONE, 0, 0);
    private TriggerType triggerType = TriggerType.NONE;
    private int triggerTasks = 1;
    private int parentRequirementCount = -1;
    private int x;
    private int y;
    private boolean isBig;
    private Either<ItemStack, FluidStack> iconStack = Either.left(ItemStack.EMPTY);
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
    
    public boolean hasReward(UUID playerId) {
        return getRewards().hasReward(playerId);
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
    
    public boolean isVisible(Player player, Map<Quest, Boolean> isVisibleCache, Map<Quest, Boolean> isLinkFreeCache) {
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
    
    public boolean isEnabled(Player player, Map<Quest, Boolean> isVisibleCache, Map<Quest, Boolean> isLinkFreeCache) {
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
    
    public boolean isLinkFree(Player player, Map<Quest, Boolean> cache) {
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
    
    public Either<ItemStack, FluidStack> getIconStack() {
        return iconStack;
    }
    
    public void setIconStack(Either<ItemStack, FluidStack> iconStack) {
        this.iconStack = iconStack;
    }
    
    public void setIconIfEmpty(ItemStack stack) {
        if (!stack.isEmpty()) {
            stack = stack.copy();
            stack.setCount(1);
            setIconIfEmpty(Either.left(stack));
        }
    }
    
    public void setIconIfEmpty(FluidStack stack) {
        if (!stack.isEmpty())
            setIconIfEmpty(Either.right(stack));
    }
    
    public void setIconIfEmpty(Either<ItemStack, FluidStack> iconStack) {
        if (this.iconStack.map(ItemStack::isEmpty, FluidStack::isEmpty)) {
            setIconStack(iconStack);
            SaveHelper.add(EditType.ICON_CHANGE);
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
        boolean hasReward = hasReward(player.getUUID());
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
    
    public QuestData createData(int players) {
        QuestData data = new QuestData(players);
        data.verifyTasksSize(this);
        return data;
    }
    
    public List<QuestTask<?>> getTasks() {
        return tasks;
    }
    
    public void removeTask(QuestTask<?> task) {
        int index = tasks.indexOf(task);
    
        task.onDelete();
    
        tasks.remove(index);
        int nextTaskId = 0;
        for (QuestTask<?> questTask : tasks) {
            questTask.updateId(nextTaskId++);
        }
        
        for (Team team : TeamManager.getInstance().getTeams()) {
            team.getQuestData(this.getQuestId()).clearTaskData(this);
        }
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
    }
    
    public boolean hasSameSetAs(Quest child) {
        return child.set.equals(set);
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
