package com.pansophicmind.server.aidog.common.netty.server;

import com.pansophicmind.server.aidog.common.netty.handler.NettyTcpServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Netty TCP服务类
 */
@Slf4j
@Component
public class NettyTcpServer implements CommandLineRunner {

    /**
     * 端口号
     */
    private int port = 8972;

    @Override
    public void run(String... args) {
        // 接收连接
        EventLoopGroup boss = new NioEventLoopGroup();
        // 处理信息
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            // 定义server
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            // 添加分组
            serverBootstrap.group(boss, worker)
                    // 添加通道设置非阻塞
                    .channel(NioServerSocketChannel.class)
                    // 服务端可连接队列数量
                    .option(ChannelOption.SO_BACKLOG, 128)
                    // 开启长连接
                    .childOption(ChannelOption.SO_KEEPALIVE, Boolean.TRUE)
                    // 流程处理
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new NettyTcpServerHandler());
                        }
                    });
            // 绑定端口
            //ChannelFuture cf = serverBootstrap.bind(port).sync();
            // 绑定多个端口
            // 存储所有的ChannelFuture
            List<ChannelFuture> futures = new ArrayList<>();
            ChannelFuture future1 = serverBootstrap.bind(8972).sync();
            futures.add(future1);
            ChannelFuture future2 = serverBootstrap.bind(8971).sync();
            futures.add(future2);

            // 等待所有的Channel关闭
            for (ChannelFuture future : futures) {
                future.channel().closeFuture().sync();
            }

            // 优雅关闭连接
            //cf.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("连接错误:{}", e.getMessage());
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
