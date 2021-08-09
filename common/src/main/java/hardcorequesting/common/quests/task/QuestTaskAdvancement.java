package hardcorequesting.common.quests.task;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiColor;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenuAdvancement;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenuTextEditor;
import hardcorequesting.common.client.interfaces.edit.PickItemMenu;
import hardcorequesting.common.event.EventTrigger;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.data.QuestDataTask;
import hardcorequesting.common.quests.data.QuestDataTaskAdvancement;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class QuestTaskAdvancement extends IconQuestTask<QuestTaskAdvancement.AdvancementTask> {
    private static final String ADVANCEMENTS = "advancements";
    private static final int Y_OFFSET = 30;
    private static final int ITEM_SIZE = 18;
    
    public QuestTaskAdvancement(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
        
        register(EventTrigger.Type.ADVANCEMENT, EventTrigger.Type.OPEN_BOOK);
    }
    
    @Override
    protected AdvancementTask createEmpty() {
        return new AdvancementTask();
    }
    
    @Override
    protected void onAddElement(Player player) {
        QuestDataTaskAdvancement data = (QuestDataTaskAdvancement) getData(player);
        data.advanced = Arrays.copyOf(data.advanced, data.advanced.length + 1);
        SaveHelper.add(SaveHelper.EditType.ADVANCEMENT_CREATE);
    }
    
    @Override
    protected void onModifyElement(Player player) {
        SaveHelper.add(SaveHelper.EditType.ADVANCEMENT_CHANGE);
    }
    
    private boolean advanced(int id, Player player) {
        return id < elements.size() && ((QuestDataTaskAdvancement) getData(player)).advanced[id];
    }
    
    private void setIcon(int id, ItemStack stack, Player player) {
        getOrCreateForModify(id, player).setIconStack(stack);
    }
    
    public void setName(int id, String str, Player player) {
        getOrCreateForModify(id, player).setName(str);
    }
    
    private void setAdvancement(int id, String advancement, Player player) {
        getOrCreateForModify(id, player).setAdvancement(advancement);
    }
    
    @Override
    public Class<? extends QuestDataTask> getDataType() {
        return QuestDataTaskAdvancement.class;
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    protected void drawElementText(PoseStack matrices, GuiQuestBook gui, Player player, AdvancementTask task, int index, int x, int y) {
        if (advanced(index, player)) {
            gui.drawString(matrices, Translator.translatable("hqm.advancementMenu.visited", GuiColor.GREEN), x, y, 0.7F, 0x404040);
        }
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void onClick(GuiQuestBook gui, Player player, int mX, int mY, int b) {
        if (Quest.canQuestsBeEdited() && gui.getCurrentMode() != EditMode.NORMAL) {
            List<AdvancementTask> advancements = getShownElements();
            for (int i = 0; i < advancements.size(); i++) {
                AdvancementTask advancement = advancements.get(i);
                
                int x = START_X;
                int y = START_Y + i * Y_OFFSET;
                
                if (gui.inBounds(x, y, ITEM_SIZE, ITEM_SIZE, mX, mY)) {
                    final int advancementId = i;
                    switch (gui.getCurrentMode()) {
                        case LOCATION:
                            GuiEditMenuAdvancement.display(gui, player, advancement.getAdvancement(),
                                    result -> setAdvancement(advancementId, result, player));
                            break;
                        case ITEM:
                            PickItemMenu.display(gui, player, advancement.getIconStack(), PickItemMenu.Type.ITEM,
                                    result -> this.setIcon(advancementId, result.get(), player));
                            break;
                        case RENAME:
                            gui.setEditMenu(new GuiEditMenuTextEditor(gui, player, this, i, advancement));
                            break;
                        case DELETE:
                            if (i < this.elements.size()) {
                                elements.remove(i);
                                SaveHelper.add(SaveHelper.EditType.ADVANCEMENT_REMOVE);
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
    public void onAdvancement(ServerPlayer playerEntity) {
        checkAdvancement(playerEntity);
    }
    
    @Override
    public void onUpdate(Player player) {
        checkAdvancement(player);
    }
    
    private void checkAdvancement(Player player) {
        Level world = player.getCommandSenderWorld();
        if (!world.isClientSide && !this.isCompleted(player) && player.getServer() != null) {
            boolean[] advanced = ((QuestDataTaskAdvancement) this.getData(player)).advanced;
            
            if (advanced.length < elements.size()) {
                boolean[] oldVisited = ArrayUtils.addAll(advanced, (boolean[]) null);
                advanced = new boolean[elements.size()];
                System.arraycopy(oldVisited, 0, advanced, 0, oldVisited.length);
                ((QuestDataTaskAdvancement) this.getData(player)).advanced = advanced;
            }
            
            boolean completed = true;
            ServerAdvancementManager manager = player.getServer().getAdvancements();
            PlayerAdvancements playerAdvancements = player.getServer().getPlayerList().getPlayerAdvancements((ServerPlayer) player);
            
            for (int i = 0; i < elements.size(); i++) {
                if (advanced[i]) continue;
                
                AdvancementTask advancement = this.elements.get(i);
                if (advancement == null || advancement.getName() == null || advancement.getAdvancement() == null) continue;
                
                ResourceLocation advResource = new ResourceLocation(advancement.getAdvancement());
                
                Advancement advAdvancement = manager.getAdvancement(advResource);
                
                if (advAdvancement == null) {
                    completed = false;
                } else {
                    AdvancementProgress progress = playerAdvancements.getOrStartProgress(advAdvancement);
                    
                    if (progress.isDone()) {
                        advanced[i] = true;
                    } else {
                        completed = false;
                    }
                }
            }
            
            if (completed && elements.size() > 0) {
                completeTask(player.getUUID());
                parent.sendUpdatedDataToTeam(player);
            }
        }
    }
    
    @Override
    public float getCompletedRatio(UUID uuid) {
        int advanced = 0;
        for (boolean b : ((QuestDataTaskAdvancement) getData(uuid)).advanced) {
            if (b) {
                advanced++;
            }
        }
        
        return (float) advanced / elements.size();
    }
    
    @Override
    public void mergeProgress(UUID uuid, QuestDataTask own, QuestDataTask other) {
        boolean[] advanced = ((QuestDataTaskAdvancement) own).advanced;
        boolean[] otherVisited = ((QuestDataTaskAdvancement) other).advanced;
        
        boolean all = true;
        for (int i = 0; i < advanced.length; i++) {
            if (otherVisited[i]) {
                advanced[i] = true;
            } else if (!advanced[i]) {
                all = false;
            }
        }
        
        if (all) {
            completeTask(uuid);
        }
    }
    
    @Override
    public void autoComplete(UUID uuid, boolean status) {
        boolean[] advanced = ((QuestDataTaskAdvancement) getData(uuid)).advanced;
        for (int i = 0; i < advanced.length; i++) {
            advanced[i] = status;
        }
    }
    
    @Override
    public void copyProgress(QuestDataTask own, QuestDataTask other) {
        super.copyProgress(own, other);
        boolean[] advanced = ((QuestDataTaskAdvancement) own).advanced;
        System.arraycopy(((QuestDataTaskAdvancement) other).advanced, 0, advanced, 0, advanced.length);
    }
    
    @Override
    public boolean allowDetect() {
        return true;
    }
    
    @Override
    public void write(Adapter.JsonObjectBuilder builder) {
        Adapter.JsonArrayBuilder array = Adapter.array();
        for (AdvancementTask advancement : elements) {
            array.add(QuestTaskAdapter.ADVANCEMENT_TASK_ADAPTER.toJsonTree(advancement));
        }
        builder.add(ADVANCEMENTS, array.build());
    }
    
    @Override
    public void read(JsonObject object) {
        elements.clear();
        for (JsonElement element : GsonHelper.getAsJsonArray(object, ADVANCEMENTS, new JsonArray())) {
            AdvancementTask advancementTask = QuestTaskAdapter.ADVANCEMENT_TASK_ADAPTER.fromJsonTree(element);
            if (advancementTask != null)
                elements.add(advancementTask);
        }
    }
    
    public enum Visibility {
        FULL("Full"),
        NONE("None");
        
        private String id;
        
        Visibility(String id) {
            this.id = id;
        }
        
        // TODO: fix these
        public String getName() {
            return Translator.get("hqm.locationMenu.vis" + id + ".title");
        }
        
        public String getDescription() {
            return Translator.get("hqm.locationMenu.vis" + id + ".desc");
        }
    }
    
    public static class AdvancementTask extends IconTask {
        
        private Visibility visible = Visibility.FULL;
        private String adv_name;
        
        public Visibility getVisible() {
            return visible;
        }
        
        public void setVisible(Visibility visible) {
            this.visible = visible;
        }
        
        public void setAdvancement(String name) {
            this.adv_name = name;
        }
        
        public String getAdvancement() {
            return this.adv_name;
        }
        
        public void setAdvancement(ResourceLocation name) {
            setAdvancement(name.toString());
        }
    }
}

