package com.server.socks;

import com.server.socks.handler.EventAndExceptionHandler;
import com.server.socks.handler.SocksShakerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socksx.SocksPortUnificationServerHandler;

/**
 * @author asyncio886(wuTao)
 * @date 2023/4/14
 */
public class InitHandler extends ChannelInitializer<NioSocketChannel> {

    @Override
    protected void initChannel(NioSocketChannel ch) throws Exception {
        ch.pipeline().addLast(new SocksPortUnificationServerHandler());
        ch.pipeline().addLast(SocksShakerHandler.NAME, new SocksShakerHandler());
        ch.pipeline().addLast(EventAndExceptionHandler.NAME, new EventAndExceptionHandler());
    }
}
