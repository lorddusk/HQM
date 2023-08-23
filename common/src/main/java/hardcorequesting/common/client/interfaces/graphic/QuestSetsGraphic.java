package hardcorequesting.common.client.interfaces.graphic;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.BookPage;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.TextMenu;
import hardcorequesting.common.client.interfaces.edit.WrappedTextMenu;
import hardcorequesting.common.client.interfaces.widget.ExtendedScrollBar;
import hardcorequesting.common.client.interfaces.widget.LargeButton;
import hardcorequesting.common.client.interfaces.widget.ScrollBar;
import hardcorequesting.common.config.HQMConfig;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestSet;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import hardcorequesting.common.util.WrappedText;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A graphic element for displaying the list of quest sets.
 * From here, the quest map of a quest set can be opened.
 */
@Environment(EnvType.CLIENT)
public class QuestSetsGraphic extends EditableGraphic {
    
    private static final int TEXT_SPACING = 20;
    private static final int LIST_X = 25;
    private static final int LIST_Y = 20;
    private static final int DESCRIPTION_X = 180;
    private static final int DESCRIPTION_Y = 20;
    private static final int VISIBLE_DESCRIPTION_LINES = 7;
    private static final int VISIBLE_SETS = 7;
    private static final int LINE_2_X = 10;
    private static final int LINE_2_Y = 12;
    private static final int INFO_Y = 100;
    
    private static int lastClicked = -1;
    private static QuestSet lastLastQuestSet = null;
    private static QuestSet selectedSet;
    
    private final BookPage.SetsPage page;
    private final ExtendedScrollBar<QuestSet> setScroll;
    private final ScrollBar descriptionScroll;
    
    {
        addClickable(new LargeButton(gui, "hqm.questBook.open", 245, 190) {
            @Override
            public boolean isVisible() {
                return selectedSet != null;
            }
        
            @Override
            public void onClick() {
                QuestSetsGraphic.this.gui.setPage(page.forSet(selectedSet));
            }
        });
    
        addClickable(new LargeButton(gui, "hqm.questBook.createSet", 185, 50) {
            @Override
            public boolean isVisible() {
                return Quest.canQuestsBeEdited() && QuestSetsGraphic.this.gui.getCurrentMode() == EditMode.CREATE;
            }
        
            @Override
            public void onClick() {
                int i = 0;
                for (QuestSet set : Quest.getQuestSets()) {
                    if (set.getName().startsWith("Unnamed set")) i++;
                }
                Quest.getQuestSets().add(new QuestSet("Unnamed set" + (i == 0 ? "" : i), WrappedText.create("No description")));
                SaveHelper.add(EditType.SET_CREATE);
            }
        });
    }
    
    public QuestSetsGraphic(BookPage.SetsPage page, GuiQuestBook gui) {
        super(gui, EditMode.NORMAL, EditMode.CREATE, EditMode.RENAME, EditMode.SWAP_SELECT, EditMode.DELETE);
        this.page = page;
        
        addScrollBar(descriptionScroll = new ScrollBar(gui, ScrollBar.Size.SMALL, 312, 18, DESCRIPTION_X) {
            @Override
            public boolean isVisible() {
                return selectedSet != null && selectedSet.getDescription(QuestSetsGraphic.this.gui).size() > VISIBLE_DESCRIPTION_LINES;
            }
        });
    
        addScrollBar(setScroll = new ExtendedScrollBar<>(gui, ScrollBar.Size.LONG, 160, 18, LIST_X,
                VISIBLE_SETS, Quest::getQuestSets));
    }
    
    public static void loginReset() {
        lastClicked = -1;
        lastLastQuestSet = null;
        selectedSet = null;
    }
    
    @Override
    public void draw(GuiGraphics graphics, int mX, int mY) {
        super.draw(graphics, mX, mY);
        
        Player player = gui.getPlayer();
        List<QuestSet> questSets = Quest.getQuestSets();
        
        HashMap<Quest, Boolean> isVisibleCache = new HashMap<>();
        HashMap<Quest, Boolean> isLinkFreeCache = new HashMap<>();
        
        int setY = LIST_Y;
        for (QuestSet questSet : setScroll.getVisibleEntries()) {
            
            String name = questSet.getName(questSets.indexOf(questSet));
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
            boolean selected = questSet == selectedSet;
            boolean inBounds = gui.inBounds(LIST_X, setY, gui.getStringWidth(name), GuiQuestBook.TEXT_HEIGHT, mX, mY);
            
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
            gui.drawString(graphics, Translator.plain(name), LIST_X, setY, color);
            
            FormattedText info;
            if (enabled) {
                if (completed)
                    info = Translator.translatable("hqm.questBook.allQuests");
                else
                    info = Translator.translatable("hqm.questBook.percentageQuests", ((completedCount * 100) / total));
            } else
                info = Translator.translatable("hqm.questBook.locked");
            gui.drawString(graphics, info, LIST_X + LINE_2_X, setY + LINE_2_Y, 0.7F, color);
            if (enabled && unclaimed != 0) {
                FormattedText toClaim = Translator.translatable("hqm.questBook.unclaimedRewards", Translator.quest(unclaimed)).withStyle(ChatFormatting.DARK_PURPLE);
                gui.drawString(graphics, toClaim, LIST_X + LINE_2_X, setY + LINE_2_Y + 8, 0.7F, 0xFFFFFFFF);
            }
            setY += GuiQuestBook.TEXT_HEIGHT + TEXT_SPACING;
        }
        
        if ((Quest.canQuestsBeEdited() && gui.getCurrentMode() == EditMode.CREATE)) {
            gui.drawString(graphics, gui.getLinesFromText(Translator.translatable("hqm.questBook.createNewSet"), 0.7F, 130), DESCRIPTION_X, DESCRIPTION_Y, 0.7F, 0x404040);
        } else {
            if (selectedSet != null) {
                List<FormattedText> description = descriptionScroll.getVisibleEntries(selectedSet.getDescription(gui), VISIBLE_DESCRIPTION_LINES);
                gui.drawString(graphics, description, DESCRIPTION_X, DESCRIPTION_Y, 0.7F, 0x404040);
            }
            
            drawQuestInfo(graphics, gui, selectedSet, DESCRIPTION_X, selectedSet == null ? DESCRIPTION_Y : INFO_Y, isVisibleCache, isLinkFreeCache);
        }
    }
    
    public static void drawQuestInfo(GuiGraphics graphics, GuiQuestBook gui, QuestSet set, int x, int y) {
        drawQuestInfo(graphics, gui, set, x, y, new HashMap<>(), new HashMap<>());
    }
    
    private static void drawQuestInfo(GuiGraphics graphics, GuiQuestBook gui, QuestSet set, int x, int y, HashMap<Quest, Boolean> isVisibleCache, HashMap<Quest, Boolean> isLinkFreeCache) {
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
        info.add(Translator.translatable("hqm.questBook.totalQuests", Translator.quest(total)).withStyle(ChatFormatting.DARK_GRAY));
        info.add(Translator.translatable("hqm.questBook.unlockedQuests", Translator.quest(enabled)).withStyle(ChatFormatting.DARK_AQUA));
        info.add(Translator.translatable("hqm.questBook.completedQuests", Translator.quest(completed)).withStyle(ChatFormatting.DARK_GREEN));
        info.add(Translator.translatable("hqm.questBook.availableQuests", Translator.quest(enabled - completed)).withStyle(ChatFormatting.BLUE));
        if (reward > 0) {
            info.add(Translator.translatable("hqm.questBook.unclaimedQuests", Translator.quest(reward)).withStyle(ChatFormatting.DARK_PURPLE));
        }
        if (Quest.canQuestsBeEdited() && !Screen.hasControlDown()) {
            info.add(Translator.translatable("hqm.questBook.inclInvisiQuests", Translator.quest(realTotal)).withStyle(ChatFormatting.GRAY));
        }
        gui.drawString(graphics, info, x, y, 0.7F, 0x404040);
    }
    
    @Override
    public void onClick(int mX, int mY, int b) {
        super.onClick(mX, mY, b);
        
        List<QuestSet> questSets = Quest.getQuestSets();
    
        int setY = LIST_Y;
        for (QuestSet questSet : setScroll.getVisibleEntries()) {
            
            if (gui.inBounds(LIST_X, setY, gui.getStringWidth(questSet.getName(questSets.indexOf(questSet))), GuiQuestBook.TEXT_HEIGHT, mX, mY)) {
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
                        TextMenu.display(gui, questSet.getName(), true,
                                result -> {
                                    if (!questSet.setName(result)) {
                                        gui.getPlayer().sendSystemMessage(Component.translatable("hqm.editMode.rename.invalid_set").setStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.RED)));
                                    }
                                });
                        break;
                    default:
                        int thisClicked = gui.getPlayer().tickCount - lastClicked;
                        
                        if (thisClicked < 0) {
                            lastClicked = -1;
                        }
                        
                        if (lastClicked != -1 && thisClicked < 6) {
                            if (selectedSet == null && lastLastQuestSet != null)
                                selectedSet = lastLastQuestSet;
                            if (selectedSet != null) {
                                gui.setPage(page.forSet(selectedSet));
                                lastLastQuestSet = null;
                            }
                        } else {
                            selectedSet = (questSet == selectedSet) ? null : questSet;
                            lastClicked = gui.getPlayer().tickCount;
                            lastLastQuestSet = questSet;
                        }
                        break;
                }
                break;
            }
            setY += GuiQuestBook.TEXT_HEIGHT + TEXT_SPACING;
        }
        
        if (Quest.canQuestsBeEdited() && gui.getCurrentMode() == EditMode.RENAME) {
            if (gui.inBounds(DESCRIPTION_X, DESCRIPTION_Y, 130, (int) (VISIBLE_DESCRIPTION_LINES * GuiQuestBook.TEXT_HEIGHT * 0.7F), mX, mY)) {
                WrappedTextMenu.display(gui, selectedSet.getRawDescription(), false, selectedSet::setDescription);
            }
        }
    }
}