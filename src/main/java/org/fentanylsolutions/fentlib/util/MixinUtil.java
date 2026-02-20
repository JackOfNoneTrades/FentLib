package org.fentanylsolutions.fentlib.util;

import java.util.List;

public class MixinUtil {

    private static List<String> earlyMixins;
    private static List<MixinBuilder> lateMixinBuilders;

    public enum Phase {
        NONE,
        EARLY,
        LATE;
    }

    public static void bindMixinLists(List<String> earlyMixinsLst, List<MixinBuilder> lateMixinBuildersLst) {
        earlyMixins = earlyMixinsLst;
        lateMixinBuilders = lateMixinBuildersLst;
    }

    public static class MixinBuilder {

        public String name;
        public String modid = "minecraft";
        public MiscUtil.Side side = MiscUtil.Side.BOTH;
        public Phase phase = Phase.NONE;

        public MixinBuilder(String name) {
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
                earlyMixins.add(this.modid + "." + this.name);
            } else if (this.phase == Phase.LATE) {
                lateMixinBuilders.add(this);
            }
        }
    }
}
