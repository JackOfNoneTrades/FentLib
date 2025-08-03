package org.fentanylsolutions.fentlib.mixins.early.minecraft;

import java.util.Map;

import net.minecraft.entity.EntityList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityList.class)
public interface EntityListAccessor {

    @Accessor
    static Map getStringToIDMapping() {
        throw new AssertionError();
    }
}
