package hardcorequesting.network;


import hardcorequesting.FileVersion;
import hardcorequesting.QuestingData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;

import java.io.*;
import java.util.zip.GZIPInputStream;

import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;

import net.minecraftforge.fml.common.FMLLog;

public class DataReader {

    private InputStream stream;
    private int byteBuffer;
    private int bitCountBuffer;
    private FileVersion version;

    DataReader(byte[] data) {
        stream = new ByteArrayInputStream(data);
        version = QuestingData.FILE_VERSION;
    }

    DataReader(InputStream stream) {
        this.stream = stream;
        version = QuestingData.FILE_VERSION;
    }

    public int readByte() {
        return readData(DataBitHelper.BYTE);
    }

    public boolean readBoolean() {
        return readData(DataBitHelper.BOOLEAN) != 0;
    }

    public int readData(DataBitHelper bitCount) {
        return readData(bitCount.getBitCount(version));
    }

    public int readData(int bitCount) {
        int data = 0;
        int readBits = 0;

        while (true) {
            int bitsLeft = bitCount - readBits;
            if (bitCountBuffer >= bitsLeft) {
                data |= (byteBuffer & PacketHandler.BIT_MASK[bitsLeft]) << readBits;
                byteBuffer >>>= bitsLeft;
                bitCountBuffer -= bitsLeft;
                readBits += bitsLeft;
                break;
            } else {
                data |= byteBuffer << readBits;
                readBits += bitCountBuffer;

                try {
                    byteBuffer = stream.read();
                } catch (IOException ignored) {
                    byteBuffer = 0;
                }

                bitCountBuffer = 8;
            }
        }


        return data;
    }

    public void close() {
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readString(DataBitHelper bits) {
        int length = readData(bits);
        if (length == 0) {
            return null;
        } else {
            byte[] bytes = new byte[length];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) readByte();
            }
            return new String(bytes);
        }
    }

    public NBTTagCompound readNBT() {
        if (readBoolean()) {
            byte[] bytes = new byte[readData(DataBitHelper.NBT_LENGTH)];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) readByte();
            }

            try {
                DataInputStream data = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(bytes))));
                return CompressedStreamTools.read(data, new NBTSizeTracker(2087152L));
            } catch (IOException ex) {
                return null;
            }
        } else {
            return null;
        }
    }

    @Deprecated
    public boolean doesUnderlyingStreamHasMoreThanAByteOfData() {
        try {
            return stream.available() > 0;
        } catch (IOException ignored) {
            return false;
        }
    }

    public FileVersion readVersion() {
        return version = FileVersion.values()[readByte()];
    }

    public ItemStack readItemStack(boolean useSize) {
        return readRawItemStack(false, useSize);
    }

    public ItemStack readAndFixItemStack(boolean useSize) {
        return readRawItemStack(true, useSize);
    }

    //fix is true if previous versions loaded it wrong here and therefore needs to get fixed
    private ItemStack readRawItemStack(boolean fix, boolean useSize) {
        if (fix && version.lacks(FileVersion.NO_ITEM_IDS_FIX)) {
            NBTTagCompound compound = readNBT();
            if (compound != null) {
                return ItemStack.loadItemStackFromNBT(compound);
            } else {
                return null;
            }
        } else {
            Item item = readItem();
            int size = useSize ? readData(DataBitHelper.SHORT) : 1;
            int dmg = readData(DataBitHelper.SHORT);
            NBTTagCompound compound = readNBT();
            ItemStack itemStack = new ItemStack(item, size, dmg);
            itemStack.setTagCompound(compound);
            return itemStack;
        }
    }


    public Item readItem() {
        if (version.contains(FileVersion.NO_ITEM_IDS)) {
            String readString = readString(DataBitHelper.SHORT);
            if (readString == null) {
                FMLLog.log("HQM", Level.ERROR, "Attempted to read an item that doesn't exist %s", readString);
                return null;
            }
            Object obj = Item.itemRegistry.getObject(new ResourceLocation(readString));
            if (obj == null) {
                FMLLog.log("HQM", Level.ERROR, "Attempted to read an item that doesn't exist %s", readString);
                return null;
            }
            if (obj instanceof Item) {
                return (Item) obj;
            }
        } else {
            readData(DataBitHelper.SHORT); //read the old integer id, this doesn't say a thing anymore so just ignore it
        }

        return null;
    }

    private static final double LOG_2 = Math.log10(2);

    public <T extends Enum> T readEnum(Class<T> clazz) {
        try {
            Object[] values = (Object[]) clazz.getMethod("values").invoke(null);
            int length = values.length;
            if (length == 0) {
                return null;
            }
            int bitCount = (int) (Math.log10(length) / LOG_2) + 1;

            int val = readData(bitCount);
            return (T) values[val];
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
}
