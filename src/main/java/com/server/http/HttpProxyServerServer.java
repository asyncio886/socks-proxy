package com.server.http;

import com.server.common.ConnectThreadPool;
import com.server.http.DecodedHttpRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Base64;

import static com.server.ConfigLoader.SERVER_CONFIG;
import static com.server.common.ConnectThreadPool.BOSS;
import static com.server.common.ConnectThreadPool.WORKERS;

/**
 * @author asyncio886(wuTao)
 * @date 2023/4/20
 */
@Slf4j
public class HttpProxyServerServer {
    public static final NioEventLoopGroup CLIENT_LOOP = ConnectThreadPool.CLIENT_EVENT_LOOP;
    static final String SERVER_CODEC = "serverCodec";
    static final String AGGREGATOR_NAME = "aggregator";
    static final String CLIENT_CODEC = "clientCodec";
    public static void startProxy() {
        if (!SERVER_CONFIG.isStartHttpProxy()) {
            return;
        }
        ServerBootstrap server = new ServerBootstrap();
        server.group(BOSS, WORKERS);
        server.channel(NioServerSocketChannel.class);
        server.childHandler(new HttpProxyInitHandler());
        server.bind(SERVER_CONFIG.getHttpPort())
                .addListener(future -> {
                    if (future.isSuccess()) {
                        log.info("http proxy started on " + SERVER_CONFIG.getHttpPort());
                    }
                    else {
                        log.info("http proxy start error" );
                    }
                });
    }

    static class HttpProxyInitHandler extends ChannelInitializer<NioSocketChannel> {
        @Override
        protected void initChannel(NioSocketChannel ch) throws Exception {
            ch.pipeline().addLast(SERVER_CODEC, new HttpServerCodec());
            ch.pipeline().addLast(AGGREGATOR_NAME,new HttpObjectAggregator(65536));
            ch.pipeline().addLast(new HttpProxyHandler());
        }
    }

    static class HttpProxyHandler extends ChannelInboundHandlerAdapter {
        ChannelFuture connectFuture = null;
        String currentTarget = null;
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof FullHttpRequest request) {
                if (request.decoderResult().isFailure()) {
                    ctx.channel().close();
                    log.info("请求解析错误");
                }
                if (!isIdentified(request)) {
                    ctx.channel().writeAndFlush(DecodedHttpRequest.identifiedError())
                            .addListener(future -> ctx.channel().close());
                    return;
                }
                DecodedHttpRequest decodedHttpRequest = new DecodedHttpRequest(request);
                currentTarget = decodedHttpRequest.getHostWithOutPort() + ":" + decodedHttpRequest.getPort();
                log.info("http proxy connect " + currentTarget);
                connectFuture = new Bootstrap()
                        .group(CLIENT_LOOP)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<NioSocketChannel>() {
                            @Override
                            protected void initChannel(NioSocketChannel ch) throws Exception {
                                ch.pipeline().addLast(CLIENT_CODEC, new HttpClientCodec());
                                ch.pipeline().addLast(AGGREGATOR_NAME, new HttpObjectAggregator(65536));
                                ch.pipeline().addLast(new RemoteServerHandler(ctx, currentTarget));
                            }
                        }).connect(decodedHttpRequest.getHostWithOutPort(), decodedHttpRequest.getPort());
                connectFuture.addListener(future -> {
                   if (future.isSuccess()) {
                       if (decodedHttpRequest.method().equals(HttpMethod.CONNECT)) {
                           ctx.channel().writeAndFlush(DecodedHttpRequest.connectSuccessResponse())
                                           .addListener(f -> {
                                               if (f.isSuccess()) {
                                                   ctx.pipeline().remove(SERVER_CODEC);
                                                   ctx.pipeline().remove(AGGREGATOR_NAME);
                                                   connectFuture.channel().pipeline().remove(CLIENT_CODEC);
                                                   connectFuture.channel().pipeline().remove(AGGREGATOR_NAME);
                                               }
                                           });
                       }
                       else {
                           connectFuture.channel()
                                   .writeAndFlush(request)
                                   .addListener(f -> {
                                       if (f.isSuccess()) {
                                           ctx.pipeline().remove(SERVER_CODEC);
                                           ctx.pipeline().remove(AGGREGATOR_NAME);
                                           connectFuture.channel().pipeline().remove(CLIENT_CODEC);
                                           connectFuture.channel().pipeline().remove(AGGREGATOR_NAME);
                                       }
                                   });
                       }
                   }
                   else {
                       log.info("connect to " + decodedHttpRequest.getHostWithOutPort() + " unsuccessfully");
                   }
                });
            }
            else {
                if (connectFuture != null) {
                    connectFuture.channel().writeAndFlush(msg);
                }
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            if (connectFuture != null) {
                connectFuture.channel().close();
            }
        }


        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            log.error(cause.toString());
            ctx.channel().close();
            if (connectFuture != null) {
                connectFuture.channel().close();
            }
        }

        private boolean isIdentified(FullHttpRequest request) {
            log.info("headers is " + request.headers());
            if (!request.method().equals(HttpMethod.CONNECT)) {
                return true;
            }
            if (!SERVER_CONFIG.isUseHttpPassword()) {
                return true;
            }
            log.info("header is " + request.headers());
            String s ;
            s = request.headers().get(HttpHeaderNames.PROXY_AUTHORIZATION);
            if (null == s) {
                return false;
            }
            String[] splitStr = s.split(" ");
            if (splitStr.length != 2) {
                return false;
            }
            String template = new String(Base64.getDecoder().decode(splitStr[1]));
            String[] userAndPassword = template.split(":");
            if (userAndPassword.length != 2) {
                return false;
            }
            return userAndPassword[0].equals(SERVER_CONFIG.getHttpUsername())
                    && userAndPassword[1].equals(SERVER_CONFIG.getHttpPassword());
        }
    }

    static class RemoteServerHandler extends ChannelInboundHandlerAdapter {
        ChannelHandlerContext localContext;
        String currentTarget;
        public RemoteServerHandler(ChannelHandlerContext localContext, String currentTarget) {
            this.localContext = localContext;
            this.currentTarget = currentTarget;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            localContext.channel().writeAndFlush(msg);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            localContext.channel().close();
            log.info("disconnect from " + currentTarget);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            log.error(cause.getMessage());
            localContext.channel().close();
            ctx.channel().close();
        }
    }
}
