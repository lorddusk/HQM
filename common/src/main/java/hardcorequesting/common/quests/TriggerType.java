package hardcorequesting.common.quests;

import hardcorequesting.common.util.Translator;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;

import java.util.Optional;
import java.util.UUID;

public enum TriggerType {
    NONE("none", false, false) {
        @Override
        public boolean isQuestVisible(Quest quest, UUID playerId) {
            return true;
        }
        
        @Override
        public Optional<FormattedText> getMessage(Quest quest) {
            return Optional.empty();
        }
    },
    QUEST_TRIGGER("quest", false, true) {
        @Override
        public boolean isQuestVisible(Quest quest, UUID playerId) {
            return false;
        }
        
    },
    TASK_TRIGGER("task", true, true) {
        @Override
        public boolean isQuestVisible(Quest quest, UUID playerId) {
            if (quest.getTriggerTasks() >= quest.getTasks().size()) {
                return quest.isCompleted(playerId);
            } else {
                return quest.getTasks().get(quest.getTriggerTasks() - 1).isCompleted(playerId);
            }
        }
        
        @Override
        public Optional<FormattedText> getMessage(Quest quest) {
            return Optional.of(getName().append(" (" + quest.getTriggerTasks() + ")").withStyle(ChatFormatting.GOLD));
        }
    },
    ANTI_TRIGGER("anti", false, false) {
        @Override
        public boolean isQuestVisible(Quest quest, UUID playerId) {
            return quest.isEnabled(playerId, false);
        }
    };
    
    private String id;
    private boolean useTaskCount;
    private boolean workAsInvisible;
    
    TriggerType(String id, boolean useTaskCount, boolean workAsInvisible) {
        this.id = id;
        this.useTaskCount = useTaskCount;
        this.workAsInvisible = workAsInvisible;
    }
    
    public MutableComponent getName() {
        return Translator.translatable("hqm.trigger." + id + ".title");
    }
    
    public FormattedText getDescription() {
        return Translator.translatable("hqm.trigger." + id + ".desc");
    }
    
    public boolean isUseTaskCount() {
        return useTaskCount;
    }
    
    public boolean doesWorkAsInvisible() {
        return workAsInvisible;
    }
    
    public abstract boolean isQuestVisible(Quest quest, UUID playerId);
    
    public Optional<FormattedText> getMessage(Quest quest) {
        return Optional.of(getName().withStyle(ChatFormatting.GOLD));
    }
}
