package com.pansophicmind.server.aidog.common.ws.impl;

import cn.hutool.json.JSONObject;
import com.pansophicmind.server.aidog.common.ws.enums.TextMessageTypeEnum;
import com.pansophicmind.server.aidog.common.ws.enums.TtsStatsEnum;
import com.pansophicmind.server.aidog.common.ws.interf.TextMessageHandlerAbstract;
import org.springframework.stereotype.Component;

import javax.websocket.Session;

@Component
public class TextMessageHandlerTts extends TextMessageHandlerAbstract {
    protected TextMessageHandlerTts() {
        super(TextMessageTypeEnum.tts);
    }

    @Override
    public void clientToServer(Session session, JSONObject message) {

    }

    /**
     * {
     * "type": "tts",
     * "state": "start/stop"
     * }
     *
     * @param message 文本消息
     */
    @Override
    public void serverToClient(Session session, JSONObject message) {
        // 监听状态下，更新客户端TTS状态为 START
        deviceConnectionService.updateTtsState(session.getId(), TtsStatsEnum.START);
        //发送消息给客户端
        session.getAsyncRemote().sendText(message.toString());

    }


}
