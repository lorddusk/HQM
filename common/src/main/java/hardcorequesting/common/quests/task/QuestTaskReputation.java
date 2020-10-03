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
import hardcorequesting.common.quests.data.QuestDataTask;
import hardcorequesting.common.reputation.Reputation;
import hardcorequesting.common.reputation.ReputationMarker;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public abstract class QuestTaskReputation extends QuestTask {
    //for this task to be completed, all reputation settings (up to 4) has to be completed at the same time, therefore it's not saved whether you've completed one of these reputation settings, just if you've completed it all
    private static final String REPUTATION = "reputation";
    private static final int OFFSET_Y = 27;
    private final int startOffsetY;
    public ReputationSetting[] settings = new ReputationSetting[0];
    
    public QuestTaskReputation(Quest parent, String description, String longDescription, int startOffsetY) {
        super(parent, description, longDescription);
        this.startOffsetY = startOffsetY;
    }
    
    public ReputationSetting[] getSettings() {
        return settings;
    }
    
    public void setSetting(int id, ReputationSetting setting) {
        if (id >= settings.length) {
            settings = Arrays.copyOf(settings, settings.length + 1);
            SaveHelper.add(SaveHelper.EditType.REPUTATION_TASK_CREATE);
        } else {
            SaveHelper.add(SaveHelper.EditType.REPUTATION_TASK_CHANGE);
        }
        
        settings[id] = setting;
    }
    
    protected boolean isPlayerInRange(Player player) {
        if (settings.length > 0) {
            
            QuestDataTask data = getData(player);
            if (!data.completed && !player.getCommandSenderWorld().isClientSide) {
                for (ReputationSetting setting : settings) {
                    if (!setting.isValid(player.getUUID())) {
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
        int size = Quest.canQuestsBeEdited() ? settings.length + 1 : settings.length;
        for (int i = 0; i < size; i++) {
            gui.applyColor(0xFFFFFFFF);
            ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);
            
            if (i >= settings.length) {
                gui.drawRect(START_X + Reputation.BAR_X, START_Y + startOffsetY + i * OFFSET_Y + Reputation.BAR_Y, Reputation.BAR_SRC_X, Reputation.BAR_SRC_Y, Reputation.BAR_WIDTH, Reputation.BAR_HEIGHT);
            } else {
                ReputationSetting setting = settings[i];
                info = setting.reputation.draw(matrices, gui, START_X, START_Y + startOffsetY + i * OFFSET_Y, mX, mY, info, getPlayerForRender(player), true, setting.lower, setting.upper, setting.inverted, null, null, getData(player).completed);
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
            int size = settings.length + 1;
            for (int i = 0; i < size; i++) {
                if (gui.inBounds(START_X, START_Y + startOffsetY + i * OFFSET_Y, Reputation.BAR_WIDTH, 20, mX, mY)) {
                    if (gui.getCurrentMode() == EditMode.REPUTATION_TASK) {
                        gui.setEditMenu(new GuiEditMenuReputationSetting(gui, player, this, i, i >= settings.length ? null : settings[i]));
                    } else if (gui.getCurrentMode() == EditMode.DELETE && i < settings.length) {
                        removeSetting(i);
                        SaveHelper.add(SaveHelper.EditType.REPUTATION_TASK_REMOVE);
                    }
                    break;
                }
            }
        }
    }
    
    @Override
    public float getCompletedRatio(UUID playerID) {
        int count = settings.length;
        if (count == 0) {
            return 0;
        }
        
        int valid = 0;
        for (ReputationSetting setting : settings) {
            if (setting.isValid(playerID)) {
                valid++;
            }
        }
        
        return (float) valid / count;
    }
    
    @Override
    public void mergeProgress(UUID playerID, QuestDataTask own, QuestDataTask other) {
        if (other.completed) {
            own.completed = true;
        }
    }
    
    @Override
    public void autoComplete(UUID playerID, boolean status) {
        getData(playerID).completed = status;
    }
    
    protected Player getPlayerForRender(Player player) {
        return player;
    }
    
    public void removeSetting(int i) {
        int id = 0;
        ReputationSetting[] settings = new ReputationSetting[this.settings.length - 1];
        for (int j = 0; j < this.settings.length; j++) {
            if (j != i) {
                settings[id] = this.settings[j];
                id++;
            }
        }
        this.settings = settings;
    }
    
    @Override
    public void write(Adapter.JsonObjectBuilder builder) {
        Adapter.JsonArrayBuilder array = Adapter.array();
        for (ReputationSetting setting : settings) {
            array.add(QuestTaskAdapter.REPUTATION_TASK_ADAPTER.toJsonTree(setting));
        }
        builder.add(REPUTATION, array.build());
    }
    
    @Override
    public void read(JsonObject object) {
        List<QuestTaskAdapter.ReputationSettingConstructor> list = new ArrayList<>();
        for (JsonElement element : GsonHelper.getAsJsonArray(object, REPUTATION, new JsonArray())) {
            list.add(QuestTaskAdapter.ReputationSettingConstructor.read(element));
        }
        QuestTaskAdapter.taskReputationListMap.put(this, list);
    }
    
    public static class ReputationSetting {
        
        private Reputation reputation;
        private ReputationMarker lower;
        private ReputationMarker upper;
        private boolean inverted;
        
        public ReputationSetting(Reputation reputation, ReputationMarker lower, ReputationMarker upper, boolean inverted) {
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
        
        public boolean isValid(UUID playerID) {
            if (getReputation() == null || !getReputation().isValid()) {
                return false;
            }
            ReputationMarker current = getReputation().getCurrentMarker(getReputation().getValue(playerID));
            
            return ((lower == null || lower.getValue() <= current.getValue()) && (upper == null || current.getValue() <= upper.getValue())) != inverted;
        }
    }
}
