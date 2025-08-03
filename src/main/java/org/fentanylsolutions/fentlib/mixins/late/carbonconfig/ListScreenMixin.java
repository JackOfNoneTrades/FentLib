package org.fentanylsolutions.fentlib.mixins.late.carbonconfig;

import com.cleanroommc.modularui.factory.ClientGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

import org.fentanylsolutions.fentlib.chadconfig.TestGui;
import org.fentanylsolutions.fentlib.util.MobUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import carbonconfiglib.gui.config.ListScreen;
import carbonconfiglib.gui.screen.ConfigScreen;
import carbonconfiglib.gui.screen.ConfigSelectorScreen;
import carbonconfiglib.gui.widgets.CarbonButton;
import carbonconfiglib.gui.widgets.GuiUtils;
import carbonconfiglib.gui.widgets.Icon;

// WIP

@Mixin(ListScreen.class)
public class ListScreenMixin {

    private Icon fentIcon = null;

    private Icon getFentIcon() {
        if (fentIcon == null) {
            fentIcon = new Icon(new ResourceLocation("fentlib:logo.png"), 0, 0, 400, 400);
        }
        return fentIcon;
    }

    @Redirect(
        method = "drawScreen",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lcarbonconfiglib/gui/widgets/GuiUtils;drawTextureRegion(FFFFLcarbonconfiglib/gui/widgets/Icon;FF)V"))
    private void redirectDrawTextureRegion(float x, float y, float width, float height, Icon icon, float texWidth,
        float texHeight) {
        // Replace with your custom logo

        if ((Object) this instanceof ConfigSelectorScreen) {
            if (((ConfigSelectorScreenAccessor) this).getModName()
                .getUnformattedText()
                .equals("Fentlib")) {
                icon = getFentIcon();
            }
        } else if ((Object) this instanceof ConfigScreen
            && ((ConfigScreenAccessor) this).getParent() instanceof ConfigSelectorScreen) {
                if (((ConfigSelectorScreenAccessor) ((ConfigScreenAccessor) this).getParent()).getModName()
                    .getUnformattedText()
                    .equals("Fentlib")) {
                    ResourceLocation bruh = new ResourceLocation("fentlib:logo.png");
                    icon = getFentIcon();
                }
            }

        GuiUtils.drawTextureRegion(x, y, width, height, icon, texWidth, texHeight);
    }

    /*
     * @Inject(method = "mouseClick", at = @At("HEAD"), cancellable = true, remap = false)
     * public void onMouseClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
     * if (mouseX >= 5.0d && mouseX <= 45.0d && mouseY >= 5.0d && mouseY <= 45.0d) {
     * try {
     * Desktop.getDesktop().browse(new URI("https://github.com/JackOfNoneTrades/FentLib"));
     * } catch (Exception e) {
     * e.printStackTrace();
     * }
     * cir.setReturnValue(true);
     * }
     * }
     */

    /*
     * @Inject(method = "<init>", at = @At("RETURN"))
     * public void onConstruct(String key, Object[] args, CallbackInfo ci) {
     * if ("gui.carbonconfig.logo.name".equals(key)) {
     * ((ChatComponentTranslation) (Object) this).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.AQUA));
     * //((ChatComponentTranslation) (Object) this).
     * ((ChatComponentTranslation) (Object) this).appendText("Your Custom Mod Name");
     * }
     * if ("gui.carbonconfig.logo.page".equals(key)) {
     * //((ChatComponentTranslation) (Object) this).clearSiblings();
     * ((ChatComponentTranslation) (Object) this).appendText("yourcustomsite.com");
     * }
     * }
     */

    private static final int CUSTOM_BUTTON_ID = 696969;

    private void onPress(GuiButton button) {
        MobUtil.printMobNames();
        ClientGUI.open(TestGui.createGUI());
    }

    @Inject(method = "initGui", at = @At("TAIL"), remap = false)
    private void onInitGui(CallbackInfo ci) {
        if ((Object) this instanceof ConfigSelectorScreen) {
            if (((ConfigSelectorScreenAccessor) this).getModName()
                .getUnformattedText()
                .equals("Fentlib")) {

                String buttonText = "Print Mob Classes";
                ((ListScreen) (Object) this).addWidget(
                    new CarbonButton(
                        30,
                        500,
                        Minecraft.getMinecraft().fontRenderer.getStringWidth(buttonText),
                        20,
                        buttonText,
                        this::onPress));
            }
        }
    }
}
