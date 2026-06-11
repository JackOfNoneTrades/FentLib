package org.fentanylsolutions.fentlib.services.http;

import java.util.function.Supplier;

import javax.net.ssl.SSLEngine;

import io.netty.channel.ChannelHandler;

/**
 * Registration point for HTTP unification on the Minecraft server port.
 * FentLib owns the protocol multiplexer and the HTTP(S) pipeline; a host mod
 * may replace the terminal request handler and enable HTTPS. When no handler
 * is registered, requests are served by {@link ReverseProxyHttpHandler}.
 */
public final class HttpPortUnification {

    public static final String TIMEOUT_HANDLER_NAME = "http_timeout";
    public static final String SSL_HANDLER_NAME = "https_ssl";

    private static final int DEFAULT_READ_TIMEOUT_SECONDS = 15;
    private static final int DEFAULT_MAX_CONTENT_LENGTH = 1024 * 1024;

    private static volatile Supplier<? extends ChannelHandler> requestHandlerFactory;
    private static volatile int readTimeoutSeconds = DEFAULT_READ_TIMEOUT_SECONDS;
    private static volatile int maxContentLengthBytes = DEFAULT_MAX_CONTENT_LENGTH;
    private static volatile Supplier<SSLEngine> sslEngineFactory;
    private static volatile int tlsHandshakeTimeoutSeconds = 10;

    private HttpPortUnification() {}

    /**
     * Replaces the terminal HTTP request handler. The factory is invoked once
     * per switched connection. The registered handler is expected to serve the
     * reverse proxy routes itself (via {@link ReverseProxyHttpHandler#tryHandle}).
     */
    public static void setRequestHandler(Supplier<? extends ChannelHandler> factory, int readTimeoutSecs,
        int maxContentLengthB) {
        requestHandlerFactory = factory;
        readTimeoutSeconds = readTimeoutSecs;
        maxContentLengthBytes = maxContentLengthB;
    }

    /** Enables same-port HTTPS. Without this, TLS connections are closed. */
    public static void setHttpsSupport(Supplier<SSLEngine> engineFactory, int handshakeTimeoutSecs) {
        sslEngineFactory = engineFactory;
        tlsHandshakeTimeoutSeconds = handshakeTimeoutSecs;
    }

    public static boolean shouldInstall() {
        return requestHandlerFactory != null || HttpPortProxyConfig.get()
            .hasRoutes();
    }

    static boolean isHttpsConfigured() {
        return sslEngineFactory != null;
    }

    static ChannelHandler newRequestHandler() {
        Supplier<? extends ChannelHandler> factory = requestHandlerFactory;
        return factory != null ? factory.get() : new ReverseProxyHttpHandler();
    }

    static SSLEngine newSslEngine() {
        return sslEngineFactory.get();
    }

    static int getReadTimeoutSeconds() {
        return readTimeoutSeconds;
    }

    static int getMaxContentLengthBytes() {
        return maxContentLengthBytes;
    }

    static int getTlsHandshakeTimeoutSeconds() {
        return tlsHandshakeTimeoutSeconds;
    }
}
