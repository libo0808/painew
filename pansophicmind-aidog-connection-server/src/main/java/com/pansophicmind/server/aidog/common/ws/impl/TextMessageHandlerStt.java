package com.pansophicmind.server.aidog.common.ws.impl;

import cn.hutool.json.JSONObject;
import com.pansophicmind.server.aidog.common.ws.enums.TextMessageTypeEnum;
import com.pansophicmind.server.aidog.common.ws.interf.TextMessageHandlerAbstract;
import org.springframework.stereotype.Component;

import javax.websocket.Session;

@Component
public class TextMessageHandlerStt extends TextMessageHandlerAbstract {
    protected TextMessageHandlerStt() {
        super(TextMessageTypeEnum.stt);
    }

    @Override
    public void clientToServer(Session session, JSONObject message) {

    }

    /**
     * {
     * "type": "stt",
     * "text": "用户说的话"
     * }
     *
     * @param message 文本消息
     */
    @Override
    public void serverToClient(Session session, JSONObject message) {
        message.set("type", TextMessageTypeEnum.stt);
        session.getAsyncRemote().sendText(message.toString());
    }


}
