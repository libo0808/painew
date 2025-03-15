package com.pansophicmind.server.aidog.common.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

import java.util.concurrent.TimeUnit;

public class NettyTcpClientTest {
    private static final String SERVER_HOST = "112.74.96.220";
    private static final int SERVER_PORT = 7000;
    private static final int CONNECTION_COUNT = 10000;
    private static final String MESSAGE = "hello netty";
    private static final long SEND_INTERVAL = 10; // 发送消息的间隔时间，单位：秒

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
              .channel(NioSocketChannel.class)
              .handler(new ChannelInitializer<SocketChannel>() {
                  @Override
                  public void initChannel(SocketChannel ch) throws Exception {
                      ch.pipeline().addLast(new StringEncoder());
                      // 添加异常处理处理器
                      ch.pipeline().addLast(new ExceptionHandler());
                  }
              });

            for (int i = 0; i < CONNECTION_COUNT; i++) {
                final int connectionNumber = i + 1;
                b.connect(SERVER_HOST, SERVER_PORT).addListener(future -> {
                    if (future.isSuccess()) {
                        Channel channel = ((ChannelFuture) future).channel();
                        System.out.println("Connection " + connectionNumber + " established.");
                        startScheduledMessageSending(channel, connectionNumber);
                    } else {
                        System.err.println("Error connecting on connection " + connectionNumber + ": " + future.cause().getMessage());
                    }
                });
            }

            // 保持主线程不退出
            Thread.currentThread().join();
        } finally {
            group.shutdownGracefully();
        }
    }

    private static void startScheduledMessageSending(Channel channel, int connectionNumber) {
        channel.eventLoop().scheduleAtFixedRate(() -> {
            if (channel.isActive()) {
                channel.writeAndFlush(MESSAGE).addListener(writeFuture -> {
                    if (writeFuture.isSuccess()) {
                        System.out.println("Connection " + connectionNumber + " sent message: " + MESSAGE);
                    } else {
                        System.err.println("Error sending message on connection " + connectionNumber + ": " + writeFuture.cause().getMessage());
                        channel.close();
                    }
                });
            }
        }, 0, SEND_INTERVAL, TimeUnit.SECONDS);
    }

    // 异常处理处理器
    private static class ExceptionHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            System.err.println("Exception caught in pipeline: " + cause.getMessage());
            // 关闭连接
            ctx.close();
        }
    }
}