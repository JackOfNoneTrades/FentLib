package org.fentanylsolutions.fentlib.services.http;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fentanylsolutions.fentlib.FentLib;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class HttpPortProxyConfig {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping()
        .setPrettyPrinting()
        .create();
    private static final String CONFIG_FILE_NAME = "http-port-routes.json";
    private static volatile HttpPortProxyConfig cachedConfig;
    private static volatile long cachedLastModified = Long.MIN_VALUE;

    private static final Map<String, Route> apiRoutes = new ConcurrentHashMap<>();

    public List<Route> routes = new ArrayList<>();

    public static final class Route {

        public Boolean enabled = Boolean.TRUE;
        public String path = "";
        public int targetPort;
        public String targetPathPrefix = "/";

        public String normalizedPath() {
            String value = path == null ? "" : path.trim();
            while (value.startsWith("/")) {
                value = value.substring(1);
            }
            while (value.endsWith("/")) {
                value = value.substring(0, value.length() - 1);
            }
            return value;
        }

        public String normalizedTargetPathPrefix() {
            String value = targetPathPrefix == null ? "/" : targetPathPrefix.trim();
            if (value.isEmpty()) {
                value = "/";
            }
            if (!value.startsWith("/")) {
                value = "/" + value;
            }
            while (value.length() > 1 && value.endsWith("/")) {
                value = value.substring(0, value.length() - 1);
            }
            return value;
        }

        public boolean isValid() {
            return !normalizedPath().isEmpty() && targetPort > 0 && targetPort <= 65535;
        }

        public boolean isEnabled() {
            return enabled == null || enabled.booleanValue();
        }
    }

    private HttpPortProxyConfig sanitized() {
        HttpPortProxyConfig result = new HttpPortProxyConfig();
        for (Route route : routes == null ? Collections.<Route>emptyList() : routes) {
            if (route == null || !route.isEnabled() || !route.isValid()) {
                continue;
            }
            Route sanitizedRoute = new Route();
            sanitizedRoute.enabled = Boolean.TRUE;
            sanitizedRoute.path = route.normalizedPath();
            sanitizedRoute.targetPort = route.targetPort;
            sanitizedRoute.targetPathPrefix = route.normalizedTargetPathPrefix();
            result.routes.add(sanitizedRoute);
        }
        return result;
    }

    public boolean hasRoutes() {
        return !routes.isEmpty();
    }

    public List<Route> getRoutes() {
        return Collections.unmodifiableList(routes);
    }

    public Route match(String path) {
        String normalizedPath = normalizeRequestPath(path);
        if (normalizedPath.isEmpty()) {
            return null;
        }
        String firstSegment = normalizedPath;
        int slash = normalizedPath.indexOf('/');
        if (slash >= 0) {
            firstSegment = normalizedPath.substring(0, slash);
        }
        Route apiRoute = apiRoutes.get(firstSegment);
        if (apiRoute != null) {
            return apiRoute;
        }
        for (Route route : routes) {
            if (route.path.equals(firstSegment)) {
                return route;
            }
        }
        return null;
    }

    /**
     * Register a route at runtime from another mod. Takes priority over file-based routes.
     *
     * @param path       first path segment to match (e.g. "emojis")
     * @param targetPort local port to proxy to
     */
    public static void registerRoute(String path, int targetPort) {
        registerRoute(path, targetPort, "/");
    }

    /**
     * Register a route at runtime from another mod. Takes priority over file-based routes.
     *
     * @param path             first path segment to match (e.g. "emojis")
     * @param targetPort       local port to proxy to
     * @param targetPathPrefix path prefix on the target (default "/")
     */
    public static void registerRoute(String path, int targetPort, String targetPathPrefix) {
        Route route = new Route();
        route.path = path;
        route.targetPort = targetPort;
        route.targetPathPrefix = targetPathPrefix;
        Route sanitized = new Route();
        sanitized.path = route.normalizedPath();
        sanitized.targetPort = route.targetPort;
        sanitized.targetPathPrefix = route.normalizedTargetPathPrefix();
        if (!sanitized.isValid()) {
            FentLib.LOG.warn("Invalid API route: path='{}', port={}", path, targetPort);
            return;
        }
        apiRoutes.put(sanitized.path, sanitized);
        FentLib.LOG.info("Registered API HTTP route '{}' -> localhost:{}", sanitized.path, targetPort);
    }

    /**
     * Unregister a previously registered API route.
     *
     * @param path the path segment to remove
     */
    public static void unregisterRoute(String path) {
        String normalized = normalizeRequestPath(path);
        Route removed = apiRoutes.remove(normalized);
        if (removed != null) {
            FentLib.LOG.info("Unregistered API HTTP route '{}'", normalized);
        }
    }

    public static String normalizeRequestPath(String path) {
        String normalizedPath = path == null ? "" : path.trim();
        while (normalizedPath.startsWith("/")) {
            normalizedPath = normalizedPath.substring(1);
        }
        while (normalizedPath.contains("//")) {
            normalizedPath = normalizedPath.replace("//", "/");
        }
        while (normalizedPath.endsWith("/") && !normalizedPath.isEmpty()) {
            normalizedPath = normalizedPath.substring(0, normalizedPath.length() - 1);
        }
        return normalizedPath;
    }

    public static String stripRoutePrefix(String path, Route route) {
        String normalizedPath = normalizeRequestPath(path);
        if (normalizedPath.equals(route.path)) {
            return "";
        }
        if (normalizedPath.startsWith(route.path + "/")) {
            return normalizedPath.substring(route.path.length() + 1);
        }
        return normalizedPath;
    }

    public static HttpPortProxyConfig get() {
        File file = getConfigFile();
        long lastModified = file.isFile() ? file.lastModified() : Long.MIN_VALUE;
        HttpPortProxyConfig config = cachedConfig;
        if (config != null && cachedLastModified == lastModified) {
            return config;
        }
        synchronized (HttpPortProxyConfig.class) {
            lastModified = file.isFile() ? file.lastModified() : Long.MIN_VALUE;
            if (cachedConfig != null && cachedLastModified == lastModified) {
                return cachedConfig;
            }
            HttpPortProxyConfig loaded = load(file);
            cachedConfig = loaded;
            cachedLastModified = file.isFile() ? file.lastModified() : Long.MIN_VALUE;
            return loaded;
        }
    }

    public static void ensureExists() {
        get();
    }

    private static HttpPortProxyConfig load(File file) {
        File parent = file.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
            FentLib.LOG.warn("Could not create FentLib HTTP config directory at {}", parent);
        }
        if (!file.isFile()) {
            HttpPortProxyConfig defaults = defaultConfig();
            save(file, defaults);
            return defaults.sanitized();
        }
        try (FileReader reader = new FileReader(file)) {
            HttpPortProxyConfig parsed = GSON.fromJson(reader, HttpPortProxyConfig.class);
            if (parsed == null) {
                HttpPortProxyConfig defaults = defaultConfig();
                save(file, defaults);
                return defaults.sanitized();
            }
            return parsed.sanitized();
        } catch (Exception e) {
            FentLib.LOG.error("Failed to read {}", file, e);
            return new HttpPortProxyConfig();
        }
    }

    private static void save(File file, HttpPortProxyConfig config) {
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            FentLib.LOG.error("Failed to write {}", file, e);
        }
    }

    private static HttpPortProxyConfig defaultConfig() {
        HttpPortProxyConfig config = new HttpPortProxyConfig();
        Route route = new Route();
        route.enabled = Boolean.TRUE;
        route.path = "dynmap";
        route.targetPort = 8123;
        route.targetPathPrefix = "/";
        config.routes.add(route);
        return config;
    }

    private static File getConfigFile() {
        return new File(getConfigDir(), CONFIG_FILE_NAME);
    }

    private static File getConfigDir() {
        if (FentLib.fentlibDir != null) {
            return FentLib.fentlibDir;
        }
        return FentLib.getConfigDir();
    }

}
