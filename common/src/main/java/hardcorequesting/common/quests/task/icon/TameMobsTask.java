package hardcorequesting.common.quests.task.icon;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiColor;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.PickMobMenu;
import hardcorequesting.common.event.EventTrigger;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.data.TameTaskData;
import hardcorequesting.common.team.Team;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

import java.util.UUID;

/**
 * A task where the player needs to part certain mobs.
 */
public class TameMobsTask extends IconLayoutTask<TameMobsTask.Part, TameTaskData> {
    private static final String TAME = "part";
    
    public static final ResourceLocation ABSTRACT_HORSE = new ResourceLocation("abstracthorse");
    
    public TameMobsTask(Quest parent, String description, String longDescription) {
        super(TameTaskData.class, EditType.Type.MONSTER, parent, description, longDescription);
        register(EventTrigger.Type.ANIMAL_TAME);
    }
    
    @Override
    protected Part createEmpty() {
        return new Part();
    }
    
    @Environment(EnvType.CLIENT)
    private void setInfo(int id, String entityId, int amount) {
        
        Part part = parts.getOrCreateForModify(id);
        part.setTame(entityId);
        part.setCount(amount);
        
        if(entityId != null && (part.getIconStack().isEmpty() || part.getIconStack().getItem() instanceof SpawnEggItem)) {
            EntityType<?> entityType = Registry.ENTITY_TYPE.get(new ResourceLocation(entityId));
            if(entityType != null) {
                Item egg = SpawnEggItem.byId(entityType);
                if(egg != null) {
                    part.setIconStack(new ItemStack(egg));
                }
            }
        }
    
    }
    
    private int tamed(int id, Player player) {
        return getData(player).getValue(id);
    }
    
    @Override
    public TameTaskData newQuestData() {
        return new TameTaskData(parts.size());
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    protected void drawElementText(PoseStack matrices, GuiQuestBook gui, Player player, Part part, int index, int x, int y) {
        int tamed = tamed(index, player);
        if (tamed == part.count) {
            gui.drawString(matrices, Translator.translatable("hqm.tameTask.allTamed", GuiColor.GREEN), x, y, 0.7F, 0x404040);
        } else {
            gui.drawString(matrices, Translator.translatable("hqm.tameTask.partTames", tamed, (100 * tamed / part.count)), x, y, 0.7F, 0x404040);
        }
        gui.drawString(matrices, Translator.translatable("hqm.tameTask.totalTames", part.count), x, y + 6, 0.7F, 0x404040);
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    protected void handleElementEditClick(GuiQuestBook gui, Player player, EditMode mode, int id, Part part) {
        if (mode == EditMode.MOB) {
            PickMobMenu.display(gui, player, part.getTame() == null ? null : ResourceLocation.tryParse(part.getTame()), part.getCount(), "tameTask",
                    PickMobMenu.EXTRA_TAME_ENTRIES, result -> setInfo(id, result.getMobId().toString(), result.getAmount()));
        }
    }
    
    @Override
    public void onUpdate(Player player) {
        
    }
    
    @Override
    public float getCompletedRatio(Team team) {
        TameTaskData data = getData(team);
        int tamed = 0;
        int total = 0;
        
        for (int i = 0; i < parts.size(); i++) {
            int req = parts.get(i).count;
            tamed += Math.min(req, data.getValue(i));
            total += req;
        }
        
        return (float) tamed / total;
    }
    
    @Override
    public void mergeProgress(UUID uuid, TameTaskData own, TameTaskData other) {
        own.merge(other);
    
        boolean all = true;
        for (int i = 0; i < parts.size(); i++) {
            if (own.getValue(i) < parts.get(i).count) {
                all = false;
                break;
            }
        }
        
        if (all) {
            completeTask(uuid);
        }
    }
    
    @Override
    public void setComplete(TameTaskData data) {
        for (int i = 0; i < parts.size(); i++) {
            data.setValue(i, parts.get(i).count);
        }
        data.completed = true;
    }
    
    @Override
    public void copyProgress(TameTaskData own, TameTaskData other) {
        own.update(other);
    }
    
    @Override
    public void onAnimalTame(Player tamer, Entity entity) {
        if (tamer != null && parent.isEnabled(tamer) && parent.isAvailable(tamer) && this.isVisible(tamer) && !isCompleted(tamer)) {
            TameTaskData data = getData(tamer);
            boolean updated = false;
            for (int i = 0; i < parts.size(); i++) {
                Part part = parts.get(i);
                if (part.count > data.getValue(i) && part.mobId != null) {
                    if (part.mobId.equals(ABSTRACT_HORSE.toString())) {
                        if (entity instanceof AbstractHorse) {
                            data.setValue(i, data.getValue(i) + 1);
                            updated = true;
                        }
                    } else {
                        EntityType<?> type = Registry.ENTITY_TYPE.get(new ResourceLocation(part.mobId));
                        if (type != null) {
                            if (type.equals(entity.getType())) {
                                data.setValue(i, data.getValue(i) + 1);
                                updated = true;
                            }
                        }
                    }
                }
            }
            
            if (updated) {
                boolean done = true;
                for (int i = 0; i < parts.size(); i++) {
                    Part part = parts.get(i);
                    
                    if (tamed(i, tamer) < part.count) {
                        done = false;
                        break;
                    }
                }
                
                if (done) {
                    completeTask(tamer.getUUID());
                }
                
                parent.sendUpdatedDataToTeam(tamer);
            }
        }
    }
    
    @Override
    public void write(Adapter.JsonObjectBuilder builder) {
        builder.add(TAME, parts.write(QuestTaskAdapter.TAME_ADAPTER));
    }
    
    @SuppressWarnings("ConstantConditions")
    @Override
    public void read(JsonObject object) {
        parts.read(GsonHelper.getAsJsonArray(object, TAME, new JsonArray()), QuestTaskAdapter.TAME_ADAPTER);
    }
    
    public static class Part extends IconLayoutTask.Part {
        private String mobId;
        private int count = 1;
        
        public String getTame() {
            return mobId;
        }
        
        public void setTame(String part) {
            this.mobId = part;
        }
        
        public int getCount() {
            return count;
        }
        
        public void setCount(int count) {
            this.count = count;
        }
    }
}
