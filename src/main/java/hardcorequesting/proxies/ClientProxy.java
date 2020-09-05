package hardcorequesting.proxies;

import hardcorequesting.client.sounds.Sounds;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestTicker;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;


public class ClientProxy extends CommonProxy {
    
    @Override
    public void initSounds(String path) {
        //init all the sounds
        Sounds.initSounds();
    }
    
    @Override
    public void initRenderers() {
        //init the rendering stuff
        
        // Items
        /*
        for (int i = 0; i < 2; i++) {
            ModelLoader.setCustomModelIdentifier(ModItems.book, i, new ModelIdentifier("hardcorequesting:quest_book", "inventory"));
            MinecraftClient.getInstance().getItemRenderer().getModels().putModel(ModItems.book, i, new ModelIdentifier("hardcorequesting:quest_book", "inventory"));
        }
        
        ModelLoader.setCustomModelIdentifier(ModItems.quarterheart, 0, new ModelIdentifier("hardcorequesting:quarterheart", "inventory"));
        MinecraftClient.getInstance().getItemRenderer().getModels().putModel(ModItems.quarterheart, new ModelIdentifier("hardcorequesting:quarterheart", "inventory"));
        
        ModelLoader.setCustomModelIdentifier(ModItems.halfheart, 0, new ModelIdentifier("hardcorequesting:halfheart", "inventory"));
        MinecraftClient.getInstance().getItemRenderer().getModels().putModel(ModItems.halfheart, new ModelIdentifier("hardcorequesting:halfheart", "inventory"));
        
        ModelLoader.setCustomModelIdentifier(ModItems.threequartsheart, 0, new ModelIdentifier("hardcorequesting:threequarts", "inventory"));
        MinecraftClient.getInstance().getItemRenderer().getModels().putModel(ModItems.threequartsheart, new ModelIdentifier("hardcorequesting:threequarts", "inventory"));
        
        ModelLoader.setCustomModelIdentifier(ModItems.heart, 0, new ModelIdentifier("hardcorequesting:heart", "inventory"));
        MinecraftClient.getInstance().getItemRenderer().getModels().putModel(ModItems.heart, new ModelIdentifier("hardcorequesting:heart", "inventory"));
        
        ModelLoader.setCustomModelIdentifier(ModItems.rottenheart, 0, new ModelIdentifier("hardcorequesting:rottenheart", "inventory"));
        MinecraftClient.getInstance().getItemRenderer().getModels().putModel(ModItems.rottenheart, new ModelIdentifier("hardcorequesting:rottenheart", "inventory"));
        
        for (int i = 0; i < BagTier.values().length; i++) {
            ModelLoader.setCustomModelIdentifier(ModItems.bags, i, new ModelIdentifier("hardcorequesting:bags", "inventory"));
            MinecraftClient.getInstance().getItemRenderer().getModels().putModel(ModItems.bags, i, new ModelIdentifier("hardcorequesting:bags", "inventory"));
        }
        
        ModelLoader.setCustomModelIdentifier(ModItems.invalidItem, 0, new ModelIdentifier("hardcorequesting:hqm_invalid_item", "inventory"));
        MinecraftClient.getInstance().getItemRenderer().getModels().putModel(ModItems.invalidItem, new ModelIdentifier("hardcorequesting:hqm_invalid_item", "inventory"));
        
        // Blocks
        ModelLoader.setCustomModelIdentifier(Item.getItemFromBlock(ModBlocks.itemBarrel), 0, new ModelIdentifier(ModBlocks.itemBarrel.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelIdentifier(Item.getItemFromBlock(ModBlocks.itemTracker), 0, new ModelIdentifier(ModBlocks.itemTracker.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelIdentifier(Item.getItemFromBlock(ModBlocks.itemPortal), 0, new ModelIdentifier(ModBlocks.itemPortal.getRegistryName(), "inventory"));
        
        Item item = null;
        
        item = Item.fromBlock(ModBlocks.itemBarrel);
        MinecraftClient.getInstance().getItemRenderer().getModels().putModel(item, new ModelIdentifier(Registry.ITEM.getId(item), "inventory"));
        
        item = Item.fromBlock(ModBlocks.itemTracker);
        MinecraftClient.getInstance().getItemRenderer().getModels().putModel(item, new ModelIdentifier(Registry.ITEM.getId(item), "inventory"));
        
        item = Item.fromBlock(ModBlocks.itemPortal);
        MinecraftClient.getInstance().getItemRenderer().getModels().putModel(item, new ModelIdentifier(Registry.ITEM.getId(item), "inventory"));
         */
    }
    
    @Override
    public void init() {
        Quest.serverTicker = new QuestTicker(false);
        Quest.clientTicker = new QuestTicker(true);
    }
    
    @Override
    public boolean isClient() {
        return true;
    }
    
    @Override
    public boolean isServer() {
        return false;
    }
    
    @Override
    public Player getPlayer(PacketContext ctx) {
        return ctx.getPacketEnvironment() == EnvType.CLIENT ? Minecraft.getInstance().player : super.getPlayer(ctx);
    }
}
