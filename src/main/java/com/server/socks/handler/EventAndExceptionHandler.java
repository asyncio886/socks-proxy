package com.server.socks.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author asyncio886(wuTao)
 * @date 2023/4/14
 */
@Slf4j
public class EventAndExceptionHandler extends ChannelInboundHandlerAdapter {
    public static final String NAME = "event_exception_handler";
    AtomicInteger warningCount = new AtomicInteger(0);
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (warningCount.incrementAndGet() >= 5) {
            ctx.channel().close();
        }
    }

}
