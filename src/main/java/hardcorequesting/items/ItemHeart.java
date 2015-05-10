package hardcorequesting.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import hardcorequesting.DeathType;
import hardcorequesting.HardcoreQuesting;
import hardcorequesting.QuestingData;
import hardcorequesting.Util;
import hardcorequesting.client.sounds.SoundHandler;
import hardcorequesting.client.sounds.Sounds;
import hardcorequesting.config.ModConfig;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.List;

public class ItemHeart extends Item {

    public ItemHeart() {
        super();
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setMaxStackSize(64);
        this.setCreativeTab(HardcoreQuesting.HQMTab);
        this.setUnlocalizedName(ItemInfo.LOCALIZATION_START + ItemInfo.HEART_UNLOCALIZED_NAME);
    }

    @SideOnly(Side.CLIENT)
    private IIcon[] icons;

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister icon) {
        pickIcon(icon);
    }

    private void pickIcon(IIconRegister icon) {
            icons = new IIcon[ItemInfo.HEART_ICONS.length];

            for (int i = 0; i < icons.length; i++) {
                icons[i] = icon.registerIcon(ItemInfo.TEXTURE_LOCATION + ":" + ItemInfo.HEART_ICONS[i]);
            }
    }

    public static final String[] names = ItemInfo.HEART_ICONS;

    private String line;

    public String getUnlocalizedName(ItemStack par1ItemStack) {
        int i = MathHelper.clamp_int(par1ItemStack.getItemDamage(), 0, 15);
        return super.getUnlocalizedName() + "_" + names[i];
    }

    public IIcon getIconFromDamage(int par1) {
        return icons[par1];
    }

    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs par2CreativeTabs, List par3List) {
        for (int x = 0; x < ItemInfo.HEART_ICONS.length; x++) {
            par3List.add(new ItemStack(this, 1, x));
        }
    }


    @Override
    public ItemStack onItemRightClick(ItemStack item, World world, EntityPlayer player) {
        if (!world.isRemote) {

            if (item.getItemDamage() == 3) {
                if (!QuestingData.isHardcoreActive()) {
                    line = "Hardcore Mode isn't enabled yet. use '/hqm hardcore' to enable it.";
                    player.addChatComponentMessage(Util.getChatComponent(line, EnumChatFormatting.WHITE));
                } else if (QuestingData.getQuestingData(player).getRawLives() < ModConfig.MAXLIVES) {
                    QuestingData.getQuestingData(player).addLives(player, 1);
                    line = "You have added 1 to your total life.";
                    player.addChatComponentMessage(Util.getChatComponent(line, EnumChatFormatting.WHITE));
                    int lives = QuestingData.getQuestingData(player).getLives();
                    line = "You have " + lives + " remaining.";
                    player.addChatComponentMessage(Util.getChatComponent(line, EnumChatFormatting.WHITE));
                    SoundHandler.play(Sounds.LIFE, player);
                    if (!player.capabilities.isCreativeMode) {
                        --item.stackSize;

                    }
                } else {
                    line = "You already have maximum lives.";
                    player.addChatComponentMessage(Util.getChatComponent(line, EnumChatFormatting.WHITE));
                }
            }
            if (item.getItemDamage() == 4) {
                if (!QuestingData.isHardcoreActive()) {
                    line = "Hardcore Mode isn't enabled yet. use '/hqm hardcore' to enable it.";
                    player.addChatComponentMessage(Util.getChatComponent(line, EnumChatFormatting.WHITE));
                } else {
                    SoundHandler.play(Sounds.ROTTEN, player);
                    line = "Why did you eat a rotten heart?";
                    player.addChatComponentMessage(Util.getChatComponent(line, EnumChatFormatting.WHITE));
                    QuestingData.getQuestingData(player).removeLifeAndSendMessage(player);
                    DeathType.HQM.onDeath(player);

                    if (!player.capabilities.isCreativeMode) {
                        --item.stackSize;
                    }
                }

            }
            return item;

        }
        return item;
    }

    @Override
    public void addInformation(ItemStack item, EntityPlayer player, List lst, boolean extraInfo) {
        super.addInformation(item, player, lst, extraInfo);

        if (item.getItemDamage() == 3) {
            lst.add("Consume to get an extra life");
            if (ModConfig.ROTTIMER) {
                NBTTagCompound tagCompound = item.getTagCompound();
                if (tagCompound == null) {
                    tagCompound = new NBTTagCompound();
                    item.setTagCompound(tagCompound);
                }
                if (tagCompound.hasKey("RotTime")) {
                    int rot = tagCompound.getInteger("RotTime");
                    int maxRot = tagCompound.getInteger("MaxRot");
                    float percentage = (float) ((rot * 100) / maxRot);
                    lst.add("Current freshness : " + percentage + " %");
                }
            }
        }
        if (item.getItemDamage() == 4) {
            lst.add("Rotten Heart. Do Not Eat");
        }
    }

    @Override
    public boolean hasEffect(ItemStack item, int pass) {
        return item.getItemDamage() == 3 || item.getItemDamage() == 4;
    }

    @Override
    public void onUpdate(ItemStack itemStack, World world, Entity entity, int par4, boolean par5) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer entityPlayer = (EntityPlayer) entity;
            if (itemStack.getItemDamage() == 3 && ModConfig.ROTTIMER) {
                NBTTagCompound tagCompound = itemStack.getTagCompound();
                if (tagCompound == null) {
                    tagCompound = new NBTTagCompound();
                    itemStack.setTagCompound(tagCompound);
                }
                if (!tagCompound.hasKey("RotTime")) {
                    int rot = (ModConfig.MAXROT * 20);
                    tagCompound.setInteger("MaxRot", rot);
                    tagCompound.setInteger("RotTime", rot);
                } else {
                    int newRot = tagCompound.getInteger("RotTime");
                    if (newRot <= 0) {
                        itemStack.setItemDamage(4);
                        line = "One or more of your hearts has just decade into a Rotten Heart.";
                        entityPlayer.addChatComponentMessage(Util.getChatComponent(line, EnumChatFormatting.WHITE));
                    } else {
                        tagCompound.setInteger("RotTime", newRot - 1);
                    }
                }
            }
        }
    }
}
