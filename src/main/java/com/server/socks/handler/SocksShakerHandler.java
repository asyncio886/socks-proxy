package com.server.socks.handler;

import com.server.ConfigLoader;
import com.server.enums.Events;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.socksx.SocksVersion;
import io.netty.handler.codec.socksx.v4.Socks4CommandRequest;
import io.netty.handler.codec.socksx.v5.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author asyncio886(wuTao)
 * @date 2023/4/14
 */
@Slf4j
public class SocksShakerHandler extends ChannelInboundHandlerAdapter {
    public static final String NAME = "socks_shaker_handler";
    private SocksVersion version = null;
    private Socks5AuthMethod method = null;
    private final String _username = ConfigLoader.SERVER_CONFIG.getUsername();
    private final String _password = ConfigLoader.SERVER_CONFIG.getPassword();
    boolean usePassword = false;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Socks4CommandRequest request) {
            handlerSocks4Command(ctx, request);
        }
        if (msg instanceof Socks5InitialRequest request) {
            handlerSocks5InitRequest(ctx, request);
        }
        else if (msg instanceof Socks5PasswordAuthRequest passwordAuthRequest) {
            handlerPasswordRequest(ctx, passwordAuthRequest);
        }
        else if (msg instanceof Socks5CommandRequest) {
            String currentCtxName = ctx.name();
            ctx.pipeline().addAfter(currentCtxName, Socks5CommandHandler.NAME, new Socks5CommandHandler());
            ctx.fireChannelRead(msg);
            ctx.pipeline().remove(this);
        }
        else if (version == null) {
            ctx.channel().close();
        }
    }

    private void handlerSocks4Command(ChannelHandlerContext ctx, Socks4CommandRequest request) {
        version = request.version();
        ctx.pipeline().addAfter(NAME, Socks4CommandHandler.NAME, new Socks4CommandHandler());
        ctx.pipeline().remove(this);
        ctx.fireChannelRead(request);
    }

    private void handlerPasswordRequest(ChannelHandlerContext ctx, Socks5PasswordAuthRequest passwordAuthRequest) {
        if (method == null ||
                version == null ||
                !method.equals(Socks5AuthMethod.PASSWORD) ||
                !version.equals(passwordAuthRequest.version())) {
            ctx.channel().close();
            return;
        }
        String password = passwordAuthRequest.password();
        String username = passwordAuthRequest.username();
        Socks5PasswordAuthStatus status = null;
        if (_username.equals(username) && _password.equals(password)) {
            status = Socks5PasswordAuthStatus.SUCCESS;
        }
        else {
            status = Socks5PasswordAuthStatus.FAILURE;
        }
        Socks5PasswordAuthResponse response = new DefaultSocks5PasswordAuthResponse(status);
        ctx.channel().writeAndFlush(response);
    }

    private void handlerSocks5InitRequest(ChannelHandlerContext ctx, Socks5InitialRequest request) {
        if (version != null || method != null) {
            ctx.fireUserEventTriggered(Events.REPEAT_INIT);
            return;
        }
        version = SocksVersion.SOCKS5;
        List<Socks5AuthMethod> authMethods = request.authMethods();
        if (usePassword && authMethods.contains(Socks5AuthMethod.PASSWORD)) {
            method = Socks5AuthMethod.PASSWORD;
        }
        else {
            method = Socks5AuthMethod.NO_AUTH;
        }
        Socks5InitialResponse response = new DefaultSocks5InitialResponse(method);
        ctx.channel().writeAndFlush(response).addListener(future -> {
            if (future.isSuccess()) {
                ctx.pipeline().addBefore(ctx.name(), "socks5_command_decoder", new Socks5CommandRequestDecoder());
            }
        });
    }
}
