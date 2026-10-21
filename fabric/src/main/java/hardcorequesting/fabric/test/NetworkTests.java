package hardcorequesting.fabric.test;

import com.google.gson.JsonObject;
import hardcorequesting.common.client.ClientChange;
import hardcorequesting.common.client.interfaces.GuiType;
import hardcorequesting.common.blocks.ModBlocks;
import hardcorequesting.common.network.GeneralUsage;
import hardcorequesting.common.network.IMessage;
import hardcorequesting.common.network.IMessageHandler;
import hardcorequesting.common.network.NetworkManager;
import hardcorequesting.common.network.PacketContext;
import hardcorequesting.common.network.message.*;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestLine;
import hardcorequesting.common.quests.QuestingData;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.team.Team;
import hardcorequesting.common.team.TeamAction;
import hardcorequesting.common.team.TeamError;
import hardcorequesting.common.team.TeamUpdateType;
import hardcorequesting.common.util.OPBookHelper;
import hardcorequesting.common.util.SyncUtil;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static hardcorequesting.fabric.test.BookFixtures.*;
import static hardcorequesting.fabric.test.TestScope.check;
import static hardcorequesting.fabric.test.TestScope.equal;
import static hardcorequesting.fabric.test.TestTeams.player;
import static hardcorequesting.fabric.test.TestTeams.teamOf;

/**
 * The messages between client and server: how they are encoded, and what the server does with the ones it receives.
 * <p>
 * Tests marked {@code required = false} describe the wanted behaviour for bad or forged messages, which the mod does not
 * have yet. They are reported, but do not fail the build.
 */
public class NetworkTests {

    /**
     * A context of a message that arrives at the server from the given player, with the task run right away.
     */
    private static PacketContext fromClient(ServerPlayer sender) {
        return new PacketContext() {
            @Override
            public Player getPlayer() {
                return sender;
            }

            @Override
            public Consumer<Runnable> getTaskQueue() {
                return Runnable::run;
            }

            @Override
            public boolean isClient() {
                return false;
            }
        };
    }

    /**
     * A context of a message that arrives at a client, with the task run right away.
     */
    private static PacketContext atClient(ServerPlayer receiver) {
        return new PacketContext() {
            @Override
            public Player getPlayer() {
                return receiver;
            }
            
            @Override
            public Consumer<Runnable> getTaskQueue() {
                return Runnable::run;
            }
            
            @Override
            public boolean isClient() {
                return true;
            }
        };
    }
    
    private static byte[] encode(IMessage message) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        message.toBytes(buf);
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        return bytes;
    }

    private static IMessage decode(Class<? extends IMessage> type, byte[] bytes, PacketContext context) throws ReflectiveOperationException {
        IMessage message = type.getDeclaredConstructor().newInstance();
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes));
        message.fromBytes(buf, context);
        check(buf.readableBytes() == 0, type.getSimpleName() + ": " + buf.readableBytes() + " bytes were not read");
        return message;
    }

    @SuppressWarnings("unchecked")
    private static java.util.Set<Class<? extends IMessage>> registeredMessages() throws ReflectiveOperationException {
        Field field = NetworkManager.class.getDeclaredField("PACKET_HANDLERS");
        field.setAccessible(true);
        return ((Map<Class<? extends IMessage>, ?>) field.get(null)).keySet();
    }

    private static Map<Class<? extends IMessage>, IMessage> samples(GameTestHelper helper, QuestLine line, ServerPlayer player) {
        Map<Class<? extends IMessage>, IMessage> samples = new LinkedHashMap<>();
        Quest quest = line.questSetsManager.quests.get(id("a"));
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, ModBlocks.blockTracker.get());
        BlockEntity blockEntity = helper.getBlockEntity(pos);
        CompoundTag nbt = new CompoundTag();
        nbt.putString("key", "value");

        samples.put(OpenGuiMessage.class, new OpenGuiMessage(GuiType.TRACKER, "data"));
        samples.put(PlayerDataSyncMessage.class, new PlayerDataSyncMessage(line, player));
        samples.put(TeamStatsMessage.class, new TeamStatsMessage(Stream.of(teamOf(player))));
        samples.put(TeamErrorMessage.class, new TeamErrorMessage(TeamError.USED_NAME));
        samples.put(QuestDataUpdateMessage.class, new QuestDataUpdateMessage(quest.getQuestId(), 1, quest.getQuestData(player.getUUID())));
        samples.put(DeathStatsMessage.class, new DeathStatsMessage(true));
        samples.put(TeamUpdateMessage.class, new TeamUpdateMessage(TeamUpdateType.FULL, "data"));
        samples.put(SoundMessage.class, new SoundMessage(ClientChange.SOUND, "1"));
        samples.put(LivesUpdate.class, new LivesUpdate(player.getUUID(), 5));
        samples.put(BlockSyncMessageClient.class, new BlockSyncMessageClient(blockEntity, 1, "{}"));
        samples.put(BlockSyncMessage.class, new BlockSyncMessage(blockEntity, 1, "{}"));
        samples.put(TeamMessage.class, new TeamMessage(TeamAction.CREATE, "Squad"));
        samples.put(ClientUpdateMessage.class, new ClientUpdateMessage(ClientChange.CLAIM_QUEST, "{}"));
        samples.put(OpActionMessage.class, new OpActionMessage(OPBookHelper.OpAction.RESET, "{}"));
        samples.put(SyncableTileMessage.class, new SyncableTileMessage(blockEntity));
        samples.put(GeneralUpdateMessage.class, new GeneralUpdateMessage(player, nbt, GeneralUsage.BOOK_SELECT_TASK.ordinal()));
        return samples;
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void everyMessageHasASample(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            QuestLine line = t.loadBook(bookOf(checkboxQuest("a")));
            Map<Class<? extends IMessage>, IMessage> samples = samples(helper, line, player(helper));

            for (Class<? extends IMessage> type : registeredMessages())
                check(samples.containsKey(type), "The message " + type.getSimpleName() + " has no sample in NetworkTests.samples. Add one so it gets tested.");
            check(registeredMessages().size() == samples.size(), "A sample is not a registered message");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void everyMessageSurvivesEncodingAndDecoding(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            QuestLine line = t.loadBook(bookOf(checkboxQuest("a")));
            ServerPlayer player = player(helper);

            for (Map.Entry<Class<? extends IMessage>, IMessage> sample : samples(helper, line, player).entrySet()) {
                String name = sample.getKey().getSimpleName();
                byte[] bytes = encode(sample.getValue());

                IMessage decoded = decode(sample.getKey(), bytes, fromClient(player));
                check(Arrays.equals(bytes, encode(decoded)), name + " changed when it was decoded and encoded again");
            }
        });
    }

    // Messages from clients

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void teamMessagesActForTheSendingPlayer(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            t.loadBook(bookOf(checkboxQuest("a")));
            ServerPlayer alice = player(helper), bob = player(helper);

            new TeamMessage.Handler().onMessage(new TeamMessage(TeamAction.CREATE, "Squad"), fromClient(alice));

            check(!teamOf(alice).isSingle() && teamOf(alice).getName().equals("Squad"), "The team should be created for the sender");
            check(teamOf(bob).isSingle(), "Other players are not affected");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void clientUpdatesActForTheSendingPlayer(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            QuestLine line = t.loadBook(bookOf(checkboxQuest("a"), checkboxQuest("b")));
            Quest a = line.questSetsManager.quests.get(id("a")), b = line.questSetsManager.quests.get(id("b"));
            ServerPlayer alice = player(helper), bob = player(helper);

            JsonObject checkbox = new JsonObject();
            checkbox.addProperty("quest", a.getQuestId().toString());
            checkbox.addProperty("task", 0);
            new ClientUpdateMessage.Handler().onMessage(new ClientUpdateMessage(ClientChange.COMPLETE_CHECKBOX_TASK, checkbox.toString()), fromClient(alice));
            check(a.isCompleted(alice.getUUID()), "The checkbox should be completed for the sender");
            check(!a.isCompleted(bob.getUUID()), "Other players are not affected");

            JsonObject select = new JsonObject();
            select.addProperty("parent", b.getQuestId().toString());
            select.addProperty("task", 0);
            new ClientUpdateMessage.Handler().onMessage(new ClientUpdateMessage(ClientChange.SELECT_QUEST, select.toString()), fromClient(bob));
            QuestingData data = QuestingDataManager.getInstance().getQuestingData(bob);
            equal(b.getQuestId(), data.selectedQuestId, "Selected quest of the sender");
            equal(0, data.selectedTask, "Selected task of the sender");
            check(QuestingDataManager.getInstance().getQuestingData(alice).selectedQuestId == null, "Other players are not affected");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void opActionsNeedOpPermission(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            QuestLine line = t.loadBook(bookOf(checkboxQuest("a")));
            Quest quest = line.questSetsManager.quests.get(id("a"));
            ServerPlayer sender = player(helper), subject = player(helper);
            PlayerList players = helper.getLevel().getServer().getPlayerList();
            OpActionMessage complete = (OpActionMessage) OPBookHelper.OpAction.QUEST_COMPLETION.build(quest, null, -1, subject.getUUID());

            new OpActionMessage.Handler().onMessage(complete, fromClient(sender));
            check(!quest.isCompleted(subject.getUUID()), "A player without permission can not complete quests for others");

            players.getOps().add(new ServerOpListEntry(sender.getGameProfile(), 4, false));   // the test server has no default operator level
            try {
                new OpActionMessage.Handler().onMessage(complete, fromClient(sender));
                check(quest.isCompleted(subject.getUUID()), "An operator should be able to complete the quest for another player, permission level " + helper.getLevel().getServer().getProfilePermissions(sender.getGameProfile()));

                new OpActionMessage.Handler().onMessage(complete, fromClient(sender));
                check(!quest.isCompleted(subject.getUUID()), "Doing it again should take the completion back");
            } finally {
                players.getOps().remove(sender.getGameProfile());
            }
        });
    }

    // Bad or forged messages

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void clientCanNotCompleteCheckboxOfLockedQuest(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            QuestLine line = t.loadBook(bookOf(checkboxQuest("a"), requiring(checkboxQuest("b"), "a")));
            Quest b = line.questSetsManager.quests.get(id("b"));
            ServerPlayer player = player(helper);

            JsonObject checkbox = new JsonObject();
            checkbox.addProperty("quest", b.getQuestId().toString());
            checkbox.addProperty("task", 0);
            ClientChange.COMPLETE_CHECKBOX_TASK.parse(player, checkbox.toString());

            check(!b.getTasks().get(0).isCompleted(player.getUUID()), "The task of a quest that is not open yet should not be completed");
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void clientCanNotSkipTasksWithCheckboxes(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            JsonObject quest = quest("a", 0, 0);
            com.google.gson.JsonArray tasks = new com.google.gson.JsonArray();
            tasks.add(taskSamples().get("checkbox").deepCopy());
            tasks.add(taskSamples().get("checkbox").deepCopy());
            quest.add("tasks", tasks);
            Quest a = t.loadBook(bookOf(quest)).questSetsManager.quests.get(id("a"));
            ServerPlayer player = player(helper);
            
            completeCheckbox(player, a, 1);
            check(!a.getTasks().get(1).isCompleted(player.getUUID()), "The second task should not be completed before the first");
            
            completeCheckbox(player, a, 0);
            check(a.getTasks().get(0).isCompleted(player.getUUID()), "The first task should be completed");
            check(!a.isCompleted(player.getUUID()), "The quest still has a task left");
            
            completeCheckbox(player, a, 1);
            check(a.isCompleted(player.getUUID()), "The quest should be completed with its last task");
        });
    }
    
    private static void completeCheckbox(ServerPlayer player, Quest quest, int task) {
        JsonObject request = new JsonObject();
        request.addProperty("quest", quest.getQuestId().toString());
        request.addProperty("task", task);
        ClientChange.COMPLETE_CHECKBOX_TASK.parse(player, request.toString());
    }
    
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void malformedClientUpdatesAreIgnored(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            t.loadBook(bookOf(checkboxQuest("a")));
            ServerPlayer player = player(helper);

            String[] payloads = {"not json", "[]", "{}", "{\"quest\": \"not a uuid\", \"task\": 0}", "{\"quest\": \"" + id("a") + "\", \"task\": \"x\"}",
                    "{\"blockPos\": 0, \"radius\": 3, \"trackerType\": 99}", "{\"parent\": \"" + id("a") + "\", \"task\": null}", "999", "-1"};
            for (ClientChange change : new ClientChange[]{ClientChange.UPDATE_TASK, ClientChange.CLAIM_QUEST, ClientChange.COMPLETE_CHECKBOX_TASK, ClientChange.SELECT_QUEST,
                    ClientChange.TRACKER_UPDATE, ClientChange.SOUND}) {
                for (String payload : payloads) {
                    try {
                        change.parse(player, payload);
                    } catch (RuntimeException e) {
                        throw new AssertionError(change + " failed for the payload " + payload + ": " + e);
                    }
                }
            }
        });
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void unknownActionsAreIgnored(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            t.loadBook(bookOf(checkboxQuest("a")));
            ServerPlayer player = player(helper);
            
            Map<Class<? extends IMessage>, IMessageHandler<IMessage, ?>> handlers = new LinkedHashMap<>();
            handlers.put(TeamMessage.class, (IMessageHandler) new TeamMessage.Handler());
            handlers.put(ClientUpdateMessage.class, (IMessageHandler) new ClientUpdateMessage.Handler());
            handlers.put(OpActionMessage.class, (IMessageHandler) new OpActionMessage.Handler());
            
            for (int action : new int[]{999, -1, Integer.MAX_VALUE}) {
                FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                buf.writeInt(action);
                buf.writeUtf("{}");
                for (Map.Entry<Class<? extends IMessage>, IMessageHandler<IMessage, ?>> handler : handlers.entrySet()) {
                    try {
                        IMessage message = handler.getKey().getDeclaredConstructor().newInstance();
                        message.fromBytes(new FriendlyByteBuf(buf.copy()), fromClient(player));
                        handler.getValue().onMessage(message, fromClient(player));
                    } catch (RuntimeException e) {
                        throw new AssertionError(handler.getKey().getSimpleName() + " failed for the action " + action + ", which does not exist: " + e);
                    }
                }
            }
        });
    }
    
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void unknownKindsAreIgnoredAtTheClient(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            t.loadBook(bookOf(checkboxQuest("a")));
            ServerPlayer player = player(helper);
            TeamError before = TeamError.latestError;
            
            Map<Class<? extends IMessage>, IMessageHandler<IMessage, ?>> handlers = new LinkedHashMap<>();
            handlers.put(OpenGuiMessage.class, (IMessageHandler) new OpenGuiMessage.Handler());
            handlers.put(SoundMessage.class, (IMessageHandler) new SoundMessage.Handler());
            handlers.put(TeamUpdateMessage.class, (IMessageHandler) new TeamUpdateMessage.Handler());
            handlers.put(TeamErrorMessage.class, (IMessageHandler) new TeamErrorMessage.Handler());
            
            for (int kind : new int[]{999, -1, Integer.MAX_VALUE}) {
                for (Map.Entry<Class<? extends IMessage>, IMessageHandler<IMessage, ?>> handler : handlers.entrySet()) {
                    FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                    buf.writeInt(kind);
                    if (handler.getKey() == TeamUpdateMessage.class)
                        SyncUtil.writeLargeString("{}", buf);
                    else if (handler.getKey() != TeamErrorMessage.class)
                        buf.writeUtf("{}");
                    try {
                        IMessage message = handler.getKey().getDeclaredConstructor().newInstance();
                        message.fromBytes(buf, atClient(player));
                        handler.getValue().onMessage(message, atClient(player));
                    } catch (ReflectiveOperationException | RuntimeException e) {
                        throw new AssertionError(handler.getKey().getSimpleName() + " failed for the kind " + kind + ", which does not exist: " + e);
                    }
                }
            }
            check(TeamError.latestError == before, "An unknown error should not replace the latest error");
        });
    }
    
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void clientCanNotRunClientOnlyGeneralUpdates(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            t.loadBook(bookOf(checkboxQuest("a")));
            ServerPlayer player = player(helper);
            
            CompoundTag book = new CompoundTag();
            book.putBoolean("OP", false);
            CompoundTag bag = new CompoundTag();
            bag.putUUID("GroupId", UUID.randomUUID());
            bag.putInt("Bag", 0);
            bag.putIntArray("Limits", new int[]{1});
            
            // These are sent from the server to open screens, the server must not run them when a client sends them
            for (GeneralUpdateMessage message : new GeneralUpdateMessage[]{
                    new GeneralUpdateMessage(player, book, GeneralUsage.BOOK_OPEN.ordinal()),
                    new GeneralUpdateMessage(player, bag, GeneralUsage.BAG_OPENED.ordinal())}) {
                try {
                    message.onMessage(message, fromClient(player));
                } catch (Throwable e) {
                    throw new AssertionError("A client could make the server run client code: " + e);
                }
            }
        });
    }
    
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void clientCanSelectATaskWithAGeneralUpdate(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            QuestLine line = t.loadBook(bookOf(checkboxQuest("a")));
            ServerPlayer player = player(helper);
            CompoundTag select = new CompoundTag();
            select.putUUID("QuestId", id("a"));
            select.putInt("TaskId", 0);
            GeneralUpdateMessage message = new GeneralUpdateMessage(player, select, GeneralUsage.BOOK_SELECT_TASK.ordinal());
            
            message.onMessage(message, fromClient(player));
            
            QuestingData data = QuestingDataManager.getInstance().getQuestingData(player);
            equal(id("a"), data.selectedQuestId, "Selected quest");
            equal(0, data.selectedTask, "Selected task");
        });
    }
    
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void serverCanNotChangeTheSelectionOfAClient(GameTestHelper helper) {
        TestScope.run(helper, t -> {
            t.loadBook(bookOf(checkboxQuest("a")));
            ServerPlayer player = player(helper);
            CompoundTag select = new CompoundTag();
            select.putUUID("QuestId", id("a"));
            select.putInt("TaskId", 0);
            GeneralUpdateMessage message = new GeneralUpdateMessage(player, select, GeneralUsage.BOOK_SELECT_TASK.ordinal());
            
            // Selecting is something clients tell the server, so arriving at a client it does nothing
            message.onMessage(message, atClient(player));
            
            check(QuestingDataManager.getInstance().getQuestingData(player).selectedQuestId == null, "A message to a client changed the selected quest");
        });
    }
}
