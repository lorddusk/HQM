package hardcorequesting.common.client.interfaces.edit;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.widget.ArrowSelectionHelper;
import hardcorequesting.common.client.interfaces.widget.LargeButton;
import hardcorequesting.common.client.interfaces.widget.NumberTextBox;
import hardcorequesting.common.client.interfaces.widget.TextBoxGroup;
import hardcorequesting.common.quests.task.icon.VisitLocationTask;
import hardcorequesting.common.util.HQMUtil;
import hardcorequesting.common.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

public class LocationMenu extends GuiEditMenu {
    
    private final Consumer<Result> resultConsumer;
    private VisitLocationTask.Visibility visibility;
    private final BlockPos.MutableBlockPos pos;
    private int radius;
    private String dimension;
    
    public static void display(GuiQuestBook gui, VisitLocationTask.Visibility visibility, BlockPos initPos, int initRadius, String initDimension, Consumer<Result> resultConsumer) {
        gui.setEditMenu(new LocationMenu(gui, visibility, initPos, initRadius, initDimension, resultConsumer));
    }
    
    private LocationMenu(GuiQuestBook gui, VisitLocationTask.Visibility visibilityIn, BlockPos initPos, int initRadius, String initDimension, Consumer<Result> resultConsumer) {
        super(gui, true);
    
        this.resultConsumer = resultConsumer;
        this.visibility = visibilityIn;
        this.pos = new BlockPos.MutableBlockPos(initPos.getX(), initPos.getY(), initPos.getZ());
        this.radius = initRadius;
        this.dimension = initDimension;
        
        addTextBox(new NumberTextBox(gui, 20, 30, Translator.translatable("hqm.locationMenu.xTarget"), true, pos::getX, pos::setX));
        
        addTextBox(new NumberTextBox(gui, 20, 30 + BOX_OFFSET, Translator.translatable("hqm.locationMenu.yTarget"), true, pos::getY, pos::setY));
        
        addTextBox(new NumberTextBox(gui, 20, 30 + 2 * BOX_OFFSET, Translator.translatable("hqm.locationMenu.zTarget"), true, pos::getZ, pos::setZ));
    
        addTextBox(new TextBoxGroup.TextBox(gui, initDimension, 20, 30 + 3 * BOX_OFFSET, true) {
            @Override
            public void textChanged() {
                super.textChanged();
                dimension = getText();
            }
            
            @Override
            protected void draw(PoseStack matrices, boolean selected, int mX, int mY) {
                super.draw(matrices, selected, mX, mY);
                
                this.gui.drawString(matrices, Translator.translatable("hqm.locationMenu.dim"), x, y + NumberTextBox.TEXT_OFFSET, 0x404040);
            }
        });
        
        addTextBox(new NumberTextBox(gui, 20, 30 + 4 * BOX_OFFSET, Translator.translatable("hqm.locationMenu.radius"), true, () -> radius, value -> radius = value) {
            @Override
            protected void draw(PoseStack matrices, boolean selected, int mX, int mY) {
                super.draw(matrices, selected, mX, mY);
    
                this.gui.drawString(matrices, this.gui.getLinesFromText(Translator.translatable("hqm.locationMenu.negRadius"), 0.7F, 130), x, y + BOX_OFFSET + TEXT_OFFSET, 0.7F, 0x404040);
            }
        });
        
        
        addClickable(new LargeButton(gui, "hqm.locationMenu.location", 100, 20) {
            @Override
            public void onClick() {
                Player player = Minecraft.getInstance().player;
                if (player != null) {
                    pos.set(player.getX(), player.getY(), player.getZ());
                    dimension = player.level.dimension().location().toString();
                    reloadTextBoxes();
                }
            }
        });
        
        addClickable(new ArrowSelectionHelper(gui, 180, 30) {
            @Override
            protected void onArrowClick(boolean left) {
                if (left) {
                    visibility = HQMUtil.cyclePrev(VisitLocationTask.Visibility.values(), visibility);
                } else {
                    visibility = HQMUtil.cycleNext(VisitLocationTask.Visibility.values(), visibility);
                }
            }
    
            @Override
            protected FormattedText getArrowText() {
                return visibility.getName();
            }
    
            @Override
            protected FormattedText getArrowDescription() {
                return visibility.getDescription();
            }
        });
    }
    
    @Override
    public void save() {
        resultConsumer.accept(new Result(visibility, pos.immutable(), radius, dimension));
    }
    
    public static class Result {
        private final VisitLocationTask.Visibility visibility;
        private final BlockPos pos;
        private final int radius;
        private final String dimension;
    
        private Result(VisitLocationTask.Visibility visibility, BlockPos pos, int radius, String dimension) {
            this.visibility = visibility;
            this.pos = pos;
            this.radius = radius;
            this.dimension = dimension;
        }
    
        public VisitLocationTask.Visibility getVisibility() {
            return visibility;
        }
    
        public BlockPos getPos() {
            return pos;
        }
    
        public int getRadius() {
            return radius;
        }
    
        public String getDimension() {
            return dimension;
        }
    }
}
