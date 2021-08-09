package hardcorequesting.common.quests.task;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiColor;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenuAdvancement;
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
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.UUID;

/**
 * A task where the player has to complete advancements.
 */
public class GetAdvancementTask extends IconQuestTask<GetAdvancementTask.AdvancementTask> {
    private static final String ADVANCEMENTS = "advancements";
    
    public GetAdvancementTask(Quest parent, String description, String longDescription) {
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
    protected void onModifyElement() {
        SaveHelper.add(SaveHelper.EditType.ADVANCEMENT_CHANGE);
    }
    
    @Override
    protected void onRemoveElement() {
        SaveHelper.add(SaveHelper.EditType.ADVANCEMENT_REMOVE);
    }
    
    private boolean advanced(int id, Player player) {
        return id < elements.size() && ((QuestDataTaskAdvancement) getData(player)).advanced[id];
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
    protected void handleElementEditClick(GuiQuestBook gui, Player player, EditMode mode, int id, AdvancementTask task) {
        if (mode == EditMode.LOCATION) {
            GuiEditMenuAdvancement.display(gui, player, task.getAdvancement(),
                    result -> setAdvancement(id, result, player));
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

