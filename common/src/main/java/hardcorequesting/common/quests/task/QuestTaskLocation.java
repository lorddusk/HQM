package hardcorequesting.common.quests.task;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiColor;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenuLocation;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenuTextEditor;
import hardcorequesting.common.client.interfaces.edit.PickItemMenu;
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
import net.minecraft.network.chat.FormattedText;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class QuestTaskLocation extends IconQuestTask<QuestTaskLocation.Location> {
    private static final String LOCATIONS = "locations";
    
    private static final int CHECK_DELAY = 20;
    private static final int Y_OFFSET = 30;
    private static final int X_TEXT_OFFSET = 23;
    private static final int X_TEXT_INDENT = 0;
    private static final int Y_TEXT_OFFSET = 0;
    private static final int ITEM_SIZE = 18;
    private int delay = 1;
    
    public QuestTaskLocation(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
        
        register(EventTrigger.Type.SERVER, EventTrigger.Type.PLAYER);
    }
    
    @Override
    protected Location createEmpty() {
        return new Location();
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
                        int current = (int) player.distanceToSqr((double) location.x + 0.5D, (double) location.y + 0.5D, (double) location.z + 0.5D);
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
    
    public void setLocation(int id, Location location, Player player) {
        if (id >= elements.size()) {
            elements.add(location);
            QuestDataTaskLocation data = (QuestDataTaskLocation) getData(player);
            data.visited = Arrays.copyOf(data.visited, data.visited.length + 1);
            SaveHelper.add(SaveHelper.EditType.LOCATION_CREATE);
        } else {
            elements.set(id, location);
            SaveHelper.add(SaveHelper.EditType.LOCATION_CHANGE);
        }
    }
    
    public void setIcon(int id, ItemStack stack, Player player) {
        if (stack.isEmpty()) return;
        
        setLocation(id, id >= elements.size() ? createEmpty() : elements.get(id), player);
        
        elements.get(id).setIconStack(stack);
    }
    
    public void setName(int id, String str, Player player) {
        setLocation(id, id >= elements.size() ? createEmpty() : elements.get(id), player);
    
        elements.get(id).setName(str);
    }
    
    @Override
    public Class<? extends QuestDataTask> getDataType() {
        return QuestDataTaskLocation.class;
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void draw(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY) {
        List<Location> locations = getShownElements();
        for (int i = 0; i < locations.size(); i++) {
            Location location = locations.get(i);
            
            int x = START_X;
            int y = START_Y + i * Y_OFFSET;
            gui.drawItemStack(location.getIconStack(), x, y, mX, mY, false);
            gui.drawString(matrices, Translator.plain(location.getName()), x + X_TEXT_OFFSET, y + Y_TEXT_OFFSET, 0x404040);
            
            if (visited(i, player)) {
                gui.drawString(matrices, Translator.translatable("hqm.locationMenu.visited", GuiColor.GREEN), x + X_TEXT_OFFSET + X_TEXT_INDENT, y + Y_TEXT_OFFSET + 9, 0.7F, 0x404040);
            } else if (location.visibility.doShowCoordinate()) {
                if (location.radius >= 0) {
                    gui.drawString(matrices, Translator.plain("(" + location.x + ", " + location.y + ", " + location.z + ")"), x + X_TEXT_OFFSET + X_TEXT_INDENT, y + Y_TEXT_OFFSET + 9, 0.7F, 0x404040);
                }
                
                if (Objects.equals(player.getCommandSenderWorld().dimension().location().toString(), location.dimension)) {
                    if (location.radius >= 0) {
                        FormattedText str;
                        int distance = (int) player.distanceToSqr(location.x + 0.5, location.y + 0.5, location.z + 0.5);
                        str = Translator.translatable("hqm.locationMenu.mAway", distance);
                        if (location.visibility.doShowRadius()) {
                            str = FormattedText.composite(str, Translator.plain(" ["), Translator.translatable("hqm.locationMenu.mRadius", location.radius), Translator.plain("]"));
                        }
                        gui.drawString(matrices, str, x + X_TEXT_OFFSET + X_TEXT_INDENT, y + Y_TEXT_OFFSET + 15, 0.7F, 0x404040);
                    }
                    
                } else {
                    gui.drawString(matrices, Translator.translatable("hqm.locationMenu.wrongDim"), x + X_TEXT_OFFSET + X_TEXT_INDENT, y + Y_TEXT_OFFSET + (location.radius >= 0 ? 15 : 9), 0.7F, 0x404040);
                }
                
            }
            
        }
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void onClick(GuiQuestBook gui, Player player, int mX, int mY, int b) {
        if (Quest.canQuestsBeEdited() && gui.getCurrentMode() != EditMode.NORMAL) {
            List<Location> locations = getShownElements();
            for (int i = 0; i < locations.size(); i++) {
                Location location = locations.get(i);
                
                int x = START_X;
                int y = START_Y + i * Y_OFFSET;
                
                if (gui.inBounds(x, y, ITEM_SIZE, ITEM_SIZE, mX, mY)) {
                    switch (gui.getCurrentMode()) {
                        case LOCATION:
                            gui.setEditMenu(new GuiEditMenuLocation(gui, this, location.copy(), i, player));
                            break;
                        case ITEM:
                            final int locationId = i;
                            PickItemMenu.display(gui, player, location.getIconStack(), PickItemMenu.Type.ITEM,
                                    result -> this.setIcon(locationId, result.get(), player));
                            break;
                        case RENAME:
                            gui.setEditMenu(new GuiEditMenuTextEditor(gui, player, this, i, location));
                            break;
                        case DELETE:
                            if (i < this.elements.size()) {
                                elements.remove(i);
                                SaveHelper.add(SaveHelper.EditType.LOCATION_REMOVE);
                            }
                            break;
                        default:
                    }
                    
                    break;
                }
            }
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
        
        private int x;
        private int y;
        private int z;
        private int radius = 3;
        private Visibility visibility = Visibility.LOCATION;
        private String dimension;
        
        private Location copy() {
            Location location = new Location();
            location.setIconStack(getIconStack().copy());
            location.copyFrom(this);
            location.x = x;
            location.y = y;
            location.z = z;
            location.radius = radius;
            location.visibility = visibility;
            location.dimension = dimension;
            
            return location;
        }
        
        public int getX() {
            return x;
        }
        
        public void setX(int x) {
            this.x = x;
        }
        
        public int getY() {
            return y;
        }
        
        public void setY(int y) {
            this.y = y;
        }
        
        public int getZ() {
            return z;
        }
        
        public void setZ(int z) {
            this.z = z;
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
