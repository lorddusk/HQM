package hardcorequesting.quests;


import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import hardcorequesting.client.interfaces.GuiBase;
import hardcorequesting.client.interfaces.GuiQuestBook;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.List;

public class QuestSet {
    private String name;
    private String description;
    private List<String> cachedDescription;
    private List<Quest> quests;
    private int id;


    public QuestSet(String name, String description) {
        this.name = name;
        this.description = description;
        quests = new ArrayList<Quest>();
        this.id = Quest.getQuestSets().size();
    }

    public List<Quest> getQuests() {
        return quests;
    }

    public String getName() {
        return name;
    }

    public String getName(int i) {
        return (i + 1) + ". " + name;
    }

    @SideOnly(Side.CLIENT)
    public List<String> getDescription(GuiBase gui) {
        if (cachedDescription == null) {
            cachedDescription = gui.getLinesFromText(description, 0.7F, 130);
        }

        return cachedDescription;
    }

    public boolean isEnabled(EntityPlayer player) {
        if (quests.isEmpty()) return false;

        for (Quest quest : quests) {
            if (quest.isEnabled(player)) {
                return true;
            }
        }

        return false;
    }

    public boolean isCompleted(EntityPlayer player) {
        if (quests.isEmpty()) return false;

        for (Quest quest : quests) {
            if (!quest.isCompleted(player)) {
                return false;
            }
        }

        return true;
    }

    public void removeQuest(Quest quest) {
        quests.remove(quest);
    }

    public void addQuest(Quest quest) {
        quests.add(quest);
    }

    public int getCompletedCount(EntityPlayer player) {
        int count = 0;
        for (Quest quest : quests) {
            if (quest.isCompleted(player) && quest.isEnabled(player)) {
                count++;
            }
        }

        return count;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        cachedDescription = null;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void decreaseId() {
        id--;
    }


}
