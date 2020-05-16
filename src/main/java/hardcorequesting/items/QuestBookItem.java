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
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class QuestBookItem extends Item {
    private static final String NBT_PLAYER = "UseAsPlayer";
    private boolean enabled;
    
    public QuestBookItem(boolean enabled) {
        super(new Item.Settings().maxCount(1).group(HardcoreQuesting.HQMTab));
        this.enabled = enabled;
    }
    
    public static ItemStack getOPBook(PlayerEntity player) {
        ItemStack stack = new ItemStack(ModItems.enabledBook);
        CompoundTag nbt = stack.getOrCreateSubTag("hqm");
        nbt.putString(NBT_PLAYER, player.getUuid().toString());
        stack.putSubTag("hqm", nbt);
        return stack;
    }
    
    @NotNull
    @Override
    @SuppressWarnings("deprecation")
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, @NotNull Hand hand) {
        if (world.isClient && Quest.isEditing && !HQMUtil.isGameSingleplayer()) {
            Quest.setEditMode(false);
        }
        
        if (!world.isClient && Quest.isEditing && HQMUtil.isGameSingleplayer() && QuestLine.doServerSync) {
            player.sendMessage(new TranslatableText("hqm.command.editMode.disableSync").setStyle(new Style().setColor(Formatting.RED).setBold(true)));
            Quest.setEditMode(false);
        }
        
        if (!world.isClient && player instanceof ServerPlayerEntity) {
            ItemStack stack = player.getStackInHand(hand);
            if (!QuestingData.isQuestActive()) {
                player.sendMessage(Translator.translateToIChatComponent("hqm.message.noQuestYet"));
            } else {
                if (stack.getItem() == ModItems.enabledBook) {
                    CompoundTag compound = stack.getSubTag("hqm");
                    if (compound != null && compound.contains(NBT_PLAYER)) {
                        String uuidS = compound.getString(NBT_PLAYER);
                        UUID uuid;
                        try {
                            uuid = UUID.fromString(uuidS);
                        } catch (IllegalArgumentException e) {
                            compound.remove(NBT_PLAYER);
                            return TypedActionResult.fail(stack);
                        }
                        if (QuestingData.hasData(uuid)) {
                            // TODO CommandHandler
//                            if (CommandHandler.isOwnerOrOp(player)) {
                            if (HardcoreQuesting.getServer().getPermissionLevel(player.getGameProfile()) >= 4) {
                                PlayerEntity subject = QuestingData.getPlayer(uuid);
                                if (subject instanceof ServerPlayerEntity) {
                                    EventTrigger.instance().onEvent(new EventTrigger.BookOpeningEvent(player.getEntityName(), true, false));
                                    PlayerEntry entry = QuestingData.getQuestingData(subject).getTeam().getEntry(subject.getUuid());
                                    if (entry != null) {
                                        entry.setBookOpen(true);
                                        GeneralUsage.sendOpenBook(player, true);
                                    } else {
                                        player.sendMessage(new TranslatableText("hqm.message.bookNoEntry"));
                                    }
                                }
                                //player.addChatComponentMessage(Translator.translateToIChatComponent("hqm.message.alreadyEditing"));
                            } else {
                                player.sendMessage(Translator.translateToIChatComponent("hqm.message.bookNoPermission"));
                            }
                        } else {
                            player.sendMessage(new TranslatableText("hqm.message.bookNoData"));
                        }
                    }
                } else {
                    EventTrigger.instance().onEvent(new EventTrigger.BookOpeningEvent(player.getEntityName(), false, true));
                    PlayerEntry entry = QuestingData.getQuestingData(player).getTeam().getEntry(player.getUuid());
                    if (entry != null) {
                        entry.setBookOpen(true);
                        GeneralUsage.sendOpenBook(player, false);
                    } else {
                        player.sendMessage(new TranslatableText("hqm.message.bookNoPlayer"));
                    }
                }
            }
            return TypedActionResult.success(stack);
        }
        return super.use(world, player, hand);
    }
    
    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        if (stack.getItem() == ModItems.enabledBook) {
            CompoundTag compound = stack.getSubTag("hqm");
            if (compound != null && compound.contains(NBT_PLAYER)) {
                PlayerEntity useAsPlayer = QuestingData.getPlayer(compound.getString(NBT_PLAYER));
                tooltip.add(Translator.translateToIChatComponent("item.hqm:quest_book_1.useAs", useAsPlayer == null ? "INVALID" : useAsPlayer.getEntityName()));
            } else
                tooltip.add(new LiteralText(GuiColor.RED + Translator.translate("item.hqm:quest_book_1.invalid")));
        }
    }
    
    @Override
    public boolean hasEnchantmentGlint(ItemStack stack) {
        return stack.getItem() == ModItems.enabledBook;
    }
}
