package hardcorequesting;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import hardcorequesting.blocks.ModBlocks;
import hardcorequesting.commands.CommandHandler;
import hardcorequesting.config.ConfigHandler;
import hardcorequesting.items.ItemBag;
import hardcorequesting.items.ModItems;
import hardcorequesting.network.PacketHandler;
import hardcorequesting.proxies.CommonProxy;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestLine;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.util.Map;

@Mod(modid = ModInformation.ID, name = ModInformation.NAME, version = ModInformation.VERSION/*, guiFactory = "hardcorequesting.client.interfaces.HQMModGuiFactory"*/)
public class HardcoreQuesting {


	@Instance(ModInformation.ID)
	public static HardcoreQuesting instance;

	@SidedProxy(clientSide = "hardcorequesting.proxies.ClientProxy", serverSide = "hardcorequesting.proxies.CommonProxy")
	public static CommonProxy proxy;
	public static CreativeTabs HQMTab = new HQMTab(CreativeTabs.getNextID(), "Hardcore Questing Mode");

    public static String path;

    public static File configDir;

    public static Configuration config;

    public static FMLEventChannel packetHandler;

    private static EntityPlayer commandUser;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
        new hardcorequesting.EventHandler();
        packetHandler = NetworkRegistry.INSTANCE.newEventDrivenChannel(ModInformation.CHANNEL);

        path = event.getModConfigurationDirectory().getAbsolutePath() + File.separator + ModInformation.CONFIG_LOC_NAME.toLowerCase() + File.separator;
        ConfigHandler.init(path);

        proxy.init();
		proxy.initRenderers();
        proxy.initSounds(path);

		ModItems.init();

        ModBlocks.init();
        ModBlocks.registerBlocks();
        ModBlocks.registerTileEntities();

        configDir = new File(path);
        config = new Configuration(new File(path+ "editmode.cfg"));
        syncEdit();

        //Quest.init(this.path);
    }
	
	@EventHandler
	public void load(FMLInitializationEvent event) {

        FMLCommonHandler.instance().bus().register(instance);

        packetHandler.register(new PacketHandler());
		new WorldEventListener();
		new PlayerDeathEventListener();	
		new PlayerTracker();


        ModItems.registerRecipes();
        ModBlocks.registerRecipes();

        //FMLInterModComms.sendMessage("Waila", "register", "hardcorequesting.waila.Provider.callbackRegister");
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        Quest.init(path);

    }



    @EventHandler
	public void modsLoaded(FMLPostInitializationEvent event) {
		
	
	}
	
	@EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new CommandHandler());
	}

    @EventHandler
    public void serverAboutToStart(FMLServerAboutToStartEvent event)
    {

    }
    @EventHandler
    public void serverAboutToStart(FMLServerStoppingEvent event)
    {

    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if(event.modID.equals(ModInformation.ID))
            syncEdit();
    }

    @NetworkCheckHandler
    public boolean networkCheck(Map<String, String> map, Side side)
    {
        return true;
    }


    public static void syncEdit(){
        Quest.isEditing = config.get(Configuration.CATEGORY_GENERAL, EDITOR_KEY, EDITOR_DEFAULT, EDITOR_COMMENT).getBoolean(EDITOR_DEFAULT);
        if(config.hasChanged()) {
            config.save();
        }
    }

    public static EntityPlayer getPlayer()
    {
        return commandUser;
    }

    public static void setPlayer(EntityPlayer player)
    {
        commandUser = player;
    }


    private static final String EDITOR_KEY = "UseEditor";
    private static final boolean EDITOR_DEFAULT = false;
    private static final String EDITOR_COMMENT = "Only use this as a map maker who wants to create quests. Leaving this off allows you the play the existing quests.";
}
