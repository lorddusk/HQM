package hardcorequesting.common.quests.task;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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

import java.util.UUID;

/**
 * A task where the player needs to tame certain mobs.
 */
public class TameMobsTask extends IconLayoutTask<TameMobsTask.Tame> {
    private static final String TAME = "tame";
    
    public static final ResourceLocation ABSTRACT_HORSE = new ResourceLocation("abstracthorse");
    
    public TameMobsTask(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
        register(EventTrigger.Type.ANIMAL_TAME);
    }
    
    @Override
    protected Tame createEmpty() {
        return new Tame();
    }
    
    @Override
    protected void onAddElement() {
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
    private void setInfo(int id, String entityId, int amount) {
        
        Tame tame = getOrCreateForModify(id);
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
        return ((QuestDataTaskTame) getData(player)).getValue(id);
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
            PickMobMenu.display(gui, player, tame.getTame() == null ? null : ResourceLocation.tryParse(tame.getTame()), tame.getCount(), "tameTask",
                    PickMobMenu.EXTRA_TAME_ENTRIES, result -> setInfo(id, result.getMobId().toString(), result.getAmount()));
        }
    }
    
    @Override
    public void onUpdate(Player player) {
        
    }
    
    @Override
    public float getCompletedRatio(UUID uuid) {
        QuestDataTaskTame data = (QuestDataTaskTame) getData(uuid);
        int tamed = 0;
        int total = 0;
        
        for (int i = 0; i < elements.size(); i++) {
            int req = elements.get(i).count;
            tamed += Math.min(req, data.getValue(i));
            total += req;
        }
        
        return (float) tamed / total;
    }
    
    @Override
    public void mergeProgress(UUID uuid, QuestDataTask own, QuestDataTask other) {
        QuestDataTaskTame data = (QuestDataTaskTame) own;
        data.merge((QuestDataTaskTame) other);
    
        boolean all = true;
        for (int i = 0; i < elements.size(); i++) {
            if (data.getValue(i) < elements.get(i).count) {
                all = false;
                break;
            }
        }
        
        if (all) {
            completeTask(uuid);
        }
    }
    
    @Override
    public void autoComplete(UUID uuid, boolean status) {
        QuestDataTaskTame data = (QuestDataTaskTame) getData(uuid);
        if (status) {
            for (int i = 0; i < elements.size(); i++) {
                data.setValue(i, elements.get(i).count);
            }
        } else {
            for (int i = 0; i < elements.size(); i++) {
                data.setValue(i, 0);
            }
        }
    }
    
    @Override
    public void copyProgress(QuestDataTask own, QuestDataTask other) {
        own.update(other);
    }
    
    @Override
    public void onAnimalTame(Player tamer, Entity entity) {
        if (tamer != null && parent.isEnabled(tamer) && parent.isAvailable(tamer) && this.isVisible(tamer) && !isCompleted(tamer)) {
            QuestDataTaskTame data = (QuestDataTaskTame) getData(tamer);
            boolean updated = false;
            for (int i = 0; i < elements.size(); i++) {
                Tame tame = elements.get(i);
                if (tame.count > data.getValue(i) && tame.tame != null) {
                    if (tame.tame.equals(ABSTRACT_HORSE.toString())) {
                        if (entity instanceof AbstractHorse) {
                            data.setValue(i, data.getValue(i) + 1);
                            updated = true;
                        }
                    } else {
                        EntityType<?> type = Registry.ENTITY_TYPE.get(new ResourceLocation(tame.tame));
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
