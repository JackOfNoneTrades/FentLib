package org.fentanylsolutions.fentlib.mixins.late.carbonconfig;

import org.fentanylsolutions.fentlib.mixininterfaces.IConfigMixin;
import org.spongepowered.asm.mixin.Mixin;

import carbonconfiglib.config.Config;

@Mixin(Config.class)
public class ConfigMixin implements IConfigMixin {

    private String realOwnerId = null;

    @Override
    public void setRealOwnerId(String ownerId) {
        realOwnerId = ownerId;
    }

    @Override
    public String getRealOwnerId() {
        return realOwnerId;
    }
}
