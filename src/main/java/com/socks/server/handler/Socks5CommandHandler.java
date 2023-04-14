package com.socks.server.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socksx.v5.*;
import lombok.extern.slf4j.Slf4j;

/**
 * @author asyncio886(wuTao)
 * @date 2023/4/14
 */
@Slf4j
public class Socks5CommandHandler extends ChannelInboundHandlerAdapter {
    public static final String NAME = "socks5_command_handler";
    private ChannelFuture connectedFuture = null;
    private static final NioEventLoopGroup CLIENT_EVENT_LOOP = ConnectThreadPool.CLIENT_EVENT_LOOP;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
        if (message instanceof Socks5CommandRequest msg) {
            Socks5CommandType commandType = msg.type();
            if (commandType.equals(Socks5CommandType.CONNECT)) {
                doConnect(ctx, msg);
            } else if (commandType.equals(Socks5CommandType.BIND)) {
                doBind(ctx, msg);
            }
        } else {
            if (connectedFuture == null) {
                return;
            }
            connectedFuture.addListener(future -> {
                if (future.isSuccess()) {
                    connectedFuture.channel().writeAndFlush(message);
                }
            });
        }
    }
    //TODO 代理bind模式
    private void doBind(ChannelHandlerContext ctx, Socks5CommandRequest msg) {

    }

    private void doConnect(ChannelHandlerContext ctx, Socks5CommandRequest msg) {
        ChannelFuture channelFuture = new Bootstrap().group(CLIENT_EVENT_LOOP)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new TransferRemoteToClientHandler(ctx));
                    }
                })
                .option(ChannelOption.SO_TIMEOUT, 20)
                .connect(msg.dstAddr(), msg.dstPort());
        connectedFuture = channelFuture;
        channelFuture.addListener(future -> {
            if (future.isSuccess()) {
                Socks5CommandResponse response = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, msg.dstAddrType());
                ctx.channel().writeAndFlush(response);
            } else {
                Socks5CommandResponse response = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, msg.dstAddrType());
                ctx.channel().writeAndFlush(response);
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (connectedFuture != null) {
            connectedFuture.channel().close();
        }
        super.channelInactive(ctx);
    }
}
