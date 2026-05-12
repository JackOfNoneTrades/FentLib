package org.fentanylsolutions.fentlib.services.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.fentanylsolutions.fentlib.FentLib;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;

public class ReverseProxyHttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final String CONTENT_TYPE_JSON = "application/json; charset=utf-8";

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        if (tryHandle(ctx, request)) {
            return;
        }
        sendJson(
            ctx,
            HttpResponseStatus.NOT_FOUND,
            "{\"error\":\"RouteNotFound\",\"errorMessage\":\"No HTTP port route matched this path\"}");
    }

    public static boolean tryHandle(ChannelHandlerContext ctx, FullHttpRequest request) {
        String uri = request.getUri();
        String path = new QueryStringDecoder(uri).path();
        HttpPortProxyConfig config = HttpPortProxyConfig.get();
        ResolvedRoute resolvedRoute = resolveRoute(config, path);
        if (resolvedRoute == null) {
            return false;
        }
        if (shouldRedirectToMountedDirectory(path, resolvedRoute)) {
            sendRedirect(ctx, addTrailingSlash(uri));
            return true;
        }

        try {
            ProxyResponse response = proxy(resolvedRoute, request);
            sendResponse(ctx, response);
        } catch (IOException e) {
            FentLib.LOG.warn("Proxy request failed for {} {}: {}", request.getMethod(), uri, e.getMessage());
            sendJson(
                ctx,
                HttpResponseStatus.BAD_GATEWAY,
                "{\"error\":\"UpstreamUnavailable\",\"errorMessage\":\"Failed to contact mapped local service\"}");
        }
        return true;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        FentLib.LOG.warn("HTTP reverse proxy exception: {}", cause.getMessage());
        ctx.close();
    }

    private static ProxyResponse proxy(ResolvedRoute resolvedRoute, FullHttpRequest request) throws IOException {
        String upstreamUrl = buildUpstreamUrl(resolvedRoute.route, request.getUri(), resolvedRoute.routeRelativePath);
        HttpURLConnection connection = (HttpURLConnection) new URL(upstreamUrl).openConnection();
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod(
            request.getMethod()
                .name());
        connection.setDoInput(true);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(15000);

        copyRequestHeaders(request, connection);

        ByteBuf content = request.content();
        if (content.readableBytes() > 0) {
            byte[] body = new byte[content.readableBytes()];
            content.getBytes(content.readerIndex(), body);
            connection.setDoOutput(true);
            try (OutputStream output = connection.getOutputStream()) {
                output.write(body);
            }
        }

        int status = connection.getResponseCode();
        byte[] body = readBody(connection);
        Map<String, String> headers = copyResponseHeaders(connection, resolvedRoute.route);
        return new ProxyResponse(status, body, headers);
    }

    private static String buildUpstreamUrl(HttpPortProxyConfig.Route route, String uri, String routeRelativePath) {
        StringBuilder builder = new StringBuilder();
        builder.append("http://127.0.0.1:")
            .append(route.targetPort)
            .append(route.normalizedTargetPathPrefix());
        if (!routeRelativePath.isEmpty()) {
            if (builder.charAt(builder.length() - 1) != '/') {
                builder.append('/');
            }
            builder.append(routeRelativePath);
        }
        int queryIndex = uri.indexOf('?');
        if (queryIndex >= 0) {
            builder.append(uri.substring(queryIndex));
        }
        return builder.toString();
    }

    private static void copyRequestHeaders(FullHttpRequest request, HttpURLConnection connection) {
        for (Map.Entry<String, String> header : request.headers()) {
            String name = header.getKey();
            if ("host".equalsIgnoreCase(name) || "connection".equalsIgnoreCase(name)
                || "content-length".equalsIgnoreCase(name)) {
                continue;
            }
            connection.setRequestProperty(name, header.getValue());
        }
    }

    private static Map<String, String> copyResponseHeaders(HttpURLConnection connection,
        HttpPortProxyConfig.Route route) {
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : connection.getHeaderFields()
            .entrySet()) {
            String name = entry.getKey();
            if (name == null || entry.getValue() == null
                || entry.getValue()
                    .isEmpty()) {
                continue;
            }
            if ("transfer-encoding".equalsIgnoreCase(name) || "connection".equalsIgnoreCase(name)) {
                continue;
            }
            String value = entry.getValue()
                .get(0);
            if ("location".equalsIgnoreCase(name)) {
                value = rewriteLocationHeader(value, route);
            }
            result.put(name, value);
        }
        return result;
    }

    private static byte[] readBody(HttpURLConnection connection) throws IOException {
        InputStream stream = null;
        try {
            stream = connection.getErrorStream();
            if (stream == null) {
                stream = connection.getInputStream();
            }
            if (stream == null) {
                return new byte[0];
            }
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = stream.read(buffer)) >= 0) {
                output.write(buffer, 0, read);
            }
            return output.toByteArray();
        } finally {
            if (stream != null) {
                stream.close();
            }
            connection.disconnect();
        }
    }

    private static void sendResponse(ChannelHandlerContext ctx, ProxyResponse proxyResponse) {
        ByteBuf content = Unpooled.wrappedBuffer(proxyResponse.body);
        FullHttpResponse response = new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            HttpResponseStatus.valueOf(proxyResponse.statusCode),
            content);
        for (Map.Entry<String, String> header : proxyResponse.headers.entrySet()) {
            response.headers()
                .set(header.getKey(), header.getValue());
        }
        response.headers()
            .set(HttpHeaders.Names.CONTENT_LENGTH, content.readableBytes());
        response.headers()
            .set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
        ctx.writeAndFlush(response)
            .addListener(ChannelFutureListener.CLOSE);
    }

    private static void sendRedirect(ChannelHandlerContext ctx, String location) {
        FullHttpResponse response = new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            HttpResponseStatus.MOVED_PERMANENTLY);
        response.headers()
            .set(HttpHeaders.Names.LOCATION, location);
        response.headers()
            .set(HttpHeaders.Names.CONTENT_LENGTH, 0);
        response.headers()
            .set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
        ctx.writeAndFlush(response)
            .addListener(ChannelFutureListener.CLOSE);
    }

    private static void sendJson(ChannelHandlerContext ctx, HttpResponseStatus status, String body) {
        ByteBuf content = Unpooled.copiedBuffer(body, CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
        response.headers()
            .set(HttpHeaders.Names.CONTENT_TYPE, CONTENT_TYPE_JSON);
        response.headers()
            .set(HttpHeaders.Names.CONTENT_LENGTH, content.readableBytes());
        response.headers()
            .set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
        ctx.writeAndFlush(response)
            .addListener(ChannelFutureListener.CLOSE);
    }

    private static ResolvedRoute resolveRoute(HttpPortProxyConfig config, String path) {
        HttpPortProxyConfig.Route directRoute = config.match(path);
        if (directRoute == null) {
            return null;
        }
        return new ResolvedRoute(directRoute, HttpPortProxyConfig.stripRoutePrefix(path, directRoute));
    }

    private static boolean shouldRedirectToMountedDirectory(String path, ResolvedRoute resolvedRoute) {
        return ("/" + resolvedRoute.route.path).equals(path);
    }

    private static String addTrailingSlash(String uri) {
        int queryIndex = uri.indexOf('?');
        if (queryIndex < 0) {
            return uri + "/";
        }
        return uri.substring(0, queryIndex) + "/" + uri.substring(queryIndex);
    }

    private static String rewriteLocationHeader(String location, HttpPortProxyConfig.Route route) {
        if (location == null || location.isEmpty()) {
            return location;
        }

        try {
            URI uri = URI.create(location);
            if (!uri.isAbsolute()) {
                if (location.startsWith("/")) {
                    return mountPath(route, location);
                }
                return mountPath(route, "/" + location);
            }

            String expectedHost = "127.0.0.1";
            if (expectedHost.equalsIgnoreCase(uri.getHost()) && uri.getPort() == route.targetPort) {
                String rewritten = mountPath(route, uri.getPath());
                if (uri.getQuery() != null && !uri.getQuery()
                    .isEmpty()) {
                    rewritten += "?" + uri.getQuery();
                }
                if (uri.getFragment() != null && !uri.getFragment()
                    .isEmpty()) {
                    rewritten += "#" + uri.getFragment();
                }
                return rewritten;
            }
        } catch (Exception e) {
            if (location.startsWith("/")) {
                return mountPath(route, location);
            }
        }

        return location;
    }

    private static String mountPath(HttpPortProxyConfig.Route route, String path) {
        String normalized = path == null ? "/" : path;
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        if (normalized.equals("/")) {
            return "/" + route.path + "/";
        }
        return "/" + route.path + normalized;
    }

    private static final class ProxyResponse {

        private final int statusCode;
        private final byte[] body;
        private final Map<String, String> headers;

        private ProxyResponse(int statusCode, byte[] body, Map<String, String> headers) {
            this.statusCode = statusCode;
            this.body = body;
            this.headers = headers;
        }
    }

    private static final class ResolvedRoute {

        private final HttpPortProxyConfig.Route route;
        private final String routeRelativePath;

        private ResolvedRoute(HttpPortProxyConfig.Route route, String routeRelativePath) {
            this.route = route;
            this.routeRelativePath = routeRelativePath;
        }
    }
}
