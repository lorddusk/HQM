package hardcorequesting.common.event;

import hardcorequesting.common.death.DeathType;
import hardcorequesting.common.quests.QuestingDataManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class PlayerDeathEventListener {
    public static PlayerDeathEventListener instance;
    
    public PlayerDeathEventListener() {
        instance = this;
    }
    
    public void onLivingDeath(LivingEntity entity, DamageSource source) {
        if (entity instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) entity;
            QuestingDataManager.getInstance().getQuestingData(player).die(player);
            DeathType.onDeath(player, source);
        }
    }

//    @SubscribeEvent(priority = EventPriority.HIGHEST)
//    public void onPlayerDropItemsOnDeath(PlayerDropsEvent event) {
//        if (event.getEntityPlayer() == null
//            || event.getEntityPlayer() instanceof FakePlayer
//            || event.isCanceled()
//            || event.getEntityPlayer().world.getGameRules().getBoolean("keepInventory")
//            || HQMConfig.LOSE_QUEST_BOOK) {
//            return;
//        }
//        
//        Iterator<EntityItem> iter = event.getDrops().iterator();
//        while (iter.hasNext()) {
//            EntityItem entityItem = iter.next();
//            ItemStack stack = entityItem.getItem();
//            if (!stack.isEmpty() && stack.getItem().equals(ModItems.book)) {
//                event.getEntityPlayer().inventory.addItemStackToInventory(stack);
//                iter.remove();
//            }
//        }
//    }

//    @SubscribeEvent
//    public void onPlayerRespawn(PlayerEvent.Clone event) {
//        if (event.getEntityPlayer() == null
//            || event.getEntityPlayer() instanceof FakePlayer
//            || !event.isWasDeath()
//            || event.isCanceled()
//            || event.getEntityPlayer().world.getGameRules().getBoolean("keepInventory")
//            || HQMConfig.LOSE_QUEST_BOOK) {
//            return;
//        }
//        
//        if (event.getOriginal().inventory.hasItemStack(new ItemStack(ModItems.book))) {
//            ItemStack bookStack = new ItemStack(ModItems.book);
//            for (ItemStack stack : event.getOriginal().inventory.mainInventory) {
//                if (bookStack.isItemEqual(stack)) {
//                    bookStack = stack.copy(); // Copy the actual stack
//                    break;
//                }
//            }
//            event.getEntityPlayer().inventory.addItemStackToInventory(bookStack);
//        }
//    }
}
