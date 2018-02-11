package hqm.quest;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

/**
 * @author canitzp
 */
public class Quest {

    public String name;
    public UUID id, parentId;
    public ItemStack icon;
    public List<String> description;
    public List<ITask> tasks;
    public int xPos, yPos;

    public Quest(String name, UUID id, UUID parentId, int xPos, int yPos, ItemStack icon, List<String> description, List<ITask> tasks) {
        this.name = name;
        this.id = id;
        this.parentId = parentId;
        this.icon = icon;
        this.description = description;
        this.xPos = xPos;
        this.yPos = yPos;
        this.tasks = tasks;
    }

    public ITask getNextTask(ITask task){
        int i = this.tasks.indexOf(task);
        if(this.tasks.size() > i + 1){
            return this.tasks.get(i + 1);
        }
        return null;
    }
}
