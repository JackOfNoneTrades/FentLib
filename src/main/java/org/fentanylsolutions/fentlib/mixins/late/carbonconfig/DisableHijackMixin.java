package org.fentanylsolutions.fentlib.mixins.late.carbonconfig;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import carbonconfiglib.CarbonConfig;
import carbonconfiglib.config.ConfigEntry;
import carbonconfiglib.config.ConfigSection;
import carbonconfiglib.impl.ReloadMode;

@SuppressWarnings("unused")
@Mixin(CarbonConfig.class)
public class DisableHijackMixin {

    @Redirect(
        method = "onPreInit",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lcarbonconfiglib/config/ConfigSection;addBool(Ljava/lang/String;Z[Ljava/lang/String;)Lcarbonconfiglib/config/ConfigEntry$BoolValue;"))
    private ConfigEntry.BoolValue redirectAddBool(ConfigSection section, String key, boolean defaultValue,
        String[] comments) {
        if ("force-forge-support".equals(key)) {
            return section.addBool(key, false, comments)
                .setRequiredReload(ReloadMode.GAME);
        }
        return section.addBool(key, defaultValue, comments);
    }
}
