package hardcorequesting.items;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.client.interfaces.GuiColor;
import hardcorequesting.event.EventTrigger;
import hardcorequesting.network.GeneralUsage;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestLine;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.team.PlayerEntry;
import hardcorequesting.util.HQMUtil;
import hardcorequesting.util.Translator;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TranslatableComponent;
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
    private boolean enabled;
    
    public QuestBookItem(boolean enabled) {
        super(new Item.Properties().stacksTo(1).tab(HardcoreQuesting.HQMTab));
        this.enabled = enabled;
    }
    
    public static ItemStack getOPBook(Player player) {
        ItemStack stack = new ItemStack(ModItems.enabledBook);
        CompoundTag nbt = stack.getOrCreateTagElement("hqm");
        nbt.putString(NBT_PLAYER, player.getUUID().toString());
        stack.addTagElement("hqm", nbt);
        return stack;
    }
    
    @NotNull
    @Override
    @SuppressWarnings("deprecation")
    public InteractionResultHolder<ItemStack> use(Level world, Player player, @NotNull InteractionHand hand) {
        if (world.isClientSide && Quest.isEditing && !HQMUtil.isGameSingleplayer()) {
            Quest.setEditMode(false);
        }
        
        if (!world.isClientSide && Quest.isEditing && HQMUtil.isGameSingleplayer() && QuestLine.doServerSync) {
            player.sendMessage(Translator.translatable("hqm.command.editMode.disableSync").withStyle(Style.EMPTY.withColor(ChatFormatting.RED).withBold(true)), Util.NIL_UUID);
            Quest.setEditMode(false);
        }
        
        if (!world.isClientSide && player instanceof ServerPlayer) {
            ItemStack stack = player.getItemInHand(hand);
            if (!QuestingData.isQuestActive()) {
                player.sendMessage(Translator.translatable("hqm.message.noQuestYet"), Util.NIL_UUID);
            } else {
                if (stack.getItem() == ModItems.enabledBook) {
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
                        if (QuestingData.hasData(uuid)) {
                            if (HardcoreQuesting.getServer().getProfilePermissions(player.getGameProfile()) >= 4) {
                                Player subject = QuestingData.getPlayer(uuid);
                                if (subject instanceof ServerPlayer) {
                                    EventTrigger.instance().onEvent(new EventTrigger.BookOpeningEvent(player.getScoreboardName(), true, false));
                                    PlayerEntry entry = QuestingData.getQuestingData(subject).getTeam().getEntry(subject.getUUID());
                                    if (entry != null) {
                                        entry.setBookOpen(true);
                                        GeneralUsage.sendOpenBook(player, true);
                                    } else {
                                        player.sendMessage(Translator.translatable("hqm.message.bookNoEntry"), Util.NIL_UUID);
                                    }
                                }
                            } else {
                                player.sendMessage(Translator.translatable("hqm.message.bookNoPermission"), Util.NIL_UUID);
                            }
                        } else {
                            player.sendMessage(Translator.translatable("hqm.message.bookNoData"), Util.NIL_UUID);
                        }
                    }
                } else {
                    EventTrigger.instance().onEvent(new EventTrigger.BookOpeningEvent(player.getScoreboardName(), false, true));
                    PlayerEntry entry = QuestingData.getQuestingData(player).getTeam().getEntry(player.getUUID());
                    if (entry != null) {
                        entry.setBookOpen(true);
                        GeneralUsage.sendOpenBook(player, false);
                    } else {
                        player.sendMessage(new TranslatableComponent("hqm.message.bookNoPlayer"), Util.NIL_UUID);
                    }
                }
            }
            return InteractionResultHolder.success(stack);
        }
        return super.use(world, player, hand);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag context) {
        if (stack.getItem() == ModItems.enabledBook) {
            CompoundTag compound = stack.getTagElement("hqm");
            if (compound != null && compound.contains(NBT_PLAYER)) {
                Player useAsPlayer = QuestingData.getPlayer(compound.getString(NBT_PLAYER));
                tooltip.add(Translator.translatable("item.hqm:quest_book_1.useAs", useAsPlayer == null ? "INVALID" : useAsPlayer.getScoreboardName()));
            } else
                tooltip.add(Translator.translatable("item.hqm:quest_book_1.invalid").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(GuiColor.RED.getHexColor() & 0xFFFFFF))));
        }
    }
    
    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.getItem() == ModItems.enabledBook;
    }
}
