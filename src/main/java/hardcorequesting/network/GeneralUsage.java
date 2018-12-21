package hardcorequesting.network;

import hardcorequesting.network.message.GeneralUpdateMessage;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.quests.task.QuestTask;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A class to replace {@link hardcorequesting.client.ClientChange} completely one day.
 * The important difference is the message design. Instead of sending json as string
 * to the client this sends plain old NBT, which is much less message space and
 * way more robust in the minecraft environment
 */
public enum GeneralUsage{
    
    BOOK_SELECT_TASK{
        @Override
        public void receiveData(EntityPlayer player, NBTTagCompound nbt, Side side){
            QuestingData data = QuestingData.getQuestingData(player);
            data.selectedQuestId = nbt.getUniqueId("QuestId");
            data.selectedTask = nbt.getInteger("TaskId");
        }
    };
    
    @SideOnly(Side.CLIENT)
    public static void sendBookSelectTaskUpdate(QuestTask task){
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setUniqueId("QuestId", task.getParent().getQuestId());
        nbt.setInteger("TaskId", task.getId());
        BOOK_SELECT_TASK.sendMessageToServer(nbt);
    }
    
    public abstract void receiveData(EntityPlayer player, NBTTagCompound nbt, Side side);
    
    @SideOnly(Side.CLIENT)
    public void sendMessageToServer(NBTTagCompound data){
        NetworkManager.sendToServer(new GeneralUpdateMessage(Minecraft.getMinecraft().player, data, ordinal()));
    }
    
}
