package org.fentanylsolutions.fentlib.gui.sodiumgui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SodiumGuiButton {

    private static final ResourceLocation BUTTON_SOUND = new ResourceLocation("gui.button.press");

    private String label;
    private Runnable action;
    private int x;
    private int y;
    private int width;
    private int height;
    private boolean selected;
    private boolean enabled = true;
    private boolean visible = true;
    private SodiumGuiTheme theme;

    public SodiumGuiButton(String label, Runnable action) {
        this.label = label;
        this.action = action;
    }

    public SodiumGuiButton setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        return this;
    }

    public SodiumGuiButton setLabel(String label) {
        this.label = label;
        return this;
    }

    public SodiumGuiButton setAction(Runnable action) {
        this.action = action;
        return this;
    }

    public SodiumGuiButton setSelected(boolean selected) {
        this.selected = selected;
        return this;
    }

    public SodiumGuiButton setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public SodiumGuiButton setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    public SodiumGuiButton setTheme(SodiumGuiTheme theme) {
        this.theme = theme;
        return this;
    }

    public SodiumGuiButton setAccentColor(int accentColor) {
        this.theme = SodiumGuiTheme.withAccent(accentColor);
        return this;
    }

    public String getLabel() {
        return this.label;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public SodiumGuiTheme getTheme() {
        return this.theme == null ? SodiumGuiTheme.getDefault() : this.theme;
    }

    public void render(FontRenderer fontRenderer, int mouseX, int mouseY) {
        if (!this.visible) {
            return;
        }

        SodiumGuiTheme resolvedTheme = getTheme();
        boolean hovered = this.enabled && SodiumGui.isInside(mouseX, mouseY, this.x, this.y, this.width, this.height);
        int background = this.enabled ? hovered ? resolvedTheme.getRowHoverColor() : resolvedTheme.getRowIdleColor()
            : resolvedTheme.getRowDisabledColor();
        int textColor = this.enabled ? 0xFFFFFFFF : 0x90FFFFFF;

        SodiumGui.drawFlatBox(this.x, this.y, this.width, this.height, background);
        int textWidth = fontRenderer.getStringWidth(this.label);
        fontRenderer
            .drawString(this.label, this.x + this.width / 2 - textWidth / 2, this.y + this.height / 2 - 4, textColor);
        if (this.enabled && this.selected) {
            Gui.drawRect(
                this.x,
                this.y + this.height - 1,
                this.x + this.width,
                this.y + this.height,
                resolvedTheme.getAccentColor());
        }
    }

    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!this.enabled || !this.visible
            || mouseButton != 0
            || !SodiumGui.isInside(mouseX, mouseY, this.x, this.y, this.width, this.height)) {
            return false;
        }
        if (this.action != null) {
            this.action.run();
        }
        playClickSound();
        return true;
    }

    public static void playClickSound() {
        Minecraft.getMinecraft()
            .getSoundHandler()
            .playSound(PositionedSoundRecord.func_147674_a(BUTTON_SOUND, 1.0F));
    }
}
