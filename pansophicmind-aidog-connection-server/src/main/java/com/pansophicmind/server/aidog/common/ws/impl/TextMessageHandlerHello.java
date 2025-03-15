package com.pansophicmind.server.aidog.common.ws.impl;

import cn.hutool.json.JSONObject;
import com.pansophicmind.server.aidog.common.ws.enums.ClientStateEnum;
import com.pansophicmind.server.aidog.common.ws.enums.TextMessageTypeEnum;
import com.pansophicmind.server.aidog.common.ws.interf.TextMessageHandlerAbstract;
import com.pansophicmind.server.aidog.common.ws.service.DeviceConnectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.websocket.Session;

@Component
@Slf4j
public class TextMessageHandlerHello extends TextMessageHandlerAbstract {
    @Resource
    private DeviceConnectionService deviceConnectionService;

    protected TextMessageHandlerHello() {
        super(TextMessageTypeEnum.hello);
    }

    /**
     * {
     * "type": "hello",
     * "version": 1,
     * "transport": "websocket",
     * "audio_params": {
     * "format": "opus",
     * "sample_rate": 16000,
     * "channels": 1,
     * "frame_duration": 60
     * }
     * }
     *
     * @param message 文本消息
     */
    @Override
    public void clientToServer(Session session, JSONObject message) {
        //记录连接状态
        ClientStateEnum state = deviceConnectionService.getClientState(session.getId());
        if (state == ClientStateEnum.CONNECTING && "websocket".equals(message.getStr("transport"))) {
            deviceConnectionService.updateClientState(session.getId(), state);
            log.info("会话ID:[{}] 音频通道打开成功", session.getId());
            serverToClient(session, message);
        }
        //TODO 保存音频参数
    }

    /**
     * {
     * "type": "hello",
     * "transport": "websocket",
     * "audio_params": {
     * "sample_rate": 16000
     * }
     * }
     *
     * @param message 文本消息
     */
    @Override
    public void serverToClient(Session session, JSONObject message) {
        //回复客户端消息
        JSONObject helloMessage = new JSONObject();
        helloMessage.set("type", "hello");
        helloMessage.set("transport", "websocket");
        helloMessage.set("audio_params", new JSONObject().set("sample_rate", 16000));
        session.getAsyncRemote().sendText(helloMessage.toString());
    }


}
