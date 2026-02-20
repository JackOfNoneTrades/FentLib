package org.fentanylsolutions.fentlib.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MixinUtil {

    public enum Phase {
        NONE,
        EARLY,
        LATE;
    }

    public static class Registry {

        private static final Set<String> SPECIAL_IDS = new HashSet<>(
            Arrays.asList("fml", "mcp", "minecraft", "minecraftforge"));
        private final List<String> earlyMixins = new ArrayList<>();
        private final List<MixinBuilder> lateMixinBuilders = new ArrayList<>();

        public MixinBuilder mixin(String name) {
            return new MixinBuilder(this, name);
        }

        public List<String> resolveEarly() {
            return new ArrayList<>(earlyMixins);
        }

        public List<String> resolveLate(Set<String> loadedCoreMods) {
            List<String> res = new ArrayList<>();
            for (MixinBuilder mb : lateMixinBuilders) {
                if (!SPECIAL_IDS.contains(mb.modid) && !loadedCoreMods.contains(mb.modid)) {
                    continue;
                }
                res.add(mb.modid + "." + mb.name);
            }
            return res;
        }
    }

    public static class MixinBuilder {

        private final Registry registry;
        public String name;
        public String modid = "minecraft";
        public MiscUtil.Side side = MiscUtil.Side.BOTH;
        public Phase phase = Phase.NONE;

        public MixinBuilder(Registry registry, String name) {
            this.registry = registry;
            this.name = name;
        }

        public MixinBuilder modid(String modid) {
            this.modid = modid;
            return this;
        }

        public MixinBuilder side(MiscUtil.Side side) {
            this.side = side;
            return this;
        }

        public MixinBuilder phase(Phase phase) {
            this.phase = phase;
            return this;
        }

        public void build() {
            if (this.phase == Phase.NONE) {
                throw new RuntimeException("MixinBuilder " + this.name + " needs a phase to be set");
            }
            if ((this.side == MiscUtil.Side.CLIENT && MiscUtil.isServer())
                || (this.side == MiscUtil.Side.SERVER && !MiscUtil.isServer())) {
                return;
            }
            if (this.phase == Phase.EARLY) {
                registry.earlyMixins.add(this.modid + "." + this.name);
            } else if (this.phase == Phase.LATE) {
                registry.lateMixinBuilders.add(this);
            }
        }
    }
}
