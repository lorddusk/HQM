package hardcorequesting.common.quests.task.icon;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hardcorequesting.common.event.EventTrigger;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.data.LocationTaskData;
import hardcorequesting.common.quests.task.client.LocationTaskGraphic;
import hardcorequesting.common.quests.task.client.TaskGraphic;
import hardcorequesting.common.team.Team;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

/**
 * A task where the player has to visit certain locations.
 */
public class VisitLocationTask extends IconLayoutTask<VisitLocationTask.Part, LocationTaskData> {
    private static final String LOCATIONS = "locations";
    
    private static final int CHECK_DELAY = 20;
    private int delay = 1;
    
    public VisitLocationTask(Quest parent, String description, String longDescription) {
        super(LocationTaskData.class, EditType.Type.LOCATION, parent, description, longDescription);
        
        register(EventTrigger.Type.SERVER, EventTrigger.Type.PLAYER);
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public TaskGraphic createGraphic(UUID playerId) {
        return new LocationTaskGraphic(this, parts, playerId);
    }
    
    @Override
    protected Part createEmpty() {
        return new Part();
    }
    
    private void tick(Player player, boolean isPlayerEvent) {
        if (!isPlayerEvent) {
            delay++;
        } else if (this.delay >= 0) {
            delay = 0;
            
            Level world = player.getCommandSenderWorld();
            if (!world.isClientSide) {
                LocationTaskData data = this.getData(player);
                boolean all = true;
                boolean updated = false;
                
                for (int i = 0; i < parts.size(); ++i) {
                    Part part = this.parts.get(i);
                    
                    if (!data.getValue(i) && Objects.equals(player.getCommandSenderWorld().dimension().location().toString(), part.dimension)) {
                        int current = (int) player.distanceToSqr((double) part.pos.getX() + 0.5D, (double) part.pos.getY() + 0.5D, (double) part.pos.getZ() + 0.5D);
                        int target = part.radius * part.radius;
                        if (part.radius >= 0 && current > target) {
                            all = false;
                        } else {
                            if (!this.isCompleted(player) && this.isVisible(player) && this.parent.isEnabled(player) && this.parent.isAvailable(player)) {
                                updated = true;
                                data.complete(i);
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
    
    public boolean visited(int id, Player player) {
        return getData(player).getValue(id);
    }
    
    public void setInfo(int id, Visibility visibility, BlockPos pos, int radius, String dimension) {
        Part part = parts.getOrCreateForModify(id);
        part.setVisibility(visibility);
        part.setPosition(pos);
        part.setRadius(radius);
        part.setDimension(dimension);
    }
    
    @Override
    public LocationTaskData newQuestData() {
        return new LocationTaskData(parts.size());
    }
    
    @Override
    public void onUpdate(Player player) {
        
    }
    
    @Override
    public float getCompletedRatio(Team team) {
        return getData(team).getCompletedRatio(parts.size());
    }
    
    @Override
    public void mergeProgress(UUID playerID, LocationTaskData own, LocationTaskData other) {
        own.mergeResult(other);
        
        if (own.areAllCompleted(parts.size())) {
            completeTask(playerID);
        }
    }
    
    @Override
    public void setComplete(LocationTaskData data) {
        for (int i = 0; i < parts.size(); i++) {
            data.complete(i);
        }
        data.completed = true;
    }
    
    @Override
    public void copyProgress(LocationTaskData own, LocationTaskData other) {
        own.update(other);
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
        builder.add(LOCATIONS, parts.write(QuestTaskAdapter.LOCATION_ADAPTER));
    }
    
    @SuppressWarnings("ConstantConditions")
    @Override
    public void read(JsonObject object) {
        parts.read(GsonHelper.getAsJsonArray(object, LOCATIONS, new JsonArray()), QuestTaskAdapter.LOCATION_ADAPTER);
    }
    
    public enum Visibility {
        FULL("Full", true, true),
        LOCATION("Location", true, false),
        NONE("None", false, false);
        
        private final boolean showCoordinate;
        private final boolean showRadius;
        private final String id;
        
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
    
    public static class Part extends IconLayoutTask.Part {
        
        @NotNull
        private BlockPos pos = BlockPos.ZERO;
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
