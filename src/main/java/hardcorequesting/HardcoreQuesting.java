package hardcorequesting;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.NetworkRegistry;
import hardcorequesting.blocks.ModBlocks;
import hardcorequesting.commands.CommandHandler;
import hardcorequesting.config.ConfigHandler;
import hardcorequesting.items.ModItems;
import hardcorequesting.network.PacketHandler;
import hardcorequesting.proxies.CommonProxy;
import hardcorequesting.quests.Quest;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;

import java.io.File;

@Mod(modid = ModInformation.ID, name = ModInformation.NAME, version = ModInformation.VERSION, guiFactory = "hardcorequesting.client.interfaces.HQMModGuiFactory")
public class HardcoreQuesting
{

    @Instance(ModInformation.ID)
    public static HardcoreQuesting instance;

    @SidedProxy(clientSide = "hardcorequesting.proxies.ClientProxy", serverSide = "hardcorequesting.proxies.CommonProxy")
    public static CommonProxy proxy;
    public static CreativeTabs HQMTab = new HQMTab();

    public static String path;

    public static File configDir;

    public static FMLEventChannel packetHandler;

    private static EntityPlayer commandUser;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        new hardcorequesting.EventHandler();
        packetHandler = NetworkRegistry.INSTANCE.newEventDrivenChannel(ModInformation.CHANNEL);

        path = event.getModConfigurationDirectory().getAbsolutePath() + File.separator + ModInformation.CONFIG_LOC_NAME.toLowerCase() + File.separator;
        configDir = new File(path);
        ConfigHandler.initModConfig(path);
        ConfigHandler.initEditConfig(path);

        proxy.init();
        proxy.initRenderers();
        proxy.initSounds(path);

        ModItems.init();

        ModBlocks.init();
        ModBlocks.registerBlocks();
        ModBlocks.registerTileEntities();

        //Quest.init(this.path);
    }

    @EventHandler
    public void load(FMLInitializationEvent event)
    {
        new File(configDir + File.separator + "QuestSheets").mkdir();

        FMLCommonHandler.instance().bus().register(instance);

        packetHandler.register(new PacketHandler());
        new WorldEventListener();
        new PlayerDeathEventListener();
        new PlayerTracker();


        ModItems.registerRecipes();
        ModBlocks.registerRecipes();

        FMLInterModComms.sendMessage("Waila", "register", "hardcorequesting.waila.Provider.callbackRegister");
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        Quest.init(path);
    }


    @EventHandler
    public void modsLoaded(FMLPostInitializationEvent event)
    {


    }
	
	@EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
        event.registerServerCommand(CommandHandler.instance);
	}

    @EventHandler
    public void serverAboutToStart(FMLServerAboutToStartEvent event)
    {

    }

    @EventHandler
    public void serverAboutToStart(FMLServerStoppingEvent event)
    {

    }

    public static EntityPlayer getPlayer()
    {
        return commandUser;
    }

    public static void setPlayer(EntityPlayer player)
    {
        commandUser = player;
    }
}
