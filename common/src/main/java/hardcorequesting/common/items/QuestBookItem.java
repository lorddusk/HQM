package hardcorequesting.common.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import hardcorequesting.common.event.EventTrigger;
import hardcorequesting.common.network.GeneralUsage;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestingData;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.team.PlayerEntry;
import hardcorequesting.common.util.HQMUtil;
import hardcorequesting.common.util.Translator;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public final class QuestBookItem extends Item {
    private final boolean enabled;

    public QuestBookItem(boolean enabled) {
        super(new Item.Properties().stacksTo(1));
        this.enabled = enabled;
    }

    public static ItemStack getOPBook(Player player) {
        ItemStack stack = new ItemStack(ModItems.enabledBook.get());
        stack.set(ModItems.DataComponents.USE_AS_PLAYER.get(), new UseAsPlayer(player.getUUID(), player.getGameProfile().getName()));
        return stack;
    }

    @NotNull
    @Override
    @SuppressWarnings("deprecation")
    public InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {

        if (level.isClientSide && Quest.isEditing && !HQMUtil.isSinglePlayerOnly()) {
            Quest.setEditMode(false);
        }

        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.success(stack);
        }

        QuestingDataManager questingData = QuestingDataManager.getInstance();
        if (!questingData.isQuestActive()) {
            serverPlayer.sendSystemMessage(Translator.translatable("hqm.message.noQuestYet"));
            return InteractionResultHolder.fail(stack);
        }

        if (enabled) {
            return useOpBook(serverPlayer, stack, questingData);
        } else {
            return useRegularBook(serverPlayer, stack, questingData);
        }
    }

    private static InteractionResultHolder<ItemStack> useRegularBook(ServerPlayer player, ItemStack stack, QuestingDataManager questingData) {

        EventTrigger.instance().onBookOpening(new EventTrigger.BookOpeningEvent(player.getUUID(), false, true));
        PlayerEntry entry = questingData.getQuestingData(player).getTeam().getEntry(player.getUUID());
        if (entry == null) {
            player.sendSystemMessage(Component.translatable("hqm.message.bookNoPlayer"));
            return InteractionResultHolder.fail(stack);
        }
        GeneralUsage.sendOpenBook(player, false);
        return InteractionResultHolder.success(stack);
    }

    private InteractionResultHolder<ItemStack> useOpBook(ServerPlayer player, ItemStack stack, QuestingDataManager questingData) {

        UseAsPlayer useAsPlayer = stack.get(ModItems.DataComponents.USE_AS_PLAYER.get());
        if (useAsPlayer == null) {
            return InteractionResultHolder.fail(stack);
        }
        if (!questingData.hasData(useAsPlayer.uuid())) {
            player.sendSystemMessage(Translator.translatable("hqm.message.bookNoData"));
            return InteractionResultHolder.fail(stack);
        }
        if (player.server.getProfilePermissions(player.getGameProfile()) < 4) {
            player.sendSystemMessage(Translator.translatable("hqm.message.bookNoPermission"));
            return InteractionResultHolder.fail(stack);
        }
        Player subject = QuestingData.getPlayer(useAsPlayer.uuid());
        if (!(subject instanceof ServerPlayer)) {
            return InteractionResultHolder.fail(stack);
        }
        EventTrigger.instance().onBookOpening(new EventTrigger.BookOpeningEvent(player.getUUID(), true, false));
        PlayerEntry entry = questingData.getQuestingData(subject).getTeam().getEntry(subject.getUUID());
        if (entry == null) {
            player.sendSystemMessage(Translator.translatable("hqm.message.bookNoEntry"));
            return InteractionResultHolder.fail(stack);
        }
        GeneralUsage.sendOpenBook(player, true);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext tooltipContext, List<Component> tooltip, TooltipFlag tooltipFlag) {
        if (enabled) {
            UseAsPlayer useAsPlayer = stack.get(ModItems.DataComponents.USE_AS_PLAYER.get());
            if (useAsPlayer != null) {
                tooltip.add(Translator.translatable("item.hqm:quest_book_1.useAs", useAsPlayer.name()));
            } else {
                tooltip.add(Translator.translatable("item.hqm:quest_book_1.invalid").withStyle(ChatFormatting.RED));
            }
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return enabled;
    }

    public record UseAsPlayer(UUID uuid, String name) {

        public static final Codec<UseAsPlayer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                UUIDUtil.STRING_CODEC.fieldOf("uuid").forGetter(UseAsPlayer::uuid),
                ExtraCodecs.PLAYER_NAME.fieldOf("name").forGetter(UseAsPlayer::name)
        ).apply(instance, UseAsPlayer::new));

        public static final StreamCodec<ByteBuf, UseAsPlayer> STREAM_CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC,
                UseAsPlayer::uuid,
                ByteBufCodecs.stringUtf8(16),
                UseAsPlayer::name,
                UseAsPlayer::new
        );

    }
}
