package hardcorequesting.common.client.interfaces.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class TextBoxGroup implements Drawable, Clickable {
    
    private TextBox selectedTextBox;
    private final List<TextBox> textBoxes = new ArrayList<>();
    
    public void add(TextBox textBox) {
        textBoxes.add(textBox);
    }
    
    public List<TextBox> getTextBoxes() {
        return textBoxes;
    }
    
    @Override
    public void render(PoseStack matrices, int mX, int mY) {
        for (TextBox textBox : textBoxes) {
            if (textBox.isVisible()) {
                textBox.draw(matrices, selectedTextBox == textBox, mX, mY);
            }
        }
    }
    
    @Override
    public boolean onClick(int mX, int mY) {
        for (TextBox textBox : textBoxes) {
            if (textBox.isVisible() && textBox.inBounds(mX, mY)) {
                if (selectedTextBox == textBox) {
                    selectedTextBox = null;
                } else {
                    selectedTextBox = textBox;
                }
                return true;
            }
        }
        return false;
    }
    
    public boolean onKeyStroke(int k) {
        if (selectedTextBox != null && selectedTextBox.isVisible()) {
            return selectedTextBox.onKeyStroke(k);
        }
        return false;
    }
    
    public boolean onCharTyped(char c) {
        if (selectedTextBox != null && selectedTextBox.isVisible()) {
            return selectedTextBox.onCharTyped(c);
        }
        return false;
    }
    
}
