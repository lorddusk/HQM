package hardcorequesting.items;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.bag.BagTier;
import hardcorequesting.bag.Group;
import hardcorequesting.client.sounds.SoundHandler;
import hardcorequesting.client.sounds.Sounds;
import hardcorequesting.network.GeneralUsage;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;
import java.util.UUID;


public class BagItem extends Item {
    public static boolean displayGui;
    public BagTier tier;
    public int tierOrdinal;
    
    public BagItem(BagTier tier) {
        super(new Item.Settings().maxDamage(0).maxCount(64).group(HardcoreQuesting.HQMTab));
        this.tier = tier;
        this.tierOrdinal = tier.ordinal();
    }
    
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (!world.isClient) {
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
                            player.inventory.markDirty();
                            openClientInterface(player, group.getId(), tierOrdinal);
                            world.playSound(player, player.getSenseCenterPos(), Sounds.BAG.getSound(), SoundCategory.MASTER, 1, 1);
                            break;
                        } else {
                            rng -= weight;
                        }
                    }
                }
            }
            
            //doing this makes sure the inventory is updated on the client, and the creative mode thingy is already handled by the calling code
            //if(!player.capabilities.isCreativeMode) {
            stack.decrement(1);
            //}
            return TypedActionResult.success(stack);
        }
        
        return TypedActionResult.consume(stack);
    }
    
    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        
        tooltip.add(new LiteralText(tier.getColor() + tier.getName()));
    }
    
    private void openClientInterface(PlayerEntity player, UUID groupId, int bag) {
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
