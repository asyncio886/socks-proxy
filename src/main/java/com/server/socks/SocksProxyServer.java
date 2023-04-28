package com.server.socks;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

import static com.server.ConfigLoader.SERVER_CONFIG;
import static com.server.common.ConnectThreadPool.BOSS;
import static com.server.common.ConnectThreadPool.WORKERS;


/**
 * @author asyncio886(wuTao)
 * @date 2023/4/21
 */
@Slf4j
public class SocksProxyServer {
    private static final ServerBootstrap SOCKS_SERVER = new ServerBootstrap();
    public static final InitHandler INIT_HANDLER = new InitHandler();
    public static void startSocksServer() {
        SOCKS_SERVER.channel(NioServerSocketChannel.class);
        SOCKS_SERVER.childHandler(INIT_HANDLER);
        SOCKS_SERVER.option(ChannelOption.SO_TIMEOUT, 20);
        SOCKS_SERVER.group(BOSS, WORKERS);
        SOCKS_SERVER.bind(SERVER_CONFIG.getSocksPort())
                .addListener(future -> {
            if (future.isSuccess()) {
                log.info("socksServer start in " + SERVER_CONFIG.getSocksPort());
            }
            else {
                log.info("socks start error");
            }
        });
    }
}
