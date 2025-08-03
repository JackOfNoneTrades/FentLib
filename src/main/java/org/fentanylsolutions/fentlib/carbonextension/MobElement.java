package org.fentanylsolutions.fentlib.carbonextension;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;

import carbonconfiglib.gui.api.IArrayNode;
import carbonconfiglib.gui.api.ICompoundNode;
import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.gui.config.ConfigElement;
import carbonconfiglib.gui.widgets.CarbonButton;

public class MobElement extends ConfigElement {

    GuiButton textBox = (GuiButton) this
        .addChild(new CarbonButton(0, 0, 72, 18, I18n.format("gui.carbonconfig.edit", new Object[0]), this::onPress));

    public MobElement(IValueNode value) {
        super(value);
    }

    public MobElement(IArrayNode array, IValueNode value) {
        super(array, value);
    }

    public MobElement(ICompoundNode compound, IValueNode value) {
        super(compound, value);
    }

    private void onPress(GuiButton button) {
        // this.mc.displayGuiScreen(new ArrayScreen(this.node, this.mc.currentScreen, this.owner.getCustomTexture()));
        System.out.println("sneed");
    }
}
