package com.pansophicmind.server.aidog.common.ws.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.log.StaticLog;
import com.pansophicmind.server.aidog.common.ws.enums.ClientStateEnum;
import com.pansophicmind.server.aidog.common.ws.enums.TextMessageTypeEnum;
import com.pansophicmind.server.aidog.common.ws.interf.TextMessageHandlerAbstract;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.Session;

@Component
@Slf4j
public class TextMessageHandlerListen extends TextMessageHandlerAbstract {
    protected TextMessageHandlerListen() {
        super(TextMessageTypeEnum.listen);
    }


    /**
     * {
     * "session_id": "",
     * "type": "listen",
     * "state": "start",
     * "mode": "auto"
     * }
     *
     * @param message 文本消息
     */
    @Override
    public void clientToServer(Session session, JSONObject message) {
        String state = message.getStr("state");
        // 监听状态下，更新客户端状态为 LISTENING
        deviceConnectionService.updateListenState(session.getId(), state);
        log.info("会话ID:[{}] 音频监听状态：[{}]", session.getId(), message.getStr("state"));
    }

    @Override
    public void serverToClient(Session session, JSONObject message) {

    }
}
