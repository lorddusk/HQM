package hardcorequesting.quests.task;


import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import hardcorequesting.client.ClientChange;
import hardcorequesting.client.interfaces.GuiBase;
import hardcorequesting.client.interfaces.GuiQuestBook;
import hardcorequesting.client.sounds.Sounds;
import hardcorequesting.event.EventTrigger;
import hardcorequesting.io.adapter.QuestTaskAdapter;
import hardcorequesting.network.NetworkManager;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestData;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.quests.RepeatType;
import hardcorequesting.quests.data.QuestDataTask;
import hardcorequesting.team.RewardSetting;
import hardcorequesting.team.TeamStats;
import hardcorequesting.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;

public abstract class QuestTask {
    
    static final int START_X = 180;
    static final int START_Y = 95;
    public String description;
    protected Quest parent;
    private List<QuestTask> requirements;
    private String longDescription;
    private int id;
    private List<String> cachedDescription;
    
    public QuestTask(Quest parent, String description, String longDescription) {
        this.parent = parent;
        this.requirements = new ArrayList<>();
        this.description = description;
        this.longDescription = longDescription;
        updateId();
    }
    
    public static void completeQuest(Quest quest, UUID uuid) {
        if (!quest.isEnabled(uuid) || !quest.isAvailable(uuid)) return;
        for (QuestTask questTask : quest.getTasks()) {
            if (!questTask.getData(uuid).completed) {
                return;
            }
        }
        QuestData data = quest.getQuestData(uuid);
        
        data.completed = true;
        data.claimed = false;
        data.available = false;
        data.time = Quest.serverTicker.getHours();
        
        
        if (QuestingData.getQuestingData(uuid).getTeam().getRewardSetting() == RewardSetting.RANDOM) {
            int rewardId = (int) (Math.random() * data.reward.length);
            data.reward[rewardId] = true;
        } else {
            for (int i = 0; i < data.reward.length; i++) {
                data.reward[i] = true;
            }
        }
        quest.sendUpdatedDataToTeam(uuid);
        TeamStats.refreshTeam(QuestingData.getQuestingData(uuid).getTeam());
        
        for (Quest child : quest.getReversedRequirement()) {
            completeQuest(child, uuid);
            child.sendUpdatedDataToTeam(uuid);
        }
        
        if (quest.getRepeatInfo().getType() == RepeatType.INSTANT) {
            quest.reset(uuid);
        }
        
        PlayerEntity player = QuestingData.getPlayer(uuid);
        if (player instanceof ServerPlayerEntity && !quest.hasReward(player)) {
            // when there is no reward and it just completes the quest play the music
            NetworkManager.sendToPlayer(ClientChange.SOUND.build(Sounds.COMPLETE), (ServerPlayerEntity) player);
        }
        
        if (player != null) {
            EventTrigger.instance().onEvent(new EventTrigger.QuestCompletedEvent(player, quest.getQuestId()));
        }
    }
    
    public void updateId() {
        this.id = parent.nextTaskId++;
    }
    
    public boolean allowManual() {
        return false;
    }
    
    public boolean allowDetect() {
        return false;
    }
    
    public boolean isCompleted(PlayerEntity player) {
        return getData(player).completed;
    }
    
    public boolean isCompleted(UUID uuid) {
        return getData(uuid).completed;
    }
    
    public boolean isVisible(PlayerEntity player) {
        Iterator<QuestTask> itr = this.requirements.iterator();
        QuestTask requirement;
        do {
            if (!itr.hasNext()) return true;
            requirement = itr.next();
        } while (requirement.isCompleted(player) && requirement.isVisible(player));
        return false;
    }
    
    public Class<? extends QuestDataTask> getDataType() {
        return QuestDataTask.class;
    }
    
    public void write(QuestDataTask task, JsonWriter out) throws IOException {
        task.write(out);
    }
    
    public void read(QuestDataTask task, JsonReader in) throws IOException {
        task.update(QuestTaskAdapter.QUEST_DATA_TASK_ADAPTER.read(in));
    }
    
    public QuestDataTask getData(PlayerEntity player) {
        return getData(player.getUuid());
    }
    
    public QuestDataTask getData(UUID uuid) {
        if (this.id < 0) {
            return newQuestData(); // possible fix for #247
        }
        QuestData questData = QuestingData.getQuestingData(uuid).getQuestData(parent.getQuestId());
        if (id >= questData.tasks.length) {
            questData.tasks = Arrays.copyOf(questData.tasks, id + 1);
            questData.tasks[id] = newQuestData();
        }
        return questData.tasks[id] = validateData(questData.tasks[id]);
    }
    
    public QuestDataTask validateData(QuestDataTask data) {
        if (data == null || data.getClass() != getDataType()) {
            return newQuestData();
        }
        
        return data;
    }
    
    private QuestDataTask newQuestData() {
        try {
            Constructor<? extends QuestDataTask> constructor = getDataType().getConstructor(QuestTask.class);
            Object obj = constructor.newInstance(this);
            return (QuestDataTask) obj;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    public String getLangKeyDescription() {
        return description;
    }
    
    public String getDescription() {
        return Translator.translate(description);
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getLangKeyLongDescription() {
        return longDescription;
    }
    
    public String getLongDescription() {
        return Translator.translate(longDescription);
    }
    
    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
        cachedDescription = null;
    }
    
    @Environment(EnvType.CLIENT)
    public List<String> getCachedLongDescription(GuiBase gui) {
        if (cachedDescription == null) {
            cachedDescription = gui.getLinesFromText(Translator.translate(longDescription), 0.7F, 130);
        }
        
        return cachedDescription;
    }
    
    public void completeTask(UUID uuid) {
        getData(uuid).completed = true;
        completeQuest(parent, uuid);
    }
    
    @Environment(EnvType.CLIENT)
    public abstract void draw(GuiQuestBook gui, PlayerEntity player, int mX, int mY);
    
    @Environment(EnvType.CLIENT)
    public abstract void onClick(GuiQuestBook gui, PlayerEntity player, int mX, int mY, int b);
    
    public abstract void onUpdate(PlayerEntity player);
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public Quest getParent() {
        return parent;
    }
    
    public List<QuestTask> getRequirements() {
        return requirements;
    }
    
    public void addRequirement(QuestTask task) {
        requirements.add(task);
    }
    
    public void clearRequirements() {
        requirements.clear();
    }
    
    public abstract float getCompletedRatio(UUID uuid);
    
    public abstract void mergeProgress(UUID playerId, QuestDataTask own, QuestDataTask other);
    
    public void autoComplete(UUID playerId) {
        autoComplete(playerId, true);
    }
    
    public void uncomplete(UUID playerId) {
    }
    
    public abstract void autoComplete(UUID playerId, boolean status);
    
    public void copyProgress(QuestDataTask own, QuestDataTask other) {
        own.completed = other.completed;
    }
    
    public void onDelete() {
        EventTrigger.instance().remove(this);
    }
    
    public void register(EventTrigger.Type... types) {
        EventTrigger.instance().add(this, types);
    }
    
    public boolean isValid() {
        return getParent() != null && getParent().getTasks() != null && getParent().getTasks().contains(this);
    }
    
    //for these to be called one must register the task using the method above using the correct types
    public void onServerTick(MinecraftServer server) {
    }
    
    public void onPlayerTick(ServerPlayerEntity playerEntity) {
    }
    
    public void onLivingDeath(LivingEntity entity, DamageSource source) {
    }
    
    public void onCrafting(PlayerEntity player, ItemStack stack, CraftingInventory craftingInv) {
    }
    
    public void onItemPickUp(PlayerEntity playerEntity, ItemStack stack) {
    }
    
    public void onOpenBook(EventTrigger.BookOpeningEvent event) {
    }
    
    public void onReputationChange(EventTrigger.ReputationEvent event) {
    }
    
    public void onAnimalTame(PlayerEntity tamer, Entity entity) {
    }
    
    public void onAdvancement(ServerPlayerEntity playerEntity) {
    }
    
    public void onQuestCompleted(EventTrigger.QuestCompletedEvent event) {
    }
    
    public void onQuestSelected(EventTrigger.QuestSelectedEvent event) {
    }
    
    public void onBlockPlaced(BlockItem item, ItemUsageContext context) {
    }
    
    public void onBlockBroken(BlockPos blockPos, BlockState blockState, ServerPlayerEntity player) {
    }
    
    public void onItemUsed(PlayerEntity playerEntity, World world, Hand hand) {
    }
    
    public void onBlockUsed(PlayerEntity playerEntity, World world, Hand hand, BlockHitResult hitResult) {
    }
}
