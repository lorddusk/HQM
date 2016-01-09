package hardcorequesting.network;


import hardcorequesting.FileVersion;
import hardcorequesting.QuestingData;

import java.io.*;

import org.apache.logging.log4j.Level;

import net.minecraftforge.fml.common.FMLLog;

public abstract class FileHelper {

    public enum SaveResult {
        SUCCESS("Success", "Everything was successfully saved"),
        BACKUP_FAIL("Backup failure", "Couldn't backup the previous saved data. Please fix this and save again."),
        SAVE_FAIL("Save failure", "Couldn't save the data to file. Your previous backup has been saved."),
        PRE_CRASH_FAILURE("Double save failure", "Couldn't save the data to file. And when trying to backup your previously saved data, this didn't work either.");


        private String name;
        private String text;

        SaveResult(String name, String text) {
            this.name = name;
            this.text = text;
        }

        public String getName() {
            return name;
        }

        public String getText() {
            return text;
        }
    }

    private static final String BACKUP_SUFFIX = "-backup";
    private static final String PRE_CRASH_SUFFIX = "-pre-crash-";

    /**
     * Makes sure that the supplied folder is created, along with all its parent folders. This is to make sure the path actually exists
     *
     * @param dir The folder to create
     * @throws java.io.IOException
     */
    private void createFolder(File dir) throws IOException {
        if (dir == null) {
            return;
        }

        File parent = dir.getParentFile();
        createFolder(parent);
        if (!dir.isDirectory()) {
            dir.mkdirs();
        }
    }


    public SaveResult saveData(File file) {
        if (!backup(file)) {
            return SaveResult.BACKUP_FAIL;
        }

        DataWriter dw = null;
        try {
            createFolder(file.getParentFile());

            dw = new DataWriter(new FileOutputStream(file));
            dw.writeByte(QuestingData.FILE_VERSION.ordinal());
            write(dw);
            dw.writeFinalBits();

            return SaveResult.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            FMLLog.log("HQM", Level.ERROR, e, "An error occurred during quest book writing");
            boolean success = false;
            if (moveUpFileIndices(file.getAbsolutePath() + PRE_CRASH_SUFFIX, 0)) {
                if (backup(new File(file.getAbsolutePath() + BACKUP_SUFFIX), new File(file.getAbsolutePath() + PRE_CRASH_SUFFIX + 0))) {
                    success = true;
                }
            }

            return success ? SaveResult.SAVE_FAIL : SaveResult.PRE_CRASH_FAILURE;
        } finally {
            if (dw != null) {
                dw.close();
            }
        }
    }

    private boolean moveUpFileIndices(String path, int id) {
        File file = new File(path + id);
        return !file.exists() || moveUpFileIndices(path, id + 1) && file.renameTo(new File(path + (id + 1)));
    }

    public boolean loadData(File file) {
        DataReader dr = null;
        try {
            createFolder(file.getParentFile());
            if (!file.exists()) {
                return false;
            }
            dr = new DataReader(new FileInputStream(file));
            read(dr, dr.readVersion());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (dr != null) {
                dr.close();
            }
        }
    }

    public boolean backup(File fileToBackUp) {
        return backup(fileToBackUp, new File(fileToBackUp.getAbsolutePath() + BACKUP_SUFFIX));
    }

    public boolean backup(File fileToBackUp, File backup) {
        if (fileToBackUp.exists()) {
            FileInputStream inputStream = null;
            FileOutputStream outputStream = null;
            try {
                inputStream = new FileInputStream(fileToBackUp);
                outputStream = new FileOutputStream(backup);
                while (inputStream.available() > 0) {
                    outputStream.write(inputStream.read());
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (IOException ignored) {
                }
            }
        } else {
            return true;
        }
    }

    public abstract void write(DataWriter dw);

    public abstract void read(DataReader dr, FileVersion version);
}
