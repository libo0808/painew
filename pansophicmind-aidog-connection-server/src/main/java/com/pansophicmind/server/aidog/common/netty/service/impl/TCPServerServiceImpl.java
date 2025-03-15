package com.pansophicmind.server.aidog.common.netty.service.impl;

import com.pansophicmind.server.aidog.common.netty.service.ITCPServerService;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Service;

/**
 * TCP服务 Service业务层处理
 *
 * @author David
 */
@Service
public class TCPServerServiceImpl implements ITCPServerService {

    @Override
    public void handlerChannelRead(ChannelHandlerContext ctx, Object msg) {
    }
    @Override
    public void handlerChannelInactive(ChannelHandlerContext ctx) {
        handlerChannelClose(ctx);
    }

    @Override
    public void handlerExceptionCaught(ChannelHandlerContext ctx) {
        handlerChannelClose(ctx);
    }

    @Override
    public void handlerUserEventTriggered(ChannelHandlerContext ctx, Object evt) {
    }

    private void handlerChannelClose(ChannelHandlerContext ctx) {
    }
}
