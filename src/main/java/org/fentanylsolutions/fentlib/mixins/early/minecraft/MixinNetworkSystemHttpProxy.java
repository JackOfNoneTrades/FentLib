package org.fentanylsolutions.fentlib.mixins.early.minecraft;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import net.minecraft.network.NetworkManager;
import net.minecraft.network.NetworkSystem;
import net.minecraft.network.PingResponseHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.NetHandlerHandshakeTCP;
import net.minecraft.util.MessageDeserializer;
import net.minecraft.util.MessageDeserializer2;
import net.minecraft.util.MessageSerializer;
import net.minecraft.util.MessageSerializer2;

import org.fentanylsolutions.fentlib.services.http.HttpPortProxyConfig;
import org.fentanylsolutions.fentlib.services.http.ProtocolSwitchHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

@Mixin(value = NetworkSystem.class, priority = 900)
public abstract class MixinNetworkSystemHttpProxy {

    @Shadow
    @Final
    private static NioEventLoopGroup eventLoops;

    @Shadow
    @Final
    private List endpoints;

    /**
     * @author FentLib
     * @reason Allow HTTP reverse proxying on the Minecraft server port.
     */
    @Overwrite
    public void addLanEndpoint(InetAddress address, int port) throws IOException {
        final NetworkSystem self = (NetworkSystem) (Object) this;
        final List networkManagers = ((AccessorNetworkSystem) self).getNetworkManagers();
        final MinecraftServer mcServer = ((AccessorNetworkSystem) self).getMcServer();
        final boolean enableHttpProxy = HttpPortProxyConfig.get()
            .hasRoutes();

        synchronized (this.endpoints) {
            this.endpoints.add(
                new ServerBootstrap().channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<Channel>() {

                        @Override
                        protected void initChannel(Channel channel) {
                            try {
                                channel.config()
                                    .setOption(ChannelOption.IP_TOS, 24);
                            } catch (ChannelException ignored) {}

                            try {
                                channel.config()
                                    .setOption(ChannelOption.TCP_NODELAY, false);
                            } catch (ChannelException ignored) {}

                            channel.pipeline()
                                .addLast("timeout", new ReadTimeoutHandler(FMLNetworkHandler.READ_TIMEOUT))
                                .addLast("legacy_query", new PingResponseHandler(self))
                                .addLast("splitter", new MessageDeserializer2())
                                .addLast("decoder", new MessageDeserializer(NetworkManager.field_152462_h))
                                .addLast("prepender", new MessageSerializer2())
                                .addLast("encoder", new MessageSerializer(NetworkManager.field_152462_h));

                            NetworkManager networkManager = new NetworkManager(false);
                            networkManagers.add(networkManager);
                            channel.pipeline()
                                .addLast("packet_handler", networkManager);
                            networkManager.setNetHandler(new NetHandlerHandshakeTCP(mcServer, networkManager));

                            if (enableHttpProxy) {
                                channel.pipeline()
                                    .addFirst("fentlib_protocol_switch", new ProtocolSwitchHandler(networkManagers));
                            }
                        }
                    })
                    .group(eventLoops)
                    .localAddress(address, port)
                    .bind()
                    .syncUninterruptibly());
        }
    }
}
