package hardcorequesting.tileentity;


import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import net.minecraft.entity.player.PlayerEntity;

import java.io.IOException;

public interface IBlockSync {
    double BLOCK_UPDATE_RANGE = 128;
    int BLOCK_UPDATE_BUFFER_DISTANCE = 5;
    
    void writeData(PlayerEntity player, boolean onServer, int type, JsonWriter writer) throws IOException;
    
    void readData(PlayerEntity player, boolean onServer, int type, JsonObject data);
}
