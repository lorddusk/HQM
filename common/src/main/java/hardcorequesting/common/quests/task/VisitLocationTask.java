package hardcorequesting.common.quests.task;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiColor;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenuLocation;
import hardcorequesting.common.event.EventTrigger;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.data.QuestDataTask;
import hardcorequesting.common.quests.data.QuestDataTaskLocation;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

/**
 * A task where the player has to visit certain locations.
 */
public class VisitLocationTask extends IconQuestTask<VisitLocationTask.Location> {
    private static final String LOCATIONS = "locations";
    
    private static final int CHECK_DELAY = 20;
    private int delay = 1;
    
    public VisitLocationTask(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
        
        register(EventTrigger.Type.SERVER, EventTrigger.Type.PLAYER);
    }
    
    @Override
    protected Location createEmpty() {
        return new Location();
    }
    
    @Override
    protected void onAddElement(Player player) {
        QuestDataTaskLocation data = (QuestDataTaskLocation) getData(player);
        data.visited = Arrays.copyOf(data.visited, data.visited.length + 1);
        SaveHelper.add(SaveHelper.EditType.LOCATION_CREATE);
    }
    
    @Override
    protected void onModifyElement() {
        SaveHelper.add(SaveHelper.EditType.LOCATION_CHANGE);
    }
    
    @Override
    protected void onRemoveElement() {
        SaveHelper.add(SaveHelper.EditType.LOCATION_REMOVE);
    }
    
    private void tick(Player player, boolean isPlayerEvent) {
        if (!isPlayerEvent) {
            delay++;
        } else if (this.delay >= 0) {
            delay = 0;
            
            Level world = player.getCommandSenderWorld();
            if (!world.isClientSide) {
                boolean[] visited = ((QuestDataTaskLocation) this.getData(player)).visited;
                boolean all = true;
                boolean updated = false;
                
                for (int i = 0; i < elements.size(); ++i) {
                    Location location = this.elements.get(i);
                    if (visited.length < i) { // Fix to make sure than the visited array is as long as the location array (#400)
                        boolean[] oldVisited = ArrayUtils.addAll(visited, (boolean[]) null);
                        visited = new boolean[i];
                        System.arraycopy(oldVisited, 0, visited, 0, oldVisited.length);
                        ((QuestDataTaskLocation) this.getData(player)).visited = visited;
                    }
                    if (!visited[i] && Objects.equals(player.getCommandSenderWorld().dimension().location().toString(), location.dimension)) {
                        int current = (int) player.distanceToSqr((double) location.pos.getX() + 0.5D, (double) location.pos.getY() + 0.5D, (double) location.pos.getZ() + 0.5D);
                        int target = location.radius * location.radius;
                        if (location.radius >= 0 && current > target) {
                            all = false;
                        } else {
                            if (!this.isCompleted(player) && this.isVisible(player) && this.parent.isEnabled(player) && this.parent.isAvailable(player)) {
                                updated = true;
                                visited[i] = true;
                            }
                        }
                    }
                }
                
                if (updated) {
                    if (all) {
                        completeTask(player.getUUID());
                    }
                    parent.sendUpdatedDataToTeam(player);
                }
            }
        }
    }
    
    private boolean visited(int id, Player player) {
        return id < elements.size() && ((QuestDataTaskLocation) getData(player)).visited[id];
    }
    
    private void setInfo(int id, Visibility visibility, BlockPos pos, int radius, String dimension, Player player) {
        Location location = getOrCreateForModify(id, player);
        location.setVisibility(visibility);
        location.setPosition(pos);
        location.setRadius(radius);
        location.setDimension(dimension);
    }
    
    @Override
    public Class<? extends QuestDataTask> getDataType() {
        return QuestDataTaskLocation.class;
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    protected void drawElementText(PoseStack matrices, GuiQuestBook gui, Player player, Location location, int index, int x, int y) {
        if (visited(index, player)) {
            gui.drawString(matrices, Translator.translatable("hqm.locationMenu.visited", GuiColor.GREEN), x, y, 0.7F, 0x404040);
        } else if (location.visibility.doShowCoordinate()) {
            int row = 0;
            if (location.radius >= 0) {
                gui.drawString(matrices, Translator.plain("(" + location.pos.toShortString() + ")"), x, y, 0.7F, 0x404040);
                row++;
            }
        
            if (Objects.equals(player.getCommandSenderWorld().dimension().location().toString(), location.dimension)) {
                if (location.radius >= 0) {
                    FormattedText str;
                    int distance = (int) player.distanceToSqr(location.pos.getX() + 0.5, location.pos.getY() + 0.5, location.pos.getZ() + 0.5);
                    str = Translator.translatable("hqm.locationMenu.mAway", distance);
                    if (location.visibility.doShowRadius()) {
                        str = FormattedText.composite(str, Translator.plain(" ["), Translator.translatable("hqm.locationMenu.mRadius", location.radius), Translator.plain("]"));
                    }
                    gui.drawString(matrices, str, x, y + 6*row, 0.7F, 0x404040);
                }
            
            } else {
                gui.drawString(matrices, Translator.translatable("hqm.locationMenu.wrongDim"), x, y + 6*row, 0.7F, 0x404040);
            }
        }
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    protected void handleElementEditClick(GuiQuestBook gui, Player player, EditMode mode, int id, Location location) {
        if (mode == EditMode.LOCATION) {
            GuiEditMenuLocation.display(gui, player, location.getVisibility(), location.getPosition(), location.radius, location.dimension,
                    result -> setInfo(id, result.getVisibility(), result.getPos(), result.getRadius(), result.getDimension(), player));
        }
    }
    
    @Override
    public void onUpdate(Player player) {
        
    }
    
    @Override
    public float getCompletedRatio(UUID playerID) {
        int visited = 0;
        for (boolean b : ((QuestDataTaskLocation) getData(playerID)).visited) {
            if (b) {
                visited++;
            }
        }
        
        return (float) visited / elements.size();
    }
    
    @Override
    public void mergeProgress(UUID playerID, QuestDataTask own, QuestDataTask other) {
        boolean[] visited = ((QuestDataTaskLocation) own).visited;
        boolean[] otherVisited = ((QuestDataTaskLocation) other).visited;
        
        boolean all = true;
        for (int i = 0; i < visited.length; i++) {
            if (otherVisited[i]) {
                visited[i] = true;
            } else if (!visited[i]) {
                all = false;
            }
        }
        
        if (all) {
            completeTask(playerID);
        }
    }
    
    @Override
    public void autoComplete(UUID playerID, boolean status) {
        boolean[] visited = ((QuestDataTaskLocation) getData(playerID)).visited;
        for (int i = 0; i < visited.length; i++) {
            visited[i] = status;
        }
    }
    
    @Override
    public void copyProgress(QuestDataTask own, QuestDataTask other) {
        super.copyProgress(own, other);
        boolean[] visited = ((QuestDataTaskLocation) own).visited;
        System.arraycopy(((QuestDataTaskLocation) other).visited, 0, visited, 0, visited.length);
    }
    
    @Override
    public void onServerTick(MinecraftServer server) {
        tick(null, false);
    }
    
    @Override
    public void onPlayerTick(ServerPlayer playerEntity) {
        if (!playerEntity.level.isClientSide) {
            tick(playerEntity, true);
        }
    }
    
    @Override
    public void write(Adapter.JsonObjectBuilder builder) {
        Adapter.JsonArrayBuilder array = Adapter.array();
        for (Location location : elements) {
            array.add(QuestTaskAdapter.LOCATION_ADAPTER.toJsonTree(location));
        }
        builder.add(LOCATIONS, array.build());
    }
    
    @Override
    public void read(JsonObject object) {
        elements.clear();
        for (JsonElement element : GsonHelper.getAsJsonArray(object, LOCATIONS, new JsonArray())) {
            Location location = QuestTaskAdapter.LOCATION_ADAPTER.fromJsonTree(element);
            if (location != null)
                elements.add(location);
        }
    }
    
    public enum Visibility {
        FULL("Full", true, true),
        LOCATION("Location", true, false),
        NONE("None", false, false);
        
        private boolean showCoordinate;
        private boolean showRadius;
        private String id;
        
        Visibility(String id, boolean showCoordinate, boolean showRadius) {
            this.id = id;
            this.showCoordinate = showCoordinate;
            this.showRadius = showRadius;
        }
        
        public boolean doShowCoordinate() {
            return showCoordinate;
        }
        
        public boolean doShowRadius() {
            return showRadius;
        }
        
        public String getName() {
            return Translator.get("hqm.locationMenu.vis" + id + ".title");
        }
        
        public String getDescription() {
            return Translator.get("hqm.locationMenu.vis" + id + ".desc");
        }
    }
    
    public static class Location extends IconTask {
        
        private BlockPos pos;
        private int radius = 3;
        private Visibility visibility = Visibility.LOCATION;
        private String dimension;
        
        public BlockPos getPosition() {
            return pos;
        }
        
        public void setPosition(BlockPos pos) {
            this.pos = pos;
        }
        
        public int getRadius() {
            return radius;
        }
        
        public void setRadius(int radius) {
            this.radius = radius;
        }
        
        public Visibility getVisibility() {
            return visibility;
        }
        
        public void setVisibility(Visibility visibility) {
            this.visibility = visibility;
        }
        
        public String getDimension() {
            return dimension;
        }
        
        public void setDimension(String dimension) {
            this.dimension = dimension;
        }
    }
}
