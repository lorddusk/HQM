package hqm.quest;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import javax.annotation.Nullable;
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
    public List<ITask> task;
    public int xPos, yPos;

    public Quest(String name, UUID id, UUID parentId, ItemStack icon, List<String> description, int xPos, int yPos) {
        this.name = name;
        this.id = id;
        this.parentId = parentId;
        this.icon = icon;
        this.description = description;
        this.xPos = xPos;
        this.yPos = yPos;
    }

    // returning null here prevents the quest from being added to a questline, quests that depend on this one are now parentless and free from the the beginning
    public Quest initTasks(World world, String taskClass, @Nullable NBTTagCompound additionalData){
        try {
            Class c = Class.forName(taskClass);
            if(c != null && ITask.class.isAssignableFrom(c)){
                ITask task = (ITask) c.newInstance();
                task.init(this, world, additionalData);
            }
        } catch (Exception e) {
            System.out.println("Exception while reading a task! We kill the Quest to prevent further problems! " + e.getMessage());
            return null;
        }
        return this;
    }
}
