package com.server.common;

import com.server.ConfigLoader;
import io.netty.channel.nio.NioEventLoopGroup;

/**
 * @author asyncio886(wuTao)
 * @date 2023/4/14
 */
public class ConnectThreadPool {
    public static final NioEventLoopGroup CLIENT_EVENT_LOOP = new NioEventLoopGroup();
    public static final NioEventLoopGroup BOSS = new NioEventLoopGroup(ConfigLoader.SERVER_CONFIG.getBossNum());
    public static final NioEventLoopGroup WORKERS = new NioEventLoopGroup(ConfigLoader.SERVER_CONFIG.getWorkerNum());
}
