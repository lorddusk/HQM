package hardcorequesting.commands.sub;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import hardcorequesting.commands.CommandHandler;
import hardcorequesting.quests.QuestingData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;

public class EnableSubCommand implements CommandHandler.SubCommand {
    @Override
    public ArgumentBuilder<CommandSourceStack, ?> build(LiteralArgumentBuilder<CommandSourceStack> builder) {
        return builder
                .requires(source -> source.hasPermission(4))
                .executes(context -> {
                    if (context.getSource().getLevel().getLevelData().isHardcore())
                        context.getSource().sendSuccess(new TranslatableComponent("hqm.message.vanillaHardcoreOn"), true);
                    else
                        context.getSource().sendSuccess(new TranslatableComponent(QuestingData.isHardcoreActive() ? "hqm.message.hardcoreAlreadyActivated" : "hqm.message.questHardcore"), true);
                    QuestingData.activateHardcore();
                    QuestingData.activateQuest(true);
                    if (context.getSource().getEntity() instanceof Player)
                        currentLives((Player) context.getSource().getEntity());
                    return 1;
                });
    }
}
