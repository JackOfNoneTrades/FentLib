package org.fentanylsolutions.fentlib.mixins.late.modularui2;

import net.minecraft.client.gui.GuiScreen;

import org.fentanylsolutions.fentlib.mixininterfaces.IModularPanel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.cleanroommc.modularui.screen.ModularPanel;

@Mixin(ModularPanel.class)
public class MixinModularPanel implements IModularPanel {

    @Unique
    GuiScreen guiScreen;

    @Override
    public void setGuiScreen(GuiScreen guiScreen) {
        this.guiScreen = guiScreen;
    }

    @Override
    public GuiScreen getGuiScreen() {
        return this.guiScreen;
    }
}
