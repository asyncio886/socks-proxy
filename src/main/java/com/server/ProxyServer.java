package com.server;

import com.server.http.HttpProxyServerServer;
import com.server.socks.InitHandler;
import com.server.socks.SocksProxyServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author asyncio886(wuTao)
 * @date 2023/4/14
 */
@Slf4j
public class ProxyServer {
    public static void main(String[] args) {
        startSocksServer();
        startDNSServer();
        startHttpServer();
    }

    private static void startSocksServer() {
        SocksProxyServer.startSocksServer();
    }

    private static void startDNSServer() {

    }

    private static void startHttpServer() {
        HttpProxyServerServer.startProxy();
    }
}
