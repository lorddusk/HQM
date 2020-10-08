package hardcorequesting.common.quests.task;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiColor;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenuItem;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenuTame;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenuTextEditor;
import hardcorequesting.common.event.EventTrigger;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import hardcorequesting.common.quests.ItemPrecision;
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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class QuestTaskTame extends QuestTask {
    private static final String TAME = "tame";
    private static final int Y_OFFSET = 30;
    private static final int X_TEXT_OFFSET = 23;
    private static final int X_TEXT_INDENT = 0;
    private static final int Y_TEXT_OFFSET = 0;
    private static final int ITEM_SIZE = 18;
    public Tame[] tames = new Tame[0];
    
    public QuestTaskTame(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
        register(EventTrigger.Type.ANIMAL_TAME);
    }
    
    public void setTame(int id, Tame tame, Player player) {
        if (id >= tames.length) {
            tames = Arrays.copyOf(tames, tames.length + 1);
            QuestDataTaskTame data = (QuestDataTaskTame) getData(player);
            data.tamed = Arrays.copyOf(data.tamed, data.tamed.length + 1);
            SaveHelper.add(SaveHelper.EditType.MONSTER_CREATE);
        } else {
            SaveHelper.add(SaveHelper.EditType.MONSTER_CHANGE);
        }
        
        tames[id] = tame;
    }
    
    public void setIcon(int id, ItemStack stack, Player player) {
        System.out.println(stack);
        setTame(id, id >= tames.length ? new Tame() : tames[id], player);
        
        tames[id].iconStack = stack;
    }
    
    public void setName(int id, String str, Player player) {
        setTame(id, id >= tames.length ? new Tame() : tames[id], player);
        
        tames[id].name = str;
    }
    
    @Environment(EnvType.CLIENT)
    private Tame[] getEditFriendlyTames(Tame[] tames) {
        if (Quest.canQuestsBeEdited()) {
            tames = Arrays.copyOf(tames, tames.length + 1);
            tames[tames.length - 1] = new Tame();
            return tames;
        } else {
            return tames;
        }
    }
    
    private int tamed(int id, Player player) {
        return id < tames.length ? ((QuestDataTaskTame) getData(player)).tamed[id] : 0;
    }
    
    @Override
    public Class<? extends QuestDataTask> getDataType() {
        return QuestDataTaskTame.class;
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void draw(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY) {
        Tame[] tames = getEditFriendlyTames(this.tames);
        for (int i = 0; i < tames.length; i++) {
            Tame tame = tames[i];
            
            int x = START_X;
            int y = START_Y + i * Y_OFFSET;
            gui.drawItemStack(tame.iconStack, x, y, mX, mY, false);
            gui.drawString(matrices, Translator.plain(tame.name), x + X_TEXT_OFFSET, y + Y_TEXT_OFFSET, 0x404040);
            
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
            Tame[] tames = getEditFriendlyTames(this.tames);
            for (int i = 0; i < tames.length; i++) {
                Tame tame = tames[i];
                
                int x = START_X;
                int y = START_Y + i * Y_OFFSET;
                
                if (gui.inBounds(x, y, ITEM_SIZE, ITEM_SIZE, mX, mY)) {
                    switch (gui.getCurrentMode()) {
                        case MOB:
                            gui.setEditMenu(new GuiEditMenuTame(gui, this, tame.copy(), i, player));
                            break;
                        case ITEM:
                            gui.setEditMenu(new GuiEditMenuItem(gui, player, tame.iconStack, i, GuiEditMenuItem.Type.TAME, 1, ItemPrecision.PRECISE));
                            break;
                        case RENAME:
                            gui.setEditMenu(new GuiEditMenuTextEditor(gui, player, this, i, tame));
                            break;
                        case DELETE:
                            if (i < this.tames.length) {
                                Tame[] newTames = new Tame[this.tames.length - 1];
                                int id = 0;
                                for (int j = 0; j < this.tames.length; j++) {
                                    if (j != i) {
                                        newTames[id] = this.tames[j];
                                        id++;
                                    }
                                }
                                this.tames = newTames;
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
        
        for (int i = 0; i < tames.length; i++) {
            tamed += ((QuestDataTaskTame) getData(uuid)).tamed[i];
            total += tames[i].count;
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
            if (tamed[i] < tames[i].count) {
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
                tamed[i] = tames[i].count;
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
            for (int i = 0; i < tames.length; i++) {
                Tame tame = tames[i];
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
                for (int i = 0; i < tames.length; i++) {
                    Tame tame = tames[i];
                    
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
        for (Tame tame : tames) {
            array.add(QuestTaskAdapter.TAME_ADAPTER.toJsonTree(tame));
        }
        builder.add(TAME, array.build());
    }
    
    @Override
    public void read(JsonObject object) {
        List<Tame> list = new ArrayList<>();
        for (JsonElement element : GsonHelper.getAsJsonArray(object, TAME, new JsonArray())) {
            list.add(QuestTaskAdapter.TAME_ADAPTER.fromJsonTree(element));
        }
        tames = list.toArray(new Tame[0]);
    }
    
    public static class Tame {
        private ItemStack iconStack = ItemStack.EMPTY;
        private String name = "New";
        private String tame;
        private int count = 1;
        
        public Tame copy() {
            Tame other = new Tame();
            other.iconStack = iconStack.isEmpty() ? ItemStack.EMPTY : iconStack.copy();
            other.name = name;
            other.tame = tame;
            other.count = count;
            
            return other;
        }
        
        public ItemStack getIconStack() {
            return iconStack;
        }
        
        public void setIconStack(@NotNull ItemStack iconStack) {
            this.iconStack = iconStack;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
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
