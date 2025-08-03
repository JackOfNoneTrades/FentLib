package org.fentanylsolutions.fentlib.mixins.mid.fml;

import com.google.common.collect.BiMap;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.RegistryDelegate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = GameData.class, remap = false)
public interface GameDataAccessor {
    @Accessor
    static int getMAX_BLOCK_ID() {
        throw new AssertionError();
    }

    @Invoker
    static GameData invokeGetMain() {
        throw new AssertionError();
    }

    @Invoker
    int invokeRegister(Object obj, String name, int idHint);

    @Invoker
    static <T> RegistryDelegate<T> invokeBuildDelegate(T referant, Class<T> type) {
        throw new AssertionError();
    }

    @Invoker
    <T> BiMap<String, T> invokeGetPersistentSubstitutionMap(Class<T> type);
}
