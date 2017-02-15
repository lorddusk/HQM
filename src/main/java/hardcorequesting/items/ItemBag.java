package hardcorequesting.items;

import hardcorequesting.HardcoreQuesting;
import hardcorequesting.ModInformation;
import hardcorequesting.bag.BagTier;
import hardcorequesting.bag.Group;
import hardcorequesting.client.interfaces.GuiType;
import hardcorequesting.client.sounds.SoundHandler;
import hardcorequesting.client.sounds.Sounds;
import hardcorequesting.network.NetworkManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


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

    @SideOnly(Side.CLIENT)
    public void initModel() {
        for (int i = 0; i < BagTier.values().length; i++) {
            ModelLoader.setCustomModelResourceLocation(this, i, new ModelResourceLocation(ModInformation.ASSET_PREFIX + ":" + ItemInfo.BAG_UNLOCALIZED_NAME, "inventory"));
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote) {
            stack = player.getHeldItem(hand);
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
            stack.stackSize = stack.stackSize - 1;
            //}
        }

        return super.onItemRightClick(stack, world, player, hand);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean extraInfo) {
        super.addInformation(stack, player, tooltip, extraInfo);

        int dmg = stack.getItemDamage();
        if (dmg >= 0 && dmg < BagTier.values().length) {
            BagTier tier = BagTier.values()[dmg];
            tooltip.add(tier.getColor() + tier.getName());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tabs, List<ItemStack> stackList) {
        for (int i = 0; i < BagTier.values().length; i++) {
            stackList.add(new ItemStack(this, 1, i));
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
