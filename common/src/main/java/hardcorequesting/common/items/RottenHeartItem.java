package hardcorequesting.common.items;

import hardcorequesting.common.client.sounds.SoundHandler;
import hardcorequesting.common.client.sounds.Sounds;
import hardcorequesting.common.death.DeathType;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.util.Translator;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

@MethodsReturnNonnullByDefault
public final class RottenHeartItem extends Item {

    public RottenHeartItem(Properties properties) {

        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            QuestingDataManager questingDataManager = QuestingDataManager.getInstance();

            if (!questingDataManager.isHardcoreActive()) {
                player.sendSystemMessage(Translator.translatable("hqm.message.noHardcoreYet"));
            } else {
                SoundHandler.play(Sounds.ROTTEN, player);
                player.sendSystemMessage(Translator.translatable("hqm.message.eatRottenHearth"));
                questingDataManager.getQuestingData(player).removeLifeAndSendMessage(player);
                DeathType.HQM.onDeath(player);

                if (!player.getAbilities().instabuild)
                    stack.grow(1);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext tooltipContext, List<Component> tooltip, TooltipFlag tooltipFlag) {

        tooltip.add(Translator.translatable("item.hqm:hearts_rottenheart.tooltip"));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
