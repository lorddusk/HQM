package hardcorequesting.common.client.interfaces.graphic;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.BookPage;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.ResourceHelper;
import hardcorequesting.common.client.interfaces.edit.*;
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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.*;

/**
 * A graphic element for displaying the quest map for a specific quest set.
 * From here, menus for individual quests can be opened.
 */
@Environment(EnvType.CLIENT)
public class QuestSetMapGraphic extends EditableGraphic {
    private final QuestSet set;
    private final BookPage.SetMapPage page;
    
    private Quest draggedQuest, selectedQuest;
    
    public QuestSetMapGraphic(GuiQuestBook gui, QuestSet set, BookPage.SetMapPage page) {
        super(gui, EditMode.NORMAL, EditMode.MOVE, EditMode.CREATE, EditMode.REQUIREMENT, EditMode.SIZE, EditMode.ITEM, EditMode.REPEATABLE, EditMode.TRIGGER, EditMode.REQUIRED_PARENTS, EditMode.QUEST_SELECTION, EditMode.QUEST_OPTION, EditMode.SWAP, EditMode.REP_BAR_CREATE, EditMode.REP_BAR_CHANGE, EditMode.DELETE);
        this.set = set;
        this.page = page;
    }
    
    @Override
    public void draw(PoseStack matrices, int mX, int mY) {
        
        super.draw(matrices, mX, mY);
        
        if (gui.isOpBook) {
            gui.drawString(matrices, gui.getLinesFromText(Translator.translatable("hqm.questBook.shiftSetReset"), 0.7F, 130), 184, 192, 0.7F, 0x707070);
        }
        
        Player player = gui.getPlayer();
        
        for (ReputationBar bar : set.getReputationBars()) {
            bar.draw(matrices, gui, mX, mY, player.getUUID());
        }
        
        HashMap<Quest, Boolean> isVisibleCache = new HashMap<>();
        HashMap<Quest, Boolean> isLinkFreeCache = new HashMap<>();
        
        drawConnectingLines(matrices, player, isVisibleCache, isLinkFreeCache);
        
        gui.setBlitOffset(50);
        
        drawQuestIcons(matrices, mX, mY, player, isVisibleCache, isLinkFreeCache);
    }
    
    @Override
    public void drawTooltip(PoseStack matrices, int mX, int mY) {
        super.drawTooltip(matrices, mX, mY);
        
        Player player = gui.getPlayer();
    
        HashMap<Quest, Boolean> isVisibleCache = new HashMap<>();
        HashMap<Quest, Boolean> isLinkFreeCache = new HashMap<>();
        
        for (Quest quest : set.getQuests().values()) {
            boolean editing = Quest.canQuestsBeEdited() && !Screen.hasControlDown();
            if ((editing || quest.isVisible(player, isVisibleCache, isLinkFreeCache)) && quest.isMouseInObject(mX, mY)) {
                boolean shouldDrawText = false;
                boolean enabled = quest.isEnabled(player, isVisibleCache, isLinkFreeCache);
                
                List<FormattedText> tooltip = new ArrayList<>();
                
                if (enabled || editing) {
                    tooltip.add(Translator.plain(quest.getName()));
                }
            
                if (!enabled) {
                    tooltip.add(Translator.translatable("hqm.questBook.lockedQuest").withStyle(ChatFormatting.DARK_GRAY));
                }
            
                if (!enabled || editing) {
                    
                    addParentTooltip(player.getUUID(), quest, editing, tooltip);
    
                    shouldDrawText |= addExternalParentTooltip(player.getUUID(), quest, editing, tooltip);
    
                    addRequirementTooltip(quest, editing, tooltip);
                }
            
                if (enabled || editing) {
                    if (quest.isCompleted(player)) {
                        tooltip.add(Translator.translatable("hqm.questBook.completed").withStyle(ChatFormatting.DARK_GREEN));
                    }
                    if (quest.hasReward(player.getUUID())) {
                        tooltip.add(Translator.translatable("hqm.questBook.unclaimedReward").withStyle(ChatFormatting.DARK_PURPLE));
                    }
    
                    tooltip.addAll(enabled
                            ? quest.getRepeatInfo().getMessage(quest, player)
                            : quest.getRepeatInfo().getShortMessage());
                
                    if (editing) {
                        addTaskTooltip(player.getUUID(), quest, tooltip);
    
                        quest.getTriggerType().getMessage(quest).ifPresent(tooltip::add);
    
                        addInvisibilityTooltip(player, isVisibleCache, isLinkFreeCache, quest, tooltip);
    
                        addOptionLinkTooltip(quest, tooltip);
    
                    }
    
                    addChildTooltip(quest, editing, tooltip);
                    shouldDrawText = true;
                }
            
                if (editing) {
                    tooltip.add(FormattedText.EMPTY);
                    tooltip.add(Translator.translatable("hqm.questBook.ctrlNonEditor").withStyle(ChatFormatting.DARK_GRAY));
                }
            
                if (gui.isOpBook && Screen.hasShiftDown()) {
                    if (quest.isCompleted(player)) {
                        tooltip.add(FormattedText.EMPTY);
                        tooltip.add(Translator.translatable("hqm.questBook.resetQuest").withStyle(ChatFormatting.DARK_RED));
                    } else {
                        tooltip.add(FormattedText.EMPTY);
                        tooltip.add(Translator.translatable("hqm.questBook.completeQuest").withStyle(ChatFormatting.GOLD));
                    }
                }
            
                if (shouldDrawText && gui.getCurrentMode() != EditMode.MOVE) {
                    gui.renderTooltipL(matrices, tooltip, mX + gui.getLeft(), mY + gui.getTop());
                }
                break;
            }
        }
    }
    
    private int getParentCount(Quest quest) {
        return quest.getRequirements().size();
    }
    
    private int getCompletedParents(Quest quest, UUID playerId) {
        int completed = 0;
        for (Quest parent : quest.getRequirements()) {
            if (parent.isCompleted(playerId))
                completed++;
        }
        return completed;
    }
    
    private List<Quest> getExternalQuests(Quest quest) {
        List<Quest> externalQuests = new ArrayList<>();
        for (Quest parent : quest.getRequirements()) {
            if (!parent.hasSameSetAs(quest)) {
                externalQuests.add(parent);
            }
        }
        return externalQuests;
    }
    
    private int getCompletedExternal(Quest quest, UUID playerId) {
        int completed = 0;
        for (Quest parent : quest.getRequirements()) {
            if (!parent.hasSameSetAs(quest) && parent.isCompleted(playerId)) {
                completed++;
            }
        }
        return completed;
    }
    
    private void addParentTooltip(UUID playerId, Quest quest, boolean editing, List<FormattedText> tooltip) {
        int totalParentCount = getParentCount(quest);
        if (editing && totalParentCount > 0) {
            boolean holdingR = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_R);
            
            int totalCompletedCount = getCompletedParents(quest, playerId);
            tooltip.add(Translator.translatable("hqm.questBook.parentCount", (totalParentCount - totalCompletedCount), Translator.quest(totalParentCount))
                    .append(" ").append(holdingText(holdingR, "R")).withStyle(ChatFormatting.DARK_GRAY));
            
            if (holdingR) {
                for (Quest parent : quest.getRequirements()) {
                    MutableComponent component = Translator.text(parent.getName()).withStyle(ChatFormatting.DARK_GRAY);
                    tooltip.add(component);
                    if (parent.isCompleted(playerId)) {
                        MutableComponent completedComponent = Translator.box(Translator.translatable("hqm.questBook.completed"))
                                .withStyle(ChatFormatting.WHITE);
                        component.append(" ").append(completedComponent);
                    }
                }
            }
        }
    }
    
    private boolean addExternalParentTooltip(UUID playerId, Quest quest, boolean editing, List<FormattedText> tooltip) {
        List<Quest> externalQuests = getExternalQuests(quest);
        int externalParents = externalQuests.size();
        int completedExternal = getCompletedExternal(quest, playerId);
        int allowedUncompleted = quest.getUseModifiedParentRequirement() ? Math.max(0, quest.getRequirements().size() - quest.getParentRequirementCount()) : 0;
        if (externalParents - completedExternal > allowedUncompleted || (editing && externalParents > 0)) {
            int parents = getParentCount(quest);
            int completedParents = getCompletedParents(quest,playerId);
            MutableComponent component = Translator.translatable("hqm.questBook.parentCountElsewhere", (parents - completedParents), Translator.quest(parents)).withStyle(ChatFormatting.RED);
            tooltip.add(component);
            if (editing) {
                boolean holdingE = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_E);
                component.append(" ").append(holdingText(holdingE, "E"));
                if (holdingE) {
                    for (Quest parent : externalQuests) {
                        MutableComponent questComponent = Translator.text(parent.getName() + " (" + parent.getQuestSet().getName() + ")").withStyle(ChatFormatting.RED);
                        tooltip.add(questComponent);
                        if (parent.isCompleted(playerId)) {
                            MutableComponent completedComponent = Translator.box(Translator.translatable("hqm.questBook.completed")).withStyle(ChatFormatting.WHITE);
                            questComponent.append(" ").append(completedComponent);
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }
    
    private void addRequirementTooltip(Quest quest, boolean editing, List<FormattedText> tooltip) {
        if (editing && quest.getUseModifiedParentRequirement()) {
            MutableComponent component;
            int amount = quest.getParentRequirementCount();
            int parentCount = getParentCount(quest);
            if (amount < parentCount) {
                component = Translator.translatable("hqm.questBook.reqOnly", Translator.quest(amount));
            } else if (amount > parentCount) {
                component = Translator.translatable("hqm.questBook.reqMore", Translator.quest(amount));
            } else {
                component = Translator.translatable("hqm.questBook.reqAll", Translator.quest(amount));
            }
            tooltip.add(component.withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }
    
    private void addTaskTooltip(UUID playerId, Quest quest, List<FormattedText> tooltip) {
        int totalTasks = quest.getTasks().size();
        int completedTasks = 0;
        for (QuestTask<?> task : quest.getTasks()) {
            if (task.isCompleted(playerId)) {
                completedTasks++;
            }
        }
        
        if (totalTasks == 0) {
            tooltip.add(Translator.translatable("hqm.questBook.noTasks").withStyle(ChatFormatting.DARK_RED));
        } else {
            boolean holdingT = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_T);
            
            tooltip.add(Translator.translatable("hqm.questBook.completedTasks", completedTasks, totalTasks)
                    .append(" ").append(holdingText(holdingT, "T")).withStyle(ChatFormatting.DARK_AQUA));
            
            if (holdingT) {
                for (QuestTask<?> task : quest.getTasks()) {
                    MutableComponent component = task.getDescription().withStyle(ChatFormatting.DARK_AQUA);
                    tooltip.add(component);
                    if (task.isCompleted(playerId)) {
                        component.append(" ").append(Translator.box(Translator.translatable("hqm.questBook.completed")).withStyle(ChatFormatting.WHITE));
                    }
                }
            }
        }
    }
    
    private void addInvisibilityTooltip(Player player, HashMap<Quest, Boolean> isVisibleCache, HashMap<Quest, Boolean> isLinkFreeCache, Quest quest, List<FormattedText> tooltip) {
        if (!quest.isVisible(player, isVisibleCache, isLinkFreeCache)) {
            Optional<MutableComponent> invisibilityMessage;
            if (quest.isLinkFree(player, isLinkFreeCache)) {
                boolean parentInvisible = false;
                for (Quest parent : quest.getRequirements()) {
                    if (!parent.isVisible(player, isVisibleCache, isLinkFreeCache)) {
                        parentInvisible = true;
                        break;
                    }
                }
    
    
                switch (quest.getTriggerType()) {
                    case ANTI_TRIGGER -> invisibilityMessage = Optional.of(Translator.translatable("hqm.questBook.invisLocked"));
                    case QUEST_TRIGGER -> {
                        invisibilityMessage = Optional.of(Translator.translatable("hqm.questBook.invisPerm"));
                        parentInvisible = false;
                    }
                    case TASK_TRIGGER -> invisibilityMessage = Optional.of(Translator.plural("hqm.questBook.invisCount", quest.getTriggerTasks()));
                    default -> invisibilityMessage = Optional.empty();
                }
                
                if (parentInvisible) {
                    MutableComponent parentText = Translator.translatable("hqm.questBook.invisInherit");
                    
                    if (invisibilityMessage.isPresent())
                        parentText = Translator.translatable("hqm.questBook.and", parentText, invisibilityMessage.get());
                    
                    invisibilityMessage = Optional.of(parentText);
                }
                
            } else {
                invisibilityMessage = Optional.of(Translator.translatable("hqm.questBook.invisOption"));
            }
            
            invisibilityMessage.ifPresent(component -> tooltip.add(component.withStyle(ChatFormatting.BLUE)));
        }
    }
    
    private void addOptionLinkTooltip(Quest quest, List<FormattedText> tooltip) {
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
            boolean holdingO = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_O);
            
            tooltip.add(Translator.translatable("hqm.questBook.optionLinks", Translator.quest(optionLinks))
                    .append(" ").append(holdingText(holdingO, "O")).withStyle(ChatFormatting.DARK_BLUE));
            
            if (holdingO) {
                for (UUID id : ids) {
                    Quest option = Quest.getQuest(id);
                    MutableComponent component = Translator.text(option.getName()).withStyle(ChatFormatting.DARK_BLUE);
                    tooltip.add(component);
                    if (!option.hasSameSetAs(quest)) {
                        component.append(" (" + option.getQuestSet().getName() + ")");
                    }
                }
            }
        }
    }
    
    private void addChildTooltip(Quest quest, boolean editing, List<FormattedText> tooltip) {
        List<Quest> externalQuests = new ArrayList<>();
        int childCount = 0;
        for (Quest child : quest.getReversedRequirement()) {
            if (!quest.hasSameSetAs(child)) {
                childCount++;
                externalQuests.add(child);
            }
        }
        
        if (childCount > 0) {
            MutableComponent component = Translator.translatable("hqm.questBook.childUnlocks", Translator.quest(childCount)).withStyle(ChatFormatting.RED);
            tooltip.add(component);
            if (editing) {
                boolean holdingU = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_U);
                component.append(" ").append(holdingText(holdingU, "U"));
                
                if (holdingU) {
                    for (Quest child : externalQuests) {
                        tooltip.add(Translator.text(child.getName() + " (" + child.getQuestSet().getName() + ")").withStyle(ChatFormatting.RED));
                    }
                }
            }
        }
    }
    
    private MutableComponent holdingText(boolean holding, String letter) {
        return Translator.box(Translator.translatable("hqm.questBook." + (holding ? "holding" : "hold"), letter));
    }
    
    private void drawConnectingLines(PoseStack matrices, Player player, HashMap<Quest, Boolean> isVisibleCache, HashMap<Quest, Boolean> isLinkFreeCache) {
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
    
    private void drawQuestIcons(PoseStack matrices, int x, int y, Player player, HashMap<Quest, Boolean> isVisibleCache, HashMap<Quest, Boolean> isLinkFreeCache) {
        for (Quest quest : set.getQuests().values()) {
            if ((Quest.canQuestsBeEdited() || quest.isVisible(player, isVisibleCache, isLinkFreeCache))) {
                
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                
                int color;
                if (quest == draggedQuest || quest == selectedQuest) color = 0xffbbffbb;
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
    public void onClick(int mX, int mY, int b) {
        super.onClick(mX, mY, b);
        
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
                    PickReputationMenu.display(gui, reputation -> {
                        set.addRepBar(new ReputationBar(reputation, mX, mY));
                        SaveHelper.add(EditType.REPUTATION_BAR_ADD);
                    });
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
                                draggedQuest = quest;
                                SaveHelper.add(EditType.QUEST_MOVE);
                                break;
                            case REQUIREMENT:
                                if (selectedQuest == quest) {
                                    if (Screen.hasShiftDown())
                                        selectedQuest.clearRequirements();
                                    selectedQuest = null;
                                } else if (selectedQuest == null) {
                                    selectedQuest = quest;
                                } else {
                                    selectedQuest.addRequirement(quest.getQuestId());
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
                                if (selectedQuest == quest) {
                                    if (Screen.hasShiftDown())
                                        selectedQuest.clearOptionLinks();
                                    selectedQuest = null;
                                } else if (selectedQuest == null) {
                                    selectedQuest = quest;
                                } else {
                                    selectedQuest.addOptionLink(quest.getQuestId());
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
                reputationBar.mouseClicked(gui, set, mX, mY);
    }
    
    @Override
    public void onRelease(int mX, int mY, int b) {
        super.onRelease(mX, mY, b);
        draggedQuest = null;
    }
    
    @Override
    public void onDrag(int mX, int mY, int b) {
        super.onDrag(mX, mY, b);
    
        if (draggedQuest != null && Quest.canQuestsBeEdited() && this.gui.getCurrentMode() == EditMode.MOVE) {
            draggedQuest.setGuiCenterX(mX);
            draggedQuest.setGuiCenterY(mY);
        }
    }
    
    @Override
    protected void setEditMode(EditMode mode) {
        draggedQuest = null;
        selectedQuest = null;
        super.setEditMode(mode);
    }
}
