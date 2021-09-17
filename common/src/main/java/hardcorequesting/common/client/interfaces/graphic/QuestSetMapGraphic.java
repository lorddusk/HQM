package hardcorequesting.common.client.interfaces.graphic;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.BookPage;
import hardcorequesting.common.client.EditButton;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.KeyboardHandler;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiColor;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.ResourceHelper;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenuParentCount;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenuRepeat;
import hardcorequesting.common.client.interfaces.edit.GuiEditMenuTrigger;
import hardcorequesting.common.client.interfaces.edit.PickItemMenu;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.QuestSet;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.reputation.ReputationBar;
import hardcorequesting.common.util.EditType;
import hardcorequesting.common.util.OPBookHelper;
import hardcorequesting.common.util.SaveHelper;
import hardcorequesting.common.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public class QuestSetMapGraphic extends Graphic {
    private final QuestSet set;
    private final BookPage.SetMapPage page;
    
    private final EditButton[] editButtons;
    
    public QuestSetMapGraphic(GuiQuestBook gui, QuestSet set, BookPage.SetMapPage page) {
        this.set = set;
        editButtons = EditButton.createButtons(gui::setCurrentMode, EditMode.NORMAL, EditMode.MOVE, EditMode.CREATE, EditMode.REQUIREMENT, EditMode.SIZE, EditMode.ITEM, EditMode.REPEATABLE, EditMode.TRIGGER, EditMode.REQUIRED_PARENTS, EditMode.QUEST_SELECTION, EditMode.QUEST_OPTION, EditMode.SWAP, EditMode.REP_BAR_CREATE, EditMode.REP_BAR_CHANGE, EditMode.DELETE);
        this.page = page;
    }
    
    @Override
    public void draw(PoseStack matrices, GuiQuestBook gui, int mX, int mY) {
        
        super.draw(matrices, gui, mX, mY);
        
        if (gui.isOpBook) {
            gui.drawString(matrices, gui.getLinesFromText(Translator.translatable("hqm.questBook.shiftSetReset"), 0.7F, 130), 184, 192, 0.7F, 0x707070);
        }
        
        Player player = gui.getPlayer();
        
        for (ReputationBar bar : set.getReputationBars()) {
            bar.draw(matrices, gui, mX, mY, player.getUUID());
        }
        
        HashMap<Quest, Boolean> isVisibleCache = new HashMap<>();
        HashMap<Quest, Boolean> isLinkFreeCache = new HashMap<>();
        
        drawConnectingLines(matrices, gui, player, isVisibleCache, isLinkFreeCache);
        
        gui.setBlitOffset(50);
        
        drawQuestIcons(matrices, gui, mX, mY, player, isVisibleCache, isLinkFreeCache);
    
        gui.drawEditButtons(matrices, mX, mY, editButtons);
    }
    
    @Override
    public void drawTooltip(PoseStack matrices, GuiQuestBook gui, int mX, int mY) {
        super.drawTooltip(matrices, gui, mX, mY);
        
        Player player = gui.getPlayer();
    
        HashMap<Quest, Boolean> isVisibleCache = new HashMap<>();
        HashMap<Quest, Boolean> isLinkFreeCache = new HashMap<>();
        
        for (Quest quest : set.getQuests().values()) {
            boolean editing = Quest.canQuestsBeEdited() && !Screen.hasControlDown();
            if ((editing || quest.isVisible(player, isVisibleCache, isLinkFreeCache)) && quest.isMouseInObject(mX, mY)) {
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
                    if (quest.hasReward(player.getUUID())) {
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
                
                
                    List<Quest> externalQuests = new ArrayList<>();
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
                    gui.renderTooltipL(matrices, Stream.of(txt.split("\n")).map(FormattedText::of).collect(Collectors.toList()), mX + gui.getLeft(), mY + gui.getTop());
                }
                break;
            }
        }
    
        gui.drawEditButtonTooltip(matrices, mX, mY, editButtons);
    }
    
    private void drawConnectingLines(PoseStack matrices, GuiQuestBook gui, Player player, HashMap<Quest, Boolean> isVisibleCache, HashMap<Quest, Boolean> isLinkFreeCache) {
        for (Quest child : set.getQuests().values()) {
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
            for (Quest child : set.getQuests().values()) {
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
    
    private void drawQuestIcons(PoseStack matrices, GuiQuestBook gui, int x, int y, Player player, HashMap<Quest, Boolean> isVisibleCache, HashMap<Quest, Boolean> isLinkFreeCache) {
        for (Quest quest : set.getQuests().values()) {
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
            }
        }
    }
    
    @Override
    public void onClick(GuiQuestBook gui, int mX, int mY, int b) {
        super.onClick(gui, mX, mY, b);
        
        Player player = gui.getPlayer();
        if (Quest.canQuestsBeEdited() && (gui.getCurrentMode() == EditMode.CREATE || gui.getCurrentMode() == EditMode.REP_BAR_CREATE)) {
            switch (gui.getCurrentMode()) {
                case CREATE:
                    if (mX > 0) {
                        Quest newQuest = new Quest("Unnamed", "Unnamed quest", 0, 0, false);
                        newQuest.setGuiCenterX(mX);
                        newQuest.setGuiCenterY(mY);
                        newQuest.setQuestSet(set);
                        SaveHelper.add(EditType.QUEST_CREATE);
                    }
                    break;
                case REP_BAR_CREATE:
                    gui.setEditMenu(new ReputationBar.EditGui(gui, player.getUUID(), mX, mY, set.getId()));
                    break;
                default:
                    break;
            }
        } else {
            HashMap<Quest, Boolean> isVisibleCache = new HashMap<>();
            HashMap<Quest, Boolean> isLinkFreeCache = new HashMap<>();
            for (Quest quest : set.getQuests().values()) {
                if ((Quest.canQuestsBeEdited() || quest.isVisible(player, isVisibleCache, isLinkFreeCache)) && quest.isMouseInObject(mX, mY)) {
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
                                PickItemMenu.display(gui, player.getUUID(), quest.getIconStack(), PickItemMenu.Type.ITEM_FLUID,
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
                                if (gui.modifyingQuestSet != null && gui.modifyingQuestSet != set) {
                                    quest.setQuestSet(gui.modifyingQuestSet);
                                    SaveHelper.add(EditType.QUEST_CHANGE_SET);
                                }
                                break;
                            case REPEATABLE:
                                gui.setEditMenu(new GuiEditMenuRepeat(gui, player.getUUID(), quest));
                                break;
                            case REQUIRED_PARENTS:
                                gui.setEditMenu(new GuiEditMenuParentCount(gui, player.getUUID(), quest));
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
                                gui.setEditMenu(new GuiEditMenuTrigger(gui, player.getUUID(), quest));
                                break;
                            case NORMAL:
                                if (gui.isOpBook && Screen.hasShiftDown()) {
                                    OPBookHelper.reverseQuestCompletion(quest, player.getUUID());
                                    break;
                                } // deliberate drop through
                            default:
                                gui.setPage(page.forQuest(quest));
                                break;
                        }
                    } else {
                        if (gui.isOpBook && Screen.hasShiftDown()) {
                            OPBookHelper.reverseQuestCompletion(quest, player.getUUID());
                        } else {
                            gui.setPage(page.forQuest(quest));
                        }
                    }
                    break;
                }
            }
        }
        
        if (Quest.canQuestsBeEdited())
            for (ReputationBar reputationBar : new ArrayList<>(set.getReputationBars()))
                reputationBar.mouseClicked(gui, mX, mY);
    
        gui.handleEditButtonClick(mX, mY, editButtons);
    }
    
    @Override
    public boolean keyPressed(int keyCode) {
        return KeyboardHandler.handleEditModeHotkey(keyCode, editButtons);
    }
}
