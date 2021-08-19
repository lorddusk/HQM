package hardcorequesting.common.quests.task.icon;

import com.google.gson.JsonArray;
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
import hardcorequesting.common.quests.data.AdvancementTaskData;
import hardcorequesting.common.util.EditType;
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

import java.util.UUID;

/**
 * A task where the player has to complete advancements.
 */
public class GetAdvancementTask extends IconLayoutTask<GetAdvancementTask.Part, AdvancementTaskData> {
    private static final String ADVANCEMENTS = "advancements";
    
    public GetAdvancementTask(Quest parent, String description, String longDescription) {
        super(EditType.Type.ADVANCEMENT, parent, description, longDescription);
        
        register(EventTrigger.Type.ADVANCEMENT, EventTrigger.Type.OPEN_BOOK);
    }
    
    @Override
    protected Part createEmpty() {
        return new Part();
    }
    
    private boolean advanced(int id, Player player) {
        return getData(player).getValue(id);
    }
    
    private void setAdvancement(int id, String advancement) {
        getOrCreateForModify(id).setAdvancement(advancement);
    }
    
    @Override
    public Class<AdvancementTaskData> getDataType() {
        return AdvancementTaskData.class;
    }
    
    @Override
    public AdvancementTaskData newQuestData() {
        return new AdvancementTaskData(elements.size());
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    protected void drawElementText(PoseStack matrices, GuiQuestBook gui, Player player, Part task, int index, int x, int y) {
        if (advanced(index, player)) {
            gui.drawString(matrices, Translator.translatable("hqm.advancementMenu.visited", GuiColor.GREEN), x, y, 0.7F, 0x404040);
        }
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    protected void handleElementEditClick(GuiQuestBook gui, Player player, EditMode mode, int id, Part task) {
        if (mode == EditMode.LOCATION) {
            GuiEditMenuAdvancement.display(gui, player, task.getAdvancement(),
                    result -> setAdvancement(id, result));
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
            AdvancementTaskData data = this.getData(player);
            
            boolean completed = true;
            ServerAdvancementManager manager = player.getServer().getAdvancements();
            PlayerAdvancements playerAdvancements = player.getServer().getPlayerList().getPlayerAdvancements((ServerPlayer) player);
            
            for (int i = 0; i < elements.size(); i++) {
                if (data.getValue(i)) continue;
                
                Part part = this.elements.get(i);
                if (part == null || part.getName() == null || part.getAdvancement() == null) continue;
                
                ResourceLocation advResource = new ResourceLocation(part.getAdvancement());
                
                Advancement advAdvancement = manager.getAdvancement(advResource);
                
                if (advAdvancement == null) {
                    completed = false;
                } else {
                    AdvancementProgress progress = playerAdvancements.getOrStartProgress(advAdvancement);
                    
                    if (progress.isDone()) {
                        data.complete(i);
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
        
        return getData(uuid).getCompletedRatio(elements.size());
    }
    
    @Override
    public void mergeProgress(UUID uuid, AdvancementTaskData own, AdvancementTaskData other) {
        own.mergeResult(other);
        
        if (own.areAllCompleted(elements.size())) {
            completeTask(uuid);
        }
    }
    
    @Override
    public void autoComplete(UUID uuid, boolean status) {
        AdvancementTaskData data = getData(uuid);
        if (status) {
            for (int i = 0; i < elements.size(); i++) {
                data.complete(i);
            }
        } else {
            data.clear();
        }
    }
    
    @Override
    public void copyProgress(AdvancementTaskData own, AdvancementTaskData other) {
        own.update(other);
    }
    
    @Override
    public boolean allowDetect() {
        return true;
    }
    
    @Override
    public void write(Adapter.JsonObjectBuilder builder) {
        builder.add(ADVANCEMENTS, writeElements(QuestTaskAdapter.ADVANCEMENT_TASK_ADAPTER));
    }
    
    @SuppressWarnings("ConstantConditions")
    @Override
    public void read(JsonObject object) {
        readElements(GsonHelper.getAsJsonArray(object, ADVANCEMENTS, new JsonArray()), QuestTaskAdapter.ADVANCEMENT_TASK_ADAPTER);
    }
    
    public enum Visibility {
        FULL("Full"),
        NONE("None");
        
        private final String id;
        
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
    
    public static class Part extends IconLayoutTask.Part {
        
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

