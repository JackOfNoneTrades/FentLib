package org.fentanylsolutions.fentlib.mixins.late.carbonconfig;

import java.util.List;
import java.util.Map;

import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import carbonconfiglib.gui.api.IValueNode;
import carbonconfiglib.gui.config.ArrayElement;
import carbonconfiglib.gui.config.ConfigElement;
import carbonconfiglib.gui.config.Element;
import carbonconfiglib.gui.widgets.CarbonHoverIconButton;
import carbonconfiglib.gui.widgets.CarbonIconButton;
import carbonconfiglib.gui.widgets.GuiUtils;
import carbonconfiglib.gui.widgets.Icon;
import carbonconfiglib.gui.widgets.screen.IWidget;

@SuppressWarnings("unused")
@Mixin(value = ConfigElement.class, remap = false)
public abstract class ConfigElementMixin extends Element {

    public ConfigElementMixin(IChatComponent name) {
        super(name);
    }

    @Shadow
    protected abstract boolean isArray();

    @Shadow
    protected abstract boolean isCompound();

    @Shadow
    protected IValueNode value;

    @Final
    @Shadow
    private static IChatComponent RELOAD;

    @Final
    @Shadow
    private static IChatComponent RESTART;

    @Final
    @Shadow
    private static IChatComponent DELETE;

    @Final
    @Shadow
    private static IChatComponent REVERT;

    @Final
    @Shadow
    private static IChatComponent DEFAULT;

    @Final
    @Shadow
    private static IChatComponent SUGGESTIONS;

    @Shadow
    protected abstract boolean renderChildren();

    @Shadow
    protected CarbonHoverIconButton moveUp;

    @Shadow
    protected abstract boolean canMoveUp();

    @Shadow
    protected CarbonHoverIconButton moveDown;

    @Shadow
    protected abstract boolean canMoveDown();

    @Shadow
    protected List<Map.Entry<IWidget, ConfigElement.AlignOffset>> mappedListeners;

    @Shadow
    protected abstract int getMaxX(int prevMaxX);

    @Shadow
    protected abstract int indexOf();

    @Shadow
    protected CarbonIconButton setReset;

    @Shadow
    protected CarbonIconButton setDefault;

    @Shadow
    protected CarbonIconButton suggestion;

    @Shadow
    protected abstract boolean hasSuggestions();

    /**
     * @author jack
     * @reason Fixes array tooltips not rendering
     */
    @Overwrite
    public void render(int x, int top, int left, int width, int height, int mouseX, int mouseY, boolean selected,
        float partialTicks) {
        if (((ConfigElementAccessor) this).invokeRenderName() && !isArray()) {
            renderName(left, top, isChanged(), isCompound() ? 80 : 200, height);
            if (!isCompound() && value != null) {
                if (value.requiresReload()) {
                    GuiUtils.drawTextureRegion(left - 16, top + (height / 2) - 6, 12, 12, Icon.RELOAD, 16, 16);
                    if (mouseX >= left - 16 && mouseX <= left - 4
                        && mouseY >= top
                        && mouseY <= top + height
                        && owner.isInsideList(mouseX, mouseY)) {
                        owner.addTooltips(RELOAD);
                    }
                }
                if (value.requiresRestart()) {
                    GuiUtils.drawTextureRegion(left - 16, top + (height / 2) - 6, 12, 12, Icon.RESTART, 16, 16);
                    if (mouseX >= left - 16 && mouseX <= left - 4
                        && mouseY >= top
                        && mouseY <= top + height
                        && owner.isInsideList(mouseX, mouseY)) {
                        owner.addTooltips(RESTART);
                    }
                }
            }
        }
        int maxX = Integer.MAX_VALUE;
        if (renderChildren()) {
            if (isArray()) {
                moveUp.xPosition = left + width - 16;
                moveUp.yPosition = top;
                moveUp.visible = canMoveUp();
                moveUp.render(((ElementAccessor) this).getMc(), mouseX, mouseY, partialTicks);
                moveDown.xPosition = left + width - 16;
                moveDown.yPosition = top + 10;
                moveDown.visible = canMoveDown();
                moveDown.render(((ElementAccessor) this).getMc(), mouseX, mouseY, partialTicks);
                if (moveDown.visible || moveUp.visible) {
                    left -= 8;
                }
            }
            for (Map.Entry<IWidget, ConfigElement.AlignOffset> entry : mappedListeners) {
                IWidget widget = entry.getKey();
                ConfigElement.AlignOffset offset = entry.getValue();
                widget.setX(
                    ((AlignOffsetAccessor) offset).getAlign()
                        .align(left, width, widget.getWidgetWidth()) + ((AlignOffsetAccessor) offset).getOffset());
                widget.setY(top);
                widget.render(((ElementAccessor) this).getMc(), mouseX, mouseY, partialTicks);
                maxX = Math.min(maxX, widget.getX());
            }
        }
        maxX = getMaxX(maxX);
        if (isArray()) {
            IChatComponent comp = new ChatComponentText(indexOf() + ":");
            renderText(comp, maxX - 115, top - 1, 105, height, ConfigElement.GuiAlign.RIGHT, -1);
        }
        if (mouseY >= top && mouseY <= top + height
            && mouseX >= left
            && mouseX <= maxX - 2
            && owner.isInsideList(mouseX, mouseY)) {
            if (value != null) {
                owner.addTooltips(value.getTooltip());
            } else if (((Object) this) instanceof ArrayElement && ((ArrayElementAccessor) this).getNode() != null) {
                owner.addTooltips(
                    ((ArrayElementAccessor) this).getNode()
                        .getTooltip());
            }
        }
        if (isArray()) {
            if (setReset.isHovered() && owner.isInsideList(mouseX, mouseY)) {
                owner.addTooltips(DELETE);
            }
        } else {
            if (setReset.isHovered() && owner.isInsideList(mouseX, mouseY)) {
                owner.addTooltips(REVERT);
            }
            if (setDefault.isHovered() && owner.isInsideList(mouseX, mouseY)) {
                owner.addTooltips(DEFAULT);
            }
            suggestion.visible = hasSuggestions();
            if (suggestion.visible && suggestion.isHovered() & owner.isInsideList(mouseX, mouseY)) {
                owner.addTooltips(SUGGESTIONS);
            }
        }
    }
}
