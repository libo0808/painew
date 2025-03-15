package com.pansophicmind.server.aidog.common.ws.interf;

import cn.hutool.json.JSONObject;
import com.pansophicmind.server.aidog.common.ws.enums.TextMessageTypeEnum;

import javax.websocket.Session;

/**
 * 文本 JSON 消息（用于传输聊天状态、TTS/STT 事件、IoT 命令等）
 */
public interface TextMessageHandler {
    TextMessageTypeEnum getType();

    /**
     * 客户端发送至服务端
     *
     * @param message 文本消息
     */
    void clientToServer(Session session, JSONObject message);

    /**
     * 服务端发送至客户端
     *
     * @param message 文本消息
     */
    void serverToClient(Session session, JSONObject message);

}