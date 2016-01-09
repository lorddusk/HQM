package hardcorequesting.quests;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import hardcorequesting.FileVersion;
import hardcorequesting.QuestingData;
import hardcorequesting.SaveHelper;
import hardcorequesting.client.EditMode;
import hardcorequesting.client.interfaces.GuiEditMenuReputationSetting;
import hardcorequesting.client.interfaces.GuiQuestBook;
import hardcorequesting.client.interfaces.ResourceHelper;
import hardcorequesting.network.DataBitHelper;
import hardcorequesting.network.DataReader;
import hardcorequesting.network.DataWriter;
import hardcorequesting.reputation.Reputation;
import hardcorequesting.reputation.ReputationMarker;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Arrays;

public abstract class QuestTaskReputation extends QuestTask {
    //for this task to be completed, all reputation settings (up to 4) has to be completed at the same time, therefore it's not saved whether you've completed one of these reputation settings, just if you've completed it all

    private ReputationSetting[] settings = new ReputationSetting[0];

    public ReputationSetting[] getSettings() {
        return settings;
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

        public ReputationMarker getUpper() {
            return upper;
        }

        public boolean isInverted() {
            return inverted;
        }

        public boolean isValid(String playerName) {
            if (getReputation() == null || !getReputation().isValid()) {
                return false;
            }
            ReputationMarker current = getReputation().getCurrentMarker(getReputation().getValue(playerName));

            return ((lower == null || lower.getValue() <= current.getValue()) && (upper == null || current.getValue() <= upper.getValue())) != inverted;
        }

        public void setLower(ReputationMarker lower) {
            this.lower = lower;
        }

        public void setUpper(ReputationMarker upper) {
            this.upper = upper;
        }
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

    public QuestTaskReputation(Quest parent, String description, String longDescription, int startOffsetY) {
        super(parent, description, longDescription);
        this.startOffsetY = startOffsetY;
    }


    protected boolean isPlayerInRange(EntityPlayer player) {
        if (settings.length > 0) {

            QuestDataTask data = getData(player);
            if (!data.completed && !player.worldObj.isRemote) {
                String name = QuestingData.getUserName(player);
                for (ReputationSetting setting : settings) {
                    if (!setting.isValid(name)) {
                        return false;
                    }
                }

                return true;
            }
        }
        return false;
    }


    @Override
    public void save(DataWriter dw) {
        dw.writeData(settings.length, DataBitHelper.REPUTATION_SETTING);
        for (ReputationSetting setting : settings) {
            dw.writeData(setting.getReputation().getId(), DataBitHelper.REPUTATION);
            dw.writeBoolean(setting.lower != null);
            if (setting.lower != null) {
                dw.writeData(setting.lower.getId(), DataBitHelper.REPUTATION_MARKER);
            }
            dw.writeBoolean(setting.upper != null);
            if (setting.upper != null) {
                dw.writeData(setting.upper.getId(), DataBitHelper.REPUTATION_MARKER);
            }
            dw.writeBoolean(setting.inverted);
        }
    }

    @Override
    public void load(DataReader dr, FileVersion version) {
        int count = dr.readData(DataBitHelper.REPUTATION_SETTING);
        settings = new ReputationSetting[count];
        for (int i = 0; i < count; i++) {
            Reputation reputation = Reputation.getReputation(dr.readData(DataBitHelper.REPUTATION));
            ReputationMarker lower = dr.readBoolean() ? reputation.getMarker(dr.readData(DataBitHelper.REPUTATION_MARKER)) : null;
            ReputationMarker upper = dr.readBoolean() ? reputation.getMarker(dr.readData(DataBitHelper.REPUTATION_MARKER)) : null;
            boolean inverted = dr.readBoolean();
            settings[i] = new ReputationSetting(reputation, lower, upper, inverted);
        }
    }

    private static final int OFFSET_Y = 27;
    private final int startOffsetY;

    @Override
    @SideOnly(Side.CLIENT)
    public void draw(GuiQuestBook gui, EntityPlayer player, int mX, int mY) {
        String info = null;
        int size = Quest.isEditing ? Math.min(settings.length + 1, DataBitHelper.REPUTATION_SETTING.getMaximum()) : settings.length;
        for (int i = 0; i < size; i++) {
            gui.applyColor(0xFFFFFFFF);
            ResourceHelper.bindResource(GuiQuestBook.MAP_TEXTURE);

            if (i >= settings.length) {
                gui.drawRect(START_X + Reputation.BAR_X, START_Y + startOffsetY + i * OFFSET_Y + Reputation.BAR_Y, Reputation.BAR_SRC_X, Reputation.BAR_SRC_Y, Reputation.BAR_WIDTH, Reputation.BAR_HEIGHT);
            } else {
                ReputationSetting setting = settings[i];
                info = setting.reputation.draw(gui, START_X, START_Y + startOffsetY + i * OFFSET_Y, mX, mY, info, getPlayerForRender(player), true, setting.lower, setting.upper, setting.inverted, null, null, getData(player).completed);
            }
        }

        if (info != null) {
            gui.drawMouseOver(info, mX + gui.getLeft(), mY + gui.getTop());
        }
    }


    protected EntityPlayer getPlayerForRender(EntityPlayer player) {
        return player;
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void onClick(GuiQuestBook gui, EntityPlayer player, int mX, int mY, int b) {
        if (Quest.isEditing && gui.getCurrentMode() != EditMode.NORMAL) {
            int size = Math.min(settings.length + 1, DataBitHelper.REPUTATION_SETTING.getMaximum());
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
    public void onUpdate(EntityPlayer player, DataReader dr) {

    }

    @Override
    public float getCompletedRatio(String playerName) {
        int count = settings.length;
        if (count == 0) {
            return 0;
        }

        int valid = 0;
        for (ReputationSetting setting : settings) {
            if (setting.isValid(playerName)) {
                valid++;
            }
        }

        return (float) valid / count;
    }

    @Override
    public void mergeProgress(String playerName, QuestDataTask own, QuestDataTask other) {
        if (other.completed) {
            own.completed = true;
        }
    }

    @Override
    public void autoComplete(String playerName) {
        getData(playerName).completed = true;
    }
}
