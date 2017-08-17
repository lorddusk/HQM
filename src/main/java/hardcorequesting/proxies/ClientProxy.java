package hardcorequesting.proxies;

import hardcorequesting.bag.BagTier;
import hardcorequesting.blocks.ModBlocks;
import hardcorequesting.client.sounds.Sounds;
import hardcorequesting.items.ModItems;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestTicker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;


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
        for(int i = 0; i < 2; i++)
        {
            ModelLoader.setCustomModelResourceLocation(ModItems.book, i, new ModelResourceLocation("hardcorequesting:quest_book", "inventory"));
            Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(ModItems.book, i, new ModelResourceLocation("hardcorequesting:quest_book", "inventory"));
        }
        
        ModelLoader.setCustomModelResourceLocation(ModItems.quarterheart, 0, new ModelResourceLocation("hardcorequesting:quarterheart", "inventory"));
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(ModItems.quarterheart, 0, new ModelResourceLocation("hardcorequesting:quarterheart", "inventory"));
        
        ModelLoader.setCustomModelResourceLocation(ModItems.halfheart, 0, new ModelResourceLocation("hardcorequesting:halfheart", "inventory"));
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(ModItems.halfheart, 0, new ModelResourceLocation("hardcorequesting:halfheart", "inventory"));
        
        ModelLoader.setCustomModelResourceLocation(ModItems.threequartsheart, 0, new ModelResourceLocation("hardcorequesting:threequarts", "inventory"));
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(ModItems.threequartsheart, 0, new ModelResourceLocation("hardcorequesting:threequarts", "inventory"));
        
        ModelLoader.setCustomModelResourceLocation(ModItems.heart, 0, new ModelResourceLocation("hardcorequesting:heart", "inventory"));
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(ModItems.heart, 0, new ModelResourceLocation("hardcorequesting:heart", "inventory"));
        
        ModelLoader.setCustomModelResourceLocation(ModItems.rottenheart, 0, new ModelResourceLocation("hardcorequesting:rottenheart", "inventory"));
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(ModItems.rottenheart, 0, new ModelResourceLocation("hardcorequesting:rottenheart", "inventory"));
        
        for(int i = 0; i < BagTier.values().length; i++)
        {
            ModelLoader.setCustomModelResourceLocation(ModItems.bags, i, new ModelResourceLocation("hardcorequesting:bags", "inventory"));
            Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(ModItems.bags, i, new ModelResourceLocation("hardcorequesting:bags", "inventory"));
        }
        
        ModelLoader.setCustomModelResourceLocation(ModItems.invalidItem, 0, new ModelResourceLocation("hardcorequesting:hqm_invalid_item", "inventory"));
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(ModItems.invalidItem, 0, new ModelResourceLocation("hardcorequesting:hqm_invalid_item", "inventory"));
        
        // Blocks
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.itemBarrel), 0, new ModelResourceLocation(ModBlocks.itemBarrel.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.itemTracker), 0, new ModelResourceLocation(ModBlocks.itemTracker.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.itemPortal), 0, new ModelResourceLocation(ModBlocks.itemPortal.getRegistryName(), "inventory"));
        
        Item item = null;
        
        item = Item.getItemFromBlock(ModBlocks.itemBarrel);
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        
        item = Item.getItemFromBlock(ModBlocks.itemTracker);
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        
        item = Item.getItemFromBlock(ModBlocks.itemPortal);
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
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
    public EntityPlayer getPlayer(MessageContext ctx) {
        return ctx.side == Side.CLIENT ? Minecraft.getMinecraft().player : super.getPlayer(ctx);
    }
}
