package hardcorequesting;

import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import hardcorequesting.config.ModConfig;
import hardcorequesting.items.ModItems;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.Iterator;

public class PlayerDeathEventListener {

    public PlayerDeathEventListener() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (event.entityLiving instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.entityLiving;
            QuestingData.getQuestingData(player).die(player);
            DeathType.onDeath(player, event.source);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerDropItemsOnDeath(PlayerDropsEvent event)
    {
        if (event.entityPlayer == null
                || event.entityPlayer instanceof FakePlayer
                || event.isCanceled()
                || event.entityPlayer.worldObj.getGameRules().getBoolean("keepInventory")
                || ModConfig.LOSE_QUEST_BOOK_ON_DEATH) {
            return;
        }

        ItemStack bookStack = new ItemStack(ModItems.book);

        Iterator<EntityItem> iter = event.drops.iterator();
        while (iter.hasNext()) {
            EntityItem entityItem = iter.next();
            ItemStack itemStack = entityItem.getEntityItem();
            if (itemStack != null && itemStack.isItemEqual(bookStack)) {
                event.entityPlayer.inventory.addItemStackToInventory(itemStack);
                iter.remove();
            }
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.Clone event) {
        if (event.entityPlayer == null
                || event.entityPlayer instanceof FakePlayer
                || !event.wasDeath
                || event.isCanceled()
                || event.entityPlayer.worldObj.getGameRules().getBoolean("keepInventory")
                || ModConfig.LOSE_QUEST_BOOK_ON_DEATH) {
            return;
        }

        if (event.original.inventory.hasItem(ModItems.book)) {
            ItemStack bookStack = new ItemStack(ModItems.book);
            for (ItemStack itemStack : event.original.inventory.mainInventory) {
                if (itemStack.isItemEqual(bookStack)) {
                    bookStack = itemStack.copy(); // Copy the actual stack
                    break;
                }
            }
            event.entityPlayer.inventory.addItemStackToInventory(bookStack);
        }
    }
}
