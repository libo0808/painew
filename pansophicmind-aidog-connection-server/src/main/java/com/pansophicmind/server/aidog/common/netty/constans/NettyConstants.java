package com.pansophicmind.server.aidog.common.netty.constans;

import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;

/**
 * netty channel 常量信息
 *
 * @author David
 */
public interface NettyConstants {

    /**
     * imei channel map
     */
    Map<String, ChannelHandlerContext> IMEI_CHANNEL_MAP = new HashMap<>();

    /**
     * channel_id imei map
     */
    Map<String, String> CHANNELID_IMEI_MAP = new HashMap<>();

    /**
     * tcp服务监听端口
     */
    Integer INET_PORT = 24203;

    /**
     * 读 心跳超时检测
     */
    Integer READER_IDLE_TIME = 1200;

    /**
     * 写 心跳超时检测
     */
    Integer WRITER_IDLE_TIME = 0;

    /**
     * 读写 心跳超时检测
     */
    Integer ALL_IDLE_TIME = 0;

    /**
     * 拆分的帧最多有多少字节 限制最大帧长度 1MB 1024*1024
     */
    Integer MAX_FRAME_LENGTH = 1048576;

    /**
     * 拆分后的帧是否去掉分隔符 默认值为true
     */
    Boolean STRIP_DELIMITER = false;

    /**
     * 帧分隔符
     */
    String DECODER_DELIMITER = "]";

    /**
     * 消息分隔符
     */
    String MESSAGE_DELIMITER = "\\*";

}
