package hardcorequesting.common.quests.task.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiColor;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.PickItemMenu;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.task.item.ItemRequirementTask;
import hardcorequesting.common.util.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Environment(EnvType.CLIENT)
public class ItemTaskGraphic implements TaskGraphic {
    
    private static final int MAX_X = 300;
    private static final int OFFSET = 20;
    private static final int SIZE = 18;
    private static final int TEXT_HEIGHT = 9;
    
    private int lastClicked;
    
    private final ItemRequirementTask task;
    
    public ItemTaskGraphic(ItemRequirementTask task) {
        this.task = task;
    }
    
    protected List<Positioned<ItemRequirementTask.Part>> getPositionedItems(List<ItemRequirementTask.Part> items) {
        List<Positioned<ItemRequirementTask.Part>> list = new ArrayList<>(items.size());
        int x = START_X;
        int y = START_Y;
        
        for (ItemRequirementTask.Part item : items) {
            list.add(new Positioned<>(x, y, item));
            
            x += OFFSET;
            if (x > MAX_X) {
                x = START_X;
                y += OFFSET;
            }
        }
        
        return list;
    }
    
    @Override
    public void draw(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY) {
        List<Positioned<ItemRequirementTask.Part>> items = getPositionedItems(task.parts.getShownElements());
    
        for (int i = 0; i < items.size(); i++) {
            Positioned<ItemRequirementTask.Part> pos = items.get(i);
            ItemRequirementTask.Part item = pos.getElement();
        
            if (item.hasItem) {
                gui.drawItemStack(item.getPermutatedItem(), pos.getX(), pos.getY(), mX, mY, false);
            } else if (item.fluid != null) {
                gui.drawFluid(item.fluid, matrices, pos.getX(), pos.getY(), mX, mY);
            }
        
            FormattedText str = Translator.plain((task.getProgress(player, i) * 100 / item.required) + "%");
            matrices.pushPose();
            matrices.translate(0, 0, 200);// magic z value to write over stack render
            float textSize = 0.8F;
            gui.drawStringWithShadow(matrices, str, (int) (pos.getX() + SIZE - gui.getStringWidth(str) * textSize), (int) (pos.getY() + SIZE - (item.hasItem && !item.getStack().isEmpty() && item.getStack().getCount() != 1 ? TEXT_HEIGHT : 0) - TEXT_HEIGHT * textSize + 2), textSize, task.getProgress(player, i) == item.required ? 0x308030 : 0xFFFFFF);
            matrices.popPose();
        }
    
        for (int i = 0; i < items.size(); i++) {
            Positioned<ItemRequirementTask.Part> pos = items.get(i);
            ItemRequirementTask.Part item = pos.getElement();
        
            if (gui.inBounds(pos.getX(), pos.getY(), SIZE, SIZE, mX, mY)) {
                GuiQuestBook.setSelectedStack(item.getStack());
                ItemStack stack = item.getStack();
                List<FormattedText> str = new ArrayList<>();
                if (item.fluid != null) {
                    List<Component> list = new ArrayList<>();
                    str.add(new TextComponent(item.fluid.getName().getString()));
                    if (Minecraft.getInstance().options.advancedItemTooltips) {
                        String entryId = Registry.FLUID.getKey(item.fluid.getFluid()).toString();
                        list.add(new TextComponent(entryId).withStyle(ChatFormatting.DARK_GRAY));
                    }
                    str.addAll(list);
                } else if (stack != null && !stack.isEmpty()) {
                    str.addAll(gui.getTooltipFromItem(stack));
                }
            
                str.add(FormattedText.composite(Translator.translatable("hqm.questBook.itemRequirementProgress"), Translator.plain(": " + task.getProgress(player, i) + "/" + item.required)));
                if (item.fluid == null && Quest.canQuestsBeEdited()) {
                    str.add(FormattedText.EMPTY);
                    str.add(Translator.text(item.getPrecision().getName(), GuiColor.GRAY));
                }
                if (gui.isOpBook && Screen.hasShiftDown()) {
                    if (task.getProgress(player, i) == item.required) {
                        str.addAll(Arrays.asList(FormattedText.EMPTY, FormattedText.EMPTY, Translator.translatable("hqm.questBook.resetTask", GuiColor.RED)));
                    } else {
                        str.addAll(Arrays.asList(FormattedText.EMPTY, FormattedText.EMPTY, Translator.translatable("hqm.questBook.completeTask", GuiColor.ORANGE)));
                    }
                }
                gui.renderTooltipL(matrices, str, mX + gui.getLeft(), mY + gui.getTop());
                break;
            }
        }
    }
    
    @Override
    public void onClick(GuiQuestBook gui, Player player, int mX, int mY, int b) {
        boolean isOpBookWithShiftKeyDown = gui.isOpBook && Screen.hasShiftDown();
        boolean doubleClick = false;
        if (Quest.canQuestsBeEdited() || isOpBookWithShiftKeyDown) {
            List<Positioned<ItemRequirementTask.Part>> items = getPositionedItems(task.parts.getShownElements());
            
            for (int i = 0; i < items.size(); i++) {
                Positioned<ItemRequirementTask.Part> pos = items.get(i);
                ItemRequirementTask.Part item = pos.getElement();
                
                if (gui.inBounds(pos.getX(), pos.getY(), SIZE, SIZE, mX, mY)) {
                    int lastDiff = player.tickCount - lastClicked;
                    if (lastDiff < 0) {
                        lastClicked = player.tickCount;
                    } else if (lastDiff < 6) {
                        doubleClick = true;
                    } else {
                        lastClicked = player.tickCount;
                    }
                    
                    if (isOpBookWithShiftKeyDown) {
                        OPBookHelper.reverseRequirementCompletion(task, i, player);
                    } else if (Quest.canQuestsBeEdited()) {
                        if (gui.getCurrentMode() == EditMode.ITEM || doubleClick) {
                            final int id = i;
                            if(task.mayUseFluids()) {
                                PickItemMenu.display(gui, player, item.hasItem ? Either.left(item.getStack()) : Either.right(item.fluid), PickItemMenu.Type.ITEM_FLUID, item.required, item.getPrecision(),
                                        result -> task.setItem(result.get(), result.getAmount(), result.getPrecision(), id));
                            } else {
                                PickItemMenu.display(gui, player, item.getStack(), PickItemMenu.Type.ITEM, item.required, item.getPrecision(),
                                        result -> task.setItem(Either.left(result.get()), result.getAmount(), result.getPrecision(), id));
                            }
                            
                        } else if (gui.getCurrentMode() == EditMode.DELETE && ((item.getStack() != null && !item.getStack().isEmpty()) || item.fluid != null)) {
                            task.parts.remove(i);
                            SaveHelper.add(EditType.TASK_ITEM_REMOVE);
                        }
                    }
                    break;
                }
            }
        } else {
            /* TODO REI
            if (Loader.isModLoaded("jei")) {
                for (ItemRequirement item : getEditFriendlyItems(this.items)) {
                    if (gui.inBounds(item.x, item.y, SIZE, SIZE, mX, mY)) {
                        JEIIntegration.showItemStack(item.getStack());
                        return;
                    }
                }
            }*/
        }
    }
}