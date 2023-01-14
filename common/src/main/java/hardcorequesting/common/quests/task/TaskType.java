package hardcorequesting.common.quests.task;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.task.icon.GetAdvancementTask;
import hardcorequesting.common.quests.task.icon.KillMobsTask;
import hardcorequesting.common.quests.task.icon.TameMobsTask;
import hardcorequesting.common.quests.task.icon.VisitLocationTask;
import hardcorequesting.common.quests.task.item.*;
import hardcorequesting.common.quests.task.reputation.HaveReputationTask;
import hardcorequesting.common.quests.task.reputation.KillReputationTask;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class TaskType<T extends QuestTask<?>> {
    public static final ResourceKey<Registry<TaskType<?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(HardcoreQuestingCore.ID, "task_type"));
    public static final DeferredRegister<TaskType<?>> REGISTER = DeferredRegister.create(HardcoreQuestingCore.ID, REGISTRY_KEY);
    
    public static final RegistrySupplier<TaskType<CheckBoxTask>> CHECKBOX = REGISTER.register("checkbox", () -> new TaskType<>("checkbox", CheckBoxTask::new));
    public static final RegistrySupplier<TaskType<ConsumeItemTask>> CONSUME = REGISTER.register("consume", () -> new TaskType<>("consume", ConsumeItemTask::new));
    public static final RegistrySupplier<TaskType<CraftingTask>> CRAFT = REGISTER.register("craft", () -> new TaskType<>("craft", CraftingTask::new));
    public static final RegistrySupplier<TaskType<VisitLocationTask>> LOCATION = REGISTER.register("location", () -> new TaskType<>("location", VisitLocationTask::new));
    public static final RegistrySupplier<TaskType<ConsumeItemQDSTask>> CONSUME_QDS = REGISTER.register("consume_qds", () -> new TaskType<>("consumeQDS", ConsumeItemQDSTask::new));
    public static final RegistrySupplier<TaskType<DetectItemTask>> DETECT = REGISTER.register("detect", () -> new TaskType<>("detect", DetectItemTask::new));
    public static final RegistrySupplier<TaskType<KillMobsTask>> KILL = REGISTER.register("kill", () -> new TaskType<>("kill", KillMobsTask::new));
    public static final RegistrySupplier<TaskType<TameMobsTask>> TAME = REGISTER.register("tame", () -> new TaskType<>("tame", TameMobsTask::new));
    public static final RegistrySupplier<TaskType<DeathTask>> DEATH = REGISTER.register("death", () -> new TaskType<>("death", DeathTask::new));
    public static final RegistrySupplier<TaskType<HaveReputationTask>> REPUTATION = REGISTER.register("reputation", () -> new TaskType<>("reputation", HaveReputationTask::new));
    public static final RegistrySupplier<TaskType<KillReputationTask>> REPUTATION_KILL = REGISTER.register("reputation_kill", () -> new TaskType<>("reputationKill", KillReputationTask::new));
    public static final RegistrySupplier<TaskType<GetAdvancementTask>> ADVANCEMENT = REGISTER.register("advancement", () -> new TaskType<>("advancement", GetAdvancementTask::new));
    public static final RegistrySupplier<TaskType<CompleteQuestTask>> COMPLETION = REGISTER.register("completion", () -> new TaskType<>("completion", CompleteQuestTask::new));
    public static final RegistrySupplier<TaskType<BreakBlockTask>> BLOCK_BREAK = REGISTER.register("block_break", () -> new TaskType<>("break", BreakBlockTask::new));
    public static final RegistrySupplier<TaskType<PlaceBlockTask>> BLOCK_PLACE = REGISTER.register("block_place", () -> new TaskType<>("place", PlaceBlockTask::new));
    
    private final String id;
    private final TaskConstructor<T> constructor;
    
    private TaskType(String id, TaskConstructor<T> constructor) {
        this.id = id;
        this.constructor = constructor;
    }
    
    public static List<? extends TaskType<?>> values() {
        return REGISTER.getRegistrar().entrySet().stream().map(Map.Entry::getValue).toList();
    }
    
    public String toDataName() {
        return String.valueOf(REGISTER.getRegistrar().getId(this));
    }
    
    public static TaskType<?> fromDataName(String str) {
        ResourceLocation id = ResourceLocation.tryParse(str);
        
        // If parsing fails (potentially due to upper-case characters),
        // try converting from the previous upper-case and modid-free format to the current format
        if (id == null)
            id = Objects.requireNonNull(ResourceLocation.tryBuild(HardcoreQuestingCore.ID, str.toLowerCase(Locale.ROOT)),
                    () -> "Not a valid task type name: %s".formatted(str));
        
        TaskType<?> type = REGISTER.getRegistrar().get(id);
        if (type != null)
            return type;
        else throw new IllegalArgumentException("Not a valid task type name: %s".formatted(str));
    }
    
    public T addTask(Quest quest) {
        T task = constructor.create(quest);
        task.updateId(quest.getTasks().size());
        quest.getTasks().add(task);
        SaveHelper.add(EditType.TASK_CREATE);
        return task;
    }
    
    public String getLangKeyDescription() {
        return "hqm.taskType." + id + ".desc";
    }
    
    public String getLangKeyName() {
        return "hqm.taskType." + id + ".title";
    }
    
    public FormattedText getDescription() {
        return Translator.translatable(getLangKeyDescription());
    }
    
    public FormattedText getName() {
        return Translator.translatable(getLangKeyName());
    }
    
    public interface TaskConstructor<T extends QuestTask<?>> {
        T create(Quest quest);
    }
}
