package com.socks.server.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socksx.v4.DefaultSocks4CommandResponse;
import io.netty.handler.codec.socksx.v4.Socks4CommandRequest;
import io.netty.handler.codec.socksx.v4.Socks4CommandStatus;
import io.netty.handler.codec.socksx.v4.Socks4CommandType;
import lombok.extern.slf4j.Slf4j;

/**
 * @author asyncio886(wuTao)
 * @date 2023/4/14
 */
@Slf4j
public class Socks4CommandHandler extends ChannelInboundHandlerAdapter {
    public static final String NAME = "socks4_command_handler";
    private static final NioEventLoopGroup CLIENT_EVENT_LOOP = ConnectThreadPool.CLIENT_EVENT_LOOP;
    private ChannelFuture connectedFuture = null;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
        log.info(message.toString());
        if (message instanceof Socks4CommandRequest msg) {
            Socks4CommandType commandType = msg.type();
            if (commandType.equals(Socks4CommandType.CONNECT)) {
                doConnect(ctx, msg);
            }
            else if (commandType.equals(Socks4CommandType.BIND)) {
                doBind(ctx, msg);
            }
        }
        else {
            if (connectedFuture == null) {
                return;
            }
            connectedFuture.channel().writeAndFlush(message);
        }
    }

    private void doConnect(ChannelHandlerContext ctx, Socks4CommandRequest msg) {

        String dstAddr = msg.dstAddr();
        int port = msg.dstPort();
        Bootstrap client = new Bootstrap();
        client.channel(NioSocketChannel.class)
                .group(CLIENT_EVENT_LOOP)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new TransferRemoteToClientHandler(ctx));
                    }
                });
        ChannelFuture channelFuture = client.connect(dstAddr, port);
        connectedFuture = channelFuture;
        channelFuture.addListener(future -> {
            if (future.isSuccess()) {
                DefaultSocks4CommandResponse response = new DefaultSocks4CommandResponse(Socks4CommandStatus.SUCCESS);
                ctx.channel().writeAndFlush(response);
            }
            else {
                DefaultSocks4CommandResponse commandResponse = new DefaultSocks4CommandResponse(Socks4CommandStatus.IDENTD_UNREACHABLE);
                ctx.channel().writeAndFlush(commandResponse);
            }
        });
    }

    private void doBind(ChannelHandlerContext ctx, Socks4CommandRequest msg) {

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (connectedFuture != null) {
            connectedFuture.channel().close();
        }
    }
}
