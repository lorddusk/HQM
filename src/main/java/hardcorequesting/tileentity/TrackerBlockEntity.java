package hardcorequesting.tileentity;

import hardcorequesting.blocks.ModBlocks;
import hardcorequesting.client.ClientChange;
import hardcorequesting.client.interfaces.GuiBase;
import hardcorequesting.client.interfaces.GuiType;
import hardcorequesting.client.interfaces.GuiWrapperEditMenu;
import hardcorequesting.client.interfaces.edit.GuiEditMenuTracker;
import hardcorequesting.network.NetworkManager;
import hardcorequesting.quests.Quest;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.UUID;

public class TrackerBlockEntity extends BlockEntity implements Tickable {
    
    private static final String NBT_QUEST = "Quest";
    private static final String NBT_RADIUS = "Radius";
    private static final String NBT_TYPE = "TrackerType";
    private Quest quest;
    private UUID questId;
    private int radius;
    private TrackerType type = TrackerType.TEAM;
    private int delay = 0;
    
    public TrackerBlockEntity() {
        super(ModBlocks.typeTracker);
    }
    
    private static TrackerBlockEntity getTracker(World world, BlockPos pos) {
        BlockEntity te = world.getBlockEntity(pos);
        return (te instanceof TrackerBlockEntity) ? (TrackerBlockEntity) te : null;
    }
    
    @Environment(EnvType.CLIENT)
    public static void openInterface(PlayerEntity player, BlockPos pos, UUID questId, int radius, TrackerType type) {
        TrackerBlockEntity tracker = getTracker(player.world, pos);
        if (tracker != null) {
            tracker.questId = questId;
            tracker.quest = null;
            tracker.radius = radius;
            tracker.type = type;
            GuiBase gui = new GuiWrapperEditMenu();
            gui.setEditMenu(new GuiEditMenuTracker(gui, player, tracker));
            MinecraftClient.getInstance().openScreen(gui);
        }
    }
    
    public static void saveToServer(PlayerEntity player, BlockPos pos, int radius, TrackerType type) {
        TrackerBlockEntity tracker = getTracker(player.world, pos);
        if (Quest.canQuestsBeEdited() && tracker != null) {
            tracker.radius = radius;
            tracker.type = type;
        }
    }
    
    @Override
    public void fromTag(CompoundTag compound) {
        super.fromTag(compound);
        
        // the following six lines are legacy code from the playername to UUID migration. can be removed in 1.14
        if (compound.contains(NBT_QUEST)) {
            try {
                compound.putUuid(NBT_QUEST, UUID.fromString(compound.getString(NBT_QUEST)));
            } catch (IllegalArgumentException ignored) {
            }
            compound.remove(NBT_QUEST);
        }
        
        if (compound.contains(NBT_QUEST + "Most")) {
            questId = compound.getUuid(NBT_QUEST);
        } else {
            quest = null;
        }
        radius = compound.getInt(NBT_RADIUS);
        type = TrackerType.values()[compound.getByte(NBT_TYPE)];
    }
    
    @Override
    public CompoundTag toTag(CompoundTag compound) {
        super.toTag(compound);
        
        if (quest != null) {
            compound.putUuid(NBT_QUEST, quest.getQuestId());
        }
        compound.putInt(NBT_RADIUS, radius);
        compound.putByte(NBT_TYPE, (byte) type.ordinal());
        return compound;
    }
    
    @Override
    public void tick() {
        if (quest == null && questId != null) {
            quest = Quest.getQuest(questId);
            questId = null;
        }

//        if (!world.isClient && delay++ == 20) {
//            if (quest != null && Quest.getQuest(quest.getQuestId()) == null) {
//                quest = null;
//            }
//            int oldMeta = world.getBlockState(pos).getBlock().getMetaFromState(ModBlocks.blockTracker.getDefaultState());
//            int meta = 0;
//            if (quest != null) {
//                meta = type.getMeta(this, quest, radius);
//            }
//            
//            if (oldMeta != meta) {
//                world.setBlockState(pos, ModBlocks.blockTracker.getDefaultState(), 3);
//                notifyUpdate(pos.getX(), pos.getY(), pos.getZ(), 2);
//            }
//            
//            delay = 0;
//        }
    }
    
    private void notifyUpdate(int x, int y, int z, int i) {
        if (i == 2 || x != pos.getX() || y != pos.getY() || z != pos.getZ()) {
            world.updateNeighborsAlways(pos, getCachedState().getBlock());
            
            if (i > 0) {
                notifyUpdate(x - 1, y, z, i - 1);
                notifyUpdate(x + 1, y, z, i - 1);
                notifyUpdate(x, y - 1, z, i - 1);
                notifyUpdate(x, y + 1, z, i - 1);
                notifyUpdate(x, y, z - 1, i - 1);
                notifyUpdate(x, y, z + 1, i - 1);
            }
        }
    }
    
    public int getRadius() {
        return radius;
    }
    
    public void setRadius(int radius) {
        this.radius = radius;
    }
    
    public TrackerType getTrackerType() {
        return type;
    }
    
    public void setTrackerType(TrackerType type) {
        this.type = type;
    }
    
    public void setCurrentQuest() {
        quest = Quest.getQuest(Quest.speciallySelectedQuestId);
    }
    
    public Quest getCurrentQuest() {
        return quest;
    }
    
    public void openInterface(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity)
            NetworkManager.sendToPlayer(GuiType.TRACKER.build(build()), (ServerPlayerEntity) player);
    }
    
    private String[] build() {
        String[] data = new String[4];
        data[0] = "" + pos.asLong();
        data[1] = quest != null ? quest.getQuestId().toString() : null;
        data[2] = "" + radius;
        data[3] = "" + type.ordinal();
        return data;
    }
    
    public void sendToServer() {
        NetworkManager.sendToServer(ClientChange.TRACKER_UPDATE.build(this));
    }
}
