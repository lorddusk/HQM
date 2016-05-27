package hardcorequesting.quests.task;


import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import hardcorequesting.client.interfaces.GuiBase;
import hardcorequesting.client.interfaces.GuiQuestBook;
import hardcorequesting.event.EventHandler;
import hardcorequesting.io.adapter.QuestTaskAdapter;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestData;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.quests.RepeatType;
import hardcorequesting.quests.data.QuestDataTask;
import hardcorequesting.team.RewardSetting;
import hardcorequesting.team.TeamStats;
import hardcorequesting.util.Translator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public abstract class QuestTask {
    protected Quest parent;
    private List<QuestTask> requirements;
    public String description;
    private String longDescription;
    private int id;
    private List<String> cachedDescription;

    static final int START_X = 180;
    static final int START_Y = 95;

    public QuestTask(Quest parent, String description, String longDescription) {
        this.parent = parent;
        this.requirements = new ArrayList<>();
        this.description = description;
        this.longDescription = longDescription;
        updateId();
    }

    public void updateId() {
        this.id = parent.nextTaskId++;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isCompleted(EntityPlayer player) {
        return getData(player).completed;
    }

    public boolean isCompleted(String uuid) {
        return getData(uuid).completed;
    }

    public boolean isVisible(EntityPlayer player) {
        Iterator itr = this.requirements.iterator();
        QuestTask requirement;
        do {
            if (!itr.hasNext()) return true;
            requirement = (QuestTask) itr.next();
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


    public QuestDataTask getData(EntityPlayer player) {
        return getData(QuestingData.getUserUUID(player));
    }

    public QuestDataTask getData(String uuid) {
        QuestData questData = QuestingData.getQuestingData(uuid).getQuestData(parent.getId());
        if (id >= questData.tasks.length) {
            questData.tasks = Arrays.copyOf(questData.tasks, id + 1);
            questData.tasks[id] = newQuestData();
        }
        return questData.tasks[id] = validateData(questData.tasks[id]);
    }

    public QuestDataTask validateData(QuestDataTask data) {
        if (data.getClass() != getDataType()) {
            return newQuestData();
        }

        return data;
    }

    private QuestDataTask newQuestData() {
        try {
            Constructor<? extends QuestDataTask> constructor = getDataType().getConstructor(new Class[]{QuestTask.class});
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

    public String getLangKeyLongDescription() {
        return longDescription;
    }

    public String getLongDescription() {
        return Translator.translate(longDescription);
    }

    @SideOnly(Side.CLIENT)
    public List<String> getCachedLongDescription(GuiBase gui) {
        if (cachedDescription == null) {
            cachedDescription = gui.getLinesFromText(Translator.translate(longDescription), 0.7F, 130);
        }

        return cachedDescription;
    }

    public void completeTask(String playerName) {
        getData(playerName).completed = true;
        completeQuest(parent, playerName);
    }

    public static void completeQuest(Quest quest, String playerName) {
        if (!quest.isEnabled(playerName) || !quest.isAvailable(playerName)) return;
        for (QuestTask questTask : quest.getTasks()) {
            if (!questTask.getData(playerName).completed) {
                return;
            }
        }
        QuestData data = quest.getQuestData(playerName);

        data.completed = true;
        data.claimed = false;
        data.available = false;
        data.time = Quest.serverTicker.getHours();


        if (QuestingData.getQuestingData(playerName).getTeam().getRewardSetting() == RewardSetting.RANDOM) {
            int rewardId = (int) (Math.random() * data.reward.length);
            data.reward[rewardId] = true;
        } else {
            for (int i = 0; i < data.reward.length; i++) {
                data.reward[i] = true;
            }
        }
        TeamStats.refreshTeam(QuestingData.getQuestingData(playerName).getTeam());

        for (Quest child : quest.getReversedRequirement()) {
            completeQuest(child, playerName);
            child.sendUpdatedDataToTeam(playerName);
        }

        if (quest.getRepeatInfo().getType() == RepeatType.INSTANT) {
            quest.reset(playerName);
        }
    }

    @SideOnly(Side.CLIENT)
    public abstract void draw(GuiQuestBook gui, EntityPlayer player, int mX, int mY);

    @SideOnly(Side.CLIENT)
    public abstract void onClick(GuiQuestBook gui, EntityPlayer player, int mX, int mY, int b);

    public abstract void onUpdate(EntityPlayer player);

    public int getId() {
        return id;
    }

    public Quest getParent() {
        return parent;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
        cachedDescription = null;
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

    public abstract float getCompletedRatio(String uuid);

    public abstract void mergeProgress(String uuid, QuestDataTask own, QuestDataTask other);

    public abstract void autoComplete(String uuid);

    public void copyProgress(QuestDataTask own, QuestDataTask other) {
        own.completed = other.completed;
    }

    public void onDelete() {
        EventHandler.instance().remove(this);
    }

    public void register(EventHandler.Type... types) {
        EventHandler.instance().add(this, types);
    }

    //for these to be called one must register the task using the method above using the correct types
    public void onServerTick(TickEvent.ServerTickEvent event) {
    }

    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
    }

    public void onLivingDeath(LivingDeathEvent event) {
    }

    public void onCrafting(PlayerEvent.ItemCraftedEvent event) {
    }

    public void onItemPickUp(EntityItemPickupEvent event) {
    }

    public void onOpenBook(EventHandler.BookOpeningEvent event) {
    }

    public void onReputationChange(EventHandler.ReputationEvent event) {
    }
}
