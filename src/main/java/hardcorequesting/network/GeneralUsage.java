package hardcorequesting.network;

import hardcorequesting.client.interfaces.GuiQuestBook;
import hardcorequesting.client.interfaces.GuiReward;
import hardcorequesting.network.message.GeneralUpdateMessage;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.quests.QuestingDataManager;
import hardcorequesting.quests.task.QuestTask;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

/**
 * A class to replace {@link hardcorequesting.client.ClientChange} completely one day.
 * The important difference is the message design. Instead of sending json as string
 * to the client this sends plain old NBT, which is much less message space and
 * way more robust in the Minecraft environment.
 * <p>
 * The IMessageHandler is registered for the Server and the Client, so packages aren't restricted to a side
 *
 * @author canitzp
 * @since 5.4.0
 */
public enum GeneralUsage {
    
    BOOK_OPEN {
        @Override
        public void receiveData(Player player, CompoundTag nbt) {
            Minecraft.getInstance().execute(() -> GuiQuestBook.displayGui(player, nbt.getBoolean("OP")));
        }
    },
    BOOK_SELECT_TASK {
        @Override
        public void receiveData(Player player, CompoundTag nbt) {
            QuestingData data = QuestingDataManager.getInstance().getQuestingData(player);
            data.selectedQuestId = nbt.getUUID("QuestId");
            data.selectedTask = nbt.getInt("TaskId");
        }
    },
    BAG_OPENED {
        @Override
        public void receiveData(Player player, CompoundTag nbt) {
            UUID groupId = nbt.getUUID("GroupId");
            int bag = nbt.getInt("Bag");
            int[] limits = nbt.getIntArray("Limits");
            
            GuiReward.open(player, groupId, bag, limits);
        }
    };
    
    // server -> client
    public static void sendOpenBook(Player player, boolean op) {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean("OP", op);
        BOOK_OPEN.sendMessageToPlayer(nbt, player);
    }
    
    // client -> server
    @Environment(EnvType.CLIENT)
    public static void sendBookSelectTaskUpdate(QuestTask task) {
        CompoundTag nbt = new CompoundTag();
        nbt.putUUID("QuestId", task.getParent().getQuestId());
        nbt.putInt("TaskId", task.getId());
        BOOK_SELECT_TASK.sendMessageToServer(nbt);
    }
    
    // server -> client
    public static void sendOpenBagUpdate(Player player, UUID groupId, int bag, int[] limits) {
        CompoundTag nbt = new CompoundTag();
        nbt.putUUID("GroupId", groupId);
        nbt.putInt("Bag", bag);
        nbt.putIntArray("Limits", limits);
        BAG_OPENED.sendMessageToPlayer(nbt, player);
    }
    
    public abstract void receiveData(Player player, CompoundTag nbt);
    
    @Environment(EnvType.CLIENT)
    public void sendMessageToServer(CompoundTag data) {
        NetworkManager.sendToServer(new GeneralUpdateMessage(Minecraft.getInstance().player, data, ordinal()));
    }
    
    public void sendMessageToPlayer(CompoundTag data, Player player) {
        if (player instanceof ServerPlayer) {
            NetworkManager.sendToPlayer(new GeneralUpdateMessage(player, data, ordinal()), (ServerPlayer) player);
        }
    }
    
}
