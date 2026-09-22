package org.fentanylsolutions.fentlib.core;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;

import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.transformer.Config;

import cpw.mods.fml.relauncher.FMLRelaunchLog;

/**
 * Drops an unmaintained mod's mixin that {@code @Redirect}s the same vanilla call EFR does, so EFR wins instead of both
 * failing {@code require = 1} and crashing the launch. Hooks the LaunchWrapper + Mixin pipeline shared by runClient and
 * runClient25.
 */
public final class EtFuturumRedirectConflictCompat implements IClassTransformer {

    /** Drop {@link #loserMixin} from {@link #loserConfig} when EFR's {@link #winnerMixin} is live. */
    private static final class Conflict {

        final String loserConfig;
        final String loserMixin;
        final String winnerConfig;
        final String winnerMixin;
        final String reason;

        Conflict(String loserConfig, String loserMixin, String winnerConfig, String winnerMixin, String reason) {
            this.loserConfig = loserConfig;
            this.loserMixin = loserMixin;
            this.winnerConfig = winnerConfig;
            this.winnerMixin = winnerMixin;
            this.reason = reason;
        }
    }

    // Unmaintained mods only; maintained ones (Angelica, ServerUtilities) are left for their upstreams / pack config.
    private static final Conflict[] CONFLICTS = {
        // ArchaicFix's downloading-terrain hider vs EFR's world-thumbnail loading bridge, both on Minecraft.loadWorld.
        new Conflict(
            "mixins.archaicfix.early.json",
            "client.core.MixinMinecraft",
            "mixins.etfuturum.early.json",
            "MixinMinecraft_LoadingBridge",
            "ArchaicFix's downloading-terrain screen hider vs EFR's world-thumbnail loading screen"),
    };

    private static final AtomicBoolean REGISTERED = new AtomicBoolean();

    /** Stop retrying if EFR never shows up. */
    private static final int MAX_ATTEMPTS = 4000;

    private final AtomicBoolean decided = new AtomicBoolean();
    private int attempts;

    static void registerEarly() {
        if (!REGISTERED.compareAndSet(false, true)) {
            return;
        }
        try {
            Launch.classLoader.registerTransformer(EtFuturumRedirectConflictCompat.class.getName());
        } catch (RuntimeException e) {
            REGISTERED.set(false);
            FMLRelaunchLog.warning("FentLib could not register its Et Futurum Requiem redirect-conflict compat: %s", e);
        }
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        // A timed hook, not a rewrite: retry on net.minecraft.* classes until the mixin configs have registered.
        if (basicClass == null || decided.get() || transformedName == null
            || !transformedName.startsWith("net.minecraft.")) {
            return basicClass;
        }
        try {
            if (resolveConflicts() || ++attempts >= MAX_ATTEMPTS) {
                decided.set(true);
            }
        } catch (Throwable t) {
            // Never take down the launch; worst case the original conflict surfaces as before.
            FMLRelaunchLog.warning("FentLib EFR redirect-conflict compat failed, leaving mixins untouched: %s", t);
            decided.set(true);
        }
        return basicClass;
    }

    /** @return true once settled; false to retry until EFR's config has registered. */
    private boolean resolveConflicts() {
        Set<Config> configs = Mixins.getConfigs();
        // EFR anchors every conflict; if its config is missing the registry is still filling, so retry.
        if (findConfig(configs, CONFLICTS[0].winnerConfig) == null) {
            return false;
        }
        for (Conflict conflict : CONFLICTS) {
            Config loser = findConfig(configs, conflict.loserConfig);
            if (loser == null) {
                continue; // Overlapping mod not installed.
            }
            Config winner = findConfig(configs, conflict.winnerConfig);
            if (winner == null || !configContainsMixin(winner.getConfig(), conflict.winnerMixin)) {
                continue; // EFR's competing feature is off, so the loser applies fine on its own.
            }
            int removed = removeMixin(loser.getConfig(), conflict.loserMixin);
            if (removed > 0) {
                FMLRelaunchLog.info(
                    "FentLib disabled %s's %s in favour of Et Futurum Requiem (%s).",
                    shortModName(conflict.loserConfig),
                    conflict.loserMixin,
                    conflict.reason);
            }
        }
        return true;
    }

    private static String shortModName(String configName) {
        // "mixins.serverutilities.early.json" -> "serverutilities"
        int start = configName.indexOf('.');
        int end = configName.indexOf('.', start + 1);
        return start >= 0 && end > start ? configName.substring(start + 1, end) : configName;
    }

    private static Config findConfig(Set<Config> configs, String name) {
        for (Config config : configs) {
            if (name.equals(config.getName())) {
                return config;
            }
        }
        return null;
    }

    private static boolean configContainsMixin(IMixinConfig config, String suffix) {
        for (Object mixin : collectMixinIdentifiers(config)) {
            if (identifierMatches(mixin, suffix)) {
                return true;
            }
        }
        return false;
    }

    /** Removes the mixin from every Collection field, covering both raw class names and MixinInfo objects. */
    private static int removeMixin(IMixinConfig config, String suffix) {
        int removed = 0;
        for (Field field : allInstanceCollectionFields(config.getClass())) {
            Object value;
            try {
                value = field.get(config);
            } catch (IllegalAccessException e) {
                continue;
            }
            if (!(value instanceof Collection)) {
                continue;
            }
            Iterator<?> it = ((Collection<?>) value).iterator();
            try {
                while (it.hasNext()) {
                    if (identifierMatches(it.next(), suffix)) {
                        it.remove();
                        removed++;
                    }
                }
            } catch (UnsupportedOperationException ignored) {
                // Immutable view; the backing list is covered by another field.
            }
        }
        return removed;
    }

    /** Pulls whatever identifiers a config exposes: raw class-name strings and MixinInfo objects from its fields. */
    private static Collection<Object> collectMixinIdentifiers(IMixinConfig config) {
        List<Object> out = new java.util.ArrayList<>();
        for (Field field : allInstanceCollectionFields(config.getClass())) {
            try {
                Object value = field.get(config);
                if (value instanceof Collection) {
                    out.addAll((Collection<?>) value);
                }
            } catch (IllegalAccessException ignored) {
                // skip
            }
        }
        return out;
    }

    private static List<Field> allInstanceCollectionFields(Class<?> type) {
        List<Field> fields = new java.util.ArrayList<>();
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                if (Collection.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    fields.add(field);
                }
            }
        }
        return fields;
    }

    /** A raw config string is the short name; a MixinInfo exposes its fully-qualified class name reflectively. */
    private static boolean identifierMatches(Object identifier, String suffix) {
        if (identifier == null) {
            return false;
        }
        if (identifier instanceof CharSequence) {
            return endsWithMixin(identifier.toString(), suffix);
        }
        String className = reflectName(identifier, "getClassName");
        if (className == null) {
            className = reflectName(identifier, "getName");
        }
        return className != null && endsWithMixin(className, suffix);
    }

    private static boolean endsWithMixin(String value, String suffix) {
        return value.equals(suffix) || value.endsWith('.' + suffix);
    }

    private static String reflectName(Object target, String method) {
        try {
            Method m = target.getClass().getMethod(method);
            Object result = m.invoke(target);
            return result == null ? null : result.toString();
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }
}
