package com.socks.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author asyncio886(wuTao)
 * @date 2023/4/14
 */
@Slf4j
public class TransferRemoteToClientHandler extends ChannelInboundHandlerAdapter {

    ChannelHandlerContext clientCtx;

    public TransferRemoteToClientHandler(ChannelHandlerContext clientCtx) {
        this.clientCtx = clientCtx;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        clientCtx.channel().writeAndFlush(msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        clientCtx.channel().close();
        super.channelInactive(ctx);
    }
}
