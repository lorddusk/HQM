package hardcorequesting.quests;

import hardcorequesting.client.EditMode;
import hardcorequesting.client.interfaces.*;
import hardcorequesting.client.interfaces.edit.*;
import hardcorequesting.io.SaveHandler;
import hardcorequesting.io.adapter.QuestAdapter;
import hardcorequesting.quests.task.QuestTask;
import hardcorequesting.reputation.ReputationBar;
import hardcorequesting.util.OPBookHelper;
import hardcorequesting.util.SaveHelper;
import hardcorequesting.util.Translator;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class QuestSet {

    private static final int LINE_2_X = 10;
    private static final int LINE_2_Y = 12;
    private static final int INFO_Y = 100;
    private static int lastClicked = -1;
    private static QuestSet lastLastQuestSet = null;
    private String name;
    private String description;
    private List<String> cachedDescription;
    private Map<UUID, Quest> quests = new ConcurrentHashMap<>();
    private List<ReputationBar> reputationBars;
    private int id;

    public QuestSet(String name, String description) {
        this.name = name;
        this.description = description;
        this.reputationBars = new ArrayList<>();
        this.id = Quest.getQuestSets().size();
    }

    public static void loginReset () {
        lastClicked = -1;
        lastLastQuestSet = null;
    }

    public static void loadAll(boolean remote) {
        try {
            SaveHandler.loadAllQuestSets(SaveHandler.getFolder(remote));
            QuestAdapter.postLoad();
            orderAll(remote);
        } catch (IOException e) {
            FMLLog.log("HQM", Level.INFO, "Failed loading quest sets");
        }
    }

    public static void saveAll() {
        try {
            SaveHandler.saveAllQuestSets(SaveHandler.getLocalFolder());
            if (Quest.isEditing && Quest.saveDefault){
                SaveHandler.saveAllQuestSets(SaveHandler.getDefaultFolder());
            }
        } catch (IOException e) {
            FMLLog.log("HQM", Level.INFO, "Failed saving quest sets");
        }
    }

    public static void saveAllDefault() {
        try {
            SaveHandler.saveAllQuestSets(SaveHandler.getDefaultFolder());
        } catch (IOException e) {
            FMLLog.log("HQM", Level.INFO, "Failed saving quest sets");
        }
    }

    public static void orderAll(boolean remote) {
        try {
            final List<String> order = SaveHandler.loadQuestSetOrder(SaveHandler.getFile("sets", remote));
            if (!order.isEmpty()) {
                Quest.getQuestSets().sort(new Comparator<QuestSet>() {
                    @Override
                    public int compare(QuestSet s1, QuestSet s2) {
                        if (s1.equals(s2)) return 0;
                        int is1 = order.indexOf(s1.getName());
                        int is2 = order.indexOf(s2.getName());
                        if (is1 == -1) {
                            return is2 == -1 ? s1.getName().compareTo(s2.getName()) : 1;
                        }
                        if (is2 == -1) return -1;
                        if (is1 == is2) return 0;
                        return is1 < is2 ? -1 : 1;
                    }
                });
            }
        } catch (IOException e) {
            FMLLog.log("HQM", Level.INFO, "Failed ordering quest sets");
        }
    }

    @SideOnly(Side.CLIENT)
    public static void drawOverview(GuiQuestBook gui, ScrollBar setScroll, ScrollBar descriptionScroll, int x, int y) {
        EntityPlayer player = gui.getPlayer();
        List<QuestSet> questSets = Quest.getQuestSets();
        int start = setScroll.isVisible(gui) ? Math.round((Quest.getQuestSets().size() - GuiQuestBook.VISIBLE_SETS) * setScroll.getScroll()) : 0;

        HashMap<Quest, Boolean> isVisibleCache = new HashMap<>();
        HashMap<Quest, Boolean> isLinkFreeCache = new HashMap<>();
        for (int i = start; i < Math.min(start + GuiQuestBook.VISIBLE_SETS, questSets.size()); i++) {
            QuestSet questSet = questSets.get(i);

            int setY = GuiQuestBook.LIST_Y + (i - start) * (GuiQuestBook.TEXT_HEIGHT + GuiQuestBook.TEXT_SPACING);

            int total = questSet.getQuests().size();
            boolean enabled = questSet.isEnabled(player, isVisibleCache, isLinkFreeCache);
            int completedCount = enabled ? questSet.getCompletedCount(player, isVisibleCache, isLinkFreeCache) : 0; //no need to check for the completed count if it's not enabled

            boolean completed = true;
            int unclaimed = 0;
            for (Quest quest : questSet.getQuests().values()) {
                if (completed && !quest.isCompleted(player) && quest.isLinkFree(player, isLinkFreeCache)) {
                    completed = false;
                }
                if (quest.isCompleted(player) && quest.hasReward(player)) unclaimed++;
            }
            boolean selected = questSet == GuiQuestBook.selectedSet;
            boolean inBounds = gui.inBounds(GuiQuestBook.LIST_X, setY, gui.getStringWidth(questSet.getName(i)), GuiQuestBook.TEXT_HEIGHT, x, y);
            int color = gui.modifyingQuestSet == questSet ? 0x4040DD : enabled ? completed ? selected ? inBounds ? 0x40BB40 : 0x40A040 : inBounds ? 0x10A010 : 0x107010 : selected ? inBounds ? 0xAAAAAA : 0x888888 : inBounds ? 0x666666 : 0x404040 : 0xDDDDDD;
            gui.drawString(questSet.getName(i), GuiQuestBook.LIST_X, setY, color);

            String info;
            if (enabled) {
                if (completed)
                    info = Translator.translate("hqm.questBook.allQuests");
                else
                    info = Translator.translate("hqm.questBook.percentageQuests", ((completedCount * 100) / total));
            } else
                info = Translator.translate("hqm.questBook.locked");
            gui.drawString(info, GuiQuestBook.LIST_X + LINE_2_X, setY + LINE_2_Y, 0.7F, color);
            if (enabled && unclaimed != 0) {
                String toClaim = GuiColor.PURPLE.toString() + Translator.translate(unclaimed != 1, "hqm.questBook.unclaimedRewards", unclaimed);
                gui.drawString(toClaim, GuiQuestBook.LIST_X + LINE_2_X, setY + LINE_2_Y + 8, 0.7F, 0xFFFFFFFF);
            }
        }

        if ((Quest.canQuestsBeEdited() && gui.getCurrentMode() == EditMode.CREATE)) {
            gui.drawString(gui.getLinesFromText(Translator.translate("hqm.questBook.createNewSet"), 0.7F, 130), GuiQuestBook.DESCRIPTION_X, GuiQuestBook.DESCRIPTION_Y, 0.7F, 0x404040);
        } else {
            if (GuiQuestBook.selectedSet != null) {
                int startLine = descriptionScroll.isVisible(gui) ? Math.round((GuiQuestBook.selectedSet.getDescription(gui).size() - GuiQuestBook.VISIBLE_DESCRIPTION_LINES) * descriptionScroll.getScroll()) : 0;
                gui.drawString(GuiQuestBook.selectedSet.getDescription(gui), startLine, GuiQuestBook.VISIBLE_DESCRIPTION_LINES, GuiQuestBook.DESCRIPTION_X, GuiQuestBook.DESCRIPTION_Y, 0.7F, 0x404040);
            }

            drawQuestInfo(gui, GuiQuestBook.selectedSet, GuiQuestBook.DESCRIPTION_X, GuiQuestBook.selectedSet == null ? GuiQuestBook.DESCRIPTION_Y : INFO_Y, isVisibleCache, isLinkFreeCache);
        }

    }

    @SideOnly(Side.CLIENT)
    public static void drawQuestInfo(GuiQuestBook gui, QuestSet set, int x, int y) {
        drawQuestInfo(gui, set, x, y, new HashMap<>(), new HashMap<>());
    }

    @SideOnly(Side.CLIENT)
    private static void drawQuestInfo(GuiQuestBook gui, QuestSet set, int x, int y, HashMap<Quest, Boolean> isVisibleCache, HashMap<Quest, Boolean> isLinkFreeCache) {
        int completed = 0;
        int reward = 0;
        int enabled = 0;
        int total = 0;
        int realTotal = 0;

        EntityPlayer player = gui.getPlayer();

        for (Quest quest : Quest.getQuests().values()) {
            if (set == null || quest.hasSet(set)) {
                realTotal++;
                if (quest.isVisible(player, isVisibleCache, isLinkFreeCache)) {
                    total++;
                    if (quest.isEnabled(player, isVisibleCache, isLinkFreeCache)) {
                        enabled++;
                        if (quest.isCompleted(player)) {
                            completed++;
                            if (quest.hasReward(player)) {
                                reward++;
                            }
                        }
                    }
                }
            }
        }

        List<String> info = new ArrayList<>();
        info.add(GuiColor.GRAY.toString() + Translator.translate(total != 1, "hqm.questBook.totalQuests", total));
        info.add(GuiColor.CYAN.toString() + Translator.translate(enabled != 1, "hqm.questBook.unlockedQuests", enabled));
        info.add(GuiColor.GREEN.toString() + Translator.translate(completed != 1, "hqm.questBook.completedQuests", completed));
        info.add(GuiColor.LIGHT_BLUE.toString() + Translator.translate((enabled - completed) != 1, "hqm.questBook.totalQuests", enabled - completed));
        if (reward > 0) {
            info.add(GuiColor.PURPLE.toString() + Translator.translate(reward != 1, "hqm.questBook.unclaimedQuests", reward));
        }
        if (Quest.canQuestsBeEdited() && !GuiScreen.isCtrlKeyDown()) {
            info.add(GuiColor.LIGHT_GRAY.toString() + Translator.translate(realTotal != 1, "hqm.questBook.inclInvisiQuests", realTotal));
        }
        gui.drawString(info, x, y, 0.7F, 0x404040);
    }

    @SideOnly(Side.CLIENT)
    public static void mouseClickedOverview(GuiQuestBook gui, ScrollBar setScroll, int x, int y) {
        List<QuestSet> questSets = Quest.getQuestSets();
        int start = setScroll.isVisible(gui) ? Math.round((Quest.getQuestSets().size() - GuiQuestBook.VISIBLE_SETS) * setScroll.getScroll()) : 0;

        HashMap<Quest, Boolean> isVisibleCache = new HashMap<>();
        HashMap<Quest, Boolean> isLinkFreeCache = new HashMap<>();

        for (int i = start; i < Math.min(start + GuiQuestBook.VISIBLE_SETS, questSets.size()); i++) {
            QuestSet questSet = questSets.get(i);

            int setY = GuiQuestBook.LIST_Y + (i - start) * (GuiQuestBook.TEXT_HEIGHT + GuiQuestBook.TEXT_SPACING);
            if (gui.inBounds(GuiQuestBook.LIST_X, setY, gui.getStringWidth(questSet.getName(i)), GuiQuestBook.TEXT_HEIGHT, x, y)) {
                switch (gui.getCurrentMode()) {
                    case DELETE:
                        if (!questSet.getQuests().isEmpty()) {
                            List<Quest> quests = new ArrayList<>(questSet.getQuests().values());
                            for (Quest q : quests) {
                                questSet.removeQuest(q);
                                Quest.removeQuest(q);
                            }
                        }
                        for (int j = questSet.getId() + 1; j < Quest.getQuestSets().size(); j++) {
                            Quest.getQuestSets().get(j).decreaseId();
                        }
                        Quest.getQuestSets().remove(questSet);
                        SaveHelper.add(SaveHelper.EditType.SET_REMOVE);
                        break;
                    case SWAP_SELECT:
                        gui.modifyingQuestSet = (gui.modifyingQuestSet == questSet ? null : questSet);
                        break;
                    case RENAME:
                        gui.setEditMenu(new GuiEditMenuTextEditor(gui, gui.getPlayer(), questSet, true));
                        break;
                    default:
                        int thisClicked = gui.getPlayer().ticksExisted - lastClicked;
                        if (lastClicked != -1 && thisClicked < 6) {
                            if (GuiQuestBook.selectedSet == null && lastLastQuestSet != null) GuiQuestBook.selectedSet = lastLastQuestSet;
                            gui.openSet();
                            lastLastQuestSet = null;
                        } else {
                            GuiQuestBook.selectedSet = (questSet == GuiQuestBook.selectedSet) ? null : questSet;
                            lastClicked = gui.getPlayer().ticksExisted;
                            lastLastQuestSet = questSet;
                        }
                        break;
                }
                break;
            }
        }


        if (Quest.canQuestsBeEdited() && gui.getCurrentMode() == EditMode.RENAME) {
            if (gui.inBounds(GuiQuestBook.DESCRIPTION_X, GuiQuestBook.DESCRIPTION_Y, 130, (int) (GuiQuestBook.VISIBLE_DESCRIPTION_LINES * GuiQuestBook.TEXT_HEIGHT * 0.7F), x, y)) {
                gui.setEditMenu(new GuiEditMenuTextEditor(gui, gui.getPlayer(), GuiQuestBook.selectedSet, false));
            }
        }
    }

    public Map<UUID, Quest> getQuests() {
        return quests;
    }

    public List<ReputationBar> getReputationBars() {
        validateBars();
        return reputationBars;
    }

    private void validateBars() {
        List<ReputationBar> toRemove = new ArrayList<>();
        for (ReputationBar reputationBar : reputationBars)
            if (!reputationBar.isValid())
                toRemove.add(reputationBar);
        reputationBars.removeAll(toRemove);
    }

    public String getName() {
        return name;
    }

    private static List<String> FORBIDDEN_SET_NAMES = Arrays.asList("sets", "reputations", "bags", "con", "prn", "aux", "nul", "com1", "com2", "com3", "com4", "com5", "com6", "com7", "com8", "com9", "com0", "lpt1", "lpt2", "lpt3", "lpt4", "lpt5", "lpt6", "lpt7", "lpt8", "lpt9", "lpt0");
    private static List<String> FORBIDDEN_SET_NAME_PIECES = Arrays.asList("<", ">", ":", "\"", "\\", "/", "|", "?", "*");

    public boolean setName(String name) {
        // Let's add some sanity checking.
        String test_name = name.toLowerCase().trim();

        if (FORBIDDEN_SET_NAMES.contains(test_name)) {
            return false;
        } else {
            for (String piece : FORBIDDEN_SET_NAME_PIECES) {
                if (test_name.contains(piece)) {
                    return false;
                }
            }
        }

        int inc = 1;
        boolean found_conflict = false;

        String new_name = name;

        while (inc < 20) {
            for (QuestSet set : Quest.getQuestSets()) {
                if (set.getName().equalsIgnoreCase(new_name)) {
                    new_name = String.format("%s%d", name, inc++);
                    found_conflict = true;
                }
            }

            if (!found_conflict) {
                name = new_name;
                break;
            }
        }

        if (found_conflict) return false;

        this.name = name;
        return true;
    }

    public String getFilename() {
        return name.replaceAll(" ", "_");
    }

    public String getName(int i) {
        return (i + 1) + ". " + name;
    }

    @SideOnly(Side.CLIENT)
    public List<String> getDescription(GuiBase gui) {
        if (cachedDescription == null) {
            cachedDescription = gui.getLinesFromText(description, 0.7F, 130);
        }

        return cachedDescription;
    }

    public boolean isEnabled(EntityPlayer player) {
        return isEnabled(player, new HashMap<>(), new HashMap<>());
    }

    private boolean isEnabled(EntityPlayer player, Map<Quest, Boolean> isVisibleCache, Map<Quest, Boolean> isLinkFreeCache) {
        if (quests.isEmpty()) return false;

        for (Quest quest : quests.values()) {
            if (quest.isEnabled(player, isVisibleCache, isLinkFreeCache)) {
                return true;
            }
        }

        return false;
    }

    public boolean isCompleted(EntityPlayer player) {
        if (quests.isEmpty()) return false;

        for (Quest quest : quests.values()) {
            if (!quest.isCompleted(player)) {
                return false;
            }
        }

        return true;
    }

    public void removeQuest(Quest quest) {
        quests.remove(quest.getQuestId());
    }

    public void addQuest(Quest quest) {
        quests.put(quest.getQuestId(), quest);
    }

    public void removeRepBar(ReputationBar repBar) {
        reputationBars.remove(repBar);
    }

    public void addRepBar(ReputationBar repBar) {
        if (repBar == null) return;
        repBar.setQuestSet(this.id);
        reputationBars.add(repBar);
    }

    public int getCompletedCount(EntityPlayer player) {
        return getCompletedCount(player, new HashMap<>(), new HashMap<>());
    }

    private int getCompletedCount(EntityPlayer player, Map<Quest, Boolean> isVisibleCache, Map<Quest, Boolean> isLinkFreeCache) {
        int count = 0;
        for (Quest quest : quests.values()) {
            if (quest.isCompleted(player) && quest.isEnabled(player, isVisibleCache, isLinkFreeCache)) {
                count++;
            }
        }

        return count;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        cachedDescription = null;
    }

    public int getId() {
        return id;
    }

    public void decreaseId() {
        id--;
        for (ReputationBar reputationBar : reputationBars)
            reputationBar.setQuestSet(this.id);
    }

    @SideOnly(Side.CLIENT)
    public void draw(GuiQuestBook gui, int x0, int y0, int x, int y) {
        if (gui.isOpBook) {
            gui.drawString(gui.getLinesFromText(Translator.translate("hqm.questBook.shiftSetReset"), 0.7F, 130), 184, 192, 0.7F, 0x707070);
        }

        EntityPlayer player = gui.getPlayer();

        for (ReputationBar bar : getReputationBars()) {
            bar.draw(gui, x, y, player);
        }

        HashMap<Quest, Boolean> isVisibleCache = new HashMap<>();
        HashMap<Quest, Boolean> isLinkFreeCache = new HashMap<>();

        for (Quest child : getQuests().values()) {
            if (Quest.canQuestsBeEdited() || child.isVisible(player, isVisibleCache, isLinkFreeCache)) {
                for (Quest parent : child.getRequirements()) {
                    if (Quest.canQuestsBeEdited() || parent.isVisible(player, isVisibleCache, isLinkFreeCache)) {
                        if (parent.hasSameSetAs(child)) {
                            int color = Quest.canQuestsBeEdited() && (!child.isVisible(player, isVisibleCache, isLinkFreeCache) || !parent.isVisible(player, isVisibleCache, isLinkFreeCache)) ? 0x55404040 : 0xFF404040;
                            gui.drawLine(gui.getLeft() + parent.getGuiCenterX(), gui.getTop() + parent.getGuiCenterY(),
                                    gui.getLeft() + child.getGuiCenterX(), gui.getTop() + child.getGuiCenterY(),
                                    5,
                                    color);
                        }
                    }
                }
            }
        }
        if (Quest.canQuestsBeEdited()) {
            for (Quest child : getQuests().values()) {
                for (Quest parent : child.getOptionLinks()) {
                    if (parent.hasSameSetAs(child)) {
                        int color = !child.isVisible(player, isVisibleCache, isLinkFreeCache) || !parent.isVisible(player, isVisibleCache, isLinkFreeCache) ? 0x554040DD : 0xFF4040DD;
                        gui.drawLine(gui.getLeft() + parent.getGuiCenterX(), gui.getTop() + parent.getGuiCenterY(),
                                gui.getLeft() + child.getGuiCenterX(), gui.getTop() + child.getGuiCenterY(),
                                5,
                                color);
                    }
                }
            }
        }

        for (Quest quest : getQuests().values()) {
            if ((Quest.canQuestsBeEdited() || quest.isVisible(player, isVisibleCache, isLinkFreeCache))) {

                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

                gui.applyColor(quest == gui.modifyingQuest ? 0xFFBBFFBB : quest.getQuestId().equals(Quest.speciallySelectedQuestId) ? 0xFFF8BBFF : quest.getColorFilter(player, gui.getTick()));
                ResourceHelper.bindResource(GuiBase.MAP_TEXTURE);
                gui.drawRect(quest.getGuiX(), quest.getGuiY(), quest.getGuiU(), quest.getGuiV(player, x, y), quest.getGuiW(), quest.getGuiH());

                int iconX = quest.getGuiCenterX() - 8;
                int iconY = quest.getGuiCenterY() - 8;

                if (quest.useBigIcon()) {
                    iconX++;
                    iconY++;
                }

                gui.drawItemStack(quest.getIconStack(), iconX, iconY, true);
                GlStateManager.popMatrix();
                //ResourceHelper.bindResource(QUEST_ICONS);
                //drawRect(quest.getIconX(), quest.getIconY(), quest.getIconU(), quest.getIconV(), quest.getIconSize(), quest.getIconSize());
            }
        }


        for (Quest quest : getQuests().values()) {
            boolean editing = Quest.canQuestsBeEdited() && !GuiScreen.isCtrlKeyDown();
            if ((editing || quest.isVisible(player, isVisibleCache, isLinkFreeCache)) && quest.isMouseInObject(x, y)) {
                boolean shouldDrawText = false;
                boolean enabled = quest.isEnabled(player, isVisibleCache, isLinkFreeCache);
                String txt = "";

                if (enabled || editing) {
                    txt += quest.getName();
                }

                if (!enabled) {
                    if (editing) {
                        txt += "\n";
                    }
                    txt += GuiColor.GRAY + Translator.translate("hqm.questBook.lockedQuest");
                }

                if (!enabled || editing) {
                    int totalParentCount = 0;
                    int totalCompletedCount = 0;
                    int parentCount = 0;
                    int completed = 0;
                    List<Quest> externalQuests = new ArrayList<>();
                    for (Quest parent : quest.getRequirements()) {
                        totalParentCount++;
                        boolean isCompleted = parent.isCompleted(player);
                        if (isCompleted) {
                            totalCompletedCount++;
                        }
                        if (!parent.hasSameSetAs(quest)) {
                            externalQuests.add(parent);
                            parentCount++;
                            if (isCompleted) {
                                completed++;
                            }
                        }

                    }

                    if (editing && totalParentCount > 0) {
                        txt += "\n" + GuiColor.GRAY + Translator.translate(totalParentCount != 1, "hqm.questBook.parentCount", (totalParentCount - totalCompletedCount), totalParentCount);

                        if (Keyboard.isKeyDown(Keyboard.KEY_R)) {
                            txt += " [" + Translator.translate("hqm.questBook.holding", "R") + "]";
                            for (Quest parent : quest.getRequirements()) {
                                txt += "\n" + GuiColor.GRAY + parent.getName();
                                if (parent.isCompleted(player)) {
                                    txt += " " + GuiColor.WHITE + " [" + Translator.translate("hqm.questBook.completed") + "]";
                                }
                            }
                        } else {
                            txt += " [" + Translator.translate("hqm.questBook.hold", "R") + "]";
                        }
                    }

                    int allowedUncompleted = quest.getUseModifiedParentRequirement() ? Math.max(0, quest.getRequirements().size() - quest.getParentRequirementCount()) : 0;
                    if (parentCount - completed > allowedUncompleted || (editing && parentCount > 0)) {
                        txt += "\n" + GuiColor.PINK + Translator.translate(totalParentCount != 1, "hqm.questBook.parentCountElsewhere", (totalParentCount - totalCompletedCount), totalParentCount);
                        shouldDrawText = true;
                        if (editing) {
                            if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
                                txt += " [" + Translator.translate("hqm.questBook.holding", "E") + "]";
                                for (Quest parent : externalQuests) {
                                    txt += "\n" + GuiColor.PINK + parent.getName() + " (" + parent.getQuestSet().getName() + ")";
                                    if (parent.isCompleted(player)) {
                                        txt += " " + GuiColor.WHITE + " [" + Translator.translate("hqm.questBook.completed") + "]";
                                    }
                                }
                            } else {
                                txt += " [" + Translator.translate("hqm.questBook.hold", "E") + "]";
                            }
                        }
                    }

                    if (editing && quest.getUseModifiedParentRequirement()) {
                        txt += "\n" + GuiColor.MAGENTA;
                        int amount = quest.getParentRequirementCount();
                        if (amount < quest.getRequirements().size()) {
                            txt += Translator.translate(amount != 1, "hqm.questBook.reqOnly", amount);
                        } else if (amount > quest.getRequirements().size()) {
                            txt += Translator.translate(amount != 1, "hqm.questBook.reqMore", amount);
                        } else {
                            txt += Translator.translate(amount != 1, "hqm.questBook.reqAll", amount);
                        }

                    }
                }

                if (enabled || editing) {
                    if (quest.isCompleted(player)) {
                        txt += "\n" + GuiColor.GREEN + Translator.translate("hqm.questBook.completed");
                    }
                    if (quest.hasReward(player)) {
                        txt += "\n" + GuiColor.PURPLE + Translator.translate("hqm.questBook.unclaimedReward");
                    }

                    String repeatMessage = enabled ? quest.getRepeatInfo().getMessage(quest, player) : quest.getRepeatInfo().getShortMessage();
                    if (repeatMessage != null) {
                        txt += "\n" + repeatMessage;
                    }

                    if (editing) {
                        int totalTasks = 0;
                        int completedTasks = 0;
                        for (QuestTask task : quest.getTasks()) {
                            totalTasks++;
                            if (task.isCompleted(player)) {
                                completedTasks++;
                            }
                        }

                        if (totalTasks == 0) {
                            txt += "\n" + GuiColor.RED + Translator.translate("hqm.questBook.noTasks");
                        } else {
                            txt += "\n" + GuiColor.CYAN + Translator.translate("hqm.questBook.completedTasks", completedTasks, totalTasks);

                            if (Keyboard.isKeyDown(Keyboard.KEY_T)) {
                                txt += " [" + Translator.translate("hqm.questBook.holding", "T") + "]";
                                for (QuestTask task : quest.getTasks()) {
                                    txt += "\n" + GuiColor.CYAN + task.getDescription();
                                    if (task.isCompleted(player)) {
                                        txt += GuiColor.WHITE + " [" + Translator.translate("hqm.questBook.completed") + "]";
                                    }
                                }
                            } else {
                                txt += " [" + Translator.translate("hqm.questBook.holding", "T") + "]";
                            }
                        }

                        String triggerMessage = quest.getTriggerType().getMessage(quest);
                        if (triggerMessage != null) {
                            txt += "\n" + triggerMessage;
                        }

                        if (!quest.isVisible(player, isVisibleCache, isLinkFreeCache)) {
                            String invisibilityMessage;
                            if (quest.isLinkFree(player, isLinkFreeCache)) {
                                boolean parentInvisible = false;
                                for (Quest parent : quest.getRequirements()) {
                                    if (!parent.isVisible(player, isVisibleCache, isLinkFreeCache)) {
                                        parentInvisible = true;
                                        break;
                                    }
                                }


                                switch (quest.getTriggerType()) {
                                    case ANTI_TRIGGER:
                                        invisibilityMessage = Translator.translate("hqm.questBook.invisLocked");
                                        break;
                                    case QUEST_TRIGGER:
                                        invisibilityMessage = Translator.translate("hqm.questBook.invisPerm");
                                        parentInvisible = false;
                                        break;
                                    case TASK_TRIGGER:
                                        invisibilityMessage = Translator.translate(quest.getTriggerTasks() != 1, "hqm.questBook.invisCount", quest.getTriggerTasks());
                                        break;
                                    default:
                                        invisibilityMessage = null;
                                }

                                if (parentInvisible) {
                                    String parentText = Translator.translate("hqm.questBook.invisInherit");
                                    if (invisibilityMessage == null) {
                                        invisibilityMessage = parentText;
                                    } else {
                                        invisibilityMessage = parentText + " " + Translator.translate("hqm.questBook.and") + " " + invisibilityMessage;
                                    }
                                }

                            } else {
                                invisibilityMessage = Translator.translate("hqm.questBook.invisOption");
                            }

                            if (invisibilityMessage != null) {
                                txt += "\n" + GuiColor.LIGHT_BLUE + invisibilityMessage;
                            }
                        }


                        List<UUID> ids = new ArrayList<>();
                        for (Quest option : quest.getOptionLinks()) {
                            ids.add(option.getQuestId());
                        }
                        for (Quest option : quest.getReversedOptionLinks()) {
                            UUID id = option.getQuestId();
                            if (!ids.contains(id)) {
                                ids.add(id);
                            }
                        }
                        int optionLinks = ids.size();
                        if (optionLinks > 0) {
                            txt += "\n" + GuiColor.BLUE + Translator.translate(optionLinks != 1, "hqm.questBook.optionLinks", optionLinks);

                            if (Keyboard.isKeyDown(Keyboard.KEY_O)) {
                                txt += " [" + Translator.translate("hqm.questBook.holding", "O") + "]";
                                for (UUID id : ids) {
                                    Quest option = Quest.getQuest(id);
                                    txt += "\n" + GuiColor.BLUE + option.getName();
                                    if (!option.hasSameSetAs(quest)) {
                                        txt += " (" + option.getQuestSet().getName() + ")";
                                    }
                                }
                            } else {
                                txt += " [" + Translator.translate("hqm.questBook.hold", "O") + "]";
                            }
                        }

                    }


                    List<Quest> externalQuests = new ArrayList<Quest>();
                    int childCount = 0;
                    for (Quest child : quest.getReversedRequirement()) {
                        if (!quest.hasSameSetAs(child)) {
                            childCount++;
                            externalQuests.add(child);
                        }
                    }

                    if (childCount > 0) {
                        txt += "\n" + GuiColor.PINK + Translator.translate(childCount != 1, "hqm.questBook.childUnlocks", childCount);
                        if (editing) {
                            if (Keyboard.isKeyDown(Keyboard.KEY_U)) {
                                txt += " [" + Translator.translate("hqm.questBook.holding", "U") + "]";
                                for (Quest child : externalQuests) {
                                    txt += "\n" + GuiColor.PINK + child.getName() + " (" + child.getQuestSet().getName() + ")";
                                }
                            } else {
                                txt += " [" + Translator.translate("hqm.questBook.hold", "U") + "]";
                            }
                        }
                    }
                    shouldDrawText = true;

                }

                if (editing) {
                    txt += "\n\n" + GuiColor.GRAY + Translator.translate("hqm.questBook.ctrlNonEditor");
                }

                if (gui.isOpBook && GuiScreen.isShiftKeyDown()) {
                    if (quest.isCompleted(player)) {
                        txt += "\n\n" + GuiColor.RED + Translator.translate("hqm.questBook.resetQuest");
                    } else {
                        txt += "\n\n" + GuiColor.ORANGE + Translator.translate("hqm.questBook.completeQuest");
                    }
                }

                if (shouldDrawText && gui.getCurrentMode() != EditMode.MOVE) {
                    gui.drawMouseOver(txt, x0, y0);
                }
                break;
            }
        }

    }

    @SideOnly(Side.CLIENT)
    public void mouseClicked(GuiQuestBook gui, int x, int y) {
        EntityPlayer player = gui.getPlayer();
        if (Quest.canQuestsBeEdited() && (gui.getCurrentMode() == EditMode.CREATE || gui.getCurrentMode() == EditMode.REP_BAR_CREATE)) {
            switch (gui.getCurrentMode()) {
                case CREATE:
                    if (x > 0) {
                        Quest newQuest = new Quest("Unnamed", "Unnamed quest", 0, 0, false);
                        newQuest.setGuiCenterX(x);
                        newQuest.setGuiCenterY(y);
                        newQuest.setQuestSet(this);
                        SaveHelper.add(SaveHelper.EditType.QUEST_CREATE);
                    }
                    break;
                case REP_BAR_CREATE:
                    gui.setEditMenu(new ReputationBar.EditGui(gui, player, x, y, this.getId()));
                    break;
                default:
                    break;
            }
        } else {
            HashMap<Quest, Boolean> isVisibleCache = new HashMap<>();
            HashMap<Quest, Boolean> isLinkFreeCache = new HashMap<>();
            for (Quest quest : this.getQuests().values()) {
                if ((Quest.canQuestsBeEdited() || quest.isVisible(player, isVisibleCache, isLinkFreeCache)) && quest.isMouseInObject(x, y)) {
                    if (Quest.canQuestsBeEdited()) {
                        switch (gui.getCurrentMode()) {
                            case MOVE:
                                gui.modifyingQuest = quest;
                                SaveHelper.add(SaveHelper.EditType.QUEST_MOVE);
                                break;
                            case REQUIREMENT:
                                if (gui.modifyingQuest == quest) {
                                    if (GuiScreen.isShiftKeyDown())
                                        gui.modifyingQuest.clearRequirements();
                                    gui.modifyingQuest = null;
                                } else if (gui.modifyingQuest == null) {
                                    gui.modifyingQuest = quest;
                                } else {
                                    gui.modifyingQuest.addRequirement(quest.getQuestId());
                                }
                                break;
                            case SIZE:
                                int cX = quest.getGuiCenterX();
                                int cY = quest.getGuiCenterY();
                                quest.setBigIcon(!quest.useBigIcon());
                                quest.setGuiCenterX(cX);
                                quest.setGuiCenterY(cY);
                                SaveHelper.add(SaveHelper.EditType.QUEST_SIZE_CHANGE);
                                break;
                            case ITEM:
                                gui.setEditMenu(new GuiEditMenuItem(gui, player, quest.getIconStack(), quest.getQuestId(), GuiEditMenuItem.Type.QUEST_ICON, 1, ItemPrecision.PRECISE));
                                break;
                            case DELETE:
                                Quest.removeQuest(quest);
                                SaveHelper.add(SaveHelper.EditType.QUEST_REMOVE);
                                break;
                            case SWAP:
                                if (gui.modifyingQuestSet != null && gui.modifyingQuestSet != this) {
                                    quest.setQuestSet(gui.modifyingQuestSet);
                                    SaveHelper.add(SaveHelper.EditType.QUEST_CHANGE_SET);
                                }
                                break;
                            case REPEATABLE:
                                gui.setEditMenu(new GuiEditMenuRepeat(gui, player, quest));
                                break;
                            case REQUIRED_PARENTS:
                                gui.setEditMenu(new GuiEditMenuParentCount(gui, player, quest));
                                break;
                            case QUEST_SELECTION:
                                if (Quest.speciallySelectedQuestId == quest.getQuestId()) Quest.speciallySelectedQuestId = null;
                                else Quest.speciallySelectedQuestId = quest.getQuestId();
                                break;
                            case QUEST_OPTION:
                                if (gui.modifyingQuest == quest) {
                                    if (GuiScreen.isShiftKeyDown())
                                        gui.modifyingQuest.clearOptionLinks();
                                    gui.modifyingQuest = null;
                                } else if (gui.modifyingQuest == null) {
                                    gui.modifyingQuest = quest;
                                } else {
                                    gui.modifyingQuest.addOptionLink(quest.getQuestId());
                                }
                                break;
                            case TRIGGER:
                                gui.setEditMenu(new GuiEditMenuTrigger(gui, player, quest));
                                break;
                            case NORMAL:
                                if (gui.isOpBook && GuiScreen.isShiftKeyDown()) {
                                    OPBookHelper.reverseQuestCompletion(quest, player);
                                    break;
                                } // deliberate drop through
                            default:
                                GuiQuestBook.selectedQuest = quest;
                                quest.onOpen(gui, player);
                                break;
                        }
                    } else {
                        if (gui.isOpBook && GuiScreen.isShiftKeyDown()) {
                            OPBookHelper.reverseQuestCompletion(quest, player);
                        } else {
                            GuiQuestBook.selectedQuest = quest;
                            quest.onOpen(gui, player);
                        }
                    }
                    break;
                }
            }
        }

        if (Quest.canQuestsBeEdited())
            for (ReputationBar reputationBar : new ArrayList<>(this.getReputationBars()))
                reputationBar.mouseClicked(gui, x, y);
    }
}
