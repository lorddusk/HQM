package hardcorequesting.common.quests.task;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.ResourceHelper;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenuReputationSetting;
import hardcorequesting.common.io.adapter.Adapter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.quests.data.TaskData;
import hardcorequesting.common.reputation.Reputation;
import hardcorequesting.common.reputation.ReputationMarker;
import hardcorequesting.common.team.Team;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.Positioned;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class ReputationTask<Data extends TaskData> extends ListTask<ReputationTask.Part, Data> {
    //for this task to be completed, all reputation settings (up to 4) has to be completed at the same time, therefore it's not saved whether you've completed one of these reputation settings, just if you've completed it all
    private static final String REPUTATION = "reputation";
    private static final int OFFSET_Y = 27;
    private final int startOffsetY;
    
    public ReputationTask(Quest parent, String description, String longDescription, int startOffsetY) {
        super(EditType.Type.REPUTATION_TASK, parent, description, longDescription);
        this.startOffsetY = startOffsetY;
    }
    
    public List<Part> getSettings() {
        return elements;
    }
    
    public void setSetting(int id, Part setting) {
        setElement(id, setting);
    }
    
    @Override
    protected Part createEmpty() {
        return new Part(null, null, null, false);
    }
    
    protected boolean isPlayerInRange(Player player) {
        if (!elements.isEmpty()) {
            
            TaskData data = getData(player);
            if (!data.completed && !player.getCommandSenderWorld().isClientSide) {
                for (Part setting : elements) {
                    if (!setting.isValid(QuestingDataManager.getInstance().getQuestingData(player).getTeam())) {
                        return false;
                    }
                }
                
                return true;
            }
        }
        return false;
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    public void draw(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY) {
        String info = null;
        List<Positioned<Part>> renderSettings = positionParts(getShownElements());
        for (int i = 0; i < renderSettings.size(); i++) {
            Positioned<Part> pos = renderSettings.get(i);
            Part part = pos.getElement();
            
            gui.applyColor(0xFFFFFFFF);
            ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
            
            if (part.reputation == null) {
                gui.drawRect(pos.getX() + Reputation.BAR_X, pos.getY() + Reputation.BAR_Y, Reputation.BAR_SRC_X, Reputation.BAR_SRC_Y, Reputation.BAR_WIDTH, Reputation.BAR_HEIGHT);
            } else {
                info = part.reputation.draw(matrices, gui, pos.getX(), pos.getY(), mX, mY, info, getPlayerForRender(player), true, part.lower, part.upper, part.inverted, null, null, getData(player).completed);
            }
        }
        
        if (info != null) {
            gui.renderTooltip(matrices, Translator.plain(info), mX + gui.getLeft(), mY + gui.getTop());
        }
    }
    
    @Override
    @Environment(EnvType.CLIENT)
    public void onClick(GuiQuestBook gui, Player player, int mX, int mY, int b) {
        if (Quest.canQuestsBeEdited() && gui.getCurrentMode() != EditMode.NORMAL) {
            List<Positioned<Part>> renderSettings = positionParts(getShownElements());
            for (int i = 0; i < renderSettings.size(); i++) {
                Positioned<Part> pos = renderSettings.get(i);
                Part part = pos.getElement();
                
                if (gui.inBounds(pos.getX(), pos.getY(), Reputation.BAR_WIDTH, 20, mX, mY)) {
                    if (gui.getCurrentMode() == EditMode.REPUTATION_TASK) {
                        gui.setEditMenu(new GuiEditMenuReputationSetting(gui, player, this, i, part));
                    } else if (gui.getCurrentMode() == EditMode.DELETE && i < elements.size()) {
                        removeSetting(i);
                        SaveHelper.add(EditType.REPUTATION_TASK_REMOVE);
                    }
                    break;
                }
            }
        }
    }
    
    protected List<Positioned<Part>> positionParts(List<Part> parts) {
        List<Positioned<Part>> list = new ArrayList<>(parts.size());
        int x = START_X;
        int y = START_Y + startOffsetY;
        for (Part part : parts) {
            list.add(new Positioned<>(x, y, part));
            y += OFFSET_Y;
        }
        return list;
    }
    
    @Override
    public void mergeProgress(UUID playerID, Data own, Data other) {
        own.completed |= other.completed;
    }
    
    @Override
    public void setComplete(Data data) {
        data.completed = true;
    }
    
    protected Player getPlayerForRender(Player player) {
        return player;
    }
    
    public void removeSetting(int i) {
        elements.remove(i);
    }
    
    @Override
    public void write(Adapter.JsonObjectBuilder builder) {
        builder.add(REPUTATION, writeElements(QuestTaskAdapter.REPUTATION_TASK_ADAPTER));
    }
    
    @Override
    public void read(JsonObject object) {
        List<QuestTaskAdapter.ReputationSettingConstructor> list = new ArrayList<>();
        for (JsonElement element : GsonHelper.getAsJsonArray(object, REPUTATION, new JsonArray())) {
            QuestTaskAdapter.ReputationSettingConstructor constructor = QuestTaskAdapter.ReputationSettingConstructor.read(element);
            if (constructor != null)
                list.add(constructor);
        }
        QuestTaskAdapter.taskReputationListMap.put(this, list);
    }
    
    public static class Part {
        
        private Reputation reputation;
        private ReputationMarker lower;
        private ReputationMarker upper;
        private boolean inverted;
        
        public Part(Reputation reputation, ReputationMarker lower, ReputationMarker upper, boolean inverted) {
            this.reputation = reputation;
            this.lower = lower;
            this.upper = upper;
            this.inverted = inverted;
        }
        
        public Reputation getReputation() {
            return reputation;
        }
        
        public ReputationMarker getLower() {
            return lower;
        }
        
        public void setLower(ReputationMarker lower) {
            this.lower = lower;
        }
        
        public ReputationMarker getUpper() {
            return upper;
        }
        
        public void setUpper(ReputationMarker upper) {
            this.upper = upper;
        }
        
        public boolean isInverted() {
            return inverted;
        }
        
        public boolean isValid(Team team) {
            if (getReputation() == null || !getReputation().isValid()) {
                return false;
            }
            ReputationMarker current = getReputation().getCurrentMarker(team.getReputation(this.getReputation()));
            
            return ((lower == null || lower.getValue() <= current.getValue()) && (upper == null || current.getValue() <= upper.getValue())) != inverted;
        }
    }
}
