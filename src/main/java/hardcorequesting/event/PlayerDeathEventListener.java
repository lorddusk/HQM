package hardcorequesting.event;

import hardcorequesting.config.ModConfig;
import hardcorequesting.death.DeathType;
import hardcorequesting.items.ModItems;
import hardcorequesting.quests.QuestingData;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Iterator;

@Mod.EventBusSubscriber
public class PlayerDeathEventListener {

    public PlayerDeathEventListener() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.getEntityLiving();
            QuestingData.getQuestingData(player).die(player);
            DeathType.onDeath(player, event.getSource());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerDropItemsOnDeath(PlayerDropsEvent event) {
        if (event.getEntityPlayer() == null
                || event.getEntityPlayer() instanceof FakePlayer
                || event.isCanceled()
                || event.getEntityPlayer().worldObj.getGameRules().getBoolean("keepInventory")
                || ModConfig.LOSE_QUEST_BOOK_ON_DEATH) {
            return;
        }

        Iterator<EntityItem> iter = event.getDrops().iterator();
        while (iter.hasNext()) {
            EntityItem entityItem = iter.next();
            ItemStack stack = entityItem.getEntityItem();
            if (stack != null && stack.getItem().equals(ModItems.book)) {
                event.getEntityPlayer().inventory.addItemStackToInventory(stack);
                iter.remove();
            }
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.Clone event) {
        if (event.getEntityPlayer() == null
                || event.getEntityPlayer() instanceof FakePlayer
                || !event.isWasDeath()
                || event.isCanceled()
                || event.getEntityPlayer().worldObj.getGameRules().getBoolean("keepInventory")
                || ModConfig.LOSE_QUEST_BOOK_ON_DEATH) {
            return;
        }

        if (event.getOriginal().inventory.hasItemStack(new ItemStack(ModItems.book))) {
            ItemStack bookStack = new ItemStack(ModItems.book);
            for (ItemStack stack : event.getOriginal().inventory.mainInventory) {
                if (bookStack.isItemEqual(stack)) {
                    bookStack = stack.copy(); // Copy the actual stack
                    break;
                }
            }
            event.getEntityPlayer().inventory.addItemStackToInventory(bookStack);
        }
    }
}
