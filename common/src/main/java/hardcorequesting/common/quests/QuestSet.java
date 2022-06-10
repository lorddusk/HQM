package hardcorequesting.common.quests;

import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.reputation.ReputationBar;
import hardcorequesting.common.util.WrappedText;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class QuestSet {
    
    private String name;
    private WrappedText description;
    private List<FormattedText> cachedDescription;
    private Map<UUID, Quest> quests = new ConcurrentHashMap<>();
    private List<ReputationBar> reputationBars;
    private int id;
    
    public QuestSet(String name, WrappedText description) {
        this.name = name;
        this.description = description;
        this.reputationBars = new ArrayList<>();
        this.id = Quest.getQuestSets().size();
    }
    
    public Map<UUID, Quest> getQuests() {
        return quests;
    }
    
    public List<ReputationBar> getReputationBars() {
        validateBars();
        return reputationBars;
    }
    
    private void validateBars() {
        List<ReputationBar> toRemove = new ArrayList<>();
        for (ReputationBar reputationBar : reputationBars)
            if (!reputationBar.isValid())
                toRemove.add(reputationBar);
        reputationBars.removeAll(toRemove);
    }
    
    public String getName() {
        return name;
    }
    
    private static List<String> FORBIDDEN_SET_NAMES = Arrays.asList("sets", "reputations", "bags", "con", "prn", "aux", "nul", "com1", "com2", "com3", "com4", "com5", "com6", "com7", "com8", "com9", "com0", "lpt1", "lpt2", "lpt3", "lpt4", "lpt5", "lpt6", "lpt7", "lpt8", "lpt9", "lpt0");
    private static List<String> FORBIDDEN_SET_NAME_PIECES = Arrays.asList("<", ">", ":", "\"", "\\", "/", "|", "?", "*");
    
    public boolean setName(String name) {
        // Let's add some sanity checking.
        String test_name = name.toLowerCase().trim();
        
        if (FORBIDDEN_SET_NAMES.contains(test_name)) {
            return false;
        } else {
            for (String piece : FORBIDDEN_SET_NAME_PIECES) {
                if (test_name.contains(piece)) {
                    return false;
                }
            }
        }
        
        int inc = 1;
        
        String new_name = name;
        
        List<String> names = Quest.getQuestSets().stream().filter((q) -> q != this).map((q) -> q.getName().toLowerCase()).collect(Collectors.toList());
        
        while (names.contains(new_name.toLowerCase())) {
            new_name = String.format("%s%d", name, inc++);
            
            if (inc >= 20) return false;
        }
        
        this.name = new_name;
        return true;
    }
    
    public String getFilename() {
        return name.replaceAll(" ", "_");
    }
    
    public String getName(int i) {
        return (i + 1) + ". " + name;
    }
    
    @Environment(EnvType.CLIENT)
    public List<FormattedText> getDescription(GuiBase gui) {
        if (cachedDescription == null) {
            cachedDescription = gui.getLinesFromText(getDescription(), 0.7F, 130);
        }
        
        return cachedDescription;
    }
    
    public boolean isEnabled(Player player) {
        return isEnabled(player, new HashMap<>(), new HashMap<>());
    }
    
    public boolean isEnabled(Player player, Map<Quest, Boolean> isVisibleCache, Map<Quest, Boolean> isLinkFreeCache) {
        if (quests.isEmpty()) return false;
        
        for (Quest quest : quests.values()) {
            if (quest.isEnabled(player, isVisibleCache, isLinkFreeCache)) {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean isCompleted(Player player) {
        if (quests.isEmpty()) return false;
        
        for (Quest quest : quests.values()) {
            if (!quest.isCompleted(player)) {
                return false;
            }
        }
        
        return true;
    }
    
    public void removeQuest(Quest quest) {
        quests.remove(quest.getQuestId());
    }
    
    public void addQuest(Quest quest) {
        quests.put(quest.getQuestId(), quest);
    }
    
    public void removeRepBar(ReputationBar repBar) {
        reputationBars.remove(repBar);
    }
    
    public void addRepBar(ReputationBar repBar) {
        if (repBar == null) return;
        reputationBars.add(repBar);
    }
    
    public int getCompletedCount(Player player) {
        return getCompletedCount(player, new HashMap<>(), new HashMap<>());
    }
    
    public int getCompletedCount(Player player, Map<Quest, Boolean> isVisibleCache, Map<Quest, Boolean> isLinkFreeCache) {
        int count = 0;
        for (Quest quest : quests.values()) {
            if (quest.isCompleted(player) && quest.isEnabled(player, isVisibleCache, isLinkFreeCache)) {
                count++;
            }
        }
        
        return count;
    }
    
    public MutableComponent getDescription() {
        return description.getText();
    }
    
    public WrappedText getRawDescription() {
        return description;
    }
    
    public void setDescription(WrappedText description) {
        this.description = description;
        cachedDescription = null;
    }
    
    public int getId() {
        return id;
    }
    
    public void decreaseId() {
        id--;
    }
}
