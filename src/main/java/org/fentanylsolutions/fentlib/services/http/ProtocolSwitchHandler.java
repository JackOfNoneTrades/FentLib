package org.fentanylsolutions.fentlib.services.http;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.minecraft.network.NetworkManager;

import org.fentanylsolutions.fentlib.FentLib;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;

/**
 * Protocol multiplexer installed as the first handler in the Netty pipeline
 * for each incoming connection on the Minecraft port. Peeks at the first
 * 2 bytes to determine the protocol.
 *
 * <ul>
 * <li>TLS detected: tears down the MC pipeline, installs TLS + HTTP handlers
 * (or closes when no HTTPS support is registered)</li>
 * <li>HTTP detected: tears down the MC pipeline, installs HTTP handlers</li>
 * <li>MC detected: removes itself, vanilla pipeline proceeds unchanged</li>
 * </ul>
 * <p>
 * Based on Netty's port unification example. The pipeline contents come from
 * {@link HttpPortUnification}.
 */
public class ProtocolSwitchHandler extends ByteToMessageDecoder {

    private static final int PEEK_BYTES = 2;

    private final List<?> networkManagers;

    /**
     * @param networkManagers the live NetworkSystem.networkManagers list,
     *                        so we can remove the NetworkManager when switching to HTTP
     */
    public ProtocolSwitchHandler(List<?> networkManagers) {
        this.networkManagers = networkManagers;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < PEEK_BYTES) {
            return;
        }

        int b1 = in.getUnsignedByte(in.readerIndex());
        int b2 = in.getUnsignedByte(in.readerIndex() + 1);

        if (isTls(b1, b2)) {
            if (!HttpPortUnification.isHttpsConfigured()) {
                FentLib.LOG.debug(
                    "TLS traffic detected from {} but same-port HTTPS is not enabled; closing",
                    ctx.channel()
                        .remoteAddress());
                ctx.close();
                return;
            }
            switchToHttp(ctx, true);
        } else if (isHttp(b1, b2)) {
            switchToHttp(ctx, false);
        } else {
            ctx.pipeline()
                .remove(this);
        }
    }

    /**
     * TLS records start with content type 0x16 (handshake), then a protocol
     * major version of 0x03 for SSLv3/TLS. Minecraft handshakes start with a
     * VarInt packet length, so this is distinct from vanilla traffic.
     */
    private static boolean isTls(int b1, int b2) {
        return b1 == 0x16 && b2 == 0x03;
    }

    /**
     * Detects HTTP by checking if the first two bytes match the start
     * of an HTTP method keyword. MC handshake packets start with a
     * VarInt length (typically 0x00-0x0F), so there is zero ambiguity.
     */
    private static boolean isHttp(int b1, int b2) {
        return (b1 == 'G' && b2 == 'E') // GET
            || (b1 == 'P' && b2 == 'O') // POST
            || (b1 == 'P' && b2 == 'U') // PUT
            || (b1 == 'D' && b2 == 'E') // DELETE
            || (b1 == 'H' && b2 == 'E') // HEAD
            || (b1 == 'O' && b2 == 'P') // OPTIONS
            || (b1 == 'P' && b2 == 'A'); // PATCH
    }

    /**
     * Tears down the MC pipeline and installs HTTP handlers.
     * <p>
     * Instead of removing handlers by hardcoded name (brittle),
     * we iterate the pipeline and remove everything except ourselves.
     * We also remove the NetworkManager from the networkManagers list
     * to prevent zombie entries in NetworkSystem.networkTick().
     */
    private void switchToHttp(ChannelHandlerContext ctx, boolean https) {
        FentLib.LOG.debug(
            "{} traffic detected from {}, switching pipeline",
            https ? "HTTPS" : "HTTP",
            ctx.channel()
                .remoteAddress());

        ChannelPipeline pipeline = ctx.pipeline();

        for (Map.Entry<String, ChannelHandler> entry : pipeline) {
            if (entry.getValue() instanceof NetworkManager) {
                synchronized (networkManagers) {
                    networkManagers.remove(entry.getValue());
                }
                break;
            }
        }

        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, ChannelHandler> entry : pipeline) {
            if (entry.getValue() != this) {
                toRemove.add(entry.getKey());
            }
        }
        for (String name : toRemove) {
            try {
                pipeline.remove(name);
            } catch (Exception e) {
                FentLib.LOG.warn("Failed to remove handler '{}' while switching to HTTP: {}", name, e.getMessage());
            }
        }

        pipeline.addLast(
            HttpPortUnification.TIMEOUT_HANDLER_NAME,
            new ReadTimeoutHandler(HttpPortUnification.getReadTimeoutSeconds()));
        if (https) {
            SslHandler sslHandler = new SslHandler(HttpPortUnification.newSslEngine());
            sslHandler.setHandshakeTimeout(HttpPortUnification.getTlsHandshakeTimeoutSeconds(), TimeUnit.SECONDS);
            pipeline.addLast(HttpPortUnification.SSL_HANDLER_NAME, sslHandler);
        }
        pipeline.addLast("http_codec", new HttpServerCodec());
        pipeline.addLast("http_aggregator", new HttpObjectAggregator(HttpPortUnification.getMaxContentLengthBytes()));
        pipeline.addLast("http_handler", HttpPortUnification.newRequestHandler());

        // Removing ourselves makes ByteToMessageDecoder.handlerRemoved() fire
        // the buffered bytes into the next handler.
        pipeline.remove(this);
    }
}
