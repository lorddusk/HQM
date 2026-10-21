package hardcorequesting.fabric.test;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs commands as a player with a given permission level, and keeps what the command said.
 */
public final class TestCommands {
    private TestCommands() {
    }

    /**
     * @param value    what the command returned
     * @param feedback the messages sent to the source
     * @param error    the message when the command did not run (unknown, no permission, wrong arguments), or the exception it ended with
     */
    public record Result(int value, List<Component> feedback, String error) {
        public boolean ok() {
            return error == null;
        }

        /**
         * @return what the command said, for the message of a failed check
         */
        public String described() {
            return feedback.stream().map(message -> message.getContents() instanceof TranslatableContents contents ? contents.getKey() : message.getString())
                    .toList().toString();
        }
        
        /**
         * @return whether one of the messages is the translation with this key
         */
        public boolean said(String translationKey) {
            return feedback.stream().anyMatch(message -> message.getContents() instanceof TranslatableContents contents && contents.getKey().equals(translationKey));
        }
    }

    public static Result run(ServerPlayer player, int permissionLevel, String command) {
        List<Component> feedback = new ArrayList<>();
        CommandSource capture = new CommandSource() {
            @Override
            public void sendSystemMessage(Component message) {
                feedback.add(message);
            }

            @Override
            public boolean acceptsSuccess() {
                return true;
            }

            @Override
            public boolean acceptsFailure() {
                return true;
            }

            @Override
            public boolean shouldInformAdmins() {
                return false;
            }
        };
        CommandSourceStack source = player.createCommandSourceStack().withSource(capture).withPermission(permissionLevel);
        CommandDispatcher<CommandSourceStack> dispatcher = player.server.getCommands().getDispatcher();
        try {
            return new Result(dispatcher.execute(command, source), feedback, null);
        } catch (CommandSyntaxException e) {
            return new Result(0, feedback, e.getMessage());
        } catch (RuntimeException e) {
            return new Result(0, feedback, e.toString());
        }
    }
}
