package hardcorequesting.common.proxies;

import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.client.interfaces.graphic.task.*;
import hardcorequesting.common.network.PacketContext;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestTicker;
import hardcorequesting.common.quests.task.TaskType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class ClientProxy extends CommonProxy {
    
    @Override
    public void init() {
        super.init();
        Quest.clientTicker = new QuestTicker();
        HardcoreQuestingCore.platform.registerOnClientTick(minecraftClient -> Quest.clientTicker.tick(minecraftClient.level, true));
    
        TaskGraphics.register(TaskType.CONSUME, (task, playerId, questBook) -> ItemTaskGraphic.createConsumeGraphic(task, playerId, questBook, true));
        TaskGraphics.register(TaskType.CRAFT, ItemTaskGraphic::new);
        TaskGraphics.register(TaskType.LOCATION, LocationTaskGraphic::new);
        TaskGraphics.register(TaskType.CONSUME_QDS, (task, playerId, questBook) -> ItemTaskGraphic.createConsumeGraphic(task, playerId, questBook, false));
        TaskGraphics.register(TaskType.DETECT, ItemTaskGraphic::createDetectGraphic);
        TaskGraphics.register(TaskType.KILL, KillMobsTaskGraphic::new);
        TaskGraphics.register(TaskType.TAME, TameMobsTaskGraphic::new);
        TaskGraphics.register(TaskType.DEATH, DeathTaskGraphic::new);
        TaskGraphics.register(TaskType.REPUTATION, ReputationTaskGraphic::new);
        TaskGraphics.register(TaskType.REPUTATION_KILL, KillReputationTaskGraphic::new);
        TaskGraphics.register(TaskType.ADVANCEMENT, AdvancementTaskGraphic::new);
        TaskGraphics.register(TaskType.COMPLETION, CompleteQuestTaskGraphic::new);
        TaskGraphics.register(TaskType.BLOCK_BREAK, ItemTaskGraphic::new);
        TaskGraphics.register(TaskType.BLOCK_PLACE, ItemTaskGraphic::new);
    }
    
    @Override
    public boolean isClient() {
        return true;
    }
    
    @Override
    public boolean isServer() {
        return false;
    }
    
    @Override
    public Player getPlayer(PacketContext ctx) {
        return ctx.isClient() ? Minecraft.getInstance().player : super.getPlayer(ctx);
    }
}
