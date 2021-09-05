package hardcorequesting.common.quests.task.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import hardcorequesting.common.client.EditMode;
import hardcorequesting.common.client.interfaces.GuiColor;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.edit.PickItemMenu;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.quests.task.PartList;
import hardcorequesting.common.quests.task.item.ItemRequirementTask;
import hardcorequesting.common.util.OPBookHelper;
import hardcorequesting.common.util.Positioned;
import hardcorequesting.common.util.Translator;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Environment(EnvType.CLIENT)
public class ItemTaskGraphic extends ListTaskGraphic<ItemRequirementTask.Part> {
    
    private static final int MAX_X = 300;
    private static final int OFFSET = 20;
    private static final int SIZE = 18;
    private static final int TEXT_HEIGHT = 9;
    
    private int lastClicked;
    
    private final ItemRequirementTask task;
    
    public ItemTaskGraphic(ItemRequirementTask task, PartList<ItemRequirementTask.Part> parts) {
        super(parts);
        this.task = task;
    }
    
    @Override
    protected List<Positioned<ItemRequirementTask.Part>> positionParts(List<ItemRequirementTask.Part> parts) {
        List<Positioned<ItemRequirementTask.Part>> list = new ArrayList<>(parts.size());
        int x = START_X;
        int y = START_Y;
        
        for (ItemRequirementTask.Part item : parts) {
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
    protected void drawPart(PoseStack matrices, GuiQuestBook gui, Player player, ItemRequirementTask.Part part, int id, int x, int y, int mX, int mY) {
        part.stack.ifLeft(itemStack -> gui.drawItemStack(matrices, part.getPermutatedItem(), x, y, mX, mY, false))
                .ifRight(fluidStack -> gui.drawFluid(fluidStack, matrices, x, y, mX, mY));
    
        FormattedText progressText = Translator.plain((task.getProgress(player, id) * 100 / part.required) + "%");
        matrices.pushPose();
        matrices.translate(0, 0, 200);// magic z value to write over stack render
        float textSize = 0.8F;
        boolean hasCountLine = part.stack.left().map(itemStack -> itemStack.getCount() > 1).orElse(false);
        gui.drawStringWithShadow(matrices, progressText, (int) (x + SIZE - gui.getStringWidth(progressText) * textSize), (int) (y + SIZE - (hasCountLine ? TEXT_HEIGHT : 0) - TEXT_HEIGHT * textSize + 2), textSize, task.getProgress(player, id) == part.required ? 0x308030 : 0xFFFFFF);
        matrices.popPose();
    }
    
    @Override
    protected List<FormattedText> getPartTooltip(GuiQuestBook gui, Player player, ItemRequirementTask.Part part, int id, int x, int y, int mX, int mY) {
        if (gui.inBounds(x, y, SIZE, SIZE, mX, mY)) {
            GuiQuestBook.setSelectedStack(part.getStack());
            List<FormattedText> str = new ArrayList<>();
            part.stack.ifRight(fluidStack -> {
                List<Component> list = new ArrayList<>();
                str.add(new TextComponent(fluidStack.getName().getString()));
                if (Minecraft.getInstance().options.advancedItemTooltips) {
                    String entryId = Registry.FLUID.getKey(fluidStack.getFluid()).toString();
                    list.add(new TextComponent(entryId).withStyle(ChatFormatting.DARK_GRAY));
                }
                str.addAll(list);
            }).ifLeft(itemStack -> str.addAll(gui.getTooltipFromItem(itemStack)));
        
            str.add(FormattedText.composite(Translator.translatable("hqm.questBook.itemRequirementProgress"), Translator.plain(": " + task.getProgress(player, id) + "/" + part.required)));
            if (part.hasItem() && Quest.canQuestsBeEdited()) {
                str.add(FormattedText.EMPTY);
                str.add(Translator.text(part.getPrecision().getName(), GuiColor.GRAY));
            }
            if (gui.isOpBook && Screen.hasShiftDown()) {
                if (task.getProgress(player, id) == part.required) {
                    str.addAll(Arrays.asList(FormattedText.EMPTY, FormattedText.EMPTY, Translator.translatable("hqm.questBook.resetTask", GuiColor.RED)));
                } else {
                    str.addAll(Arrays.asList(FormattedText.EMPTY, FormattedText.EMPTY, Translator.translatable("hqm.questBook.completeTask", GuiColor.ORANGE)));
                }
            }
            return str;
        }
        return null;
    }
    
    @Override
    protected boolean isInPartBounds(GuiQuestBook gui, int mX, int mY, Positioned<ItemRequirementTask.Part> pos) {
        return gui.inBounds(pos.getX(), pos.getY(), SIZE, SIZE, mX, mY);
    }
    
    @Override
    public void onClick(GuiQuestBook gui, Player player, int mX, int mY, int b) {
        if (gui.isOpBook && Screen.hasShiftDown()) {
            int id = getClickedPart(gui, mX, mY);
            if (id >= 0) {
                OPBookHelper.reverseRequirementCompletion(task, id, player);
            }
        } else if (Quest.canQuestsBeEdited()) {
            super.onClick(gui, player, mX, mY, b);
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
    
    @Override
    protected boolean handlePartClick(GuiQuestBook gui, Player player, EditMode mode, ItemRequirementTask.Part part, int id) {
        boolean doubleClick = false;
        int lastDiff = player.tickCount - lastClicked;
        if (lastDiff < 0) {
            lastClicked = player.tickCount;
        } else if (lastDiff < 6) {
            doubleClick = true;
        } else {
            lastClicked = player.tickCount;
        }
    
        if (gui.getCurrentMode() == EditMode.ITEM || doubleClick) {
            if (task.mayUseFluids()) {
                PickItemMenu.display(gui, player, part.stack, PickItemMenu.Type.ITEM_FLUID, part.required, part.getPrecision(),
                        result -> task.setItem(result.get(), result.getAmount(), result.getPrecision(), id));
            } else {
                PickItemMenu.display(gui, player, part.getStack(), PickItemMenu.Type.ITEM, part.required, part.getPrecision(),
                        result -> task.setItem(Either.left(result.get()), result.getAmount(), result.getPrecision(), id));
            }
            return true;
        } else {
            return super.handlePartClick(gui, player, mode, part, id);
        }
    }
}