package hardcorequesting.common.quests.task;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiColor;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenuTame;
import hardcorequesting.common.event.EventTrigger;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.data.QuestDataTask;
import hardcorequesting.common.quests.data.QuestDataTaskTame;
import hardcorequesting.common.util.SaveHelper;
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

import java.util.Arrays;
import java.util.UUID;

/**
 * A task where the player needs to tame certain mobs.
 */
public class TameMobsTask extends IconQuestTask<TameMobsTask.Tame> {
    private static final String TAME = "tame";
    
    public TameMobsTask(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
        register(EventTrigger.Type.ANIMAL_TAME);
    }
    
    @Override
    protected Tame createEmpty() {
        return new Tame();
    }
    
    @Override
    protected void onAddElement(Player player) {
        QuestDataTaskTame data = (QuestDataTaskTame) getData(player);
        data.tamed = Arrays.copyOf(data.tamed, data.tamed.length + 1);
        SaveHelper.add(SaveHelper.EditType.MONSTER_CREATE);
    }
    
    @Override
    protected void onModifyElement() {
        SaveHelper.add(SaveHelper.EditType.MONSTER_CHANGE);
    }
    
    @Override
    protected void onRemoveElement() {
        SaveHelper.add(SaveHelper.EditType.MONSTER_REMOVE);
    }
    
    @Environment(EnvType.CLIENT)
    private void setInfo(int id, String entityId, int amount, Player player) {
        
        Tame tame = getOrCreateForModify(id, player);
        tame.setTame(entityId);
        tame.setCount(amount);
        
        if(entityId != null && (tame.getIconStack().isEmpty() || tame.getIconStack().getItem() instanceof SpawnEggItem)) {
            EntityType<?> entityType = Registry.ENTITY_TYPE.get(new ResourceLocation(entityId));
            if(entityType != null) {
                Item egg = SpawnEggItem.byId(entityType);
                if(egg != null) {
                    tame.setIconStack(new ItemStack(egg));
                }
            }
        }
    
    }
    
    private int tamed(int id, Player player) {
        return id < elements.size() ? ((QuestDataTaskTame) getData(player)).tamed[id] : 0;
    }
    
    @Override
    public Class<? extends QuestDataTask> getDataType() {
        return QuestDataTaskTame.class;
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    protected void drawElementText(PoseStack matrices, GuiQuestBook gui, Player player, Tame tame, int index, int x, int y) {
        int tamed = tamed(index, player);
        if (tamed == tame.count) {
            gui.drawString(matrices, Translator.translatable("hqm.tameTask.allTamed", GuiColor.GREEN), x, y, 0.7F, 0x404040);
        } else {
            gui.drawString(matrices, Translator.translatable("hqm.tameTask.partTames", tamed, (100 * tamed / tame.count)), x, y, 0.7F, 0x404040);
        }
        gui.drawString(matrices, Translator.translatable("hqm.tameTask.totalTames", tame.count), x, y + 6, 0.7F, 0x404040);
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    protected void handleElementEditClick(GuiQuestBook gui, Player player, EditMode mode, int id, Tame tame) {
        if (mode == EditMode.MOB) {
            GuiEditMenuTame.display(gui, player, tame.getTame(), tame.getCount(),
                    result -> setInfo(id, result.getEntityId(), result.getAmount(), player));
        }
    }
    
    @Override
    public void onUpdate(Player player) {
        
    }
    
    @Override
    public float getCompletedRatio(UUID uuid) {
        int tamed = 0;
        int total = 0;
        
        for (int i = 0; i < elements.size(); i++) {
            tamed += ((QuestDataTaskTame) getData(uuid)).tamed[i];
            total += elements.get(i).count;
        }
        
        return (float) tamed / total;
    }
    
    @Override
    public void mergeProgress(UUID uuid, QuestDataTask own, QuestDataTask other) {
        int[] tamed = ((QuestDataTaskTame) own).tamed;
        int[] otherTamed = ((QuestDataTaskTame) other).tamed;
        
        boolean all = true;
        for (int i = 0; i < tamed.length; i++) {
            tamed[i] = Math.max(tamed[i], otherTamed[i]);
            if (tamed[i] < elements.get(i).count) {
                all = false;
            }
        }
        
        if (all) {
            completeTask(uuid);
        }
    }
    
    @Override
    public void autoComplete(UUID uuid, boolean status) {
        int[] tamed = ((QuestDataTaskTame) getData(uuid)).tamed;
        int q = tamed.length;
        for (int i = 0; i < q; i++) {
            // This can sometimes cause an array-out-of-bounds error
            if (q != tamed.length) q = tamed.length;
            if (status) {
                tamed[i] = elements.get(i).count;
            } else {
                tamed[i] = 0;
            }
        }
    }
    
    @Override
    public void copyProgress(QuestDataTask own, QuestDataTask other) {
        super.copyProgress(own, other);
        int[] tamed = ((QuestDataTaskTame) own).tamed;
        System.arraycopy(((QuestDataTaskTame) other).tamed, 0, tamed, 0, tamed.length);
    }
    
    @Override
    public void onAnimalTame(Player tamer, Entity entity) {
        if (tamer != null && parent.isEnabled(tamer) && parent.isAvailable(tamer) && this.isVisible(tamer) && !isCompleted(tamer)) {
            boolean updated = false;
            for (int i = 0; i < elements.size(); i++) {
                Tame tame = elements.get(i);
                if (tame.count > ((QuestDataTaskTame) getData(tamer)).tamed[i] && tame.tame != null) {
                    if (tame.tame.equals("minecraft:abstracthorse")) {
                        if (entity instanceof AbstractHorse) {
                            ((QuestDataTaskTame) getData(tamer)).tamed[i]++;
                            updated = true;
                        }
                    } else {
                        EntityType<?> type = Registry.ENTITY_TYPE.get(new ResourceLocation(tame.tame));
                        if (type != null) {
                            if (type.equals(entity.getType())) {
                                ((QuestDataTaskTame) getData(tamer)).tamed[i]++;
                                updated = true;
                            }
                        }
                    }
                }
            }
            
            if (updated) {
                boolean done = true;
                for (int i = 0; i < elements.size(); i++) {
                    Tame tame = elements.get(i);
                    
                    if (tamed(i, tamer) < tame.count) {
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
        Adapter.JsonArrayBuilder array = Adapter.array();
        for (Tame tame : elements) {
            array.add(QuestTaskAdapter.TAME_ADAPTER.toJsonTree(tame));
        }
        builder.add(TAME, array.build());
    }
    
    @Override
    public void read(JsonObject object) {
        elements.clear();
        for (JsonElement element : GsonHelper.getAsJsonArray(object, TAME, new JsonArray())) {
            Tame tame = QuestTaskAdapter.TAME_ADAPTER.fromJsonTree(element);
            if (tame != null)
                elements.add(tame);
        }
    }
    
    public static class Tame extends IconTask {
        private String tame;
        private int count = 1;
        
        public String getTame() {
            return tame;
        }
        
        public void setTame(String tame) {
            this.tame = tame;
        }
        
        public int getCount() {
            return count;
        }
        
        public void setCount(int count) {
            this.count = count;
        }
    }
}
