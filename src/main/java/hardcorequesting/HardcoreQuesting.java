package hardcorequesting;

import hardcorequesting.blocks.ModBlocks;
import hardcorequesting.commands.CommandHandler;
import hardcorequesting.config.ConfigHandler;
import hardcorequesting.event.PlayerDeathEventListener;
import hardcorequesting.event.PlayerTracker;
import hardcorequesting.event.WorldEventListener;
import hardcorequesting.io.SaveHandler;
import hardcorequesting.items.ModItems;
import hardcorequesting.network.NetworkManager;
import hardcorequesting.proxies.CommonProxy;
import hardcorequesting.quests.QuestLine;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

import java.io.File;

@Mod(modid = ModInformation.ID, name = ModInformation.NAME, version = ModInformation.VERSION, guiFactory = "hardcorequesting.client.interfaces.HQMModGuiFactory")
public class HardcoreQuesting {

    @Instance(ModInformation.ID)
    public static HardcoreQuesting instance;

    @SidedProxy(clientSide = "hardcorequesting.proxies.ClientProxy", serverSide = "hardcorequesting.proxies.CommonProxy")
    public static CommonProxy proxy;
    public static CreativeTabs HQMTab = new HQMTab();

    public static String path;

    public static File configDir;

    public static Side loadingSide;

    private static EntityPlayer commandUser;

    public static EntityPlayer getPlayer() {
        return commandUser;
    }

    public static void setPlayer(EntityPlayer player) {
        commandUser = player;
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        loadingSide = event.getSide();
        new hardcorequesting.event.EventHandler();

        path = event.getModConfigurationDirectory().getAbsolutePath() + File.separator + ModInformation.CONFIG_LOC_NAME.toLowerCase() + File.separator;
        configDir = new File(path);
        ConfigHandler.initModConfig(path);
        ConfigHandler.initEditConfig(path);
        QuestLine.init(path);

        proxy.init();
        proxy.initSounds(path);

        ModBlocks.init();
        ModBlocks.registerTileEntities();

        ModItems.init();
        proxy.initRenderers();
    }

    @EventHandler
    public void load(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(instance);

        new WorldEventListener();
        new PlayerDeathEventListener();
        new PlayerTracker();

        NetworkManager.init();

        ModItems.registerRecipes();
        ModBlocks.registerRecipes();

        FMLInterModComms.sendMessage("Waila", "register", "hardcorequesting.waila.Provider.callbackRegister");
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(CommandHandler.instance);
    }

    @EventHandler
    public void missingMappings(FMLMissingMappingsEvent event) {
        for (FMLMissingMappingsEvent.MissingMapping mapping : event.get()) {
            ResourceLocation loc = mapping.resourceLocation;
            if (loc.getResourceDomain().equals("HardcoreQuesting")) {
                if (mapping.type.equals(GameRegistry.Type.BLOCK)) {
                    Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("hardcorequesting", loc.getResourcePath()));
                    if (block != null) {
                        mapping.remap(block);
                    }
                } else {
                    Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("hardcorequesting", loc.getResourcePath()));
                    if (item != null) {
                        mapping.remap(item);
                    }
                }
            }
            if (mapping.resourceLocation.getResourcePath().toLowerCase().equals("hqminvaliditem")) {
                mapping.remap(ModItems.invalidItem);
            }
        }
    }
}
