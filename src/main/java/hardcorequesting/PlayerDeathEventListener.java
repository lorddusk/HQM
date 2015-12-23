package hardcorequesting;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import hardcorequesting.config.ModConfig;
import hardcorequesting.items.ModItems;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;

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
                || event.entityPlayer.worldObj.getGameRules().getGameRuleBooleanValue("keepInventory")
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
}
