package com.pansophicmind.server.aidog.common.ws.server;

import com.pansophicmind.server.aidog.common.ws.service.BinaryMessageService;
import com.pansophicmind.server.aidog.common.ws.service.DeviceConnectionService;
import com.pansophicmind.server.aidog.common.ws.service.TextMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

// WebSocket 端点类
@Component
@ServerEndpoint("/connection")
@Slf4j
public class WebSocketEndpoint {

    private static DeviceConnectionService deviceConnectionService;

    @Autowired
    public void setDeviceConnectionManager(DeviceConnectionService deviceConnectionService) {
        WebSocketEndpoint.deviceConnectionService = deviceConnectionService;
    }

    @Value("${audioHandler.config.temp.path}")
    private static String tempPath;

    @Value("${audioHandler.config.temp.path}")
    public void setTempPath(String tempPath) {
        WebSocketEndpoint.tempPath = tempPath;
    }

    private static BinaryMessageService binaryMessageService;

    @Autowired
    public void setBinaryMessageService(BinaryMessageService binaryMessageService) {
        WebSocketEndpoint.binaryMessageService = binaryMessageService;
    }

    private static TextMessageService textMessageService;

    @Autowired
    public void setTextMessageService(TextMessageService textMessageService) {
        WebSocketEndpoint.textMessageService = textMessageService;
    }

    /**
     * Authorization: 用于存放访问令牌，形如 "Bearer <token>"
     * Protocol-Version: 固定示例中为 "1"
     * Device-Id: 设备物理网卡 MAC 地址
     * Client-Id: 设备 UUID（可在应用中唯一标识设备）
     *
     * @param session WebSocket 会话
     */
    @OnOpen
    public void onOpen(Session session) {
        deviceConnectionService.addConnection(session);
    }

    @OnMessage
    public void onMessage(String text, Session session) {
        textMessageService.handleTextMessage(session, text);
    }

    @OnMessage
    public void onMessage(byte[] audioStream, Session session) throws InterruptedException {
        log.info("Received audio stream of size: {}", audioStream.length);
        log.info("Received audio stream: {}", audioStream);
        if (session == null || !session.isOpen()) {
            return;
        }
        binaryMessageService.handleAudioStream(session, audioStream);
    }

    @OnClose
    public void onClose(Session session) {
        deviceConnectionService.removeConnection(session.getId());
    }




}
