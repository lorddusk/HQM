package hardcorequesting.common.quests.task;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiColor;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenuTame;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenuTextEditor;
import hardcorequesting.common.client.interfaces.edit.PickItemMenu;
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
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class QuestTaskTame extends IconQuestTask<QuestTaskTame.Tame> {
    private static final String TAME = "tame";
    private static final int Y_OFFSET = 30;
    private static final int X_TEXT_OFFSET = 23;
    private static final int X_TEXT_INDENT = 0;
    private static final int Y_TEXT_OFFSET = 0;
    private static final int ITEM_SIZE = 18;
    
    public QuestTaskTame(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
        register(EventTrigger.Type.ANIMAL_TAME);
    }
    
    @Override
    protected Tame createEmpty() {
        return new Tame();
    }
    
    public void setTame(int id, Tame tame, Player player) {
        if (id >= elements.size()) {
            elements.add(tame);
            QuestDataTaskTame data = (QuestDataTaskTame) getData(player);
            data.tamed = Arrays.copyOf(data.tamed, data.tamed.length + 1);
            SaveHelper.add(SaveHelper.EditType.MONSTER_CREATE);
        } else {
            elements.set(id, tame);
            SaveHelper.add(SaveHelper.EditType.MONSTER_CHANGE);
        }
    }
    
    public void setIcon(int id, ItemStack stack, Player player) {
        if (stack.isEmpty()) return;
        
        setTame(id, id >= elements.size() ? createEmpty() : elements.get(id), player);
    
        elements.get(id).setIconStack(stack);
    }
    
    public void setName(int id, String str, Player player) {
        setTame(id, id >= elements.size() ? createEmpty() : elements.get(id), player);
    
        elements.get(id).setName(str);
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
    public void draw(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY) {
        List<Tame> tames = getShownElements();
        for (int i = 0; i < tames.size(); i++) {
            Tame tame = tames.get(i);
            
            int x = START_X;
            int y = START_Y + i * Y_OFFSET;
            gui.drawItemStack(tame.getIconStack(), x, y, mX, mY, false);
            gui.drawString(matrices, Translator.plain(tame.getName()), x + X_TEXT_OFFSET, y + Y_TEXT_OFFSET, 0x404040);
            
            int tamed = tamed(i, player);
            if (tamed == tame.count) {
                gui.drawString(matrices, Translator.translatable("hqm.tameTask.allTamed", GuiColor.GREEN), x + X_TEXT_OFFSET + X_TEXT_INDENT, y + Y_TEXT_OFFSET + 9, 0.7F, 0x404040);
            } else {
                gui.drawString(matrices, Translator.translatable("hqm.tameTask.partTames", tamed, (100 * tamed / tame.count)), x + X_TEXT_OFFSET + X_TEXT_INDENT, y + Y_TEXT_OFFSET + 9, 0.7F, 0x404040);
            }
            gui.drawString(matrices, Translator.translatable("hqm.tameTask.totalTames", tame.count), x + X_TEXT_OFFSET + X_TEXT_INDENT, y + Y_TEXT_OFFSET + 15, 0.7F, 0x404040);
        }
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void onClick(GuiQuestBook gui, Player player, int mX, int mY, int b) {
        if (Quest.canQuestsBeEdited() && gui.getCurrentMode() != EditMode.NORMAL) {
            List<Tame> tames = getShownElements();
            for (int i = 0; i < tames.size(); i++) {
                Tame tame = tames.get(i);
                
                int x = START_X;
                int y = START_Y + i * Y_OFFSET;
                
                if (gui.inBounds(x, y, ITEM_SIZE, ITEM_SIZE, mX, mY)) {
                    switch (gui.getCurrentMode()) {
                        case MOB:
                            gui.setEditMenu(new GuiEditMenuTame(gui, this, tame.copy(), i, player));
                            break;
                        case ITEM:
                            final int tameId = i;
                            PickItemMenu.display(gui, player, tame.getIconStack(), PickItemMenu.Type.ITEM,
                                    result -> this.setIcon(tameId, result.get(), player));
                            break;
                        case RENAME:
                            gui.setEditMenu(new GuiEditMenuTextEditor(gui, player, this, i, tame));
                            break;
                        case DELETE:
                            if (i < this.elements.size()) {
                                elements.remove(i);
                                SaveHelper.add(SaveHelper.EditType.MONSTER_REMOVE);
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
        
        public Tame copy() {
            Tame other = new Tame();
            other.copyFrom(this);
            other.tame = tame;
            other.count = count;
            
            return other;
        }
        
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
