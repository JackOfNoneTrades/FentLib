package org.fentanylsolutions.fentlib.util;

import java.util.ArrayList;
import java.util.List;

public class MixinUtil {

    public static class MixinBuilder {

        private final List<String> mixins = new ArrayList<>();

        public MixinBuilder addMixin(String name, MiscUtil.Side side, String modid) {
            if ((side == MiscUtil.Side.CLIENT && MiscUtil.isServer())
                || (side == MiscUtil.Side.SERVER && !MiscUtil.isServer())) {
                return this;
            }

            mixins.add(modid + "." + name);
            return this;
        }

        public MixinBuilder addMixin(String name, MiscUtil.Side side) {
            return addMixin(name, side, "minecraft");
        }

        public List<String> build() {
            return mixins;
        }
    }
}
