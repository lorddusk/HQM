package hardcorequesting.integration;

import de.canitzp.tumat.api.TUMATApi;
import hardcorequesting.HardcoreQuesting;
import hardcorequesting.integration.tumat.TumatIntegration;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class IntegrationHandler{
    
    public static final String ID_WAILA = "waila";
    public static final String ID_TUMAT = "tumat";
    
    public static void preInit(FMLPreInitializationEvent event, HardcoreQuesting mod){}
    
    public static void init(FMLInitializationEvent event, HardcoreQuesting mod){
        /*
         * Waila / Hwyla IMC
         */
        FMLInterModComms.sendMessage(ID_WAILA, "register", "hardcorequesting.integration.waila.Provider.callbackRegister");
        /*
         * The One Probe IMC
         */
        FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", "hardcorequesting.integration.top.TheOneProbeIntegration");
    }
    
    public static void postInit(FMLPostInitializationEvent event, HardcoreQuesting mod){
        if(event.getSide().isClient()){
            if(Loader.isModLoaded(ID_TUMAT)){
                TUMATApi.registerRenderComponent(TumatIntegration.class);
            }
        }
    }
    
}
