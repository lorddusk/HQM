package hardcorequesting.network;

import hardcorequesting.client.interfaces.GuiQuestBook;
import hardcorequesting.client.interfaces.GuiReward;
import hardcorequesting.items.ItemQuestBook;
import hardcorequesting.network.message.GeneralUpdateMessage;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.quests.task.QuestTask;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.UUID;

/**
 * A class to replace {@link hardcorequesting.client.ClientChange} completely one day.
 * The important difference is the message design. Instead of sending json as string
 * to the client this sends plain old NBT, which is much less message space and
 * way more robust in the Minecraft environment.
 *
 * The IMessageHandler is registered for the Server and the Client, so packages aren't restricted to a side
 *
 * @author canitzp
 * @since 5.4.0
 */
public enum GeneralUsage{
    
    BOOK_OPEN{
        @Override
        public void receiveData(EntityPlayer player, NBTTagCompound nbt){
            GuiQuestBook.displayGui(player, nbt.getBoolean("OP"));
        }
    },
    BOOK_SELECT_TASK{
        @Override
        public void receiveData(EntityPlayer player, NBTTagCompound nbt){
            QuestingData data = QuestingData.getQuestingData(player);
            data.selectedQuestId = nbt.getUniqueId("QuestId");
            data.selectedTask = nbt.getInteger("TaskId");
        }
    },
    BAG_OPENED{
        @Override
        public void receiveData(EntityPlayer player, NBTTagCompound nbt){
            UUID groupId = nbt.getUniqueId("GroupId");
            int bag = nbt.getInteger("Bag");
            int[] limits = nbt.getIntArray("Limits");
    
            GuiReward.open(player, groupId, bag, limits);
        }
    };
    
    // server -> client
    public static void sendOpenBook(EntityPlayer player, boolean op){
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setBoolean("OP", op);
        BOOK_OPEN.sendMessageToPlayer(nbt, player);
    }
    
    // client -> server
    @SideOnly(Side.CLIENT)
    public static void sendBookSelectTaskUpdate(QuestTask task){
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setUniqueId("QuestId", task.getParent().getQuestId());
        nbt.setInteger("TaskId", task.getId());
        BOOK_SELECT_TASK.sendMessageToServer(nbt);
    }
    
    // server -> client
    public static void sendOpenBagUpdate(EntityPlayer player, UUID groupId, int bag, int[] limits){
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setUniqueId("GroupId", groupId);
        nbt.setInteger("Bag", bag);
        nbt.setIntArray("Limits", limits);
        BAG_OPENED.sendMessageToPlayer(nbt, player);
    }
    
    public abstract void receiveData(EntityPlayer player, NBTTagCompound nbt);
    
    @SideOnly(Side.CLIENT)
    public void sendMessageToServer(NBTTagCompound data){
        NetworkManager.sendToServer(new GeneralUpdateMessage(Minecraft.getMinecraft().player, data, ordinal()));
    }
    
    public void sendMessageToPlayer(NBTTagCompound data, EntityPlayer player){
        if(player instanceof EntityPlayerMP){
            NetworkManager.sendToPlayer(new GeneralUpdateMessage(player, data, ordinal()), (EntityPlayerMP) player);
        }
    }
    
}
