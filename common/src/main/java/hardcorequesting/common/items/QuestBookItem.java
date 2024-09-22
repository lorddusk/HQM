package hardcorequesting.common.items;

import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.event.EventTrigger;
import hardcorequesting.common.network.GeneralUsage;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestingData;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.team.PlayerEntry;
import hardcorequesting.common.util.HQMUtil;
import hardcorequesting.common.util.Translator;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class QuestBookItem extends Item {
    private static final String NBT_PLAYER = "UseAsPlayer";
    private final boolean enabled;
    
    public QuestBookItem(boolean enabled) {
        super(new Item.Properties().stacksTo(1));
        this.enabled = enabled;
    }
    
    public static ItemStack getOPBook(Player player) {
        ItemStack stack = new ItemStack(ModItems.enabledBook.get());
        //TODO replace with our own data component
        CustomData.update(DataComponents.CUSTOM_DATA, stack, compoundTag -> {
            CompoundTag hqmTag = new CompoundTag();
            hqmTag.putString(NBT_PLAYER, player.getUUID().toString());
            compoundTag.put("hqm", hqmTag);
        });
        return stack;
    }
    
    @NotNull
    @Override
    @SuppressWarnings("deprecation")
    public InteractionResultHolder<ItemStack> use(Level world, Player player, @NotNull InteractionHand hand) {
        if (world.isClientSide && Quest.isEditing && !HQMUtil.isSinglePlayerOnly()) {
            Quest.setEditMode(false);
        }

        if (!world.isClientSide && player instanceof ServerPlayer) {
            ItemStack stack = player.getItemInHand(hand);
            QuestingDataManager questingData = QuestingDataManager.getInstance();
            if (!questingData.isQuestActive()) {
                player.sendSystemMessage(Translator.translatable("hqm.message.noQuestYet"));
            } else {
                if (enabled) {
                    CompoundTag stackTag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                    CompoundTag hqmTag = stackTag.getCompound("hqm");
                    if (hqmTag.contains(NBT_PLAYER, Tag.TAG_STRING)) {
                        String uuidS = hqmTag.getString(NBT_PLAYER);
                        UUID uuid;
                        try {
                            uuid = UUID.fromString(uuidS);
                        } catch (IllegalArgumentException e) {
                            hqmTag.remove(NBT_PLAYER);
                            if (hqmTag.isEmpty())
                                stackTag.remove("hqm");
                            CustomData.set(DataComponents.CUSTOM_DATA, stack, stackTag);
                            return InteractionResultHolder.fail(stack);
                        }
                        if (questingData.hasData(uuid)) {
                            if (HardcoreQuestingCore.getServer().getProfilePermissions(player.getGameProfile()) >= 4) {
                                Player subject = QuestingData.getPlayer(uuid);
                                if (subject instanceof ServerPlayer) {
                                    EventTrigger.instance().onBookOpening(new EventTrigger.BookOpeningEvent(player.getUUID(), true, false));
                                    PlayerEntry entry = questingData.getQuestingData(subject).getTeam().getEntry(subject.getUUID());
                                    if (entry != null) {
                                        GeneralUsage.sendOpenBook(player, true);
                                    } else {
                                        player.sendSystemMessage(Translator.translatable("hqm.message.bookNoEntry"));
                                    }
                                }
                            } else {
                                player.sendSystemMessage(Translator.translatable("hqm.message.bookNoPermission"));
                            }
                        } else {
                            player.sendSystemMessage(Translator.translatable("hqm.message.bookNoData"));
                        }
                    }
                } else {
                    EventTrigger.instance().onBookOpening(new EventTrigger.BookOpeningEvent(player.getUUID(), false, true));
                    PlayerEntry entry = questingData.getQuestingData(player).getTeam().getEntry(player.getUUID());
                    if (entry != null) {
                        GeneralUsage.sendOpenBook(player, false);
                    } else {
                        player.sendSystemMessage(Component.translatable("hqm.message.bookNoPlayer"));
                    }
                }
            }
            return InteractionResultHolder.success(stack);
        }
        return super.use(world, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext tooltipContext, List<Component> tooltip, TooltipFlag tooltipFlag) {
        if (enabled) {
            CompoundTag hqmTag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).getUnsafe().getCompound("hqm");
            if (hqmTag.contains(NBT_PLAYER)) {
                Player useAsPlayer = QuestingData.getPlayer(hqmTag.getString(NBT_PLAYER));
                tooltip.add(Translator.translatable("item.hqm:quest_book_1.useAs", useAsPlayer == null ? "INVALID" : useAsPlayer.getScoreboardName()));
            } else
                tooltip.add(Translator.translatable("item.hqm:quest_book_1.invalid").withStyle(ChatFormatting.RED));
        }
    }
    
    @Override
    public boolean isFoil(ItemStack stack) {
        return enabled;
    }
}
