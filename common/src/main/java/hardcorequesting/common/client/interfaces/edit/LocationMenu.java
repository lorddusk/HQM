package hardcorequesting.common.client.interfaces.edit;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.widget.ArrowSelectionHelper;
import hardcorequesting.common.client.interfaces.widget.LargeButton;
import hardcorequesting.common.client.interfaces.widget.NumberTextBox;
import hardcorequesting.common.client.interfaces.widget.TextBoxGroup;
import hardcorequesting.common.quests.task.icon.VisitLocationTask;
import hardcorequesting.common.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;
import java.util.function.Consumer;

public class LocationMenu extends GuiEditMenu {
    
    private final Consumer<Result> resultConsumer;
    private VisitLocationTask.Visibility visibility;
    private BlockPos.MutableBlockPos pos;
    private int radius;
    private String dimension;
    private final ArrowSelectionHelper selectionHelper;
    
    public static void display(GuiQuestBook gui, UUID playerId, VisitLocationTask.Visibility visibility, BlockPos initPos, int initRadius, String initDimension, Consumer<Result> resultConsumer) {
        gui.setEditMenu(new LocationMenu(gui, playerId, visibility, initPos, initRadius, initDimension, resultConsumer));
    }
    
    private LocationMenu(GuiQuestBook gui, UUID playerId, VisitLocationTask.Visibility visibilityIn, BlockPos initPos, int initRadius, String initDimension, Consumer<Result> resultConsumer) {
        super(gui, playerId, true);
    
        this.resultConsumer = resultConsumer;
        this.visibility = visibilityIn;
        this.pos = new BlockPos.MutableBlockPos(initPos.getX(), initPos.getY(), initPos.getZ());
        this.radius = initRadius;
        this.dimension = initDimension;
        
        textBoxes.add(new TextBoxNumberNegative(gui, 20, 30, "hqm.locationMenu.xTarget") {
            @Override
            protected void setValue(int number) {
                pos.setX(number);
            }
            
            @Override
            protected int getValue() {
                return pos.getX();
            }
        });
        
        textBoxes.add(new TextBoxNumberNegative(gui, 20, 30 + BOX_OFFSET, "hqm.locationMenu.yTarget") {
            @Override
            protected void setValue(int number) {
                pos.setY(number);
            }
            
            @Override
            protected int getValue() {
                return pos.getY();
            }
        });
        
        textBoxes.add(new TextBoxNumberNegative(gui, 20, 30 + 2 * BOX_OFFSET, "hqm.locationMenu.zTarget") {
            @Override
            protected void setValue(int number) {
                pos.setZ(number);
            }
            
            @Override
            protected int getValue() {
                return pos.getZ();
            }
        });
        
        TextBoxGroup.TextBox locationBox;
        textBoxes.add(locationBox = new TextBoxGroup.TextBox(gui, initDimension, 20, 30 + 3 * BOX_OFFSET, true) {
            @Override
            public void textChanged() {
                super.textChanged();
                dimension = getText();
            }
            
            @Override
            protected void draw(PoseStack matrices, boolean selected) {
                super.draw(matrices, selected);
                
                this.gui.drawString(matrices, Translator.translatable("hqm.locationMenu.dim"), x, y + NumberTextBox.TEXT_OFFSET, 0x404040);
            }
        });
        locationBox.recalculateCursor();
        
        textBoxes.add(new TextBoxNumberNegative(gui, 20, 30 + 4 * BOX_OFFSET, "hqm.locationMenu.radius") {
            @Override
            protected int getValue() {
                return radius;
            }
            
            @Override
            protected void setValue(int number) {
                radius = number;
            }
            
            @Override
            protected void draw(PoseStack matrices, boolean selected) {
                super.draw(matrices, selected);
    
                this.gui.drawString(matrices, this.gui.getLinesFromText(Translator.translatable("hqm.locationMenu.negRadius"), 0.7F, 130), x, y + BOX_OFFSET + TEXT_OFFSET, 0.7F, 0x404040);
            }
        });
        
        
        buttons.add(new LargeButton(gui, "hqm.locationMenu.location", 100, 20) {
            @Override
            public boolean isEnabled() {
                return true;
            }
            
            @Override
            public boolean isVisible() {
                return true;
            }
            
            @Override
            public void onClick() {
                Player player = Minecraft.getInstance().player;
                pos = new BlockPos.MutableBlockPos(player.getX(), player.getY(), player.getZ());
                dimension = player.level.dimension().location().toString();
                for (TextBoxGroup.TextBox textBox : textBoxes.getTextBoxes()) {
                    if (textBox instanceof NumberTextBox)
                        textBox.reloadText();
                    else
                        textBox.recalculateCursor();
                }
            }
        });
        
        selectionHelper = new ArrowSelectionHelper(gui, 180, 30) {
            @Override
            protected void onArrowClick(boolean left) {
                if (left) {
                    visibility = VisitLocationTask.Visibility.values()[(visibility.ordinal() + VisitLocationTask.Visibility.values().length - 1) % VisitLocationTask.Visibility.values().length];
                } else {
                    visibility = VisitLocationTask.Visibility.values()[(visibility.ordinal() + 1) % VisitLocationTask.Visibility.values().length];
                }
            }
    
            @Override
            protected String getArrowText() {
                return visibility.getName();
            }
    
            @Override
            protected String getArrowDescription() {
                return visibility.getDescription();
            }
        };
    }
    
    @Override
    public void draw(PoseStack matrices, int mX, int mY) {
        super.draw(matrices, mX, mY);
        
        selectionHelper.render(matrices, mX, mY);
    }
    
    @Override
    public void onClick(int mX, int mY, int b) {
        super.onClick(mX, mY, b);
        
        selectionHelper.onClick(mX, mY);
    }
    
    @Override
    public void onRelease(int mX, int mY) {
        super.onRelease(mX, mY);
        
        selectionHelper.onRelease();
    }
    
    @Override
    public void save() {
        resultConsumer.accept(new Result(visibility, pos.immutable(), radius, dimension));
    }
    
    private abstract class TextBoxNumberNegative extends NumberTextBox {
        public TextBoxNumberNegative(GuiQuestBook gui, int x, int y, String title) {
            super(gui, x, y, title);
        }
        
        @Override
        protected boolean isNegativeAllowed() {
            return true;
        }
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
