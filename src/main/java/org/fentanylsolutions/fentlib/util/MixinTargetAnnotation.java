package org.fentanylsolutions.fentlib.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class MixinTargetAnnotation {

    public enum Phase {
        EARLY,
        // MID,
        LATE;
    }

    public enum Side {
        CLIENT,
        SERVER,
        BOTH;
    }

    // @Retention(RetentionPolicy.RUNTIME)
    @Retention(RetentionPolicy.CLASS)
    @Target(ElementType.TYPE)
    public @interface MixinTarget {

        Phase phase() default Phase.LATE;

        Side side() default Side.BOTH;

        String modid() default "minecraft";
    }
}
