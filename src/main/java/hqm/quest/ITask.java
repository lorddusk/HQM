package hqm.quest;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author canitzp
 */
// TODO add various update methods to check if a team has reached the task criteria
public interface ITask {

    void init(Quest quest, World world, @Nullable NBTTagCompound additionalData);

    List<String> getTaskDescription();

    // may be empty, depending on the task like mod kill types
    NonNullList<ItemStack> getInput();

    NonNullList<ItemStack> getReward();

}
