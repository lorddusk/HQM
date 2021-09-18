package hardcorequesting.common.client.interfaces.graphic;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.BookPage;
import hardcorequesting.common.client.EditButton;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.KeyboardHandler;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiColor;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.TextMenu;
import hardcorequesting.common.client.interfaces.widget.LargeButton;
import hardcorequesting.common.client.interfaces.widget.ScrollBar;
import hardcorequesting.common.config.HQMConfig;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestSet;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Environment(EnvType.CLIENT)
public class QuestSetsGraphic extends Graphic {
    
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
    private final GuiQuestBook gui;
    private final ScrollBar setScroll;
    private final ScrollBar descriptionScroll;
    private final EditButton[] editButtons;
    
    {
        addScrollBar(descriptionScroll = new ScrollBar(312, 18, 64, 249, 102, DESCRIPTION_X) {
            @Override
            public boolean isVisible(GuiBase gui) {
                return selectedSet != null && selectedSet.getDescription(gui).size() > VISIBLE_DESCRIPTION_LINES;
            }
        });
    
        addScrollBar(setScroll = new ScrollBar(160, 18, 186, 171, 69, LIST_X) {
            @Override
            public boolean isVisible(GuiBase gui) {
                return Quest.getQuestSets().size() > VISIBLE_SETS;
            }
        });
    
        addButton(new LargeButton("hqm.questBook.open", 245, 190) {
            @Override
            public boolean isEnabled(GuiBase gui) {
                return true;
            }
        
            @Override
            public boolean isVisible(GuiBase gui) {
                return selectedSet != null;
            }
        
            @Override
            public void onClick(GuiBase gui) {
                QuestSetsGraphic.this.gui.setPage(page.forSet(selectedSet));
            }
        });
    
        addButton(new LargeButton("hqm.questBook.createSet", 185, 50) {
            @Override
            public boolean isEnabled(GuiBase gui) {
                return true;
            }
        
            @Override
            public boolean isVisible(GuiBase gui) {
                return Quest.canQuestsBeEdited() && QuestSetsGraphic.this.gui.getCurrentMode() == EditMode.CREATE;
            }
        
            @Override
            public void onClick(GuiBase gui) {
                int i = 0;
                for (QuestSet set : Quest.getQuestSets()) {
                    if (set.getName().startsWith("Unnamed set")) i++;
                }
                Quest.getQuestSets().add(new QuestSet("Unnamed set" + (i == 0 ? "" : i), "No description"));
                SaveHelper.add(EditType.SET_CREATE);
            }
        });
    }
    
    public QuestSetsGraphic(BookPage.SetsPage page, GuiQuestBook gui) {
        this.page = page;
        this.gui = gui;
        editButtons = EditButton.createButtons(gui::setCurrentMode, EditMode.NORMAL, EditMode.CREATE, EditMode.RENAME, EditMode.SWAP_SELECT, EditMode.DELETE);
    }
    
    public static void loginReset() {
        lastClicked = -1;
        lastLastQuestSet = null;
        selectedSet = null;
    }
    
    @Override
    public void draw(PoseStack matrices, GuiQuestBook gui, int mX, int mY) {
        super.draw(matrices, gui, mX, mY);
        
        Player player = gui.getPlayer();
        List<QuestSet> questSets = Quest.getQuestSets();
        int start = setScroll.isVisible(gui) ? Math.round((Quest.getQuestSets().size() - VISIBLE_SETS) * setScroll.getScroll()) : 0;
        
        HashMap<Quest, Boolean> isVisibleCache = new HashMap<>();
        HashMap<Quest, Boolean> isLinkFreeCache = new HashMap<>();
        for (int i = start; i < Math.min(start + VISIBLE_SETS, questSets.size()); i++) {
            QuestSet questSet = questSets.get(i);
            
            int setY = LIST_Y + (i - start) * (GuiQuestBook.TEXT_HEIGHT + GuiQuestBook.TEXT_SPACING);
            
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
            boolean inBounds = gui.inBounds(LIST_X, setY, gui.getStringWidth(questSet.getName(i)), GuiQuestBook.TEXT_HEIGHT, mX, mY);
            
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
            gui.drawString(matrices, Translator.plain(questSet.getName(i)), LIST_X, setY, color);
            
            FormattedText info;
            if (enabled) {
                if (completed)
                    info = Translator.translatable("hqm.questBook.allQuests");
                else
                    info = Translator.translatable("hqm.questBook.percentageQuests", ((completedCount * 100) / total));
            } else
                info = Translator.translatable("hqm.questBook.locked");
            gui.drawString(matrices, info, LIST_X + LINE_2_X, setY + LINE_2_Y, 0.7F, color);
            if (enabled && unclaimed != 0) {
                FormattedText toClaim = Translator.pluralTranslated(unclaimed != 1, "hqm.questBook.unclaimedRewards", GuiColor.PURPLE, unclaimed);
                gui.drawString(matrices, toClaim, LIST_X + LINE_2_X, setY + LINE_2_Y + 8, 0.7F, 0xFFFFFFFF);
            }
        }
        
        if ((Quest.canQuestsBeEdited() && gui.getCurrentMode() == EditMode.CREATE)) {
            gui.drawString(matrices, gui.getLinesFromText(Translator.translatable("hqm.questBook.createNewSet"), 0.7F, 130), DESCRIPTION_X, DESCRIPTION_Y, 0.7F, 0x404040);
        } else {
            if (selectedSet != null) {
                int startLine = descriptionScroll.isVisible(gui) ? Math.round((selectedSet.getDescription(gui).size() - VISIBLE_DESCRIPTION_LINES) * descriptionScroll.getScroll()) : 0;
                gui.drawString(matrices, selectedSet.getDescription(gui), startLine, VISIBLE_DESCRIPTION_LINES, DESCRIPTION_X, DESCRIPTION_Y, 0.7F, 0x404040);
            }
            
            drawQuestInfo(matrices, gui, selectedSet, DESCRIPTION_X, selectedSet == null ? DESCRIPTION_Y : INFO_Y, isVisibleCache, isLinkFreeCache);
        }
    
        gui.drawEditButtons(matrices, mX, mY, editButtons);
    }
    
    @Override
    public void drawTooltip(PoseStack matrices, GuiQuestBook gui, int mX, int mY) {
        super.drawTooltip(matrices, gui, mX, mY);
    
        gui.drawEditButtonTooltip(matrices, mX, mY, editButtons);
    }
    
    public static void drawQuestInfo(PoseStack matrices, GuiQuestBook gui, QuestSet set, int x, int y) {
        drawQuestInfo(matrices, gui, set, x, y, new HashMap<>(), new HashMap<>());
    }
    
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
    
    @Override
    public void onClick(GuiQuestBook gui, int mX, int mY, int b) {
        super.onClick(gui, mX, mY, b);
        
        List<QuestSet> questSets = Quest.getQuestSets();
        int start = setScroll.isVisible(gui) ? Math.round((Quest.getQuestSets().size() - VISIBLE_SETS) * setScroll.getScroll()) : 0;
        
        for (int i = start; i < Math.min(start + VISIBLE_SETS, questSets.size()); i++) {
            QuestSet questSet = questSets.get(i);
            
            int setY = LIST_Y + (i - start) * (GuiQuestBook.TEXT_HEIGHT + GuiQuestBook.TEXT_SPACING);
            if (gui.inBounds(LIST_X, setY, gui.getStringWidth(questSet.getName(i)), GuiQuestBook.TEXT_HEIGHT, mX, mY)) {
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
        }
        
        
        if (Quest.canQuestsBeEdited() && gui.getCurrentMode() == EditMode.RENAME) {
            if (gui.inBounds(DESCRIPTION_X, DESCRIPTION_Y, 130, (int) (VISIBLE_DESCRIPTION_LINES * GuiQuestBook.TEXT_HEIGHT * 0.7F), mX, mY)) {
                TextMenu.display(gui, gui.getPlayer().getUUID(), selectedSet.getDescription(), false, selectedSet::setDescription);
            }
        }
        
        gui.handleEditButtonClick(mX, mY, editButtons);
    }
    
    @Override
    public boolean keyPressed(GuiQuestBook gui, int keyCode) {
        return KeyboardHandler.handleEditModeHotkey(keyCode, editButtons)
                || super.keyPressed(gui, keyCode);
    }
}