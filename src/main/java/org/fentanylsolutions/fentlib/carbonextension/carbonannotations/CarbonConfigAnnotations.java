package org.fentanylsolutions.fentlib.carbonextension.carbonannotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class CarbonConfigAnnotations {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface FentConfig {

        String name();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface FentConfigOnSync {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface FentConfigOnCreate {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ConfigArray {

        String name();

        String comment() default "";

        int min() default Integer.MIN_VALUE;

        int max() default Integer.MAX_VALUE;

        Class<? extends IArraySerializer<?>> serializer() default Serializers.DefaultStringSerializer.class;

        String category();

        boolean clientSynced() default false;

        boolean requiresGameReload() default false;

        boolean requiresWorldReload() default false;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ConfigInt {

        String name();

        String comment() default "";

        int defaultValue();

        int min() default Integer.MIN_VALUE;

        int max() default Integer.MAX_VALUE;

        String category();

        boolean clientSynced() default false;

        boolean requiresGameReload() default false;

        boolean requiresWorldReload() default false;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ConfigDouble {

        String name();

        String comment() default "";

        double defaultValue();

        double min() default Double.MIN_VALUE;

        double max() default Double.MAX_VALUE;

        String category();

        boolean clientSynced() default false;

        boolean requiresGameReload() default false;

        boolean requiresWorldReload() default false;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ConfigBool {

        String name();

        String comment() default "";

        boolean defaultValue();

        String category();

        boolean clientSynced() default false;

        boolean requiresGameReload() default false;

        boolean requiresWorldReload() default false;
    }
}
