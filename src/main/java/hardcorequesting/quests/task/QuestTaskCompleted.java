package hardcorequesting.quests.task;

import hardcorequesting.client.EditMode;
import hardcorequesting.client.interfaces.GuiColor;
import hardcorequesting.client.interfaces.GuiQuestBook;
import hardcorequesting.event.EventTrigger;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.data.QuestDataTask;
import hardcorequesting.quests.data.QuestDataTaskCompleted;
import hardcorequesting.util.SaveHelper;
import hardcorequesting.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.UUID;

public class QuestTaskCompleted extends QuestTask {
    private static final int Y_OFFSET = 30;
    private static final int X_TEXT_OFFSET = 23;
    private static final int X_TEXT_INDENT = 0;
    private static final int Y_TEXT_OFFSET = 0;
    private static final int ITEM_SIZE = 18;
    public CompletedQuestTask[] quests = new CompletedQuestTask[0];

    public QuestTaskCompleted(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);

        register(EventTrigger.Type.QUEST_COMPLETED, EventTrigger.Type.OPEN_BOOK);
    }

    @SideOnly(Side.CLIENT)
    private CompletedQuestTask[] getEditFriendlyCompleted(CompletedQuestTask[] completed) {
        if (Quest.canQuestsBeEdited(Minecraft.getMinecraft().player)) {
            completed = Arrays.copyOf(completed, completed.length + 1);
            completed[completed.length - 1] = new CompletedQuestTask();
            return completed;
        } else {
            return completed;
        }
    }

    private boolean completed(int id, EntityPlayer player) {
        return id < quests.length && ((QuestDataTaskCompleted) getData(player)).quests[id];
    }

    public void setTask(int id, CompletedQuestTask task, EntityPlayer player) {
        if (id >= quests.length) {
            quests = Arrays.copyOf(quests, quests.length + 1);
            QuestDataTaskCompleted data = (QuestDataTaskCompleted) getData(player);
            data.quests = Arrays.copyOf(data.quests, data.quests.length + 1);
            SaveHelper.add(SaveHelper.EditType.COMPLETE_CHECK_CREATE);
        } else {
            SaveHelper.add(SaveHelper.EditType.COMPLETE_CHECK_CHANGE);
        }

        quests[id] = task;
    }

    public void setQuest(int id, UUID quest, EntityPlayer player) {
        setTask(id, id >= quests.length ? new CompletedQuestTask() : quests[id], player);

        quests[id].setQuest(quest);
    }

    @Override
    public Class<? extends QuestDataTask> getDataType() {
        return QuestDataTaskCompleted.class;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void draw(GuiQuestBook gui, EntityPlayer player, int mX, int mY) {
        CompletedQuestTask[] completed_quests = getEditFriendlyCompleted(this.quests);
        for (int i = 0; i < completed_quests.length; i++) {
            CompletedQuestTask completed = completed_quests[i];

            int x = START_X;
            int y = START_Y + i * Y_OFFSET;
            gui.drawItemStack(completed.getIconStack(), x, y, mX, mY, false);
            gui.drawString(completed.getName(), x + X_TEXT_OFFSET, y + Y_TEXT_OFFSET, 0x404040);

            if (completed(i, player)) {
                gui.drawString(GuiColor.GREEN + Translator.translate("hqm.completedMenu.visited"), x + X_TEXT_OFFSET + X_TEXT_INDENT, y + Y_TEXT_OFFSET + 9, 0.7F, 0x404040);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void onClick(GuiQuestBook gui, EntityPlayer player, int mX, int mY, int b) {
        if (Quest.canQuestsBeEdited(player) && gui.getCurrentMode() != EditMode.NORMAL) {
            CompletedQuestTask[] completed_quests = getEditFriendlyCompleted(this.quests);
            for (int i = 0; i < completed_quests.length; i++) {
                CompletedQuestTask completed = completed_quests[i];

                int x = START_X;
                int y = START_Y + i * Y_OFFSET;

                if (gui.inBounds(x, y, ITEM_SIZE, ITEM_SIZE, mX, mY)) {
                    switch (gui.getCurrentMode()) {
                        case LOCATION:
                        case ITEM:
                        case RENAME:
                            CompletedQuestTask copy = completed.copy();
                            copy.setQuest(Quest.speciallySelectedQuestId);
                            this.setTask(i, copy, player);
                            break;
                        case DELETE:
                            if (i < this.quests.length) {
                                CompletedQuestTask[] newCompleted = new CompletedQuestTask[this.quests.length - 1];
                                int id = 0;
                                for (int j = 0; j < this.quests.length; j++) {
                                    if (j != i) {
                                        newCompleted[id] = this.quests[j];
                                        id++;
                                    }
                                }
                                this.quests = newCompleted;
                                SaveHelper.add(SaveHelper.EditType.COMPLETE_CHECK_REMOVE);
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
    public void onQuestCompleted(EventTrigger.QuestCompletedEvent event) {
        checkCompleted(event.getPlayer());
    }

    public void onQuestSelected(EventTrigger.QuestSelectedEvent event) {
        checkCompleted(event.getPlayer());
    }

    @Override
    public void onUpdate(EntityPlayer player) {
        checkCompleted(player);
    }

    private void checkCompleted(EntityPlayer player) {
        World world = player.getEntityWorld();
        if (!world.isRemote && !this.isCompleted(player) && player.getServer() != null) {
            boolean[] other_completed_quests = ((QuestDataTaskCompleted) this.getData(player)).quests;

            if (other_completed_quests.length < this.quests.length) {
                boolean[] oldCompleted = ArrayUtils.addAll(other_completed_quests, (boolean[]) null);
                other_completed_quests = new boolean[this.quests.length];
                System.arraycopy(oldCompleted, 0, other_completed_quests, 0, oldCompleted.length);
                ((QuestDataTaskCompleted) this.getData(player)).quests = other_completed_quests;
            }

            boolean completed = true;

            for (int i = 0; i < this.quests.length; i++) {
                if (other_completed_quests[i]) continue;

                CompletedQuestTask task_quest = this.quests[i];
                if (task_quest == null || task_quest.getName() == null || task_quest.getQuest() == null) continue;

                Quest quest = task_quest.getQuest();
                if (quest != null) {
                    if (quest.isCompleted(player)) {
                        other_completed_quests[i] = true;
                    } else {
                        other_completed_quests[i] = false;
                        completed = false;
                    }
                } else {
                    completed = false;
                }
            }

            if (completed && this.quests.length > 0) {
                completeTask(player.getUniqueID());
                parent.sendUpdatedDataToTeam(player);
            }
        }
    }

    @Override
    public float getCompletedRatio(UUID uuid) {
        int completed = 0;
        for (boolean b : ((QuestDataTaskCompleted) getData(uuid)).quests) {
            if (b) {
                completed++;
            }
        }

        return (float) completed / quests.length;
    }

    @Override
    public void mergeProgress(UUID uuid, QuestDataTask own, QuestDataTask other) {
        boolean[] completed = ((QuestDataTaskCompleted) own).quests;
        boolean[] otherCompleted = ((QuestDataTaskCompleted) other).quests;

        boolean all = true;
        for (int i = 0; i < completed.length; i++) {
            if (otherCompleted[i]) {
                completed[i] = true;
            } else if (!completed[i]) {
                all = false;
            }
        }

        if (all) {
            completeTask(uuid);
        }
    }

    @Override
    public void autoComplete(UUID uuid) {
        boolean[] completed = ((QuestDataTaskCompleted) getData(uuid)).quests;
        for (int i = 0; i < quests.length; i++) {
            completed[i] = true;
        }
    }

    @Override
    public void copyProgress(QuestDataTask own, QuestDataTask other) {
        super.copyProgress(own, other);
        boolean[] completed = ((QuestDataTaskCompleted) own).quests;
        System.arraycopy(((QuestDataTaskCompleted) other).quests, 0, completed, 0, completed.length);
    }

    @Override
    public boolean allowDetect () {
        return true;
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
            return Translator.translate("hqm.locationMenu.vis" + id + ".title");
        }

        public String getDescription() {
            return Translator.translate("hqm.locationMenu.vis" + id + ".desc");
        }
    }

    public static class CompletedQuestTask {

        private Visibility visible = Visibility.FULL;
        private UUID quest_id;

        private CompletedQuestTask copy() {
            CompletedQuestTask completed = new CompletedQuestTask();
            completed.visible = visible;
            completed.quest_id = quest_id;

            return completed;
        }

        public ItemStack getIconStack() {
            Quest q = getQuest();
            return (q != null) ? q.getIconStack() : ItemStack.EMPTY;
        }

        public String getName() {
            Quest q = getQuest();
            return (q != null) ? q.getName() : "Invalid quest";
        }

        public Visibility getVisible() {
            return visible;
        }

        public void setVisible(Visibility visible) {
            this.visible = visible;
        }

        public void setQuest(UUID quest_id) {
            this.quest_id = quest_id;
        }

        public UUID getQuestId() {
            return this.quest_id;
        }

        public Quest getQuest () {
            if (getQuestId() == null) return null;

            return Quest.getQuest(getQuestId());
        }
    }
}

