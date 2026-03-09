package org.fentanylsolutions.fentlib.services.http;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.network.NetworkManager;

import org.fentanylsolutions.fentlib.FentLib;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.ReadTimeoutHandler;

public class ProtocolSwitchHandler extends ByteToMessageDecoder {

    private static final int PEEK_BYTES = 2;
    private static final int DEFAULT_MAX_CONTENT_LENGTH = 1024 * 1024;
    private static final int DEFAULT_TIMEOUT_SECONDS = 15;

    private final List<?> networkManagers;

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
        if (isHttp(b1, b2)) {
            switchToHttp(ctx);
            return;
        }

        ctx.pipeline()
            .remove(this);
    }

    private static boolean isHttp(int b1, int b2) {
        return (b1 == 'G' && b2 == 'E') || (b1 == 'P' && b2 == 'O')
            || (b1 == 'P' && b2 == 'U')
            || (b1 == 'D' && b2 == 'E')
            || (b1 == 'H' && b2 == 'E')
            || (b1 == 'O' && b2 == 'P')
            || (b1 == 'P' && b2 == 'A');
    }

    private void switchToHttp(ChannelHandlerContext ctx) {
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

        pipeline.addLast("http_timeout", new ReadTimeoutHandler(DEFAULT_TIMEOUT_SECONDS));
        pipeline.addLast("http_codec", new HttpServerCodec());
        pipeline.addLast("http_aggregator", new HttpObjectAggregator(DEFAULT_MAX_CONTENT_LENGTH));
        pipeline.addLast("http_proxy", new ReverseProxyHttpHandler());
        pipeline.remove(this);
    }
}
