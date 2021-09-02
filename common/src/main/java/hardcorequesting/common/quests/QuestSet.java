package hardcorequesting.common.quests;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.*;
import hardcorequesting.common.client.interfaces.edit.*;
import hardcorequesting.common.config.HQMConfig;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.reputation.ReputationBar;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.OPBookHelper;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QuestSet {
    
    private static final int LINE_2_X = 10;
    private static final int LINE_2_Y = 12;
    private static final int INFO_Y = 100;
    private static int lastClicked = -1;
    private static QuestSet lastLastQuestSet = null;
    private String name;
    private String description;
    private List<FormattedText> cachedDescription;
    private Map<UUID, Quest> quests = new ConcurrentHashMap<>();
    private List<ReputationBar> reputationBars;
    private int id;
    
    public QuestSet(String name, String description) {
        this.name = name;
        this.description = description;
        this.reputationBars = new ArrayList<>();
        this.id = Quest.getQuestSets().size();
    }
    
    public static void loginReset() {
        lastClicked = -1;
        lastLastQuestSet = null;
    }
    
    @Environment(EnvType.CLIENT)
    public static void drawOverview(PoseStack matrices, GuiQuestBook gui, ScrollBar setScroll, ScrollBar descriptionScroll, int x, int y) {
        Player player = gui.getPlayer();
        List<QuestSet> questSets = Quest.getQuestSets();
        int start = setScroll.isVisible(gui) ? Math.round((Quest.getQuestSets().size() - GuiQuestBook.VISIBLE_SETS) * setScroll.getScroll()) : 0;
        
        HashMap<Quest, Boolean> isVisibleCache = new HashMap<>();
        HashMap<Quest, Boolean> isLinkFreeCache = new HashMap<>();
        for (int i = start; i < Math.min(start + GuiQuestBook.VISIBLE_SETS, questSets.size()); i++) {
            QuestSet questSet = questSets.get(i);
            
            int setY = GuiQuestBook.LIST_Y + (i - start) * (GuiQuestBook.TEXT_HEIGHT + GuiQuestBook.TEXT_SPACING);
            
            int total = questSet.getQuests().size();
            
            boolean enabled = questSet.isEnabled(player, isVisibleCache, isLinkFreeCache);
            
            int completedCount; //no need to check for the completed count if it's not enabled
            if (enabled) {
                completedCount = questSet.getCompletedCount(player, isVisibleCache, isLinkFreeCache);
            } else {
                completedCount = 0;
            }
            
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
            
            int color;
            if (gui.modifyingQuestSet == questSet) {
                color = HQMConfig.CURRENTLY_MODIFYING_QUEST_SET;
            } else if (enabled) {
                if (completed) {
                    if (selected) {
                        if (inBounds) {
                            color = HQMConfig.COMPLETED_SELECTED_IN_BOUNDS_SET;
                        } else {
                            color = HQMConfig.COMPLETED_SELECTED_OUT_OF_BOUNDS_SET;
                        }
                    } else if (inBounds) {
                        color = HQMConfig.COMPLETED_UNSELECTED_IN_BOUNDS_SET;
                    } else {
                        color = HQMConfig.COMPLETED_UNSELECTED_OUT_OF_BOUNDS_SET;
                    }
                } else if (selected) {
                    if (inBounds) {
                        color = HQMConfig.UNCOMPLETED_SELECTED_IN_BOUNDS_SET;
                    } else {
                        color = HQMConfig.UNCOMPLETED_SELECTED_OUT_OF_BOUNDS_SET;
                    }
                } else if (inBounds) {
                    color = HQMConfig.UNCOMPLETED_UNSELECTED_IN_BOUNDS_SET;
                } else {
                    color = HQMConfig.UNCOMPLETED_UNSELECTED_OUT_OF_BOUNDS_SET;
                }
            } else {
                color = HQMConfig.DISABLED_SET;
            }
            gui.drawString(matrices, Translator.plain(questSet.getName(i)), GuiQuestBook.LIST_X, setY, color);
            
            FormattedText info;
            if (enabled) {
                if (completed)
                    info = Translator.translatable("hqm.questBook.allQuests");
                else
                    info = Translator.translatable("hqm.questBook.percentageQuests", ((completedCount * 100) / total));
            } else
                info = Translator.translatable("hqm.questBook.locked");
            gui.drawString(matrices, info, GuiQuestBook.LIST_X + LINE_2_X, setY + LINE_2_Y, 0.7F, color);
            if (enabled && unclaimed != 0) {
                FormattedText toClaim = Translator.pluralTranslated(unclaimed != 1, "hqm.questBook.unclaimedRewards", GuiColor.PURPLE, unclaimed);
                gui.drawString(matrices, toClaim, GuiQuestBook.LIST_X + LINE_2_X, setY + LINE_2_Y + 8, 0.7F, 0xFFFFFFFF);
            }
        }
        
        if ((Quest.canQuestsBeEdited() && gui.getCurrentMode() == EditMode.CREATE)) {
            gui.drawString(matrices, gui.getLinesFromText(Translator.translatable("hqm.questBook.createNewSet"), 0.7F, 130), GuiQuestBook.DESCRIPTION_X, GuiQuestBook.DESCRIPTION_Y, 0.7F, 0x404040);
        } else {
            if (GuiQuestBook.selectedSet != null) {
                int startLine = descriptionScroll.isVisible(gui) ? Math.round((GuiQuestBook.selectedSet.getDescription(gui).size() - GuiQuestBook.VISIBLE_DESCRIPTION_LINES) * descriptionScroll.getScroll()) : 0;
                gui.drawString(matrices, GuiQuestBook.selectedSet.getDescription(gui), startLine, GuiQuestBook.VISIBLE_DESCRIPTION_LINES, GuiQuestBook.DESCRIPTION_X, GuiQuestBook.DESCRIPTION_Y, 0.7F, 0x404040);
            }
            
            drawQuestInfo(matrices, gui, GuiQuestBook.selectedSet, GuiQuestBook.DESCRIPTION_X, GuiQuestBook.selectedSet == null ? GuiQuestBook.DESCRIPTION_Y : INFO_Y, isVisibleCache, isLinkFreeCache);
        }
        
    }
    
    @Environment(EnvType.CLIENT)
    public static void drawQuestInfo(PoseStack matrices, GuiQuestBook gui, QuestSet set, int x, int y) {
        drawQuestInfo(matrices, gui, set, x, y, new HashMap<>(), new HashMap<>());
    }
    
    @Environment(EnvType.CLIENT)
    private static void drawQuestInfo(PoseStack matrices, GuiQuestBook gui, QuestSet set, int x, int y, HashMap<Quest, Boolean> isVisibleCache, HashMap<Quest, Boolean> isLinkFreeCache) {
        int completed = 0;
        int reward = 0;
        int enabled = 0;
        int total = 0;
        int realTotal = 0;
        
        Player player = gui.getPlayer();
        
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
        
        List<FormattedText> info = new ArrayList<>();
        info.add(Translator.pluralTranslated(total != 1, "hqm.questBook.totalQuests", GuiColor.GRAY, total));
        info.add(Translator.pluralTranslated(enabled != 1, "hqm.questBook.unlockedQuests", GuiColor.CYAN, enabled));
        info.add(Translator.pluralTranslated(completed != 1, "hqm.questBook.completedQuests", GuiColor.GREEN, completed));
        info.add(Translator.pluralTranslated((enabled - completed) != 1, "hqm.questBook.totalQuests", GuiColor.LIGHT_BLUE, enabled - completed));
        if (reward > 0) {
            info.add(Translator.pluralTranslated(reward != 1, "hqm.questBook.unclaimedQuests", GuiColor.PURPLE, reward));
        }
        if (Quest.canQuestsBeEdited() && !Screen.hasControlDown()) {
            info.add(Translator.pluralTranslated(realTotal != 1, "hqm.questBook.inclInvisiQuests", GuiColor.LIGHT_GRAY, realTotal));
        }
        gui.drawString(matrices, info, x, y, 0.7F, 0x404040);
    }
    
    @Environment(EnvType.CLIENT)
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
                        SaveHelper.add(EditType.SET_REMOVE);
                        break;
                    case SWAP_SELECT:
                        gui.modifyingQuestSet = (gui.modifyingQuestSet == questSet ? null : questSet);
                        break;
                    case RENAME:
                        TextMenu.display(gui, gui.getPlayer(), questSet.getName(), true,
                                result -> {
                                    if (!questSet.setName(result)) {
                                        gui.getPlayer().sendMessage(new TranslatableComponent("hqm.editMode.rename.invalid_set").setStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.RED)), Util.NIL_UUID);
                                    }
                                });
                        break;
                    default:
                        int thisClicked = gui.getPlayer().tickCount - lastClicked;
                        
                        if (thisClicked < 0) {
                            lastClicked = -1;
                        }
                        
                        if (lastClicked != -1 && thisClicked < 6) {
                            if (GuiQuestBook.selectedSet == null && lastLastQuestSet != null) GuiQuestBook.selectedSet = lastLastQuestSet;
                            gui.openSet();
                            lastLastQuestSet = null;
                        } else {
                            GuiQuestBook.selectedSet = (questSet == GuiQuestBook.selectedSet) ? null : questSet;
                            lastClicked = gui.getPlayer().tickCount;
                            lastLastQuestSet = questSet;
                        }
                        break;
                }
                break;
            }
        }
        
        
        if (Quest.canQuestsBeEdited() && gui.getCurrentMode() == EditMode.RENAME) {
            if (gui.inBounds(GuiQuestBook.DESCRIPTION_X, GuiQuestBook.DESCRIPTION_Y, 130, (int) (GuiQuestBook.VISIBLE_DESCRIPTION_LINES * GuiQuestBook.TEXT_HEIGHT * 0.7F), x, y)) {
                TextMenu.display(gui, gui.getPlayer(), GuiQuestBook.selectedSet.getDescription(), false, GuiQuestBook.selectedSet::setDescription);
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
        
        String new_name = name;
        
        List<String> names = Quest.getQuestSets().stream().filter((q) -> q != this).map((q) -> q.getName().toLowerCase()).collect(Collectors.toList());
        
        while (names.contains(new_name.toLowerCase())) {
            new_name = String.format("%s%d", name, inc++);
            
            if (inc >= 20) return false;
        }
        
        this.name = new_name;
        return true;
    }
    
    public String getFilename() {
        return name.replaceAll(" ", "_");
    }
    
    public String getName(int i) {
        return (i + 1) + ". " + name;
    }
    
    @Environment(EnvType.CLIENT)
    public List<FormattedText> getDescription(GuiBase gui) {
        if (cachedDescription == null) {
            cachedDescription = gui.getLinesFromText(Translator.plain(description), 0.7F, 130);
        }
        
        return cachedDescription;
    }
    
    public boolean isEnabled(Player player) {
        return isEnabled(player, new HashMap<>(), new HashMap<>());
    }
    
    private boolean isEnabled(Player player, Map<Quest, Boolean> isVisibleCache, Map<Quest, Boolean> isLinkFreeCache) {
        if (quests.isEmpty()) return false;
        
        for (Quest quest : quests.values()) {
            if (quest.isEnabled(player, isVisibleCache, isLinkFreeCache)) {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean isCompleted(Player player) {
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
    
    public int getCompletedCount(Player player) {
        return getCompletedCount(player, new HashMap<>(), new HashMap<>());
    }
    
    private int getCompletedCount(Player player, Map<Quest, Boolean> isVisibleCache, Map<Quest, Boolean> isLinkFreeCache) {
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
    
    @Environment(EnvType.CLIENT)
    public void draw(PoseStack matrices, GuiQuestBook gui, int x0, int y0, int x, int y) {
        
        if (gui.isOpBook) {
            gui.drawString(matrices, gui.getLinesFromText(Translator.translatable("hqm.questBook.shiftSetReset"), 0.7F, 130), 184, 192, 0.7F, 0x707070);
        }
        
        Player player = gui.getPlayer();
        
        for (ReputationBar bar : getReputationBars()) {
            bar.draw(matrices, gui, x, y, player);
        }
        
        HashMap<Quest, Boolean> isVisibleCache = new HashMap<>();
        HashMap<Quest, Boolean> isLinkFreeCache = new HashMap<>();
    
        drawConnectingLines(matrices, gui, player, isVisibleCache, isLinkFreeCache);
        
        gui.setBlitOffset(50);
        
        drawQuestIcons(matrices, gui, x, y, player, isVisibleCache, isLinkFreeCache);
    
        for (Quest quest : getQuests().values()) {
            boolean editing = Quest.canQuestsBeEdited() && !Screen.hasControlDown();
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
                    txt += GuiColor.GRAY + I18n.get("hqm.questBook.lockedQuest");
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
                        txt += "\n" + GuiColor.GRAY + Translator.rawString(Translator.pluralTranslated(totalParentCount != 1, "hqm.questBook.parentCount", (totalParentCount - totalCompletedCount), totalParentCount));
                        
                        if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_R)) {
                            txt += " [" + I18n.get("hqm.questBook.holding", "R") + "]";
                            for (Quest parent : quest.getRequirements()) {
                                txt += "\n" + GuiColor.GRAY + parent.getName();
                                if (parent.isCompleted(player)) {
                                    txt += " " + GuiColor.WHITE + " [" + I18n.get("hqm.questBook.completed") + "]";
                                }
                            }
                        } else {
                            txt += " [" + I18n.get("hqm.questBook.hold", "R") + "]";
                        }
                    }
                    
                    int allowedUncompleted = quest.getUseModifiedParentRequirement() ? Math.max(0, quest.getRequirements().size() - quest.getParentRequirementCount()) : 0;
                    if (parentCount - completed > allowedUncompleted || (editing && parentCount > 0)) {
                        txt += "\n" + GuiColor.PINK + Translator.rawString(Translator.pluralTranslated(totalParentCount != 1, "hqm.questBook.parentCountElsewhere", (totalParentCount - totalCompletedCount), totalParentCount));
                        shouldDrawText = true;
                        if (editing) {
                            if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_E)) {
                                txt += " [" + I18n.get("hqm.questBook.holding", "E") + "]";
                                for (Quest parent : externalQuests) {
                                    txt += "\n" + GuiColor.PINK + parent.getName() + " (" + parent.getQuestSet().getName() + ")";
                                    if (parent.isCompleted(player)) {
                                        txt += " " + GuiColor.WHITE + " [" + I18n.get("hqm.questBook.completed") + "]";
                                    }
                                }
                            } else {
                                txt += " [" + I18n.get("hqm.questBook.hold", "E") + "]";
                            }
                        }
                    }
                    
                    if (editing && quest.getUseModifiedParentRequirement()) {
                        txt += "\n" + GuiColor.MAGENTA;
                        int amount = quest.getParentRequirementCount();
                        if (amount < quest.getRequirements().size()) {
                            txt += Translator.rawString(Translator.pluralTranslated(amount != 1, "hqm.questBook.reqOnly", amount));
                        } else if (amount > quest.getRequirements().size()) {
                            txt += Translator.rawString(Translator.pluralTranslated(amount != 1, "hqm.questBook.reqMore", amount));
                        } else {
                            txt += Translator.rawString(Translator.pluralTranslated(amount != 1, "hqm.questBook.reqAll", amount));
                        }
                        
                    }
                }
                
                if (enabled || editing) {
                    if (quest.isCompleted(player)) {
                        txt += "\n" + GuiColor.GREEN + I18n.get("hqm.questBook.completed");
                    }
                    if (quest.hasReward(player)) {
                        txt += "\n" + GuiColor.PURPLE + I18n.get("hqm.questBook.unclaimedReward");
                    }
                    
                    String repeatMessage = enabled ? quest.getRepeatInfo().getMessage(quest, player) : quest.getRepeatInfo().getShortMessage();
                    if (repeatMessage != null) {
                        txt += "\n" + repeatMessage;
                    }
                    
                    if (editing) {
                        int totalTasks = 0;
                        int completedTasks = 0;
                        for (QuestTask<?> task : quest.getTasks()) {
                            totalTasks++;
                            if (task.isCompleted(player)) {
                                completedTasks++;
                            }
                        }
                        
                        if (totalTasks == 0) {
                            txt += "\n" + GuiColor.RED + I18n.get("hqm.questBook.noTasks");
                        } else {
                            txt += "\n" + GuiColor.CYAN + I18n.get("hqm.questBook.completedTasks", completedTasks, totalTasks);
                            
                            if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_T)) {
                                txt += " [" + I18n.get("hqm.questBook.holding", "T") + "]";
                                for (QuestTask<?> task : quest.getTasks()) {
                                    txt += "\n" + GuiColor.CYAN + task.getDescription();
                                    if (task.isCompleted(player)) {
                                        txt += GuiColor.WHITE + " [" + I18n.get("hqm.questBook.completed") + "]";
                                    }
                                }
                            } else {
                                txt += " [" + I18n.get("hqm.questBook.holding", "T") + "]";
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
                                        invisibilityMessage = I18n.get("hqm.questBook.invisLocked");
                                        break;
                                    case QUEST_TRIGGER:
                                        invisibilityMessage = I18n.get("hqm.questBook.invisPerm");
                                        parentInvisible = false;
                                        break;
                                    case TASK_TRIGGER:
                                        invisibilityMessage = Translator.rawString(Translator.pluralTranslated(quest.getTriggerTasks() != 1, "hqm.questBook.invisCount", quest.getTriggerTasks()));
                                        break;
                                    default:
                                        invisibilityMessage = null;
                                }
                                
                                if (parentInvisible) {
                                    String parentText = I18n.get("hqm.questBook.invisInherit");
                                    if (invisibilityMessage == null) {
                                        invisibilityMessage = parentText;
                                    } else {
                                        invisibilityMessage = parentText + " " + I18n.get("hqm.questBook.and") + " " + invisibilityMessage;
                                    }
                                }
                                
                            } else {
                                invisibilityMessage = I18n.get("hqm.questBook.invisOption");
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
                            txt += "\n" + GuiColor.BLUE + Translator.rawString(Translator.pluralTranslated(optionLinks != 1, "hqm.questBook.optionLinks", optionLinks));
                            
                            if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_O)) {
                                txt += " [" + I18n.get("hqm.questBook.holding", "O") + "]";
                                for (UUID id : ids) {
                                    Quest option = Quest.getQuest(id);
                                    txt += "\n" + GuiColor.BLUE + option.getName();
                                    if (!option.hasSameSetAs(quest)) {
                                        txt += " (" + option.getQuestSet().getName() + ")";
                                    }
                                }
                            } else {
                                txt += " [" + I18n.get("hqm.questBook.hold", "O") + "]";
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
                        txt += "\n" + GuiColor.PINK + Translator.rawString(Translator.pluralTranslated(childCount != 1, "hqm.questBook.childUnlocks", childCount));
                        if (editing) {
                            if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_U)) {
                                txt += " [" + I18n.get("hqm.questBook.holding", "U") + "]";
                                for (Quest child : externalQuests) {
                                    txt += "\n" + GuiColor.PINK + child.getName() + " (" + child.getQuestSet().getName() + ")";
                                }
                            } else {
                                txt += " [" + I18n.get("hqm.questBook.hold", "U") + "]";
                            }
                        }
                    }
                    shouldDrawText = true;
                    
                }
                
                if (editing) {
                    txt += "\n\n" + GuiColor.GRAY + I18n.get("hqm.questBook.ctrlNonEditor");
                }
                
                if (gui.isOpBook && Screen.hasShiftDown()) {
                    if (quest.isCompleted(player)) {
                        txt += "\n\n" + GuiColor.RED + I18n.get("hqm.questBook.resetQuest");
                    } else {
                        txt += "\n\n" + GuiColor.ORANGE + I18n.get("hqm.questBook.completeQuest");
                    }
                }
                
                if (shouldDrawText && gui.getCurrentMode() != EditMode.MOVE) {
                    gui.renderTooltipL(matrices, Stream.of(txt.split("\n")).map(FormattedText::of).collect(Collectors.toList()), x0, y0);
                }
                break;
            }
        }
        
    }
    
    @Environment(EnvType.CLIENT)
    private void drawConnectingLines(PoseStack matrices, GuiQuestBook gui, Player player, HashMap<Quest, Boolean> isVisibleCache, HashMap<Quest, Boolean> isLinkFreeCache) {
        for (Quest child : getQuests().values()) {
            if (Quest.canQuestsBeEdited() || child.isVisible(player, isVisibleCache, isLinkFreeCache)) {
                for (Quest parent : child.getRequirements()) {
                    if (Quest.canQuestsBeEdited() || parent.isVisible(player, isVisibleCache, isLinkFreeCache)) {
                        if (parent.hasSameSetAs(child)) {
                            int color = Quest.canQuestsBeEdited() && (!child.isVisible(player, isVisibleCache, isLinkFreeCache) || !parent.isVisible(player, isVisibleCache, isLinkFreeCache)) ? 0x55404040 : 0xFF404040;
                            gui.drawLine(matrices, gui.getLeft() + parent.getGuiCenterX(), gui.getTop() + parent.getGuiCenterY(),
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
                        gui.drawLine(matrices, gui.getLeft() + parent.getGuiCenterX(), gui.getTop() + parent.getGuiCenterY(),
                                gui.getLeft() + child.getGuiCenterX(), gui.getTop() + child.getGuiCenterY(),
                                5,
                                color);
                    }
                }
            }
        }
    }
    
    @Environment(EnvType.CLIENT)
    private void drawQuestIcons(PoseStack matrices, GuiQuestBook gui, int x, int y, Player player, HashMap<Quest, Boolean> isVisibleCache, HashMap<Quest, Boolean> isLinkFreeCache) {
        for (Quest quest : getQuests().values()) {
            if ((Quest.canQuestsBeEdited() || quest.isVisible(player, isVisibleCache, isLinkFreeCache))) {
                
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                
                int color;
                if (quest == gui.modifyingQuest) color = 0xffbbffbb;
                else if (quest.getQuestId().equals(Quest.speciallySelectedQuestId)) color = 0xfff8bbff;
                else color = quest.getColorFilter(player, gui.getTick());
                
                gui.applyColor(color);
                ResourceHelper.bindResource(GuiBase.MAP_TEXTURE);
                gui.drawRect(matrices, quest.getGuiX(), quest.getGuiY(), quest.getGuiU(), quest.getGuiV(player, x, y), quest.getGuiW(), quest.getGuiH());
                
                int iconX = quest.getGuiCenterX() - 8;
                int iconY = quest.getGuiCenterY() - 8;
                
                if (quest.useBigIcon()) {
                    iconX++;
                    iconY++;
                }
                
                final int iconX_ = iconX, iconY_ = iconY;
                quest.getIconStack().ifLeft(itemStack -> gui.drawItemStack(itemStack, iconX_, iconY_, true))
                        .ifRight(fluidStack -> gui.drawFluid(fluidStack, matrices, iconX_, iconY_));
                //ResourceHelper.bindResource(QUEST_ICONS);
                //drawRect(quest.getIconX(), quest.getIconY(), quest.getIconU(), quest.getIconV(), quest.getIconSize(), quest.getIconSize());
            }
        }
    }
    
    @Environment(EnvType.CLIENT)
    public void mouseClicked(GuiQuestBook gui, int x, int y) {
        Player player = gui.getPlayer();
        if (Quest.canQuestsBeEdited() && (gui.getCurrentMode() == EditMode.CREATE || gui.getCurrentMode() == EditMode.REP_BAR_CREATE)) {
            switch (gui.getCurrentMode()) {
                case CREATE:
                    if (x > 0) {
                        Quest newQuest = new Quest("Unnamed", "Unnamed quest", 0, 0, false);
                        newQuest.setGuiCenterX(x);
                        newQuest.setGuiCenterY(y);
                        newQuest.setQuestSet(this);
                        SaveHelper.add(EditType.QUEST_CREATE);
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
                                SaveHelper.add(EditType.QUEST_MOVE);
                                break;
                            case REQUIREMENT:
                                if (gui.modifyingQuest == quest) {
                                    if (Screen.hasShiftDown())
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
                                SaveHelper.add(EditType.QUEST_SIZE_CHANGE);
                                break;
                            case ITEM:
                                PickItemMenu.display(gui, player, quest.getIconStack(), PickItemMenu.Type.ITEM_FLUID,
                                        result -> {
                                            try {
                                                quest.setIconStack(result.get());
                                            } catch (Exception e) {
                                                System.out.println("Tell LordDusk that he found the issue.");
                                            }
                                            SaveHelper.add(EditType.ICON_CHANGE);
                                        });
                                break;
                            case DELETE:
                                Quest.removeQuest(quest);
                                SaveHelper.add(EditType.QUEST_REMOVE);
                                break;
                            case SWAP:
                                if (gui.modifyingQuestSet != null && gui.modifyingQuestSet != this) {
                                    quest.setQuestSet(gui.modifyingQuestSet);
                                    SaveHelper.add(EditType.QUEST_CHANGE_SET);
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
                                    if (Screen.hasShiftDown())
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
                                if (gui.isOpBook && Screen.hasShiftDown()) {
                                    OPBookHelper.reverseQuestCompletion(quest, player);
                                    break;
                                } // deliberate drop through
                            default:
                                GuiQuestBook.selectedQuest = quest;
                                quest.onOpen(gui, player);
                                break;
                        }
                    } else {
                        if (gui.isOpBook && Screen.hasShiftDown()) {
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
