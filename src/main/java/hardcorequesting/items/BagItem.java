package hardcorequesting.items;

import hardcorequesting.bag.BagTier;
import hardcorequesting.bag.Group;
import hardcorequesting.client.sounds.SoundHandler;
import hardcorequesting.client.sounds.Sounds;
import hardcorequesting.network.GeneralUsage;
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
        ItemStack stack = player.getItemInHand(hand);
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
            
            //doing this makes sure the inventory is updated on the client, and the creative mode thingy is already handled by the calling code
            //if(!player.capabilities.isCreativeMode) {
            stack.shrink(1);
            //}
            return InteractionResultHolder.success(stack);
        }
        
        return InteractionResultHolder.consume(stack);
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
