package hqm.quest;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.UUID;

/**
 * @author canitzp
 */
// TODO add various update methods to check if a team has reached the tasks criteria
public interface ITask {

    void setId(UUID uuid);

    UUID getId();

    void init(NBTTagCompound data);

}
