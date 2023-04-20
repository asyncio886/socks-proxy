package com.server.http;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.*;

/**
 * @author asyncio886(wuTao)
 * @date 2023/4/20
 */
public class DecodedHttpRequest implements FullHttpRequest {
    public static final int HTTP_PORT = 80;
    public static final int HTTPS_PORT = 443;
    private FullHttpRequest fullHttpRequest;
    private String host = null;
    private Integer port = null;
    private String decodedHost = null;

    public static DefaultFullHttpResponse connectSuccessResponse() {
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
    }

    public static DefaultFullHttpResponse identifiedError() {
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.PROXY_AUTHENTICATION_REQUIRED);
    }


    public int getPort() {
        if (port != null) {
            return port;
        }
        if (host == null) {
            throw new IllegalArgumentException("host 头不存在");
        }

        else {
            int index;
            if ((index = host.indexOf(":")) == -1) {
                port = HTTP_PORT;
            }
            else port = Integer.parseInt(host.substring(index + 1));
            return port;
        }
    }

    public DecodedHttpRequest(FullHttpRequest fullHttpRequest) {
        this.fullHttpRequest = fullHttpRequest;
        this.host = fullHttpRequest.headers().get(HttpHeaderNames.HOST);
    }


    public String getHostWithOutPort() {
        if (decodedHost != null) {
            return decodedHost;
        }
        if (host == null) {
            throw new IllegalArgumentException("host参数不存在");
        }
        int i;
        return (decodedHost = host.substring(0, (i = host.indexOf(":")) == -1 ? host.length() : i));
    }

    @Override
    public FullHttpRequest copy() {
        return fullHttpRequest.copy();
    }

    @Override
    public FullHttpRequest duplicate() {
        return fullHttpRequest.duplicate();
    }

    @Override
    public FullHttpRequest retainedDuplicate() {
        return fullHttpRequest.retainedDuplicate();
    }

    @Override
    public FullHttpRequest replace(ByteBuf content) {
        return fullHttpRequest.replace(content);
    }

    @Override
    public FullHttpRequest retain(int increment) {
        return fullHttpRequest.retain(increment);
    }

    @Override
    public FullHttpRequest retain() {
        return fullHttpRequest.retain();
    }

    @Override
    public FullHttpRequest touch() {
        return fullHttpRequest.touch();
    }

    @Override
    public FullHttpRequest touch(Object hint) {
        return fullHttpRequest.touch(hint);
    }

    @Override
    public FullHttpRequest setProtocolVersion(HttpVersion version) {
        return fullHttpRequest.setProtocolVersion(version);
    }

    @Override
    public FullHttpRequest setMethod(HttpMethod method) {
        return fullHttpRequest.setMethod(method);
    }

    @Override
    public FullHttpRequest setUri(String uri) {
        return fullHttpRequest.setUri(uri);
    }

    /**
     * @deprecated Use {@link #method()} instead.
     */
    @Override
    @Deprecated
    public HttpMethod getMethod() {
        return fullHttpRequest.getMethod();
    }

    /**
     * Returns the {@link HttpMethod} of this {@link HttpRequest}.
     *
     * @return The {@link HttpMethod} of this {@link HttpRequest}
     */
    @Override
    public HttpMethod method() {
        return fullHttpRequest.method();
    }

    /**
     * @deprecated Use {@link #uri()} instead.
     */
    @Override
    @Deprecated
    public String getUri() {
        return fullHttpRequest.getUri();
    }

    /**
     * Returns the requested URI (or alternatively, path)
     *
     * @return The URI being requested
     */
    @Override
    public String uri() {
        return fullHttpRequest.uri();
    }

    /**
     * @deprecated Use {@link #protocolVersion()} instead.
     */
    @Override
    @Deprecated
    public HttpVersion getProtocolVersion() {
        return fullHttpRequest.getProtocolVersion();
    }

    /**
     * Returns the protocol version of this {@link HttpMessage}
     */
    @Override
    public HttpVersion protocolVersion() {
        return fullHttpRequest.protocolVersion();
    }

    /**
     * Returns the headers of this message.
     */
    @Override
    public HttpHeaders headers() {
        return fullHttpRequest.headers();
    }

    /**
     * @deprecated Use {@link #decoderResult()} instead.
     */
    @Override
    @Deprecated
    public DecoderResult getDecoderResult() {
        return fullHttpRequest.getDecoderResult();
    }

    /**
     * Returns the result of decoding this object.
     */
    @Override
    public DecoderResult decoderResult() {
        return fullHttpRequest.decoderResult();
    }

    /**
     * Updates the result of decoding this object. This method is supposed to be invoked by a decoder.
     * Do not call this method unless you know what you are doing.
     * @param result
     */
    @Override
    public void setDecoderResult(DecoderResult result) {
        fullHttpRequest.setDecoderResult(result);
    }

    @Override
    public HttpHeaders trailingHeaders() {
        return fullHttpRequest.trailingHeaders();
    }

    @Override
    public ByteBuf content() {
        return fullHttpRequest.content();
    }

    /**
     * Returns the reference count of this object.  If {@code 0}, it means this object has been deallocated.
     */
    @Override
    public int refCnt() {
        return fullHttpRequest.refCnt();
    }

    /**
     * Decreases the reference count by {@code 1} and deallocates this object if the reference count reaches at
     * {@code 0}.
     *
     * @return {@code true} if and only if the reference count became {@code 0} and this object has been deallocated
     */
    @Override
    public boolean release() {
        return fullHttpRequest.release();
    }

    /**
     * Decreases the reference count by the specified {@code decrement} and deallocates this object if the reference
     * count reaches at {@code 0}.
     *
     * @return {@code true} if and only if the reference count became {@code 0} and this object has been deallocated
     * @param decrement
     */
    @Override
    public boolean release(int decrement) {
        return fullHttpRequest.release(decrement);
    }
}
