package org.fentanylsolutions.fentlib.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.fentanylsolutions.fentlib.util.MixinUtil;

import cpw.mods.fml.common.Loader;

public abstract class FentMixins {

    private static final List<String> specialIds = Arrays.asList("fml", "mcp", "minecraft", "minecraftforge");

    private static final List<String> earlyMixins = new ArrayList<>();
    private static final List<MixinUtil.MixinBuilder> lateMixinBuilders = new ArrayList<>();

    public static void staticInit() {
        MixinUtil.bindMixinLists(earlyMixins, lateMixinBuilders);
    }

    public static List<String> getEarlyMixins() {
        staticInit();
        return earlyMixins;
    }

    public static List<String> getLateMixins() {
        List<String> res = new ArrayList<>();
        for (MixinUtil.MixinBuilder mb : lateMixinBuilders) {
            if (!specialIds.contains(mb.modid) && !Loader.isModLoaded(mb.modid)) {
                continue;
            }
            res.add(mb.modid + "." + mb.name);
        }
        return res;
    }
}
