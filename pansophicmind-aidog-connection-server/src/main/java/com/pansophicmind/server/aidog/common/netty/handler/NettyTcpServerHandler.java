package com.pansophicmind.server.aidog.common.netty.handler;

import cn.hutool.log.StaticLog;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class NettyTcpServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 保存连接到服务端的通道信息，对连接的客户端进行管理，包括连接的添加、删除、查找等操作。
     */
    private static final ConcurrentHashMap<ChannelId, ChannelHandlerContext> CHANNEL_MAP = new ConcurrentHashMap<>();


    /**
     * 当有客户端连接到服务器时，此方法会被触发。
     * 它会记录客户端的 IP 地址、端口号以及连接的 ChannelId，并将该连接添加到 CHANNEL_MAP 中。
     * 如果连接已经存在于 CHANNEL_MAP 中，会打印相应的日志信息；如果不存在，则添加到映射中并记录连接信息。
     *
     * @param ctx 通道处理器上下文，包含了通道的信息和操作通道的方法
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // 获取客户端的网络地址信息
        InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
        // 获取客户端的 IP 地址
        String clientIp = insocket.getAddress().getHostAddress();
        // 获取客户端的端口号
        int clientPort = insocket.getPort();
        // 获取连接通道的唯一标识
        ChannelId channelId = ctx.channel().id();

        // 如果该连接通道已经在映射中，打印连接状态信息
        if (CHANNEL_MAP.containsKey(channelId)) {
            log.info("客户端【" + channelId + "】是连接状态，连接通道数量: " + CHANNEL_MAP.size());
        } else {
            // 将新的连接添加到映射中
            CHANNEL_MAP.put(channelId, ctx);
            log.info("客户端【" + channelId + "】连接 netty 服务器[IP:" + clientIp + "--->PORT:" + clientPort + "]");
            log.info("连接通道数量: " + CHANNEL_MAP.size());
        }
    }


    /**
     * 当有客户端终止连接服务器时，此方法会被触发。
     * 它会从 CHANNEL_MAP 中移除该客户端的连接信息，并打印相应的退出信息和更新后的连接通道数量。
     * 首先检查该连接是否存在于 CHANNEL_MAP 中，如果存在则进行移除操作。
     *
     * @param ctx 通道处理器上下文
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIp = insocket.getAddress().getHostAddress();
        ChannelId channelId = ctx.channel().id();
        // 检查映射中是否包含该客户端连接
        if (CHANNEL_MAP.containsKey(channelId)) {
            // 从映射中移除连接
            CHANNEL_MAP.remove(channelId);
            log.info("客户端【" + channelId + "】退出 netty 服务器[IP:" + clientIp + "--->PORT:" + insocket.getPort() + "]");
            log.info("连接通道数量: " + CHANNEL_MAP.size());
        }
    }


    /**
     * 当有客户端向服务器发送消息时，此方法会被触发。
     * 它会打印接收到的客户端消息，并调用 channelWrite 方法将消息返回给客户端。
     * 首先会打印接收到客户端报文的日志信息，然后调用 channelWrite 方法进行响应。
     *
     * @param ctx 通道处理器上下文
     * @param msg 从客户端接收到的消息对象
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.info("加载客户端报文......");
        ByteBuf byteBuf = (ByteBuf) msg;
        String message = byteBuf.toString(Charset.forName("GBK"));
        log.info("当前连接数：" + CHANNEL_MAP.size() + "，当前通道ID:【" + ctx.channel().id() + "】" + " 接收到消息:" + message);
        // 下面可以解析数据，保存数据，生成返回报文，将需要返回报文写入 write 函数
        // 调用 channelWrite 方法将消息返回给客户端
        this.channelWrite(ctx.channel().id(), msg);
    }


    /**
     * 服务端给客户端发送消息的方法。
     * 首先根据传入的 ChannelId 从 CHANNEL_MAP 中获取对应的 ChannelHandlerContext，
     * 然后检查消息是否为空以及 ChannelHandlerContext 是否存在，若存在则将消息写入通道并刷新缓冲区。
     *
     * @param channelId 连接通道的唯一标识
     * @param msg       需要发送的消息内容
     */
    public void channelWrite(ChannelId channelId, Object msg) {
        // 获取与 ChannelId 对应的 ChannelHandlerContext
        ChannelHandlerContext ctx = CHANNEL_MAP.get(channelId);
        if (ctx == null) {
            log.info("通道【" + channelId + "】不存在");
            return;
        }
        if (msg == null || msg == "") {
            log.info("服务端响应空的消息");
            return;
        }
        // 将消息写入通道
        ctx.write(msg);
        // 刷新通道的输出缓冲区，确保消息被发送出去
        ctx.flush();
    }


    /**
     * 当触发用户事件时，此方法会被调用，主要用于处理空闲状态事件。
     * 根据不同的空闲状态（读、写、总超时）进行相应的处理，如断开连接。
     * 首先检查触发的事件是否是 IdleStateEvent，如果是则判断具体的空闲状态并进行相应处理。
     *
     * @param ctx 通道处理器上下文
     * @param evt 触发的事件对象
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        String socketString = ctx.channel().remoteAddress().toString();
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                log.info("Client: " + socketString + " READER_IDLE 读超时");
                // 读超时，断开连接
                ctx.disconnect();
            } else if (event.state() == IdleState.WRITER_IDLE) {
                log.info("Client: " + socketString + " WRITER_IDLE 写超时");
                // 写超时，断开连接
                ctx.disconnect();
            } else if (event.state() == IdleState.ALL_IDLE) {
                log.info("Client: " + socketString + " ALL_IDLE 总超时");
                // 总超时，断开连接
                ctx.disconnect();
            }
        }
    }


    /**
     * 当发生异常时，此方法会被触发。
     * 它会关闭通道并打印相应的错误信息，同时打印当前的连接通道数量。
     * 异常发生时，会关闭通道以防止资源泄漏，并且打印异常发生的通道信息和当前的连接数量。
     *
     * @param ctx   通道处理器上下文
     * @param cause 引发异常的原因
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
        log.info(ctx.channel().id() + " 发生了错误,此连接被关闭" + "此时连通数量: " + CHANNEL_MAP.size());
    }
}
