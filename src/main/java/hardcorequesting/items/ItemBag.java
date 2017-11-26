package hardcorequesting.items;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.bag.BagTier;
import hardcorequesting.bag.Group;
import hardcorequesting.client.interfaces.GuiType;
import hardcorequesting.client.sounds.SoundHandler;
import hardcorequesting.client.sounds.Sounds;
import hardcorequesting.network.NetworkManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class ItemBag extends Item {

    public static boolean displayGui;

    public ItemBag() {
        super();
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setMaxStackSize(64);
        this.setCreativeTab(HardcoreQuesting.HQMTab);
        this.setRegistryName(ItemInfo.BAG_UNLOCALIZED_NAME);
        this.setUnlocalizedName(ItemInfo.LOCALIZATION_START + ItemInfo.BAG_UNLOCALIZED_NAME);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote) {
            ItemStack stack = player.getHeldItem(hand);
            int dmg = stack.getItemDamage();
            if (dmg >= 0 && dmg < BagTier.values().length) {
                int totalWeight = 0;
                for (Group group : Group.getGroups().values()) {
                    if (group.isValid(player)) {
                        totalWeight += group.getTier().getWeights()[dmg];
                    }
                }
                if (totalWeight > 0) {
                    int rng = (int) (Math.random() * totalWeight);
                    for (Group group : Group.getGroups().values()) {
                        if (group.isValid(player)) {
                            int weight = group.getTier().getWeights()[dmg];
                            if (rng < weight) {
                                group.open(player);
                                player.inventory.markDirty();
                                openClientInterface(player, group.getId(), dmg);
                                world.playSound(player, player.getPosition(), Sounds.BAG.getSound(), SoundCategory.MASTER, 1, 1);
                                break;
                            } else {
                                rng -= weight;
                            }
                        }
                    }
                }
            }

            //doing this makes sure the inventory is updated on the client, and the creative mode thingy is already handled by the calling code
            //if(!player.capabilities.isCreativeMode) {
            stack.shrink(1);
            //}
        }

        return super.onItemRightClick(world, player, hand);
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        int dmg = stack.getItemDamage();
        if (dmg >= 0 && dmg < BagTier.values().length) {
            BagTier tier = BagTier.values()[dmg];
            tooltip.add(tier.getColor() + tier.getName());
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tabs, NonNullList<ItemStack> stackList) {
        if (isInCreativeTab(tabs)) {
            for (int i = 0; i < BagTier.values().length; i++) {
                stackList.add(new ItemStack(this, 1, i));
            }
        }
    }

    private void openClientInterface(EntityPlayer player, String id, int bag) {
        List<String> data = new ArrayList<>();
        data.add(id);
        data.add("" + bag);
        data.addAll(Group.getGroups().values().stream()
                .filter(group -> group.getLimit() != 0)
                .map(group -> group.getRetrievalCount(player) + "")
                .collect(Collectors.toList()));
        if (ItemBag.displayGui && player instanceof EntityPlayerMP)
            NetworkManager.sendToPlayer(GuiType.BAG.build(data.toArray(new String[data.size()])), (EntityPlayerMP) player);
        SoundHandler.play(Sounds.BAG, player);
    }
}
