package org.fentanylsolutions.fentlib.mixins.early.carbonconfig;

import org.fentanylsolutions.fentlib.FentLib;
import org.fentanylsolutions.fentlib.mixininterfaces.IConfigMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import carbonconfiglib.config.ConfigHandler;
import carbonconfiglib.impl.internal.EventHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

@SuppressWarnings("unused")
@Mixin(EventHandler.class)
public class EventHandlerMixin {

    @Redirect(
        method = "onConfigCreated",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lcpw/mods/fml/common/Loader;activeModContainer()Lcpw/mods/fml/common/ModContainer;"))
    private ModContainer redirectActiveModContainer(Loader loader, ConfigHandler config) {
        String modid = ((IConfigMixin) config.getConfig()).getRealOwnerId();
        FentLib.LOG.info("Redirected Loader.activeModContainer() [{}]", modid);
        if (modid == null) {
            return loader.activeModContainer();
        } else {
            for (ModContainer container : Loader.instance()
                .getModList()) {
                if (modid.equals(container.getModId())) {
                    return container;
                }
            }
        }
        return loader.activeModContainer();
    }
}
