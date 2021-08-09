package hardcorequesting.common.client.interfaces.edit;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.LargeButton;
import hardcorequesting.common.client.interfaces.TextBoxGroup;
import hardcorequesting.common.quests.task.QuestTaskLocation;
import hardcorequesting.common.util.Translator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

public class GuiEditMenuLocation extends GuiEditMenuExtended {
    
    private final Consumer<Result> resultConsumer;
    private QuestTaskLocation.Visibility visibility;
    private BlockPos.MutableBlockPos pos;
    private int radius;
    private String dimension;
    
    public static void display(GuiQuestBook gui, Player player, QuestTaskLocation.Visibility visibility, BlockPos initPos, int initRadius, String initDimension, Consumer<Result> resultConsumer) {
        gui.setEditMenu(new GuiEditMenuLocation(gui, player, visibility, initPos, initRadius, initDimension, resultConsumer));
    }
    
    private GuiEditMenuLocation(GuiQuestBook gui, Player player, QuestTaskLocation.Visibility visibility, BlockPos initPos, int initRadius, String initDimension, Consumer<Result> resultConsumer) {
        super(gui, player, true, 180, 30, 20, 30);
    
        this.resultConsumer = resultConsumer;
        this.visibility = visibility;
        this.pos = new BlockPos.MutableBlockPos(initPos.getX(), initPos.getY(), initPos.getZ());
        this.radius = initRadius;
        this.dimension = initDimension;
        
        textBoxes.add(new TextBoxNumberNegative(gui, 0, "hqm.locationMenu.xTarget") {
            @Override
            protected void setValue(int number) {
                pos.setX(number);
            }
            
            @Override
            protected int getValue() {
                return pos.getX();
            }
        });
        
        textBoxes.add(new TextBoxNumberNegative(gui, 1, "hqm.locationMenu.yTarget") {
            @Override
            protected void setValue(int number) {
                pos.setY(number);
            }
            
            @Override
            protected int getValue() {
                return pos.getY();
            }
        });
        
        textBoxes.add(new TextBoxNumberNegative(gui, 2, "hqm.locationMenu.zTarget") {
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
        textBoxes.add(locationBox = new TextBoxGroup.TextBox(gui, initDimension, BOX_X, BOX_Y + BOX_OFFSET * 3, true) {
            @Override
            public void textChanged(GuiBase gui) {
                super.textChanged(gui);
                dimension = getText();
            }
            
            @Override
            protected void draw(PoseStack matrices, GuiBase gui, boolean selected) {
                super.draw(matrices, gui, selected);
                
                gui.drawString(matrices, Translator.translatable("hqm.locationMenu.dim"), BOX_X, BOX_Y + BOX_OFFSET * 3 + TEXT_OFFSET, 0x404040);
            }
        });
        locationBox.recalculateCursor(gui);
        
        textBoxes.add(new TextBoxNumberNegative(gui, 4, "hqm.locationMenu.radius") {
            @Override
            protected int getValue() {
                return radius;
            }
            
            @Override
            protected void setValue(int number) {
                radius = number;
            }
            
            @Override
            protected void draw(PoseStack matrices, GuiBase gui, boolean selected) {
                super.draw(matrices, gui, selected);
                
                gui.drawString(matrices, gui.getLinesFromText(Translator.translatable("hqm.locationMenu.negRadius"), 0.7F, 130), BOX_X, BOX_Y + BOX_OFFSET * 5 + TEXT_OFFSET, 0.7F, 0x404040);
            }
        });
        
        
        buttons.add(new LargeButton("hqm.locationMenu.location", 100, 20) {
            @Override
            public boolean isEnabled(GuiBase gui, Player player) {
                return true;
            }
            
            @Override
            public boolean isVisible(GuiBase gui, Player player) {
                return true;
            }
            
            @Override
            public void onClick(GuiBase gui, Player player) {
                pos = new BlockPos.MutableBlockPos(player.getX(), player.getY(), player.getZ());
                dimension = player.level.dimension().location().toString();
                for (TextBoxGroup.TextBox textBox : textBoxes.getTextBoxes()) {
                    if (textBox instanceof TextBoxNumber)
                        textBox.setTextAndCursor(gui, String.valueOf(((TextBoxNumber) textBox).getValue()));
                    else
                        textBox.recalculateCursor(gui);
                }
            }
        });
    }
    
    @Override
    protected void onArrowClick(boolean left) {
        if (left) {
            visibility = QuestTaskLocation.Visibility.values()[(visibility.ordinal() + QuestTaskLocation.Visibility.values().length - 1) % QuestTaskLocation.Visibility.values().length];
        } else {
            visibility = QuestTaskLocation.Visibility.values()[(visibility.ordinal() + 1) % QuestTaskLocation.Visibility.values().length];
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
    
    @Override
    public void save(GuiBase gui) {
        resultConsumer.accept(new Result(visibility, pos.immutable(), radius, dimension));
    }
    
    private abstract class TextBoxNumberNegative extends TextBoxNumber {
        public TextBoxNumberNegative(GuiQuestBook gui, int id, String title) {
            super(gui, id, title);
        }
        
        @Override
        protected boolean isNegativeAllowed() {
            return true;
        }
    }
    
    public static class Result {
        private final QuestTaskLocation.Visibility visibility;
        private final BlockPos pos;
        private final int radius;
        private final String dimension;
    
        private Result(QuestTaskLocation.Visibility visibility, BlockPos pos, int radius, String dimension) {
            this.visibility = visibility;
            this.pos = pos;
            this.radius = radius;
            this.dimension = dimension;
        }
    
        public QuestTaskLocation.Visibility getVisibility() {
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
