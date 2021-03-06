package org.cyclops.cyclopscore.client.gui.component.button;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraft.util.text.ITextComponent;
import org.cyclops.cyclopscore.client.gui.image.Images;

/**
 * Inspired by {@link CheckboxButton}, but more flexible.
 * @author rubensworks
 */
public class ButtonCheckbox extends Button {
    private boolean checked;

    public ButtonCheckbox(int x, int y, int width, int height, ITextComponent title, IPressable pressedAction) {
        super(x, y, width, height, title, pressedAction);
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean isChecked() {
        return checked;
    }

    @Override
    public void onPress() {
        setChecked(!isChecked());
        super.onPress();
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if(visible) {
            int i = 0;
            if (isChecked()) {
                i = 2;
            } else if (isHovered) {
                i = 1;
            }
            Images.CHECKBOX[i].draw(this, matrixStack, x, y);
        }
    }
}
