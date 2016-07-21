package hardcorequesting.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import hardcorequesting.HardcoreQuesting;
import hardcorequesting.ModInformation;
import hardcorequesting.bag.BagTier;
import hardcorequesting.bag.Group;
import hardcorequesting.client.interfaces.GuiType;
import hardcorequesting.client.sounds.SoundHandler;
import hardcorequesting.client.sounds.Sounds;
import hardcorequesting.network.NetworkManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class ItemBag extends Item {
    public static boolean displayGui;

    public ItemBag() {
        super();
        this.setHasSubtypes(true);
        this.setMaxDurability(0);
        this.setMaxStackSize(64);
        this.setCreativeTab(HardcoreQuesting.HQMTab);
        this.setUnlocalizedName(ItemInfo.LOCALIZATION_START + ItemInfo.BAG_UNLOCALIZED_NAME);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer player, List tooltip, boolean extraInfo) {
        super.addInformation(itemstack, player, tooltip, extraInfo);

        int dmg = itemstack.getMetadata();
        if (dmg >= 0 && dmg < BagTier.values().length) {
            BagTier tier = BagTier.values()[dmg];
            tooltip.add(tier.getColor() + tier.getName());
        }
    }


    @Override
    @SuppressWarnings("unchecked")
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tabs, List stackList) {
        for (int i = 0; i < BagTier.values().length; i++) {
            stackList.add(new ItemStack(this, 1, i));
        }
    }

    @Override
    public ItemStack onItemRightClick(ItemStack item, World world, EntityPlayer player) {
        if (!world.isRemote) {
            int dmg = item.getMetadata();
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
                                SoundHandler.play(Sounds.BAG, player);
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
            --item.stackSize;
            //}
        }

        return item;
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
