package com.pansophicmind.server.aidog.common.utils;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.log.StaticLog;
import com.pansophicmind.server.aidog.common.netty.constans.NettyConstants;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Netty工具类
 *
 * @author David
 */
@Slf4j
public class NettyUtils {

    public static void sendMessage(String imei, String message) {
        try {
            ChannelHandlerContext channelHandlerContext = NettyConstants.IMEI_CHANNEL_MAP.get(imei);
            if (ObjectUtil.isNull(channelHandlerContext) || !channelHandlerContext.channel().isActive()) {
                log.error("服务端下发失败 原因===>设备离线 设备号===>{} 下发内容===>{}", imei, message);
                return;
            }
            channelHandlerContext.writeAndFlush(Unpooled.copiedBuffer(message, CharsetUtil.ISO_8859_1));
            log.info("服务端下发 设备号===>{} 下发内容===>{}", imei, message);
        } catch (Exception e) {
            log.error("服务端下发失败 原因===>{} 设备号===>{} 下发内容===>{}", e.getMessage(), imei, message);
        }
    }

}
