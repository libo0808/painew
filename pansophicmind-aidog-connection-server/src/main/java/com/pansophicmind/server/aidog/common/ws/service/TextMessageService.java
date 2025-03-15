package com.pansophicmind.server.aidog.common.ws.service;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.StaticLog;
import com.pansophicmind.server.aidog.common.ws.factory.TextMessageHandlerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.websocket.Session;

/**
 * 文本 JSON 消息（用于传输聊天状态、TTS/STT 事件、IoT 命令等）
 */
@Component
@Slf4j
public class TextMessageService {
    @Resource
    private TextMessageHandlerFactory textMessageHandlerFactory;

    public void handleTextMessage(Session session, String message) {
        JSONObject jsonMessage = JSONUtil.parseObj(message);
        log.info("会话ID:[{}] 收到消息:{}", session.getId(), jsonMessage);
        textMessageHandlerFactory.getHandler(jsonMessage.getStr("type")).clientToServer(session, jsonMessage);
    }


}