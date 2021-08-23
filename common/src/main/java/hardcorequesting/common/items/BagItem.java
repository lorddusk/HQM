package hardcorequesting.common.items;

import hardcorequesting.common.bag.BagTier;
import hardcorequesting.common.bag.Group;
import hardcorequesting.common.client.sounds.SoundHandler;
import hardcorequesting.common.client.sounds.Sounds;
import hardcorequesting.common.network.GeneralUsage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;
import java.util.UUID;


public class BagItem extends Item {
    public static boolean displayGui;
    public BagTier tier;
    public int tierOrdinal;
    
    public BagItem(BagTier tier) {
        super(new Item.Properties().durability(0).stacksTo(64).tab(ModCreativeTabs.HQMTab));
        this.tier = tier;
        this.tierOrdinal = tier.ordinal();
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (!world.isClientSide) {
            int totalWeight = 0;
            for (Group group : Group.getGroups().values()) {
                if (group.isValid(player)) {
                    totalWeight += group.getTier().getWeights()[tierOrdinal];
                }
            }
            if (totalWeight > 0) {
                int rng = (int) (Math.random() * totalWeight);
                for (Group group : Group.getGroups().values()) {
                    if (group.isValid(player)) {
                        int weight = group.getTier().getWeights()[tierOrdinal];
                        if (rng < weight) {
                            group.open(player);
                            player.inventory.setChanged();
                            openClientInterface(player, group.getId(), tierOrdinal);
                            world.playSound(player, player.blockPosition(), Sounds.BAG.getSound(), SoundSource.MASTER, 1, 1);
                            break;
                        } else {
                            rng -= weight;
                        }
                    }
                }
            }
            
            ItemStack stack = player.getItemInHand(hand).copy();
            stack.shrink(1);
            player.setItemInHand(hand, stack);
    
            return InteractionResultHolder.success(stack);
        }
        
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag context) {
        super.appendHoverText(stack, world, tooltip, context);
        
        tooltip.add(new TextComponent(tier.getColor() + tier.getName()));
    }
    
    private void openClientInterface(Player player, UUID groupId, int bag) {
        /* legacy code
        List<String> data = new ArrayList<>();
        data.add(groupId.toString());
        data.add("" + bag);
        data.addAll(Group.getGroups().values().stream()
                .filter(group -> group.getLimit() != 0)
                .map(group -> group.getRetrievalCount(player) + "")
                .collect(Collectors.toList()));
        if (ItemBag.displayGui && player instanceof ServerPlayerEntity)
            NetworkManager.sendToPlayer(GuiType.BAG.build(data.toArray(new String[data.size()])), (ServerPlayerEntity) player);
            */
        GeneralUsage.sendOpenBagUpdate(player, groupId, bag, ArrayUtils.toPrimitive(Group.getGroups().values().stream()
                .filter(group -> group.getLimit() != 0)
                .map(group -> group.getRetrievalCount(player))
                .toArray(Integer[]::new)));
        SoundHandler.play(Sounds.BAG, player);
    }
}
