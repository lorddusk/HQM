package hardcorequesting.common.commands.sub;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import hardcorequesting.common.commands.CommandHandler;
import hardcorequesting.common.quests.QuestingDataManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class EnableSubCommand implements CommandHandler.SubCommand {
    @Override
    public ArgumentBuilder<CommandSourceStack, ?> build(LiteralArgumentBuilder<CommandSourceStack> builder) {
        return builder
                .requires(source -> source.hasPermission(4))
                .executes(context -> {
                    if (context.getSource().getLevel().getLevelData().isHardcore())
                        context.getSource().sendSuccess(Component.translatable("hqm.message.vanillaHardcoreOn"), true);
                    else
                        context.getSource().sendSuccess(Component.translatable(QuestingDataManager.getInstance().isHardcoreActive() ? "hqm.message.hardcoreAlreadyActivated" : "hqm.message.questHardcore"), true);
                    QuestingDataManager.getInstance().activateHardcore();
                    QuestingDataManager.getInstance().activateQuest(true);
                    if (context.getSource().getEntity() instanceof Player)
                        currentLives((Player) context.getSource().getEntity());
                    return 1;
                });
    }
}
