package hardcorequesting.common.tileentity;

import hardcorequesting.common.blocks.ModBlocks;
import hardcorequesting.common.client.ClientChange;
import hardcorequesting.common.client.interfaces.EditTrackerScreen;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiType;
import hardcorequesting.common.network.NetworkManager;
import hardcorequesting.common.quests.Quest;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class TrackerBlockEntity extends BlockEntity {
    
    private static final String NBT_QUEST = "Quest";
    private static final String NBT_RADIUS = "Radius";
    private static final String NBT_TYPE = "TrackerType";
    private Quest quest;
    private UUID questId;
    private int radius;
    private TrackerType type = TrackerType.TEAM;
    private int delay = 0;
    
    public TrackerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.typeTracker.get(), pos, state);
    }
    
    private static TrackerBlockEntity getTracker(Level world, BlockPos pos) {
        BlockEntity te = world.getBlockEntity(pos);
        return (te instanceof TrackerBlockEntity) ? (TrackerBlockEntity) te : null;
    }
    
    @Environment(EnvType.CLIENT)
    public static void openInterface(Player player, BlockPos pos, UUID questId, int radius, TrackerType type) {
        TrackerBlockEntity tracker = getTracker(player.level, pos);
        if (tracker != null) {
            tracker.questId = questId;
            tracker.quest = null;
            tracker.radius = radius;
            tracker.type = type;
            GuiBase gui = new EditTrackerScreen(tracker);
            Minecraft.getInstance().setScreen(gui);
        }
    }
    
    public static void saveToServer(Player player, BlockPos pos, int radius, TrackerType type) {
        TrackerBlockEntity tracker = getTracker(player.level, pos);
        if (Quest.canQuestsBeEdited() && tracker != null) {
            tracker.radius = radius;
            tracker.type = type;
        }
    }
    
    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        
        // the following six lines are legacy code from the playername to UUID migration. can be removed in 1.14
        if (compound.contains(NBT_QUEST)) {
            try {
                compound.putUUID(NBT_QUEST, UUID.fromString(compound.getString(NBT_QUEST)));
            } catch (IllegalArgumentException ignored) {
            }
            compound.remove(NBT_QUEST);
        }
        
        if (compound.contains(NBT_QUEST + "Most")) {
            questId = compound.getUUID(NBT_QUEST);
        } else {
            quest = null;
        }
        radius = compound.getInt(NBT_RADIUS);
        type = TrackerType.values()[compound.getByte(NBT_TYPE)];
    }
    
    @Override
    public CompoundTag save(CompoundTag compound) {
        super.save(compound);
        
        if (quest != null) {
            compound.putUUID(NBT_QUEST, quest.getQuestId());
        }
        compound.putInt(NBT_RADIUS, radius);
        compound.putByte(NBT_TYPE, (byte) type.ordinal());
        return compound;
    }
    
    public static void tick(Level level, BlockPos blockPos, BlockState blockState, TrackerBlockEntity blockEntity) {
        if (blockEntity.quest == null && blockEntity.questId != null) {
            blockEntity.quest = Quest.getQuest(blockEntity.questId);
            blockEntity.questId = null;
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
        if (i == 2 || x != worldPosition.getX() || y != worldPosition.getY() || z != worldPosition.getZ()) {
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            
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
    
    public void openInterface(Player player) {
        if (player instanceof ServerPlayer)
            NetworkManager.sendToPlayer(GuiType.TRACKER.build(build()), (ServerPlayer) player);
    }
    
    private String[] build() {
        String[] data = new String[4];
        data[0] = "" + worldPosition.asLong();
        data[1] = quest != null ? quest.getQuestId().toString() : null;
        data[2] = "" + radius;
        data[3] = "" + type.ordinal();
        return data;
    }
    
    public void sendToServer() {
        NetworkManager.sendToServer(ClientChange.TRACKER_UPDATE.build(this));
    }
}
