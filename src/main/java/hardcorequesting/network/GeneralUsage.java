package hardcorequesting.network;

import hardcorequesting.client.interfaces.GuiQuestBook;
import hardcorequesting.client.interfaces.GuiReward;
import hardcorequesting.network.message.GeneralUpdateMessage;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.quests.task.QuestTask;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;

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
        public void receiveData(PlayerEntity player, CompoundTag nbt) {
            MinecraftClient.getInstance().execute(() -> GuiQuestBook.displayGui(player, nbt.getBoolean("OP")));
        }
    },
    BOOK_SELECT_TASK {
        @Override
        public void receiveData(PlayerEntity player, CompoundTag nbt) {
            QuestingData data = QuestingData.getQuestingData(player);
            data.selectedQuestId = nbt.getUuid("QuestId");
            data.selectedTask = nbt.getInt("TaskId");
        }
    },
    BAG_OPENED {
        @Override
        public void receiveData(PlayerEntity player, CompoundTag nbt) {
            UUID groupId = nbt.getUuid("GroupId");
            int bag = nbt.getInt("Bag");
            int[] limits = nbt.getIntArray("Limits");
            
            GuiReward.open(player, groupId, bag, limits);
        }
    };
    
    // server -> client
    public static void sendOpenBook(PlayerEntity player, boolean op) {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean("OP", op);
        BOOK_OPEN.sendMessageToPlayer(nbt, player);
    }
    
    // client -> server
    @Environment(EnvType.CLIENT)
    public static void sendBookSelectTaskUpdate(QuestTask task) {
        CompoundTag nbt = new CompoundTag();
        nbt.putUuid("QuestId", task.getParent().getQuestId());
        nbt.putInt("TaskId", task.getId());
        BOOK_SELECT_TASK.sendMessageToServer(nbt);
    }
    
    // server -> client
    public static void sendOpenBagUpdate(PlayerEntity player, UUID groupId, int bag, int[] limits) {
        CompoundTag nbt = new CompoundTag();
        nbt.putUuid("GroupId", groupId);
        nbt.putInt("Bag", bag);
        nbt.putIntArray("Limits", limits);
        BAG_OPENED.sendMessageToPlayer(nbt, player);
    }
    
    public abstract void receiveData(PlayerEntity player, CompoundTag nbt);
    
    @Environment(EnvType.CLIENT)
    public void sendMessageToServer(CompoundTag data) {
        NetworkManager.sendToServer(new GeneralUpdateMessage(MinecraftClient.getInstance().player, data, ordinal()));
    }
    
    public void sendMessageToPlayer(CompoundTag data, PlayerEntity player) {
        if (player instanceof ServerPlayerEntity) {
            NetworkManager.sendToPlayer(new GeneralUpdateMessage(player, data, ordinal()), (ServerPlayerEntity) player);
        }
    }
    
}
