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
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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

public class QuestBookItem extends Item {
    private static final String NBT_PLAYER = "UseAsPlayer";
    private final boolean enabled;
    
    public QuestBookItem(boolean enabled) {
        super(new Item.Properties().stacksTo(1));
        this.enabled = enabled;
    }
    
    public static ItemStack getOPBook(Player player) {
        ItemStack stack = new ItemStack(ModItems.enabledBook.get());
        CompoundTag nbt = stack.getOrCreateTagElement("hqm");
        nbt.putString(NBT_PLAYER, player.getUUID().toString());
        stack.addTagElement("hqm", nbt);
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
                    CompoundTag compound = stack.getTagElement("hqm");
                    if (compound != null && compound.contains(NBT_PLAYER)) {
                        String uuidS = compound.getString(NBT_PLAYER);
                        UUID uuid;
                        try {
                            uuid = UUID.fromString(uuidS);
                        } catch (IllegalArgumentException e) {
                            compound.remove(NBT_PLAYER);
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
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag context) {
        if (enabled) {
            CompoundTag compound = stack.getTagElement("hqm");
            if (compound != null && compound.contains(NBT_PLAYER)) {
                Player useAsPlayer = QuestingData.getPlayer(compound.getString(NBT_PLAYER));
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
