package hardcorequesting.items;

import hardcorequesting.ModInformation;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import hardcorequesting.HardcoreQuesting;
import hardcorequesting.bag.BagTier;
import hardcorequesting.bag.Group;
import hardcorequesting.client.sounds.SoundHandler;
import hardcorequesting.client.sounds.Sounds;
import hardcorequesting.config.ModConfig;
import hardcorequesting.network.DataBitHelper;
import hardcorequesting.network.DataWriter;
import hardcorequesting.network.PacketHandler;
import hardcorequesting.network.PacketId;
//import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;

import static hardcorequesting.items.ItemInfo.HEART_ICONS;


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

    @SuppressWarnings("unchecked")
    @Override
    public void addInformation(ItemStack itemstack, EntityPlayer player, List tooltip, boolean extraInfo) {
        super.addInformation(itemstack, player, tooltip, extraInfo);

        int dmg = itemstack.getItemDamage();
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
    public ActionResult<ItemStack> onItemRightClick(ItemStack item, World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote) {
            int dmg = item.getItemDamage();
            if (dmg >= 0 && dmg < BagTier.values().length) {
                int totalWeight = 0;
                for (Group group : Group.getGroups()) {
                    if (group.isValid(player)) {
                        totalWeight += group.getTier().getWeights()[dmg];
                    }
                }
                if (totalWeight > 0) {
                    int rng = (int) (Math.random() * totalWeight);
                    List<Group> groups = Group.getGroups();
                    for (int i = 0; i < groups.size(); i++) {
                        Group group = groups.get(i);
                        if (group.isValid(player)) {
                            int weight = group.getTier().getWeights()[dmg];
                            if (rng < weight) {
                                group.open(player);
                                player.inventory.markDirty();
                                openClientInterface(player, i, dmg);
                                // @todo
//                                world.playSound(player, player.getPosition(), Sounds.BAG.getSound(), SoundCategory.BLOCKS, 1, 1);
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

        return new ActionResult<>(EnumActionResult.SUCCESS, item);
    }

    private void openClientInterface(EntityPlayer player, int id, int bag) {
        DataWriter dw = PacketHandler.getWriter(PacketId.BAG_INTERFACE);
        dw.writeData(id, DataBitHelper.GROUP_COUNT);
        dw.writeData(bag, DataBitHelper.BAG_TIER);
        for (Group group : Group.getGroups()) {
            if (group.getLimit() != 0) {
                dw.writeData(group.getRetrievalCount(player), DataBitHelper.LIMIT);
            }
        }

        if (ItemBag.displayGui) {
            PacketHandler.sendToRawPlayer(player, dw);
        }
        SoundHandler.play(Sounds.BAG, player);
    }
}
