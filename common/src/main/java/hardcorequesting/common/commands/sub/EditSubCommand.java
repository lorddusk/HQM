package hardcorequesting.common.commands.sub;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import hardcorequesting.common.commands.CommandHandler;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.util.HQMUtil;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;

import java.util.Arrays;

public class EditSubCommand implements CommandHandler.SubCommand {
    @Override
    public ArgumentBuilder<CommandSourceStack, ?> build(LiteralArgumentBuilder<CommandSourceStack> builder) {
        return builder
                .requires(source -> source.hasPermission(4))
                .executes(context -> {
                    if (HQMUtil.isGameSingleplayer()) {
                        boolean newEditModeState = !Quest.canQuestsBeEdited();
                        Quest.setEditMode(newEditModeState);
                        if (newEditModeState) {
                            context.getSource().sendSuccess(Translator.translatable("hqm.command.editMode.enabled"), false);
                        } else {
                            context.getSource().sendSuccess(Translator.translatable("hqm.command.editMode.disabled"), false);
                        }
                    } else {
                        context.getSource().sendSuccess(Translator.translatable("hqm.command.editMode.server"), false);
                        Quest.setEditMode(false);
                    }
                    
                    if (Arrays.stream(SaveHelper.list).anyMatch(element -> element.count > 0))
                        context.getSource().sendSuccess(Translator.text("You still have unsaved changes! Caution!", ChatFormatting.RED), false);
                    return 1;
                });
    }
}
