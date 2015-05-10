package hardcorequesting.config;



import java.io.File;

public class ConfigHandler {

	public static void init(String configPath)
    {

       ModConfig.init(new File(configPath + "hqmconfig.cfg"));
       
       
    }

}
