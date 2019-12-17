package hqm.quest;

import hqm.HQM;
import hqm.HQMJson;
import hqm.debug.DebugQuestbook;
import net.minecraft.world.IWorld;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.File;
import java.io.IOException;
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

    static {
        DebugQuestbook debugQuestbook = new DebugQuestbook();
        //QUEST_DATA.put(debugQuestbook.getId(), debugQuestbook);
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event){
        QUEST_DATA.clear();
        if(!event.getWorld().isRemote()){
            File worldSave = getWorldSaveFolder(event.getWorld());
            if(worldSave != null){
                File hqmWorldFolder = new File(worldSave, "hqm");
                if(!hqmWorldFolder.exists()){
                    hqmWorldFolder.mkdir();
                    return; // There is no point of reading the content, if we created the folder one line above
                }
                readQuestbooks(hqmWorldFolder).forEach(questbook -> QUEST_DATA.put(questbook.getId(), questbook));
            } else {
                System.out.println("F*** there is something really wrong with this world save folder finding reflection");
            }
        }
    }
    
    @SubscribeEvent
    public static void onWorldSave(WorldEvent.Save event){
        if(!event.getWorld().isRemote()){
            File worldSave = getWorldSaveFolder(event.getWorld());
            if(worldSave != null){
                File hqmWorldFolder = new File(worldSave, "hqm");
                if(hqmWorldFolder.exists()){
                    for(Questbook questbook : QUEST_DATA.values()){
                        File questbookFolder = new File(hqmWorldFolder, questbook.getId().toString());
                        if(questbookFolder.exists()){
                            saveTeams(questbook, questbookFolder);
                        }
                    }
                }
            } else {
                System.out.println("F*** there is something really wrong with this world save folder finding reflection");
            }
        }
    }
    
    @SubscribeEvent
    public static void onServerJoin(PlayerEvent.PlayerLoggedInEvent event){
        QUEST_DATA.clear();
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
                            readTeams(questbook, questbookFolder);
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
        File[] questLineJsons = questbookFolder.listFiles((dir, name) -> name.endsWith(".json") && name.length() >= 30);
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

    public static void readTeams(Questbook questbook, File questbookFolder){
        File teamJson = new File(questbookFolder, "teams.json");
        if(teamJson.exists()){
            try {
                TeamList teamList = HQMJson.readFromFile(teamJson, TeamList.class);
                if(teamList != null && teamList.getTeams() != null){
                    teamList.getTeams().forEach(questbook::addTeam);
                }
            } catch (Exception e){
                e.printStackTrace();
                System.out.println("Can't read teams file for " + questbook + ". " + e.getMessage());
            }
        }
    }

    public static void saveTeams(Questbook questbook, File questbookFolder){
        File teamJson = new File(questbookFolder, "teams.json");
        if(teamJson.exists()){
            teamJson.delete();
        }
        try {
            teamJson.createNewFile();
            HQMJson.writeToFile(teamJson, new TeamList(questbook.getTeams()));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Can't write team.json. " + e.getMessage());
        }
    }

    private static File getWorldSaveFolder(IWorld world){
        /* todo idk how to in 1.15
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
        }*/
        return null;
    }

}
