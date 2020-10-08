package hardcorequesting.common.tileentity;


import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import net.minecraft.world.entity.player.Player;

import java.io.IOException;

public interface IBlockSync {
    double BLOCK_UPDATE_RANGE = 128;
    int BLOCK_UPDATE_BUFFER_DISTANCE = 5;
    
    void writeData(Player player, boolean onServer, int type, JsonWriter writer) throws IOException;
    
    void readData(Player player, boolean onServer, int type, JsonObject data);
}
