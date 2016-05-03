package hardcorequesting.quests;

import hardcorequesting.EventHandler;
import hardcorequesting.FileVersion;
import hardcorequesting.SaveHelper;
import hardcorequesting.Translator;
import hardcorequesting.client.EditMode;
import hardcorequesting.client.interfaces.*;
import hardcorequesting.network.DataBitHelper;
import hardcorequesting.network.DataReader;
import hardcorequesting.network.DataWriter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;

public class QuestTaskLocation extends QuestTask {


    private int delay = 1;
    private static final int CHECK_DELAY = 20;

    @Override
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.side == Side.SERVER) {
            tick(null, false);
        }
    }

    @Override
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side == Side.SERVER) {
            tick(event.player, true);
        }
    }

    private void tick(EntityPlayer player, boolean isPlayerEvent) {
        if (!isPlayerEvent) {
            delay++;
            delay %= CHECK_DELAY;
        } else if (this.delay == 0) {
            World world = player.worldObj;
            if (!world.isRemote) {
                boolean[] visited = ((QuestDataTaskLocation) this.getData(player)).visited;
                boolean all = true;
                boolean updated = false;

                for (int i = 0; i < locations.length; ++i) {
                    Location location = this.locations[i];
                    if (!visited[i] && player.worldObj.provider.getDimension() == location.dimension) {
                        int current = (int) player.getDistanceSq((double) location.x + 0.5D, (double) location.y + 0.5D, (double) location.z + 0.5D);
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
                        completeTask(player.getGameProfile().getName());
                    }
                    parent.sendUpdatedDataToTeam(player);
                }
            }
        }
    }


    public static class Location {

        private ItemStack icon;
        private String name = "New";
        private int x;
        private int y;
        private int z;
        private int radius = 3;
        private Visibility visible = Visibility.LOCATION;
        private int dimension;

        private Location copy() {
            Location location = new Location();
            location.icon = icon == null ? null : icon.copy();
            location.name = name;
            location.x = x;
            location.y = y;
            location.z = z;
            location.radius = radius;
            location.visible = visible;
            location.dimension = dimension;

            return location;
        }

        public ItemStack getIcon() {
            return icon;
        }

        public void setIcon(ItemStack icon) {
            this.icon = icon;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
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

        public Visibility getVisible() {
            return visible;
        }

        public void setVisible(Visibility visible) {
            this.visible = visible;
        }

        public void save(DataWriter dw) {
            dw.writeBoolean(icon != null);
            if (icon != null) {
                dw.writeItem(icon.getItem());
                dw.writeData(icon.getItemDamage(), DataBitHelper.SHORT);
                dw.writeNBT(icon.getTagCompound());
            }
            dw.writeString(name, DataBitHelper.NAME_LENGTH);
            dw.writeData(x, DataBitHelper.WORLD_COORDINATE);
            dw.writeData(y, DataBitHelper.WORLD_COORDINATE);
            dw.writeData(z, DataBitHelper.WORLD_COORDINATE);
            dw.writeData(radius, DataBitHelper.WORLD_COORDINATE);
            dw.writeData(visible.ordinal(), DataBitHelper.LOCATION_VISIBILITY);
            dw.writeData(dimension, DataBitHelper.WORLD_COORDINATE);
        }

        public void load(DataReader dr, FileVersion version) {
            if (dr.readBoolean()) {
                Item item = dr.readItem();
                int dmg = dr.readData(DataBitHelper.SHORT);
                NBTTagCompound compound = dr.readNBT();
                ItemStack itemStack = new ItemStack(item, 1, dmg);
                itemStack.setTagCompound(compound);
                this.icon = itemStack;
            } else {
                this.icon = null;
            }
            name = dr.readString(DataBitHelper.NAME_LENGTH);
            x = dr.readData(DataBitHelper.WORLD_COORDINATE);
            y = dr.readData(DataBitHelper.WORLD_COORDINATE);
            z = dr.readData(DataBitHelper.WORLD_COORDINATE);
            radius = dr.readData(DataBitHelper.WORLD_COORDINATE);
            visible = Visibility.values()[dr.readData(DataBitHelper.LOCATION_VISIBILITY)];
            dimension = dr.readData(DataBitHelper.WORLD_COORDINATE);
        }

        public int getDimension() {
            return dimension;
        }

        public void setDimension(int dimension) {
            this.dimension = dimension;
        }
    }

    public enum Visibility {
        FULL("Full", true, true),
        LOCATION("Location", true, false),
        NONE("None", false, false);

        private boolean showCoordinate;
        private boolean showRadius;
        private String id;

        public boolean doShowCoordinate() {
            return showCoordinate;
        }

        public boolean doShowRadius() {
            return showRadius;
        }

        Visibility(String id, boolean showCoordinate, boolean showRadius) {
            this.id = id;
            this.showCoordinate = showCoordinate;
            this.showRadius = showRadius;
        }

        public String getName() {
            return Translator.translate("hqm.locationMenu.vis" + id + ".title");
        }

        public String getDescription() {
            return Translator.translate("hqm.locationMenu.vis" + id + ".desc");
        }
    }

    public Location[] locations = new Location[0];

    private Location[] getEditFriendlyLocations(Location[] locations) {
        if (Quest.isEditing && locations.length < DataBitHelper.TASK_LOCATION_COUNT.getMaximum()) {
            locations = Arrays.copyOf(locations, locations.length + 1);
            locations[locations.length - 1] = new Location();
            return locations;
        } else {
            return locations;
        }
    }

    public QuestTaskLocation(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);

        register(EventHandler.Type.SERVER, EventHandler.Type.PLAYER);
    }

    private boolean visited(int id, EntityPlayer player) {
        return id < locations.length && ((QuestDataTaskLocation) getData(player)).visited[id];
    }


    public void setLocation(int id, Location location, EntityPlayer player) {
        if (id >= locations.length) {
            locations = Arrays.copyOf(locations, locations.length + 1);
            QuestDataTaskLocation data = (QuestDataTaskLocation) getData(player);
            data.visited = Arrays.copyOf(data.visited, data.visited.length + 1);
            SaveHelper.add(SaveHelper.EditType.LOCATION_CREATE);
        } else {
            SaveHelper.add(SaveHelper.EditType.LOCATION_CHANGE);
        }

        locations[id] = location;
    }

    public void setIcon(int id, ItemStack item, EntityPlayer player) {
        setLocation(id, id >= locations.length ? new Location() : locations[id], player);

        locations[id].icon = item;
    }


    public void setName(int id, String str, EntityPlayer player) {
        setLocation(id, id >= locations.length ? new Location() : locations[id], player);

        locations[id].name = str;
    }

    @Override
    public void save(DataWriter dw) {
        dw.writeData(locations.length, DataBitHelper.TASK_LOCATION_COUNT);
        for (Location location : locations) {
            location.save(dw);
        }
    }

    @Override
    public void load(DataReader dr, FileVersion version) {
        int count = dr.readData(DataBitHelper.TASK_LOCATION_COUNT);
        locations = new Location[count];
        for (int i = 0; i < locations.length; i++) {
            locations[i] = new Location();
            locations[i].load(dr, version);
        }
    }

    @Override
    public void write(DataWriter dw, QuestDataTask task, boolean light) {
        super.write(dw, task, light);

        if (!light) {
            dw.writeData(((QuestDataTaskLocation) task).visited.length, DataBitHelper.TASK_LOCATION_COUNT);
        }
        for (boolean b : ((QuestDataTaskLocation) task).visited) {
            dw.writeBoolean(b);
        }
    }

    @Override
    public void read(DataReader dr, QuestDataTask task, FileVersion version, boolean light) {
        super.read(dr, task, version, light);

        QuestDataTaskLocation locationData = ((QuestDataTaskLocation) task);

        if (light) {
            boolean[] visited = locationData.visited;
            for (int i = 0; i < visited.length; i++) {
                visited[i] = dr.readBoolean();
            }
        } else {
            int count = dr.readData(DataBitHelper.TASK_LOCATION_COUNT);
            boolean[] visited = locationData.visited;
            for (int i = 0; i < count; i++) {
                boolean val = dr.readBoolean();
                if (i < visited.length) {
                    visited[i] = val;
                }
            }
        }
    }

    private static final int Y_OFFSET = 30;
    private static final int X_TEXT_OFFSET = 23;
    private static final int X_TEXT_INDENT = 0;
    private static final int Y_TEXT_OFFSET = 0;
    private static final int ITEM_SIZE = 18;

    @SideOnly(Side.CLIENT)
    @Override
    public void draw(GuiQuestBook gui, EntityPlayer player, int mX, int mY) {
        Location[] locations = getEditFriendlyLocations(this.locations);
        for (int i = 0; i < locations.length; i++) {
            Location location = locations[i];

            int x = START_X;
            int y = START_Y + i * Y_OFFSET;
            gui.drawItem(location.icon, x, y, mX, mY, false);
            gui.drawString(location.name, x + X_TEXT_OFFSET, y + Y_TEXT_OFFSET, 0x404040);

            if (visited(i, player)) {
                gui.drawString(GuiColor.GREEN + Translator.translate("hqm.locationMenu.visited"), x + X_TEXT_OFFSET + X_TEXT_INDENT, y + Y_TEXT_OFFSET + 9, 0.7F, 0x404040);
            } else if (location.visible.doShowCoordinate()) {
                if (location.radius >= 0) {
                    gui.drawString("(" + location.x + ", " + location.y + ", " + location.z + ")", x + X_TEXT_OFFSET + X_TEXT_INDENT, y + Y_TEXT_OFFSET + 9, 0.7F, 0x404040);
                }

                if (player.worldObj.provider.getDimension() == location.dimension) {
                    if (location.radius >= 0) {
                        String str;
                        int distance = (int) player.getDistance(location.x + 0.5, location.y + 0.5, location.z + 0.5);
                        str = Translator.translate("hqm.locationMenu.mAway", distance);
                        if (location.visible.doShowRadius()) {
                            str += " [" + Translator.translate("hqm.locationMenu.mRadius", location.radius) + "]";
                        }
                        gui.drawString(str, x + X_TEXT_OFFSET + X_TEXT_INDENT, y + Y_TEXT_OFFSET + 15, 0.7F, 0x404040);
                    }

                } else {
                    gui.drawString(Translator.translate("hqm.locationMenu.wrongDim"), x + X_TEXT_OFFSET + X_TEXT_INDENT, y + Y_TEXT_OFFSET + (location.radius >= 0 ? 15 : 9), 0.7F, 0x404040);
                }

            }

        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void onClick(GuiQuestBook gui, EntityPlayer player, int mX, int mY, int b) {
        if (Quest.isEditing && gui.getCurrentMode() != EditMode.NORMAL) {
            Location[] locations = getEditFriendlyLocations(this.locations);
            for (int i = 0; i < locations.length; i++) {
                Location location = locations[i];

                int x = START_X;
                int y = START_Y + i * Y_OFFSET;

                if (gui.inBounds(x, y, ITEM_SIZE, ITEM_SIZE, mX, mY)) {
                    switch (gui.getCurrentMode()) {
                        case LOCATION:
                            gui.setEditMenu(new GuiEditMenuLocation(gui, this, location.copy(), i, player));
                            break;
                        case ITEM:
                            gui.setEditMenu(new GuiEditMenuItem(gui, player, location.icon, i, GuiEditMenuItem.Type.LOCATION, 1, ItemPrecision.PRECISE));
                            break;
                        case RENAME:
                            gui.setEditMenu(new GuiEditMenuTextEditor(gui, player, this, i, location));
                            break;
                        case DELETE:
                            if (i < this.locations.length) {
                                Location[] newLocations = new Location[this.locations.length - 1];
                                int id = 0;
                                for (int j = 0; j < this.locations.length; j++) {
                                    if (j != i) {
                                        newLocations[id] = this.locations[j];
                                        id++;
                                    }
                                }
                                this.locations = newLocations;
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
    public void onUpdate(EntityPlayer player, DataReader dr) {

    }

    @Override
    public Class<? extends QuestDataTask> getDataType() {
        return QuestDataTaskLocation.class;
    }

    @Override
    public float getCompletedRatio(String playerName) {
        int visited = 0;
        for (boolean b : ((QuestDataTaskLocation) getData(playerName)).visited) {
            if (b) {
                visited++;
            }
        }

        return (float) visited / locations.length;
    }

    @Override
    public void mergeProgress(String playerName, QuestDataTask own, QuestDataTask other) {
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
            completeTask(playerName);
        }
    }

    @Override
    public void copyProgress(QuestDataTask own, QuestDataTask other) {
        super.copyProgress(own, other);
        boolean[] visited = ((QuestDataTaskLocation) own).visited;
        System.arraycopy(((QuestDataTaskLocation) other).visited, 0, visited, 0, visited.length);
    }

    @Override
    public void autoComplete(String playerName) {
        boolean[] visited = ((QuestDataTaskLocation) getData(playerName)).visited;
        for (int i = 0; i < visited.length; i++) {
            visited[i] = true;
        }
    }
}
