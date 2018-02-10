package hqm.quest;

import hqm.HQM;
import hqm.HQMJson;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author canitzp
 */
@Mod.EventBusSubscriber(modid = HQM.MODID)
public class SaveHandler {

    public static final Map<UUID, Questbook> QUEST_DATA = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event){
        if(!event.getWorld().isRemote){
            File worldSave = getWorldSaveFolder(event.getWorld());
            if(worldSave != null){
                File hqmWorldFolder = new File(worldSave, "hqm");
                if(!hqmWorldFolder.exists()){
                    hqmWorldFolder.mkdir();
                    return; // There is no point of reading the content, if we created the folder one line above
                }

                System.out.println(HQMJson.readFromFile(new File(hqmWorldFolder, "test.json"), NBTTagCompound.class));

                //readQuestbooks(hqmWorldFolder).forEach(questbook -> QUEST_DATA.put(questbook.getId(), questbook));
            } else {
                System.out.println("F*** there is something really wrong with this world save folder finding reflection");
            }
        }
    }

    public static List<Questbook> readQuestbooks(File hqmWorldFolder){
        List<Questbook> questbooks = new ArrayList<>();
        File[] jsonFiles = hqmWorldFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if(jsonFiles != null){
            for(File mainJson : jsonFiles){
                try {
                    Questbook questbook = HQMJson.readFromFile(mainJson, Questbook.class);
                    if(questbook != null){
                        questbooks.add(questbook);
                        File questbookFolder = new File(hqmWorldFolder, questbook.getId().toString());
                        if(questbookFolder.exists()){
                            readQuestLine(questbook, questbookFolder);
                        } else {
                            questbookFolder.mkdir();
                        }
                    }
                } catch (Exception e){
                    System.out.println("Can't read a questbook. Folder " + mainJson + "   " + e.getMessage());
                }
            }
        }
        return questbooks;
    }

    public static void readQuestLine(Questbook questbook, File questbookFolder){
        File[] questLineJsons = questbookFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if(questLineJsons != null){
            for(File questLineJson : questLineJsons){
                try {
                    QuestLine questLine = HQMJson.readFromFile(questLineJson, QuestLine.class);
                    if(questLine != null){
                        questbook.addQuestLine(questLine);
                    }
                } catch (Exception e){
                    System.out.println("Can't read QuestLine for " + questbook + " from file " + questLineJson + ". " + e.getMessage());
                }
            }
        }
    }

    private static File getWorldSaveFolder(World world){
        MapStorage mapStorage = world.getMapStorage();
        if(mapStorage != null){
            try {
                ISaveHandler saveHandler = ReflectionHelper.getPrivateValue(MapStorage.class, mapStorage, 0);
                if(saveHandler != null){
                    return saveHandler.getWorldDirectory();
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }

}
