package hardcorequesting.common.client.interfaces.edit;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.common.client.interfaces.GuiBase;
import hardcorequesting.common.client.interfaces.GuiQuestBook;
import hardcorequesting.common.client.interfaces.LargeButton;
import hardcorequesting.common.client.interfaces.TextBoxGroup;
import hardcorequesting.common.quests.task.QuestTaskLocation;
import hardcorequesting.common.util.Translator;
import net.minecraft.world.entity.player.Player;

public class GuiEditMenuLocation extends GuiEditMenuExtended {
    
    private int id;
    private QuestTaskLocation task;
    private QuestTaskLocation.Location location;
    private Player player;
    
    
    public GuiEditMenuLocation(GuiQuestBook gui, QuestTaskLocation task, final QuestTaskLocation.Location location, int id, Player player) {
        super(gui, player, true, 180, 30, 20, 30);
        this.id = id;
        this.task = task;
        this.location = location;
        this.player = player;
        
        
        textBoxes.add(new TextBoxNumberNegative(gui, 0, "hqm.locationMenu.xTarget") {
            @Override
            protected void setValue(int number) {
                location.setX(number);
            }
            
            @Override
            protected int getValue() {
                return location.getX();
            }
        });
        
        textBoxes.add(new TextBoxNumberNegative(gui, 1, "hqm.locationMenu.yTarget") {
            @Override
            protected void setValue(int number) {
                location.setY(number);
            }
            
            @Override
            protected int getValue() {
                return location.getY();
            }
        });
        
        textBoxes.add(new TextBoxNumberNegative(gui, 2, "hqm.locationMenu.zTarget") {
            @Override
            protected void setValue(int number) {
                location.setZ(number);
            }
            
            @Override
            protected int getValue() {
                return location.getZ();
            }
        });
        
        TextBoxGroup.TextBox locationBox;
        textBoxes.add(locationBox = new TextBoxGroup.TextBox(gui, location.getDimension(), BOX_X, BOX_Y + BOX_OFFSET * 3, true) {
            @Override
            public void textChanged(GuiBase gui) {
                super.textChanged(gui);
                location.setDimension(getText());
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
                return location.getRadius();
            }
            
            @Override
            protected void setValue(int number) {
                location.setRadius(number);
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
                location.setX((int) player.getX());
                location.setY((int) player.getY());
                location.setZ((int) player.getZ());
                location.setDimension(player.level.dimension().location().toString());
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
            location.setVisibility(QuestTaskLocation.Visibility.values()[(location.getVisibility().ordinal() + QuestTaskLocation.Visibility.values().length - 1) % QuestTaskLocation.Visibility.values().length]);
        } else {
            location.setVisibility(QuestTaskLocation.Visibility.values()[(location.getVisibility().ordinal() + 1) % QuestTaskLocation.Visibility.values().length]);
        }
    }
    
    @Override
    protected String getArrowText() {
        return location.getVisibility().getName();
    }
    
    @Override
    protected String getArrowDescription() {
        return location.getVisibility().getDescription();
    }
    
    @Override
    public void save(GuiBase gui) {
        task.setLocation(id, location, player);
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
}
