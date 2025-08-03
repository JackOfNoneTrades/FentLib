package org.fentanylsolutions.fentlib.varinstances;

import java.util.HashMap;
import java.util.Map;

import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityList;

import net.minecraft.item.Item;
import org.fentanylsolutions.fentlib.Config;
import org.fentanylsolutions.fentlib.FentLib;
import org.fentanylsolutions.fentlib.carbonextension.CarbonNamespacedRegistry;
import org.fentanylsolutions.fentlib.carbonextension.MobWrapper;
import org.fentanylsolutions.fentlib.mixins.early.minecraft.EntityListAccessor;
import org.fentanylsolutions.fentlib.util.MobUtil;

public class VarInstanceCommon {

    public HashMap<String, Float> mobAggroValues = new HashMap<>();
    public CarbonCompat carbonCompat = new CarbonCompat();

    public void initHook() {
        if (FentLib.carbonConfigLoaded) {
            carbonCompat.initHook();
        }
    }

    public void postInitHook() {

    }
}
