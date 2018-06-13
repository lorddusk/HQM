package hqm.quest;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;
import java.util.UUID;

/**
 * @author canitzp
 */
public class Quest {

    public String name;
    public UUID id, parent;
    private ItemStack icon;
    public List<String> desc;
    public List<ITask> tasks;
    public int posX, posY;
    public NBTTagCompound data;

    public Quest(String name, UUID id, UUID parentId, int xPos, int yPos, ItemStack icon, List<String> description, List<ITask> tasks) {
        this.name = name;
        this.id = id;
        this.parent = parentId;
        this.icon = icon;
        this.desc = description;
        this.posX = xPos;
        this.posY = yPos;
        this.tasks = tasks;
    }

    public ITask getNextTask(ITask task){
        int i = this.tasks.indexOf(task);
        if(this.tasks.size() > i + 1){
            return this.tasks.get(i + 1);
        }
        return null;
    }

    public ItemStack getIcon() {
        return icon != null ? icon : ItemStack.EMPTY;
    }

    public boolean isBig(){
        return this.data != null && this.data.getBoolean("big");
    }

    public boolean isDone(Team team){
        return team.hasSolved(this) || (this.data != null && this.data.getBoolean("solved"));
    }

    public boolean isParentDone(QuestLine questLine, Team team){
        return questLine.getQuest(this.parent) != null && questLine.getQuest(this.parent).isDone(team);
    }

    public boolean isOpen(QuestLine questLine, Team team){
        return this.parent == null || this.isDone(team) || this.isParentDone(questLine, team) || (this.data != null && this.data.getBoolean("open"));
    }

    public boolean isInvisible(QuestLine questLine, Team team){
        return !this.isOpen(questLine, team) && (this.data != null && this.data.getBoolean("invisible"));
    }
}
