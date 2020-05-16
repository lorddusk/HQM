package hardcorequesting.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.PacketByteBuf;

import java.util.ArrayList;
import java.util.List;

public class SyncUtil {
    private static int splitCount = 3000;
    
    public static int getSplitCount() {
        return splitCount;
    }
    
    public static void setSplitCount(int count) {
        splitCount = count;
    }
    
    public static List<String> splitData(String input, int splitSize) {
        List<String> output = new ArrayList<>();
        
        int len = input.length();
        
        for (int i = 0; i < len; i += splitSize) {
            output.add(input.substring(i, Math.min(len, i + splitSize)));
        }
        
        return output;
    }
    
    public static String joinData(List<String> input) {
        return joinData(input, "");
    }
    
    public static String joinData(List<String> input, String joiner) {
        return String.join(joiner, input);
    }
    
    public static void reconstructString(List<String> data, ByteBuf buf) {
        data.clear();
        
        int count = buf.readInt();
        
        PacketByteBuf packetByteBuf = new PacketByteBuf(buf);
        for (int i = 0; i < count; i++) {
            data.add(packetByteBuf.readString(32767));
        }
    }
    
    public static List<String> reconstructString(ByteBuf buf) {
        List<String> data = new ArrayList<>();
        
        reconstructString(data, buf);
        
        return data;
    }
    
    public static void deconstructString(List<String> input, ByteBuf output) {
        output.writeInt(input.size());
        PacketByteBuf packetByteBuf = new PacketByteBuf(output);
        for (String chunk : input) {
            packetByteBuf.writeString(chunk);
        }
    }
    
    public static void writeLargeString(String input, ByteBuf output) {
        List<String> data = splitData(input, getSplitCount());
        
        deconstructString(data, output);
    }
    
    public static String readLargeString(ByteBuf input) {
        return joinData(reconstructString(input));
    }
}
