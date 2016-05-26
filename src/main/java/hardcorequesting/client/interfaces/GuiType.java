package hardcorequesting.client.interfaces;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import hardcorequesting.network.message.OpenGuiMessage;
import hardcorequesting.tileentity.TileEntityTracker;
import hardcorequesting.tileentity.TrackerType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public enum GuiType
{
    NONE
    {
        @Override
        public IMessage build(String... data)
        {
            return null;
        }

        @Override
        public void open(EntityPlayer player, String data)
        {

        }
    },
    TRACKER
    {
        private static final String BLOCK_POS = "blockPos";
        private static final String QUEST = "quest";
        private static final String RADIUS = "radius";
        private static final String TYPE = "trackerType";


        @Override
        public IMessage build(String... data)
        {
            StringWriter stringWriter = new StringWriter();
            try
            {
                JsonWriter writer = new JsonWriter(stringWriter);
                writer.beginObject();
                writer.name(BLOCK_POS).value(data[0]);
                writer.name(QUEST).value(data[1]);
                writer.name(RADIUS).value(data[2]);
                writer.name(TYPE).value(data[3]);
                writer.endObject();
            } catch (IOException ignored) {}

            return new OpenGuiMessage(this, stringWriter.toString());
        }

        @Override
        public void open(EntityPlayer player, String data)
        {
            JsonParser parser = new JsonParser();
            JsonObject root = parser.parse(data).getAsJsonObject();
            BlockPos pos = BlockPos.fromLong(root.get(BLOCK_POS).getAsLong());
            JsonElement quest = root.get(QUEST);
            String questId = quest.isJsonNull() ? null : root.getAsString();
            int radius = root.get(RADIUS).getAsInt();
            TrackerType type = TrackerType.values()[root.get(TYPE).getAsInt()];
            TileEntityTracker.openInterface(player, pos, questId, radius, type);
        }
    },
    BOOK
    {
        @Override
        public IMessage build(String... data)
        {
            return new OpenGuiMessage(this, data[0]);
        }

        @Override
        public void open(EntityPlayer player, String data)
        {
            GuiQuestBook.displayGui(player, Boolean.parseBoolean(data));
        }
    },
    BAG
    {
        private static final String GROUP = "group";
        private static final String BAG = "bag";
        private static final String LIMIT = "limit";

        @Override
        public IMessage build(String... data)
        {
            StringWriter stringWriter = new StringWriter();

            try
            {
                JsonWriter writer = new JsonWriter(stringWriter);
                writer.beginObject();
                writer.name(GROUP).value(data[0]);
                writer.name(BAG).value(data[1]);
                writer.name(LIMIT).beginArray();
                int i = 2;
                while (i < data.length)
                    writer.value(Integer.parseInt(data[i]));
                writer.endArray();
                writer.endObject();
                writer.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }

            return new OpenGuiMessage(this, stringWriter.toString());
        }

        @Override
        public void open(EntityPlayer player, String data)
        {
            JsonParser parser = new JsonParser();
            JsonObject root = parser.parse(data).getAsJsonObject();
            String groupId = root.get(GROUP).getAsString();
            int bag = root.get(BAG).getAsInt();
            JsonArray limitsArray = root.get(LIMIT).getAsJsonArray();
            List<Integer> limits = new ArrayList<>();
            for (JsonElement element : limitsArray)
                limits.add(element.getAsInt());
            GuiReward.open(player, groupId, bag, limits);
        }
    };

    public abstract IMessage build(String... data);

    public abstract void open(EntityPlayer player, String data);
}
