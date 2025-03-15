package com.pansophicmind.server.aidog.common.netty.service;

import io.netty.channel.ChannelHandlerContext;

/**
 * TCP服务 Service接口
 *
 * @author David
 */
public interface ITCPServerService {

    void handlerChannelRead(ChannelHandlerContext ctx, Object msg);

    void handlerChannelInactive(ChannelHandlerContext ctx);

    void handlerExceptionCaught(ChannelHandlerContext ctx);

    void handlerUserEventTriggered(ChannelHandlerContext ctx, Object evt);

}
