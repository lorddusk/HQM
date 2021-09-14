package hardcorequesting.common.quests;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiColor;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.TextMenu;
import hardcorequesting.common.client.interfaces.widget.ScrollBar;
import hardcorequesting.common.config.HQMConfig;
import hardcorequesting.common.reputation.ReputationBar;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
                if (quest.isCompleted(player) && quest.hasReward(player.getUUID())) unclaimed++;
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
                            if (quest.hasReward(player.getUUID())) {
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
                        TextMenu.display(gui, gui.getPlayer().getUUID(), questSet.getName(), true,
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
                TextMenu.display(gui, gui.getPlayer().getUUID(), GuiQuestBook.selectedSet.getDescription(), false, GuiQuestBook.selectedSet::setDescription);
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
}
