package com.socks.server.handler;

import io.netty.channel.nio.NioEventLoopGroup;

/**
 * @author asyncio886(wuTao)
 * @date 2023/4/14
 */
public class ConnectThreadPool {
    public static final NioEventLoopGroup CLIENT_EVENT_LOOP = new NioEventLoopGroup();
}
