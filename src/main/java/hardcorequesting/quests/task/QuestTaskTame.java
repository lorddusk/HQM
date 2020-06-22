package hardcorequesting.quests.task;

import hardcorequesting.client.EditMode;
import hardcorequesting.client.interfaces.GuiColor;
import hardcorequesting.client.interfaces.GuiQuestBook;
import hardcorequesting.client.interfaces.edit.GuiEditMenuItem;
import hardcorequesting.client.interfaces.edit.GuiEditMenuTame;
import hardcorequesting.client.interfaces.edit.GuiEditMenuTextEditor;
import hardcorequesting.event.EventTrigger;
import hardcorequesting.quests.ItemPrecision;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.data.QuestDataTask;
import hardcorequesting.quests.data.QuestDataTaskTame;
import hardcorequesting.util.SaveHelper;
import hardcorequesting.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.StringRenderable;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.UUID;

public class QuestTaskTame extends QuestTask {
    
    
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
    
    public void setTame(int id, Tame tame, PlayerEntity player) {
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
    
    public void setIcon(int id, ItemStack stack, PlayerEntity player) {
        System.out.println(stack);
        setTame(id, id >= tames.length ? new Tame() : tames[id], player);
        
        tames[id].iconStack = stack;
    }
    
    public void setName(int id, String str, PlayerEntity player) {
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
    
    private int tamed(int id, PlayerEntity player) {
        return id < tames.length ? ((QuestDataTaskTame) getData(player)).tamed[id] : 0;
    }
    
    @Override
    public Class<? extends QuestDataTask> getDataType() {
        return QuestDataTaskTame.class;
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void draw(MatrixStack matrices, GuiQuestBook gui, PlayerEntity player, int mX, int mY) {
        Tame[] tames = getEditFriendlyTames(this.tames);
        for (int i = 0; i < tames.length; i++) {
            Tame tame = tames[i];
            
            int x = START_X;
            int y = START_Y + i * Y_OFFSET;
            gui.drawItemStack(tame.iconStack, x, y, mX, mY, false);
            gui.drawString(matrices, Translator.plain(tame.name), x + X_TEXT_OFFSET, y + Y_TEXT_OFFSET, 0x404040);
            
            int tamed = tamed(i, player);
            if (tamed == tame.count) {
                gui.drawString(matrices, Translator.translated("hqm.tameTask.allTamed", GuiColor.GREEN), x + X_TEXT_OFFSET + X_TEXT_INDENT, y + Y_TEXT_OFFSET + 9, 0.7F, 0x404040);
            } else {
                gui.drawString(matrices, Translator.translated("hqm.tameTask.partTames", tamed, (100 * tamed / tame.count)), x + X_TEXT_OFFSET + X_TEXT_INDENT, y + Y_TEXT_OFFSET + 9, 0.7F, 0x404040);
            }
            gui.drawString(matrices, Translator.translated("hqm.tameTask.totalTames", tame.count), x + X_TEXT_OFFSET + X_TEXT_INDENT, y + Y_TEXT_OFFSET + 15, 0.7F, 0x404040);
        }
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void onClick(GuiQuestBook gui, PlayerEntity player, int mX, int mY, int b) {
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
    public void onUpdate(PlayerEntity player) {
        
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
    public void onAnimalTame(PlayerEntity tamer, Entity entity) {
        if (tamer != null && parent.isEnabled(tamer) && parent.isAvailable(tamer) && this.isVisible(tamer) && !isCompleted(tamer)) {
            boolean updated = false;
            for (int i = 0; i < tames.length; i++) {
                Tame tame = tames[i];
                if (tame.count > ((QuestDataTaskTame) getData(tamer)).tamed[i] && tame.tame != null) {
                    if (tame.tame.equals("minecraft:abstracthorse")) {
                        if (entity instanceof HorseBaseEntity) {
                            ((QuestDataTaskTame) getData(tamer)).tamed[i]++;
                            updated = true;
                        }
                    } else {
                        EntityType<?> type = Registry.ENTITY_TYPE.get(new Identifier(tame.tame));
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
                    completeTask(tamer.getUuid());
                }
                
                parent.sendUpdatedDataToTeam(tamer);
            }
        }
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
