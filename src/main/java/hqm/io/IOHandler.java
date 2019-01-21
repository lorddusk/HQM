package hqm.io;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hardcorequesting.HardcoreQuesting;
import hqm.api.IQuestbook;
import hardcorequesting.io.adapter.MinecraftAdapter;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class IOHandler{
    
    public static final Gson GENERAL_GSON = new GsonBuilder()
        .registerTypeAdapter(ItemStack.class, MinecraftAdapter.ITEM_STACK)
        .registerTypeAdapter(NBTTagCompound.class, Adapter.NBT_TAG_COMPOUND)
        .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE_WITH_SPACES)
        .setPrettyPrinting()
        .create();
    
    /*
        Internal Methods
     */
    
    // returns true if a error occurred
    private static boolean writeTypeToFile(@Nonnull File file, @Nonnull Object type){
        try{
            if(file.exists()){
                if(!file.delete()){
                    new IOException("Could not delete file! " + file.getAbsolutePath()).printStackTrace();
                    return true;
                }
            }
            if(file.createNewFile()){
                try(FileWriter writer = new FileWriter(file)){
                    GENERAL_GSON.toJson(type, writer);
                }
                return false;
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        return true;
    }
    
    private static <T> T readTypeFromFile(@Nonnull File file, Class<T> type){
        if(file.exists()){
            try(FileReader fr = new FileReader(file)){
                return GENERAL_GSON.fromJson(fr, type);
            }catch(IOException e){
                e.printStackTrace();
            }
        }
        return null;
    }
    
    
    /*
        Non Internals
     */
    
    @Nonnull
    private static File getRootFolder(){
        File hqmFolder = new File(Launch.minecraftHome, "hqm");
        if(!hqmFolder.exists()){
            hqmFolder.mkdirs();
        }
        return hqmFolder;
    }
    
    private static final Map<File, IQuestbook> questbooksLoaded = new ConcurrentHashMap<>();
    public static void readQuestbooks(boolean invalidateCache){
        if(invalidateCache){
            questbooksLoaded.clear();
        }
        if(questbooksLoaded.isEmpty()){
            for(File file : FileUtils.listFiles(getRootFolder(), new String[]{"json"}, true)){
                QuestbookData qd = readQuestbookDataFromFile(file);
                if(qd != null){
                    questbooksLoaded.put(file, qd.generateQuestbook(true, true, true, true));
                }
            }
        }
    }
    
    /**
     * A method to get the .json file for a specific questbook.
     * If the file doesn't exist, it won't be created, but cached.
     * It does it's best to find a free filename to save to,
     * but it should NEVER be necessary to run into the while loop!
     *
     * @param questbook The questbook to get the .json from.
     * @return The corresponding .json file for the questbook.
     */
    public static File getQuestbookFile(@Nonnull IQuestbook questbook){
        readQuestbooks(false); // in case nothing has tried to load every questbook before
        for(Map.Entry<File, IQuestbook> entry : questbooksLoaded.entrySet()){
            if(entry.getValue().equals(questbook)){
                return entry.getKey();
            }
        }
        String fileName = (questbook.getNameTranslationKey() != null && !questbook.getNameTranslationKey().isEmpty()) ? questbook.getNameTranslationKey() : String.valueOf(questbook.getUUID());
        File file = new File(getRootFolder(), fileName.concat(".json"));
        while(file.exists()){ // we never want to overwrite a existing book
            String newName = file.getName().replace(".json", "_.json");
            if(newName.length() >= 200){ // windows caps at 255, but we want to have some space
                newName = "_".concat(File.separator).concat(newName.replaceAll("_", ""));
            }
            file = new File(getRootFolder(), newName);
        }
        questbooksLoaded.put(file, questbook);
        return file;
    }
    
    public static List<IQuestbook> getAllQuestbooks(){
        return new ArrayList<>(questbooksLoaded.values());
    }
    
    @Nullable
    public static IQuestbook getQuestbook(UUID questbookId){
        return getAllQuestbooks().stream().filter(questbook -> questbook.getUUID().equals(questbookId)).findFirst().orElse(null);
    }
    
    public static void addQuestbook(@Nonnull IQuestbook questbook){
        getQuestbookFile(questbook);
    }
    
    /**
     * Writes the questbook to the corresponding file.
     * Creates a backup of the old state id it exists.
     *
     * @param questbook The questbook to write.
     */
    public static void writeQuestbook(@Nonnull IQuestbook questbook){
        File file = getQuestbookFile(questbook);
        File oldFile = new File(file.getParentFile(), file.getName().concat(".old"));
        try{
            FileUtils.deleteQuietly(oldFile);
            if(file.exists() && !oldFile.exists()){
                FileUtils.copyFile(file, oldFile);
            }
        } catch(IOException e){
            e.printStackTrace();
        }
        FileUtils.deleteQuietly(file);
        writeQuestbookToFile(new QuestbookData().generateData(questbook), file);
    }
    
    public static void writeTeamDataToFile(@Nonnull TeamData teamData, @Nonnull File file){
        if(writeTypeToFile(file, teamData)){
            HardcoreQuesting.LOG.error(String.format("A error occurred while writing TeamData to %s", file.getAbsolutePath()));
        }
    }
    
    @Nullable
    public static TeamData readTeamDataFromFile(@Nonnull File file){
        TeamData teamData = readTypeFromFile(file, TeamData.class);
        if(teamData == null){
            HardcoreQuesting.LOG.error(String.format("A error occurred while reading TeamData from %s", file.getAbsolutePath()));
        }
        return teamData;
    }
    
    public static void writeQuestbookToFile(@Nonnull QuestbookData questbookData, @Nonnull File file){
        if(writeTypeToFile(file, questbookData)){
            HardcoreQuesting.LOG.error(String.format("A error occurred while writing QuestbookData to %s", file.getAbsolutePath()));
        }
    }
    
    @Nullable
    public static QuestbookData readQuestbookDataFromFile(@Nonnull File file){
        QuestbookData questbookData = readTypeFromFile(file, QuestbookData.class);
        if(questbookData == null){
            HardcoreQuesting.LOG.error(String.format("A error occurred while reading QuestbookData from %s", file.getAbsolutePath()));
        }
        return questbookData;
    }
    
}
