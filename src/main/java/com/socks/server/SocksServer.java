package com.socks.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import static com.socks.server.ConfigLoader.SERVER_CONFIG;

/**
 * @author asyncio886(wuTao)
 * @date 2023/4/14
 */
@Slf4j
public class SocksServer {
    public static final InitHandler INIT_HANDLER = new InitHandler();
    public static void main(String[] args) {
        ServerBootstrap server = new ServerBootstrap();
        server.channel(NioServerSocketChannel.class);
        server.childHandler(INIT_HANDLER);
        server.option(ChannelOption.SO_TIMEOUT, 20);
        server.group(new NioEventLoopGroup(SERVER_CONFIG.getBossNum()),
                new NioEventLoopGroup(SERVER_CONFIG.getWorkerNum()));
        server.bind(SERVER_CONFIG.getPort()).addListener(future -> {
            if (future.isSuccess()) {
                log.info("server start in port " + SERVER_CONFIG.getPort());
            }
        });
    }
}
